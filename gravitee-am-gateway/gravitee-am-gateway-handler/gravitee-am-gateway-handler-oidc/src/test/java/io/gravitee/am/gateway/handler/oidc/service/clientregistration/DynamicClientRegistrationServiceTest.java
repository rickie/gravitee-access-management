/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.gateway.handler.oidc.service.clientregistration;

import static io.gravitee.am.gateway.handler.oidc.service.clientregistration.impl.DynamicClientRegistrationServiceImpl.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.common.oidc.ClientAuthenticationMethod;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.gateway.handler.oidc.service.clientregistration.impl.ClientServiceImpl;
import io.gravitee.am.gateway.handler.oidc.service.clientregistration.impl.DynamicClientRegistrationServiceImpl;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDDiscoveryService;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDProviderMetadata;
import io.gravitee.am.gateway.handler.oidc.service.jwk.JWKService;
import io.gravitee.am.gateway.handler.oidc.service.jws.JWSService;
import io.gravitee.am.gateway.handler.oidc.service.utils.JWAlgorithmUtils;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.application.ApplicationScopeSettings;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.oidc.Keys;
import io.gravitee.am.model.oidc.OIDCSettings;
import io.gravitee.am.service.CertificateService;
import io.gravitee.am.service.EmailTemplateService;
import io.gravitee.am.service.FormService;
import io.gravitee.am.service.IdentityProviderService;
import io.gravitee.am.service.exception.InvalidClientMetadataException;
import io.gravitee.am.service.exception.InvalidRedirectUriException;




import io.reactivex.observers.TestObserver;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamicClientRegistrationServiceTest {

    public static final String DUMMY_JWKS_URI = "https://somewhere/jwks";
    public static final String DUMMY_REDIRECTURI = "https://redirecturi";
    @InjectMocks
    private DynamicClientRegistrationService dcrService = new DynamicClientRegistrationServiceImpl();

    @Mock
    private OpenIDDiscoveryService openIDDiscoveryService;

    @Mock
    private OpenIDProviderMetadata openIDProviderMetadata;

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private CertificateService certificateService;

    @Mock
    private JWKService jwkService;

    @Mock
    private JWSService jwsService;

    @Mock
    private JWTService jwtService;

    @Mock
    public WebClient webClient;

    @Mock
    public ClientService clientService;

    @Mock
    private Domain domain;

    @Mock
    private FormService formService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private Environment environment;

    private static final String DOMAIN_ID = "domain";
    private static final String BASE_PATH = "";
    private static final String ID_SOURCE = "123";
    private static final String ID_TARGET = "abc";

    @Before
    public void setUp() {
        reset(domain, environment);

        when(domain.getId()).thenReturn(DOMAIN_ID);
        when(identityProviderService.findByDomain_migrated(DOMAIN_ID)).thenReturn(Flux.empty());
        when(certificateService.findByDomain_migrated(DOMAIN_ID)).thenReturn(Flux.empty());
        when(openIDProviderMetadata.getRegistrationEndpoint()).thenReturn("https://issuer/register");
        when(openIDDiscoveryService.getConfiguration(BASE_PATH)).thenReturn(openIDProviderMetadata);
        when(openIDProviderMetadata.getIssuer()).thenReturn("https://issuer");
        when(jwtService.encode_migrated(any(JWT.class),any(Client.class))).thenReturn(Mono.just("jwt"));

        when(clientService.create_migrated(any())).thenAnswer(i -> {
            Client res = i.getArgument(0);
            res.setId(ID_TARGET);
            return Mono.just(res);
        });
        when(clientService.update_migrated(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(clientService.delete_migrated(any())).thenReturn(Mono.empty());
        when(clientService.renewClientSecret_migrated(any(), any())).thenAnswer(i -> {
            Client toRenew = new Client();
            toRenew.setClientSecret("secretRenewed");
            return Mono.just(toRenew);
        });

        when(domain.useFapiBrazilProfile()).thenReturn(false);
        when(environment.getProperty(OPENID_DCR_ACCESS_TOKEN_VALIDITY, Integer.class, Client.DEFAULT_ACCESS_TOKEN_VALIDITY_SECONDS)).thenReturn(Client.DEFAULT_REFRESH_TOKEN_VALIDITY_SECONDS);
        when(environment.getProperty(OPENID_DCR_ACCESS_TOKEN_VALIDITY, Integer.class, FAPI_OPENBANKING_BRAZIL_DEFAULT_ACCESS_TOKEN_VALIDITY)).thenReturn(FAPI_OPENBANKING_BRAZIL_DEFAULT_ACCESS_TOKEN_VALIDITY);
        when(environment.getProperty(OPENID_DCR_REFRESH_TOKEN_VALIDITY, Integer.class, Client.DEFAULT_REFRESH_TOKEN_VALIDITY_SECONDS)).thenReturn(1000);
        when(environment.getProperty(OPENID_DCR_ID_TOKEN_VALIDITY, Integer.class, Client.DEFAULT_ID_TOKEN_VALIDITY_SECONDS)).thenReturn(1100);
    }

    @Test
    public void create_nullRequest() {
        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(null, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("One of the Client Metadata value is invalid.");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_missingRedirectUri() {
        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(new DynamicClientRegistrationRequest(), BASE_PATH)).test();
        testObserver.assertError(InvalidRedirectUriException.class);
        testObserver.assertErrorMessage("Missing or invalid redirect_uris.");//redirect_uri metadata can be null but is mandatory
        testObserver.assertNotComplete();
    }

    @Test
    public void create_emptyRedirectUriArray() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.of(Arrays.asList()));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(client -> client.getRedirectUris().isEmpty());
    }

    @Test
    public void create_defaultCase() {
        String clientName = "name";

        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setClientName(Optional.of(clientName));
        request.setRedirectUris(Optional.empty());

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(client -> this.defaultAssertion(client) &&
                client.getClientName().equals(clientName) &&
                client.getIdentities() == null &&
                client.getCertificate() == null
        );
        verify(clientService, times(1)).create_migrated(any());
    }

    @Test
    public void create_applyDefaultIdentiyProvider() {
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        when(identityProvider.getId()).thenReturn("identity-provider-id-123");
        when(identityProviderService.findByDomain_migrated(DOMAIN_ID)).thenReturn(Flux.just(identityProvider));

        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> defaultAssertion(client) && client.getIdentities().contains("identity-provider-id-123"));
    }

    @Test
    public void create_applyDefaultCertificateProvider() {
        Certificate certificate = Mockito.mock(Certificate.class);
        when(certificate.getId()).thenReturn("certificate-id-123");
        when(certificateService.findByDomain_migrated(any())).thenReturn(Flux.just(certificate));

        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> defaultAssertion(client) && client.getCertificate().equals("certificate-id-123"));
    }

    @Test
    public void create_notAllowedScopes() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setScope(Optional.of("not allowed"));

        OIDCSettings oidc = OIDCSettings.defaultSettings();
        oidc.getClientRegistrationSettings().setAllowedScopesEnabled(true);
        oidc.getClientRegistrationSettings().setAllowedScopes(Arrays.asList("openid","profile"));
        when(domain.getOidc()).thenReturn(oidc);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        //scope is not allowed, so expecting to erase scope (no scope)
        testObserver.assertValue(client -> this.defaultAssertion(client) && client.getScopeSettings()==null);
    }

    @Test
    public void create_notAllowedScopes_defaultScopes() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setScope(Optional.of("not allowed"));

        OIDCSettings oidc = OIDCSettings.defaultSettings();
        oidc.getClientRegistrationSettings().setAllowedScopesEnabled(true);
        oidc.getClientRegistrationSettings().setAllowedScopes(Arrays.asList("openid","profile"));
        oidc.getClientRegistrationSettings().setDefaultScopes(Arrays.asList("phone","email"));
        when(domain.getOidc()).thenReturn(oidc);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> this.defaultAssertion(client) &&
                client.getScopeSettings().size() == 2 &&
                client.getScopeSettings().stream().map(ApplicationScopeSettings::getScope).collect(Collectors.toList())
                        .containsAll(Arrays.asList("phone","email"))
        );
    }

    @Test
    public void create_filteredScopes() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setScope(Optional.of("openid not allowed"));

        OIDCSettings oidc = OIDCSettings.defaultSettings();
        oidc.getClientRegistrationSettings().setAllowedScopesEnabled(true);
        oidc.getClientRegistrationSettings().setAllowedScopes(Arrays.asList("openid","profile"));
        when(domain.getOidc()).thenReturn(oidc);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> this.defaultAssertion(client) &&
                client.getScopeSettings().size() == 1 &&
                !client.getScopeSettings()
                        .stream().filter(setting -> setting.getScope().equalsIgnoreCase("openid")).findFirst().isEmpty()
        );
    }

    @Test
    public void create_defaultScopes_notUsed() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setScope(Optional.of("openid not allowed"));

        OIDCSettings oidc = OIDCSettings.defaultSettings();
        oidc.getClientRegistrationSettings().setDefaultScopes(Arrays.asList("phone","email"));
        when(domain.getOidc()).thenReturn(oidc);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> this.defaultAssertion(client) &&
                client.getScopeSettings().size() == 3 &&
                client.getScopeSettings().stream().map(ApplicationScopeSettings::getScope).collect(Collectors.toList())
                        .containsAll(Arrays.asList("openid","not","allowed"))
        );
    }

    @Test
    public void create_defaultScopes() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());

        OIDCSettings oidc = OIDCSettings.defaultSettings();
        oidc.getClientRegistrationSettings().setDefaultScopes(Arrays.asList("phone","email"));
        when(domain.getOidc()).thenReturn(oidc);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> this.defaultAssertion(client) &&
                client.getScopeSettings().size() == 2 &&
                client.getScopeSettings().stream().map(ApplicationScopeSettings::getScope).collect(Collectors.toList())
                        .containsAll(Arrays.asList("phone","email"))
        );
    }

    @Test
    public void create_emptyResponseTypePayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setResponseTypes(Optional.empty());

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> client.getResponseTypes()==null);
    }

    @Test
    public void create_unknownResponseTypePayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setResponseTypes(Optional.of(Arrays.asList("unknownResponseType")));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Invalid response type.");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_unknownGrantTypePayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setGrantTypes(Optional.of(Arrays.asList("unknownGrantType")));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Missing or invalid grant type.");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_unsupportedSubjectTypePayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setSubjectType(Optional.of("unknownSubjectType"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported subject type");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_unsupportedUserinfoSigningAlgorithmPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setUserinfoSignedResponseAlg(Optional.of("unknownSigningAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported userinfo signing algorithm");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_unsupportedUserinfoResponseAlgPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setUserinfoEncryptedResponseAlg(Optional.of("unknownEncryptionAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported userinfo_encrypted_response_alg value");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_missingUserinfoResponseAlgPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setUserinfoEncryptedResponseEnc(Optional.of("unknownEncryptionAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("When userinfo_encrypted_response_enc is included, userinfo_encrypted_response_alg MUST also be provided");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_unsupportedUserinfoResponseEncPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setUserinfoEncryptedResponseAlg(Optional.of("RSA-OAEP-256"));
        request.setUserinfoEncryptedResponseEnc(Optional.of("unknownEncryptionAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported userinfo_encrypted_response_enc value");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_defaultUserinfoResponseEncPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setUserinfoEncryptedResponseAlg(Optional.of("RSA-OAEP-256"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> defaultAssertion(client) && client.getUserinfoEncryptedResponseEnc()!=null);
    }

    @Test
    public void create_unsupportedIdTokenSigningAlgorithmPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setIdTokenSignedResponseAlg(Optional.of("unknownSigningAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported id_token signing algorithm");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_unsupportedIdTokenResponseAlgPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setIdTokenEncryptedResponseAlg(Optional.of("unknownEncryptionAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported id_token_encrypted_response_alg value");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_missingIdTokenResponseAlgPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setIdTokenEncryptedResponseEnc(Optional.of("unknownEncryptionAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("When id_token_encrypted_response_enc is included, id_token_encrypted_response_alg MUST also be provided");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_unsupportedIdTokenResponseEncPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setIdTokenEncryptedResponseAlg(Optional.of("RSA-OAEP-256"));
        request.setIdTokenEncryptedResponseEnc(Optional.of("unknownEncryptionAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported id_token_encrypted_response_enc value");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_defaultIdTokenResponseEncPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setIdTokenEncryptedResponseAlg(Optional.of("RSA-OAEP-256"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> defaultAssertion(client) && client.getIdTokenEncryptedResponseEnc()!=null);
    }

    @Test
    public void create_invalidRequestUris() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequestUris(Optional.of(Arrays.asList("nonValidUri")));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertNotComplete();
        assertTrue("Should have only one exception", testObserver.errorCount()==1);
        assertTrue("Unexpected start of error message", testObserver.errors().get(0).getMessage().startsWith("request_uris:"));
    }

    @Test
    public void create_invalidTlsParameters_noTlsField() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setTokenEndpointAuthMethod(Optional.of(ClientAuthenticationMethod.TLS_CLIENT_AUTH));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertNotComplete();
        assertTrue("Should have only one exception", testObserver.errorCount()==1);
        assertTrue("Unexpected start of error message", testObserver.errors().get(0).getMessage().equals("Missing TLS parameter for tls_client_auth."));
    }

    @Test
    public void create_invalidTlsParameters_multipleTlsField() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setTokenEndpointAuthMethod(Optional.of(ClientAuthenticationMethod.TLS_CLIENT_AUTH));
        request.setTlsClientAuthSubjectDn(Optional.of("subject-dn"));
        request.setTlsClientAuthSanDns(Optional.of("san-dns"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertNotComplete();
        assertTrue("Should have only one exception", testObserver.errorCount()==1);
        assertTrue("Unexpected start of error message", testObserver.errors().get(0).getMessage().equals("The tls_client_auth must use exactly one of the TLS parameters."));
    }

    @Test
    public void create_invalidSelfSignedClient_noJWKS() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setTokenEndpointAuthMethod(Optional.of(ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertNotComplete();
        assertTrue("Should have only one exception", testObserver.errorCount()==1);
        assertTrue("Unexpected start of error message", testObserver.errors().get(0).getMessage().equals("The self_signed_tls_client_auth requires at least a jwks or a valid jwks_uri."));
    }

    @Test
    public void create_validSelfSignedClient() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setJwks(Optional.of(new Keys()));
        request.setTokenEndpointAuthMethod(Optional.of(ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
    }

    @Test
    public void create_validRequestUris() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequestUris(Optional.of(Arrays.asList("https://valid/request/uri")));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> defaultAssertion(client) && client.getRequestUris().contains("https://valid/request/uri"));
    }


    @Test
    public void create_sectorIdentifierUriBadFormat() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setSectorIdentifierUri(Optional.of("blabla"));//fail due to invalid url

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertNotComplete();
        assertTrue("Should have only one exception", testObserver.errorCount()==1);
        assertTrue("Unexpected start of error message", testObserver.errors().get(0).getMessage().startsWith("sector_identifier_uri:"));
    }

    @Test
    public void create_sectorIdentifierUriNottHttps() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setSectorIdentifierUri(Optional.of("http://something"));//fail due to invalid url

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertNotComplete();
        assertTrue("Should have only one exception", testObserver.errorCount()==1);
        assertTrue("Unexpected start of error message", testObserver.errors().get(0).getMessage().startsWith("Scheme must be https for sector_identifier_uri"));
    }

    @Test
    public void create_sectorIdentifierUriBadRequest() {
        final String sectorUri = "https://sector/uri";
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setSectorIdentifierUri(Optional.of(sectorUri));//fail due to invalid url
        HttpRequest<Buffer> httpRequest = Mockito.mock(HttpRequest.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);

        when(webClient.getAbs(sectorUri)).thenReturn(httpRequest);
        when(httpRequest.rxSend()).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(httpResponse)));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertNotComplete();
        assertTrue("Should have only one exception", testObserver.errorCount()==1);
        assertTrue("Unexpected start of error message", testObserver.errors().get(0).getMessage().startsWith("Unable to parse sector_identifier_uri"));
    }

    @Test
    public void create_sectorIdentifierUri_invalidRedirectUri() {
        final String sectorUri = "https://sector/uri";
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.of(Arrays.asList("https://graviee.io/callback")));
        request.setSectorIdentifierUri(Optional.of(sectorUri));//fail due to invalid url
        HttpRequest<Buffer> httpRequest = Mockito.mock(HttpRequest.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);

        when(webClient.getAbs(sectorUri)).thenReturn(httpRequest);
        when(httpRequest.rxSend()).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(httpResponse)));
        when(httpResponse.bodyAsString()).thenReturn("[\"https://not/same/redirect/uri\"]");

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidRedirectUriException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void create_sectorIdentifierUri_validRedirectUri() {
        final String redirectUri = "https://graviee.io/callback";
        final String sectorUri = "https://sector/uri";
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.of(Arrays.asList(redirectUri)));
        request.setSectorIdentifierUri(Optional.of(sectorUri));
        HttpRequest<Buffer> httpRequest = Mockito.mock(HttpRequest.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);

        when(webClient.getAbs(sectorUri)).thenReturn(httpRequest);
        when(httpRequest.rxSend()).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(httpResponse)));
        when(httpResponse.bodyAsString()).thenReturn("[\""+redirectUri+"\"]");

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
    }

    @Test
    public void create_validateJWKsDuplicatedSource() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setJwks(Optional.of(new Keys()));
        request.setJwksUri(Optional.of("something"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("The jwks_uri and jwks parameters MUST NOT be used together.");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_validateJWKsUriWithoutJwkSet() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setJwksUri(Optional.of("something"));

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.empty());

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("No JWK found behind jws uri...");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_validateJWKsUriOk() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setJwksUri(Optional.of("something"));

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> defaultAssertion(client) && client.getJwksUri().equals("something"));
    }

    @Test
    public void create_client_credentials() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setGrantTypes(Optional.of(Arrays.asList("client_credentials")));
        request.setResponseTypes(Optional.of(Arrays.asList()));
        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> client.getRedirectUris()==null &&
                client.getAuthorizedGrantTypes().size()==1 &&
                client.getAuthorizedGrantTypes().contains("client_credentials") &&
                client.getResponseTypes().isEmpty()
        );
    }

    @Test
    public void create_implicit() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.of(Arrays.asList("https://graviee.io/callback")));
        request.setGrantTypes(Optional.of(Arrays.asList("implicit")));
        request.setResponseTypes(Optional.of(Arrays.asList("token")));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> client.getRedirectUris().contains("https://graviee.io/callback") &&
                client.getAuthorizedGrantTypes().size() == 1 &&
                client.getAuthorizedGrantTypes().contains("implicit") &&
                client.getResponseTypes().size() == 1 &&
                client.getResponseTypes().contains("token")
        );
    }

    private boolean defaultAssertion(Client client) {
        assertNotNull("Client is null",client);
        assertNotNull("Client id is null", client.getClientId());

        assertNull("expecting no redirect_uris", client.getRedirectUris());

        assertEquals("Domain is wrong",DOMAIN_ID,client.getDomain());
        assertEquals("registration uri is wrong", "https://issuer/register"+"/"+client.getClientId(), client.getRegistrationClientUri());
        assertEquals("registration token is wrong", "jwt", client.getRegistrationAccessToken());
        assertEquals("should be default value \"web\"", "web", client.getApplicationType());

        assertTrue("should be default value \"code\"", client.getResponseTypes().size()==1 && client.getResponseTypes().contains("code"));
        return true;
    }

    @Test
    public void patch_noRedirectUriMetadata() {
        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.patch_migrated(new Client(), new DynamicClientRegistrationRequest(), BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        verify(clientService, times(1)).update_migrated(any());
    }

    @Test
    public void patch() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.of(Arrays.asList("https://graviee.io/callback")));
        request.setJwksUri(Optional.of("something"));

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.patch_migrated(new Client(), request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> client.getJwksUri().equals("something") && client.getRedirectUris().size()==1);
        verify(clientService, times(1)).update_migrated(any());
    }

    @Test
    public void update_missingRedirectUri() {
        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.update_migrated(new Client(), new DynamicClientRegistrationRequest(), BASE_PATH)).test();
        testObserver.assertError(InvalidRedirectUriException.class);
        testObserver.assertErrorMessage("Missing or invalid redirect_uris.");//redirect_uri metadata can be null but is mandatory
        testObserver.assertNotComplete();
    }

    @Test
    public void update() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.of(Arrays.asList()));
        request.setApplicationType(Optional.of("something"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.update_migrated(new Client(), request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> client.getApplicationType().equals("something") && client.getRedirectUris().isEmpty());
        verify(clientService, times(1)).update_migrated(any());
    }

    @Test
    public void delete() {
        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.delete_migrated(new Client())).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        verify(clientService, times(1)).delete_migrated(any());
    }

    @Test
    public void renewSecret() {
        Client toRenew = new Client();
        toRenew.setId("id");
        toRenew.setDomain("domain_id");
        toRenew.setClientId("client_id");
        toRenew.setClientSecret("oldSecret");

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.renewSecret_migrated(toRenew, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> client.getClientSecret().equals("secretRenewed"));
        verify(clientService, times(1)).renewClientSecret_migrated(anyString(), anyString());
        verify(clientService, times(1)).update_migrated(any());
    }

    @Test
    public void createFromTemplate_templateNotFound() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setSoftwareId(Optional.of("123"));

        when(domain.isDynamicClientRegistrationTemplateEnabled()).thenReturn(true);
        when(clientService.findById_migrated(any())).thenReturn(Mono.empty());

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNotComplete();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("No template found for software_id 123");
        verify(clientService, times(0)).create_migrated(any());
    }

    @Test
    public void createFromTemplate_isNotTemplate() {
        Client template = new Client();
        template.setId("123");
        template.setClientName("shouldBeRemoved");
        template.setClientId("shouldBeReplaced");
        template.setClientSecret("shouldBeRemoved");
        template.setRedirectUris(Arrays.asList("shouldBeRemoved"));
        template.setSectorIdentifierUri("shouldBeRemoved");
        template.setJwks(new Keys());

        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setSoftwareId(Optional.of("123"));
        request.setApplicationType(Optional.of("app"));

        when(domain.isDynamicClientRegistrationTemplateEnabled()).thenReturn(true);
        when(clientService.findById_migrated("123")).thenReturn(Mono.just(template));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNotComplete();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Client behind software_id is not a template");
        verify(clientService, times(0)).create_migrated(any());
    }

    @Test
    public void createFromTemplate() {
        Client template = new Client();
        template.setId(ID_SOURCE);
        template.setClientName("shouldBeRemoved");
        template.setClientId("shouldBeReplaced");
        template.setClientSecret("shouldBeRemoved");
        template.setRedirectUris(Arrays.asList("shouldBeRemoved"));
        template.setSectorIdentifierUri("shouldBeRemoved");
        template.setJwks(new Keys());
        template.setTemplate(true);

        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setSoftwareId(Optional.of(ID_SOURCE));
        request.setApplicationType(Optional.of("app"));

        when(formService.copyFromClient_migrated(DOMAIN_ID, ID_SOURCE, ID_TARGET)).thenReturn(Mono.just(Collections.emptyList()));
        when(emailTemplateService.copyFromClient_migrated(DOMAIN_ID, ID_SOURCE, ID_TARGET)).thenReturn(Flux.empty());
        when(domain.isDynamicClientRegistrationTemplateEnabled()).thenReturn(true);
        when(clientService.findById_migrated("123")).thenReturn(Mono.just(template));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(client ->
                client.getId().equals("abc") &&
                client.getApplicationType().equals("app") &&
                client.getClientId() != null &&
                !client.getClientId().equals("shouldBeReplaced") &&
                client.getRedirectUris() == null &&
                client.getClientName().equals(ClientServiceImpl.DEFAULT_CLIENT_NAME) &&
                client.getClientSecret() == null &&
                client.getJwks() == null &&
                client.getSectorIdentifierUri() == null
        );
        verify(clientService, times(1)).create_migrated(any());
    }

    @Test
    public void create_unsupportedAuthorizationSigningAlgorithmPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setAuthorizationSignedResponseAlg(Optional.of("unknownSigningAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported authorization signing algorithm");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_undefinedAuthorizationSigningAlgorithmPayload() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
        testObserver.assertValue(client -> client.getAuthorizationSignedResponseAlg().equals(JWSAlgorithm.RS256.getName()));
        verify(clientService, times(1)).create_migrated(any());
    }

    @Test
    public void create_unsupportedRequestObjectSigningAlg() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequestObjectSigningAlg(Optional.of("unknownSigningAlg"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported request object signing algorithm");
        testObserver.assertNotComplete();
    }

    @Test
    public void fapi_create_unsupportedRequestObjectSigningAlg() {
        when(domain.usePlainFapiProfile()).thenReturn(true);
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequestObjectSigningAlg(Optional.of(JWSAlgorithm.RS256.getName()));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("request_object_signing_alg shall be PS256");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_missingRequestObjectEncryptionAlgorithm() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequestObjectEncryptionAlg(null);
        request.setRequestObjectEncryptionEnc(Optional.of(JWAlgorithmUtils.getDefaultRequestObjectEnc()));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("When request_object_encryption_enc is included, request_object_encryption_alg MUST also be provided");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_unsupportedRequestObjectEncryptionAlgorithm() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequestObjectEncryptionAlg(Optional.of("unknownKeyAlg"));
        request.setRequestObjectEncryptionEnc(Optional.of(JWAlgorithmUtils.getDefaultRequestObjectEnc()));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported request_object_encryption_alg value");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_supportRequestObjectEncryptionAlgorithm() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP_256.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(JWAlgorithmUtils.getDefaultRequestObjectEnc()));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();
    }

    @Test
    public void create_unsupportedRequestObjectContentEncryptionAlgorithm() {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP_256.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of("unsupported"));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Unsupported request_object_encryption_enc value");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_unsupportRequestObjectEncryptionAlgorithm_FapiBrazil() {
        when(domain.useFapiBrazilProfile()).thenReturn(true);

        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP_256.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(JWAlgorithmUtils.getDefaultRequestObjectEnc()));

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Request object must be encrypted using RSA-OAEP with A256GCM");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_noOpenBanking_JWKS_URI_FapiBrazil() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));
        request.setSoftwareStatement(Optional.of("jws"));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(null);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("No jwks_uri for OpenBanking Directory, unable to validate software_statement");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_SoftwareStatement_invalidSignatureAlg() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.RS256, Instant.now())));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("software_statement isn't signed or doesn't use PS256");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_SoftwareStatement_invalidSignature() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.PS256, Instant.now().minus(6, ChronoUnit.MINUTES))));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));
        when(jwkService.getKey_migrated(any(), any())).thenReturn(Mono.just(new io.gravitee.am.model.jose.RSAKey()));
        when(jwsService.isValidSignature(any(), any())).thenReturn(false);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("Invalid signature for software_statement");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_SoftwareStatement_iatToOld() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.PS256, Instant.now().minus(6, ChronoUnit.MINUTES))));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));
        when(jwkService.getKey_migrated(any(), any())).thenReturn(Mono.just(new io.gravitee.am.model.jose.RSAKey()));
        when(jwsService.isValidSignature(any(), any())).thenReturn(true);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("software_statement older than 5 minutes");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_SoftwareStatement_jwks_forbidden() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));
        final Keys jwkSet = new Keys();
        jwkSet.setKeys(Arrays.asList(new io.gravitee.am.model.jose.RSAKey()));
        request.setJwks(Optional.of(jwkSet));

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.PS256, Instant.now())));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));
        when(jwkService.getKey_migrated(any(), any())).thenReturn(Mono.just(new io.gravitee.am.model.jose.RSAKey()));
        when(jwsService.isValidSignature(any(), any())).thenReturn(true);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("jwks is forbidden, prefer jwks_uri");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_SoftwareStatement_missing_jwks_uri() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.PS256, Instant.now())));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));
        when(jwkService.getKey_migrated(any(), any())).thenReturn(Mono.just(new io.gravitee.am.model.jose.RSAKey()));
        when(jwsService.isValidSignature(any(), any())).thenReturn(true);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("jwks_uri is required");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_SoftwareStatement_invalid_jwks_uri() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));
        request.setJwksUri(Optional.of("https://invalid"));

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.PS256, Instant.now())));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));
        when(jwkService.getKey_migrated(any(), any())).thenReturn(Mono.just(new io.gravitee.am.model.jose.RSAKey()));
        when(jwsService.isValidSignature(any(), any())).thenReturn(true);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("jwks_uri doesn't match the software_jwks_uri");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_SoftwareStatement_missing_redirect_uris() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));
        request.setJwksUri(Optional.of(DUMMY_JWKS_URI));

        request.setRedirectUris(Optional.empty());

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.PS256, Instant.now())));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));
        when(jwkService.getKey_migrated(any(), any())).thenReturn(Mono.just(new io.gravitee.am.model.jose.RSAKey()));
        when(jwsService.isValidSignature(any(), any())).thenReturn(true);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("redirect_uris are missing");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_SoftwareStatement_invalid_redirect_uris() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));
        request.setJwksUri(Optional.of(DUMMY_JWKS_URI));

        request.setRedirectUris(Optional.of(Arrays.asList("https://invalid")));

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.PS256, Instant.now())));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));
        when(jwkService.getKey_migrated(any(), any())).thenReturn(Mono.just(new io.gravitee.am.model.jose.RSAKey()));
        when(jwsService.isValidSignature(any(), any())).thenReturn(true);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("redirect_uris contains unknown uri from software_statement");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_tlsClientAuth_missingDN() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));
        request.setJwksUri(Optional.of(DUMMY_JWKS_URI));
        request.setRedirectUris(Optional.of(Arrays.asList(DUMMY_REDIRECTURI)));

        request.setTokenEndpointAuthMethod(Optional.of(ClientAuthenticationMethod.TLS_CLIENT_AUTH));
        request.setTlsClientAuthSanEmail(Optional.of("email@domain.net"));
        request.setTlsClientAuthSubjectDn(Optional.empty());
        request.setTlsClientAuthSanDns(Optional.empty());
        request.setTlsClientAuthSanIp(Optional.empty());
        request.setTlsClientAuthSanUri(Optional.empty());

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.PS256, Instant.now())));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));
        when(jwkService.getKey_migrated(any(), any())).thenReturn(Mono.just(new io.gravitee.am.model.jose.RSAKey()));
        when(jwsService.isValidSignature(any(), any())).thenReturn(true);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertErrorMessage("tls_client_auth_subject_dn is required with tls_client_auth as client authentication method");
        testObserver.assertNotComplete();
    }

    @Test
    public void create_FapiBrazil_success() throws Exception {
        DynamicClientRegistrationRequest request = new DynamicClientRegistrationRequest();
        request.setRedirectUris(Optional.empty());
        request.setRequireParRequest(Optional.of(false));
        request.setRequestObjectEncryptionAlg(Optional.of(JWEAlgorithm.RSA_OAEP.getName()));
        request.setRequestObjectEncryptionEnc(Optional.of(EncryptionMethod.A256GCM.getName()));
        request.setJwksUri(Optional.of(DUMMY_JWKS_URI));
        request.setRedirectUris(Optional.of(Arrays.asList(DUMMY_REDIRECTURI)));

        request.setTokenEndpointAuthMethod(Optional.of(ClientAuthenticationMethod.TLS_CLIENT_AUTH));
        request.setTlsClientAuthSubjectDn(Optional.of("Subject DN"));
        request.setTlsClientAuthSanEmail(Optional.empty());
        // request.setTlsClientAuthSanDns(Optional.empty()); // DO NOT provide, empty should be present at the end
        // request.setTlsClientAuthSanIp(Optional.empty()); // DO NOT provide it, empty should be present at the end
        request.setTlsClientAuthSanUri(Optional.empty());

        assertNull(request.getTlsClientAuthSanDns());
        assertNull(request.getTlsClientAuthSanIp());

        final RSAKey rsaKey = generateRSAKey();
        request.setSoftwareStatement(Optional.of(generateSoftwareStatement(rsaKey, JWSAlgorithm.PS256, Instant.now())));

        when(domain.useFapiBrazilProfile()).thenReturn(true);
        when(environment.getProperty(DynamicClientRegistrationServiceImpl.FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI)).thenReturn(DUMMY_JWKS_URI);

        when(jwkService.getKeys_migrated(anyString())).thenReturn(Mono.just(new Keys()));
        when(jwkService.getKey_migrated(any(), any())).thenReturn(Mono.just(new io.gravitee.am.model.jose.RSAKey()));
        when(jwsService.isValidSignature(any(), any())).thenReturn(true);

        TestObserver<Client> testObserver = RxJava2Adapter.monoToSingle(dcrService.create_migrated(request, BASE_PATH)).test();
        testObserver.assertNoErrors();
        testObserver.assertComplete();

        verify(clientService).create_migrated(argThat(client -> client.getAccessTokenValiditySeconds() == FAPI_OPENBANKING_BRAZIL_DEFAULT_ACCESS_TOKEN_VALIDITY
                && client.getRefreshTokenValiditySeconds() == 1000
                && client.getIdTokenValiditySeconds() == 1100));

        assertNotNull(request.getTlsClientAuthSanDns());
        assertTrue(request.getTlsClientAuthSanDns().isEmpty());
        assertNotNull(request.getTlsClientAuthSanIp());
        assertTrue(request.getTlsClientAuthSanIp().isEmpty());
    }

    private RSAKey generateRSAKey() throws Exception {
        return new RSAKeyGenerator(2048)
                .keyID("123")
                .generate();
    }

    private String generateSoftwareStatement(RSAKey rsaJWK, JWSAlgorithm jwsAlg, Instant iat) throws Exception {
        JWSSigner signer = new RSASSASigner(rsaJWK);

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("iat", iat.getEpochSecond());
        jsonObject.put("software_jwks_uri", DUMMY_JWKS_URI);
        final JSONArray redirectUris = new JSONArray();
        redirectUris.add(DUMMY_REDIRECTURI);
        jsonObject.put("software_redirect_uris", redirectUris);

        JWSObject jwsObject = new JWSObject(
                new JWSHeader.Builder(jwsAlg).keyID(rsaJWK.getKeyID()).build(),
                new Payload(jsonObject));

        jwsObject.sign(signer);

        return jwsObject.serialize();
    }

}

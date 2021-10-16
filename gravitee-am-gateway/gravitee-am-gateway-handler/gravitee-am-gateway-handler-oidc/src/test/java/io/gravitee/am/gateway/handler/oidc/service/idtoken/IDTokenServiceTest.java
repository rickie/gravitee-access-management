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
package io.gravitee.am.gateway.handler.oidc.service.idtoken;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.am.certificate.api.CertificateProvider;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.common.oauth2.TokenTypeHint;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.common.oidc.idtoken.Claims;
import io.gravitee.am.gateway.handler.common.certificate.CertificateManager;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.gateway.handler.context.ExecutionContextFactory;
import io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDDiscoveryService;
import io.gravitee.am.gateway.handler.oidc.service.idtoken.impl.IDTokenServiceImpl;
import io.gravitee.am.gateway.handler.oidc.service.jwe.JWEService;
import io.gravitee.am.model.TokenClaim;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.ExecutionContext;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.*;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class IDTokenServiceTest {

    @InjectMocks
    private IDTokenService idTokenService = new IDTokenServiceImpl();

    @Mock
    private CertificateManager certificateManager;

    @Mock
    private CertificateProvider certificateProvider;

    @Mock
    private CertificateProvider defaultCertificateProvider;

    @Mock
    private OpenIDDiscoveryService openIDDiscoveryService;

    @Mock
    private JWTService jwtService;

    @Mock
    private JWEService jweService;

    @Mock
    private ExecutionContextFactory executionContextFactory;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldCreateIDToken_clientOnly_clientIdTokenCertificate() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(Collections.singleton("openid"));

        Client client = new Client();
        client.setCertificate("client-certificate");

        String idTokenPayload = "payload";

        io.gravitee.am.gateway.certificate.CertificateProvider idTokenCert = new io.gravitee.am.gateway.certificate.CertificateProvider(certificateProvider);
        io.gravitee.am.gateway.certificate.CertificateProvider clientCert = new io.gravitee.am.gateway.certificate.CertificateProvider(certificateProvider);
        io.gravitee.am.gateway.certificate.CertificateProvider defaultCert = new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider);

        ExecutionContext executionContext = mock(ExecutionContext.class);

        when(certificateManager.findByAlgorithm_migrated(any())).thenReturn(Mono.just(idTokenCert));
        when(certificateManager.get_migrated(anyString())).thenReturn(Mono.just(clientCert));
        when(certificateManager.defaultCertificateProvider()).thenReturn(defaultCert);
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just(idTokenPayload));
        when(executionContextFactory.create(any())).thenReturn(executionContext);

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, null)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateManager, times(1)).findByAlgorithm_migrated(any());
        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(certificateManager, times(1)).defaultCertificateProvider();
        verify(jwtService, times(1)).encode_migrated(any(), eq(idTokenCert));
    }

    @Test
    public void shouldCreateIDToken_clientOnly_clientCertificate() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(Collections.singleton("openid"));

        Client client = new Client();
        client.setCertificate("client-certificate");

        String idTokenPayload = "payload";

        io.gravitee.am.gateway.certificate.CertificateProvider clientCert = new io.gravitee.am.gateway.certificate.CertificateProvider(certificateProvider);
        io.gravitee.am.gateway.certificate.CertificateProvider defaultCert = new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider);

        ExecutionContext executionContext = mock(ExecutionContext.class);

        when(certificateManager.findByAlgorithm_migrated(any())).thenReturn(Mono.empty());
        when(certificateManager.get_migrated(anyString())).thenReturn(Mono.just(clientCert));
        when(certificateManager.defaultCertificateProvider()).thenReturn(defaultCert);
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just(idTokenPayload));
        when(executionContextFactory.create(any())).thenReturn(executionContext);

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, null)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateManager, times(1)).findByAlgorithm_migrated(any());
        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(certificateManager, times(1)).defaultCertificateProvider();
        verify(jwtService, times(1)).encode_migrated(any(), eq(clientCert));
    }

    @Test
    public void shouldCreateIDToken_clientOnly_defaultCertificate() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(Collections.singleton("openid"));

        Client client = new Client();
        client.setCertificate("certificate-client");

        String idTokenPayload = "payload";

        io.gravitee.am.gateway.certificate.CertificateProvider defaultCert = new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider);

        ExecutionContext executionContext = mock(ExecutionContext.class);

        when(certificateManager.findByAlgorithm_migrated(any())).thenReturn(Mono.empty());
        when(certificateManager.get_migrated(any())).thenReturn(Mono.empty());
        when(certificateManager.defaultCertificateProvider()).thenReturn(defaultCert);
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just(idTokenPayload));
        when(executionContextFactory.create(any())).thenReturn(executionContext);

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, null)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateManager, times(1)).findByAlgorithm_migrated(any());
        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(certificateManager, times(1)).defaultCertificateProvider();
        verify(jwtService, times(1)).encode_migrated(any(), eq(defaultCert));
    }

    @Test
    public void shouldCreateIDToken_customClaims() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(Collections.singleton("openid"));

        TokenClaim customClaim = new TokenClaim();
        customClaim.setTokenType(TokenTypeHint.ID_TOKEN);
        customClaim.setClaimName("iss");
        customClaim.setClaimValue("https://custom-iss");

        Client client = new Client();
        client.setCertificate("certificate-client");
        client.setClientId("my-client-id");
        client.setTokenCustomClaims(Arrays.asList(customClaim));

        ExecutionContext executionContext = mock(ExecutionContext.class);
        TemplateEngine templateEngine = mock(TemplateEngine.class);
        when(templateEngine.getValue("https://custom-iss", Object.class)).thenReturn("https://custom-iss");
        when(executionContext.getTemplateEngine()).thenReturn(templateEngine);

        String idTokenPayload = "payload";
        io.gravitee.am.gateway.certificate.CertificateProvider defaultCert = new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider);

        ArgumentCaptor<JWT> jwtCaptor = ArgumentCaptor.forClass(JWT.class);
        when(jwtService.encode_migrated(jwtCaptor.capture(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just(idTokenPayload));
        when(certificateManager.findByAlgorithm_migrated(any())).thenReturn(Mono.empty());
        when(certificateManager.get_migrated(any())).thenReturn(Mono.empty());
        when(certificateManager.defaultCertificateProvider()).thenReturn(defaultCert);

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, null, executionContext)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        JWT jwt = jwtCaptor.getValue();
        assertNotNull(jwt);
        assertTrue(jwt.get("iss") != null && "https://custom-iss".equals(jwt.get("iss")));
        verify(certificateManager, times(1)).findByAlgorithm_migrated(any());
        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(certificateManager, times(1)).defaultCertificateProvider();
        verify(jwtService, times(1)).encode_migrated(any(), eq(defaultCert));
    }

    @Test
    public void shouldCreateIDToken_clientOnly_defaultCertificate_withEncryption() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(Collections.singleton("openid"));

        Client client = new Client();
        client.setCertificate("certificate-client");
        client.setIdTokenEncryptedResponseAlg("expectEncryption");

        String idTokenPayload = "payload";

        io.gravitee.am.gateway.certificate.CertificateProvider defaultCert = new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider);

        ExecutionContext executionContext = mock(ExecutionContext.class);

        when(certificateManager.findByAlgorithm_migrated(any())).thenReturn(Mono.empty());
        when(certificateManager.get_migrated(any())).thenReturn(Mono.empty());
        when(certificateManager.defaultCertificateProvider()).thenReturn(defaultCert);
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just(idTokenPayload));
        when(jweService.encryptIdToken_migrated(anyString(),any())).thenReturn(Mono.just("encryptedToken"));
        when(executionContextFactory.create(any())).thenReturn(executionContext);

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, null)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateManager, times(1)).findByAlgorithm_migrated(any());
        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(certificateManager, times(1)).defaultCertificateProvider();
        verify(jwtService, times(1)).encode_migrated(any(), eq(defaultCert));
        verify(jweService, times(1)).encryptIdToken_migrated(anyString(),any());
    }

    @Test
    @Ignore
    // ignore due to map order and current timestamp (local test)
    public void shouldCreateIDToken_withUser_claimsRequest() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(Collections.singleton("openid"));
        oAuth2Request.setSubject("subject");
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.put("claims", Collections.singletonList("{\"id_token\":{\"name\":{\"essential\":true}}}"));
        oAuth2Request.setParameters(requestParameters);

        Client client = new Client();

        User user = createUser();

        JWT expectedJwt = new JWT();
        expectedJwt.setSub(user.getId());
        expectedJwt.setAud("client-id");
        expectedJwt.setIss(null);
        expectedJwt.put(StandardClaims.NAME, user.getAdditionalInformation().get(StandardClaims.NAME));
        expectedJwt.setIat(System.currentTimeMillis() / 1000l);
        expectedJwt.setExp(expectedJwt.getIat() + 14400);

        when(certificateManager.defaultCertificateProvider()).thenReturn(new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider));
        when(certificateManager.get_migrated(anyString())).thenReturn(Mono.just(new io.gravitee.am.gateway.certificate.CertificateProvider(certificateProvider)));
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just("test"));
        ((IDTokenServiceImpl) idTokenService).setObjectMapper(objectMapper);

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, user)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(jwtService, times(1)).encode_migrated(eq(expectedJwt), any(io.gravitee.am.gateway.certificate.CertificateProvider.class));
    }

    @Test
    public void shouldCreateIDToken_withUser_claimsRequest_acrValues() {
        Client client = new Client();
        User user = createUser();

        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(Collections.singleton("openid"));
        oAuth2Request.setSubject("subject");
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.put("claims", Collections.singletonList("{\"id_token\":{\"acr\":{\"value\":\"urn:mace:incommon:iap:silver\",\"essential\":true}}}"));
        oAuth2Request.setParameters(requestParameters);

        io.gravitee.am.gateway.certificate.CertificateProvider defaultCert = new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider);

        ExecutionContext executionContext = mock(ExecutionContext.class);

        when(certificateManager.findByAlgorithm_migrated(any())).thenReturn(Mono.empty());
        when(certificateManager.get_migrated(any())).thenReturn(Mono.empty());
        when(certificateManager.defaultCertificateProvider()).thenReturn(defaultCert);
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just("test"));
        when(executionContextFactory.create(any())).thenReturn(executionContext);
        ((IDTokenServiceImpl) idTokenService).setObjectMapper(objectMapper);

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, user)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        ArgumentCaptor<JWT> tokenArgumentCaptor = ArgumentCaptor.forClass(JWT.class);
        verify(jwtService).encode_migrated(tokenArgumentCaptor.capture(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class));
        JWT idToken = tokenArgumentCaptor.getValue();
        assertTrue(idToken.containsKey(Claims.acr) && idToken.get(Claims.acr).equals("urn:mace:incommon:iap:silver"));
    }

    @Test
    @Ignore
    // ignore due to map order and current timestamp (local test)
    public void shouldCreateIDToken_withUser_scopesRequest() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(new HashSet<>(Arrays.asList("openid", "profile")));
        oAuth2Request.setSubject("subject");

        Client client = new Client();

        User user = createUser();

        JWT expectedJwt = new JWT();
        expectedJwt.setSub(user.getId());
        expectedJwt.put(StandardClaims.WEBSITE, user.getAdditionalInformation().get(StandardClaims.WEBSITE));
        expectedJwt.put(StandardClaims.ZONEINFO, user.getAdditionalInformation().get(StandardClaims.ZONEINFO));
        expectedJwt.put(StandardClaims.BIRTHDATE, user.getAdditionalInformation().get(StandardClaims.BIRTHDATE));
        expectedJwt.put(StandardClaims.GENDER, user.getAdditionalInformation().get(StandardClaims.GENDER));
        expectedJwt.put(StandardClaims.PROFILE, user.getAdditionalInformation().get(StandardClaims.PROFILE));
        expectedJwt.setIss(null);
        expectedJwt.put(StandardClaims.PREFERRED_USERNAME, user.getAdditionalInformation().get(StandardClaims.PREFERRED_USERNAME));
        expectedJwt.put(StandardClaims.GIVEN_NAME, user.getAdditionalInformation().get(StandardClaims.GIVEN_NAME));
        expectedJwt.put(StandardClaims.MIDDLE_NAME, user.getAdditionalInformation().get(StandardClaims.MIDDLE_NAME));
        expectedJwt.put(StandardClaims.LOCALE, user.getAdditionalInformation().get(StandardClaims.LOCALE));
        expectedJwt.put(StandardClaims.PICTURE, user.getAdditionalInformation().get(StandardClaims.PICTURE));
        expectedJwt.setAud("client-id");
        expectedJwt.put(StandardClaims.UPDATED_AT, user.getAdditionalInformation().get(StandardClaims.UPDATED_AT));
        expectedJwt.put(StandardClaims.NAME, user.getAdditionalInformation().get(StandardClaims.NAME));
        expectedJwt.put(StandardClaims.NICKNAME, user.getAdditionalInformation().get(StandardClaims.NICKNAME));
        expectedJwt.setExp((System.currentTimeMillis() / 1000l) + 14400);
        expectedJwt.setIat(System.currentTimeMillis() / 1000l);
        expectedJwt.put(StandardClaims.FAMILY_NAME, user.getAdditionalInformation().get(StandardClaims.FAMILY_NAME));

        when(certificateManager.defaultCertificateProvider()).thenReturn(new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider));
        when(certificateManager.get_migrated(anyString())).thenReturn(Mono.just(new io.gravitee.am.gateway.certificate.CertificateProvider(certificateProvider)));
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just("test"));

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, user)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(jwtService, times(1)).encode_migrated(eq(expectedJwt), any(io.gravitee.am.gateway.certificate.CertificateProvider.class));
    }

    @Test
    @Ignore
    // ignore due to map order and current timestamp (local test)
    public void shouldCreateIDToken_withUser_scopesRequest_email() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(new HashSet<>(Arrays.asList("openid", "email")));
        oAuth2Request.setSubject("subject");

        Client client = new Client();

        User user = createUser();

        JWT expectedJwt = new JWT();
        expectedJwt.setSub(user.getId());
        expectedJwt.setAud("client-id");
        expectedJwt.put(StandardClaims.EMAIL_VERIFIED, user.getAdditionalInformation().get(StandardClaims.EMAIL_VERIFIED));
        expectedJwt.setIss(null);
        expectedJwt.setExp((System.currentTimeMillis() / 1000l) + 14400);
        expectedJwt.setIat(System.currentTimeMillis() / 1000l);
        expectedJwt.put(StandardClaims.EMAIL, user.getAdditionalInformation().get(StandardClaims.EMAIL));

        when(certificateManager.defaultCertificateProvider()).thenReturn(new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider));
        when(certificateManager.get_migrated(anyString())).thenReturn(Mono.just(new io.gravitee.am.gateway.certificate.CertificateProvider(certificateProvider)));
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just("test"));

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, user)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(jwtService, times(1)).encode_migrated(eq(expectedJwt), any(io.gravitee.am.gateway.certificate.CertificateProvider.class));
    }

    @Test
    @Ignore
    // ignore due to map order and current timestamp (local test)
    public void shouldCreateIDToken_withUser_scopesRequest_email_address() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(new HashSet<>(Arrays.asList("openid", "email", "address")));
        oAuth2Request.setSubject("subject");

        Client client = new Client();

        User user = createUser();

        JWT expectedJwt = new JWT();
        expectedJwt.setSub(user.getId());
        expectedJwt.setAud("client-id");
        expectedJwt.put(StandardClaims.ADDRESS, user.getAdditionalInformation().get(StandardClaims.ADDRESS));
        expectedJwt.put(StandardClaims.EMAIL_VERIFIED, user.getAdditionalInformation().get(StandardClaims.EMAIL_VERIFIED));
        expectedJwt.setIss(null);
        expectedJwt.setExp((System.currentTimeMillis() / 1000l) + 14400);
        expectedJwt.setIat(System.currentTimeMillis() / 1000l);
        expectedJwt.put(StandardClaims.EMAIL, user.getAdditionalInformation().get(StandardClaims.EMAIL));

        when(certificateManager.defaultCertificateProvider()).thenReturn(new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider));
        when(certificateManager.get_migrated(anyString())).thenReturn(Mono.just(new io.gravitee.am.gateway.certificate.CertificateProvider(certificateProvider)));
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just("test"));

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, user)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(jwtService, times(1)).encode_migrated(eq(expectedJwt), any(io.gravitee.am.gateway.certificate.CertificateProvider.class));
    }

    @Test
    @Ignore
    // ignore due to map order and current timestamp (local test)
    public void shouldCreateIDToken_withUser_scopesRequest_and_claimsRequest() {
        OAuth2Request oAuth2Request = new OAuth2Request();
        oAuth2Request.setClientId("client-id");
        oAuth2Request.setScopes(new HashSet<>(Arrays.asList("openid", "email", "address")));
        oAuth2Request.setSubject("subject");
        MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
        requestParameters.put("claims", Collections.singletonList("{\"id_token\":{\"name\":{\"essential\":true}}}"));
        oAuth2Request.setParameters(requestParameters);

        Client client = new Client();

        User user = createUser();

        JWT expectedJwt = new JWT();
        expectedJwt.setSub(user.getId());
        expectedJwt.setAud("client-id");
        expectedJwt.put(StandardClaims.ADDRESS, user.getAdditionalInformation().get(StandardClaims.ADDRESS));
        expectedJwt.put(StandardClaims.EMAIL_VERIFIED, user.getAdditionalInformation().get(StandardClaims.EMAIL_VERIFIED));
        expectedJwt.setIss(null);
        expectedJwt.put(StandardClaims.NAME, user.getAdditionalInformation().get(StandardClaims.NAME));
        expectedJwt.setExp((System.currentTimeMillis() / 1000l) + 14400);
        expectedJwt.setIat(System.currentTimeMillis() / 1000l);
        expectedJwt.put(StandardClaims.EMAIL, user.getAdditionalInformation().get(StandardClaims.EMAIL));

        when(certificateManager.defaultCertificateProvider()).thenReturn(new io.gravitee.am.gateway.certificate.CertificateProvider(defaultCertificateProvider));
        when(certificateManager.get_migrated(anyString())).thenReturn(Mono.just(new io.gravitee.am.gateway.certificate.CertificateProvider(certificateProvider)));
        when(jwtService.encode_migrated(any(), any(io.gravitee.am.gateway.certificate.CertificateProvider.class))).thenReturn(Mono.just("test"));
        ((IDTokenServiceImpl) idTokenService).setObjectMapper(objectMapper);

        TestObserver<String> testObserver = RxJava2Adapter.monoToSingle(idTokenService.create_migrated(oAuth2Request, client, user)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateManager, times(1)).get_migrated(anyString());
        verify(jwtService, times(1)).encode_migrated(eq(expectedJwt), any(io.gravitee.am.gateway.certificate.CertificateProvider.class));
    }

    private User createUser() {
        User user = new User();
        user.setId("technical-id");
        Map<String, Object> additionalInformation  = new HashMap<>();
        additionalInformation.put(StandardClaims.SUB, "user");
        additionalInformation.put(StandardClaims.NAME, "gravitee user");
        additionalInformation.put(StandardClaims.FAMILY_NAME, "gravitee");
        additionalInformation.put(StandardClaims.GIVEN_NAME, "gravitee");
        additionalInformation.put(StandardClaims.MIDDLE_NAME, "gravitee");
        additionalInformation.put(StandardClaims.NICKNAME, "gravitee");
        additionalInformation.put(StandardClaims.PREFERRED_USERNAME, "gravitee");
        additionalInformation.put(StandardClaims.PROFILE, "gravitee");
        additionalInformation.put(StandardClaims.PICTURE, "gravitee");
        additionalInformation.put(StandardClaims.WEBSITE, "gravitee");
        additionalInformation.put(StandardClaims.GENDER, "gravitee");
        additionalInformation.put(StandardClaims.BIRTHDATE, "gravitee");
        additionalInformation.put(StandardClaims.ZONEINFO, "gravitee");
        additionalInformation.put(StandardClaims.LOCALE, "gravitee");
        additionalInformation.put(StandardClaims.UPDATED_AT, "gravitee");
        additionalInformation.put(StandardClaims.EMAIL, "gravitee");
        additionalInformation.put(StandardClaims.EMAIL_VERIFIED, "gravitee");
        additionalInformation.put(StandardClaims.ADDRESS, "gravitee");
        additionalInformation.put(StandardClaims.PHONE_NUMBER, "gravitee");
        additionalInformation.put(StandardClaims.PHONE_NUMBER_VERIFIED, "gravitee");
        user.setAdditionalInformation(additionalInformation);

        return user;
    }
}

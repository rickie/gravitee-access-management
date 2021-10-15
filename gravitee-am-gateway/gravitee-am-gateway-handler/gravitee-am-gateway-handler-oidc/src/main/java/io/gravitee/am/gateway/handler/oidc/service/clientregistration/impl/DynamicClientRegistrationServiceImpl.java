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
package io.gravitee.am.gateway.handler.oidc.service.clientregistration.impl;

import static io.gravitee.am.common.oidc.Scope.SCOPE_DELIMITER;
import static io.gravitee.am.gateway.handler.oidc.service.utils.JWAlgorithmUtils.*;

import com.google.common.base.Strings;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.common.oidc.ClientAuthenticationMethod;
import io.gravitee.am.common.oidc.Scope;
import io.gravitee.am.common.utils.SecureRandomString;
import io.gravitee.am.common.web.UriBuilder;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.gateway.handler.oidc.service.clientregistration.ClientService;
import io.gravitee.am.gateway.handler.oidc.service.clientregistration.DynamicClientRegistrationRequest;
import io.gravitee.am.gateway.handler.oidc.service.clientregistration.DynamicClientRegistrationService;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDDiscoveryService;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDProviderMetadata;
import io.gravitee.am.gateway.handler.oidc.service.jwk.JWKService;
import io.gravitee.am.gateway.handler.oidc.service.jws.JWSService;
import io.gravitee.am.gateway.handler.oidc.service.utils.JWAlgorithmUtils;
import io.gravitee.am.gateway.handler.oidc.service.utils.SubjectTypeUtils;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.service.CertificateService;
import io.gravitee.am.service.EmailTemplateService;
import io.gravitee.am.service.FormService;
import io.gravitee.am.service.IdentityProviderService;
import io.gravitee.am.service.exception.InvalidClientMetadataException;
import io.gravitee.am.service.exception.InvalidRedirectUriException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.utils.GrantTypeUtils;
import io.gravitee.am.service.utils.ResponseTypeUtils;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.ArrayList;
import java.util.HashSet;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class DynamicClientRegistrationServiceImpl implements DynamicClientRegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClientRegistrationServiceImpl.class);
    public static final int FIVE_MINUTES_IN_SEC = 300;
    public static final String FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI = "openid.fapi.openbanking.brazil.directory.jwks_uri";
    public static final String OPENID_DCR_ACCESS_TOKEN_VALIDITY = "openid.dcr.access_token.validity";
    public static final String OPENID_DCR_REFRESH_TOKEN_VALIDITY = "openid.dcr.refresh_token.validity";
    public static final String OPENID_DCR_ID_TOKEN_VALIDITY = "openid.dcr.id_token.validity";
    public static final int FAPI_OPENBANKING_BRAZIL_DEFAULT_ACCESS_TOKEN_VALIDITY = 900;

    @Autowired
    private OpenIDDiscoveryService openIDDiscoveryService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private JWKService jwkService;

    @Autowired
    private JWSService jwsService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    @Qualifier("oidcWebClient")
    public WebClient client;

    @Autowired
    private Domain domain;

    @Autowired
    private FormService formService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private Environment environment;

    @Override
    public Single<Client> create(DynamicClientRegistrationRequest request, String basePath) {
        //If Dynamic client registration from template is enabled and request contains a software_id
        if(domain.isDynamicClientRegistrationTemplateEnabled() && request.getSoftwareId()!=null && request.getSoftwareId().isPresent()) {
            return this.createClientFromTemplate(request, basePath);
        }
        return this.createClientFromRequest(request,basePath);
    }

    @Override
    public Single<Client> patch(Client toPatch, DynamicClientRegistrationRequest request, String basePath) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.validateClientPatchRequest(request)).map(RxJavaReactorMigrationUtil.toJdkFunction(req -> req.patch(toPatch))).flatMap(app->RxJava2Adapter.singleToMono(this.applyRegistrationAccessToken(basePath, app))).flatMap(v->RxJava2Adapter.singleToMono((Single<Client>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Client, Single<Client>>)clientService::update).apply(v))));
    }

    @Override
    public Single<Client> update(Client toUpdate, DynamicClientRegistrationRequest request, String basePath) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.validateClientRegistrationRequest(request)).map(RxJavaReactorMigrationUtil.toJdkFunction(req -> req.patch(toUpdate))).flatMap(app->RxJava2Adapter.singleToMono(this.applyRegistrationAccessToken(basePath, app))).flatMap(v->RxJava2Adapter.singleToMono((Single<Client>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Client, Single<Client>>)clientService::update).apply(v))));
    }

    @Override
    public Single<Client> delete(Client toDelete) {
        return this.clientService.delete(toDelete.getId()).toSingleDefault(toDelete);
    }

    @Override
    public Single<Client> renewSecret(Client toRenew, String basePath) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(clientService.renewClientSecret(domain.getId(), toRenew.getId())).flatMap(client->RxJava2Adapter.singleToMono(applyRegistrationAccessToken(basePath, client))).flatMap(v->RxJava2Adapter.singleToMono((Single<Client>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Client, Single<Client>>)clientService::update).apply(v))));
    }

    private Single<Client> createClientFromRequest(DynamicClientRegistrationRequest request, String basePath) {
        Client client = new Client();
        client.setClientId(SecureRandomString.generate());
        client.setClientName(ClientServiceImpl.DEFAULT_CLIENT_NAME);
        client.setDomain(domain.getId());

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.validateClientRegistrationRequest(request)
                .map(req -> req.patch(client))
                .flatMap(this::applyDefaultIdentityProvider)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Client, SingleSource<Client>>toJdkFunction(this::applyDefaultCertificateProvider).apply(v)))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Client, SingleSource<Client>>toJdkFunction(this::applyAccessTokenValidity).apply(v)))).flatMap(app->RxJava2Adapter.singleToMono(this.applyRegistrationAccessToken(basePath, app))).flatMap(v->RxJava2Adapter.singleToMono((Single<Client>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Client, Single<Client>>)clientService::create).apply(v))));
    }

    private Single<Client> applyAccessTokenValidity(Client client) {
        client.setAccessTokenValiditySeconds(environment.getProperty(OPENID_DCR_ACCESS_TOKEN_VALIDITY, Integer.class, domain.useFapiBrazilProfile() ? FAPI_OPENBANKING_BRAZIL_DEFAULT_ACCESS_TOKEN_VALIDITY : Client.DEFAULT_ACCESS_TOKEN_VALIDITY_SECONDS));
        client.setRefreshTokenValiditySeconds(environment.getProperty(OPENID_DCR_REFRESH_TOKEN_VALIDITY, Integer.class, Client.DEFAULT_REFRESH_TOKEN_VALIDITY_SECONDS));
        client.setIdTokenValiditySeconds(environment.getProperty(OPENID_DCR_ID_TOKEN_VALIDITY, Integer.class, Client.DEFAULT_ID_TOKEN_VALIDITY_SECONDS));
        return RxJava2Adapter.monoToSingle(Mono.just(client));
    }

    /**
     * <pre>
     * Software_id is based on id field and not client_id because:
     * this field is not intended to be human readable and is usually opaque to the client and authorization server.
     * the client may switch back from template to real client and then this is better to not expose it's client_id.
     * @param request
     * @param basePath
     * @return
     * </pre>
     */
    private Single<Client> createClientFromTemplate(DynamicClientRegistrationRequest request, String basePath) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(clientService.findById(request.getSoftwareId().get())
                .switchIfEmpty(Maybe.error(new InvalidClientMetadataException("No template found for software_id "+request.getSoftwareId().get())))
                .flatMapSingle(this::sanitizeTemplate)
                .map(request::patch)).flatMap(app->RxJava2Adapter.singleToMono(this.applyRegistrationAccessToken(basePath, app))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Client, SingleSource<Client>>toJdkFunction(clientService::create).apply(v)))).flatMap(client->RxJava2Adapter.singleToMono(copyForms(request.getSoftwareId().get(), client))).flatMap(client->RxJava2Adapter.singleToMono(copyEmails(request.getSoftwareId().get(), client))));
    }

    private Single<Client> sanitizeTemplate(Client template) {
        if(!template.isTemplate()) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Client behind software_id is not a template")));
        }
        //Erase potential confidential values.
        template.setClientId(SecureRandomString.generate());
        template.setDomain(domain.getId());
        template.setId(null);
        template.setClientSecret(null);
        template.setClientName(ClientServiceImpl.DEFAULT_CLIENT_NAME);
        template.setRedirectUris(null);
        template.setSectorIdentifierUri(null);
        template.setJwks(null);
        template.setJwksUri(null);
        //Set it as non template
        template.setTemplate(false);

        return RxJava2Adapter.monoToSingle(Mono.just(template));
    }

    private Single<Client> copyForms(String sourceId, Client client) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(formService.copyFromClient(domain.getId(), sourceId, client.getId())).flatMap(irrelevant->Mono.just(client)));
    }

    private Single<Client> copyEmails(String sourceId, Client client) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(emailTemplateService.copyFromClient(domain.getId(), sourceId, client.getId())).collectList().flatMap(irrelevant->Mono.just(client)));
    }

    /**
     * Identity provider is not part of dynamic client registration but needed on the client.
     * So we set the first identoty provider available on the domain.
     * @param client App to create
     * @return
     */
    private Single<Client> applyDefaultIdentityProvider(Client client) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(identityProviderService.findByDomain(client.getDomain())).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(identityProviders -> {
                if(identityProviders!=null && !identityProviders.isEmpty()) {
                    client.setIdentities(Collections.singleton(identityProviders.get(0).getId()));
                }
                return client;
            })));
    }

    /**
     * Certificate provider is not part of dynamic client registration but needed on the client.
     * So we set the first certificate provider available on the domain.
     * @param client App to create
     * @return
     */
    private Single<Client> applyDefaultCertificateProvider(Client client) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(certificateService.findByDomain(client.getDomain())).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(certificates -> {
                    if(certificates!=null && !certificates.isEmpty()) {
                        client.setCertificate(certificates.get(0).getId());
                    }
                    return client;
                })));
    }

    private Single<Client> applyRegistrationAccessToken(String basePath, Client client) {

        OpenIDProviderMetadata openIDProviderMetadata = openIDDiscoveryService.getConfiguration(basePath);

        JWT jwt = new JWT();
        jwt.setIss(openIDProviderMetadata.getIssuer());
        jwt.setSub(client.getClientId());
        jwt.setAud(client.getClientId());
        jwt.setDomain(client.getDomain());
        jwt.setIat(new Date().getTime() / 1000l);
        jwt.setExp(Date.from(new Date().toInstant().plusSeconds(3600*24*365*2)).getTime() / 1000l);
        jwt.setScope(Scope.DCR.getKey());
        jwt.setJti(SecureRandomString.generate());

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(jwtService.encode(jwt, client)).map(RxJavaReactorMigrationUtil.toJdkFunction(token -> {
                    client.setRegistrationAccessToken(token);
                    client.setRegistrationClientUri(openIDProviderMetadata.getRegistrationEndpoint()+"/"+client.getClientId());
                    return client;
                })));
    }

    /**
     * Validate payload according to openid specifications.
     *
     * https://openid.net/specs/openid-connect-registration-1_0.html#ClientMetadata
     *
     * @param request DynamicClientRegistrationRequest
     */
    private Single<DynamicClientRegistrationRequest> validateClientRegistrationRequest(final DynamicClientRegistrationRequest request) {
        LOGGER.debug("Validating dynamic client registration payload");
        return this.validateClientRegistrationRequest(request,false);
    }

    private Single<DynamicClientRegistrationRequest> validateClientPatchRequest(DynamicClientRegistrationRequest request) {
        LOGGER.debug("Validating dynamic client registration payload : patch");
        //redirect_uri is mandatory in the request, but in case of patch we may omit it...
        return this.validateClientRegistrationRequest(request,true);
    }

    private Single<DynamicClientRegistrationRequest> validateClientRegistrationRequest(final DynamicClientRegistrationRequest request, boolean isPatch) {
        if(request==null) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException()));
        }

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.validateRedirectUri(request, isPatch)
                .flatMap(this::validateScopes)
                .flatMap(this::validateGrantType)
                .flatMap(this::validateResponseType)
                .flatMap(this::validateSubjectType)
                .flatMap(this::validateRequestUri)
                .flatMap(this::validateSectorIdentifierUri)
                .flatMap(this::validateJKWs)
                .flatMap(this::validateUserinfoSigningAlgorithm)
                .flatMap(this::validateUserinfoEncryptionAlgorithm)
                .flatMap(this::validateIdTokenSigningAlgorithm)
                .flatMap(this::validateIdTokenEncryptionAlgorithm)
                .flatMap(this::validateTlsClientAuth)
                .flatMap(this::validateSelfSignedClientAuth)
                .flatMap(this::validateAuthorizationSigningAlgorithm)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<DynamicClientRegistrationRequest, SingleSource<DynamicClientRegistrationRequest>>toJdkFunction(this::validateAuthorizationEncryptionAlgorithm).apply(v)))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<DynamicClientRegistrationRequest, SingleSource<DynamicClientRegistrationRequest>>toJdkFunction(this::validateRequestObjectSigningAlgorithm).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<DynamicClientRegistrationRequest, SingleSource<DynamicClientRegistrationRequest>>toJdkFunction(this::validateRequestObjectEncryptionAlgorithm).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono((Single<DynamicClientRegistrationRequest>)RxJavaReactorMigrationUtil.toJdkFunction((Function<DynamicClientRegistrationRequest, Single<DynamicClientRegistrationRequest>>)this::enforceWithSoftwareStatement).apply(v))));
    }

    private Single<DynamicClientRegistrationRequest> enforceWithSoftwareStatement(DynamicClientRegistrationRequest request) {
        if (this.domain.useFapiBrazilProfile()) {
            if (request.getSoftwareStatement() == null || request.getSoftwareStatement().isEmpty()) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("software_statement is required")));
            }

            final String directoryJwksUri = environment.getProperty(FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI);
            if (Strings.isNullOrEmpty(directoryJwksUri)) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("No jwks_uri for OpenBanking Directory, unable to validate software_statement")));
            }

            try {

                com.nimbusds.jwt.JWT jwt = JWTParser.parse(request.getSoftwareStatement().get());
                if (jwt instanceof SignedJWT) {
                    final SignedJWT signedJWT = (SignedJWT) jwt;
                    if (isSignAlgCompliantWithFapi(signedJWT.getHeader().getAlgorithm().getName())) {
                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(jwkService.getKeys(directoryJwksUri)
                                .flatMap(jwk -> jwkService.getKey(jwk, signedJWT.getHeader().getKeyID()))).switchIfEmpty(RxJava2Adapter.singleToMono(Single.wrap(Single.error(new TechnicalManagementException("Invalid jwks_uri for OpenBanking Directory"))))))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(jwk -> jwsService.isValidSignature(signedJWT, jwk))).switchIfEmpty(Mono.error(new InvalidClientMetadataException("Invalid signature for software_statement"))).map(RxJavaReactorMigrationUtil.toJdkFunction(__ -> {
                                    LOGGER.debug("software_statement is valid, check claims regarding the registration request information");
                                    JSONObject softwareStatement = signedJWT.getPayload().toJSONObject();
                                    final Number iat = softwareStatement.getAsNumber("iat");
                                    if (iat == null || (Instant.now().getEpochSecond() - (iat.longValue())) > FIVE_MINUTES_IN_SEC) {
                                        throw new InvalidClientMetadataException("software_statement older than 5 minutes");
                                    }

                                    if (request.getJwks() != null && !request.getJwks().isEmpty()) {
                                        throw new InvalidClientMetadataException("jwks is forbidden, prefer jwks_uri");
                                    }

                                    if (request.getJwksUri() == null || request.getJwksUri().isEmpty()) {
                                        throw new InvalidClientMetadataException("jwks_uri is required");
                                    }

                                    if (!request.getJwksUri().get().equals(softwareStatement.getAsString("software_jwks_uri"))) {
                                        throw new InvalidClientMetadataException("jwks_uri doesn't match the software_jwks_uri");
                                    }

                                    final Object software_redirect_uris = softwareStatement.get("software_redirect_uris");
                                    if (software_redirect_uris != null) {
                                        if (request.getRedirectUris() == null || request.getRedirectUris().isEmpty()) {
                                            throw new InvalidClientMetadataException("redirect_uris are missing");
                                        }

                                        final List<String> redirectUris = request.getRedirectUris().get();
                                        if (software_redirect_uris instanceof JSONArray) {
                                            redirectUris.forEach(uri -> {
                                                if (!((JSONArray) software_redirect_uris).contains(uri)) {
                                                    throw new InvalidClientMetadataException("redirect_uris contains unknown uri from software_statement");
                                                }
                                            });
                                        } else if (software_redirect_uris instanceof String && (redirectUris.size() > 1 || !software_redirect_uris.equals(redirectUris.get(0)))) {
                                            throw new InvalidClientMetadataException("redirect_uris contains unknown uri from software_statement");
                                        }
                                    }

                                    if (request.getTokenEndpointAuthMethod() != null && !request.getTokenEndpointAuthMethod().isEmpty()) {

                                        if (!(ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH.equals(request.getTokenEndpointAuthMethod().get()) ||
                                                ClientAuthenticationMethod.TLS_CLIENT_AUTH.equals(request.getTokenEndpointAuthMethod().get()) ||
                                                ClientAuthenticationMethod.PRIVATE_KEY_JWT.equals(request.getTokenEndpointAuthMethod().get()))) {
                                            throw new InvalidClientMetadataException("invalid token_endpoint_auth_method");
                                        }

                                        if (ClientAuthenticationMethod.TLS_CLIENT_AUTH.equals(request.getTokenEndpointAuthMethod().get()) && (request.getTlsClientAuthSubjectDn() == null || request.getTlsClientAuthSubjectDn().isEmpty())) {
                                            throw new InvalidClientMetadataException("tls_client_auth_subject_dn is required with tls_client_auth as client authentication method");
                                        }
                                    }

                                    return request;
                                })));
                    }
                }

                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("software_statement isn't signed or doesn't use PS256")));

            } catch (ParseException pe) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("signature of software_statement is invalid")));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    /**
     * According to openid specification, redirect_uris are REQUIRED in the request for creation.
     * But according to the grant type, it may be null or empty.
     * @param request DynamicClientRegistrationRequest
     * @param isPatch true if only updating some fields (else means all fields will overwritten)
     * @return DynamicClientRegistrationRequest
     */
    private Single<DynamicClientRegistrationRequest> validateRedirectUri(DynamicClientRegistrationRequest request, boolean isPatch) {

        //Except for patching a client, redirect_uris metadata is required but may be null or empty.
        if(!isPatch && request.getRedirectUris() == null) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException()));
        }

        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateResponseType(DynamicClientRegistrationRequest request) {
        //if response_type provided, they must be valid.
        if(request.getResponseTypes()!=null) {
            if(!ResponseTypeUtils.isSupportedResponseType(request.getResponseTypes().orElse(Collections.emptyList()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Invalid response type.")));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateGrantType(DynamicClientRegistrationRequest request) {
        //if grant_type provided, they must be valid.
        if(request.getGrantTypes()!=null) {
            if (!GrantTypeUtils.isSupportedGrantType(request.getGrantTypes().orElse(Collections.emptyList()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Missing or invalid grant type.")));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateSubjectType(DynamicClientRegistrationRequest request) {
        //if subject_type is provided, it must be valid.
        if(request.getSubjectType()!=null && request.getSubjectType().isPresent()) {
            if(!SubjectTypeUtils.isValidSubjectType(request.getSubjectType().get())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported subject type")));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateUserinfoSigningAlgorithm(DynamicClientRegistrationRequest request) {
        //if userinfo_signed_response_alg is provided, it must be valid.
        if(request.getUserinfoSignedResponseAlg()!=null && request.getUserinfoSignedResponseAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidUserinfoSigningAlg(request.getUserinfoSignedResponseAlg().get())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported userinfo signing algorithm")));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateUserinfoEncryptionAlgorithm(DynamicClientRegistrationRequest request) {
        if(request.getUserinfoEncryptedResponseEnc()!=null && request.getUserinfoEncryptedResponseAlg()==null) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("When userinfo_encrypted_response_enc is included, userinfo_encrypted_response_alg MUST also be provided")));
        }
        //if userinfo_encrypted_response_alg is provided, it must be valid.
        if(request.getUserinfoEncryptedResponseAlg()!=null && request.getUserinfoEncryptedResponseAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidUserinfoResponseAlg(request.getUserinfoEncryptedResponseAlg().get())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported userinfo_encrypted_response_alg value")));
            }
            if(request.getUserinfoEncryptedResponseEnc()!=null && request.getUserinfoEncryptedResponseEnc().isPresent()) {
                if(!JWAlgorithmUtils.isValidUserinfoResponseEnc(request.getUserinfoEncryptedResponseEnc().get())) {
                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported userinfo_encrypted_response_enc value")));
                }
            }
            else {
                //Apply default value if userinfo_encrypted_response_alg is informed and not userinfo_encrypted_response_enc.
                request.setUserinfoEncryptedResponseEnc(Optional.of(JWAlgorithmUtils.getDefaultUserinfoResponseEnc()));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }


    private Single<DynamicClientRegistrationRequest> validateRequestObjectSigningAlgorithm(DynamicClientRegistrationRequest request) {
        //if userinfo_signed_response_alg is provided, it must be valid.
        if(request.getRequestObjectSigningAlg() !=null && request.getRequestObjectSigningAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidRequestObjectSigningAlg(request.getRequestObjectSigningAlg().get())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported request object signing algorithm")));
            }

            if (this.domain.usePlainFapiProfile() && !isSignAlgCompliantWithFapi(request.getRequestObjectSigningAlg().get())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("request_object_signing_alg shall be PS256")));
            }
        }

        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateRequestObjectEncryptionAlgorithm(DynamicClientRegistrationRequest request) {
        if(request.getRequestObjectEncryptionEnc() !=null && request.getRequestObjectEncryptionAlg()==null) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("When request_object_encryption_enc is included, request_object_encryption_alg MUST also be provided")));
        }

        //if userinfo_encrypted_response_alg is provided, it must be valid.
        if(request.getRequestObjectEncryptionAlg()!=null && request.getRequestObjectEncryptionAlg().isPresent()) {
            if(!domain.useFapiBrazilProfile() && !JWAlgorithmUtils.isValidRequestObjectAlg(request.getRequestObjectEncryptionAlg().get())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported request_object_encryption_alg value")));
            }

            if(request.getRequestObjectEncryptionEnc()!=null && request.getRequestObjectEncryptionEnc().isPresent()) {
                if(!JWAlgorithmUtils.isValidRequestObjectEnc(request.getRequestObjectEncryptionEnc().get())) {
                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported request_object_encryption_enc value")));
                }
            }
            else {
                //Apply default value if request_object_encryption_alg is informed and not request_object_encryption_enc.
                request.setRequestObjectEncryptionEnc(Optional.of(JWAlgorithmUtils.getDefaultRequestObjectEnc()));
            }
        }

        if (domain.useFapiBrazilProfile()) {
            // for Fapi Brazil, request object MUST be encrypted using RSA-OAEP with A256GCM
            // if the request object is passed by_value (requiredPARRequest set to false)
            final boolean requestModeByValue = request.getRequireParRequest() != null && !request.getRequireParRequest().isEmpty() && !request.getRequireParRequest().get();
            if (requestModeByValue &&
                    ((request.getRequestObjectEncryptionEnc() == null || request.getRequestObjectEncryptionEnc().isEmpty()) ||
                            (request.getRequestObjectEncryptionAlg() == null || request.getRequestObjectEncryptionAlg().isEmpty()) ||
                            !(isKeyEncCompliantWithFapiBrazil(request.getRequestObjectEncryptionAlg().get()) && isContentEncCompliantWithFapiBrazil(request.getRequestObjectEncryptionEnc().get())))) {
                throw new InvalidClientMetadataException("Request object must be encrypted using RSA-OAEP with A256GCM");
            }
        }

        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }


    private Single<DynamicClientRegistrationRequest> validateIdTokenSigningAlgorithm(DynamicClientRegistrationRequest request) {
        //if userinfo_signed_response_alg is provided, it must be valid.
        if(request.getIdTokenSignedResponseAlg()!=null && request.getIdTokenSignedResponseAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidIdTokenSigningAlg(request.getIdTokenSignedResponseAlg().get())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported id_token signing algorithm")));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateIdTokenEncryptionAlgorithm(DynamicClientRegistrationRequest request) {
        if(request.getIdTokenEncryptedResponseEnc()!=null && request.getIdTokenEncryptedResponseAlg()==null) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("When id_token_encrypted_response_enc is included, id_token_encrypted_response_alg MUST also be provided")));
        }
        //if id_token_encrypted_response_alg is provided, it must be valid.
        if(request.getIdTokenEncryptedResponseAlg()!=null && request.getIdTokenEncryptedResponseAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidIdTokenResponseAlg(request.getIdTokenEncryptedResponseAlg().get())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported id_token_encrypted_response_alg value")));
            }
            if(request.getIdTokenEncryptedResponseEnc()!=null && request.getIdTokenEncryptedResponseEnc().isPresent()) {
                if(!JWAlgorithmUtils.isValidIdTokenResponseEnc(request.getIdTokenEncryptedResponseEnc().get())) {
                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported id_token_encrypted_response_enc value")));
                }
            }
            else {
                //Apply default value if id_token_encrypted_response_alg is informed and not id_token_encrypted_response_enc.
                request.setIdTokenEncryptedResponseEnc(Optional.of(JWAlgorithmUtils.getDefaultIdTokenResponseEnc()));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateRequestUri(DynamicClientRegistrationRequest request) {
        //Check request_uri well formated
        if(request.getRequestUris()!=null && request.getRequestUris().isPresent()) {
            try {
                //throw exception if uri mal formated
                request.getRequestUris().get().stream().forEach(this::formatUrl);
            } catch (InvalidClientMetadataException err) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("request_uris: "+err.getMessage())));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateSectorIdentifierUri(DynamicClientRegistrationRequest request) {
        //if sector_identifier_uri is provided, then retrieve content and validate redirect_uris among this list.
        if(request.getSectorIdentifierUri()!=null && request.getSectorIdentifierUri().isPresent()) {

            URI uri;
            try {
                //throw exception if uri mal formated
                uri = formatUrl(request.getSectorIdentifierUri().get());
            } catch (InvalidClientMetadataException err) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("sector_identifier_uri: "+err.getMessage())));
            }

            if(!uri.getScheme().equalsIgnoreCase("https")) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Scheme must be https for sector_identifier_uri : "+request.getSectorIdentifierUri().get())));
            }

            return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(client.getAbs(uri.toString())
                    .rxSend()).map(RxJavaReactorMigrationUtil.toJdkFunction(HttpResponse::bodyAsString)).map(RxJavaReactorMigrationUtil.toJdkFunction(JsonArray::new)))
                    .onErrorResumeNext(RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unable to parse sector_identifier_uri : "+ uri.toString()))))).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(Flowable::fromIterable)))
                    .cast(String.class)
                    .collect(HashSet::new,HashSet::add)).flatMap(allowedRedirectUris->RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToObservable(RxJava2Adapter.observableToFlux(Observable.fromIterable(request.getRedirectUris().get()), BackpressureStrategy.BUFFER).filter(RxJavaReactorMigrationUtil.toJdkPredicate((java.lang.String redirectUri)->!allowedRedirectUris.contains(redirectUri)))).collect(ArrayList<String>::new, ArrayList::add)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<ArrayList<String>, SingleSource<DynamicClientRegistrationRequest>>toJdkFunction((java.util.ArrayList<java.lang.String> missing)->{
if (!missing.isEmpty()) {
return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException("redirect uris are not allowed according to sector_identifier_uri: " + String.join(" ", missing))));
} else {
return RxJava2Adapter.monoToSingle(Mono.just(request));
}
}).apply(v))))));
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateJKWs(DynamicClientRegistrationRequest request) {
        //The jwks_uri and jwks parameters MUST NOT be used together.
        if(request.getJwks()!=null && request.getJwks().isPresent() && request.getJwksUri()!=null && request.getJwksUri().isPresent()) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The jwks_uri and jwks parameters MUST NOT be used together.")));
        }

        //Check jwks_uri
        if(request.getJwksUri()!=null && request.getJwksUri().isPresent()) {
            return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(jwkService.getKeys(request.getJwksUri().get())).switchIfEmpty(Mono.error(new InvalidClientMetadataException("No JWK found behind jws uri..."))))
                    .flatMapSingle(jwkSet -> {
                        /* Uncomment if we expect to save it as fallback
                        if(jwkSet!=null && jwkSet.isPresent()) {
                            request.setJwks(jwkSet);
                        }
                        */
                        return RxJava2Adapter.monoToSingle(Mono.just(request));
                    });
        }

        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    /**
     * Remove non allowed scopes (if feature is enabled) and then apply default scopes.
     * The scopes validations are done later (validateMetadata) on the process.
     * @param request DynamicClientRegistrationRequest
     * @return DynamicClientRegistrationRequest
     */
    private Single<DynamicClientRegistrationRequest> validateScopes(DynamicClientRegistrationRequest request) {

        final boolean hasAllowedScopes = domain.getOidc()!=null && domain.getOidc().getClientRegistrationSettings()!=null &&
                domain.getOidc().getClientRegistrationSettings().isAllowedScopesEnabled() &&
                domain.getOidc().getClientRegistrationSettings().getAllowedScopes()!=null;

        final boolean hasDefaultScopes = domain.getOidc()!=null && domain.getOidc().getClientRegistrationSettings()!=null &&
                domain.getOidc().getClientRegistrationSettings().getDefaultScopes()!=null &&
                !domain.getOidc().getClientRegistrationSettings().getDefaultScopes().isEmpty();

        //Remove from the request every non allowed scope
        if(request.getScope()!=null && request.getScope().isPresent() && hasAllowedScopes) {

            final Set<String> allowedScopes = new HashSet<>(domain.getOidc().getClientRegistrationSettings().getAllowedScopes());
            final Set<String> requestedScopes = new HashSet<>(request.getScope().get());

            //Remove non allowed scope
            requestedScopes.retainAll(allowedScopes);

            //Update the request
            request.setScope(Optional.of(String.join(SCOPE_DELIMITER,requestedScopes)));
        }

        //Apply default scope if scope metadata is empty
        if((request.getScope()==null || !request.getScope().isPresent() || request.getScope().get().isEmpty()) && hasDefaultScopes) {
            //Add default scopes if needed
            request.setScope(Optional.of(String.join(SCOPE_DELIMITER,domain.getOidc().getClientRegistrationSettings().getDefaultScopes())));
        }

        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateAuthorizationSigningAlgorithm(DynamicClientRegistrationRequest request) {
        // Signing an authorization response is required
        // As per https://bitbucket.org/openid/fapi/src/master/Financial_API_JWT_Secured_Authorization_Response_Mode.md#markdown-header-5-client-metadata
        // If unspecified, the default algorithm to use for signing authorization responses is RS256. The algorithm none is not allowed.
        if (request.getAuthorizationSignedResponseAlg() == null || !request.getAuthorizationSignedResponseAlg().isPresent()) {
            request.setAuthorizationSignedResponseAlg(Optional.of(JWSAlgorithm.RS256.getName()));
        }

        if (!JWAlgorithmUtils.isValidAuthorizationSigningAlg(request.getAuthorizationSignedResponseAlg().get())) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported authorization signing algorithm")));
        }

        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    private Single<DynamicClientRegistrationRequest> validateAuthorizationEncryptionAlgorithm(DynamicClientRegistrationRequest request) {
        if ((request.getAuthorizationEncryptedResponseEnc() != null && request.getAuthorizationEncryptedResponseEnc().isPresent()) &&
                (request.getAuthorizationEncryptedResponseAlg() == null || !request.getAuthorizationEncryptedResponseAlg().isPresent())) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("When authorization_encrypted_response_enc is included, authorization_encrypted_response_alg MUST also be provided")));
        }

        // If authorization_encrypted_response_alg is provided, it must be valid.
        if (request.getAuthorizationEncryptedResponseAlg() != null && request.getAuthorizationEncryptedResponseAlg().isPresent()) {
            if (!JWAlgorithmUtils.isValidAuthorizationResponseAlg(request.getAuthorizationEncryptedResponseAlg().get())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported authorization_encrypted_response_alg value")));
            }

            if (request.getAuthorizationEncryptedResponseEnc() != null && request.getAuthorizationEncryptedResponseEnc().isPresent()) {
                if (!JWAlgorithmUtils.isValidAuthorizationResponseEnc(request.getAuthorizationEncryptedResponseEnc().get())) {
                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unsupported authorization_encrypted_response_enc value")));
                }
            } else {
                // Apply default value if authorization_encrypted_response_alg is informed and not authorization_encrypted_response_enc.
                request.setAuthorizationEncryptedResponseEnc(Optional.of(JWAlgorithmUtils.getDefaultAuthorizationResponseEnc()));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    /**
     * <p>
     *    A client using the "tls_client_auth" authentication method MUST use exactly one of the
     *    below metadata parameters to indicate the certificate subject value that the authorization server is
     *    to expect when authenticating the respective client.
     * </p>
     * <a href="https://tools.ietf.org/html/rfc8705#section-2.1.2">Client Registration Metadata</a>
     *
     * @param request DynamicClientRegistrationRequest
     * @return DynamicClientRegistrationRequest
     */
    private Single<DynamicClientRegistrationRequest> validateTlsClientAuth(DynamicClientRegistrationRequest request) {
        if(request.getTokenEndpointAuthMethod() != null &&
                request.getTokenEndpointAuthMethod().isPresent() &&
                ClientAuthenticationMethod.TLS_CLIENT_AUTH.equalsIgnoreCase(request.getTokenEndpointAuthMethod().get())) {

            if ((request.getTlsClientAuthSubjectDn() == null || ! request.getTlsClientAuthSubjectDn().isPresent()) &&
                    (request.getTlsClientAuthSanDns() == null || ! request.getTlsClientAuthSanDns().isPresent()) &&
                    (request.getTlsClientAuthSanIp() == null || ! request.getTlsClientAuthSanIp().isPresent()) &&
                    (request.getTlsClientAuthSanEmail() == null || ! request.getTlsClientAuthSanEmail().isPresent()) &&
                    (request.getTlsClientAuthSanUri() == null || ! request.getTlsClientAuthSanUri().isPresent())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Missing TLS parameter for tls_client_auth.")));
            }

            if (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent() && (
                    (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent()) ||
                            (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent()) ||
                            (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent()) ||
                            (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            } else if (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent() && (
                    (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent()) ||
                            (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent()) ||
                            (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent()) ||
                            (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            } else if (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent() && (
                    (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent()) ||
                            (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent()) ||
                            (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent()) ||
                            (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            } else if (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent() && (
                    (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent()) ||
                            (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent()) ||
                            (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent()) ||
                            (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            } else if (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent() && (
                    (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent()) ||
                            (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent()) ||
                            (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent()) ||
                            (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            }

            // because only TLS parameter is authorized, we force the missing options to empty to avoid
            // remaining values during an update as the null value for a field do not reset the field...
            if (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent()) {
                request.setTlsClientAuthSanDns(Optional.empty());
                request.setTlsClientAuthSanEmail(Optional.empty());
                request.setTlsClientAuthSanIp(Optional.empty());
                request.setTlsClientAuthSanUri(Optional.empty());
            } else if (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent()) {
                request.setTlsClientAuthSubjectDn(Optional.empty());
                request.setTlsClientAuthSanEmail(Optional.empty());
                request.setTlsClientAuthSanIp(Optional.empty());
                request.setTlsClientAuthSanUri(Optional.empty());
            } else if (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent()) {
                request.setTlsClientAuthSubjectDn(Optional.empty());
                request.setTlsClientAuthSanEmail(Optional.empty());
                request.setTlsClientAuthSanDns(Optional.empty());
                request.setTlsClientAuthSanUri(Optional.empty());
            } else if (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent()) {
                request.setTlsClientAuthSubjectDn(Optional.empty());
                request.setTlsClientAuthSanIp(Optional.empty());
                request.setTlsClientAuthSanDns(Optional.empty());
                request.setTlsClientAuthSanUri(Optional.empty());
            } else if (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent()) {
                request.setTlsClientAuthSubjectDn(Optional.empty());
                request.setTlsClientAuthSanIp(Optional.empty());
                request.setTlsClientAuthSanDns(Optional.empty());
                request.setTlsClientAuthSanEmail(Optional.empty());
            }
        }

        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    /**
     * <p>
     *    This method of mutual-TLS OAuth client authentication is intended to
     *    support client authentication using self-signed certificates.  As a
     *    prerequisite, the client registers its X.509 certificates (using
     *    "jwks" defined in [RFC7591]) or a reference to a trusted source for
     *    its X.509 certificates (using "jwks_uri" from [RFC7591]) with the
     *    authorization server.
     * </p>
     * <a href="https://tools.ietf.org/html/rfc8705#section-2.2.2">Client Registration Metadata</a>
     *
     * @param request DynamicClientRegistrationRequest
     * @return DynamicClientRegistrationRequest
     */
    private Single<DynamicClientRegistrationRequest> validateSelfSignedClientAuth(DynamicClientRegistrationRequest request) {
        if (request.getTokenEndpointAuthMethod() != null &&
                request.getTokenEndpointAuthMethod().isPresent() &&
                ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH.equalsIgnoreCase(request.getTokenEndpointAuthMethod().get())) {
            if ((request.getJwks() == null || !request.getJwks().isPresent()) &&
                    (request.getJwksUri() == null || !request.getJwksUri().isPresent())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The self_signed_tls_client_auth requires at least a jwks or a valid jwks_uri.")));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(request));
    }

    /**
     * Check Uri is well formatted
     * @param uri String
     * @return URI if well formatted, else throw an InvalidClientMetadataException
     */
    private URI formatUrl(String uri) {
        try {
            return UriBuilder.fromHttpUrl(uri).build();
        }
        catch(IllegalArgumentException | URISyntaxException ex) {
            throw new InvalidClientMetadataException(uri+" is not valid.");
        }
    }
}

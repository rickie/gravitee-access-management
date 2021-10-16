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
import com.google.errorprone.annotations.InlineMe;
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

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(request, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Client> create(DynamicClientRegistrationRequest request, String basePath) {
 return RxJava2Adapter.monoToSingle(create_migrated(request, basePath));
}
@Override
    public Mono<Client> create_migrated(DynamicClientRegistrationRequest request, String basePath) {
        //If Dynamic client registration from template is enabled and request contains a software_id
        if(domain.isDynamicClientRegistrationTemplateEnabled() && request.getSoftwareId()!=null && request.getSoftwareId().isPresent()) {
            return this.createClientFromTemplate_migrated(request, basePath);
        }
        return this.createClientFromRequest_migrated(request, basePath);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(toPatch, request, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Client> patch(Client toPatch, DynamicClientRegistrationRequest request, String basePath) {
 return RxJava2Adapter.monoToSingle(patch_migrated(toPatch, request, basePath));
}
@Override
    public Mono<Client> patch_migrated(Client toPatch, DynamicClientRegistrationRequest request, String basePath) {
        return this.validateClientPatchRequest_migrated(request).map(RxJavaReactorMigrationUtil.toJdkFunction(req -> req.patch(toPatch))).flatMap(app->this.applyRegistrationAccessToken_migrated(basePath, app)).flatMap(v->RxJava2Adapter.singleToMono((Single<Client>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Client, Single<Client>>)(io.gravitee.am.model.oidc.Client ident) -> RxJava2Adapter.monoToSingle(clientService.update_migrated(ident))).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(toUpdate, request, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Client> update(Client toUpdate, DynamicClientRegistrationRequest request, String basePath) {
 return RxJava2Adapter.monoToSingle(update_migrated(toUpdate, request, basePath));
}
@Override
    public Mono<Client> update_migrated(Client toUpdate, DynamicClientRegistrationRequest request, String basePath) {
        return this.validateClientRegistrationRequest_migrated(request).map(RxJavaReactorMigrationUtil.toJdkFunction(req -> req.patch(toUpdate))).flatMap(app->this.applyRegistrationAccessToken_migrated(basePath, app)).flatMap(v->RxJava2Adapter.singleToMono((Single<Client>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Client, Single<Client>>)(io.gravitee.am.model.oidc.Client ident) -> RxJava2Adapter.monoToSingle(clientService.update_migrated(ident))).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.delete_migrated(toDelete))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Client> delete(Client toDelete) {
 return RxJava2Adapter.monoToSingle(delete_migrated(toDelete));
}
@Override
    public Mono<Client> delete_migrated(Client toDelete) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToCompletable(this.clientService.delete_migrated(toDelete.getId())).toSingleDefault(toDelete));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.renewSecret_migrated(toRenew, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Client> renewSecret(Client toRenew, String basePath) {
 return RxJava2Adapter.monoToSingle(renewSecret_migrated(toRenew, basePath));
}
@Override
    public Mono<Client> renewSecret_migrated(Client toRenew, String basePath) {
        return clientService.renewClientSecret_migrated(domain.getId(), toRenew.getId()).flatMap(client->applyRegistrationAccessToken_migrated(basePath, client)).flatMap(v->RxJava2Adapter.singleToMono((Single<Client>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Client, Single<Client>>)(io.gravitee.am.model.oidc.Client ident) -> RxJava2Adapter.monoToSingle(clientService.update_migrated(ident))).apply(v)));
    }

    
private Mono<Client> createClientFromRequest_migrated(DynamicClientRegistrationRequest request, String basePath) {
        Client client = new Client();
        client.setClientId(SecureRandomString.generate());
        client.setClientName(ClientServiceImpl.DEFAULT_CLIENT_NAME);
        client.setDomain(domain.getId());

        return this.validateClientRegistrationRequest_migrated(request).map(RxJavaReactorMigrationUtil.toJdkFunction(req -> req.patch(client))).flatMap(v->applyDefaultIdentityProvider_migrated(v)).flatMap(v->applyDefaultCertificateProvider_migrated(v)).flatMap(v->applyAccessTokenValidity_migrated(v)).flatMap(app->this.applyRegistrationAccessToken_migrated(basePath, app)).flatMap(v->RxJava2Adapter.singleToMono((Single<Client>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Client, Single<Client>>)(io.gravitee.am.model.oidc.Client ident) -> RxJava2Adapter.monoToSingle(clientService.create_migrated(ident))).apply(v)));
    }

    
private Mono<Client> applyAccessTokenValidity_migrated(Client client) {
        client.setAccessTokenValiditySeconds(environment.getProperty(OPENID_DCR_ACCESS_TOKEN_VALIDITY, Integer.class, domain.useFapiBrazilProfile() ? FAPI_OPENBANKING_BRAZIL_DEFAULT_ACCESS_TOKEN_VALIDITY : Client.DEFAULT_ACCESS_TOKEN_VALIDITY_SECONDS));
        client.setRefreshTokenValiditySeconds(environment.getProperty(OPENID_DCR_REFRESH_TOKEN_VALIDITY, Integer.class, Client.DEFAULT_REFRESH_TOKEN_VALIDITY_SECONDS));
        client.setIdTokenValiditySeconds(environment.getProperty(OPENID_DCR_ID_TOKEN_VALIDITY, Integer.class, Client.DEFAULT_ID_TOKEN_VALIDITY_SECONDS));
        return Mono.just(client);
    }

    
private Mono<Client> createClientFromTemplate_migrated(DynamicClientRegistrationRequest request, String basePath) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(clientService.findById_migrated(request.getSoftwareId().get()).switchIfEmpty(Mono.error(new InvalidClientMetadataException("No template found for software_id "+request.getSoftwareId().get()))))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Client, SingleSource<Client>>toJdkFunction((io.gravitee.am.model.oidc.Client ident) -> RxJava2Adapter.monoToSingle(sanitizeTemplate_migrated(ident))).apply(y)))))).map(RxJavaReactorMigrationUtil.toJdkFunction(request::patch)).flatMap(app->this.applyRegistrationAccessToken_migrated(basePath, app)).flatMap(v->clientService.create_migrated(v)).flatMap(client->copyForms_migrated(request.getSoftwareId().get(), client)).flatMap(client->copyEmails_migrated(request.getSoftwareId().get(), client));
    }

    
private Mono<Client> sanitizeTemplate_migrated(Client template) {
        if(!template.isTemplate()) {
            return Mono.error(new InvalidClientMetadataException("Client behind software_id is not a template"));
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

        return Mono.just(template);
    }

    
private Mono<Client> copyForms_migrated(String sourceId, Client client) {
        return formService.copyFromClient_migrated(domain.getId(), sourceId, client.getId()).flatMap(irrelevant->Mono.just(client));
    }

    
private Mono<Client> copyEmails_migrated(String sourceId, Client client) {
        return emailTemplateService.copyFromClient_migrated(domain.getId(), sourceId, client.getId()).collectList().flatMap(irrelevant->Mono.just(client));
    }

    
private Mono<Client> applyDefaultIdentityProvider_migrated(Client client) {
        return identityProviderService.findByDomain_migrated(client.getDomain()).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(identityProviders -> {
                if(identityProviders!=null && !identityProviders.isEmpty()) {
                    client.setIdentities(Collections.singleton(identityProviders.get(0).getId()));
                }
                return client;
            }));
    }

    
private Mono<Client> applyDefaultCertificateProvider_migrated(Client client) {
        return certificateService.findByDomain_migrated(client.getDomain()).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(certificates -> {
                    if(certificates!=null && !certificates.isEmpty()) {
                        client.setCertificate(certificates.get(0).getId());
                    }
                    return client;
                }));
    }

    
private Mono<Client> applyRegistrationAccessToken_migrated(String basePath, Client client) {

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

        return jwtService.encode_migrated(jwt, client).map(RxJavaReactorMigrationUtil.toJdkFunction(token -> {
                    client.setRegistrationAccessToken(token);
                    client.setRegistrationClientUri(openIDProviderMetadata.getRegistrationEndpoint()+"/"+client.getClientId());
                    return client;
                }));
    }

    
private Mono<DynamicClientRegistrationRequest> validateClientRegistrationRequest_migrated(final DynamicClientRegistrationRequest request) {
        LOGGER.debug("Validating dynamic client registration payload");
        return this.validateClientRegistrationRequest_migrated(request, false);
    }

    
private Mono<DynamicClientRegistrationRequest> validateClientPatchRequest_migrated(DynamicClientRegistrationRequest request) {
        LOGGER.debug("Validating dynamic client registration payload : patch");
        //redirect_uri is mandatory in the request, but in case of patch we may omit it...
        return this.validateClientRegistrationRequest_migrated(request, true);
    }

    
private Mono<DynamicClientRegistrationRequest> validateClientRegistrationRequest_migrated(final DynamicClientRegistrationRequest request, boolean isPatch) {
        if(request==null) {
            return Mono.error(new InvalidClientMetadataException());
        }

        return this.validateRedirectUri_migrated(request, isPatch).flatMap(v->validateScopes_migrated(v)).flatMap(v->validateGrantType_migrated(v)).flatMap(v->validateResponseType_migrated(v)).flatMap(v->validateSubjectType_migrated(v)).flatMap(v->validateRequestUri_migrated(v)).flatMap(v->validateSectorIdentifierUri_migrated(v)).flatMap(v->validateJKWs_migrated(v)).flatMap(v->validateUserinfoSigningAlgorithm_migrated(v)).flatMap(v->validateUserinfoEncryptionAlgorithm_migrated(v)).flatMap(v->validateIdTokenSigningAlgorithm_migrated(v)).flatMap(v->validateIdTokenEncryptionAlgorithm_migrated(v)).flatMap(v->validateTlsClientAuth_migrated(v)).flatMap(v->validateSelfSignedClientAuth_migrated(v)).flatMap(v->validateAuthorizationSigningAlgorithm_migrated(v)).flatMap(v->validateAuthorizationEncryptionAlgorithm_migrated(v)).flatMap(v->validateRequestObjectSigningAlgorithm_migrated(v)).flatMap(v->validateRequestObjectEncryptionAlgorithm_migrated(v)).flatMap(v->RxJava2Adapter.singleToMono((Single<DynamicClientRegistrationRequest>)RxJavaReactorMigrationUtil.toJdkFunction((Function<DynamicClientRegistrationRequest, Single<DynamicClientRegistrationRequest>>)(io.gravitee.am.gateway.handler.oidc.service.clientregistration.DynamicClientRegistrationRequest ident) -> RxJava2Adapter.monoToSingle(enforceWithSoftwareStatement_migrated(ident))).apply(v)));
    }

    
private Mono<DynamicClientRegistrationRequest> enforceWithSoftwareStatement_migrated(DynamicClientRegistrationRequest request) {
        if (this.domain.useFapiBrazilProfile()) {
            if (request.getSoftwareStatement() == null || request.getSoftwareStatement().isEmpty()) {
                return Mono.error(new InvalidClientMetadataException("software_statement is required"));
            }

            final String directoryJwksUri = environment.getProperty(FAPI_OPENBANKING_BRAZIL_DIRECTORY_JWKS_URI);
            if (Strings.isNullOrEmpty(directoryJwksUri)) {
                return Mono.error(new InvalidClientMetadataException("No jwks_uri for OpenBanking Directory, unable to validate software_statement"));
            }

            try {

                com.nimbusds.jwt.JWT jwt = JWTParser.parse(request.getSoftwareStatement().get());
                if (jwt instanceof SignedJWT) {
                    final SignedJWT signedJWT = (SignedJWT) jwt;
                    if (isSignAlgCompliantWithFapi(signedJWT.getHeader().getAlgorithm().getName())) {
                        return jwkService.getKeys_migrated(directoryJwksUri).flatMap(z->jwkService.getKey_migrated(z, signedJWT.getHeader().getKeyID())).switchIfEmpty(Mono.error(new TechnicalManagementException("Invalid jwks_uri for OpenBanking Directory"))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(jwk -> jwsService.isValidSignature(signedJWT, jwk))).switchIfEmpty(Mono.error(new InvalidClientMetadataException("Invalid signature for software_statement"))).map(RxJavaReactorMigrationUtil.toJdkFunction(__ -> {
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
                                }));
                    }
                }

                return Mono.error(new InvalidClientMetadataException("software_statement isn't signed or doesn't use PS256"));

            } catch (ParseException pe) {
                return Mono.error(new InvalidClientMetadataException("signature of software_statement is invalid"));
            }
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateRedirectUri_migrated(DynamicClientRegistrationRequest request, boolean isPatch) {

        //Except for patching a client, redirect_uris metadata is required but may be null or empty.
        if(!isPatch && request.getRedirectUris() == null) {
            return Mono.error(new InvalidRedirectUriException());
        }

        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateResponseType_migrated(DynamicClientRegistrationRequest request) {
        //if response_type provided, they must be valid.
        if(request.getResponseTypes()!=null) {
            if(!ResponseTypeUtils.isSupportedResponseType(request.getResponseTypes().orElse(Collections.emptyList()))) {
                return Mono.error(new InvalidClientMetadataException("Invalid response type."));
            }
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateGrantType_migrated(DynamicClientRegistrationRequest request) {
        //if grant_type provided, they must be valid.
        if(request.getGrantTypes()!=null) {
            if (!GrantTypeUtils.isSupportedGrantType(request.getGrantTypes().orElse(Collections.emptyList()))) {
                return Mono.error(new InvalidClientMetadataException("Missing or invalid grant type."));
            }
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateSubjectType_migrated(DynamicClientRegistrationRequest request) {
        //if subject_type is provided, it must be valid.
        if(request.getSubjectType()!=null && request.getSubjectType().isPresent()) {
            if(!SubjectTypeUtils.isValidSubjectType(request.getSubjectType().get())) {
                return Mono.error(new InvalidClientMetadataException("Unsupported subject type"));
            }
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateUserinfoSigningAlgorithm_migrated(DynamicClientRegistrationRequest request) {
        //if userinfo_signed_response_alg is provided, it must be valid.
        if(request.getUserinfoSignedResponseAlg()!=null && request.getUserinfoSignedResponseAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidUserinfoSigningAlg(request.getUserinfoSignedResponseAlg().get())) {
                return Mono.error(new InvalidClientMetadataException("Unsupported userinfo signing algorithm"));
            }
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateUserinfoEncryptionAlgorithm_migrated(DynamicClientRegistrationRequest request) {
        if(request.getUserinfoEncryptedResponseEnc()!=null && request.getUserinfoEncryptedResponseAlg()==null) {
            return Mono.error(new InvalidClientMetadataException("When userinfo_encrypted_response_enc is included, userinfo_encrypted_response_alg MUST also be provided"));
        }
        //if userinfo_encrypted_response_alg is provided, it must be valid.
        if(request.getUserinfoEncryptedResponseAlg()!=null && request.getUserinfoEncryptedResponseAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidUserinfoResponseAlg(request.getUserinfoEncryptedResponseAlg().get())) {
                return Mono.error(new InvalidClientMetadataException("Unsupported userinfo_encrypted_response_alg value"));
            }
            if(request.getUserinfoEncryptedResponseEnc()!=null && request.getUserinfoEncryptedResponseEnc().isPresent()) {
                if(!JWAlgorithmUtils.isValidUserinfoResponseEnc(request.getUserinfoEncryptedResponseEnc().get())) {
                    return Mono.error(new InvalidClientMetadataException("Unsupported userinfo_encrypted_response_enc value"));
                }
            }
            else {
                //Apply default value if userinfo_encrypted_response_alg is informed and not userinfo_encrypted_response_enc.
                request.setUserinfoEncryptedResponseEnc(Optional.of(JWAlgorithmUtils.getDefaultUserinfoResponseEnc()));
            }
        }
        return Mono.just(request);
    }


    
private Mono<DynamicClientRegistrationRequest> validateRequestObjectSigningAlgorithm_migrated(DynamicClientRegistrationRequest request) {
        //if userinfo_signed_response_alg is provided, it must be valid.
        if(request.getRequestObjectSigningAlg() !=null && request.getRequestObjectSigningAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidRequestObjectSigningAlg(request.getRequestObjectSigningAlg().get())) {
                return Mono.error(new InvalidClientMetadataException("Unsupported request object signing algorithm"));
            }

            if (this.domain.usePlainFapiProfile() && !isSignAlgCompliantWithFapi(request.getRequestObjectSigningAlg().get())) {
                return Mono.error(new InvalidClientMetadataException("request_object_signing_alg shall be PS256"));
            }
        }

        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateRequestObjectEncryptionAlgorithm_migrated(DynamicClientRegistrationRequest request) {
        if(request.getRequestObjectEncryptionEnc() !=null && request.getRequestObjectEncryptionAlg()==null) {
            return Mono.error(new InvalidClientMetadataException("When request_object_encryption_enc is included, request_object_encryption_alg MUST also be provided"));
        }

        //if userinfo_encrypted_response_alg is provided, it must be valid.
        if(request.getRequestObjectEncryptionAlg()!=null && request.getRequestObjectEncryptionAlg().isPresent()) {
            if(!domain.useFapiBrazilProfile() && !JWAlgorithmUtils.isValidRequestObjectAlg(request.getRequestObjectEncryptionAlg().get())) {
                return Mono.error(new InvalidClientMetadataException("Unsupported request_object_encryption_alg value"));
            }

            if(request.getRequestObjectEncryptionEnc()!=null && request.getRequestObjectEncryptionEnc().isPresent()) {
                if(!JWAlgorithmUtils.isValidRequestObjectEnc(request.getRequestObjectEncryptionEnc().get())) {
                    return Mono.error(new InvalidClientMetadataException("Unsupported request_object_encryption_enc value"));
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

        return Mono.just(request);
    }


    
private Mono<DynamicClientRegistrationRequest> validateIdTokenSigningAlgorithm_migrated(DynamicClientRegistrationRequest request) {
        //if userinfo_signed_response_alg is provided, it must be valid.
        if(request.getIdTokenSignedResponseAlg()!=null && request.getIdTokenSignedResponseAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidIdTokenSigningAlg(request.getIdTokenSignedResponseAlg().get())) {
                return Mono.error(new InvalidClientMetadataException("Unsupported id_token signing algorithm"));
            }
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateIdTokenEncryptionAlgorithm_migrated(DynamicClientRegistrationRequest request) {
        if(request.getIdTokenEncryptedResponseEnc()!=null && request.getIdTokenEncryptedResponseAlg()==null) {
            return Mono.error(new InvalidClientMetadataException("When id_token_encrypted_response_enc is included, id_token_encrypted_response_alg MUST also be provided"));
        }
        //if id_token_encrypted_response_alg is provided, it must be valid.
        if(request.getIdTokenEncryptedResponseAlg()!=null && request.getIdTokenEncryptedResponseAlg().isPresent()) {
            if(!JWAlgorithmUtils.isValidIdTokenResponseAlg(request.getIdTokenEncryptedResponseAlg().get())) {
                return Mono.error(new InvalidClientMetadataException("Unsupported id_token_encrypted_response_alg value"));
            }
            if(request.getIdTokenEncryptedResponseEnc()!=null && request.getIdTokenEncryptedResponseEnc().isPresent()) {
                if(!JWAlgorithmUtils.isValidIdTokenResponseEnc(request.getIdTokenEncryptedResponseEnc().get())) {
                    return Mono.error(new InvalidClientMetadataException("Unsupported id_token_encrypted_response_enc value"));
                }
            }
            else {
                //Apply default value if id_token_encrypted_response_alg is informed and not id_token_encrypted_response_enc.
                request.setIdTokenEncryptedResponseEnc(Optional.of(JWAlgorithmUtils.getDefaultIdTokenResponseEnc()));
            }
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateRequestUri_migrated(DynamicClientRegistrationRequest request) {
        //Check request_uri well formated
        if(request.getRequestUris()!=null && request.getRequestUris().isPresent()) {
            try {
                //throw exception if uri mal formated
                request.getRequestUris().get().stream().forEach(this::formatUrl);
            } catch (InvalidClientMetadataException err) {
                return Mono.error(new InvalidClientMetadataException("request_uris: "+err.getMessage()));
            }
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateSectorIdentifierUri_migrated(DynamicClientRegistrationRequest request) {
        //if sector_identifier_uri is provided, then retrieve content and validate redirect_uris among this list.
        if(request.getSectorIdentifierUri()!=null && request.getSectorIdentifierUri().isPresent()) {

            URI uri;
            try {
                //throw exception if uri mal formated
                uri = formatUrl(request.getSectorIdentifierUri().get());
            } catch (InvalidClientMetadataException err) {
                return Mono.error(new InvalidClientMetadataException("sector_identifier_uri: "+err.getMessage()));
            }

            if(!uri.getScheme().equalsIgnoreCase("https")) {
                return Mono.error(new InvalidClientMetadataException("Scheme must be https for sector_identifier_uri : "+request.getSectorIdentifierUri().get()));
            }

            return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(client.getAbs(uri.toString())
                    .rxSend()).map(RxJavaReactorMigrationUtil.toJdkFunction(HttpResponse::bodyAsString)).map(RxJavaReactorMigrationUtil.toJdkFunction(JsonArray::new)))
                    .onErrorResumeNext(RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Unable to parse sector_identifier_uri : "+ uri.toString()))))).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(Flowable::fromIterable)))
                    .cast(String.class)
                    .collect(HashSet::new,HashSet::add)).flatMap(allowedRedirectUris->RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToObservable(RxJava2Adapter.observableToFlux(Observable.fromIterable(request.getRedirectUris().get()), BackpressureStrategy.BUFFER).filter(RxJavaReactorMigrationUtil.toJdkPredicate((java.lang.String redirectUri)->!allowedRedirectUris.contains(redirectUri)))).collect(ArrayList<String>::new, ArrayList::add)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<ArrayList<String>, SingleSource<DynamicClientRegistrationRequest>>toJdkFunction((java.util.ArrayList<java.lang.String> missing)->{
if (!missing.isEmpty()) {
return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException("redirect uris are not allowed according to sector_identifier_uri: " + String.join(" ", missing))));
} else {
return RxJava2Adapter.monoToSingle(Mono.just(request));
}
}).apply(v)))));
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateJKWs_migrated(DynamicClientRegistrationRequest request) {
        //The jwks_uri and jwks parameters MUST NOT be used together.
        if(request.getJwks()!=null && request.getJwks().isPresent() && request.getJwksUri()!=null && request.getJwksUri().isPresent()) {
            return Mono.error(new InvalidClientMetadataException("The jwks_uri and jwks parameters MUST NOT be used together."));
        }

        //Check jwks_uri
        if(request.getJwksUri()!=null && request.getJwksUri().isPresent()) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(jwkService.getKeys_migrated(request.getJwksUri().get()).switchIfEmpty(Mono.error(new InvalidClientMetadataException("No JWK found behind jws uri..."))))
                    .flatMapSingle(jwkSet -> {
                        /* Uncomment if we expect to save it as fallback
                        if(jwkSet!=null && jwkSet.isPresent()) {
                            request.setJwks(jwkSet);
                        }
                        */
                        return RxJava2Adapter.monoToSingle(Mono.just(request));
                    }));
        }

        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateScopes_migrated(DynamicClientRegistrationRequest request) {

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

        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateAuthorizationSigningAlgorithm_migrated(DynamicClientRegistrationRequest request) {
        // Signing an authorization response is required
        // As per https://bitbucket.org/openid/fapi/src/master/Financial_API_JWT_Secured_Authorization_Response_Mode.md#markdown-header-5-client-metadata
        // If unspecified, the default algorithm to use for signing authorization responses is RS256. The algorithm none is not allowed.
        if (request.getAuthorizationSignedResponseAlg() == null || !request.getAuthorizationSignedResponseAlg().isPresent()) {
            request.setAuthorizationSignedResponseAlg(Optional.of(JWSAlgorithm.RS256.getName()));
        }

        if (!JWAlgorithmUtils.isValidAuthorizationSigningAlg(request.getAuthorizationSignedResponseAlg().get())) {
            return Mono.error(new InvalidClientMetadataException("Unsupported authorization signing algorithm"));
        }

        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateAuthorizationEncryptionAlgorithm_migrated(DynamicClientRegistrationRequest request) {
        if ((request.getAuthorizationEncryptedResponseEnc() != null && request.getAuthorizationEncryptedResponseEnc().isPresent()) &&
                (request.getAuthorizationEncryptedResponseAlg() == null || !request.getAuthorizationEncryptedResponseAlg().isPresent())) {
            return Mono.error(new InvalidClientMetadataException("When authorization_encrypted_response_enc is included, authorization_encrypted_response_alg MUST also be provided"));
        }

        // If authorization_encrypted_response_alg is provided, it must be valid.
        if (request.getAuthorizationEncryptedResponseAlg() != null && request.getAuthorizationEncryptedResponseAlg().isPresent()) {
            if (!JWAlgorithmUtils.isValidAuthorizationResponseAlg(request.getAuthorizationEncryptedResponseAlg().get())) {
                return Mono.error(new InvalidClientMetadataException("Unsupported authorization_encrypted_response_alg value"));
            }

            if (request.getAuthorizationEncryptedResponseEnc() != null && request.getAuthorizationEncryptedResponseEnc().isPresent()) {
                if (!JWAlgorithmUtils.isValidAuthorizationResponseEnc(request.getAuthorizationEncryptedResponseEnc().get())) {
                    return Mono.error(new InvalidClientMetadataException("Unsupported authorization_encrypted_response_enc value"));
                }
            } else {
                // Apply default value if authorization_encrypted_response_alg is informed and not authorization_encrypted_response_enc.
                request.setAuthorizationEncryptedResponseEnc(Optional.of(JWAlgorithmUtils.getDefaultAuthorizationResponseEnc()));
            }
        }
        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateTlsClientAuth_migrated(DynamicClientRegistrationRequest request) {
        if(request.getTokenEndpointAuthMethod() != null &&
                request.getTokenEndpointAuthMethod().isPresent() &&
                ClientAuthenticationMethod.TLS_CLIENT_AUTH.equalsIgnoreCase(request.getTokenEndpointAuthMethod().get())) {

            if ((request.getTlsClientAuthSubjectDn() == null || ! request.getTlsClientAuthSubjectDn().isPresent()) &&
                    (request.getTlsClientAuthSanDns() == null || ! request.getTlsClientAuthSanDns().isPresent()) &&
                    (request.getTlsClientAuthSanIp() == null || ! request.getTlsClientAuthSanIp().isPresent()) &&
                    (request.getTlsClientAuthSanEmail() == null || ! request.getTlsClientAuthSanEmail().isPresent()) &&
                    (request.getTlsClientAuthSanUri() == null || ! request.getTlsClientAuthSanUri().isPresent())) {
                return Mono.error(new InvalidClientMetadataException("Missing TLS parameter for tls_client_auth."));
            }

            if (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent() && (
                    (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent()) ||
                            (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent()) ||
                            (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent()) ||
                            (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
            } else if (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent() && (
                    (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent()) ||
                            (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent()) ||
                            (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent()) ||
                            (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
            } else if (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent() && (
                    (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent()) ||
                            (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent()) ||
                            (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent()) ||
                            (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
            } else if (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent() && (
                    (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent()) ||
                            (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent()) ||
                            (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent()) ||
                            (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
            } else if (request.getTlsClientAuthSanUri() != null && request.getTlsClientAuthSanUri().isPresent() && (
                    (request.getTlsClientAuthSubjectDn() != null && request.getTlsClientAuthSubjectDn().isPresent()) ||
                            (request.getTlsClientAuthSanDns() != null && request.getTlsClientAuthSanDns().isPresent()) ||
                            (request.getTlsClientAuthSanIp() != null && request.getTlsClientAuthSanIp().isPresent()) ||
                            (request.getTlsClientAuthSanEmail() != null && request.getTlsClientAuthSanEmail().isPresent()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
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

        return Mono.just(request);
    }

    
private Mono<DynamicClientRegistrationRequest> validateSelfSignedClientAuth_migrated(DynamicClientRegistrationRequest request) {
        if (request.getTokenEndpointAuthMethod() != null &&
                request.getTokenEndpointAuthMethod().isPresent() &&
                ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH.equalsIgnoreCase(request.getTokenEndpointAuthMethod().get())) {
            if ((request.getJwks() == null || !request.getJwks().isPresent()) &&
                    (request.getJwksUri() == null || !request.getJwksUri().isPresent())) {
                return Mono.error(new InvalidClientMetadataException("The self_signed_tls_client_auth requires at least a jwks or a valid jwks_uri."));
            }
        }
        return Mono.just(request);
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

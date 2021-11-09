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
package io.gravitee.am.gateway.handler.oauth2.service.assertion.impl;

import static io.gravitee.am.common.oidc.ClientAuthenticationMethod.JWT_BEARER;
import static io.gravitee.am.gateway.handler.oidc.service.utils.JWAlgorithmUtils.isSignAlgCompliantWithFapi;


import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.gravitee.am.common.oidc.ClientAuthenticationMethod;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.oauth2.exception.InvalidClientException;
import io.gravitee.am.gateway.handler.oauth2.exception.ServerErrorException;
import io.gravitee.am.gateway.handler.oauth2.service.assertion.ClientAssertionService;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDDiscoveryService;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDProviderMetadata;
import io.gravitee.am.gateway.handler.oidc.service.jwk.JWKService;
import io.gravitee.am.gateway.handler.oidc.service.jws.JWSService;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.jose.JWK;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.oidc.Keys;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.functions.Function;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * Client assertion as described for <a href="https://tools.ietf.org/html/rfc7521#section-4.2">oauth2 assertion framework</a>
 * and <a href="https://openid.net/specs/openid-connect-core-1_0.html#ClientAuthentication">openid client authentication specs</a>
 *
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class ClientAssertionServiceImpl implements ClientAssertionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAssertionServiceImpl.class);

    private static final InvalidClientException NOT_VALID = new InvalidClientException("assertion is not valid");

    @Autowired
    private ClientSyncService clientSyncService;

    @Autowired
    private JWKService jwkService;

    @Autowired
    private JWSService jwsService;

    @Autowired
    private OpenIDDiscoveryService openIDDiscoveryService;

    @Autowired
    private Domain domain;

    
@Override
    public Mono<Client> assertClient_migrated(String assertionType, String assertion, String basePath) {

        InvalidClientException unsupportedAssertionType = new InvalidClientException("Unknown or unsupported assertion_type");

        if (assertionType == null || assertionType.isEmpty()) {
            return Mono.error(unsupportedAssertionType);
        }

        if (JWT_BEARER.equals(assertionType)) {
            return this.validateJWT_migrated(assertion, basePath).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<JWT, MaybeSource<Client>>toJdkFunction(new Function<JWT, MaybeSource<Client>>() {
                        @Override
                        public MaybeSource<Client> apply(JWT jwt) throws Exception {
                            // Handle client_secret_key client authentication
                            if (JWSAlgorithm.Family.HMAC_SHA.contains(jwt.getHeader().getAlgorithm())) {
                                return RxJava2Adapter.monoToMaybe(validateSignatureWithHMAC_migrated(jwt));
                            } else {
                                // Handle private_key_jwt client authentication
                                return RxJava2Adapter.monoToMaybe(validateSignatureWithPublicKey_migrated(jwt));
                            }
                        }
                    }).apply(v))));
        }

        return Mono.error(unsupportedAssertionType);
    }

    
private Mono<JWT> validateJWT_migrated(String assertion, String basePath) {
        try {
            JWT jwt = JWTParser.parse(assertion);

            String iss = jwt.getJWTClaimsSet().getIssuer();
            String sub = jwt.getJWTClaimsSet().getSubject();
            List<String> aud = jwt.getJWTClaimsSet().getAudience();
            Date exp = jwt.getJWTClaimsSet().getExpirationTime();

            if  (iss == null || iss.isEmpty() || sub == null || sub.isEmpty() || aud == null || aud.isEmpty() || exp == null) {
                return Mono.error(NOT_VALID);
            }

            if (exp.before(Date.from(Instant.now()))) {
                return Mono.error(new InvalidClientException("assertion has expired"));
            }

            //Check audience, here we expect to have absolute token endpoint path.
            OpenIDProviderMetadata discovery = openIDDiscoveryService.getConfiguration(basePath);
            if (discovery == null || discovery.getTokenEndpoint() == null) {
                return Mono.error(new ServerErrorException("Unable to retrieve discovery token endpoint."));
            }

            // OIDC specifies that "The Audience SHOULD be the URL of the Authorization Server's Token Endpoint."
            // https://openid.net/specs/openid-connect-core-1_0.html#ClientAuthentication
            // BUT the PAR specification specify the usage of the Issuer value, Token endpoint or PAR endpoint.
            // https://tools.ietf.org/id/draft-lodderstedt-oauth-par-00.html#pushed-authorization-request-endpoint
            if (aud.stream().filter(discovery.getTokenEndpoint()::equals).count() == 0 &&
                    (discovery.getIssuer() != null && aud.stream().filter(discovery.getIssuer()::equals).count() == 0) &&
                    (discovery.getParEndpoint() != null && aud.stream().filter(discovery.getParEndpoint()::equals).count() == 0)) {
                return Mono.error(NOT_VALID);
            }

            if (this.domain.usePlainFapiProfile() && !isSignAlgCompliantWithFapi(jwt.getHeader().getAlgorithm().getName())) {
                return Mono.error(new InvalidClientException("JWT Assertion must be signed with PS256"));
            }

            return Mono.just(jwt);
        } catch (ParseException pe) {
            return Mono.error(NOT_VALID);
        }
    }

    
private Mono<Client> validateSignatureWithPublicKey_migrated(JWT jwt) {
        try {
            String clientId = jwt.getJWTClaimsSet().getSubject();
            SignedJWT signedJWT = (SignedJWT) jwt;

            return this.clientSyncService.findByClientId_migrated(clientId).switchIfEmpty(Mono.error(new InvalidClientException("Missing or invalid client"))).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<Client, MaybeSource<Client>>toJdkFunction(client -> {
                        if (client.getTokenEndpointAuthMethod() == null ||
                                ClientAuthenticationMethod.PRIVATE_KEY_JWT.equalsIgnoreCase(client.getTokenEndpointAuthMethod())) {
                            return RxJava2Adapter.monoToMaybe(this.getClientJwkSet_migrated(client).switchIfEmpty(Mono.error(new InvalidClientException("No jwk keys available on client"))).flatMap(z->jwkService.getKey_migrated(z, signedJWT.getHeader().getKeyID())).switchIfEmpty(Mono.error(new InvalidClientException("Unable to validate client, no matching key."))).flatMap(t->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<JWK, MaybeSource<Client>>toJdkFunction(jwk -> {
                                        if (jwsService.isValidSignature(signedJWT, jwk)) {
                                            return RxJava2Adapter.monoToMaybe(Mono.just(client));
                                        }
                                        return RxJava2Adapter.monoToMaybe(Mono.error(new InvalidClientException("Unable to validate client, assertion signature is not valid.")));
                                    }).apply(t)))));
                        } else {
                            return RxJava2Adapter.monoToMaybe(Mono.error(new InvalidClientException("Invalid client: missing or unsupported authentication method")));
                        }
                    }).apply(v))));
        } catch (ClassCastException | ParseException ex) {
            LOGGER.error(ex.getMessage(),ex);
            return Mono.error(NOT_VALID);
        }
        catch (IllegalArgumentException ex) {
            return Mono.error(new InvalidClientException(ex.getMessage()));
        }
    }

    
private Mono<Client> validateSignatureWithHMAC_migrated(JWT jwt) {
        try {
            Algorithm algorithm = jwt.getHeader().getAlgorithm();

            if (algorithm instanceof JWSAlgorithm) {
                JWSAlgorithm jwsAlgorithm = JWSAlgorithm.parse(jwt.getHeader().getAlgorithm().getName());
                if (jwsAlgorithm != JWSAlgorithm.HS256 && jwsAlgorithm != JWSAlgorithm.HS384 && jwsAlgorithm != JWSAlgorithm.HS512) {
                    return Mono.error(new InvalidClientException("Unable to validate client, assertion signature is not valid."));
                }
            } else {
                return Mono.error(new InvalidClientException("Unable to validate client, assertion signature is not valid."));
            }

            String clientId = jwt.getJWTClaimsSet().getSubject();
            SignedJWT signedJWT = (SignedJWT) jwt;



            return this.clientSyncService.findByClientId_migrated(clientId).switchIfEmpty(Mono.error(new InvalidClientException("Missing or invalid client"))).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<Client, MaybeSource<Client>>toJdkFunction(client -> {
                        try {
                            // Ensure to validate JWT using client_secret_key only if client is authorized to use this auth method
                            if (client.getTokenEndpointAuthMethod() == null ||
                                    ClientAuthenticationMethod.CLIENT_SECRET_JWT.equalsIgnoreCase(client.getTokenEndpointAuthMethod())) {
                                JWSVerifier verifier = new MACVerifier(client.getClientSecret());
                                if (signedJWT.verify(verifier)) {
                                    return RxJava2Adapter.monoToMaybe(Mono.just(client));
                                }
                            } else {
                                return RxJava2Adapter.monoToMaybe(Mono.error(new InvalidClientException("Invalid client: missing or unsupported authentication method")));
                            }
                        } catch (JOSEException josee) {
                        }

                        return RxJava2Adapter.monoToMaybe(Mono.error(new InvalidClientException("Unable to validate client, assertion signature is not valid.")));
                    }).apply(v))));
        } catch (ClassCastException | ParseException ex) {
            LOGGER.error(ex.getMessage(),ex);
            return Mono.error(NOT_VALID);
        }
        catch (IllegalArgumentException ex) {
            return Mono.error(new InvalidClientException(ex.getMessage()));
        }
    }

    
private Mono<Keys> getClientJwkSet_migrated(Client client) {
        if(client.getJwksUri()!=null && !client.getJwksUri().trim().isEmpty()) {
            return jwkService.getKeys_migrated(client.getJwksUri());
        }
        else if(client.getJwks()!=null) {
            return Mono.just(client.getJwks());
        }
        return Mono.empty();
    }
}

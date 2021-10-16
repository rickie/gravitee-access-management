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
package io.gravitee.am.gateway.handler.vertx.auth.webauthn.store;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.jwt.Claims;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.jwt.JWTBuilder;
import io.gravitee.am.model.Credential;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.service.CredentialService;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.ext.auth.webauthn.Authenticator;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RepositoryCredentialStore {

    @Autowired
    private CredentialService credentialService;

    @Autowired
    @Qualifier("managementJwtBuilder")
    private JWTBuilder jwtBuilder;

    @Autowired
    private Domain domain;

    public Future<List<Authenticator>> fetch(Authenticator query) {
        Promise<List<Authenticator>> promise = Promise.promise();

        Single<List<Credential>> fetchCredentials = query.getUserName() != null ?
                RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(credentialService.findByUsername_migrated(ReferenceType.DOMAIN, domain.getId(), query.getUserName()))).collectList()) :
                RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(credentialService.findByCredentialId_migrated(ReferenceType.DOMAIN, domain.getId(), query.getCredID()))).collectList());

        RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(fetchCredentials).flatMap(v->RxJava2Adapter.singleToMono((Single<List<Authenticator>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Credential>, Single<List<Authenticator>>>)credentials -> {
                    if (credentials.isEmpty() && query.getUserName() != null) {
                        // If, when initiating an authentication ceremony, there is no account matching the provided username,
                        // continue the ceremony by invoking navigator.credentials.get() using a syntactically valid
                        // PublicKeyCredentialRequestOptions object that is populated with plausible imaginary values.
                        // Prevent 14.6.2. Username Enumeration (https://www.w3.org/TR/webauthn-2/#sctn-username-enumeration)
                        return Single.zip(
                                RxJava2Adapter.monoToSingle(generateCredID_migrated(query.getUserName(), Claims.sub)),
                                RxJava2Adapter.monoToSingle(generateCredID_migrated(query.getUserName(), StandardClaims.PREFERRED_USERNAME)), (part1, part2) -> {
                                    MessageDigest md = MessageDigest.getInstance("SHA-512");
                                    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
                                    secureRandom.setSeed(part1.getBytes());
                                    int nbDevices = secureRandom.nextInt(3) + 1;
                                    int deviceType = secureRandom.nextInt(2) + 1;
                                    List<Authenticator> authenticators = new ArrayList<>(nbDevices);
                                    for (int i = 0; i < nbDevices; i++) {
                                        byte[] salt = new byte[16];
                                        secureRandom.nextBytes(salt);
                                        md.update(salt);
                                        String initialValue = shiftValue(part2, i);
                                        Authenticator authenticator = new Authenticator();
                                        authenticator.setUserName(query.getUserName());
                                        if (deviceType == 1) {
                                            if (i < 2) {
                                                if (initialValue.length() > 27) {
                                                    initialValue = initialValue.substring(0, 27);
                                                }
                                                authenticator.setCredID(initialValue);
                                            } else {
                                                authenticator.setCredID(createCredID(md, initialValue, part1));
                                            }
                                        } else {
                                            if (i < 2) {
                                                authenticator.setCredID(createCredID(md, initialValue, part1));
                                            } else {
                                                if (initialValue.length() > 27) {
                                                    initialValue = initialValue.substring(0, 27);
                                                }
                                                authenticator.setCredID(initialValue);
                                            }
                                        }
                                        authenticators.add(authenticator);
                                    }
                                    return authenticators;
                                });
                    } else {
                        return RxJava2Adapter.monoToSingle(Mono.just(credentials
                                .stream()
                                .map(this::convert)
                                .collect(Collectors.toList())));
                    }
                }).apply(v))))
                .subscribe(
                        promise::complete,
                        promise::fail
                );

        return promise.future();
    }

    public Future<Void> store(Authenticator authenticator) {
        Promise<Void> promise = Promise.promise();

        RxJava2Adapter.monoToCompletable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(credentialService.findByCredentialId_migrated(ReferenceType.DOMAIN, domain.getId(), authenticator.getCredID()))).collectList().flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Credential>, CompletableSource>)credentials -> {
                    if (credentials.isEmpty()) {
                        // no credential found, create it
                        return RxJava2Adapter.monoToCompletable(create_migrated(authenticator));
                    } else {
                        // update current credentials
                        return Observable.fromIterable(credentials)
                                .flatMapCompletable(credential -> {
                                    credential.setCounter(authenticator.getCounter());
                                    credential.setUpdatedAt(new Date());
                                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialService.update_migrated(credential))).then());
                                });
                    }
                }).apply(y)))).then())
                .subscribe(
                        promise::complete,
                        error -> promise.fail(error.getMessage())
                );
        return promise.future();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.create_migrated(authenticator))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable create(Authenticator authenticator) {
 return RxJava2Adapter.monoToCompletable(create_migrated(authenticator));
}
private Mono<Void> create_migrated(Authenticator authenticator) {
        Credential credential = new Credential();
        credential.setReferenceType(ReferenceType.DOMAIN);
        credential.setReferenceId(domain.getId());
        credential.setUsername(authenticator.getUserName());
        credential.setCredentialId(authenticator.getCredID());
        credential.setPublicKey(authenticator.getPublicKey());
        credential.setCounter(authenticator.getCounter());
        credential.setAaguid(authenticator.getAaguid());
        credential.setAttestationStatementFormat(authenticator.getFmt());
        credential.setAttestationStatement(authenticator.getAttestationCertificates().toString());
        credential.setCreatedAt(new Date());
        credential.setUpdatedAt(credential.getCreatedAt());
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialService.create_migrated(credential))).then()));
    }

    private Authenticator convert(Credential credential) {
        if (credential == null) {
            return null;
        }
        Authenticator authenticator = new Authenticator();
        authenticator.setUserName(credential.getUsername());
        authenticator.setCredID(credential.getCredentialId());
        if (credential.getCounter() != null) {
            authenticator.setCounter(credential.getCounter());
        }
        authenticator.setPublicKey(credential.getPublicKey());

        return authenticator;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.generateCredID_migrated(username, claim))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<String> generateCredID(String username, String claim) {
 return RxJava2Adapter.monoToSingle(generateCredID_migrated(username, claim));
}
private Mono<String> generateCredID_migrated(String username, String claim) {
        return RxJava2Adapter.singleToMono(Single.create(emitter -> {
            String credID = jwtBuilder.sign(new JWT(Collections.singletonMap(claim, username))).split("\\.")[2];
            emitter.onSuccess(credID);
        }));
    }

    private static String createCredID(MessageDigest md, String input, String suffix) {
        String result = Base64.getUrlEncoder().encodeToString(md.digest(input.getBytes(StandardCharsets.UTF_8))).replace("=", "") + suffix;
        if (result.length() > 87) {
            result = result.substring(0, 87);
        }
        return result;
    }

    private static String shiftValue(String input, int delta) {
        String credID = "";
        char c;
        for (int j = 0; j < input.length(); j++) {
            c = input.charAt(j);
            char deltaC = (char) (c + delta);
            if (Character.isLetterOrDigit(deltaC)) {
                credID += deltaC;
            } else {
                credID += c;
            }
        }
        return credID;
    }
}

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
package io.gravitee.am.gateway.handler.common.oauth2.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.exception.jwt.JWTException;
import io.gravitee.am.common.exception.oauth2.InvalidTokenException;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.gateway.handler.common.oauth2.IntrospectionTokenService;

import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;


import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.time.Instant;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class IntrospectionTokenServiceImpl implements IntrospectionTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntrospectionTokenServiceImpl.class);
    private static final long OFFLINE_VERIFICATION_TIMER_SECONDS = 10;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ClientSyncService clientService;

    @Lazy
    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.introspect_migrated(token, offlineVerification))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<JWT> introspect(String token, boolean offlineVerification) {
 return RxJava2Adapter.monoToSingle(introspect_migrated(token, offlineVerification));
}
@Override
    public Mono<JWT> introspect_migrated(String token, boolean offlineVerification) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(jwtService.decode_migrated(token).flatMap(e->clientService.findByDomainAndClientId_migrated(e.getDomain(), e.getAud())).switchIfEmpty(Mono.error(new InvalidTokenException("Invalid or unknown client for this token"))))
                .flatMapSingle(client -> RxJava2Adapter.monoToSingle(jwtService.decodeAndVerify_migrated(token, client)))).flatMap(v->RxJava2Adapter.singleToMono((Single<JWT>)RxJavaReactorMigrationUtil.toJdkFunction((Function<JWT, Single<JWT>>)jwt -> {
                    // Just check the JWT signature and JWT validity if offline verification option is enabled
                    // or if the token has just been created (could not be in database so far because of async database storing process delay)
                    if (offlineVerification || Instant.now().isBefore(Instant.ofEpochSecond(jwt.getIat() + OFFLINE_VERIFICATION_TIMER_SECONDS))) {
                        return RxJava2Adapter.monoToSingle(Mono.just(jwt));
                    }

                    // check if token is not revoked
                    return RxJava2Adapter.monoToSingle(accessTokenRepository.findByToken_migrated(jwt.getJti()).switchIfEmpty(Mono.error(new InvalidTokenException("The token is invalid", "Token with JTI [" + jwt.getJti() + "] not found in the database", jwt))).map(RxJavaReactorMigrationUtil.toJdkFunction(accessToken -> {
                                if (accessToken.getExpireAt().before(new Date())) {
                                    throw new InvalidTokenException("The token expired", "Token with JTI [" + jwt.getJti() + "] is expired", jwt);
                                }
                                return jwt;
                            })));
                }).apply(v))))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<JWT>>toJdkFunction(ex -> {
                    if (ex instanceof JWTException) {
                        LOGGER.debug("An error occurs while decoding JWT access token : {}", token, ex);
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidTokenException(ex.getMessage(), ex)));
                    }
                    if (ex instanceof InvalidTokenException) {
                        InvalidTokenException invalidTokenException = (InvalidTokenException) ex;
                        String details = invalidTokenException.getDetails();
                        JWT jwt = invalidTokenException.getJwt();
                        LOGGER.debug("An error occurs while checking JWT access token validity: {}\n\t - details: {}\n\t - decoded jwt: {}",
                                token, details != null ? details : "none", jwt != null ? jwt.toString() : "{}", invalidTokenException);
                    }
                    return RxJava2Adapter.monoToSingle(Mono.error(ex));
                }).apply(err)));
    }
}

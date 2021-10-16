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
package io.gravitee.am.gateway.handler.oauth2.service.revocation.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.exception.oauth2.InvalidTokenException;
import io.gravitee.am.common.oauth2.TokenTypeHint;
import io.gravitee.am.gateway.handler.oauth2.exception.InvalidGrantException;
import io.gravitee.am.gateway.handler.oauth2.service.revocation.RevocationTokenRequest;
import io.gravitee.am.gateway.handler.oauth2.service.revocation.RevocationTokenService;
import io.gravitee.am.gateway.handler.oauth2.service.token.Token;
import io.gravitee.am.gateway.handler.oauth2.service.token.TokenService;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RevocationTokenServiceImpl implements RevocationTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RevocationTokenServiceImpl.class);

    @Autowired
    private TokenService tokenService;

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revoke_migrated(request, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable revoke(RevocationTokenRequest request, Client client) {
 return RxJava2Adapter.monoToCompletable(revoke_migrated(request, client));
}
@Override
    public Mono<Void> revoke_migrated(RevocationTokenRequest request, Client client) {
        String token = request.getToken();

        // Check the refresh_token store first. Fall back to the access token store if we don't
        // find anything. See RFC 7009, Sec 2.1: https://tools.ietf.org/html/rfc7009#section-2.1
        if (request.getHint() != null && request.getHint() == TokenTypeHint.REFRESH_TOKEN) {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(revokeRefreshToken_migrated(token, client))
                    .onErrorResumeNext(throwable -> {
                        // if the token was not issued to the client making the revocation request
                        // the request is refused and the client is informed of the error
                        if (throwable instanceof InvalidGrantException) {
                            return RxJava2Adapter.monoToCompletable(Mono.error(throwable));
                        }

                        // Note: invalid tokens do not cause an error response since the client
                        // cannot handle such an error in a reasonable way.  Moreover, the
                        // purpose of the revocation request, invalidating the particular token,
                        // is already achieved.
                        // Log the result anyway for posterity.
                        if (throwable instanceof InvalidTokenException) {
                            logger.debug("No refresh token {} found in the token store.", token);
                        }

                        // fallback to access token
                        return RxJava2Adapter.monoToCompletable(revokeAccessToken_migrated(token, client));
                    })
                    .onErrorResumeNext(throwable -> {
                        // Note: invalid tokens do not cause an error response since the client
                        // cannot handle such an error in a reasonable way.  Moreover, the
                        // purpose of the revocation request, invalidating the particular token,
                        // is already achieved.
                        // Log the result anyway for posterity.
                        if (throwable instanceof InvalidTokenException) {
                            logger.debug("No access token {} found in the token store.", token);
                            return RxJava2Adapter.monoToCompletable(Mono.empty());
                        }
                        return RxJava2Adapter.monoToCompletable(Mono.error(throwable));
                    }));
        }

        // The user didn't hint that this is a refresh token, so it MAY be an access
        // token. If we don't find an access token... check if it's a refresh token.
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(revokeAccessToken_migrated(token, client))
                .onErrorResumeNext(throwable -> {
                    // if the token was not issued to the client making the revocation request
                    // the request is refused and the client is informed of the error
                    if (throwable instanceof InvalidGrantException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(throwable));
                    }

                    // Note: invalid tokens do not cause an error response since the client
                    // cannot handle such an error in a reasonable way.  Moreover, the
                    // purpose of the revocation request, invalidating the particular token,
                    // is already achieved.
                    // Log the result anyway for posterity.
                    if (throwable instanceof InvalidTokenException) {
                        logger.debug("No access token {} found in the token store.", token);
                    }

                    // fallback to refresh token
                    return RxJava2Adapter.monoToCompletable(revokeRefreshToken_migrated(token, client));
                })
                .onErrorResumeNext(throwable -> {
                    // Note: invalid tokens do not cause an error response since the client
                    // cannot handle such an error in a reasonable way.  Moreover, the
                    // purpose of the revocation request, invalidating the particular token,
                    // is already achieved.
                    // Log the result anyway for posterity.
                    if (throwable instanceof InvalidTokenException) {
                        logger.debug("No refresh token {} found in the token store.", token);
                        return RxJava2Adapter.monoToCompletable(Mono.empty());
                    }
                    return RxJava2Adapter.monoToCompletable(Mono.error(throwable));
                }));

    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeAccessToken_migrated(token, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable revokeAccessToken(String token, Client client) {
 return RxJava2Adapter.monoToCompletable(revokeAccessToken_migrated(token, client));
}
private Mono<Void> revokeAccessToken_migrated(String token, Client client) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(tokenService.getAccessToken_migrated(token, client))).switchIfEmpty(Mono.error(new InvalidTokenException("Unknown access token"))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Token, CompletableSource>)accessToken -> {
                    String tokenClientId = accessToken.getClientId();
                    if (!client.getClientId().equals(tokenClientId)) {
                        logger.debug("Revoke FAILED: requesting client = {}, token's client = {}.", client.getClientId(), tokenClientId);
                        return RxJava2Adapter.monoToCompletable(Mono.error(new InvalidGrantException("Cannot revoke tokens issued to other clients.")));
                    }

                    return RxJava2Adapter.monoToCompletable(tokenService.deleteAccessToken_migrated(accessToken.getValue()));
                }).apply(y)))).then()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeRefreshToken_migrated(token, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable revokeRefreshToken(String token, Client client) {
 return RxJava2Adapter.monoToCompletable(revokeRefreshToken_migrated(token, client));
}
private Mono<Void> revokeRefreshToken_migrated(String token, Client client) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(tokenService.getRefreshToken_migrated(token, client))).switchIfEmpty(Mono.error(new InvalidTokenException("Unknown refresh token"))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Token, CompletableSource>)refreshToken -> {
                    String tokenClientId = refreshToken.getClientId();
                    if (!client.getClientId().equals(tokenClientId)) {
                        logger.debug("Revoke FAILED: requesting client = {}, token's client = {}.", client.getClientId(), tokenClientId);
                        return RxJava2Adapter.monoToCompletable(Mono.error(new InvalidGrantException("Cannot revoke tokens issued to other clients.")));
                    }

                    return RxJava2Adapter.monoToCompletable(tokenService.deleteRefreshToken_migrated(refreshToken.getValue()));
                }).apply(y)))).then()));
    }
}

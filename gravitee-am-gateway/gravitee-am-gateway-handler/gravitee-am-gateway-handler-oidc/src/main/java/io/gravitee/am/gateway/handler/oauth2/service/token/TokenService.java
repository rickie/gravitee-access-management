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
package io.gravitee.am.gateway.handler.oauth2.service.token;

import io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request;
import io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequest;
import io.gravitee.am.model.AuthenticationFlowContext;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface TokenService {

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.gateway.handler.oauth2.service.token.Token> getAccessToken(java.lang.String accessToken, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToMaybe(getAccessToken_migrated(accessToken, client));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.oauth2.service.token.Token> getAccessToken_migrated(String accessToken, Client client) {
    return RxJava2Adapter.maybeToMono(getAccessToken(accessToken, client));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.gateway.handler.oauth2.service.token.Token> getRefreshToken(java.lang.String refreshToken, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToMaybe(getRefreshToken_migrated(refreshToken, client));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.oauth2.service.token.Token> getRefreshToken_migrated(String refreshToken, Client client) {
    return RxJava2Adapter.maybeToMono(getRefreshToken(refreshToken, client));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.oauth2.service.token.Token> introspect(java.lang.String token) {
    return RxJava2Adapter.monoToSingle(introspect_migrated(token));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.oauth2.service.token.Token> introspect_migrated(String token) {
    return RxJava2Adapter.singleToMono(introspect(token));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.oauth2.service.token.Token> create(io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request oAuth2Request, io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User endUser) {
    return RxJava2Adapter.monoToSingle(create_migrated(oAuth2Request, client, endUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.oauth2.service.token.Token> create_migrated(OAuth2Request oAuth2Request, Client client, User endUser) {
    return RxJava2Adapter.singleToMono(create(oAuth2Request, client, endUser));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.oauth2.service.token.Token> refresh(java.lang.String refreshToken, io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequest tokenRequest, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToSingle(refresh_migrated(refreshToken, tokenRequest, client));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.oauth2.service.token.Token> refresh_migrated(String refreshToken, TokenRequest tokenRequest, Client client) {
    return RxJava2Adapter.singleToMono(refresh(refreshToken, tokenRequest, client));
}

      @Deprecated  
default io.reactivex.Completable deleteAccessToken(java.lang.String accessToken) {
    return RxJava2Adapter.monoToCompletable(deleteAccessToken_migrated(accessToken));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteAccessToken_migrated(String accessToken) {
    return RxJava2Adapter.completableToMono(deleteAccessToken(accessToken));
}

      @Deprecated  
default io.reactivex.Completable deleteRefreshToken(java.lang.String refreshToken) {
    return RxJava2Adapter.monoToCompletable(deleteRefreshToken_migrated(refreshToken));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteRefreshToken_migrated(String refreshToken) {
    return RxJava2Adapter.completableToMono(deleteRefreshToken(refreshToken));
}
}

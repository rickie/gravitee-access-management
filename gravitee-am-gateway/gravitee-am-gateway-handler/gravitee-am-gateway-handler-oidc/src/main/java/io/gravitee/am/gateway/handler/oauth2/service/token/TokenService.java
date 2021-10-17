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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request;
import io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequest;
import io.gravitee.am.gateway.handler.oauth2.service.token.Token;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface TokenService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getAccessToken_migrated(accessToken, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Token> getAccessToken(String accessToken, Client client) {
    return RxJava2Adapter.monoToMaybe(getAccessToken_migrated(accessToken, client));
}
default Mono<Token> getAccessToken_migrated(String accessToken, Client client) {
    return RxJava2Adapter.maybeToMono(getAccessToken(accessToken, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getRefreshToken_migrated(refreshToken, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Token> getRefreshToken(String refreshToken, Client client) {
    return RxJava2Adapter.monoToMaybe(getRefreshToken_migrated(refreshToken, client));
}
default Mono<Token> getRefreshToken_migrated(String refreshToken, Client client) {
    return RxJava2Adapter.maybeToMono(getRefreshToken(refreshToken, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.introspect_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Token> introspect(String token) {
    return RxJava2Adapter.monoToSingle(introspect_migrated(token));
}
default Mono<Token> introspect_migrated(String token) {
    return RxJava2Adapter.singleToMono(introspect(token));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(oAuth2Request, client, endUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Token> create(OAuth2Request oAuth2Request, Client client, User endUser) {
    return RxJava2Adapter.monoToSingle(create_migrated(oAuth2Request, client, endUser));
}
default Mono<Token> create_migrated(OAuth2Request oAuth2Request, Client client, User endUser) {
    return RxJava2Adapter.singleToMono(create(oAuth2Request, client, endUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.refresh_migrated(refreshToken, tokenRequest, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Token> refresh(String refreshToken, TokenRequest tokenRequest, Client client) {
    return RxJava2Adapter.monoToSingle(refresh_migrated(refreshToken, tokenRequest, client));
}
default Mono<Token> refresh_migrated(String refreshToken, TokenRequest tokenRequest, Client client) {
    return RxJava2Adapter.singleToMono(refresh(refreshToken, tokenRequest, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteAccessToken_migrated(accessToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable deleteAccessToken(String accessToken) {
    return RxJava2Adapter.monoToCompletable(deleteAccessToken_migrated(accessToken));
}
default Mono<Void> deleteAccessToken_migrated(String accessToken) {
    return RxJava2Adapter.completableToMono(deleteAccessToken(accessToken));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteRefreshToken_migrated(refreshToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable deleteRefreshToken(String refreshToken) {
    return RxJava2Adapter.monoToCompletable(deleteRefreshToken_migrated(refreshToken));
}
default Mono<Void> deleteRefreshToken_migrated(String refreshToken) {
    return RxJava2Adapter.completableToMono(deleteRefreshToken(refreshToken));
}
}

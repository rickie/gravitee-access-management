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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.gateway.api.ExecutionContext;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface IDTokenService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(oAuth2Request, client, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.String> create(io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request oAuth2Request, io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(create_migrated(oAuth2Request, client, user));
}default Mono<String>  create_migrated(OAuth2Request oAuth2Request, Client client, User user) {
        return RxJava2Adapter.singleToMono(create(oAuth2Request, client, user, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(oAuth2Request, client, user, executionContext))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.String> create(io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request oAuth2Request, io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user, io.gravitee.gateway.api.ExecutionContext executionContext) {
    return RxJava2Adapter.monoToSingle(create_migrated(oAuth2Request, client, user, executionContext));
}
default reactor.core.publisher.Mono<java.lang.String> create_migrated(OAuth2Request oAuth2Request, Client client, User user, ExecutionContext executionContext) {
    return RxJava2Adapter.singleToMono(create(oAuth2Request, client, user, executionContext));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.extractUser_migrated(idToken, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> extractUser(java.lang.String idToken, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToSingle(extractUser_migrated(idToken, client));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> extractUser_migrated(String idToken, Client client) {
    return RxJava2Adapter.singleToMono(extractUser(idToken, client));
}
}

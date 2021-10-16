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
package io.gravitee.am.gateway.handler.oauth2.service.request;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class TokenRequestResolver extends AbstractRequestResolver<TokenRequest> {

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.resolve_migrated(tokenRequest, client, endUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Single<TokenRequest> resolve(TokenRequest tokenRequest, Client client, User endUser) {
 return RxJava2Adapter.monoToSingle(resolve_migrated(tokenRequest, client, endUser));
}
public Mono<TokenRequest> resolve_migrated(TokenRequest tokenRequest, Client client, User endUser) {
        return RxJava2Adapter.singleToMono(resolveAuthorizedScopes(tokenRequest, client, endUser));
    }
}

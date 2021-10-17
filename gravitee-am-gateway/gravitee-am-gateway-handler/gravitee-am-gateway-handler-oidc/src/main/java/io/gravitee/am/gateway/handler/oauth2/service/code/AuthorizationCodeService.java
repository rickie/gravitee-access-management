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
package io.gravitee.am.gateway.handler.oauth2.service.code;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.oauth2.service.request.AuthorizationRequest;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.oauth2.model.AuthorizationCode;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AuthorizationCodeService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(authorizationRequest, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AuthorizationCode> create(AuthorizationRequest authorizationRequest, User user) {
    return RxJava2Adapter.monoToSingle(create_migrated(authorizationRequest, user));
}
default Mono<AuthorizationCode> create_migrated(AuthorizationRequest authorizationRequest, User user) {
    return RxJava2Adapter.singleToMono(create(authorizationRequest, user));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.remove_migrated(code, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<AuthorizationCode> remove(String code, Client client) {
    return RxJava2Adapter.monoToMaybe(remove_migrated(code, client));
}
default Mono<AuthorizationCode> remove_migrated(String code, Client client) {
    return RxJava2Adapter.maybeToMono(remove(code, client));
}
}

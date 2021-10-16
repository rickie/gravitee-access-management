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
package io.gravitee.am.gateway.handler.oauth2.service.revocation;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.oidc.Client;

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface RevocationTokenService {

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revoke_migrated(request, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable revoke(io.gravitee.am.gateway.handler.oauth2.service.revocation.RevocationTokenRequest request, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToCompletable(revoke_migrated(request, client));
}
default reactor.core.publisher.Mono<java.lang.Void> revoke_migrated(RevocationTokenRequest request, Client client) {
    return RxJava2Adapter.completableToMono(revoke(request, client));
}
}

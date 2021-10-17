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
package io.gravitee.am.gateway.handler.common.auth.idp;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.AuthenticationProvider;
import io.gravitee.am.identityprovider.api.UserProvider;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.common.service.Service;
import io.reactivex.Maybe;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface IdentityProviderManager extends Service {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.get_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<AuthenticationProvider> get(String id) {
    return RxJava2Adapter.monoToMaybe(get_migrated(id));
}
default Mono<AuthenticationProvider> get_migrated(String id) {
    return RxJava2Adapter.maybeToMono(get(id));
}

    IdentityProvider getIdentityProvider(String id);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getUserProvider_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<UserProvider> getUserProvider(String id) {
    return RxJava2Adapter.monoToMaybe(getUserProvider_migrated(id));
}
default Mono<UserProvider> getUserProvider_migrated(String id) {
    return RxJava2Adapter.maybeToMono(getUserProvider(id));
}
}

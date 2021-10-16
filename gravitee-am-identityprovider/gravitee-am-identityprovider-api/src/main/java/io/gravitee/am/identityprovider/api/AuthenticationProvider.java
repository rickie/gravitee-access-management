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
package io.gravitee.am.identityprovider.api;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.common.component.Lifecycle;
import io.gravitee.common.service.Service;

import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AuthenticationProvider extends Service<AuthenticationProvider> {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadUserByUsername_migrated(authentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.identityprovider.api.User> loadUserByUsername(io.gravitee.am.identityprovider.api.Authentication authentication) {
    return RxJava2Adapter.monoToMaybe(loadUserByUsername_migrated(authentication));
}
default reactor.core.publisher.Mono<io.gravitee.am.identityprovider.api.User> loadUserByUsername_migrated(Authentication authentication) {
    return RxJava2Adapter.maybeToMono(loadUserByUsername(authentication));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadUserByUsername_migrated(username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.identityprovider.api.User> loadUserByUsername(java.lang.String username) {
    return RxJava2Adapter.monoToMaybe(loadUserByUsername_migrated(username));
}
default reactor.core.publisher.Mono<io.gravitee.am.identityprovider.api.User> loadUserByUsername_migrated(String username) {
    return RxJava2Adapter.maybeToMono(loadUserByUsername(username));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadPreAuthenticatedUser_migrated(authentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.identityprovider.api.User> loadPreAuthenticatedUser(io.gravitee.am.identityprovider.api.Authentication authentication) {
    return RxJava2Adapter.monoToMaybe(loadPreAuthenticatedUser_migrated(authentication));
}default Mono<User> loadPreAuthenticatedUser_migrated(Authentication authentication) {
        io.gravitee.am.model.User user = (io.gravitee.am.model.User) authentication.getPrincipal();
        return loadUserByUsername_migrated(user.getUsername());
    }

    default Metadata metadata(String idpUrl) {
        return null;
    }

    default Lifecycle.State lifecycleState() {
        return Lifecycle.State.INITIALIZED;
    }

    default AuthenticationProvider start() throws Exception {
        return this;
    }

    default AuthenticationProvider stop() throws Exception {
        return this;
    }
}

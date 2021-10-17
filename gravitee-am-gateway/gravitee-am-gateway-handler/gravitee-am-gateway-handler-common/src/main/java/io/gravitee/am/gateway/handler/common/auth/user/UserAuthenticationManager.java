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
package io.gravitee.am.gateway.handler.common.auth.user;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.Authentication;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.gateway.api.Request;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserAuthenticationManager {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.authenticate_migrated(client, authentication, preAuthenticated))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> authenticate(Client client, Authentication authentication, boolean preAuthenticated) {
    return RxJava2Adapter.monoToSingle(authenticate_migrated(client, authentication, preAuthenticated));
}
default Mono<io.gravitee.am.model.User> authenticate_migrated(Client client, Authentication authentication, boolean preAuthenticated) {
    return RxJava2Adapter.singleToMono(authenticate(client, authentication, preAuthenticated));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadUserByUsername_migrated(client, username, request))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<io.gravitee.am.model.User> loadUserByUsername(Client client, String username, Request request) {
    return RxJava2Adapter.monoToMaybe(loadUserByUsername_migrated(client, username, request));
}
default Mono<io.gravitee.am.model.User> loadUserByUsername_migrated(Client client, String username, Request request) {
    return RxJava2Adapter.maybeToMono(loadUserByUsername(client, username, request));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadPreAuthenticatedUser_migrated(subject, request))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<io.gravitee.am.model.User> loadPreAuthenticatedUser(String subject, Request request) {
    return RxJava2Adapter.monoToMaybe(loadPreAuthenticatedUser_migrated(subject, request));
}
default Mono<io.gravitee.am.model.User> loadPreAuthenticatedUser_migrated(String subject, Request request) {
    return RxJava2Adapter.maybeToMono(loadPreAuthenticatedUser(subject, request));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.connect_migrated(user, afterAuthentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> connect(io.gravitee.am.identityprovider.api.User user, boolean afterAuthentication) {
    return RxJava2Adapter.monoToSingle(connect_migrated(user, afterAuthentication));
}
default Mono<io.gravitee.am.model.User> connect_migrated(io.gravitee.am.identityprovider.api.User user, boolean afterAuthentication) {
    return RxJava2Adapter.singleToMono(connect(user, afterAuthentication));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.authenticate_migrated(client, authentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> authenticate(Client client, Authentication authentication) {
    return RxJava2Adapter.monoToSingle(authenticate_migrated(client, authentication));
}default Mono<User> authenticate_migrated(Client client, Authentication authentication) {
        return RxJava2Adapter.singleToMono(authenticate(client, authentication, false));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadPreAuthenticatedUser_migrated(subject))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<io.gravitee.am.model.User> loadPreAuthenticatedUser(String subject) {
    return RxJava2Adapter.monoToMaybe(loadPreAuthenticatedUser_migrated(subject));
}default Mono<User> loadPreAuthenticatedUser_migrated(String subject) {
        return RxJava2Adapter.maybeToMono(loadPreAuthenticatedUser(subject, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.connect_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> connect(io.gravitee.am.identityprovider.api.User user) {
    return RxJava2Adapter.monoToSingle(connect_migrated(user));
}default Mono<User> connect_migrated(io.gravitee.am.identityprovider.api.User user) {
        return RxJava2Adapter.singleToMono(connect(user, true));
    }
}

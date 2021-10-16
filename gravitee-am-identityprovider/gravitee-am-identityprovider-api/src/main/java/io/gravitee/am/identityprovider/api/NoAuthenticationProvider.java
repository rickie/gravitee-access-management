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
import io.reactivex.Maybe;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class NoAuthenticationProvider implements AuthenticationProvider {

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadUserByUsername_migrated(authentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> loadUserByUsername(Authentication authentication) {
 return RxJava2Adapter.monoToMaybe(loadUserByUsername_migrated(authentication));
}
@Override
    public Mono<User> loadUserByUsername_migrated(Authentication authentication) {
        return RxJava2Adapter.maybeToMono(null);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadUserByUsername_migrated(username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> loadUserByUsername(String username) {
 return RxJava2Adapter.monoToMaybe(loadUserByUsername_migrated(username));
}
@Override
    public Mono<User> loadUserByUsername_migrated(String username) {
        return RxJava2Adapter.maybeToMono(null);
    }
}

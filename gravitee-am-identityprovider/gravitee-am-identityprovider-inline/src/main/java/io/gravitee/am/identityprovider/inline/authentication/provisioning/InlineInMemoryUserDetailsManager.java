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
package io.gravitee.am.identityprovider.inline.authentication.provisioning;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.inline.model.User;
import io.reactivex.Maybe;
import java.util.HashMap;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class InlineInMemoryUserDetailsManager {

    private final Map<String, User> users = new HashMap<>();

    public void createUser(User user) {
        users.putIfAbsent(user.getUsername().toLowerCase(), user);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadUserByUsername_migrated(username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Maybe<User> loadUserByUsername(String username) {
 return RxJava2Adapter.monoToMaybe(loadUserByUsername_migrated(username));
}
public Mono<User> loadUserByUsername_migrated(String username) {
        User user = users.get(username.toLowerCase());
        return RxJava2Adapter.maybeToMono((user != null) ? RxJava2Adapter.monoToMaybe(Mono.just(user)) : RxJava2Adapter.monoToMaybe(Mono.empty()));
    }
}

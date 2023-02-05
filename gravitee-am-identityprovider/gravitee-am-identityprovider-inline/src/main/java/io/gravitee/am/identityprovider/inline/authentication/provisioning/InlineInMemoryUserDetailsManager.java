/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.identityprovider.inline.authentication.provisioning;

import io.gravitee.am.identityprovider.inline.model.User;
import io.reactivex.Maybe;

import java.util.HashMap;
import java.util.Map;

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

    public Maybe<User> loadUserByUsername(String username) {
        User user = users.get(username.toLowerCase());
        return (user != null) ? Maybe.just(user) : Maybe.empty();
    }
}

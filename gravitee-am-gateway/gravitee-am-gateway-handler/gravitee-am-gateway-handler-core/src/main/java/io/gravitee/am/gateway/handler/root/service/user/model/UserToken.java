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
package io.gravitee.am.gateway.handler.root.service.user.model;

import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserToken {

    private User user;
    private Client client;
    private JWT token;

    public UserToken() {}

    public UserToken(User user, Client client) {
        this.user = user;
        this.client = client;
    }

    public UserToken(User user, Client client, JWT token) {
        this(user, client);
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public Client getClient() {
        return client;
    }

    public JWT getToken() {
        return token;
    }
}

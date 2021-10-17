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


import io.gravitee.am.identityprovider.api.Authentication;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.gateway.api.Request;



import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserAuthenticationManager {

      
Mono<io.gravitee.am.model.User> authenticate_migrated(Client client, Authentication authentication, boolean preAuthenticated);

      
Mono<io.gravitee.am.model.User> loadUserByUsername_migrated(Client client, String username, Request request);

      
Mono<io.gravitee.am.model.User> loadPreAuthenticatedUser_migrated(String subject, Request request);

      
Mono<io.gravitee.am.model.User> connect_migrated(io.gravitee.am.identityprovider.api.User user, boolean afterAuthentication);

      Mono<User> authenticate_migrated(Client client, Authentication authentication);

      Mono<User> loadPreAuthenticatedUser_migrated(String subject);

      Mono<User> connect_migrated(io.gravitee.am.identityprovider.api.User user);
}

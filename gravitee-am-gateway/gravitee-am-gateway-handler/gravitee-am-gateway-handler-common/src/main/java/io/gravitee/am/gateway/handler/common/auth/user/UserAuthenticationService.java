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
import io.gravitee.am.model.User;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.gravitee.gateway.api.Request;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserAuthenticationService {

    /**
     * Method called when a user has been authenticated by any means (login, extension-grant, token ...)
     *
     * @param principal Authenticated user
     * @param afterAuthentication if authentication has been done by login action
     * @return user fetch or create from the repository
     */
      
Mono<io.gravitee.am.model.User> connect_migrated(io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication);

    /**
     * Use to find a pre-authenticated user (from a previous authentication step)
     *
     * The user should be present in gravitee repository and should be retrieved from the user last identity provider
     * @param subject user technical id
     * @param request http request
     * @return Pre-authenticated user
     */
      
Mono<io.gravitee.am.model.User> loadPreAuthenticatedUser_migrated(String subject, Request request);

    /**
     * Use to find a pre-authenticated user (from a previous authentication step)
     *
     * The user should be present in gravitee repository and should be retrieved from the user last identity provider
     * @param principal user end-user
     * @return Pre-authenticated user
     */
      
Mono<io.gravitee.am.model.User> loadPreAuthenticatedUser_migrated(io.gravitee.am.identityprovider.api.User principal);

    /**
     * Lock user account if login max attempts has been reached
     *
     * @param criteria login attempt criteria
     * @param accountSettings account settings
     * @param client oauth2 client
     * @param user End-User to lock
     * @return
     */
      
Mono<Void> lockAccount_migrated(LoginAttemptCriteria criteria, AccountSettings accountSettings, Client client, User user);

      Mono<User> connect_migrated(io.gravitee.am.identityprovider.api.User principal);
}

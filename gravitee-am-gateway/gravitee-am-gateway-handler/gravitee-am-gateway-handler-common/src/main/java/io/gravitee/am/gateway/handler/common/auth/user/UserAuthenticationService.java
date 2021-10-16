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
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.connect_migrated(principal, afterAuthentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> connect(io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication) {
    return RxJava2Adapter.monoToSingle(connect_migrated(principal, afterAuthentication));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> connect_migrated(io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication) {
    return RxJava2Adapter.singleToMono(connect(principal, afterAuthentication));
}

    /**
     * Use to find a pre-authenticated user (from a previous authentication step)
     *
     * The user should be present in gravitee repository and should be retrieved from the user last identity provider
     * @param subject user technical id
     * @param request http request
     * @return Pre-authenticated user
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadPreAuthenticatedUser_migrated(subject, request))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> loadPreAuthenticatedUser(java.lang.String subject, io.gravitee.gateway.api.Request request) {
    return RxJava2Adapter.monoToMaybe(loadPreAuthenticatedUser_migrated(subject, request));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> loadPreAuthenticatedUser_migrated(String subject, Request request) {
    return RxJava2Adapter.maybeToMono(loadPreAuthenticatedUser(subject, request));
}

    /**
     * Use to find a pre-authenticated user (from a previous authentication step)
     *
     * The user should be present in gravitee repository and should be retrieved from the user last identity provider
     * @param principal user end-user
     * @return Pre-authenticated user
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadPreAuthenticatedUser_migrated(principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> loadPreAuthenticatedUser(io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToMaybe(loadPreAuthenticatedUser_migrated(principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> loadPreAuthenticatedUser_migrated(io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.maybeToMono(loadPreAuthenticatedUser(principal));
}

    /**
     * Lock user account if login max attempts has been reached
     *
     * @param criteria login attempt criteria
     * @param accountSettings account settings
     * @param client oauth2 client
     * @param user End-User to lock
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.lockAccount_migrated(criteria, accountSettings, client, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable lockAccount(io.gravitee.am.repository.management.api.search.LoginAttemptCriteria criteria, io.gravitee.am.model.account.AccountSettings accountSettings, io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToCompletable(lockAccount_migrated(criteria, accountSettings, client, user));
}
default reactor.core.publisher.Mono<java.lang.Void> lockAccount_migrated(LoginAttemptCriteria criteria, AccountSettings accountSettings, Client client, User user) {
    return RxJava2Adapter.completableToMono(lockAccount(criteria, accountSettings, client, user));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.connect_migrated(principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> connect(io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(connect_migrated(principal));
}default Mono<User> connect_migrated(io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.singleToMono(connect(principal, true));
    }
}

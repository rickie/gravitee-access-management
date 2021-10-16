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
package io.gravitee.am.gateway.handler.common.user;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.User;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserService {

    /**
     * Find a user by its technical id
     * @param id user technical id
     * @return end user
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

    /**
     * Find a user by its domain, its external id and its identity provider
     * @param domain user security domain
     * @param externalId user external id
     * @param source user identity provider
     * @return end user
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndExternalIdAndSource_migrated(domain, externalId, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findByDomainAndExternalIdAndSource(java.lang.String domain, java.lang.String externalId, java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndExternalIdAndSource_migrated(domain, externalId, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findByDomainAndExternalIdAndSource_migrated(String domain, String externalId, String source) {
    return RxJava2Adapter.maybeToMono(findByDomainAndExternalIdAndSource(domain, externalId, source));
}

    /**
     * Find a user by its domain, its username and its identity provider
     * @param domain user security domain
     * @param username user username
     * @param source user identity provider
     * @return end user
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndUsernameAndSource_migrated(domain, username, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findByDomainAndUsernameAndSource(java.lang.String domain, java.lang.String username, java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndUsernameAndSource_migrated(domain, username, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findByDomainAndUsernameAndSource_migrated(String domain, String username, String source) {
    return RxJava2Adapter.maybeToMono(findByDomainAndUsernameAndSource(domain, username, source));
}

    /**
     * Find users by security domain and email
     * @param domain user security domain
     * @param criteria search criteria
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomainAndCriteria_migrated(domain, criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.User>> findByDomainAndCriteria(java.lang.String domain, io.gravitee.am.repository.management.api.search.FilterCriteria criteria) {
    return RxJava2Adapter.monoToSingle(findByDomainAndCriteria_migrated(domain, criteria));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.User>> findByDomainAndCriteria_migrated(String domain, FilterCriteria criteria) {
    return RxJava2Adapter.singleToMono(findByDomainAndCriteria(domain, criteria));
}

    /**
     * Create a new user
     * @param user user to create
     * @return created user
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> create(io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(create_migrated(user));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> create_migrated(User user) {
    return RxJava2Adapter.singleToMono(create(user));
}

    /**
     * Update an existing user
     * @param user user to update
     * @return updated user
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> update(io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(update_migrated(user));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> update_migrated(User user) {
    return RxJava2Adapter.singleToMono(update(user));
}

    /**
     * Fetch additional data such as groups/roles to enhance user profile information
     * @param user end user
     * @return Enhanced user
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enhance_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> enhance(io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(enhance_migrated(user));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> enhance_migrated(User user) {
    return RxJava2Adapter.singleToMono(enhance(user));
}

    /**
     * Add an MFA factor to an end-user
     * @param userId the end-user id
     * @param enrolledFactor the factor to enroll
     * @param principal the user who has performed this action
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.addFactor_migrated(userId, enrolledFactor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> addFactor(java.lang.String userId, io.gravitee.am.model.factor.EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(addFactor_migrated(userId, enrolledFactor, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> addFactor_migrated(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(addFactor(userId, enrolledFactor, principal));
}
}

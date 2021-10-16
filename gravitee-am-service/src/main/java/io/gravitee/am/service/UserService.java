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
package io.gravitee.am.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.analytics.AnalyticsQuery;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserService extends CommonUserService {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.User> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.User> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndUsername_migrated(domain, username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findByDomainAndUsername(java.lang.String domain, java.lang.String username) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndUsername_migrated(domain, username));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findByDomainAndUsername_migrated(String domain, String username) {
    return RxJava2Adapter.maybeToMono(findByDomainAndUsername(domain, username));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndUsernameAndSource_migrated(domain, username, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findByDomainAndUsernameAndSource(java.lang.String domain, java.lang.String username, java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndUsernameAndSource_migrated(domain, username, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findByDomainAndUsernameAndSource_migrated(String domain, String username, String source) {
    return RxJava2Adapter.maybeToMono(findByDomainAndUsernameAndSource(domain, username, source));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> create(java.lang.String domain, io.gravitee.am.service.model.NewUser newUser) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> create_migrated(String domain, NewUser newUser) {
    return RxJava2Adapter.singleToMono(create(domain, newUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateUser updateUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> update_migrated(String domain, String id, UpdateUser updateUser) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countByDomain(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(countByDomain_migrated(domain));
}
default reactor.core.publisher.Mono<java.lang.Long> countByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(countByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByApplication_migrated(domain, application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countByApplication(java.lang.String domain, java.lang.String application) {
    return RxJava2Adapter.monoToSingle(countByApplication_migrated(domain, application));
}
default reactor.core.publisher.Mono<java.lang.Long> countByApplication_migrated(String domain, String application) {
    return RxJava2Adapter.singleToMono(countByApplication(domain, application));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.statistics_migrated(query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.Map<java.lang.Object, java.lang.Object>> statistics(io.gravitee.am.model.analytics.AnalyticsQuery query) {
    return RxJava2Adapter.monoToSingle(statistics_migrated(query));
}
default reactor.core.publisher.Mono<java.util.Map<java.lang.Object, java.lang.Object>> statistics_migrated(AnalyticsQuery query) {
    return RxJava2Adapter.singleToMono(statistics(query));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.upsertFactor_migrated(userId, enrolledFactor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> upsertFactor(java.lang.String userId, io.gravitee.am.model.factor.EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(upsertFactor_migrated(userId, enrolledFactor, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> upsertFactor_migrated(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(upsertFactor(userId, enrolledFactor, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.removeFactor_migrated(userId, factorId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable removeFactor(java.lang.String userId, java.lang.String factorId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(removeFactor_migrated(userId, factorId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> removeFactor_migrated(String userId, String factorId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(removeFactor(userId, factorId, principal));
}
}

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
package io.gravitee.am.repository.management.api;

import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.analytics.AnalyticsQuery;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.common.CrudRepository;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserRepository extends CommonUserRepository {

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.User> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.User> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, int page, int size) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(referenceType, referenceId, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size) {
    return RxJava2Adapter.singleToMono(findAll(referenceType, referenceId, page, size));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> search(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String query, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, query, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> search_migrated(ReferenceType referenceType, String referenceId, String query, int page, int size) {
    return RxJava2Adapter.singleToMono(search(referenceType, referenceId, query, page, size));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> search(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.repository.management.api.search.FilterCriteria criteria, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, criteria, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> search_migrated(ReferenceType referenceType, String referenceId, FilterCriteria criteria, int page, int size) {
    return RxJava2Adapter.singleToMono(search(referenceType, referenceId, criteria, page, size));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.User> findByDomainAndEmail(java.lang.String domain, java.lang.String email, boolean strict) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndEmail_migrated(domain, email, strict));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.User> findByDomainAndEmail_migrated(String domain, String email, boolean strict) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndEmail(domain, email, strict));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findByUsernameAndDomain(java.lang.String domain, java.lang.String username) {
    return RxJava2Adapter.monoToMaybe(findByUsernameAndDomain_migrated(domain, username));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findByUsernameAndDomain_migrated(String domain, String username) {
    return RxJava2Adapter.maybeToMono(findByUsernameAndDomain(domain, username));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findByUsernameAndSource(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String username, java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByUsernameAndSource_migrated(referenceType, referenceId, username, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findByUsernameAndSource_migrated(ReferenceType referenceType, String referenceId, String username, String source) {
    return RxJava2Adapter.maybeToMono(findByUsernameAndSource(referenceType, referenceId, username, source));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findByExternalIdAndSource(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String externalId, java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByExternalIdAndSource_migrated(referenceType, referenceId, externalId, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findByExternalIdAndSource_migrated(ReferenceType referenceType, String referenceId, String externalId, String source) {
    return RxJava2Adapter.maybeToMono(findByExternalIdAndSource(referenceType, referenceId, externalId, source));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.User> findByIdIn(java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.User> findByIdIn_migrated(List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String userId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, userId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findById_migrated(ReferenceType referenceType, String referenceId, String userId) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, userId));
}

      @Deprecated  
default io.reactivex.Single<java.lang.Long> countByReference(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.monoToSingle(countByReference_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Mono<java.lang.Long> countByReference_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.singleToMono(countByReference(referenceType, referenceId));
}

      @Deprecated  
default io.reactivex.Single<java.lang.Long> countByApplication(java.lang.String domain, java.lang.String application) {
    return RxJava2Adapter.monoToSingle(countByApplication_migrated(domain, application));
}
default reactor.core.publisher.Mono<java.lang.Long> countByApplication_migrated(String domain, String application) {
    return RxJava2Adapter.singleToMono(countByApplication(domain, application));
}

      @Deprecated  
default io.reactivex.Single<java.util.Map<java.lang.Object, java.lang.Object>> statistics(io.gravitee.am.model.analytics.AnalyticsQuery query) {
    return RxJava2Adapter.monoToSingle(statistics_migrated(query));
}
default reactor.core.publisher.Mono<java.util.Map<java.lang.Object, java.lang.Object>> statistics_migrated(AnalyticsQuery query) {
    return RxJava2Adapter.singleToMono(statistics(query));
}
}

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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.analytics.AnalyticsQuery;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserRepository extends CommonUserRepository {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<User> findAll(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default Flux<User> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(referenceType, referenceId, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<User>> findAll(ReferenceType referenceType, String referenceId, int page, int size) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(referenceType, referenceId, page, size));
}
default Mono<Page<User>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size) {
    return RxJava2Adapter.singleToMono(findAll(referenceType, referenceId, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<User>> search(ReferenceType referenceType, String referenceId, String query, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, query, page, size));
}
default Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, String query, int page, int size) {
    return RxJava2Adapter.singleToMono(search(referenceType, referenceId, query, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, criteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<User>> search(ReferenceType referenceType, String referenceId, FilterCriteria criteria, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, criteria, page, size));
}
default Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, FilterCriteria criteria, int page, int size) {
    return RxJava2Adapter.singleToMono(search(referenceType, referenceId, criteria, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndEmail_migrated(domain, email, strict))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<User> findByDomainAndEmail(String domain, String email, boolean strict) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndEmail_migrated(domain, email, strict));
}
default Flux<User> findByDomainAndEmail_migrated(String domain, String email, boolean strict) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndEmail(domain, email, strict));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsernameAndDomain_migrated(domain, username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<User> findByUsernameAndDomain(String domain, String username) {
    return RxJava2Adapter.monoToMaybe(findByUsernameAndDomain_migrated(domain, username));
}
default Mono<User> findByUsernameAndDomain_migrated(String domain, String username) {
    return RxJava2Adapter.maybeToMono(findByUsernameAndDomain(domain, username));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsernameAndSource_migrated(referenceType, referenceId, username, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<User> findByUsernameAndSource(ReferenceType referenceType, String referenceId, String username, String source) {
    return RxJava2Adapter.monoToMaybe(findByUsernameAndSource_migrated(referenceType, referenceId, username, source));
}
default Mono<User> findByUsernameAndSource_migrated(ReferenceType referenceType, String referenceId, String username, String source) {
    return RxJava2Adapter.maybeToMono(findByUsernameAndSource(referenceType, referenceId, username, source));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByExternalIdAndSource_migrated(referenceType, referenceId, externalId, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<User> findByExternalIdAndSource(ReferenceType referenceType, String referenceId, String externalId, String source) {
    return RxJava2Adapter.monoToMaybe(findByExternalIdAndSource_migrated(referenceType, referenceId, externalId, source));
}
default Mono<User> findByExternalIdAndSource_migrated(ReferenceType referenceType, String referenceId, String externalId, String source) {
    return RxJava2Adapter.maybeToMono(findByExternalIdAndSource(referenceType, referenceId, externalId, source));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<User> findByIdIn(List<String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default Flux<User> findByIdIn_migrated(List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<User> findById(ReferenceType referenceType, String referenceId, String userId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, userId));
}
default Mono<User> findById_migrated(ReferenceType referenceType, String referenceId, String userId) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, userId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByReference_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countByReference(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.monoToSingle(countByReference_migrated(referenceType, referenceId));
}
default Mono<Long> countByReference_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.singleToMono(countByReference(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByApplication_migrated(domain, application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countByApplication(String domain, String application) {
    return RxJava2Adapter.monoToSingle(countByApplication_migrated(domain, application));
}
default Mono<Long> countByApplication_migrated(String domain, String application) {
    return RxJava2Adapter.singleToMono(countByApplication(domain, application));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.statistics_migrated(query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Map<Object, Object>> statistics(AnalyticsQuery query) {
    return RxJava2Adapter.monoToSingle(statistics_migrated(query));
}
default Mono<Map<Object, Object>> statistics_migrated(AnalyticsQuery query) {
    return RxJava2Adapter.singleToMono(statistics(query));
}
}

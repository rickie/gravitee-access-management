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

import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.analytics.AnalyticsQuery;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CommonUserService {

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.User> findByIdIn(java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.User> findByIdIn_migrated(List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
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
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> search(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.repository.management.api.search.FilterCriteria filterCriteria, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, filterCriteria, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> search_migrated(ReferenceType referenceType, String referenceId, FilterCriteria filterCriteria, int page, int size) {
    return RxJava2Adapter.singleToMono(search(referenceType, referenceId, filterCriteria, page, size));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findByUsernameAndSource(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String username, java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByUsernameAndSource_migrated(referenceType, referenceId, username, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findByUsernameAndSource_migrated(ReferenceType referenceType, String referenceId, String username, String source) {
    return RxJava2Adapter.maybeToMono(findByUsernameAndSource(referenceType, referenceId, username, source));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(findById(referenceType, referenceId, id));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findByExternalIdAndSource(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String externalId, java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByExternalIdAndSource_migrated(referenceType, referenceId, externalId, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findByExternalIdAndSource_migrated(ReferenceType referenceType, String referenceId, String externalId, String source) {
    return RxJava2Adapter.maybeToMono(findByExternalIdAndSource(referenceType, referenceId, externalId, source));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.service.model.NewUser newUser) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> create_migrated(ReferenceType referenceType, String referenceId, NewUser newUser) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newUser));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> create(io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(create_migrated(user));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> create_migrated(User user) {
    return RxJava2Adapter.singleToMono(create(user));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> update(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id, io.gravitee.am.service.model.UpdateUser updateUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateUser updateUser) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateUser));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> update(io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(update_migrated(user));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> update_migrated(User user) {
    return RxJava2Adapter.singleToMono(update(user));
}

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String userId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(userId));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String userId) {
    return RxJava2Adapter.completableToMono(delete(userId));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> enhance(io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(enhance_migrated(user));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> enhance_migrated(User user) {
    return RxJava2Adapter.singleToMono(enhance(user));
}
}

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
import io.gravitee.am.model.Role;

import io.gravitee.am.repository.common.CrudRepository;



import java.util.List;

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface RoleRepository extends CrudRepository<Role, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Role> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Role> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(referenceType, referenceId, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Role>> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, int page, int size) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(referenceType, referenceId, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Role>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size) {
    return RxJava2Adapter.singleToMono(findAll(referenceType, referenceId, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Role>> search(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String query, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, query, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Role>> search_migrated(ReferenceType referenceType, String referenceId, String query, int page, int size) {
    return RxJava2Adapter.singleToMono(search(referenceType, referenceId, query, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Role> findByIdIn(java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Role> findByIdIn_migrated(List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Role> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String role) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, role));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> findById_migrated(ReferenceType referenceType, String referenceId, String role) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, role));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByNameAndAssignableType_migrated(referenceType, referenceId, name, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Role> findByNameAndAssignableType(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String name, io.gravitee.am.model.ReferenceType assignableType) {
    return RxJava2Adapter.monoToMaybe(findByNameAndAssignableType_migrated(referenceType, referenceId, name, assignableType));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> findByNameAndAssignableType_migrated(ReferenceType referenceType, String referenceId, String name, ReferenceType assignableType) {
    return RxJava2Adapter.maybeToMono(findByNameAndAssignableType(referenceType, referenceId, name, assignableType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByNamesAndAssignableType_migrated(referenceType, referenceId, name, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Role> findByNamesAndAssignableType(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.util.List<java.lang.String> name, io.gravitee.am.model.ReferenceType assignableType) {
    return RxJava2Adapter.fluxToFlowable(findByNamesAndAssignableType_migrated(referenceType, referenceId, name, assignableType));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Role> findByNamesAndAssignableType_migrated(ReferenceType referenceType, String referenceId, List<String> name, ReferenceType assignableType) {
    return RxJava2Adapter.flowableToFlux(findByNamesAndAssignableType(referenceType, referenceId, name, assignableType));
}
}

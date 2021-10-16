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
import io.gravitee.am.model.Environment;
import io.gravitee.am.repository.common.CrudRepository;



import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EnvironmentRepository extends CrudRepository<Environment, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Environment> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Environment> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Environment> findAll(java.lang.String organizationId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Environment> findAll_migrated(String organizationId) {
    return RxJava2Adapter.flowableToFlux(findAll(organizationId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Environment> findById(java.lang.String id, java.lang.String organizationId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id, organizationId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Environment> findById_migrated(String id, String organizationId) {
    return RxJava2Adapter.maybeToMono(findById(id, organizationId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.count_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> count() {
    return RxJava2Adapter.monoToSingle(count_migrated());
}
default reactor.core.publisher.Mono<java.lang.Long> count_migrated() {
    return RxJava2Adapter.singleToMono(count());
}
}

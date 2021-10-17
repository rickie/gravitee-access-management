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
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.common.CrudRepository;
import io.gravitee.am.repository.management.api.search.DomainCriteria;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import java.util.Collection;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface DomainRepository extends CrudRepository<Domain, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Domain> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default Flux<Domain> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByReferenceId_migrated(environmentId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Domain> findAllByReferenceId(String environmentId) {
    return RxJava2Adapter.fluxToFlowable(findAllByReferenceId_migrated(environmentId));
}
default Flux<Domain> findAllByReferenceId_migrated(String environmentId) {
    return RxJava2Adapter.flowableToFlux(findAllByReferenceId(environmentId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.search_migrated(environmentId, query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Domain> search(String environmentId, String query) {
    return RxJava2Adapter.fluxToFlowable(search_migrated(environmentId, query));
}
default Flux<Domain> search_migrated(String environmentId, String query) {
    return RxJava2Adapter.flowableToFlux(search(environmentId, query));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByHrid_migrated(referenceType, referenceId, hrid))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Domain> findByHrid(ReferenceType referenceType, String referenceId, String hrid) {
    return RxJava2Adapter.monoToMaybe(findByHrid_migrated(referenceType, referenceId, hrid));
}
default Mono<Domain> findByHrid_migrated(ReferenceType referenceType, String referenceId, String hrid) {
    return RxJava2Adapter.maybeToMono(findByHrid(referenceType, referenceId, hrid));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Domain> findByIdIn(Collection<String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default Flux<Domain> findByIdIn_migrated(Collection<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByCriteria_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Domain> findAllByCriteria(DomainCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findAllByCriteria_migrated(criteria));
}
default Flux<Domain> findAllByCriteria_migrated(DomainCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findAllByCriteria(criteria));
}
}

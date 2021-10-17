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
import io.gravitee.am.model.alert.AlertNotifier;
import io.gravitee.am.repository.common.CrudRepository;
import io.gravitee.am.repository.management.api.search.AlertNotifierCriteria;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AlertNotifierRepository extends CrudRepository<AlertNotifier, String> {

    /**
     * Find an alert notifier by its id.
     *
     * @param id the alert notifier id.
     * @return the alert notifier found or nothing if it has not been found.
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<AlertNotifier> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<AlertNotifier> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

    /**
     * Find all the alert notifier attached to the specified reference.
     *
     * @param referenceType the type of the reference.
     * @param referenceId the id of the reference.
     * @return the alert notifiers found.
     */
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<AlertNotifier> findAll(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default Flux<AlertNotifier> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

    /**
     * Find all the alert notifier attached to the specified reference and matching the specified criteria.
     *
     * @param referenceType the type of the reference.
     * @param referenceId the id of the reference.
     * @param criteria the criteria to match.
     * @return the alert notifiers found.
     */
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCriteria_migrated(referenceType, referenceId, criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<AlertNotifier> findByCriteria(ReferenceType referenceType, String referenceId, AlertNotifierCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findByCriteria_migrated(referenceType, referenceId, criteria));
}
default Flux<AlertNotifier> findByCriteria_migrated(ReferenceType referenceType, String referenceId, AlertNotifierCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findByCriteria(referenceType, referenceId, criteria));
}
}

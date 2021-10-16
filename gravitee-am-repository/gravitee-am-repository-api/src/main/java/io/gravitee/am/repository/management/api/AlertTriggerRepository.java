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
import io.gravitee.am.model.alert.AlertTrigger;
import io.gravitee.am.repository.common.CrudRepository;
import io.gravitee.am.repository.management.api.search.AlertTriggerCriteria;
import io.reactivex.Flowable;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AlertTriggerRepository extends CrudRepository<AlertTrigger, String> {

    /**
     * Find all alert triggers for a given reference type and id.
     *
     * @param referenceType the reference type.
     * @param referenceId the reference id.
     * @return the list of alert triggers.
     */
      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.alert.AlertTrigger> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.alert.AlertTrigger> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

    /**
     * Find all alert triggers for a given reference type and id and matching specified criteria.
     *
     * @param referenceType the reference type.
     * @param referenceId the reference id.
     * @return the list of alert triggers.
     */
      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.alert.AlertTrigger> findByCriteria(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.repository.management.api.search.AlertTriggerCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findByCriteria_migrated(referenceType, referenceId, criteria));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.alert.AlertTrigger> findByCriteria_migrated(ReferenceType referenceType, String referenceId, AlertTriggerCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findByCriteria(referenceType, referenceId, criteria));
}
}

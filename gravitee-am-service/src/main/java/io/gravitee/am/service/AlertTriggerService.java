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

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.alert.AlertTrigger;
import io.gravitee.am.repository.management.api.search.AlertTriggerCriteria;
import io.gravitee.am.service.model.PatchAlertTrigger;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AlertTriggerService {
      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.alert.AlertTrigger> getById(java.lang.String id) {
    return RxJava2Adapter.monoToSingle(getById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.alert.AlertTrigger> getById_migrated(String id) {
    return RxJava2Adapter.singleToMono(getById(id));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.alert.AlertTrigger> getById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id) {
    return RxJava2Adapter.monoToSingle(getById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.alert.AlertTrigger> getById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(getById(referenceType, referenceId, id));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.alert.AlertTrigger> findByDomainAndCriteria(java.lang.String domainId, io.gravitee.am.repository.management.api.search.AlertTriggerCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndCriteria_migrated(domainId, criteria));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.alert.AlertTrigger> findByDomainAndCriteria_migrated(String domainId, AlertTriggerCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndCriteria(domainId, criteria));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.alert.AlertTrigger> createOrUpdate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.service.model.PatchAlertTrigger patchAlertTrigger, io.gravitee.am.identityprovider.api.User byUser) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, patchAlertTrigger, byUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.alert.AlertTrigger> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, PatchAlertTrigger patchAlertTrigger, User byUser) {
    return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, patchAlertTrigger, byUser));
}

      @Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String alertTriggerId, io.gravitee.am.identityprovider.api.User byUser) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, alertTriggerId, byUser));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(ReferenceType referenceType, String referenceId, String alertTriggerId, User byUser) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, alertTriggerId, byUser));
}
}

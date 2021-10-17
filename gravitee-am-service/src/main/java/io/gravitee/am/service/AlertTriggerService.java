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
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.alert.AlertTrigger;
import io.gravitee.am.repository.management.api.search.AlertTriggerCriteria;
import io.gravitee.am.service.model.PatchAlertTrigger;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AlertTriggerService {
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AlertTrigger> getById(String id) {
    return RxJava2Adapter.monoToSingle(getById_migrated(id));
}
default Mono<AlertTrigger> getById_migrated(String id) {
    return RxJava2Adapter.singleToMono(getById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AlertTrigger> getById(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.monoToSingle(getById_migrated(referenceType, referenceId, id));
}
default Mono<AlertTrigger> getById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(getById(referenceType, referenceId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndCriteria_migrated(domainId, criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<AlertTrigger> findByDomainAndCriteria(String domainId, AlertTriggerCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndCriteria_migrated(domainId, criteria));
}
default Flux<AlertTrigger> findByDomainAndCriteria_migrated(String domainId, AlertTriggerCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndCriteria(domainId, criteria));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, patchAlertTrigger, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AlertTrigger> createOrUpdate(ReferenceType referenceType, String referenceId, PatchAlertTrigger patchAlertTrigger, User byUser) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, patchAlertTrigger, byUser));
}
default Mono<AlertTrigger> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, PatchAlertTrigger patchAlertTrigger, User byUser) {
    return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, patchAlertTrigger, byUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, alertTriggerId, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(ReferenceType referenceType, String referenceId, String alertTriggerId, User byUser) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, alertTriggerId, byUser));
}
default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String alertTriggerId, User byUser) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, alertTriggerId, byUser));
}
}

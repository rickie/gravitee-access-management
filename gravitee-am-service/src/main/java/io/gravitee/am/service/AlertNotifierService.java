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

import io.gravitee.am.repository.management.api.search.AlertNotifierCriteria;
import io.gravitee.am.service.model.NewAlertNotifier;
import io.gravitee.am.service.model.PatchAlertNotifier;



import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AlertNotifierService {
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getById_migrated(referenceType, referenceId, notifierId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.alert.AlertNotifier> getById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String notifierId) {
    return RxJava2Adapter.monoToSingle(getById_migrated(referenceType, referenceId, notifierId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.alert.AlertNotifier> getById_migrated(ReferenceType referenceType, String referenceId, String notifierId) {
    return RxJava2Adapter.singleToMono(getById(referenceType, referenceId, notifierId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndCriteria_migrated(domainId, criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.alert.AlertNotifier> findByDomainAndCriteria(java.lang.String domainId, io.gravitee.am.repository.management.api.search.AlertNotifierCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndCriteria_migrated(domainId, criteria));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.alert.AlertNotifier> findByDomainAndCriteria_migrated(String domainId, AlertNotifierCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndCriteria(domainId, criteria));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReferenceAndCriteria_migrated(referenceType, referenceId, criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.alert.AlertNotifier> findByReferenceAndCriteria(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.repository.management.api.search.AlertNotifierCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findByReferenceAndCriteria_migrated(referenceType, referenceId, criteria));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.alert.AlertNotifier> findByReferenceAndCriteria_migrated(ReferenceType referenceType, String referenceId, AlertNotifierCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findByReferenceAndCriteria(referenceType, referenceId, criteria));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newAlertNotifier, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.alert.AlertNotifier> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.service.model.NewAlertNotifier newAlertNotifier, io.gravitee.am.identityprovider.api.User byUser) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newAlertNotifier, byUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.alert.AlertNotifier> create_migrated(ReferenceType referenceType, String referenceId, NewAlertNotifier newAlertNotifier, User byUser) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newAlertNotifier, byUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, alertNotifierId, patchAlertNotifier, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.alert.AlertNotifier> update(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String alertNotifierId, io.gravitee.am.service.model.PatchAlertNotifier patchAlertNotifier, io.gravitee.am.identityprovider.api.User byUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, alertNotifierId, patchAlertNotifier, byUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.alert.AlertNotifier> update_migrated(ReferenceType referenceType, String referenceId, String alertNotifierId, PatchAlertNotifier patchAlertNotifier, User byUser) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, alertNotifierId, patchAlertNotifier, byUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, notifierId, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String notifierId, io.gravitee.am.identityprovider.api.User byUser) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, notifierId, byUser));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(ReferenceType referenceType, String referenceId, String notifierId, User byUser) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, notifierId, byUser));
}
}

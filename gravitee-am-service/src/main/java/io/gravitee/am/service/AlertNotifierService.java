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
import io.gravitee.am.model.alert.AlertNotifier;
import io.gravitee.am.repository.management.api.search.AlertNotifierCriteria;
import io.gravitee.am.service.model.NewAlertNotifier;
import io.gravitee.am.service.model.PatchAlertNotifier;
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
public interface AlertNotifierService {
      
Mono<AlertNotifier> getById_migrated(ReferenceType referenceType, String referenceId, String notifierId);

      
Flux<AlertNotifier> findByDomainAndCriteria_migrated(String domainId, AlertNotifierCriteria criteria);

      
Flux<AlertNotifier> findByReferenceAndCriteria_migrated(ReferenceType referenceType, String referenceId, AlertNotifierCriteria criteria);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newAlertNotifier, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AlertNotifier> create(ReferenceType referenceType, String referenceId, NewAlertNotifier newAlertNotifier, User byUser) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newAlertNotifier, byUser));
}
default Mono<AlertNotifier> create_migrated(ReferenceType referenceType, String referenceId, NewAlertNotifier newAlertNotifier, User byUser) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newAlertNotifier, byUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, alertNotifierId, patchAlertNotifier, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AlertNotifier> update(ReferenceType referenceType, String referenceId, String alertNotifierId, PatchAlertNotifier patchAlertNotifier, User byUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, alertNotifierId, patchAlertNotifier, byUser));
}
default Mono<AlertNotifier> update_migrated(ReferenceType referenceType, String referenceId, String alertNotifierId, PatchAlertNotifier patchAlertNotifier, User byUser) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, alertNotifierId, patchAlertNotifier, byUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, notifierId, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(ReferenceType referenceType, String referenceId, String notifierId, User byUser) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, notifierId, byUser));
}
default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String notifierId, User byUser) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, notifierId, byUser));
}
}

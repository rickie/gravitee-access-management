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
import io.gravitee.am.model.Entrypoint;
import io.gravitee.am.model.Organization;
import io.gravitee.am.service.model.NewEntrypoint;
import io.gravitee.am.service.model.UpdateEntrypoint;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EntrypointService {

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Entrypoint> findById(java.lang.String id, java.lang.String organizationId) {
    return RxJava2Adapter.monoToSingle(findById_migrated(id, organizationId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Entrypoint> findById_migrated(String id, String organizationId) {
    return RxJava2Adapter.singleToMono(findById(id, organizationId));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Entrypoint> findAll(java.lang.String organizationId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Entrypoint> findAll_migrated(String organizationId) {
    return RxJava2Adapter.flowableToFlux(findAll(organizationId));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Entrypoint> create(java.lang.String organizationId, io.gravitee.am.service.model.NewEntrypoint entrypoint, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(organizationId, entrypoint, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Entrypoint> create_migrated(String organizationId, NewEntrypoint entrypoint, User principal) {
    return RxJava2Adapter.singleToMono(create(organizationId, entrypoint, principal));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Entrypoint> createDefaults(io.gravitee.am.model.Organization organization) {
    return RxJava2Adapter.fluxToFlowable(createDefaults_migrated(organization));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Entrypoint> createDefaults_migrated(Organization organization) {
    return RxJava2Adapter.flowableToFlux(createDefaults(organization));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Entrypoint> update(java.lang.String entrypointId, java.lang.String organizationId, io.gravitee.am.service.model.UpdateEntrypoint entrypoint, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(entrypointId, organizationId, entrypoint, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Entrypoint> update_migrated(String entrypointId, String organizationId, UpdateEntrypoint entrypoint, User principal) {
    return RxJava2Adapter.singleToMono(update(entrypointId, organizationId, entrypoint, principal));
}

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String entrypointId, java.lang.String organizationId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(entrypointId, organizationId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String entrypointId, String organizationId, User principal) {
    return RxJava2Adapter.completableToMono(delete(entrypointId, organizationId, principal));
}
}

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
import io.gravitee.am.model.Entrypoint;
import io.gravitee.am.model.Organization;
import io.gravitee.am.service.model.NewEntrypoint;
import io.gravitee.am.service.model.UpdateEntrypoint;
import io.reactivex.Completable;

import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EntrypointService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Entrypoint> findById(String id, String organizationId) {
    return RxJava2Adapter.monoToSingle(findById_migrated(id, organizationId));
}
default Mono<Entrypoint> findById_migrated(String id, String organizationId) {
    return RxJava2Adapter.singleToMono(findById(id, organizationId));
}

      
Flux<Entrypoint> findAll_migrated(String organizationId);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(organizationId, entrypoint, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Entrypoint> create(String organizationId, NewEntrypoint entrypoint, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(organizationId, entrypoint, principal));
}
default Mono<Entrypoint> create_migrated(String organizationId, NewEntrypoint entrypoint, User principal) {
    return RxJava2Adapter.singleToMono(create(organizationId, entrypoint, principal));
}

      
Flux<Entrypoint> createDefaults_migrated(Organization organization);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(entrypointId, organizationId, entrypoint, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Entrypoint> update(String entrypointId, String organizationId, UpdateEntrypoint entrypoint, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(entrypointId, organizationId, entrypoint, principal));
}
default Mono<Entrypoint> update_migrated(String entrypointId, String organizationId, UpdateEntrypoint entrypoint, User principal) {
    return RxJava2Adapter.singleToMono(update(entrypointId, organizationId, entrypoint, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(entrypointId, organizationId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String entrypointId, String organizationId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(entrypointId, organizationId, principal));
}
default Mono<Void> delete_migrated(String entrypointId, String organizationId, User principal) {
    return RxJava2Adapter.completableToMono(delete(entrypointId, organizationId, principal));
}
}

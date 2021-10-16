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
import io.gravitee.am.model.Environment;
import io.gravitee.am.service.model.NewEnvironment;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EnvironmentService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Environment> findById(java.lang.String id, java.lang.String organizationId) {
    return RxJava2Adapter.monoToSingle(findById_migrated(id, organizationId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Environment> findById_migrated(String id, String organizationId) {
    return RxJava2Adapter.singleToMono(findById(id, organizationId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Environment> findById(java.lang.String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Environment> findById_migrated(String id) {
    return RxJava2Adapter.singleToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Environment> findAll(java.lang.String organizationId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Environment> findAll_migrated(String organizationId) {
    return RxJava2Adapter.flowableToFlux(findAll(organizationId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.createDefault_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Environment> createDefault() {
    return RxJava2Adapter.monoToMaybe(createDefault_migrated());
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Environment> createDefault_migrated() {
    return RxJava2Adapter.maybeToMono(createDefault());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(organizationId, environmentId, newEnvironment, createdBy))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Environment> createOrUpdate(java.lang.String organizationId, java.lang.String environmentId, io.gravitee.am.service.model.NewEnvironment newEnvironment, io.gravitee.am.identityprovider.api.User createdBy) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(organizationId, environmentId, newEnvironment, createdBy));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Environment> createOrUpdate_migrated(String organizationId, String environmentId, NewEnvironment newEnvironment, User createdBy) {
    return RxJava2Adapter.singleToMono(createOrUpdate(organizationId, environmentId, newEnvironment, createdBy));
}
}
 
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EnvironmentService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Environment> findById(String id, String organizationId) {
    return RxJava2Adapter.monoToSingle(findById_migrated(id, organizationId));
}
default Mono<Environment> findById_migrated(String id, String organizationId) {
    return RxJava2Adapter.singleToMono(findById(id, organizationId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Environment> findById(String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(id));
}
default Mono<Environment> findById_migrated(String id) {
    return RxJava2Adapter.singleToMono(findById(id));
}

      
Flux<Environment> findAll_migrated(String organizationId);

      
Mono<Environment> createDefault_migrated();

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(organizationId, environmentId, newEnvironment, createdBy))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Environment> createOrUpdate(String organizationId, String environmentId, NewEnvironment newEnvironment, User createdBy) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(organizationId, environmentId, newEnvironment, createdBy));
}
default Mono<Environment> createOrUpdate_migrated(String organizationId, String environmentId, NewEnvironment newEnvironment, User createdBy) {
    return RxJava2Adapter.singleToMono(createOrUpdate(organizationId, environmentId, newEnvironment, createdBy));
}
}
 
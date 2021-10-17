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
import io.gravitee.am.model.flow.Flow;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface FlowService {

      
Flux<Flow> findAll_migrated(ReferenceType referenceType, String referenceId, boolean excludeApps);

      
Flux<Flow> findByApplication_migrated(ReferenceType referenceType, String referenceId, String application);

    List<Flow> defaultFlows(ReferenceType referenceType, String referenceId);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Flow> findById(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
default Mono<Flow> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Flow> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Flow> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, flow, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Flow> create(ReferenceType referenceType, String referenceId, Flow flow, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, flow, principal));
}
default Mono<Flow> create_migrated(ReferenceType referenceType, String referenceId, Flow flow, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, flow, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, application, flow, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Flow> create(ReferenceType referenceType, String referenceId, String application, Flow flow, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, application, flow, principal));
}
default Mono<Flow> create_migrated(ReferenceType referenceType, String referenceId, String application, Flow flow, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, application, flow, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, flow, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Flow> update(ReferenceType referenceType, String referenceId, String id, Flow flow, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, flow, principal));
}
default Mono<Flow> update_migrated(ReferenceType referenceType, String referenceId, String id, Flow flow, User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, flow, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, flows, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<Flow>> createOrUpdate(ReferenceType referenceType, String referenceId, List<Flow> flows, User principal) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, flows, principal));
}
default Mono<List<Flow>> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, List<Flow> flows, User principal) {
    return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, flows, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, application, flows, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<Flow>> createOrUpdate(ReferenceType referenceType, String referenceId, String application, List<Flow> flows, User principal) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, application, flows, principal));
}
default Mono<List<Flow>> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, String application, List<Flow> flows, User principal) {
    return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, application, flows, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String id, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id, principal));
}
default Mono<Void> delete_migrated(String id, User principal) {
    return RxJava2Adapter.completableToMono(delete(id, principal));
}

      
Mono<String> getSchema_migrated();

      Flux<Flow> findAll_migrated(ReferenceType referenceType, String referenceId);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, flow))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Flow> create(ReferenceType referenceType, String referenceId, Flow flow) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, flow));
}default Mono<Flow> create_migrated(ReferenceType referenceType, String referenceId, Flow flow) {
        return RxJava2Adapter.singleToMono(create(referenceType, referenceId, flow, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, application, flow))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Flow> create(ReferenceType referenceType, String referenceId, String application, Flow flow) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, application, flow));
}default Mono<Flow> create_migrated(ReferenceType referenceType, String referenceId, String application, Flow flow) {
        return RxJava2Adapter.singleToMono(create(referenceType, referenceId, application, flow, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, flow))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Flow> update(ReferenceType referenceType, String referenceId, String id, Flow flow) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, flow));
}default Mono<Flow> update_migrated(ReferenceType referenceType, String referenceId, String id, Flow flow) {
        return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, flow, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, flows))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<Flow>> createOrUpdate(ReferenceType referenceType, String referenceId, List<Flow> flows) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, flows));
}default Mono<List<Flow>> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, List<Flow> flows) {
        return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, flows, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, application, flows))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<Flow>> createOrUpdate(ReferenceType referenceType, String referenceId, String application, List<Flow> flows) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, application, flows));
}default Mono<List<Flow>> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, String application, List<Flow> flows) {
        return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, application, flows, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}default Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(delete(id, null));
    }
}

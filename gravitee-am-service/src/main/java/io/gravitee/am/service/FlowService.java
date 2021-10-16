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




import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface FlowService {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId, excludeApps))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.flow.Flow> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, boolean excludeApps) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId, excludeApps));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.flow.Flow> findAll_migrated(ReferenceType referenceType, String referenceId, boolean excludeApps) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId, excludeApps));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByApplication_migrated(referenceType, referenceId, application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.flow.Flow> findByApplication(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String application) {
    return RxJava2Adapter.fluxToFlowable(findByApplication_migrated(referenceType, referenceId, application));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.flow.Flow> findByApplication_migrated(ReferenceType referenceType, String referenceId, String application) {
    return RxJava2Adapter.flowableToFlux(findByApplication(referenceType, referenceId, application));
}

    List<Flow> defaultFlows(ReferenceType referenceType, String referenceId);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.flow.Flow> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.flow.Flow> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.flow.Flow> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.flow.Flow> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, flow, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.flow.Flow> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.model.flow.Flow flow, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, flow, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.flow.Flow> create_migrated(ReferenceType referenceType, String referenceId, Flow flow, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, flow, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, application, flow, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.flow.Flow> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String application, io.gravitee.am.model.flow.Flow flow, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, application, flow, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.flow.Flow> create_migrated(ReferenceType referenceType, String referenceId, String application, Flow flow, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, application, flow, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, flow, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.flow.Flow> update(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id, io.gravitee.am.model.flow.Flow flow, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, flow, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.flow.Flow> update_migrated(ReferenceType referenceType, String referenceId, String id, Flow flow, User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, flow, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, flows, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.flow.Flow>> createOrUpdate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.util.List<io.gravitee.am.model.flow.Flow> flows, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, flows, principal));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.flow.Flow>> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, List<Flow> flows, User principal) {
    return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, flows, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, application, flows, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.flow.Flow>> createOrUpdate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String application, java.util.List<io.gravitee.am.model.flow.Flow> flows, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, application, flows, principal));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.flow.Flow>> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, String application, List<Flow> flows, User principal) {
    return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, application, flows, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String id, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String id, User principal) {
    return RxJava2Adapter.completableToMono(delete(id, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getSchema_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.String> getSchema() {
    return RxJava2Adapter.monoToSingle(getSchema_migrated());
}
default reactor.core.publisher.Mono<java.lang.String> getSchema_migrated() {
    return RxJava2Adapter.singleToMono(getSchema());
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.flow.Flow> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}default Flux<Flow> findAll_migrated(ReferenceType referenceType, String referenceId) {
        return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId, false));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, flow))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.flow.Flow> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.model.flow.Flow flow) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, flow));
}default Mono<Flow> create_migrated(ReferenceType referenceType, String referenceId, Flow flow) {
        return RxJava2Adapter.singleToMono(create(referenceType, referenceId, flow, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, application, flow))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.flow.Flow> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String application, io.gravitee.am.model.flow.Flow flow) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, application, flow));
}default Mono<Flow> create_migrated(ReferenceType referenceType, String referenceId, String application, Flow flow) {
        return RxJava2Adapter.singleToMono(create(referenceType, referenceId, application, flow, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, flow))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.flow.Flow> update(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id, io.gravitee.am.model.flow.Flow flow) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, flow));
}default Mono<Flow> update_migrated(ReferenceType referenceType, String referenceId, String id, Flow flow) {
        return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, flow, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, flows))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.flow.Flow>> createOrUpdate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.util.List<io.gravitee.am.model.flow.Flow> flows) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, flows));
}default Mono<List<Flow>> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, List<Flow> flows) {
        return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, flows, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, application, flows))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.flow.Flow>> createOrUpdate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String application, java.util.List<io.gravitee.am.model.flow.Flow> flows) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, application, flows));
}default Mono<List<Flow>> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, String application, List<Flow> flows) {
        return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, application, flows, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}default Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(delete(id, null));
    }
}

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
package io.gravitee.am.repository.management.api;

import io.gravitee.am.model.Policy;
import io.gravitee.am.repository.common.CrudRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * NOTE: only use for the PoliciesToFlowsUpgrader Upgrader
 * Use the {@link io.gravitee.am.repository.management.api.FlowRepository} for the flow management
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Deprecated
public interface PolicyRepository {

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Policy> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Policy> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @Deprecated  
default io.reactivex.Single<java.lang.Boolean> collectionExists() {
    return RxJava2Adapter.monoToSingle(collectionExists_migrated());
}
default reactor.core.publisher.Mono<java.lang.Boolean> collectionExists_migrated() {
    return RxJava2Adapter.singleToMono(collectionExists());
}

      @Deprecated  
default io.reactivex.Completable deleteCollection() {
    return RxJava2Adapter.monoToCompletable(deleteCollection_migrated());
}
default reactor.core.publisher.Mono<java.lang.Void> deleteCollection_migrated() {
    return RxJava2Adapter.completableToMono(deleteCollection());
}
}

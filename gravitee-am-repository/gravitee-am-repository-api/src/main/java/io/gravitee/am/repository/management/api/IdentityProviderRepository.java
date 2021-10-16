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

import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.common.CrudRepository;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface IdentityProviderRepository extends CrudRepository<IdentityProvider, String> {

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.IdentityProvider> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.IdentityProvider> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.IdentityProvider> findAll(io.gravitee.am.model.ReferenceType referenceType) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.IdentityProvider> findAll_migrated(ReferenceType referenceType) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.IdentityProvider> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default reactor.core.publisher.Flux<io.gravitee.am.model.IdentityProvider> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.IdentityProvider> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String identityProviderId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, identityProviderId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.IdentityProvider> findById_migrated(ReferenceType referenceType, String referenceId, String identityProviderId) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, identityProviderId));
}
}

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

import io.gravitee.am.model.Application;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.common.CrudRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ApplicationRepository extends CrudRepository<Application, String> {

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> findAll(int page, int size) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> findAll_migrated(int page, int size) {
    return RxJava2Adapter.singleToMono(findAll(page, size));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> search(java.lang.String domain, java.lang.String query, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(domain, query, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> search_migrated(String domain, String query, int page, int size) {
    return RxJava2Adapter.singleToMono(search(domain, query, page, size));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByCertificate(java.lang.String certificate) {
    return RxJava2Adapter.fluxToFlowable(findByCertificate_migrated(certificate));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByCertificate_migrated(String certificate) {
    return RxJava2Adapter.flowableToFlux(findByCertificate(certificate));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByIdentityProvider(java.lang.String identityProvider) {
    return RxJava2Adapter.fluxToFlowable(findByIdentityProvider_migrated(identityProvider));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByIdentityProvider_migrated(String identityProvider) {
    return RxJava2Adapter.flowableToFlux(findByIdentityProvider(identityProvider));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByFactor(java.lang.String factor) {
    return RxJava2Adapter.fluxToFlowable(findByFactor_migrated(factor));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByFactor_migrated(String factor) {
    return RxJava2Adapter.flowableToFlux(findByFactor(factor));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByDomainAndExtensionGrant(java.lang.String domain, java.lang.String extensionGrant) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndExtensionGrant_migrated(domain, extensionGrant));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByDomainAndExtensionGrant_migrated(String domain, String extensionGrant) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndExtensionGrant(domain, extensionGrant));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByIdIn(java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByIdIn_migrated(List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @Deprecated  
default io.reactivex.Single<java.lang.Long> count() {
    return RxJava2Adapter.monoToSingle(count_migrated());
}
default reactor.core.publisher.Mono<java.lang.Long> count_migrated() {
    return RxJava2Adapter.singleToMono(count());
}

      @Deprecated  
default io.reactivex.Single<java.lang.Long> countByDomain(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(countByDomain_migrated(domain));
}
default reactor.core.publisher.Mono<java.lang.Long> countByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(countByDomain(domain));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Application> findByDomainAndClientId(java.lang.String domain, java.lang.String clientId) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientId_migrated(domain, clientId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Application> findByDomainAndClientId_migrated(String domain, String clientId) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientId(domain, clientId));
}

}

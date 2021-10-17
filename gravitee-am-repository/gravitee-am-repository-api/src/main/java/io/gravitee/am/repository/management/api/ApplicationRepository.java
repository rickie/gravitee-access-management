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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.common.CrudRepository;
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
public interface ApplicationRepository extends CrudRepository<Application, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Application> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default Flux<Application> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<Application>> findAll(int page, int size) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(page, size));
}
default Mono<Page<Application>> findAll_migrated(int page, int size) {
    return RxJava2Adapter.singleToMono(findAll(page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Application> findByDomain(String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default Flux<Application> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<Application>> findByDomain(String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default Mono<Page<Application>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<Application>> search(String domain, String query, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(domain, query, page, size));
}
default Mono<Page<Application>> search_migrated(String domain, String query, int page, int size) {
    return RxJava2Adapter.singleToMono(search(domain, query, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCertificate_migrated(certificate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Application> findByCertificate(String certificate) {
    return RxJava2Adapter.fluxToFlowable(findByCertificate_migrated(certificate));
}
default Flux<Application> findByCertificate_migrated(String certificate) {
    return RxJava2Adapter.flowableToFlux(findByCertificate(certificate));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdentityProvider_migrated(identityProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Application> findByIdentityProvider(String identityProvider) {
    return RxJava2Adapter.fluxToFlowable(findByIdentityProvider_migrated(identityProvider));
}
default Flux<Application> findByIdentityProvider_migrated(String identityProvider) {
    return RxJava2Adapter.flowableToFlux(findByIdentityProvider(identityProvider));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByFactor_migrated(factor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Application> findByFactor(String factor) {
    return RxJava2Adapter.fluxToFlowable(findByFactor_migrated(factor));
}
default Flux<Application> findByFactor_migrated(String factor) {
    return RxJava2Adapter.flowableToFlux(findByFactor(factor));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndExtensionGrant_migrated(domain, extensionGrant))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Application> findByDomainAndExtensionGrant(String domain, String extensionGrant) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndExtensionGrant_migrated(domain, extensionGrant));
}
default Flux<Application> findByDomainAndExtensionGrant_migrated(String domain, String extensionGrant) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndExtensionGrant(domain, extensionGrant));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Application> findByIdIn(List<String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default Flux<Application> findByIdIn_migrated(List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.count_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> count() {
    return RxJava2Adapter.monoToSingle(count_migrated());
}
default Mono<Long> count_migrated() {
    return RxJava2Adapter.singleToMono(count());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countByDomain(String domain) {
    return RxJava2Adapter.monoToSingle(countByDomain_migrated(domain));
}
default Mono<Long> countByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(countByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientId_migrated(domain, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Application> findByDomainAndClientId(String domain, String clientId) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientId_migrated(domain, clientId));
}
default Mono<Application> findByDomainAndClientId_migrated(String domain, String clientId) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientId(domain, clientId));
}

}

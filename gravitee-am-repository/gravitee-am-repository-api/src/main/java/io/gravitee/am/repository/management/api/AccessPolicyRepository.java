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

import io.gravitee.am.model.uma.policy.AccessPolicy;
import io.gravitee.am.repository.common.CrudRepository;


import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AccessPolicyRepository extends CrudRepository<AccessPolicy, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.policy.AccessPolicy>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.policy.AccessPolicy>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndResource_migrated(domain, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.policy.AccessPolicy> findByDomainAndResource(java.lang.String domain, java.lang.String resource) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndResource_migrated(domain, resource));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.policy.AccessPolicy> findByDomainAndResource_migrated(String domain, String resource) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndResource(domain, resource));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByResources_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.policy.AccessPolicy> findByResources(java.util.List<java.lang.String> resources) {
    return RxJava2Adapter.fluxToFlowable(findByResources_migrated(resources));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.policy.AccessPolicy> findByResources_migrated(List<String> resources) {
    return RxJava2Adapter.flowableToFlux(findByResources(resources));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByResource_migrated(resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countByResource(java.lang.String resource) {
    return RxJava2Adapter.monoToSingle(countByResource_migrated(resource));
}
default reactor.core.publisher.Mono<java.lang.Long> countByResource_migrated(String resource) {
    return RxJava2Adapter.singleToMono(countByResource(resource));
}
}

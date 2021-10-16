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

import io.gravitee.am.model.uma.Resource;
import io.gravitee.am.repository.common.CrudRepository;



import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface ResourceRepository extends CrudRepository<Resource, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.Resource>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.Resource>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomainAndClient_migrated(domain, client, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.Resource>> findByDomainAndClient(java.lang.String domain, java.lang.String client, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomainAndClient_migrated(domain, client, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.Resource>> findByDomainAndClient_migrated(String domain, String client, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomainAndClient(domain, client, page, size));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByResources_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.Resource> findByResources(java.util.List<java.lang.String> resources) {
    return RxJava2Adapter.fluxToFlowable(findByResources_migrated(resources));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.Resource> findByResources_migrated(List<String> resources) {
    return RxJava2Adapter.flowableToFlux(findByResources(resources));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndUser_migrated(domain, client, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndUser(java.lang.String domain, java.lang.String client, java.lang.String userId) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndUser_migrated(domain, client, userId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndUser_migrated(String domain, String client, String userId) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClientAndUser(domain, client, userId));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndResources_migrated(domain, client, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndResources(java.lang.String domain, java.lang.String client, java.util.List<java.lang.String> resource) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndResources_migrated(domain, client, resource));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndResources_migrated(String domain, String client, List<String> resource) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClientAndResources(domain, client, resource));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndUserAndResource(java.lang.String domain, java.lang.String client, java.lang.String userId, java.lang.String resource) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resource));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndUserAndResource_migrated(String domain, String client, String userId, String resource) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, userId, resource));
}
}

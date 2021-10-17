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
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.uma.Resource;
import io.gravitee.am.model.uma.policy.AccessPolicy;
import io.gravitee.am.service.model.NewResource;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ResourceService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<Resource>> findByDomain(String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default Mono<Page<Resource>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomainAndClient_migrated(domain, client, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<Resource>> findByDomainAndClient(String domain, String client, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomainAndClient_migrated(domain, client, page, size));
}
default Mono<Page<Resource>> findByDomainAndClient_migrated(String domain, String client, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomainAndClient(domain, client, page, size));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByResources_migrated(resourceIds))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Resource> findByResources(List<String> resourceIds) {
    return RxJava2Adapter.fluxToFlowable(findByResources_migrated(resourceIds));
}
default Flux<Resource> findByResources_migrated(List<String> resourceIds) {
    return RxJava2Adapter.flowableToFlux(findByResources(resourceIds));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.listByDomainAndClientAndUser_migrated(domain, client, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Resource> listByDomainAndClientAndUser(String domain, String client, String userId) {
    return RxJava2Adapter.fluxToFlowable(listByDomainAndClientAndUser_migrated(domain, client, userId));
}
default Flux<Resource> listByDomainAndClientAndUser_migrated(String domain, String client, String userId) {
    return RxJava2Adapter.flowableToFlux(listByDomainAndClientAndUser(domain, client, userId));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndResources_migrated(domain, client, resourceIds))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Resource> findByDomainAndClientAndResources(String domain, String client, List<String> resourceIds) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndResources_migrated(domain, client, resourceIds));
}
default Flux<Resource> findByDomainAndClientAndResources_migrated(String domain, String client, List<String> resourceIds) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClientAndResources(domain, client, resourceIds));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Resource> findByDomainAndClientAndUserAndResource(String domain, String client, String userId, String resourceId) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resourceId));
}
default Mono<Resource> findByDomainAndClientAndUserAndResource_migrated(String domain, String client, String userId, String resourceId) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, userId, resourceId));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientResource_migrated(domain, client, resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Resource> findByDomainAndClientResource(String domain, String client, String resourceId) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientResource_migrated(domain, client, resourceId));
}
default Mono<Resource> findByDomainAndClientResource_migrated(String domain, String client, String resourceId) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientResource(domain, client, resourceId));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getMetadata_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Map<String, Map<String, Object>>> getMetadata(List<Resource> resources) {
    return RxJava2Adapter.monoToSingle(getMetadata_migrated(resources));
}
default Mono<Map<String, Map<String, Object>>> getMetadata_migrated(List<Resource> resources) {
    return RxJava2Adapter.singleToMono(getMetadata(resources));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(newResource, domain, client, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Resource> create(NewResource newResource, String domain, String client, String userId) {
    return RxJava2Adapter.monoToSingle(create_migrated(newResource, domain, client, userId));
}
default Mono<Resource> create_migrated(NewResource newResource, String domain, String client, String userId) {
    return RxJava2Adapter.singleToMono(create(newResource, domain, client, userId));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(newResource, domain, client, userId, resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Resource> update(NewResource newResource, String domain, String client, String userId, String resourceId) {
    return RxJava2Adapter.monoToSingle(update_migrated(newResource, domain, client, userId, resourceId));
}
default Mono<Resource> update_migrated(NewResource newResource, String domain, String client, String userId, String resourceId) {
    return RxJava2Adapter.singleToMono(update(newResource, domain, client, userId, resourceId));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Resource> update(Resource resource) {
    return RxJava2Adapter.monoToSingle(update_migrated(resource));
}
default Mono<Resource> update_migrated(Resource resource) {
    return RxJava2Adapter.singleToMono(update(resource));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, client, userId, resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String domain, String client, String userId, String resourceId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, client, userId, resourceId));
}
default Mono<Void> delete_migrated(String domain, String client, String userId, String resourceId) {
    return RxJava2Adapter.completableToMono(delete(domain, client, userId, resourceId));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(Resource resource) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(resource));
}
default Mono<Void> delete_migrated(Resource resource) {
    return RxJava2Adapter.completableToMono(delete(resource));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAccessPolicies_migrated(domain, client, user, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<AccessPolicy> findAccessPolicies(String domain, String client, String user, String resource) {
    return RxJava2Adapter.fluxToFlowable(findAccessPolicies_migrated(domain, client, user, resource));
}
default Flux<AccessPolicy> findAccessPolicies_migrated(String domain, String client, String user, String resource) {
    return RxJava2Adapter.flowableToFlux(findAccessPolicies(domain, client, user, resource));
}
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAccessPoliciesByResources_migrated(resourceIds))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<AccessPolicy> findAccessPoliciesByResources(List<String> resourceIds) {
    return RxJava2Adapter.fluxToFlowable(findAccessPoliciesByResources_migrated(resourceIds));
}
default Flux<AccessPolicy> findAccessPoliciesByResources_migrated(List<String> resourceIds) {
    return RxJava2Adapter.flowableToFlux(findAccessPoliciesByResources(resourceIds));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countAccessPolicyByResource_migrated(resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countAccessPolicyByResource(String resourceId) {
    return RxJava2Adapter.monoToSingle(countAccessPolicyByResource_migrated(resourceId));
}
default Mono<Long> countAccessPolicyByResource_migrated(String resourceId) {
    return RxJava2Adapter.singleToMono(countAccessPolicyByResource(resourceId));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findAccessPolicy_migrated(domain, client, user, resource, accessPolicy))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<AccessPolicy> findAccessPolicy(String domain, String client, String user, String resource, String accessPolicy) {
    return RxJava2Adapter.monoToMaybe(findAccessPolicy_migrated(domain, client, user, resource, accessPolicy));
}
default Mono<AccessPolicy> findAccessPolicy_migrated(String domain, String client, String user, String resource, String accessPolicy) {
    return RxJava2Adapter.maybeToMono(findAccessPolicy(domain, client, user, resource, accessPolicy));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findAccessPolicy_migrated(accessPolicy))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<AccessPolicy> findAccessPolicy(String accessPolicy) {
    return RxJava2Adapter.monoToMaybe(findAccessPolicy_migrated(accessPolicy));
}
default Mono<AccessPolicy> findAccessPolicy_migrated(String accessPolicy) {
    return RxJava2Adapter.maybeToMono(findAccessPolicy(accessPolicy));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createAccessPolicy_migrated(accessPolicy, domain, client, user, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AccessPolicy> createAccessPolicy(AccessPolicy accessPolicy, String domain, String client, String user, String resource) {
    return RxJava2Adapter.monoToSingle(createAccessPolicy_migrated(accessPolicy, domain, client, user, resource));
}
default Mono<AccessPolicy> createAccessPolicy_migrated(AccessPolicy accessPolicy, String domain, String client, String user, String resource) {
    return RxJava2Adapter.singleToMono(createAccessPolicy(accessPolicy, domain, client, user, resource));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.updateAccessPolicy_migrated(accessPolicy, domain, client, user, resource, accessPolicyId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AccessPolicy> updateAccessPolicy(AccessPolicy accessPolicy, String domain, String client, String user, String resource, String accessPolicyId) {
    return RxJava2Adapter.monoToSingle(updateAccessPolicy_migrated(accessPolicy, domain, client, user, resource, accessPolicyId));
}
default Mono<AccessPolicy> updateAccessPolicy_migrated(AccessPolicy accessPolicy, String domain, String client, String user, String resource, String accessPolicyId) {
    return RxJava2Adapter.singleToMono(updateAccessPolicy(accessPolicy, domain, client, user, resource, accessPolicyId));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteAccessPolicy_migrated(domain, client, user, resource, accessPolicy))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable deleteAccessPolicy(String domain, String client, String user, String resource, String accessPolicy) {
    return RxJava2Adapter.monoToCompletable(deleteAccessPolicy_migrated(domain, client, user, resource, accessPolicy));
}
default Mono<Void> deleteAccessPolicy_migrated(String domain, String client, String user, String resource, String accessPolicy) {
    return RxJava2Adapter.completableToMono(deleteAccessPolicy(domain, client, user, resource, accessPolicy));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Set<Resource>> findByDomain(String domain) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain));
}default Mono<Set<Resource>> findByDomain_migrated(String domain) {
        return RxJava2Adapter.singleToMono(findByDomain(domain, 0, Integer.MAX_VALUE)).map(RxJavaReactorMigrationUtil.toJdkFunction(pagedResources -> (pagedResources.getData() == null) ? Collections.emptySet() : new HashSet<>(pagedResources.getData())));
    }
}

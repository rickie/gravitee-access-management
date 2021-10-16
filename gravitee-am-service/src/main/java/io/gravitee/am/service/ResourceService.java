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

import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.uma.Resource;
import io.gravitee.am.model.uma.policy.AccessPolicy;
import io.gravitee.am.service.model.NewResource;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.*;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ResourceService {

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.Resource>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.Resource>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}
      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.Resource>> findByDomainAndClient(java.lang.String domain, java.lang.String client, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomainAndClient_migrated(domain, client, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.uma.Resource>> findByDomainAndClient_migrated(String domain, String client, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomainAndClient(domain, client, page, size));
}
      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.Resource> findByResources(java.util.List<java.lang.String> resourceIds) {
    return RxJava2Adapter.fluxToFlowable(findByResources_migrated(resourceIds));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.Resource> findByResources_migrated(List<String> resourceIds) {
    return RxJava2Adapter.flowableToFlux(findByResources(resourceIds));
}
      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.Resource> listByDomainAndClientAndUser(java.lang.String domain, java.lang.String client, java.lang.String userId) {
    return RxJava2Adapter.fluxToFlowable(listByDomainAndClientAndUser_migrated(domain, client, userId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.Resource> listByDomainAndClientAndUser_migrated(String domain, String client, String userId) {
    return RxJava2Adapter.flowableToFlux(listByDomainAndClientAndUser(domain, client, userId));
}
      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndResources(java.lang.String domain, java.lang.String client, java.util.List<java.lang.String> resourceIds) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndResources_migrated(domain, client, resourceIds));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndResources_migrated(String domain, String client, List<String> resourceIds) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClientAndResources(domain, client, resourceIds));
}
      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndUserAndResource(java.lang.String domain, java.lang.String client, java.lang.String userId, java.lang.String resourceId) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resourceId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.Resource> findByDomainAndClientAndUserAndResource_migrated(String domain, String client, String userId, String resourceId) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserAndResource(domain, client, userId, resourceId));
}
      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.uma.Resource> findByDomainAndClientResource(java.lang.String domain, java.lang.String client, java.lang.String resourceId) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientResource_migrated(domain, client, resourceId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.Resource> findByDomainAndClientResource_migrated(String domain, String client, String resourceId) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientResource(domain, client, resourceId));
}
      @Deprecated  
default io.reactivex.Single<java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Object>>> getMetadata(java.util.List<io.gravitee.am.model.uma.Resource> resources) {
    return RxJava2Adapter.monoToSingle(getMetadata_migrated(resources));
}
default reactor.core.publisher.Mono<java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Object>>> getMetadata_migrated(List<Resource> resources) {
    return RxJava2Adapter.singleToMono(getMetadata(resources));
}
      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.uma.Resource> create(io.gravitee.am.service.model.NewResource newResource, java.lang.String domain, java.lang.String client, java.lang.String userId) {
    return RxJava2Adapter.monoToSingle(create_migrated(newResource, domain, client, userId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.Resource> create_migrated(NewResource newResource, String domain, String client, String userId) {
    return RxJava2Adapter.singleToMono(create(newResource, domain, client, userId));
}
      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.uma.Resource> update(io.gravitee.am.service.model.NewResource newResource, java.lang.String domain, java.lang.String client, java.lang.String userId, java.lang.String resourceId) {
    return RxJava2Adapter.monoToSingle(update_migrated(newResource, domain, client, userId, resourceId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.Resource> update_migrated(NewResource newResource, String domain, String client, String userId, String resourceId) {
    return RxJava2Adapter.singleToMono(update(newResource, domain, client, userId, resourceId));
}
      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.uma.Resource> update(io.gravitee.am.model.uma.Resource resource) {
    return RxJava2Adapter.monoToSingle(update_migrated(resource));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.Resource> update_migrated(Resource resource) {
    return RxJava2Adapter.singleToMono(update(resource));
}
      @Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String client, java.lang.String userId, java.lang.String resourceId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, client, userId, resourceId));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String domain, String client, String userId, String resourceId) {
    return RxJava2Adapter.completableToMono(delete(domain, client, userId, resourceId));
}
      @Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.model.uma.Resource resource) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(resource));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(Resource resource) {
    return RxJava2Adapter.completableToMono(delete(resource));
}
      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.policy.AccessPolicy> findAccessPolicies(java.lang.String domain, java.lang.String client, java.lang.String user, java.lang.String resource) {
    return RxJava2Adapter.fluxToFlowable(findAccessPolicies_migrated(domain, client, user, resource));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.policy.AccessPolicy> findAccessPolicies_migrated(String domain, String client, String user, String resource) {
    return RxJava2Adapter.flowableToFlux(findAccessPolicies(domain, client, user, resource));
}
      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.uma.policy.AccessPolicy> findAccessPoliciesByResources(java.util.List<java.lang.String> resourceIds) {
    return RxJava2Adapter.fluxToFlowable(findAccessPoliciesByResources_migrated(resourceIds));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.uma.policy.AccessPolicy> findAccessPoliciesByResources_migrated(List<String> resourceIds) {
    return RxJava2Adapter.flowableToFlux(findAccessPoliciesByResources(resourceIds));
}
      @Deprecated  
default io.reactivex.Single<java.lang.Long> countAccessPolicyByResource(java.lang.String resourceId) {
    return RxJava2Adapter.monoToSingle(countAccessPolicyByResource_migrated(resourceId));
}
default reactor.core.publisher.Mono<java.lang.Long> countAccessPolicyByResource_migrated(String resourceId) {
    return RxJava2Adapter.singleToMono(countAccessPolicyByResource(resourceId));
}
      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.uma.policy.AccessPolicy> findAccessPolicy(java.lang.String domain, java.lang.String client, java.lang.String user, java.lang.String resource, java.lang.String accessPolicy) {
    return RxJava2Adapter.monoToMaybe(findAccessPolicy_migrated(domain, client, user, resource, accessPolicy));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.policy.AccessPolicy> findAccessPolicy_migrated(String domain, String client, String user, String resource, String accessPolicy) {
    return RxJava2Adapter.maybeToMono(findAccessPolicy(domain, client, user, resource, accessPolicy));
}
      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.uma.policy.AccessPolicy> findAccessPolicy(java.lang.String accessPolicy) {
    return RxJava2Adapter.monoToMaybe(findAccessPolicy_migrated(accessPolicy));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.policy.AccessPolicy> findAccessPolicy_migrated(String accessPolicy) {
    return RxJava2Adapter.maybeToMono(findAccessPolicy(accessPolicy));
}
      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.uma.policy.AccessPolicy> createAccessPolicy(io.gravitee.am.model.uma.policy.AccessPolicy accessPolicy, java.lang.String domain, java.lang.String client, java.lang.String user, java.lang.String resource) {
    return RxJava2Adapter.monoToSingle(createAccessPolicy_migrated(accessPolicy, domain, client, user, resource));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.policy.AccessPolicy> createAccessPolicy_migrated(AccessPolicy accessPolicy, String domain, String client, String user, String resource) {
    return RxJava2Adapter.singleToMono(createAccessPolicy(accessPolicy, domain, client, user, resource));
}
      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.uma.policy.AccessPolicy> updateAccessPolicy(io.gravitee.am.model.uma.policy.AccessPolicy accessPolicy, java.lang.String domain, java.lang.String client, java.lang.String user, java.lang.String resource, java.lang.String accessPolicyId) {
    return RxJava2Adapter.monoToSingle(updateAccessPolicy_migrated(accessPolicy, domain, client, user, resource, accessPolicyId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.policy.AccessPolicy> updateAccessPolicy_migrated(AccessPolicy accessPolicy, String domain, String client, String user, String resource, String accessPolicyId) {
    return RxJava2Adapter.singleToMono(updateAccessPolicy(accessPolicy, domain, client, user, resource, accessPolicyId));
}
      @Deprecated  
default io.reactivex.Completable deleteAccessPolicy(java.lang.String domain, java.lang.String client, java.lang.String user, java.lang.String resource, java.lang.String accessPolicy) {
    return RxJava2Adapter.monoToCompletable(deleteAccessPolicy_migrated(domain, client, user, resource, accessPolicy));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteAccessPolicy_migrated(String domain, String client, String user, String resource, String accessPolicy) {
    return RxJava2Adapter.completableToMono(deleteAccessPolicy(domain, client, user, resource, accessPolicy));
}

      @Deprecated  
default io.reactivex.Single<java.util.Set<io.gravitee.am.model.uma.Resource>> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain));
}default Mono<Set<Resource>> findByDomain_migrated(String domain) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(findByDomain(domain, 0, Integer.MAX_VALUE)).map(RxJavaReactorMigrationUtil.toJdkFunction(pagedResources -> (pagedResources.getData() == null) ? Collections.emptySet() : new HashSet<>(pagedResources.getData())))));
    }
}

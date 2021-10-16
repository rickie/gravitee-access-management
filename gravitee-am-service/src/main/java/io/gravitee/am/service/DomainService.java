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

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Domain;
import io.gravitee.am.repository.management.api.search.DomainCriteria;
import io.gravitee.am.service.model.NewDomain;
import io.gravitee.am.service.model.PatchDomain;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface DomainService {

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Domain> findAllByEnvironment(java.lang.String organizationId, java.lang.String environment) {
    return RxJava2Adapter.fluxToFlowable(findAllByEnvironment_migrated(organizationId, environment));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Domain> findAllByEnvironment_migrated(String organizationId, String environment) {
    return RxJava2Adapter.flowableToFlux(findAllByEnvironment(organizationId, environment));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Domain> search(java.lang.String organizationId, java.lang.String environmentId, java.lang.String query) {
    return RxJava2Adapter.fluxToFlowable(search_migrated(organizationId, environmentId, query));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Domain> search_migrated(String organizationId, String environmentId, String query) {
    return RxJava2Adapter.flowableToFlux(search(organizationId, environmentId, query));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Domain> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Domain> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Domain> findByHrid(java.lang.String environmentId, java.lang.String hrid) {
    return RxJava2Adapter.monoToSingle(findByHrid_migrated(environmentId, hrid));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Domain> findByHrid_migrated(String environmentId, String hrid) {
    return RxJava2Adapter.singleToMono(findByHrid(environmentId, hrid));
}

      @Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.Domain>> findAll() {
    return RxJava2Adapter.monoToSingle(findAll_migrated());
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.Domain>> findAll_migrated() {
    return RxJava2Adapter.singleToMono(findAll());
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Domain> findAllByCriteria(io.gravitee.am.repository.management.api.search.DomainCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findAllByCriteria_migrated(criteria));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Domain> findAllByCriteria_migrated(DomainCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findAllByCriteria(criteria));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Domain> findByIdIn(java.util.Collection<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Domain> findByIdIn_migrated(Collection<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Domain> create(java.lang.String organizationId, java.lang.String environmentId, io.gravitee.am.service.model.NewDomain domain, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(organizationId, environmentId, domain, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Domain> create_migrated(String organizationId, String environmentId, NewDomain domain, User principal) {
    return RxJava2Adapter.singleToMono(create(organizationId, environmentId, domain, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Domain> update(java.lang.String domainId, io.gravitee.am.model.Domain domain) {
    return RxJava2Adapter.monoToSingle(update_migrated(domainId, domain));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Domain> update_migrated(String domainId, Domain domain) {
    return RxJava2Adapter.singleToMono(update(domainId, domain));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Domain> patch(java.lang.String domainId, io.gravitee.am.service.model.PatchDomain domain, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domainId, domain, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Domain> patch_migrated(String domainId, PatchDomain domain, User principal) {
    return RxJava2Adapter.singleToMono(patch(domainId, domain, principal));
}

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String domain, User principal) {
    return RxJava2Adapter.completableToMono(delete(domain, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Domain> create(java.lang.String organizationId, java.lang.String environmentId, io.gravitee.am.service.model.NewDomain domain) {
    return RxJava2Adapter.monoToSingle(create_migrated(organizationId, environmentId, domain));
}default Mono<Domain> create_migrated(String organizationId, String environmentId, NewDomain domain) {
        return RxJava2Adapter.singleToMono(create(organizationId, environmentId, domain, null));
    }

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Domain> patch(java.lang.String domainId, io.gravitee.am.service.model.PatchDomain domain) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domainId, domain));
}default Mono<Domain> patch_migrated(String domainId, PatchDomain domain) {
        return RxJava2Adapter.singleToMono(patch(domainId, domain, null));
    }

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String domain) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain));
}default Mono<Void> delete_migrated(String domain) {
        return RxJava2Adapter.completableToMono(delete(domain, null));
    }

    String buildUrl(Domain domain, String path);
}

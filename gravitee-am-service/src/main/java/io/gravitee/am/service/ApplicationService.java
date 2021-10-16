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
import io.gravitee.am.model.Application;
import io.gravitee.am.model.application.ApplicationType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.service.model.NewApplication;
import io.gravitee.am.service.model.PatchApplication;
import io.gravitee.am.service.model.TopApplication;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ApplicationService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> findAll(int page, int size) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> findAll_migrated(int page, int size) {
    return RxJava2Adapter.singleToMono(findAll(page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> search(java.lang.String domain, java.lang.String query, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(domain, query, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Application>> search_migrated(String domain, String query, int page, int size) {
    return RxJava2Adapter.singleToMono(search(domain, query, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCertificate_migrated(certificate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByCertificate(java.lang.String certificate) {
    return RxJava2Adapter.fluxToFlowable(findByCertificate_migrated(certificate));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByCertificate_migrated(String certificate) {
    return RxJava2Adapter.flowableToFlux(findByCertificate(certificate));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdentityProvider_migrated(identityProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByIdentityProvider(java.lang.String identityProvider) {
    return RxJava2Adapter.fluxToFlowable(findByIdentityProvider_migrated(identityProvider));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByIdentityProvider_migrated(String identityProvider) {
    return RxJava2Adapter.flowableToFlux(findByIdentityProvider(identityProvider));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByFactor_migrated(factor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByFactor(java.lang.String factor) {
    return RxJava2Adapter.fluxToFlowable(findByFactor_migrated(factor));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByFactor_migrated(String factor) {
    return RxJava2Adapter.flowableToFlux(findByFactor(factor));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomainAndExtensionGrant_migrated(domain, extensionGrant))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.Set<io.gravitee.am.model.Application>> findByDomainAndExtensionGrant(java.lang.String domain, java.lang.String extensionGrant) {
    return RxJava2Adapter.monoToSingle(findByDomainAndExtensionGrant_migrated(domain, extensionGrant));
}
default reactor.core.publisher.Mono<java.util.Set<io.gravitee.am.model.Application>> findByDomainAndExtensionGrant_migrated(String domain, String extensionGrant) {
    return RxJava2Adapter.singleToMono(findByDomainAndExtensionGrant(domain, extensionGrant));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Application> findByIdIn(java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Application> findByIdIn_migrated(List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Application> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Application> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientId_migrated(domain, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Application> findByDomainAndClientId(java.lang.String domain, java.lang.String clientId) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientId_migrated(domain, clientId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Application> findByDomainAndClientId_migrated(String domain, String clientId) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientId(domain, clientId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newApplication, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Application> create(java.lang.String domain, io.gravitee.am.service.model.NewApplication newApplication, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newApplication, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Application> create_migrated(String domain, NewApplication newApplication, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newApplication, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Application> create(io.gravitee.am.model.Application application) {
    return RxJava2Adapter.monoToSingle(create_migrated(application));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Application> create_migrated(Application application) {
    return RxJava2Adapter.singleToMono(create(application));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Application> update(io.gravitee.am.model.Application application) {
    return RxJava2Adapter.monoToSingle(update_migrated(application));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Application> update_migrated(Application application) {
    return RxJava2Adapter.singleToMono(update(application));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.updateType_migrated(domain, id, type, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Application> updateType(java.lang.String domain, java.lang.String id, io.gravitee.am.model.application.ApplicationType type, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(updateType_migrated(domain, id, type, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Application> updateType_migrated(String domain, String id, ApplicationType type, User principal) {
    return RxJava2Adapter.singleToMono(updateType(domain, id, type, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(domain, id, patchApplication, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Application> patch(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.PatchApplication patchApplication, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domain, id, patchApplication, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Application> patch_migrated(String domain, String id, PatchApplication patchApplication, User principal) {
    return RxJava2Adapter.singleToMono(patch(domain, id, patchApplication, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.renewClientSecret_migrated(domain, id, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Application> renewClientSecret(java.lang.String domain, java.lang.String id, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(renewClientSecret_migrated(domain, id, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Application> renewClientSecret_migrated(String domain, String id, User principal) {
    return RxJava2Adapter.singleToMono(renewClientSecret(domain, id, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String id, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String id, User principal) {
    return RxJava2Adapter.completableToMono(delete(id, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.count_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> count() {
    return RxJava2Adapter.monoToSingle(count_migrated());
}
default reactor.core.publisher.Mono<java.lang.Long> count_migrated() {
    return RxJava2Adapter.singleToMono(count());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByDomain_migrated(domainId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countByDomain(java.lang.String domainId) {
    return RxJava2Adapter.monoToSingle(countByDomain_migrated(domainId));
}
default reactor.core.publisher.Mono<java.lang.Long> countByDomain_migrated(String domainId) {
    return RxJava2Adapter.singleToMono(countByDomain(domainId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTopApplications_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.Set<io.gravitee.am.service.model.TopApplication>> findTopApplications() {
    return RxJava2Adapter.monoToSingle(findTopApplications_migrated());
}
default reactor.core.publisher.Mono<java.util.Set<io.gravitee.am.service.model.TopApplication>> findTopApplications_migrated() {
    return RxJava2Adapter.singleToMono(findTopApplications());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTopApplicationsByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.Set<io.gravitee.am.service.model.TopApplication>> findTopApplicationsByDomain(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(findTopApplicationsByDomain_migrated(domain));
}
default reactor.core.publisher.Mono<java.util.Set<io.gravitee.am.service.model.TopApplication>> findTopApplicationsByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(findTopApplicationsByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.Set<io.gravitee.am.model.Application>> findAll() {
    return RxJava2Adapter.monoToSingle(findAll_migrated());
}default Mono<Set<Application>> findAll_migrated() {
        return RxJava2Adapter.singleToMono(findAll(0, Integer.MAX_VALUE)).map(RxJavaReactorMigrationUtil.toJdkFunction(pagedApplications -> (pagedApplications.getData() == null) ? Collections.emptySet() : new HashSet<>(pagedApplications.getData())));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.Set<io.gravitee.am.model.Application>> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain));
}default Mono<Set<Application>> findByDomain_migrated(String domain) {
        return RxJava2Adapter.singleToMono(findByDomain(domain, 0, Integer.MAX_VALUE)).map(RxJavaReactorMigrationUtil.toJdkFunction(pagedApplications -> (pagedApplications.getData() == null) ? Collections.emptySet() : new HashSet<>(pagedApplications.getData())));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newApplication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Application> create(java.lang.String domain, io.gravitee.am.service.model.NewApplication newApplication) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newApplication));
}default Mono<Application> create_migrated(String domain, NewApplication newApplication) {
        return RxJava2Adapter.singleToMono(create(domain, newApplication, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(domain, id, patchApplication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Application> patch(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.PatchApplication patchApplication) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domain, id, patchApplication));
}default Mono<Application> patch_migrated(String domain, String id, PatchApplication patchApplication) {
        return RxJava2Adapter.singleToMono(patch(domain, id, patchApplication, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.renewClientSecret_migrated(domain, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Application> renewClientSecret(java.lang.String domain, java.lang.String id) {
    return RxJava2Adapter.monoToSingle(renewClientSecret_migrated(domain, id));
}default Mono<Application> renewClientSecret_migrated(String domain, String id) {
        return RxJava2Adapter.singleToMono(renewClientSecret(domain, id, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}default Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(delete(id, null));
    }
}

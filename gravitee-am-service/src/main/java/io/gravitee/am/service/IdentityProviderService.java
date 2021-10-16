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
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.service.model.NewIdentityProvider;
import io.gravitee.am.service.model.UpdateIdentityProvider;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface IdentityProviderService {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.IdentityProvider> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default reactor.core.publisher.Flux<io.gravitee.am.model.IdentityProvider> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.IdentityProvider> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.IdentityProvider> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(findById(referenceType, referenceId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.IdentityProvider> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.IdentityProvider> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.IdentityProvider> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.IdentityProvider> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.IdentityProvider> findAll(io.gravitee.am.model.ReferenceType referenceType) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.IdentityProvider> findAll_migrated(ReferenceType referenceType) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.IdentityProvider> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.IdentityProvider> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.IdentityProvider> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.service.model.NewIdentityProvider newIdentityProvider, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newIdentityProvider, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.IdentityProvider> create_migrated(ReferenceType referenceType, String referenceId, NewIdentityProvider newIdentityProvider, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newIdentityProvider, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, identityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.IdentityProvider> create(java.lang.String domain, io.gravitee.am.service.model.NewIdentityProvider identityProvider, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, identityProvider, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.IdentityProvider> create_migrated(String domain, NewIdentityProvider identityProvider, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, identityProvider, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.IdentityProvider> update(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id, io.gravitee.am.service.model.UpdateIdentityProvider updateIdentityProvider, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateIdentityProvider, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.IdentityProvider> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateIdentityProvider, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.IdentityProvider> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateIdentityProvider updateIdentityProvider, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateIdentityProvider, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.IdentityProvider> update_migrated(String domain, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateIdentityProvider, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, identityProviderId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String identityProviderId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, identityProviderId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(ReferenceType referenceType, String referenceId, String identityProviderId, User principal) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, identityProviderId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, identityProviderId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String identityProviderId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, identityProviderId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String domain, String identityProviderId, User principal) {
    return RxJava2Adapter.completableToMono(delete(domain, identityProviderId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, identityProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.IdentityProvider> create(java.lang.String domain, io.gravitee.am.service.model.NewIdentityProvider identityProvider) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, identityProvider));
}default Mono<IdentityProvider> create_migrated(String domain, NewIdentityProvider identityProvider) {
        return RxJava2Adapter.singleToMono(create(domain, identityProvider, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateIdentityProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.IdentityProvider> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateIdentityProvider updateIdentityProvider) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateIdentityProvider));
}default Mono<IdentityProvider> update_migrated(String domain, String id, UpdateIdentityProvider updateIdentityProvider) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateIdentityProvider, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, identityProviderId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String identityProviderId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, identityProviderId));
}default Mono<Void> delete_migrated(String domain, String identityProviderId) {
        return RxJava2Adapter.completableToMono(delete(domain, identityProviderId, null));
    }
}

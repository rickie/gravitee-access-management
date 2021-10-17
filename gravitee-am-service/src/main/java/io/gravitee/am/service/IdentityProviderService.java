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
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface IdentityProviderService {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<IdentityProvider> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default Flux<IdentityProvider> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<IdentityProvider> findById(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
default Mono<IdentityProvider> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(findById(referenceType, referenceId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<IdentityProvider> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<IdentityProvider> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<IdentityProvider> findAll(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default Flux<IdentityProvider> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<IdentityProvider> findAll(ReferenceType referenceType) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
default Flux<IdentityProvider> findAll_migrated(ReferenceType referenceType) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<IdentityProvider> findByDomain(String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default Flux<IdentityProvider> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<IdentityProvider> create(ReferenceType referenceType, String referenceId, NewIdentityProvider newIdentityProvider, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newIdentityProvider, principal));
}
default Mono<IdentityProvider> create_migrated(ReferenceType referenceType, String referenceId, NewIdentityProvider newIdentityProvider, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newIdentityProvider, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, identityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<IdentityProvider> create(String domain, NewIdentityProvider identityProvider, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, identityProvider, principal));
}
default Mono<IdentityProvider> create_migrated(String domain, NewIdentityProvider identityProvider, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, identityProvider, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<IdentityProvider> update(ReferenceType referenceType, String referenceId, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateIdentityProvider, principal));
}
default Mono<IdentityProvider> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateIdentityProvider, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<IdentityProvider> update(String domain, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateIdentityProvider, principal));
}
default Mono<IdentityProvider> update_migrated(String domain, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateIdentityProvider, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, identityProviderId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(ReferenceType referenceType, String referenceId, String identityProviderId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, identityProviderId, principal));
}
default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String identityProviderId, User principal) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, identityProviderId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, identityProviderId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String domain, String identityProviderId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, identityProviderId, principal));
}
default Mono<Void> delete_migrated(String domain, String identityProviderId, User principal) {
    return RxJava2Adapter.completableToMono(delete(domain, identityProviderId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, identityProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<IdentityProvider> create(String domain, NewIdentityProvider identityProvider) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, identityProvider));
}default Mono<IdentityProvider> create_migrated(String domain, NewIdentityProvider identityProvider) {
        return RxJava2Adapter.singleToMono(create(domain, identityProvider, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateIdentityProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<IdentityProvider> update(String domain, String id, UpdateIdentityProvider updateIdentityProvider) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateIdentityProvider));
}default Mono<IdentityProvider> update_migrated(String domain, String id, UpdateIdentityProvider updateIdentityProvider) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateIdentityProvider, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, identityProviderId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String domain, String identityProviderId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, identityProviderId));
}default Mono<Void> delete_migrated(String domain, String identityProviderId) {
        return RxJava2Adapter.completableToMono(delete(domain, identityProviderId, null));
    }
}

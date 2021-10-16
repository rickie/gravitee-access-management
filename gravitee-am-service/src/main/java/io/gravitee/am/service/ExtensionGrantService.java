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
import io.gravitee.am.model.ExtensionGrant;
import io.gravitee.am.service.model.NewExtensionGrant;
import io.gravitee.am.service.model.UpdateExtensionGrant;




import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ExtensionGrantService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.ExtensionGrant> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.ExtensionGrant> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(tokenGranter))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.ExtensionGrant> findByDomain(java.lang.String tokenGranter) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(tokenGranter));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.ExtensionGrant> findByDomain_migrated(String tokenGranter) {
    return RxJava2Adapter.flowableToFlux(findByDomain(tokenGranter));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newExtensionGrant, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.ExtensionGrant> create(java.lang.String domain, io.gravitee.am.service.model.NewExtensionGrant newExtensionGrant, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newExtensionGrant, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.ExtensionGrant> create_migrated(String domain, NewExtensionGrant newExtensionGrant, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newExtensionGrant, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateExtensionGrant, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.ExtensionGrant> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateExtensionGrant updateExtensionGrant, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateExtensionGrant, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.ExtensionGrant> update_migrated(String domain, String id, UpdateExtensionGrant updateExtensionGrant, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateExtensionGrant, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, certificateId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String certificateId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, certificateId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String domain, String certificateId, User principal) {
    return RxJava2Adapter.completableToMono(delete(domain, certificateId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newExtensionGrant))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.ExtensionGrant> create(java.lang.String domain, io.gravitee.am.service.model.NewExtensionGrant newExtensionGrant) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newExtensionGrant));
}default Mono<ExtensionGrant> create_migrated(String domain, NewExtensionGrant newExtensionGrant) {
        return RxJava2Adapter.singleToMono(create(domain, newExtensionGrant, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateExtensionGrant))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.ExtensionGrant> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateExtensionGrant updateExtensionGrant) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateExtensionGrant));
}default Mono<ExtensionGrant> update_migrated(String domain, String id, UpdateExtensionGrant updateExtensionGrant) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateExtensionGrant, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, certificateId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String certificateId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, certificateId));
}default Mono<Void> delete_migrated(String domain, String certificateId) {
        return RxJava2Adapter.completableToMono(delete(domain, certificateId, null));
    }

}

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
import io.gravitee.am.model.Factor;
import io.gravitee.am.service.model.NewFactor;
import io.gravitee.am.service.model.UpdateFactor;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface FactorService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Factor> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Factor> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Factor> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Factor> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, factor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Factor> create(java.lang.String domain, io.gravitee.am.service.model.NewFactor factor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, factor, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Factor> create_migrated(String domain, NewFactor factor, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, factor, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateFactor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Factor> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateFactor updateFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateFactor, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Factor> update_migrated(String domain, String id, UpdateFactor updateFactor, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateFactor, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, factorId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String factorId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, factorId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String domain, String factorId, User principal) {
    return RxJava2Adapter.completableToMono(delete(domain, factorId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, factor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Factor> create(java.lang.String domain, io.gravitee.am.service.model.NewFactor factor) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, factor));
}default Mono<Factor> create_migrated(String domain, NewFactor factor) {
        return RxJava2Adapter.singleToMono(create(domain, factor, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateFactor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Factor> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateFactor updateFactor) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateFactor));
}default Mono<Factor> update_migrated(String domain, String id, UpdateFactor updateFactor) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateFactor, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, factorId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String factorId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, factorId));
}default Mono<Void> delete_migrated(String domain, String factorId) {
        return RxJava2Adapter.completableToMono(delete(domain, factorId, null));
    }
}

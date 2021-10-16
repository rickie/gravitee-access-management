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
import io.gravitee.am.model.Reporter;
import io.gravitee.am.service.model.NewReporter;
import io.gravitee.am.service.model.UpdateReporter;





import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ReporterService {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Reporter> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Reporter> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Reporter> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Reporter> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Reporter> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Reporter> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createDefault_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Reporter> createDefault(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(createDefault_migrated(domain));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Reporter> createDefault_migrated(String domain) {
    return RxJava2Adapter.singleToMono(createDefault(domain));
}

    NewReporter createInternal(String domain);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newReporter, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Reporter> create(java.lang.String domain, io.gravitee.am.service.model.NewReporter newReporter, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newReporter, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Reporter> create_migrated(String domain, NewReporter newReporter, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newReporter, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateReporter, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Reporter> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateReporter updateReporter, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateReporter, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Reporter> update_migrated(String domain, String id, UpdateReporter updateReporter, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateReporter, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(reporterId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String reporterId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(reporterId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String reporterId, User principal) {
    return RxJava2Adapter.completableToMono(delete(reporterId, principal));
}

    default NewReporter createInternal() {
        return createInternal(null);
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newReporter))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Reporter> create(java.lang.String domain, io.gravitee.am.service.model.NewReporter newReporter) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newReporter));
}default Mono<Reporter> create_migrated(String domain, NewReporter newReporter) {
        return RxJava2Adapter.singleToMono(create(domain, newReporter, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateReporter))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Reporter> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateReporter updateReporter) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateReporter));
}default Mono<Reporter> update_migrated(String domain, String id, UpdateReporter updateReporter) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateReporter, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(reporterId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String reporterId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(reporterId));
}default Mono<Void> delete_migrated(String reporterId) {
        return RxJava2Adapter.completableToMono(delete(reporterId, null));
    }
}

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
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ReporterService {

      
Flux<Reporter> findAll_migrated();

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Reporter> findByDomain(String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default Flux<Reporter> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Reporter> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Reporter> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Mono<Reporter> createDefault_migrated(String domain);

    NewReporter createInternal(String domain);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newReporter, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Reporter> create(String domain, NewReporter newReporter, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newReporter, principal));
}
default Mono<Reporter> create_migrated(String domain, NewReporter newReporter, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newReporter, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateReporter, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Reporter> update(String domain, String id, UpdateReporter updateReporter, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateReporter, principal));
}
default Mono<Reporter> update_migrated(String domain, String id, UpdateReporter updateReporter, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateReporter, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(reporterId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String reporterId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(reporterId, principal));
}
default Mono<Void> delete_migrated(String reporterId, User principal) {
    return RxJava2Adapter.completableToMono(delete(reporterId, principal));
}

    default NewReporter createInternal() {
        return createInternal(null);
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newReporter))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Reporter> create(String domain, NewReporter newReporter) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newReporter));
}default Mono<Reporter> create_migrated(String domain, NewReporter newReporter) {
        return RxJava2Adapter.singleToMono(create(domain, newReporter, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateReporter))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Reporter> update(String domain, String id, UpdateReporter updateReporter) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateReporter));
}default Mono<Reporter> update_migrated(String domain, String id, UpdateReporter updateReporter) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateReporter, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(reporterId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String reporterId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(reporterId));
}default Mono<Void> delete_migrated(String reporterId) {
        return RxJava2Adapter.completableToMono(delete(reporterId, null));
    }
}

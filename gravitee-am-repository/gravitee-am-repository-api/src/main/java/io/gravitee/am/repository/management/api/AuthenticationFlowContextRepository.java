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
package io.gravitee.am.repository.management.api;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.AuthenticationFlowContext;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * Repository to store information between different phases of authentication flow.
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AuthenticationFlowContextRepository {
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.AuthenticationFlowContext> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.AuthenticationFlowContext> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}
    /**
     * Find last context data for given sessionId
     *
     * @param transactionId transactionId id
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findLastByTransactionId_migrated(transactionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.AuthenticationFlowContext> findLastByTransactionId(java.lang.String transactionId) {
    return RxJava2Adapter.monoToMaybe(findLastByTransactionId_migrated(transactionId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.AuthenticationFlowContext> findLastByTransactionId_migrated(String transactionId) {
    return RxJava2Adapter.maybeToMono(findLastByTransactionId(transactionId));
}
    /**
     * Find all contexts data for given sessionId
     *
     * @param transactionId transactionId id
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByTransactionId_migrated(transactionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.AuthenticationFlowContext> findByTransactionId(java.lang.String transactionId) {
    return RxJava2Adapter.fluxToFlowable(findByTransactionId_migrated(transactionId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.AuthenticationFlowContext> findByTransactionId_migrated(String transactionId) {
    return RxJava2Adapter.flowableToFlux(findByTransactionId(transactionId));
}

    /**
     * Create authentication context
     * @param
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(context))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.AuthenticationFlowContext> create(io.gravitee.am.model.AuthenticationFlowContext context) {
    return RxJava2Adapter.monoToSingle(create_migrated(context));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.AuthenticationFlowContext> create_migrated(AuthenticationFlowContext context) {
    return RxJava2Adapter.singleToMono(create(context));
}

    /**
     * Delete all context for given transactionId Id
     * @param transactionId
     * @return acknowledge of the operation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(transactionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String transactionId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(transactionId));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String transactionId) {
    return RxJava2Adapter.completableToMono(delete(transactionId));
}

    /**
     * Delete context for given transactionId Id and specific version
     * @param transactionId
     * @param version
     * @return acknowledge of the operation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(transactionId, version))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String transactionId, int version) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(transactionId, version));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String transactionId, int version) {
    return RxJava2Adapter.completableToMono(delete(transactionId, version));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.purgeExpiredData_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable purgeExpiredData() {
    return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}default Mono<Void> purgeExpiredData_migrated() {
        return Mono.empty();
    }
}

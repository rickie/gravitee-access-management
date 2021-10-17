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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository to store information between different phases of authentication flow.
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AuthenticationFlowContextRepository {
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<AuthenticationFlowContext> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<AuthenticationFlowContext> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}
    /**
     * Find last context data for given sessionId
     *
     * @param transactionId transactionId id
     * @return
     */
      
Mono<AuthenticationFlowContext> findLastByTransactionId_migrated(String transactionId);
    /**
     * Find all contexts data for given sessionId
     *
     * @param transactionId transactionId id
     * @return
     */
      
Flux<AuthenticationFlowContext> findByTransactionId_migrated(String transactionId);

    /**
     * Create authentication context
     * @param
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(context))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AuthenticationFlowContext> create(AuthenticationFlowContext context) {
    return RxJava2Adapter.monoToSingle(create_migrated(context));
}
default Mono<AuthenticationFlowContext> create_migrated(AuthenticationFlowContext context) {
    return RxJava2Adapter.singleToMono(create(context));
}

    /**
     * Delete all context for given transactionId Id
     * @param transactionId
     * @return acknowledge of the operation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(transactionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String transactionId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(transactionId));
}
default Mono<Void> delete_migrated(String transactionId) {
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
default Completable delete(String transactionId, int version) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(transactionId, version));
}
default Mono<Void> delete_migrated(String transactionId, int version) {
    return RxJava2Adapter.completableToMono(delete(transactionId, version));
}

      default Mono<Void> purgeExpiredData_migrated() {
        return Mono.empty();
    }
}

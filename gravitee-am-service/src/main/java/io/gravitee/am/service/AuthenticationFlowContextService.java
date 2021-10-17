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
import io.gravitee.am.model.AuthenticationFlowContext;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AuthenticationFlowContextService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadContext_migrated(transactionId, expectedVersion))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<AuthenticationFlowContext> loadContext(final String transactionId, final int expectedVersion) {
    return RxJava2Adapter.monoToMaybe(loadContext_migrated(transactionId, expectedVersion));
}
default Mono<AuthenticationFlowContext> loadContext_migrated(final String transactionId, final int expectedVersion) {
    return RxJava2Adapter.maybeToMono(loadContext(transactionId, expectedVersion));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.removeContext_migrated(transactionId, expectedVersion))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<AuthenticationFlowContext> removeContext(final String transactionId, final int expectedVersion) {
    return RxJava2Adapter.monoToMaybe(removeContext_migrated(transactionId, expectedVersion));
}
default Mono<AuthenticationFlowContext> removeContext_migrated(final String transactionId, final int expectedVersion) {
    return RxJava2Adapter.maybeToMono(removeContext(transactionId, expectedVersion));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.clearContext_migrated(transactionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable clearContext(final String transactionId) {
    return RxJava2Adapter.monoToCompletable(clearContext_migrated(transactionId));
}
default Mono<Void> clearContext_migrated(final String transactionId) {
    return RxJava2Adapter.completableToMono(clearContext(transactionId));
}
}

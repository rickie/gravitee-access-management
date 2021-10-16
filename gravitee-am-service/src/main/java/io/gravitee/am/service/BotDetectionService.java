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
import io.gravitee.am.model.BotDetection;
import io.gravitee.am.service.model.NewBotDetection;
import io.gravitee.am.service.model.UpdateBotDetection;





import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface BotDetectionService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.BotDetection> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.BotDetection> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.BotDetection> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.BotDetection> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, botDetection, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.BotDetection> create(java.lang.String domain, io.gravitee.am.service.model.NewBotDetection botDetection, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, botDetection, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.BotDetection> create_migrated(String domain, NewBotDetection botDetection, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, botDetection, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateBotDetection, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.BotDetection> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateBotDetection updateBotDetection, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateBotDetection, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.BotDetection> update_migrated(String domain, String id, UpdateBotDetection updateBotDetection, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateBotDetection, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, botDetectionId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String botDetectionId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, botDetectionId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String domain, String botDetectionId, User principal) {
    return RxJava2Adapter.completableToMono(delete(domain, botDetectionId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, botDetection))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.BotDetection> create(java.lang.String domain, io.gravitee.am.service.model.NewBotDetection botDetection) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, botDetection));
}default Mono<BotDetection> create_migrated(String domain, NewBotDetection botDetection) {
        return RxJava2Adapter.singleToMono(create(domain, botDetection, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateBotDetection))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.BotDetection> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateBotDetection updateBotDetection) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateBotDetection));
}default Mono<BotDetection> update_migrated(String domain, String id, UpdateBotDetection updateBotDetection) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateBotDetection, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, botDetectionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String botDetectionId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, botDetectionId));
}default Mono<Void> delete_migrated(String domain, String botDetectionId) {
        return RxJava2Adapter.completableToMono(delete(domain, botDetectionId, null));
    }
}

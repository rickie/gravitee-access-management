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

import io.gravitee.am.model.LoginAttempt;
import io.gravitee.am.repository.common.CrudRepository;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface LoginAttemptRepository extends CrudRepository<LoginAttempt, String> {

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.LoginAttempt> findByCriteria(io.gravitee.am.repository.management.api.search.LoginAttemptCriteria criteria) {
    return RxJava2Adapter.monoToMaybe(findByCriteria_migrated(criteria));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.LoginAttempt> findByCriteria_migrated(LoginAttemptCriteria criteria) {
    return RxJava2Adapter.maybeToMono(findByCriteria(criteria));
}

      @Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.repository.management.api.search.LoginAttemptCriteria criteria) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(criteria));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(LoginAttemptCriteria criteria) {
    return RxJava2Adapter.completableToMono(delete(criteria));
}

      @Deprecated  
default io.reactivex.Completable purgeExpiredData() {
    return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}default Mono<Void> purgeExpiredData_migrated() {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty()));
    }
}

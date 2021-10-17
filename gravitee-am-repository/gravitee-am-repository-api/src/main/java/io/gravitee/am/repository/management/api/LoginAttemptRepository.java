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

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByCriteria_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<LoginAttempt> findByCriteria(LoginAttemptCriteria criteria) {
    return RxJava2Adapter.monoToMaybe(findByCriteria_migrated(criteria));
}
default Mono<LoginAttempt> findByCriteria_migrated(LoginAttemptCriteria criteria) {
    return RxJava2Adapter.maybeToMono(findByCriteria(criteria));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(LoginAttemptCriteria criteria) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(criteria));
}
default Mono<Void> delete_migrated(LoginAttemptCriteria criteria) {
    return RxJava2Adapter.completableToMono(delete(criteria));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.purgeExpiredData_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable purgeExpiredData() {
    return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}default Mono<Void> purgeExpiredData_migrated() {
        return Mono.empty();
    }
}

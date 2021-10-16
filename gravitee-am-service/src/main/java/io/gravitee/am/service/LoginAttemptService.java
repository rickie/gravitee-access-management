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
import io.gravitee.am.model.LoginAttempt;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface LoginAttemptService {

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.loginSucceeded_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable loginSucceeded(io.gravitee.am.repository.management.api.search.LoginAttemptCriteria criteria) {
    return RxJava2Adapter.monoToCompletable(loginSucceeded_migrated(criteria));
}
default reactor.core.publisher.Mono<java.lang.Void> loginSucceeded_migrated(LoginAttemptCriteria criteria) {
    return RxJava2Adapter.completableToMono(loginSucceeded(criteria));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.loginFailed_migrated(criteria, accountSettings))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.LoginAttempt> loginFailed(io.gravitee.am.repository.management.api.search.LoginAttemptCriteria criteria, io.gravitee.am.model.account.AccountSettings accountSettings) {
    return RxJava2Adapter.monoToSingle(loginFailed_migrated(criteria, accountSettings));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.LoginAttempt> loginFailed_migrated(LoginAttemptCriteria criteria, AccountSettings accountSettings) {
    return RxJava2Adapter.singleToMono(loginFailed(criteria, accountSettings));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.reset_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable reset(io.gravitee.am.repository.management.api.search.LoginAttemptCriteria criteria) {
    return RxJava2Adapter.monoToCompletable(reset_migrated(criteria));
}
default reactor.core.publisher.Mono<java.lang.Void> reset_migrated(LoginAttemptCriteria criteria) {
    return RxJava2Adapter.completableToMono(reset(criteria));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.checkAccount_migrated(criteria, accountSettings))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.LoginAttempt> checkAccount(io.gravitee.am.repository.management.api.search.LoginAttemptCriteria criteria, io.gravitee.am.model.account.AccountSettings accountSettings) {
    return RxJava2Adapter.monoToMaybe(checkAccount_migrated(criteria, accountSettings));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.LoginAttempt> checkAccount_migrated(LoginAttemptCriteria criteria, AccountSettings accountSettings) {
    return RxJava2Adapter.maybeToMono(checkAccount(criteria, accountSettings));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.LoginAttempt> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.LoginAttempt> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}


}

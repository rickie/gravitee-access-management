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
package io.gravitee.am.factor.api;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.factor.api.Enrollment;
import io.gravitee.am.factor.api.FactorContext;
import io.gravitee.am.model.User;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface FactorProvider {

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.verify_migrated(context))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable verify(FactorContext context) {
    return RxJava2Adapter.monoToCompletable(verify_migrated(context));
}
default Mono<Void> verify_migrated(FactorContext context) {
    return RxJava2Adapter.completableToMono(verify(context));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enroll_migrated(account))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Enrollment> enroll(String account) {
    return RxJava2Adapter.monoToSingle(enroll_migrated(account));
}
default Mono<Enrollment> enroll_migrated(String account) {
    return RxJava2Adapter.singleToMono(enroll(account));
}

    boolean checkSecurityFactor(EnrolledFactor securityFactor);

    boolean needChallengeSending();

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.sendChallenge_migrated(context))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable sendChallenge(FactorContext context) {
    return RxJava2Adapter.monoToCompletable(sendChallenge_migrated(context));
}
default Mono<Void> sendChallenge_migrated(FactorContext context) {
    return RxJava2Adapter.completableToMono(sendChallenge(context));
}

    default boolean useVariableFactorSecurity() {
        return false;
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.changeVariableFactorSecurity_migrated(factor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<EnrolledFactor> changeVariableFactorSecurity(EnrolledFactor factor) {
    return RxJava2Adapter.monoToSingle(changeVariableFactorSecurity_migrated(factor));
}default Mono<EnrolledFactor> changeVariableFactorSecurity_migrated(EnrolledFactor factor) {
        return Mono.just(factor);
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.generateQrCode_migrated(user, enrolledFactor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<String> generateQrCode(User user, EnrolledFactor enrolledFactor) {
    return RxJava2Adapter.monoToMaybe(generateQrCode_migrated(user, enrolledFactor));
}default Mono<String> generateQrCode_migrated(User user, EnrolledFactor enrolledFactor) {
        return Mono.empty();
    }
}

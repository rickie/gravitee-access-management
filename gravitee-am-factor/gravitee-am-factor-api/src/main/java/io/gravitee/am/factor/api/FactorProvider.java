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


import io.gravitee.am.factor.api.Enrollment;
import io.gravitee.am.factor.api.FactorContext;
import io.gravitee.am.model.User;
import io.gravitee.am.model.factor.EnrolledFactor;




import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface FactorProvider {

      
Mono<Void> verify_migrated(FactorContext context);

      
Mono<Enrollment> enroll_migrated(String account);

    boolean checkSecurityFactor(EnrolledFactor securityFactor);

    boolean needChallengeSending();

      
Mono<Void> sendChallenge_migrated(FactorContext context);

    default boolean useVariableFactorSecurity() {
        return false;
    }

      default Mono<EnrolledFactor> changeVariableFactorSecurity_migrated(EnrolledFactor factor) {
        return Mono.just(factor);
    }

      default Mono<String> generateQrCode_migrated(User user, EnrolledFactor enrolledFactor) {
        return Mono.empty();
    }
}

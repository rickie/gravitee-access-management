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
package io.gravitee.am.gateway.handler.users.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.oauth2.ScopeApproval;

import io.reactivex.Maybe;

import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<io.gravitee.am.model.User> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<io.gravitee.am.model.User> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Mono<Set<ScopeApproval>> consents_migrated(String userId);

      
Mono<Set<ScopeApproval>> consents_migrated(String userId, String clientId);

      
Mono<ScopeApproval> consent_migrated(String consentId);

      
Mono<Void> revokeConsent_migrated(String userId, String consentId, io.gravitee.am.identityprovider.api.User principal);

      
Mono<Void> revokeConsents_migrated(String userId, io.gravitee.am.identityprovider.api.User principal);

      
Mono<Void> revokeConsents_migrated(String userId, String clientId, io.gravitee.am.identityprovider.api.User principal);

      Mono<Void> revokeConsent_migrated(String userId, String consentId);

      Mono<Void> revokeConsents_migrated(String userId);

      Mono<Void> revokeConsents_migrated(String userId, String clientId);
}

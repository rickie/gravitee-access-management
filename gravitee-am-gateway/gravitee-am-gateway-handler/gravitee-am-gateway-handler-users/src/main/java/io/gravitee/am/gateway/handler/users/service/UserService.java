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
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
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

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.consents_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Set<ScopeApproval>> consents(String userId) {
    return RxJava2Adapter.monoToSingle(consents_migrated(userId));
}
default Mono<Set<ScopeApproval>> consents_migrated(String userId) {
    return RxJava2Adapter.singleToMono(consents(userId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.consents_migrated(userId, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Set<ScopeApproval>> consents(String userId, String clientId) {
    return RxJava2Adapter.monoToSingle(consents_migrated(userId, clientId));
}
default Mono<Set<ScopeApproval>> consents_migrated(String userId, String clientId) {
    return RxJava2Adapter.singleToMono(consents(userId, clientId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.consent_migrated(consentId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<ScopeApproval> consent(String consentId) {
    return RxJava2Adapter.monoToMaybe(consent_migrated(consentId));
}
default Mono<ScopeApproval> consent_migrated(String consentId) {
    return RxJava2Adapter.maybeToMono(consent(consentId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeConsent_migrated(userId, consentId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable revokeConsent(String userId, String consentId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(revokeConsent_migrated(userId, consentId, principal));
}
default Mono<Void> revokeConsent_migrated(String userId, String consentId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(revokeConsent(userId, consentId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeConsents_migrated(userId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable revokeConsents(String userId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(revokeConsents_migrated(userId, principal));
}
default Mono<Void> revokeConsents_migrated(String userId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(revokeConsents(userId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeConsents_migrated(userId, clientId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable revokeConsents(String userId, String clientId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(revokeConsents_migrated(userId, clientId, principal));
}
default Mono<Void> revokeConsents_migrated(String userId, String clientId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(revokeConsents(userId, clientId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeConsent_migrated(userId, consentId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable revokeConsent(String userId, String consentId) {
    return RxJava2Adapter.monoToCompletable(revokeConsent_migrated(userId, consentId));
}default Mono<Void> revokeConsent_migrated(String userId, String consentId) {
        return RxJava2Adapter.completableToMono(revokeConsent(userId, consentId, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeConsents_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable revokeConsents(String userId) {
    return RxJava2Adapter.monoToCompletable(revokeConsents_migrated(userId));
}default Mono<Void> revokeConsents_migrated(String userId) {
        return RxJava2Adapter.completableToMono(revokeConsents(userId, (io.gravitee.am.identityprovider.api.User) null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeConsents_migrated(userId, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable revokeConsents(String userId, String clientId) {
    return RxJava2Adapter.monoToCompletable(revokeConsents_migrated(userId, clientId));
}default Mono<Void> revokeConsents_migrated(String userId, String clientId) {
        return RxJava2Adapter.completableToMono(revokeConsents(userId, clientId, null));
    }
}

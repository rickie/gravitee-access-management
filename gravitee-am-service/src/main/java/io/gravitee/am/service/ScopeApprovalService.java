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
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Completable;

import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ScopeApprovalService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<ScopeApproval> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<ScopeApproval> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Flux<ScopeApproval> findByDomainAndUser_migrated(String domain, String user);

      
Flux<ScopeApproval> findByDomainAndUserAndClient_migrated(String domain, String user, String client);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.saveConsent_migrated(domain, client, approvals, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<ScopeApproval>> saveConsent(String domain, Client client, List<ScopeApproval> approvals, User principal) {
    return RxJava2Adapter.monoToSingle(saveConsent_migrated(domain, client, approvals, principal));
}
default Mono<List<ScopeApproval>> saveConsent_migrated(String domain, Client client, List<ScopeApproval> approvals, User principal) {
    return RxJava2Adapter.singleToMono(saveConsent(domain, client, approvals, principal));
}

      
Mono<Void> revokeByConsent_migrated(String domain, String userId, String consentId, User principal);

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByUser_migrated(domain, user, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable revokeByUser(String domain, String user, User principal) {
    return RxJava2Adapter.monoToCompletable(revokeByUser_migrated(domain, user, principal));
}
default Mono<Void> revokeByUser_migrated(String domain, String user, User principal) {
    return RxJava2Adapter.completableToMono(revokeByUser(domain, user, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByUserAndClient_migrated(domain, user, clientId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable revokeByUserAndClient(String domain, String user, String clientId, User principal) {
    return RxJava2Adapter.monoToCompletable(revokeByUserAndClient_migrated(domain, user, clientId, principal));
}
default Mono<Void> revokeByUserAndClient_migrated(String domain, String user, String clientId, User principal) {
    return RxJava2Adapter.completableToMono(revokeByUserAndClient(domain, user, clientId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.saveConsent_migrated(domain, client, approvals))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<ScopeApproval>> saveConsent(String domain, Client client, List<ScopeApproval> approvals) {
    return RxJava2Adapter.monoToSingle(saveConsent_migrated(domain, client, approvals));
}default Mono<List<ScopeApproval>> saveConsent_migrated(String domain, Client client, List<ScopeApproval> approvals) {
        return RxJava2Adapter.singleToMono(saveConsent(domain, client, approvals, null));
    }

      Mono<Void> revokeByConsent_migrated(String domain, String userId, String consentId);

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByUser_migrated(domain, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable revokeByUser(String domain, String userId) {
    return RxJava2Adapter.monoToCompletable(revokeByUser_migrated(domain, userId));
}default Mono<Void> revokeByUser_migrated(String domain, String userId) {
        return RxJava2Adapter.completableToMono(revokeByUser(domain, userId, null));
    }

      default Mono<Void> revokeByUserAndClient_migrated(String domain, String userId, String clientId) {
        return RxJava2Adapter.completableToMono(revokeByUserAndClient(domain, userId, clientId, null));
    }
}

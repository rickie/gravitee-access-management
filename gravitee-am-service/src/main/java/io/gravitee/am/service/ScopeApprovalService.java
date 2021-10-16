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




import java.util.List;

import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ScopeApprovalService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.oauth2.ScopeApproval> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.ScopeApproval> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndUser_migrated(domain, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.oauth2.ScopeApproval> findByDomainAndUser(java.lang.String domain, java.lang.String user) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndUser_migrated(domain, user));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.oauth2.ScopeApproval> findByDomainAndUser_migrated(String domain, String user) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndUser(domain, user));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndUserAndClient_migrated(domain, user, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.oauth2.ScopeApproval> findByDomainAndUserAndClient(java.lang.String domain, java.lang.String user, java.lang.String client) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndUserAndClient_migrated(domain, user, client));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.oauth2.ScopeApproval> findByDomainAndUserAndClient_migrated(String domain, String user, String client) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndUserAndClient(domain, user, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.saveConsent_migrated(domain, client, approvals, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.oauth2.ScopeApproval>> saveConsent(java.lang.String domain, io.gravitee.am.model.oidc.Client client, java.util.List<io.gravitee.am.model.oauth2.ScopeApproval> approvals, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(saveConsent_migrated(domain, client, approvals, principal));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.oauth2.ScopeApproval>> saveConsent_migrated(String domain, Client client, List<ScopeApproval> approvals, User principal) {
    return RxJava2Adapter.singleToMono(saveConsent(domain, client, approvals, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByConsent_migrated(domain, userId, consentId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable revokeByConsent(java.lang.String domain, java.lang.String userId, java.lang.String consentId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(revokeByConsent_migrated(domain, userId, consentId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> revokeByConsent_migrated(String domain, String userId, String consentId, User principal) {
    return RxJava2Adapter.completableToMono(revokeByConsent(domain, userId, consentId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByUser_migrated(domain, user, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable revokeByUser(java.lang.String domain, java.lang.String user, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(revokeByUser_migrated(domain, user, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> revokeByUser_migrated(String domain, String user, User principal) {
    return RxJava2Adapter.completableToMono(revokeByUser(domain, user, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByUserAndClient_migrated(domain, user, clientId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable revokeByUserAndClient(java.lang.String domain, java.lang.String user, java.lang.String clientId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(revokeByUserAndClient_migrated(domain, user, clientId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> revokeByUserAndClient_migrated(String domain, String user, String clientId, User principal) {
    return RxJava2Adapter.completableToMono(revokeByUserAndClient(domain, user, clientId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.saveConsent_migrated(domain, client, approvals))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.oauth2.ScopeApproval>> saveConsent(java.lang.String domain, io.gravitee.am.model.oidc.Client client, java.util.List<io.gravitee.am.model.oauth2.ScopeApproval> approvals) {
    return RxJava2Adapter.monoToSingle(saveConsent_migrated(domain, client, approvals));
}default Mono<List<ScopeApproval>> saveConsent_migrated(String domain, Client client, List<ScopeApproval> approvals) {
        return RxJava2Adapter.singleToMono(saveConsent(domain, client, approvals, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByConsent_migrated(domain, userId, consentId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable revokeByConsent(java.lang.String domain, java.lang.String userId, java.lang.String consentId) {
    return RxJava2Adapter.monoToCompletable(revokeByConsent_migrated(domain, userId, consentId));
}default Mono<Void> revokeByConsent_migrated(String domain, String userId, String consentId) {
        return RxJava2Adapter.completableToMono(revokeByConsent(domain, userId, consentId, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByUser_migrated(domain, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable revokeByUser(java.lang.String domain, java.lang.String userId) {
    return RxJava2Adapter.monoToCompletable(revokeByUser_migrated(domain, userId));
}default Mono<Void> revokeByUser_migrated(String domain, String userId) {
        return RxJava2Adapter.completableToMono(revokeByUser(domain, userId, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByUserAndClient_migrated(domain, userId, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable revokeByUserAndClient(java.lang.String domain, java.lang.String userId, java.lang.String clientId) {
    return RxJava2Adapter.monoToCompletable(revokeByUserAndClient_migrated(domain, userId, clientId));
}default Mono<Void> revokeByUserAndClient_migrated(String domain, String userId, String clientId) {
        return RxJava2Adapter.completableToMono(revokeByUserAndClient(domain, userId, clientId, null));
    }
}

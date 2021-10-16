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
package io.gravitee.am.repository.oauth2.api;

import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.repository.common.CrudRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ScopeApprovalRepository extends CrudRepository<ScopeApproval, String> {

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.oauth2.ScopeApproval> findByDomainAndUserAndClient(java.lang.String domain, java.lang.String userId, java.lang.String clientId) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndUserAndClient_migrated(domain, userId, clientId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.oauth2.ScopeApproval> findByDomainAndUserAndClient_migrated(String domain, String userId, String clientId) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndUserAndClient(domain, userId, clientId));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.oauth2.ScopeApproval> findByDomainAndUser(java.lang.String domain, java.lang.String user) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndUser_migrated(domain, user));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.oauth2.ScopeApproval> findByDomainAndUser_migrated(String domain, String user) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndUser(domain, user));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oauth2.ScopeApproval> upsert(io.gravitee.am.model.oauth2.ScopeApproval scopeApproval) {
    return RxJava2Adapter.monoToSingle(upsert_migrated(scopeApproval));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.ScopeApproval> upsert_migrated(ScopeApproval scopeApproval) {
    return RxJava2Adapter.singleToMono(upsert(scopeApproval));
}

      @Deprecated  
default io.reactivex.Completable deleteByDomainAndScopeKey(java.lang.String domain, java.lang.String scope) {
    return RxJava2Adapter.monoToCompletable(deleteByDomainAndScopeKey_migrated(domain, scope));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteByDomainAndScopeKey_migrated(String domain, String scope) {
    return RxJava2Adapter.completableToMono(deleteByDomainAndScopeKey(domain, scope));
}

      @Deprecated  
default io.reactivex.Completable deleteByDomainAndUserAndClient(java.lang.String domain, java.lang.String user, java.lang.String client) {
    return RxJava2Adapter.monoToCompletable(deleteByDomainAndUserAndClient_migrated(domain, user, client));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteByDomainAndUserAndClient_migrated(String domain, String user, String client) {
    return RxJava2Adapter.completableToMono(deleteByDomainAndUserAndClient(domain, user, client));
}

      @Deprecated  
default io.reactivex.Completable deleteByDomainAndUser(java.lang.String domain, java.lang.String user) {
    return RxJava2Adapter.monoToCompletable(deleteByDomainAndUser_migrated(domain, user));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteByDomainAndUser_migrated(String domain, String user) {
    return RxJava2Adapter.completableToMono(deleteByDomainAndUser(domain, user));
}

      @Deprecated  
default io.reactivex.Completable purgeExpiredData() {
    return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}default Mono<Void> purgeExpiredData_migrated() {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty()));
    }

}

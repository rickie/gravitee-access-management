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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.repository.common.CrudRepository;
import io.reactivex.Completable;


import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ScopeApprovalRepository extends CrudRepository<ScopeApproval, String> {

      
Flux<ScopeApproval> findByDomainAndUserAndClient_migrated(String domain, String userId, String clientId);

      
Flux<ScopeApproval> findByDomainAndUser_migrated(String domain, String user);

      
Mono<ScopeApproval> upsert_migrated(ScopeApproval scopeApproval);

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainAndScopeKey_migrated(domain, scope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable deleteByDomainAndScopeKey(String domain, String scope) {
    return RxJava2Adapter.monoToCompletable(deleteByDomainAndScopeKey_migrated(domain, scope));
}
default Mono<Void> deleteByDomainAndScopeKey_migrated(String domain, String scope) {
    return RxJava2Adapter.completableToMono(deleteByDomainAndScopeKey(domain, scope));
}

      
Mono<Void> deleteByDomainAndUserAndClient_migrated(String domain, String user, String client);

      
Mono<Void> deleteByDomainAndUser_migrated(String domain, String user);

      default Mono<Void> purgeExpiredData_migrated() {
        return Mono.empty();
    }

}

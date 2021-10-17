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
package io.gravitee.am.repository.jdbc.oauth2.api.spring;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcScopeApproval;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringScopeApprovalRepository extends RxJava2CrudRepository<JdbcScopeApproval, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndUserAndClient_migrated(domain, user, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcScopeApproval> findByDomainAndUserAndClient(@Param(value = "domain")
String domain, @Param(value = "user")
String user, @Param(value = "client")
String client) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndUserAndClient_migrated(domain, user, client));
}
default Flux<JdbcScopeApproval> findByDomainAndUserAndClient_migrated(@Param(value = "domain")
String domain, @Param(value = "user")
String user, @Param(value = "client")
String client) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndUserAndClient(domain, user, client));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndUser_migrated(domain, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcScopeApproval> findByDomainAndUser(@Param(value = "domain")
String domain, @Param(value = "user")
String user) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndUser_migrated(domain, user));
}
default Flux<JdbcScopeApproval> findByDomainAndUser_migrated(@Param(value = "domain")
String domain, @Param(value = "user")
String user) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndUser(domain, user));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndUserAndClientAndScope_migrated(domain, user, client, scope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcScopeApproval> findByDomainAndUserAndClientAndScope(@Param(value = "domain")
String domain, @Param(value = "user")
String user, @Param(value = "client")
String client, @Param(value = "scope")
String scope) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndUserAndClientAndScope_migrated(domain, user, client, scope));
}
default Mono<JdbcScopeApproval> findByDomainAndUserAndClientAndScope_migrated(@Param(value = "domain")
String domain, @Param(value = "user")
String user, @Param(value = "client")
String client, @Param(value = "scope")
String scope) {
    return RxJava2Adapter.maybeToMono(findByDomainAndUserAndClientAndScope(domain, user, client, scope));
}


}

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

import io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcScopeApproval;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringScopeApprovalRepository extends RxJava2CrudRepository<JdbcScopeApproval, String> {

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcScopeApproval> findByDomainAndUserAndClient(@org.springframework.data.repository.query.Param(value = "domain")
java.lang.String domain, @org.springframework.data.repository.query.Param(value = "user")
java.lang.String user, @org.springframework.data.repository.query.Param(value = "client")
java.lang.String client) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndUserAndClient_migrated(domain, user, client));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcScopeApproval> findByDomainAndUserAndClient_migrated(@Param(value = "domain")
String domain, @Param(value = "user")
String user, @Param(value = "client")
String client) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndUserAndClient(domain, user, client));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcScopeApproval> findByDomainAndUser(@org.springframework.data.repository.query.Param(value = "domain")
java.lang.String domain, @org.springframework.data.repository.query.Param(value = "user")
java.lang.String user) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndUser_migrated(domain, user));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcScopeApproval> findByDomainAndUser_migrated(@Param(value = "domain")
String domain, @Param(value = "user")
String user) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndUser(domain, user));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcScopeApproval> findByDomainAndUserAndClientAndScope(@org.springframework.data.repository.query.Param(value = "domain")
java.lang.String domain, @org.springframework.data.repository.query.Param(value = "user")
java.lang.String user, @org.springframework.data.repository.query.Param(value = "client")
java.lang.String client, @org.springframework.data.repository.query.Param(value = "scope")
java.lang.String scope) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndUserAndClientAndScope_migrated(domain, user, client, scope));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcScopeApproval> findByDomainAndUserAndClientAndScope_migrated(@Param(value = "domain")
String domain, @Param(value = "user")
String user, @Param(value = "client")
String client, @Param(value = "scope")
String scope) {
    return RxJava2Adapter.maybeToMono(findByDomainAndUserAndClientAndScope(domain, user, client, scope));
}


}

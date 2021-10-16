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
package io.gravitee.am.repository.jdbc.management.api.spring.resources;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcResource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
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
public interface SpringResourceRepository extends RxJava2CrudRepository<JdbcResource, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countByDomain(@org.springframework.data.repository.query.Param(value = "domain")
java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(countByDomain_migrated(domain));
}
default reactor.core.publisher.Mono<java.lang.Long> countByDomain_migrated(@Param(value = "domain")
String domain) {
    return RxJava2Adapter.singleToMono(countByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcResource> findByIdIn(@org.springframework.data.repository.query.Param(value = "ids")
java.util.List<java.lang.String> resources) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(resources));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcResource> findByIdIn_migrated(@Param(value = "ids")
List<String> resources) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(resources));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndUser_migrated(domain, client, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcResource> findByDomainAndClientAndUser(@org.springframework.data.repository.query.Param(value = "domain")
java.lang.String domain, @org.springframework.data.repository.query.Param(value = "client")
java.lang.String client, @org.springframework.data.repository.query.Param(value = "uid")
java.lang.String user) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndUser_migrated(domain, client, user));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcResource> findByDomainAndClientAndUser_migrated(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "uid")
String user) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClientAndUser(domain, client, user));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndResources_migrated(domain, client, resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcResource> findByDomainAndClientAndResources(@org.springframework.data.repository.query.Param(value = "domain")
java.lang.String domain, @org.springframework.data.repository.query.Param(value = "client")
java.lang.String client, @org.springframework.data.repository.query.Param(value = "ids")
java.util.List<java.lang.String> resources) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndResources_migrated(domain, client, resources));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcResource> findByDomainAndClientAndResources_migrated(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "ids")
List<String> resources) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClientAndResources(domain, client, resources));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndUserIdAndResource_migrated(domain, client, user, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcResource> findByDomainAndClientAndUserIdAndResource(@org.springframework.data.repository.query.Param(value = "domain")
java.lang.String domain, @org.springframework.data.repository.query.Param(value = "client")
java.lang.String client, @org.springframework.data.repository.query.Param(value = "uid")
java.lang.String user, @org.springframework.data.repository.query.Param(value = "rid")
java.lang.String resource) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndUserIdAndResource_migrated(domain, client, user, resource));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcResource> findByDomainAndClientAndUserIdAndResource_migrated(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "uid")
String user, @Param(value = "rid")
String resource) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserIdAndResource(domain, client, user, resource));
}
}

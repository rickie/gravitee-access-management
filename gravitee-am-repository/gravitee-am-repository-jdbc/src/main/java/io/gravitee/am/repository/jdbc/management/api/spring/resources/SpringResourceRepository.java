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
public interface SpringResourceRepository extends RxJava2CrudRepository<JdbcResource, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countByDomain(@Param(value = "domain")
String domain) {
    return RxJava2Adapter.monoToSingle(countByDomain_migrated(domain));
}
default Mono<Long> countByDomain_migrated(@Param(value = "domain")
String domain) {
    return RxJava2Adapter.singleToMono(countByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcResource> findByIdIn(@Param(value = "ids")
List<String> resources) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(resources));
}
default Flux<JdbcResource> findByIdIn_migrated(@Param(value = "ids")
List<String> resources) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(resources));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndUser_migrated(domain, client, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcResource> findByDomainAndClientAndUser(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "uid")
String user) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndUser_migrated(domain, client, user));
}
default Flux<JdbcResource> findByDomainAndClientAndUser_migrated(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "uid")
String user) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClientAndUser(domain, client, user));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndResources_migrated(domain, client, resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcResource> findByDomainAndClientAndResources(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "ids")
List<String> resources) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndResources_migrated(domain, client, resources));
}
default Flux<JdbcResource> findByDomainAndClientAndResources_migrated(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "ids")
List<String> resources) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClientAndResources(domain, client, resources));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndUserIdAndResource_migrated(domain, client, user, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcResource> findByDomainAndClientAndUserIdAndResource(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "uid")
String user, @Param(value = "rid")
String resource) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndUserIdAndResource_migrated(domain, client, user, resource));
}
default Mono<JdbcResource> findByDomainAndClientAndUserIdAndResource_migrated(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "uid")
String user, @Param(value = "rid")
String resource) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientAndUserIdAndResource(domain, client, user, resource));
}
}

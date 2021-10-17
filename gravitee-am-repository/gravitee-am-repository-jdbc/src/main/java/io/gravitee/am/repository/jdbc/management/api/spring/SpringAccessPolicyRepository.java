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
package io.gravitee.am.repository.jdbc.management.api.spring;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcAccessPolicy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.List;
import org.springframework.data.domain.Pageable;
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
public interface SpringAccessPolicyRepository extends RxJava2CrudRepository<JdbcAccessPolicy, String> {
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countByDomain(String domain) {
    return RxJava2Adapter.monoToSingle(countByDomain_migrated(domain));
}
default Mono<Long> countByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(countByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain, page))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcAccessPolicy> findByDomain(@Param(value = "domain")
String domain, Pageable page) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain, page));
}
default Flux<JdbcAccessPolicy> findByDomain_migrated(@Param(value = "domain")
String domain, Pageable page) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain, page));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByResource_migrated(resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countByResource(String resource) {
    return RxJava2Adapter.monoToSingle(countByResource_migrated(resource));
}
default Mono<Long> countByResource_migrated(String resource) {
    return RxJava2Adapter.singleToMono(countByResource(resource));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndResource_migrated(domain, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcAccessPolicy> findByDomainAndResource(@Param(value = "domain")
String domain, @Param(value = "resource")
String resource) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndResource_migrated(domain, resource));
}
default Flux<JdbcAccessPolicy> findByDomainAndResource_migrated(@Param(value = "domain")
String domain, @Param(value = "resource")
String resource) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndResource(domain, resource));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByResourceIn_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcAccessPolicy> findByResourceIn(@Param(value = "resources")
List<String> resources) {
    return RxJava2Adapter.fluxToFlowable(findByResourceIn_migrated(resources));
}
default Flux<JdbcAccessPolicy> findByResourceIn_migrated(@Param(value = "resources")
List<String> resources) {
    return RxJava2Adapter.flowableToFlux(findByResourceIn(resources));
}
}

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
package io.gravitee.am.repository.jdbc.management.api.spring.application;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication;


import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringApplicationRepository extends RxJava2CrudRepository<JdbcApplication, String> {
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

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findByDomain(@org.springframework.data.repository.query.Param(value = "domain")
java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findByDomain_migrated(@Param(value = "domain")
String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCertificate_migrated(certificate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findByCertificate(@org.springframework.data.repository.query.Param(value = "cert")
java.lang.String certificate) {
    return RxJava2Adapter.fluxToFlowable(findByCertificate_migrated(certificate));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findByCertificate_migrated(@Param(value = "cert")
String certificate) {
    return RxJava2Adapter.flowableToFlux(findByCertificate(certificate));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByFactor_migrated(factor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findAllByFactor(@org.springframework.data.repository.query.Param(value = "factor")
java.lang.String factor) {
    return RxJava2Adapter.fluxToFlowable(findAllByFactor_migrated(factor));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findAllByFactor_migrated(@Param(value = "factor")
String factor) {
    return RxJava2Adapter.flowableToFlux(findAllByFactor(factor));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByDomainAndGrant_migrated(domain, grant))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findAllByDomainAndGrant(@org.springframework.data.repository.query.Param(value = "domain")
java.lang.String domain, @org.springframework.data.repository.query.Param(value = "grant")
java.lang.String grant) {
    return RxJava2Adapter.fluxToFlowable(findAllByDomainAndGrant_migrated(domain, grant));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findAllByDomainAndGrant_migrated(@Param(value = "domain")
String domain, @Param(value = "grant")
String grant) {
    return RxJava2Adapter.flowableToFlux(findAllByDomainAndGrant(domain, grant));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findByIdIn(@org.springframework.data.repository.query.Param(value = "ids")
java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication> findByIdIn_migrated(@Param(value = "ids")
List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}


}

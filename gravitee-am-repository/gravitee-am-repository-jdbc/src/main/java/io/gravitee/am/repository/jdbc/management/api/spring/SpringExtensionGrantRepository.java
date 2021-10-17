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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcExtensionGrant;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface SpringExtensionGrantRepository extends RxJava2CrudRepository<JdbcExtensionGrant, String> {
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcExtensionGrant> findByDomain(@Param(value = "domain")
String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default Flux<JdbcExtensionGrant> findByDomain_migrated(@Param(value = "domain")
String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndName_migrated(domain, name))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcExtensionGrant> findByDomainAndName(@Param(value = "domain")
String domain, @Param(value = "name")
String name) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndName_migrated(domain, name));
}
default Mono<JdbcExtensionGrant> findByDomainAndName_migrated(@Param(value = "domain")
String domain, @Param(value = "name")
String name) {
    return RxJava2Adapter.maybeToMono(findByDomainAndName(domain, name));
}
}

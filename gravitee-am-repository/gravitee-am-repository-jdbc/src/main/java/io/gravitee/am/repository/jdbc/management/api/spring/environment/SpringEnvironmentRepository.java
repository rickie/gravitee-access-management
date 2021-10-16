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
package io.gravitee.am.repository.jdbc.management.api.spring.environment;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcEnvironment;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringEnvironmentRepository extends RxJava2CrudRepository<JdbcEnvironment, String> {
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByIdAndOrganization_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcEnvironment> findByIdAndOrganization(java.lang.String id, java.lang.String organizationId) {
    return RxJava2Adapter.monoToMaybe(findByIdAndOrganization_migrated(id, organizationId));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcEnvironment> findByIdAndOrganization_migrated(String id, String organizationId) {
    return RxJava2Adapter.maybeToMono(findByIdAndOrganization(id, organizationId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByOrganization_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcEnvironment> findByOrganization(java.lang.String organizationId) {
    return RxJava2Adapter.fluxToFlowable(findByOrganization_migrated(organizationId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcEnvironment> findByOrganization_migrated(String organizationId) {
    return RxJava2Adapter.flowableToFlux(findByOrganization(organizationId));
}
}

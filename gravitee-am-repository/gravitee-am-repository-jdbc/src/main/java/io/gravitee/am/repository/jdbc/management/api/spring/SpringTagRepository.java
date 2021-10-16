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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcTag;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface SpringTagRepository extends RxJava2CrudRepository<JdbcTag, String> {
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByOrganization_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcTag> findByOrganization(@org.springframework.data.repository.query.Param(value = "orgId")
java.lang.String organizationId) {
    return RxJava2Adapter.fluxToFlowable(findByOrganization_migrated(organizationId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcTag> findByOrganization_migrated(@Param(value = "orgId")
String organizationId) {
    return RxJava2Adapter.flowableToFlux(findByOrganization(organizationId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcTag> findById(@org.springframework.data.repository.query.Param(value = "tid")
java.lang.String id, @org.springframework.data.repository.query.Param(value = "orgId")
java.lang.String organizationId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id, organizationId));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcTag> findById_migrated(@Param(value = "tid")
String id, @Param(value = "orgId")
String organizationId) {
    return RxJava2Adapter.maybeToMono(findById(id, organizationId));
}
}

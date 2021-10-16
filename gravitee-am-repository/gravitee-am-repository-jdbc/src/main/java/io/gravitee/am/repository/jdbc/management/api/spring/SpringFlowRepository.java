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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcFlow;
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
public interface SpringFlowRepository extends RxJava2CrudRepository<JdbcFlow, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcFlow> findById(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "id")
java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcFlow> findById_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId, @Param(value = "id")
String id) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcFlow> findAll(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcFlow> findAll_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByApplication_migrated(referenceType, referenceId, appId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcFlow> findByApplication(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "appId")
java.lang.String appId) {
    return RxJava2Adapter.fluxToFlowable(findByApplication_migrated(referenceType, referenceId, appId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcFlow> findByApplication_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId, @Param(value = "appId")
String appId) {
    return RxJava2Adapter.flowableToFlux(findByApplication(referenceType, referenceId, appId));
}
}

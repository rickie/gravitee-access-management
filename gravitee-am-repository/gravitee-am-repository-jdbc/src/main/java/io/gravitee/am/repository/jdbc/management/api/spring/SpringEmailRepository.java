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

import io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail;
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
public interface SpringEmailRepository extends RxJava2CrudRepository<JdbcEmail, String> {
      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findById(@org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "id")
java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(refId, refType, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findById_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "id")
String id) {
    return RxJava2Adapter.maybeToMono(findById(refId, refType, id));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findAllByReference(@org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType) {
    return RxJava2Adapter.fluxToFlowable(findAllByReference_migrated(refId, refType));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findAllByReference_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType) {
    return RxJava2Adapter.flowableToFlux(findAllByReference(refId, refType));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findAllByReferenceAndClient(@org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "cli")
java.lang.String client) {
    return RxJava2Adapter.fluxToFlowable(findAllByReferenceAndClient_migrated(refId, refType, client));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findAllByReferenceAndClient_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "cli")
String client) {
    return RxJava2Adapter.flowableToFlux(findAllByReferenceAndClient(refId, refType, client));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findByClientAndTemplate(@org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "cli")
java.lang.String client, @org.springframework.data.repository.query.Param(value = "tpl")
java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(refId, refType, client, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findByClientAndTemplate_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "cli")
String client, @Param(value = "tpl")
String template) {
    return RxJava2Adapter.maybeToMono(findByClientAndTemplate(refId, refType, client, template));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findByTemplate(@org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "tpl")
java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(refId, refType, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail> findByTemplate_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "tpl")
String template) {
    return RxJava2Adapter.maybeToMono(findByTemplate(refId, refType, template));
}
}

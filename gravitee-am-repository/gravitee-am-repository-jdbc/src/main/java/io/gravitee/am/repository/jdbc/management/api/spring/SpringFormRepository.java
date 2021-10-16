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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcForm;



import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringFormRepository extends RxJava2CrudRepository<JdbcForm, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findAll(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findAll_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findAll(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findAll_migrated(@Param(value = "refType")
String referenceType) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClient_migrated(referenceType, referenceId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findByClient(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "cli")
java.lang.String client) {
    return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findByClient_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId, @Param(value = "cli")
String client) {
    return RxJava2Adapter.flowableToFlux(findByClient(referenceType, referenceId, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(referenceType, referenceId, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findByTemplate(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "tpl")
java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findByTemplate_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId, @Param(value = "tpl")
String template) {
    return RxJava2Adapter.maybeToMono(findByTemplate(referenceType, referenceId, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findByClientAndTemplate(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "cli")
java.lang.String client, @org.springframework.data.repository.query.Param(value = "tpl")
java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findByClientAndTemplate_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId, @Param(value = "cli")
String client, @Param(value = "tpl")
String template) {
    return RxJava2Adapter.maybeToMono(findByClientAndTemplate(referenceType, referenceId, client, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findById(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "id")
java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcForm> findById_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId, @Param(value = "id")
String id) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, id));
}
}

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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
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
public interface SpringEmailRepository extends RxJava2CrudRepository<JdbcEmail, String> {
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(refId, refType, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcEmail> findById(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "id")
String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(refId, refType, id));
}
default Mono<JdbcEmail> findById_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "id")
String id) {
    return RxJava2Adapter.maybeToMono(findById(refId, refType, id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByReference_migrated(refId, refType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcEmail> findAllByReference(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType) {
    return RxJava2Adapter.fluxToFlowable(findAllByReference_migrated(refId, refType));
}
default Flux<JdbcEmail> findAllByReference_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType) {
    return RxJava2Adapter.flowableToFlux(findAllByReference(refId, refType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByReferenceAndClient_migrated(refId, refType, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcEmail> findAllByReferenceAndClient(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "cli")
String client) {
    return RxJava2Adapter.fluxToFlowable(findAllByReferenceAndClient_migrated(refId, refType, client));
}
default Flux<JdbcEmail> findAllByReferenceAndClient_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "cli")
String client) {
    return RxJava2Adapter.flowableToFlux(findAllByReferenceAndClient(refId, refType, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(refId, refType, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcEmail> findByClientAndTemplate(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "cli")
String client, @Param(value = "tpl")
String template) {
    return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(refId, refType, client, template));
}
default Mono<JdbcEmail> findByClientAndTemplate_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "cli")
String client, @Param(value = "tpl")
String template) {
    return RxJava2Adapter.maybeToMono(findByClientAndTemplate(refId, refType, client, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(refId, refType, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcEmail> findByTemplate(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "tpl")
String template) {
    return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(refId, refType, template));
}
default Mono<JdbcEmail> findByTemplate_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "tpl")
String template) {
    return RxJava2Adapter.maybeToMono(findByTemplate(refId, refType, template));
}
}

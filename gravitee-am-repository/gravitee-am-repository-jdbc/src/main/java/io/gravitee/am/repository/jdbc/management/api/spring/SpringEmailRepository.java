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

      
Flux<JdbcEmail> findAllByReference_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType);

      
Flux<JdbcEmail> findAllByReferenceAndClient_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "cli")
String client);

      
Mono<JdbcEmail> findByClientAndTemplate_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "cli")
String client, @Param(value = "tpl")
String template);

      
Mono<JdbcEmail> findByTemplate_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "tpl")
String template);
}

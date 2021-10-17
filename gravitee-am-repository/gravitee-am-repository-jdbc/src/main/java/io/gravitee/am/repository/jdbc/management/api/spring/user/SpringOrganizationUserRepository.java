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
package io.gravitee.am.repository.jdbc.management.api.spring.user;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser;

import io.reactivex.Maybe;

import java.util.List;
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
public interface SpringOrganizationUserRepository extends RxJava2CrudRepository<JdbcOrganizationUser, String> {

      
Mono<Long> countByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(refType, refId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcOrganizationUser> findById(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "id")
String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(refType, refId, id));
}
default Mono<JdbcOrganizationUser> findById_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "id")
String id) {
    return RxJava2Adapter.maybeToMono(findById(refType, refId, id));
}

      
Mono<JdbcOrganizationUser> findByExternalIdAndSource_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "id")
String externalId, @Param(value = "src")
String source);

      
Mono<JdbcOrganizationUser> findByUsernameAndSource_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "name")
String username, @Param(value = "src")
String source);

      
Flux<JdbcOrganizationUser> findByIdIn_migrated(@Param(value = "ids")
List<String> ids);

      
Flux<JdbcOrganizationUser> findByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId);
}

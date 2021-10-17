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
package io.gravitee.am.repository.jdbc.management.api.spring.role;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcRole;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
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
public interface SpringRoleRepository extends RxJava2CrudRepository<JdbcRole, String> {

      
Mono<Long> countByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId);

      
Flux<JdbcRole> findByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId);

      
Flux<JdbcRole> findByIdIn_migrated(@Param(value = "rid")
List<String> roles);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(refType, refId, role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcRole> findById(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "rid")
String role) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(refType, refId, role));
}
default Mono<JdbcRole> findById_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "rid")
String role) {
    return RxJava2Adapter.maybeToMono(findById(refType, refId, role));
}

      
Mono<JdbcRole> findByNameAndAssignableType_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "name")
String name, @Param(value = "assignable")
String assignableType);

      
Flux<JdbcRole> findByNamesAndAssignableType_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "names")
List<String> names, @Param(value = "assignable")
String assignableType);
}

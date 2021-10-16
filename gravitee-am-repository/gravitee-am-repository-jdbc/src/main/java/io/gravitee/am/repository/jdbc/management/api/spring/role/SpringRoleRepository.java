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
public interface SpringRoleRepository extends RxJava2CrudRepository<JdbcRole, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByReference_migrated(refType, refId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countByReference(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId) {
    return RxJava2Adapter.monoToSingle(countByReference_migrated(refType, refId));
}
default reactor.core.publisher.Mono<java.lang.Long> countByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId) {
    return RxJava2Adapter.singleToMono(countByReference(refType, refId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReference_migrated(refType, refId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findByReference(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId) {
    return RxJava2Adapter.fluxToFlowable(findByReference_migrated(refType, refId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId) {
    return RxJava2Adapter.flowableToFlux(findByReference(refType, refId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(roles))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findByIdIn(@org.springframework.data.repository.query.Param(value = "rid")
java.util.List<java.lang.String> roles) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(roles));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findByIdIn_migrated(@Param(value = "rid")
List<String> roles) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(roles));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(refType, refId, role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findById(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "rid")
java.lang.String role) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(refType, refId, role));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findById_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "rid")
String role) {
    return RxJava2Adapter.maybeToMono(findById(refType, refId, role));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByNameAndAssignableType_migrated(refType, refId, name, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findByNameAndAssignableType(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "name")
java.lang.String name, @org.springframework.data.repository.query.Param(value = "assignable")
java.lang.String assignableType) {
    return RxJava2Adapter.monoToMaybe(findByNameAndAssignableType_migrated(refType, refId, name, assignableType));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findByNameAndAssignableType_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "name")
String name, @Param(value = "assignable")
String assignableType) {
    return RxJava2Adapter.maybeToMono(findByNameAndAssignableType(refType, refId, name, assignableType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByNamesAndAssignableType_migrated(refType, refId, names, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findByNamesAndAssignableType(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "names")
java.util.List<java.lang.String> names, @org.springframework.data.repository.query.Param(value = "assignable")
java.lang.String assignableType) {
    return RxJava2Adapter.fluxToFlowable(findByNamesAndAssignableType_migrated(refType, refId, names, assignableType));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcRole> findByNamesAndAssignableType_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "names")
List<String> names, @Param(value = "assignable")
String assignableType) {
    return RxJava2Adapter.flowableToFlux(findByNamesAndAssignableType(refType, refId, names, assignableType));
}
}

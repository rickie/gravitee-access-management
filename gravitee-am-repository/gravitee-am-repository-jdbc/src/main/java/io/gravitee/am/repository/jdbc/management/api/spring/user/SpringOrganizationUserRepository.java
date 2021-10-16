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
public interface SpringOrganizationUserRepository extends RxJava2CrudRepository<JdbcOrganizationUser, String> {

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

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(refType, refId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findById(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "id")
java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(refType, refId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findById_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "id")
String id) {
    return RxJava2Adapter.maybeToMono(findById(refType, refId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByExternalIdAndSource_migrated(refType, refId, externalId, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findByExternalIdAndSource(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "id")
java.lang.String externalId, @org.springframework.data.repository.query.Param(value = "src")
java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByExternalIdAndSource_migrated(refType, refId, externalId, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findByExternalIdAndSource_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "id")
String externalId, @Param(value = "src")
String source) {
    return RxJava2Adapter.maybeToMono(findByExternalIdAndSource(refType, refId, externalId, source));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsernameAndSource_migrated(refType, refId, username, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findByUsernameAndSource(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "name")
java.lang.String username, @org.springframework.data.repository.query.Param(value = "src")
java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByUsernameAndSource_migrated(refType, refId, username, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findByUsernameAndSource_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "name")
String username, @Param(value = "src")
String source) {
    return RxJava2Adapter.maybeToMono(findByUsernameAndSource(refType, refId, username, source));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findByIdIn(@org.springframework.data.repository.query.Param(value = "ids")
java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findByIdIn_migrated(@Param(value = "ids")
List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReference_migrated(refType, refId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findByReference(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId) {
    return RxJava2Adapter.fluxToFlowable(findByReference_migrated(refType, refId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganizationUser> findByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId) {
    return RxJava2Adapter.flowableToFlux(findByReference(refType, refId));
}
}

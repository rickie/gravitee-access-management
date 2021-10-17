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

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByReference_migrated(refType, refId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countByReference(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId) {
    return RxJava2Adapter.monoToSingle(countByReference_migrated(refType, refId));
}
default Mono<Long> countByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId) {
    return RxJava2Adapter.singleToMono(countByReference(refType, refId));
}

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

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByExternalIdAndSource_migrated(refType, refId, externalId, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcOrganizationUser> findByExternalIdAndSource(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "id")
String externalId, @Param(value = "src")
String source) {
    return RxJava2Adapter.monoToMaybe(findByExternalIdAndSource_migrated(refType, refId, externalId, source));
}
default Mono<JdbcOrganizationUser> findByExternalIdAndSource_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "id")
String externalId, @Param(value = "src")
String source) {
    return RxJava2Adapter.maybeToMono(findByExternalIdAndSource(refType, refId, externalId, source));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsernameAndSource_migrated(refType, refId, username, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcOrganizationUser> findByUsernameAndSource(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "name")
String username, @Param(value = "src")
String source) {
    return RxJava2Adapter.monoToMaybe(findByUsernameAndSource_migrated(refType, refId, username, source));
}
default Mono<JdbcOrganizationUser> findByUsernameAndSource_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "name")
String username, @Param(value = "src")
String source) {
    return RxJava2Adapter.maybeToMono(findByUsernameAndSource(refType, refId, username, source));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcOrganizationUser> findByIdIn(@Param(value = "ids")
List<String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default Flux<JdbcOrganizationUser> findByIdIn_migrated(@Param(value = "ids")
List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReference_migrated(refType, refId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcOrganizationUser> findByReference(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId) {
    return RxJava2Adapter.fluxToFlowable(findByReference_migrated(refType, refId));
}
default Flux<JdbcOrganizationUser> findByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId) {
    return RxJava2Adapter.flowableToFlux(findByReference(refType, refId));
}
}

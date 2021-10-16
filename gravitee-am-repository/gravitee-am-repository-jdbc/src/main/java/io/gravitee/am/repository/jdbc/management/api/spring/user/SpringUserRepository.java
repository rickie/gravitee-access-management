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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcUser;



import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringUserRepository extends RxJava2CrudRepository<JdbcUser, String> {

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

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByClient_migrated(refType, refId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countByClient(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "client")
java.lang.String client) {
    return RxJava2Adapter.monoToSingle(countByClient_migrated(refType, refId, client));
}
default reactor.core.publisher.Mono<java.lang.Long> countByClient_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "client")
String client) {
    return RxJava2Adapter.singleToMono(countByClient(refType, refId, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(refType, refId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findById(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "id")
java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(refType, refId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findById_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "id")
String id) {
    return RxJava2Adapter.maybeToMono(findById(refType, refId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByExternalIdAndSource_migrated(refType, refId, externalId, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByExternalIdAndSource(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "id")
java.lang.String externalId, @org.springframework.data.repository.query.Param(value = "src")
java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByExternalIdAndSource_migrated(refType, refId, externalId, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByExternalIdAndSource_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "id")
String externalId, @Param(value = "src")
String source) {
    return RxJava2Adapter.maybeToMono(findByExternalIdAndSource(refType, refId, externalId, source));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsernameAndSource_migrated(refType, refId, username, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByUsernameAndSource(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "name")
java.lang.String username, @org.springframework.data.repository.query.Param(value = "src")
java.lang.String source) {
    return RxJava2Adapter.monoToMaybe(findByUsernameAndSource_migrated(refType, refId, username, source));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByUsernameAndSource_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "name")
String username, @Param(value = "src")
String source) {
    return RxJava2Adapter.maybeToMono(findByUsernameAndSource(refType, refId, username, source));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsername_migrated(refType, refId, username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByUsername(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "name")
java.lang.String username) {
    return RxJava2Adapter.monoToMaybe(findByUsername_migrated(refType, refId, username));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByUsername_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "name")
String username) {
    return RxJava2Adapter.maybeToMono(findByUsername(refType, refId, username));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByIdIn(@org.springframework.data.repository.query.Param(value = "ids")
java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByIdIn_migrated(@Param(value = "ids")
List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReference_migrated(refType, refId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByReference(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId) {
    return RxJava2Adapter.fluxToFlowable(findByReference_migrated(refType, refId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser> findByReference_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId) {
    return RxJava2Adapter.flowableToFlux(findByReference(refType, refId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countLockedUser_migrated(refType, refId, notLocked, lockedUntil))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countLockedUser(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "nl")
boolean notLocked, @org.springframework.data.repository.query.Param(value = "lockedUntil")
java.time.LocalDateTime lockedUntil) {
    return RxJava2Adapter.monoToSingle(countLockedUser_migrated(refType, refId, notLocked, lockedUntil));
}
default reactor.core.publisher.Mono<java.lang.Long> countLockedUser_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "nl")
boolean notLocked, @Param(value = "lockedUntil")
LocalDateTime lockedUntil) {
    return RxJava2Adapter.singleToMono(countLockedUser(refType, refId, notLocked, lockedUntil));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countLockedUserByClient_migrated(refType, refId, client, notLocked, lockedUntil))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countLockedUserByClient(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "client")
java.lang.String client, @org.springframework.data.repository.query.Param(value = "nl")
boolean notLocked, @org.springframework.data.repository.query.Param(value = "lockedUntil")
java.time.LocalDateTime lockedUntil) {
    return RxJava2Adapter.monoToSingle(countLockedUserByClient_migrated(refType, refId, client, notLocked, lockedUntil));
}
default reactor.core.publisher.Mono<java.lang.Long> countLockedUserByClient_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "client")
String client, @Param(value = "nl")
boolean notLocked, @Param(value = "lockedUntil")
LocalDateTime lockedUntil) {
    return RxJava2Adapter.singleToMono(countLockedUserByClient(refType, refId, client, notLocked, lockedUntil));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countDisabledUser_migrated(refType, refId, enabled))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countDisabledUser(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "en")
boolean enabled) {
    return RxJava2Adapter.monoToSingle(countDisabledUser_migrated(refType, refId, enabled));
}
default reactor.core.publisher.Mono<java.lang.Long> countDisabledUser_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "en")
boolean enabled) {
    return RxJava2Adapter.singleToMono(countDisabledUser(refType, refId, enabled));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countDisabledUserByClient_migrated(refType, refId, client, enabled))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countDisabledUserByClient(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "client")
java.lang.String client, @org.springframework.data.repository.query.Param(value = "en")
boolean enabled) {
    return RxJava2Adapter.monoToSingle(countDisabledUserByClient_migrated(refType, refId, client, enabled));
}
default reactor.core.publisher.Mono<java.lang.Long> countDisabledUserByClient_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "client")
String client, @Param(value = "en")
boolean enabled) {
    return RxJava2Adapter.singleToMono(countDisabledUserByClient(refType, refId, client, enabled));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countInactiveUser_migrated(refType, refId, threshold))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countInactiveUser(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "threshold")
java.time.LocalDateTime threshold) {
    return RxJava2Adapter.monoToSingle(countInactiveUser_migrated(refType, refId, threshold));
}
default reactor.core.publisher.Mono<java.lang.Long> countInactiveUser_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "threshold")
LocalDateTime threshold) {
    return RxJava2Adapter.singleToMono(countInactiveUser(refType, refId, threshold));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countInactiveUserByClient_migrated(refType, refId, client, threshold))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countInactiveUserByClient(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "client")
java.lang.String client, @org.springframework.data.repository.query.Param(value = "threshold")
java.time.LocalDateTime threshold) {
    return RxJava2Adapter.monoToSingle(countInactiveUserByClient_migrated(refType, refId, client, threshold));
}
default reactor.core.publisher.Mono<java.lang.Long> countInactiveUserByClient_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "client")
String client, @Param(value = "threshold")
LocalDateTime threshold) {
    return RxJava2Adapter.singleToMono(countInactiveUserByClient(refType, refId, client, threshold));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countPreRegisteredUser_migrated(refType, refId, preRegister))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countPreRegisteredUser(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "pre")
boolean preRegister) {
    return RxJava2Adapter.monoToSingle(countPreRegisteredUser_migrated(refType, refId, preRegister));
}
default reactor.core.publisher.Mono<java.lang.Long> countPreRegisteredUser_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "pre")
boolean preRegister) {
    return RxJava2Adapter.singleToMono(countPreRegisteredUser(refType, refId, preRegister));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countRegistrationCompletedUser_migrated(refType, refId, preRegister, completed))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countRegistrationCompletedUser(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String refType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String refId, @org.springframework.data.repository.query.Param(value = "pre")
boolean preRegister, @org.springframework.data.repository.query.Param(value = "compl")
boolean completed) {
    return RxJava2Adapter.monoToSingle(countRegistrationCompletedUser_migrated(refType, refId, preRegister, completed));
}
default reactor.core.publisher.Mono<java.lang.Long> countRegistrationCompletedUser_migrated(@Param(value = "refType")
String refType, @Param(value = "refId")
String refId, @Param(value = "pre")
boolean preRegister, @Param(value = "compl")
boolean completed) {
    return RxJava2Adapter.singleToMono(countRegistrationCompletedUser(refType, refId, preRegister, completed));
}
}

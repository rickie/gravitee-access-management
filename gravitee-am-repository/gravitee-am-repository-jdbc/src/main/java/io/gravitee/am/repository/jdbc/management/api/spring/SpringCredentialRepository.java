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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcCredential;
import io.reactivex.Flowable;
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
public interface SpringCredentialRepository extends RxJava2CrudRepository<JdbcCredential, String> {
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByUserId_migrated(referenceType, referenceId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcCredential> findByUserId(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "userId")
java.lang.String userId) {
    return RxJava2Adapter.fluxToFlowable(findByUserId_migrated(referenceType, referenceId, userId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcCredential> findByUserId_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId, @Param(value = "userId")
String userId) {
    return RxJava2Adapter.flowableToFlux(findByUserId(referenceType, referenceId, userId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByUsername_migrated(referenceType, referenceId, username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcCredential> findByUsername(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "username")
java.lang.String username) {
    return RxJava2Adapter.fluxToFlowable(findByUsername_migrated(referenceType, referenceId, username));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcCredential> findByUsername_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId, @Param(value = "username")
String username) {
    return RxJava2Adapter.flowableToFlux(findByUsername(referenceType, referenceId, username));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCredentialId_migrated(referenceType, referenceId, credentialId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcCredential> findByCredentialId(@org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "credId")
java.lang.String credentialId) {
    return RxJava2Adapter.fluxToFlowable(findByCredentialId_migrated(referenceType, referenceId, credentialId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcCredential> findByCredentialId_migrated(@Param(value = "refType")
String referenceType, @Param(value = "refId")
String referenceId, @Param(value = "credId")
String credentialId) {
    return RxJava2Adapter.flowableToFlux(findByCredentialId(referenceType, referenceId, credentialId));
}
}

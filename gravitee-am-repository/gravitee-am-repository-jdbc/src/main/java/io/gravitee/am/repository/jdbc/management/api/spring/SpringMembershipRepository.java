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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcMembership;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
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
public interface SpringMembershipRepository extends RxJava2CrudRepository<JdbcMembership, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReference_migrated(referenceId, referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcMembership> findByReference(@org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType) {
    return RxJava2Adapter.fluxToFlowable(findByReference_migrated(referenceId, referenceType));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcMembership> findByReference_migrated(@Param(value = "refId")
String referenceId, @Param(value = "refType")
String referenceType) {
    return RxJava2Adapter.flowableToFlux(findByReference(referenceId, referenceType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByMember_migrated(memberId, memberType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcMembership> findByMember(@org.springframework.data.repository.query.Param(value = "mid")
java.lang.String memberId, @org.springframework.data.repository.query.Param(value = "mtype")
java.lang.String memberType) {
    return RxJava2Adapter.fluxToFlowable(findByMember_migrated(memberId, memberType));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcMembership> findByMember_migrated(@Param(value = "mid")
String memberId, @Param(value = "mtype")
String memberType) {
    return RxJava2Adapter.flowableToFlux(findByMember(memberId, memberType));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByReferenceAndMember_migrated(referenceId, referenceType, memberId, memberType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.management.api.model.JdbcMembership> findByReferenceAndMember(@org.springframework.data.repository.query.Param(value = "refId")
java.lang.String referenceId, @org.springframework.data.repository.query.Param(value = "refType")
java.lang.String referenceType, @org.springframework.data.repository.query.Param(value = "mid")
java.lang.String memberId, @org.springframework.data.repository.query.Param(value = "mtype")
java.lang.String memberType) {
    return RxJava2Adapter.monoToMaybe(findByReferenceAndMember_migrated(referenceId, referenceType, memberId, memberType));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.management.api.model.JdbcMembership> findByReferenceAndMember_migrated(@Param(value = "refId")
String referenceId, @Param(value = "refType")
String referenceType, @Param(value = "mid")
String memberId, @Param(value = "mtype")
String memberType) {
    return RxJava2Adapter.maybeToMono(findByReferenceAndMember(referenceId, referenceType, memberId, memberType));
}
}

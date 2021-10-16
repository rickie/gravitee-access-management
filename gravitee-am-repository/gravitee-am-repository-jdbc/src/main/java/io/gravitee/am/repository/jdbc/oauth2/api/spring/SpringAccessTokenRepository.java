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
package io.gravitee.am.repository.jdbc.oauth2.api.spring;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken;



import java.time.LocalDateTime;

import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringAccessTokenRepository extends RxJava2CrudRepository<JdbcAccessToken, String> {
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByToken_migrated(token, now))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken> findByToken(@org.springframework.data.repository.query.Param(value = "token")
java.lang.String token, @org.springframework.data.repository.query.Param(value = "now")
java.time.LocalDateTime now) {
    return RxJava2Adapter.monoToMaybe(findByToken_migrated(token, now));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken> findByToken_migrated(@Param(value = "token")
String token, @Param(value = "now")
LocalDateTime now) {
    return RxJava2Adapter.maybeToMono(findByToken(token, now));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClientIdAndSubject_migrated(clientId, subject, now))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken> findByClientIdAndSubject(@org.springframework.data.repository.query.Param(value = "cli")
java.lang.String clientId, @org.springframework.data.repository.query.Param(value = "sub")
java.lang.String subject, @org.springframework.data.repository.query.Param(value = "now")
java.time.LocalDateTime now) {
    return RxJava2Adapter.fluxToFlowable(findByClientIdAndSubject_migrated(clientId, subject, now));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken> findByClientIdAndSubject_migrated(@Param(value = "cli")
String clientId, @Param(value = "sub")
String subject, @Param(value = "now")
LocalDateTime now) {
    return RxJava2Adapter.flowableToFlux(findByClientIdAndSubject(clientId, subject, now));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClientId_migrated(clientId, now))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken> findByClientId(@org.springframework.data.repository.query.Param(value = "cli")
java.lang.String clientId, @org.springframework.data.repository.query.Param(value = "now")
java.time.LocalDateTime now) {
    return RxJava2Adapter.fluxToFlowable(findByClientId_migrated(clientId, now));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken> findByClientId_migrated(@Param(value = "cli")
String clientId, @Param(value = "now")
LocalDateTime now) {
    return RxJava2Adapter.flowableToFlux(findByClientId(clientId, now));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByAuthorizationCode_migrated(code, now))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken> findByAuthorizationCode(@org.springframework.data.repository.query.Param(value = "auth")
java.lang.String code, @org.springframework.data.repository.query.Param(value = "now")
java.time.LocalDateTime now) {
    return RxJava2Adapter.fluxToFlowable(findByAuthorizationCode_migrated(code, now));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken> findByAuthorizationCode_migrated(@Param(value = "auth")
String code, @Param(value = "now")
LocalDateTime now) {
    return RxJava2Adapter.flowableToFlux(findByAuthorizationCode(code, now));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByClientId_migrated(clientId, now))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countByClientId(@org.springframework.data.repository.query.Param(value = "cli")
java.lang.String clientId, @org.springframework.data.repository.query.Param(value = "now")
java.time.LocalDateTime now) {
    return RxJava2Adapter.monoToSingle(countByClientId_migrated(clientId, now));
}
default reactor.core.publisher.Mono<java.lang.Long> countByClientId_migrated(@Param(value = "cli")
String clientId, @Param(value = "now")
LocalDateTime now) {
    return RxJava2Adapter.singleToMono(countByClientId(clientId, now));
}

}

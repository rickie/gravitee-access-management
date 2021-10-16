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

import io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcRefreshToken;
import io.reactivex.Maybe;
import java.time.LocalDateTime;
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
public interface SpringRefreshTokenRepository extends RxJava2CrudRepository<JdbcRefreshToken, String> {
      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcRefreshToken> findByToken(@org.springframework.data.repository.query.Param(value = "token")
java.lang.String token, @org.springframework.data.repository.query.Param(value = "now")
java.time.LocalDateTime now) {
    return RxJava2Adapter.monoToMaybe(findByToken_migrated(token, now));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcRefreshToken> findByToken_migrated(@Param(value = "token")
String token, @Param(value = "now")
LocalDateTime now) {
    return RxJava2Adapter.maybeToMono(findByToken(token, now));
}
}

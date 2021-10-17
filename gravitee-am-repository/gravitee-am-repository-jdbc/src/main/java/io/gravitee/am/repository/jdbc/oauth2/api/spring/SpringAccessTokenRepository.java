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
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
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
public interface SpringAccessTokenRepository extends RxJava2CrudRepository<JdbcAccessToken, String> {
      
Mono<JdbcAccessToken> findByToken_migrated(@Param(value = "token")
String token, @Param(value = "now")
LocalDateTime now);

      
Flux<JdbcAccessToken> findByClientIdAndSubject_migrated(@Param(value = "cli")
String clientId, @Param(value = "sub")
String subject, @Param(value = "now")
LocalDateTime now);

      
Flux<JdbcAccessToken> findByClientId_migrated(@Param(value = "cli")
String clientId, @Param(value = "now")
LocalDateTime now);

      
Flux<JdbcAccessToken> findByAuthorizationCode_migrated(@Param(value = "auth")
String code, @Param(value = "now")
LocalDateTime now);

      
Mono<Long> countByClientId_migrated(@Param(value = "cli")
String clientId, @Param(value = "now")
LocalDateTime now);

}

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


import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringUserRoleRepository extends RxJava2CrudRepository<JdbcUser.Role, String> {
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByUserId_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser.Role> findByUserId(@org.springframework.data.repository.query.Param(value = "user")
java.lang.String userId) {
    return RxJava2Adapter.fluxToFlowable(findByUserId_migrated(userId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcUser.Role> findByUserId_migrated(@Param(value = "user")
String userId) {
    return RxJava2Adapter.flowableToFlux(findByUserId(userId));
}
}

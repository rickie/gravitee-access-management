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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcEvent;

import java.time.LocalDateTime;

import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface SpringEventRepository extends RxJava2CrudRepository<JdbcEvent, String> {
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByTimeFrame_migrated(from, to))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.repository.jdbc.management.api.model.JdbcEvent> findByTimeFrame(@org.springframework.data.repository.query.Param(value = "from")
java.time.LocalDateTime from, @org.springframework.data.repository.query.Param(value = "to")
java.time.LocalDateTime to) {
    return RxJava2Adapter.fluxToFlowable(findByTimeFrame_migrated(from, to));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.jdbc.management.api.model.JdbcEvent> findByTimeFrame_migrated(@Param(value = "from")
LocalDateTime from, @Param(value = "to")
LocalDateTime to) {
    return RxJava2Adapter.flowableToFlux(findByTimeFrame(from, to));
}
}

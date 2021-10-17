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
import io.gravitee.am.repository.jdbc.management.api.model.JdbcMonitoring;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import java.time.LocalDateTime;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface SpringNodeMonitoringRepository extends RxJava2CrudRepository<JdbcMonitoring, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByNodeIdAndType_migrated(nodeId, type))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcMonitoring> findByNodeIdAndType(@Param(value = "nodeId")
String nodeId, @Param(value = "type")
String type) {
    return RxJava2Adapter.monoToMaybe(findByNodeIdAndType_migrated(nodeId, type));
}
default Mono<JdbcMonitoring> findByNodeIdAndType_migrated(@Param(value = "nodeId")
String nodeId, @Param(value = "type")
String type) {
    return RxJava2Adapter.maybeToMono(findByNodeIdAndType(nodeId, type));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByTypeAndTimeFrame_migrated(type, from, to))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcMonitoring> findByTypeAndTimeFrame(@Param(value = "type")
String type, @Param(value = "from")
LocalDateTime from, @Param(value = "to")
LocalDateTime to) {
    return RxJava2Adapter.fluxToFlowable(findByTypeAndTimeFrame_migrated(type, from, to));
}
default Flux<JdbcMonitoring> findByTypeAndTimeFrame_migrated(@Param(value = "type")
String type, @Param(value = "from")
LocalDateTime from, @Param(value = "to")
LocalDateTime to) {
    return RxJava2Adapter.flowableToFlux(findByTypeAndTimeFrame(type, from, to));
}
}

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
package io.gravitee.am.repository.jdbc.management.api.spring.alert;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcAlertTrigger;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcAlertTrigger.AlertNotifier;
import io.reactivex.Flowable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringAlertTriggerAlertNotifierRepository extends RxJava2CrudRepository<JdbcAlertTrigger.AlertNotifier, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByAlertTriggerId_migrated(alertTriggerId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<AlertNotifier> findByAlertTriggerId(@Param(value = "alertTriggerId")
String alertTriggerId) {
    return RxJava2Adapter.fluxToFlowable(findByAlertTriggerId_migrated(alertTriggerId));
}
default Flux<AlertNotifier> findByAlertTriggerId_migrated(@Param(value = "alertTriggerId")
String alertTriggerId) {
    return RxJava2Adapter.flowableToFlux(findByAlertTriggerId(alertTriggerId));
}
}

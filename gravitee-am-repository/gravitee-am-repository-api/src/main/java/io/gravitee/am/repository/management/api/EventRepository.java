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
package io.gravitee.am.repository.management.api;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.common.CrudRepository;
import io.reactivex.Flowable;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EventRepository extends CrudRepository<Event, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByTimeFrame_migrated(from, to))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Event> findByTimeFrame(long from, long to) {
    return RxJava2Adapter.fluxToFlowable(findByTimeFrame_migrated(from, to));
}
default Flux<Event> findByTimeFrame_migrated(long from, long to) {
    return RxJava2Adapter.flowableToFlux(findByTimeFrame(from, to));
}

}

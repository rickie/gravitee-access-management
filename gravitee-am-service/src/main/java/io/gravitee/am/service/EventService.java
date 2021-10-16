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
package io.gravitee.am.service;

import io.gravitee.am.model.common.event.Event;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EventService {

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.event.Event> create(io.gravitee.am.model.common.event.Event event) {
    return RxJava2Adapter.monoToSingle(create_migrated(event));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.event.Event> create_migrated(Event event) {
    return RxJava2Adapter.singleToMono(create(event));
}

      @Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.common.event.Event>> findByTimeFrame(long from, long to) {
    return RxJava2Adapter.monoToSingle(findByTimeFrame_migrated(from, to));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.common.event.Event>> findByTimeFrame_migrated(long from, long to) {
    return RxJava2Adapter.singleToMono(findByTimeFrame(from, to));
}
}

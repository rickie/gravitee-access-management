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
package io.gravitee.am.service.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.management.api.EventRepository;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.reactivex.Single;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class EventServiceImpl implements EventService {

    private final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);

    @Lazy
    @Autowired
    private EventRepository eventRepository;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(event))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Event> create(Event event) {
 return RxJava2Adapter.monoToSingle(create_migrated(event));
}
@Override
    public Mono<Event> create_migrated(Event event) {
        LOGGER.debug("Create a new event {}", event);

        event.setCreatedAt(new Date());
        event.setUpdatedAt(event.getCreatedAt());
        return eventRepository.create_migrated(event).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Event>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create an event", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create an event", ex)));
                }).apply(err)));
    }

    
@Override
    public Mono<List<Event>> findByTimeFrame_migrated(long from, long to) {
        LOGGER.debug("Find events with time frame {} and {}", from, to);
        return eventRepository.findByTimeFrame_migrated(from, to).collectList().onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<List<Event>>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to find events by time frame", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find events by time frame", ex)));
                }).apply(err)));
    }
}

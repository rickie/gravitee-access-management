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
package io.gravitee.am.repository.jdbc.management.api;

import static java.time.ZoneOffset.UTC;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.from;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcEvent;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringEventRepository;
import io.gravitee.am.repository.management.api.EventRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcEventRepository extends AbstractJdbcRepository implements EventRepository {

    @Autowired
    private SpringEventRepository eventRepository;

    protected Event toEntity(JdbcEvent entity) {
        return mapper.map(entity, Event.class);
    }

    protected JdbcEvent toJdbcEntity(Event entity) {
        return mapper.map(entity, JdbcEvent.class);
    }

    @Override
    public Flowable<Event> findByTimeFrame(long from, long to) {
        LOGGER.debug("findByTimeFrame({}, {})", from, to);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(eventRepository.findByTimeFrame(
                LocalDateTime.ofInstant(Instant.ofEpochMilli(from), UTC),
                LocalDateTime.ofInstant(Instant.ofEpochMilli(to), UTC))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Maybe<Event> findById(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(eventRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Single<Event> create(Event item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create event with id {}", item.getId());

        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("events");

        // doesn't use the class introspection to allow the usage of Json type in PostgreSQL
        insertSpec = addQuotedField(insertSpec,"id", item.getId(), String.class);
        insertSpec = addQuotedField(insertSpec,"type", item.getType(), String.class);
        insertSpec = databaseDialectHelper.addJsonField(insertSpec, "payload", item.getPayload());
        insertSpec = addQuotedField(insertSpec,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = insertSpec.fetch().rowsUpdated();

        return RxJava2Adapter.monoToSingle(action.flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()));
    }

    @Override
    public Single<Event> update(Event item) {
        LOGGER.debug("update event with id {}", item.getId());

        final DatabaseClient.GenericUpdateSpec updateSpec = dbClient.update().table("events");
        Map<SqlIdentifier, Object> updateFields = new HashMap<>();
        updateFields = addQuotedField(updateFields,"id", item.getId(), String.class);
        updateFields = addQuotedField(updateFields,"type", item.getType(), String.class);
        updateFields = databaseDialectHelper.addJsonField(updateFields, "payload", item.getPayload());
        updateFields = addQuotedField(updateFields,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);
        Mono<Integer> action = updateSpec.using(Update.from(updateFields)).matching(from(where("id").is(item.getId()))).fetch().rowsUpdated();

        return RxJava2Adapter.monoToSingle(action.flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()));
    }

    @Override
    public Completable delete(String id) {
        LOGGER.debug("delete({})", id);
        return eventRepository.deleteById(id);
    }
}

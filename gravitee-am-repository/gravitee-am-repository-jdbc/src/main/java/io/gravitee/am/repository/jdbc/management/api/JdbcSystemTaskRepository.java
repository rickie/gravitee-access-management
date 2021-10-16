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

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.from;
import static reactor.adapter.rxjava.RxJava2Adapter.*;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.SystemTask;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcSystemTask;
import io.gravitee.am.repository.jdbc.management.api.model.mapper.LocalDateConverter;
import io.gravitee.am.repository.management.api.SystemTaskRepository;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
public class JdbcSystemTaskRepository extends AbstractJdbcRepository implements SystemTaskRepository {

    protected final LocalDateConverter dateConverter = new LocalDateConverter();

    protected SystemTask toEntity(JdbcSystemTask entity) {
        return mapper.map(entity, SystemTask.class);
    }

    protected JdbcSystemTask toJdbcEntity(SystemTask entity) {
        return mapper.map(entity, JdbcSystemTask.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<SystemTask> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<SystemTask> findById_migrated(String id) {
        LOGGER.debug("findById({}, {}, {})", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(dbClient.select()
                .from(JdbcSystemTask.class)
                .project("*")
                .matching(from(where("id").is(id)))
                .as(JdbcSystemTask.class).first().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));

    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<SystemTask> create(SystemTask item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<SystemTask> create_migrated(SystemTask item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("Create SystemTask with id {}", item.getId());

        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("system_tasks");
        insertSpec = addQuotedField(insertSpec, "id", item.getId(), String.class);
        insertSpec = addQuotedField(insertSpec, "type", item.getType(), String.class);
        insertSpec = addQuotedField(insertSpec, "status", item.getStatus(), String.class);
        insertSpec = addQuotedField(insertSpec, "operation_id", item.getOperationId(), String.class);
        insertSpec = addQuotedField(insertSpec, "created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec, "updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = insertSpec.fetch().rowsUpdated();
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(action.flatMap(i->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.findById_migrated(item.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<SystemTask> update(SystemTask item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<SystemTask> update_migrated(SystemTask item) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new IllegalStateException("SystemTask can't be updated without control on the operationId"))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.updateIf_migrated(item, operationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<SystemTask> updateIf(SystemTask item, String operationId) {
 return RxJava2Adapter.monoToSingle(updateIf_migrated(item, operationId));
}
@Override
    public Mono<SystemTask> updateIf_migrated(SystemTask item, String operationId) {
        LOGGER.debug("Update SystemTask with id {}", item.getId());

        final DatabaseClient.GenericUpdateSpec updateSpec = dbClient.update().table("system_tasks");
        Map<SqlIdentifier, Object> updateFields = new HashMap<>();
        updateFields = addQuotedField(updateFields, "id", item.getId(), String.class);
        updateFields = addQuotedField(updateFields, "type", item.getType(), String.class);
        updateFields = addQuotedField(updateFields, "status", item.getStatus(), String.class);
        updateFields = addQuotedField(updateFields, "operation_id", item.getOperationId(), String.class);
        updateFields = addQuotedField(updateFields, "created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields, "updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = updateSpec.using(Update.from(updateFields))
                .matching(from(where("id").is(item.getId()).and(where("operation_id").is(operationId))))
                .fetch()
                .rowsUpdated();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(action.flatMap(i->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.findById_migrated(item.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("Delete SystemTask with id {}", id);
        Mono<Integer> delete = dbClient.delete().from(JdbcSystemTask.class)
                .matching(from(where("id").is(id))).fetch().rowsUpdated();
        return RxJava2Adapter.completableToMono(monoToCompletable(delete));
    }
}

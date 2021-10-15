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
import static org.springframework.data.relational.core.query.Criteria.from;
import static org.springframework.data.relational.core.query.Criteria.where;
import static reactor.adapter.rxjava.RxJava2Adapter.*;

import io.gravitee.am.model.AuthenticationFlowContext;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcAuthenticationFlowContext;
import io.gravitee.am.repository.management.api.AuthenticationFlowContextRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcAuthenticationFlowContextRepository extends AbstractJdbcRepository implements AuthenticationFlowContextRepository {

    protected AuthenticationFlowContext toEntity(JdbcAuthenticationFlowContext entity) {
        return mapper.map(entity, AuthenticationFlowContext.class);
    }

    @Override
    public Maybe<AuthenticationFlowContext> findById(String id) {
        LOGGER.debug("findById({})", id);
        if (id == null) {
            return RxJava2Adapter.monoToMaybe(Mono.empty());
        }
        return RxJava2Adapter.monoToMaybe(dbClient.select()
                .from(JdbcAuthenticationFlowContext.class)
                .matching(from(where("id").is(id)))
                .as(JdbcAuthenticationFlowContext.class).one().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Maybe<AuthenticationFlowContext> findLastByTransactionId(String transactionId) {
        LOGGER.debug("findLastByTransactionId({})", transactionId);
        if (transactionId == null) {
            return RxJava2Adapter.monoToMaybe(Mono.empty());
        }
        
        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.monoToMaybe(dbClient.select()
                .from(JdbcAuthenticationFlowContext.class)
                .matching(from(where("transaction_id").is(transactionId).and(where("expire_at").greaterThan(now))))
                .orderBy(Sort.Order.desc("version"))
                .as(JdbcAuthenticationFlowContext.class).first().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Flowable<AuthenticationFlowContext> findByTransactionId(String transactionId) {
        LOGGER.debug("findByTransactionId({})", transactionId);
        if (transactionId == null) {
            return RxJava2Adapter.fluxToFlowable(Flux.empty());
        }

        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.fluxToFlowable(dbClient.select()
                .from(JdbcAuthenticationFlowContext.class)
                .matching(from(where("transaction_id").is(transactionId).and(where("expire_at").greaterThan(now))))
                .orderBy(Sort.Order.desc("version"))
                .as(JdbcAuthenticationFlowContext.class).all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Single<AuthenticationFlowContext> create(AuthenticationFlowContext context) {
       String id = context.getTransactionId() + "-" + context.getVersion();
        LOGGER.debug("Create AuthenticationContext with id {}", id);

        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("auth_flow_ctx");

        // doesn't use the class introspection to handle json objects
        insertSpec = addQuotedField(insertSpec,"id", id, String.class);
        insertSpec = addQuotedField(insertSpec,"transaction_id", context.getTransactionId(), String.class);
        insertSpec = addQuotedField(insertSpec,"version", context.getVersion(), Integer.class);
        insertSpec = addQuotedField(insertSpec,"created_at", dateConverter.convertTo(context.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec,"expire_at", dateConverter.convertTo(context.getExpireAt(), null), LocalDateTime.class);
        insertSpec = databaseDialectHelper.addJsonField(insertSpec,"data", context.getData());

        Mono<Integer> insertAction = insertSpec.fetch().rowsUpdated();

        return RxJava2Adapter.monoToSingle(insertAction.flatMap(i->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(this.findById(id)).single()))));
    }

    @Override
    public Completable delete(String transactionId) {
        LOGGER.debug("delete({})", transactionId);
        return monoToCompletable(dbClient.delete()
                .from(JdbcAuthenticationFlowContext.class)
                .matching(from(where("transaction_id").is(transactionId))).fetch().rowsUpdated());
    }

    @Override
    public Completable delete(String transactionId, int version) {
        LOGGER.debug("delete({}, {})", transactionId, version);
        return monoToCompletable(dbClient.delete()
                .from(JdbcAuthenticationFlowContext.class)
                .matching(from(where("transaction_id").is(transactionId).and(where("version").is(version)))).fetch().rowsUpdated());
    }

    @Override
    public Completable purgeExpiredData() {
        LOGGER.debug("purgeExpiredData()");
        LocalDateTime now = LocalDateTime.now(UTC);
        return dbClient.delete().from(JdbcAuthenticationFlowContext.class).matching(where("expire_at").lessThan(now)).then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to purge authentication contexts", error))).as(RxJava2Adapter::monoToCompletable);
    }
}

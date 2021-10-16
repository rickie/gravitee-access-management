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
package io.gravitee.am.repository.jdbc.oauth2.api;

import static java.time.ZoneOffset.UTC;
import static org.springframework.data.relational.core.query.Criteria.from;
import static org.springframework.data.relational.core.query.Criteria.where;
import static reactor.adapter.rxjava.RxJava2Adapter.*;

import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcLoginAttempt;
import io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAuthorizationCode;
import io.gravitee.am.repository.jdbc.oauth2.api.spring.SpringAuthorizationCodeRepository;
import io.gravitee.am.repository.oauth2.api.AuthorizationCodeRepository;
import io.gravitee.am.repository.oauth2.model.AuthorizationCode;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcAuthorizationCodeRepository extends AbstractJdbcRepository implements AuthorizationCodeRepository {
    @Autowired
    private SpringAuthorizationCodeRepository authorizationCodeRepository;

    protected AuthorizationCode toEntity(JdbcAuthorizationCode entity) {
        return mapper.map(entity, AuthorizationCode.class);
    }

    protected JdbcAuthorizationCode toJdbcEntity(AuthorizationCode entity) {
        return mapper.map(entity, JdbcAuthorizationCode.class);
    }

    @Deprecated
@Override
    public Single<AuthorizationCode> create(AuthorizationCode authorizationCode) {
 return RxJava2Adapter.monoToSingle(create_migrated(authorizationCode));
}
@Override
    public Mono<AuthorizationCode> create_migrated(AuthorizationCode authorizationCode) {
        authorizationCode.setId(authorizationCode.getId() == null ? RandomString.generate() : authorizationCode.getId());
        LOGGER.debug("Create authorizationCode with id {} and code {}", authorizationCode.getId(), authorizationCode.getCode());

        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("authorization_codes");

        // doesn't use the class introspection to handle json objects
        insertSpec = addQuotedField(insertSpec,"id", authorizationCode.getId(), String.class);
        insertSpec = addQuotedField(insertSpec,"client_id", authorizationCode.getClientId(), String.class);
        insertSpec = addQuotedField(insertSpec,"code", authorizationCode.getCode(), String.class);
        insertSpec = addQuotedField(insertSpec,"redirect_uri", authorizationCode.getRedirectUri(), String.class);
        insertSpec = addQuotedField(insertSpec,"subject", authorizationCode.getSubject(), String.class);
        insertSpec = addQuotedField(insertSpec,"transaction_id", authorizationCode.getTransactionId(), String.class);
        insertSpec = addQuotedField(insertSpec,"context_version", authorizationCode.getContextVersion(), int.class);
        insertSpec = addQuotedField(insertSpec,"created_at", dateConverter.convertTo(authorizationCode.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec,"expire_at", dateConverter.convertTo(authorizationCode.getExpireAt(), null), LocalDateTime.class);
        insertSpec = databaseDialectHelper.addJsonField(insertSpec,"scopes", authorizationCode.getScopes());
        insertSpec = databaseDialectHelper.addJsonField(insertSpec,"request_parameters", authorizationCode.getRequestParameters());

        Mono<Integer> insertAction = insertSpec.fetch().rowsUpdated();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(insertAction.flatMap(i->RxJava2Adapter.maybeToMono(authorizationCodeRepository.findById(authorizationCode.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).single()).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("Unable to create authorizationCode with id {}", authorizationCode.getId(), error)))));
    }

    @Deprecated
@Override
    public Maybe<AuthorizationCode> delete(String id) {
 return RxJava2Adapter.monoToMaybe(delete_migrated(id));
}
@Override
    public Mono<AuthorizationCode> delete_migrated(String id) {
        LOGGER.debug("delete({})", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(authorizationCodeRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->dbClient.delete().from(JdbcAuthorizationCode.class).matching(from(where("id").is(id))).fetch().rowsUpdated().map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Integer i)->z)))));
    }

    @Deprecated
@Override
    public Maybe<AuthorizationCode> findByCode(String code) {
 return RxJava2Adapter.monoToMaybe(findByCode_migrated(code));
}
@Override
    public Mono<AuthorizationCode> findByCode_migrated(String code) {
        LOGGER.debug("findByCode({})", code);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(authorizationCodeRepository.findByCode(code, LocalDateTime.now(UTC))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to retrieve AuthorizationCode with code {}", code)))));
    }

    @Deprecated
public Completable purgeExpiredData() {
 return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}
public Mono<Void> purgeExpiredData_migrated() {
        LOGGER.debug("purgeExpiredData()");
        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.completableToMono(dbClient.delete().from(JdbcAuthorizationCode.class).matching(where("expire_at").lessThan(now)).then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to purge authorization tokens", error))).as(RxJava2Adapter::monoToCompletable));
    }
}

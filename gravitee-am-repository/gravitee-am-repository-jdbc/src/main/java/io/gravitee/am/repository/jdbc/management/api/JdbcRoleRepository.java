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

import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcRole;
import io.gravitee.am.repository.jdbc.management.api.spring.role.SpringRoleOauthScopeRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.role.SpringRoleRepository;
import io.gravitee.am.repository.management.api.RoleRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcRoleRepository extends AbstractJdbcRepository implements RoleRepository {

    @Autowired
    private SpringRoleRepository roleRepository;

    @Autowired
    private SpringRoleOauthScopeRepository oauthScopeRepository;

    protected Role toEntity(JdbcRole entity) {
        return mapper.map(entity, Role.class);
    }

    protected JdbcRole toJdbcEntity(Role entity) {
        return mapper.map(entity, JdbcRole.class);
    }

    @Override
    public Flowable<Role> findAll(ReferenceType referenceType, String referenceId) {
        LOGGER.debug("findAll({}, {})", referenceType, referenceId);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(roleRepository.findByReference(referenceType.name(), referenceId)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(role -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(completeWithScopes(RxJava2Adapter.monoToMaybe(Mono.just(role)), role.getId())).flux()))));
    }

    @Override
    public Single<Page<Role>> findAll(ReferenceType referenceType, String referenceId, int page, int size) {
        LOGGER.debug("findAll({}, {}, {}, {})", referenceType, referenceId, page, size);
        return RxJava2Adapter.monoToSingle(dbClient.select()
                .from(JdbcRole.class)
                .matching(from(where("reference_id").is(referenceId)
                        .and(where("reference_type").is(referenceType.name()))))
                .orderBy(Sort.Order.asc("name"))
                .page(PageRequest.of(page, size))
                .as(JdbcRole.class).all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(role -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(completeWithScopes(RxJava2Adapter.monoToMaybe(Mono.just(role)), role.getId())).flux()))).collectList().flatMap(content->RxJava2Adapter.singleToMono(roleRepository.countByReference(referenceType.name(), referenceId)).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long count)->new Page<Role>(content, page, count)))));
    }

    @Override
    public Single<Page<Role>> search(ReferenceType referenceType, String referenceId, String query, int page, int size) {
        LOGGER.debug("search({}, {}, {}, {}, {})", referenceType, referenceId, query, page, size);

        boolean wildcardSearch = query.contains("*");
        String wildcardValue = query.replaceAll("\\*+", "%");

        String search = this.databaseDialectHelper.buildSearchRoleQuery(wildcardSearch, page, size);
        String count = this.databaseDialectHelper.buildCountRoleQuery(wildcardSearch);

        return RxJava2Adapter.monoToSingle(dbClient.execute(search)
                .bind("value", wildcardSearch ? wildcardValue : query)
                .bind("refId", referenceId)
                .bind("refType", referenceType.name())
                .as(JdbcRole.class)
                .fetch().all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(role -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(completeWithScopes(RxJava2Adapter.monoToMaybe(Mono.just(role)), role.getId())).flux()))).collectList().flatMap(data->dbClient.execute(count).bind("value", wildcardSearch ? wildcardValue : query).bind("refId", referenceId).bind("refType", referenceType.name()).as(Long.class).fetch().first().map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long total)->new Page<Role>(data, page, total)))));
    }

    @Override
    public Flowable<Role> findByIdIn(List<String> ids) {
        LOGGER.debug("findByIdIn({})", ids);
        if (ids == null || ids.isEmpty()) {
            return RxJava2Adapter.fluxToFlowable(Flux.empty());
        }
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(roleRepository.findByIdIn(ids)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(role -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(completeWithScopes(RxJava2Adapter.monoToMaybe(Mono.just(role)), role.getId())).flux()))));
    }

    @Override
    public Maybe<Role> findById(ReferenceType referenceType, String referenceId, String role) {
        LOGGER.debug("findById({},{},{})", referenceType, referenceId, role);
        return completeWithScopes(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(roleRepository.findById(referenceType.name(), referenceId, role)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))), role);
    }

    @Override
    public Maybe<Role> findByNameAndAssignableType(ReferenceType referenceType, String referenceId, String name, ReferenceType assignableType) {
        LOGGER.debug("findByNameAndAssignableType({},{},{},{})", referenceType, referenceId, name, assignableType);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(roleRepository.findByNameAndAssignableType(referenceType.name(), referenceId, name, assignableType.name())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->completeWithScopes(RxJava2Adapter.monoToMaybe(Mono.just(z)), z.getId()).as(RxJava2Adapter::maybeToMono)));
    }

    @Override
    public Flowable<Role> findByNamesAndAssignableType(ReferenceType referenceType, String referenceId, List<String> names, ReferenceType assignableType) {
        LOGGER.debug("findByNamesAndAssignableType({},{},{},{})", referenceType, referenceId, names, assignableType);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(roleRepository.findByNamesAndAssignableType(referenceType.name(), referenceId, names, assignableType.name())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<Role, MaybeSource<Role>>toJdkFunction(role -> completeWithScopes(RxJava2Adapter.monoToMaybe(Mono.just(role)), role.getId())).apply(e)))));
    }

    @Override
    public Maybe<Role> findById(String id) {
        LOGGER.debug("findById({})", id);
        return completeWithScopes(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(roleRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))), id);
    }

    @Override
    public Single<Role> create(Role item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("Create Role with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("roles");

        // doesn't use the class introspection to allow the usage of Json type in PostgreSQL
        insertSpec = addQuotedField(insertSpec,"id", item.getId(), String.class);
        insertSpec = addQuotedField(insertSpec,"name", item.getName(), String.class);
        insertSpec = addQuotedField(insertSpec,"system", item.isSystem(), String.class);
        insertSpec = addQuotedField(insertSpec,"default_role", item.isDefaultRole(), String.class);
        insertSpec = addQuotedField(insertSpec,"description", item.getDescription(), String.class);
        insertSpec = addQuotedField(insertSpec,"reference_id", item.getReferenceId(), String.class);
        insertSpec = addQuotedField(insertSpec,"reference_type", item.getReferenceType() == null ? null : item.getReferenceType().name(), String.class);
        insertSpec = addQuotedField(insertSpec,"assignable_type", item.getAssignableType() == null ? null : item.getAssignableType().name(), String.class);
        insertSpec = addQuotedField(insertSpec,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);
        insertSpec = databaseDialectHelper.addJsonField(insertSpec, "permission_acls", item.getPermissionAcls());

        Mono<Integer> action = insertSpec.fetch().rowsUpdated();

        final List<String> resourceScopes = item.getOauthScopes();
        if (resourceScopes != null && !resourceScopes.isEmpty()) {
            action = action.then(Flux.fromIterable(resourceScopes).concatMap(scope -> {
                JdbcRole.OAuthScope rScope = new JdbcRole.OAuthScope();
                rScope.setScope(scope);
                rScope.setRoleId(item.getId());
                return dbClient.insert().into(JdbcRole.OAuthScope.class).using(rScope).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return RxJava2Adapter.monoToSingle(action.as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()));
    }

    @Override
    public Single<Role> update(Role item) {
        LOGGER.debug("Update Role with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> deleteScopes = dbClient.delete().from(JdbcRole.OAuthScope.class)
                .matching(from(where("role_id").is(item.getId()))).fetch().rowsUpdated();

        final DatabaseClient.GenericUpdateSpec updateSpec = dbClient.update().table("roles");
        Map<SqlIdentifier, Object> updateFields = new HashMap<>();
        // doesn't use the class introspection to allow the usage of Json type in PostgreSQL
        updateFields = addQuotedField(updateFields,"id", item.getId(), String.class);
        updateFields = addQuotedField(updateFields,"name", item.getName(), String.class);
        updateFields = addQuotedField(updateFields,"system", item.isSystem(), String.class);
        updateFields = addQuotedField(updateFields,"default_role", item.isDefaultRole(), String.class);
        updateFields = addQuotedField(updateFields,"description", item.getDescription(), String.class);
        updateFields = addQuotedField(updateFields,"reference_id", item.getReferenceId(), String.class);
        updateFields = addQuotedField(updateFields,"reference_type", item.getReferenceType() == null ? null : item.getReferenceType().name(), String.class);
        updateFields = addQuotedField(updateFields,"assignable_type", item.getAssignableType() == null ? null : item.getAssignableType().name(), String.class);
        updateFields = addQuotedField(updateFields,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);
        updateFields = databaseDialectHelper.addJsonField(updateFields, "permission_acls", item.getPermissionAcls());

        Mono<Integer> action = updateSpec.using(Update.from(updateFields)).matching(from(where("id").is(item.getId()))).fetch().rowsUpdated();

        final List<String> resourceScopes = item.getOauthScopes();
        if (resourceScopes != null && !resourceScopes.isEmpty()) {
            action = action.then(Flux.fromIterable(resourceScopes).concatMap(scope -> {
                JdbcRole.OAuthScope rScope = new JdbcRole.OAuthScope();
                rScope.setScope(scope);
                rScope.setRoleId(item.getId());
                return dbClient.insert().into(JdbcRole.OAuthScope.class).using(rScope).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return RxJava2Adapter.monoToSingle(deleteScopes.then(action).as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()));
    }

    @Override
    public Completable delete(String id) {
        LOGGER.debug("Delete Role with id {}", id);

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> deleteScopes = dbClient.delete().from(JdbcRole.OAuthScope.class)
                .matching(from(where("role_id").is(id))).fetch().rowsUpdated();

        Mono<Integer> delete = dbClient.delete().from(JdbcRole.class)
                .matching(from(where("id").is(id))).fetch().rowsUpdated();

        return monoToCompletable(delete.then(deleteScopes.as(trx::transactional)));
    }

    private Maybe<Role> completeWithScopes(Maybe<Role> maybeRole, String id) {
        Maybe<List<String>> scopes = RxJava2Adapter.monoToMaybe(RxJava2Adapter.flowableToFlux(oauthScopeRepository.findAllByRole(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcRole.OAuthScope::getScope)).collectList());

        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(maybeRole).zipWith(RxJava2Adapter.maybeToMono(scopes), RxJavaReactorMigrationUtil.toJdkBiFunction((role, scope) -> {
            LOGGER.debug("findById({}) fetch {} oauth scopes", id, scope == null ? 0 : scope.size());
            role.setOauthScopes(scope);
            return role;
        })));
    }
}

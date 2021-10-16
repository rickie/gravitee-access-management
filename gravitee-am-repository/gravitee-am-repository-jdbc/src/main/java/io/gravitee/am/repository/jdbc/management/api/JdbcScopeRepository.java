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
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcScope;
import io.gravitee.am.repository.jdbc.management.api.spring.scope.SpringScopeClaimRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.scope.SpringScopeRepository;
import io.gravitee.am.repository.management.api.ScopeRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
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
public class JdbcScopeRepository extends AbstractJdbcRepository implements ScopeRepository {

    @Autowired
    private SpringScopeRepository scopeRepository;

    @Autowired
    private SpringScopeClaimRepository claimRepository;

    protected Scope toEntity(JdbcScope entity) {
        return mapper.map(entity, Scope.class);
    }

    protected JdbcScope toJdbcEntity(Scope entity) {
        return mapper.map(entity, JdbcScope.class);
    }

    @Override
    public Single<Page<Scope>> findByDomain(String domain, int page, int size) {

        LOGGER.debug("findByDomain({}, {}, {})", domain, page, size);
        return RxJava2Adapter.monoToSingle(dbClient.select()
                .from("scopes")
                .project("*")
                .matching(from(where("domain").is(domain)))
                .orderBy(Sort.Order.by("scopes."+databaseDialectHelper.toSql(SqlIdentifier.quoted("key"))))
                .page(PageRequest.of(page, size))
                .as(JdbcScope.class).fetch().all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(scope -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(completeWithClaims(RxJava2Adapter.monoToMaybe(Mono.just(scope)), scope.getId())).flux()))).collectList().flatMap(content->RxJava2Adapter.singleToMono(countByDomain(domain)).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long count)->new Page<>(content, page, count)))));
    }

    @Deprecated
private Single<Long> countByDomain(String domain) {
 return RxJava2Adapter.monoToSingle(countByDomain_migrated(domain));
}
private Mono<Long> countByDomain_migrated(String domain) {
        return RxJava2Adapter.singleToMono(monoToSingle(dbClient.execute("select count(s."+databaseDialectHelper.toSql(SqlIdentifier.quoted("key"))+") from scopes s where s.domain = :domain")
                .bind("domain", domain)
                .as(Long.class)
                .fetch().first()));
    }

    @Override
    public Single<Page<Scope>> search(String domain, String query, int page, int size) {
        LOGGER.debug("search({}, {})", domain, query);

        boolean wildcardSearch = query.contains("*");
        String wildcardQuery = query.replaceAll("\\*+", "%");

        String search = this.databaseDialectHelper.buildSearchScopeQuery(wildcardSearch, page, size);
        String count = this.databaseDialectHelper.buildCountScopeQuery(wildcardSearch);

        return RxJava2Adapter.monoToSingle(dbClient.execute(search)
                .bind("domain", domain)
                .bind("value", wildcardSearch ? wildcardQuery.toUpperCase() : query.toUpperCase())
                .as(JdbcScope.class)
                .fetch().all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(scope -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(completeWithClaims(RxJava2Adapter.monoToMaybe(Mono.just(scope)), scope.getId())).flux()))).collectList().flatMap(data->dbClient.execute(count).bind("domain", domain).bind("value", wildcardSearch ? wildcardQuery.toUpperCase() : query.toUpperCase()).as(Long.class).fetch().first().map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long total)->new Page<>(data, page, total)))));
    }

    @Deprecated
private Maybe<Scope> completeWithClaims(Maybe<Scope> maybeScope, String id) {
 return RxJava2Adapter.monoToMaybe(completeWithClaims_migrated(maybeScope, id));
}
private Mono<Scope> completeWithClaims_migrated(Maybe<Scope> maybeScope, String id) {
        Maybe<List<String>> scopeClaims = RxJava2Adapter.monoToMaybe(RxJava2Adapter.flowableToFlux(claimRepository.findByScopeId(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcScope.Claims::getClaim)).collectList());

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(maybeScope).zipWith(RxJava2Adapter.maybeToMono(scopeClaims), RxJavaReactorMigrationUtil.toJdkBiFunction((scope, claims) -> {
            LOGGER.debug("findById({}) fetch {} scopeClaims", id, claims == null ? 0 : claims.size());
            scope.setClaims(claims);
            return scope;
        }))));
    }

    @Override
    public Maybe<Scope> findByDomainAndKey(String domain, String key) {
        LOGGER.debug("findByDomainAndKey({}, {})", domain, key);
        return RxJava2Adapter.monoToMaybe(dbClient.select().from(JdbcScope.class)
                .project("*")
                .matching(from(where("domain").is(domain)
                        .and(where(databaseDialectHelper.toSql(SqlIdentifier.quoted("key"))).is(key))))
                .as(JdbcScope.class)
                .first().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->completeWithClaims(RxJava2Adapter.monoToMaybe(Mono.just(z)), z.getId()).as(RxJava2Adapter::maybeToMono)));
    }

    @Override
    public Flowable<Scope> findByDomainAndKeys(String domain, List<String> keys) {
        LOGGER.debug("findByDomainAndKeys({}, {})", domain, keys);
        return RxJava2Adapter.fluxToFlowable(dbClient.select().from(JdbcScope.class)
                .project("*")
                .matching(from(where("domain").is(domain)
                        .and(where(databaseDialectHelper.toSql(SqlIdentifier.quoted("key"))).in(keys))))
                .as(JdbcScope.class)
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(scope -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(completeWithClaims(RxJava2Adapter.monoToMaybe(Mono.just(scope)), scope.getId())).flux()))));
    }

    @Deprecated
@Override
    public Maybe<Scope> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Scope> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(scopeRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->completeWithClaims(RxJava2Adapter.monoToMaybe(Mono.just(z)), z.getId()).as(RxJava2Adapter::maybeToMono))));
    }

    @Deprecated
@Override
    public Single<Scope> create(Scope item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Scope> create_migrated(Scope item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("Create Scope with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("scopes");
        // doesn't use the class introspection to detect the fields due to keyword column name
        insertSpec = addQuotedField(insertSpec, "id", item.getId(), String.class);
        insertSpec = addQuotedField(insertSpec, "name", item.getName(), String.class);
        insertSpec = addQuotedField(insertSpec, "domain", item.getDomain(), String.class);
        insertSpec = addQuotedField(insertSpec, "description", item.getDescription(), String.class);
        insertSpec = addQuotedField(insertSpec, "expires_in", item.getExpiresIn(), Integer.class);
        insertSpec = addQuotedField(insertSpec, "icon_uri", item.getIconUri(), String.class);
        insertSpec = addQuotedField(insertSpec, "key", item.getKey(), String.class); // mssql keyword
        insertSpec = addQuotedField(insertSpec, "discovery", item.isDiscovery(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "parameterized", item.isParameterized(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "system", item.isSystem(), Boolean.class);
        insertSpec = addQuotedField(insertSpec, "created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec, "updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = insertSpec.fetch().rowsUpdated();

        final List<String> scopeClaims = item.getClaims();
        if (scopeClaims != null && !scopeClaims.isEmpty()) {
            action = action.then(Flux.fromIterable(scopeClaims).concatMap(claim -> {
                JdbcScope.Claims sClaim = new JdbcScope.Claims();
                sClaim.setClaim(claim);
                sClaim.setScopeId(item.getId());
                return dbClient.insert().into(JdbcScope.Claims.class).using(sClaim).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(action.as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single())));
    }

    @Deprecated
@Override
    public Single<Scope> update(Scope item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Scope> update_migrated(Scope item) {
        LOGGER.debug("Update Scope with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> deleteClaims = dbClient.delete().from(JdbcScope.Claims.class)
                .matching(from(where("scope_id").is(item.getId()))).fetch().rowsUpdated();

        final DatabaseClient.GenericUpdateSpec updateSpec = dbClient.update().table("scopes");
        // doesn't use the class introspection to detect the fields due to keyword column name
        Map<SqlIdentifier, Object> updateFields = new HashMap<>();
        updateFields = addQuotedField(updateFields, "id", item.getId(), String.class);
        updateFields = addQuotedField(updateFields, "name", item.getName(), String.class);
        updateFields = addQuotedField(updateFields, "domain", item.getDomain(), String.class);
        updateFields = addQuotedField(updateFields, "description", item.getDescription(), String.class);
        updateFields = addQuotedField(updateFields, "expires_in", item.getExpiresIn(), Integer.class);
        updateFields = addQuotedField(updateFields, "icon_uri", item.getIconUri(), String.class);
        updateFields = addQuotedField(updateFields, "key", item.getKey(), String.class); // mssql keyword
        updateFields = addQuotedField(updateFields, "discovery", item.isDiscovery(), Boolean.class);
        updateFields = addQuotedField(updateFields, "parameterized", item.isParameterized(), Boolean.class);
        updateFields = addQuotedField(updateFields, "system", item.isSystem(), Boolean.class);
        updateFields = addQuotedField(updateFields, "created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields, "updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = updateSpec.using(Update.from(updateFields)).matching(from(where("id").is(item.getId()))).fetch().rowsUpdated();

        final List<String> scopeClaims = item.getClaims();
        if (scopeClaims != null && !scopeClaims.isEmpty()) {
            action = action.then(Flux.fromIterable(scopeClaims).concatMap(claim -> {
                JdbcScope.Claims sClaim = new JdbcScope.Claims();
                sClaim.setClaim(claim);
                sClaim.setScopeId(item.getId());
                return dbClient.insert().into(JdbcScope.Claims.class).using(sClaim).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(deleteClaims.then(action).as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single())));
    }

    @Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("delete({})", id);

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> deleteClaim = dbClient.delete().from(JdbcScope.Claims.class)
                .matching(from(where("scope_id").is(id))).fetch().rowsUpdated();

        Mono<Integer> delete = dbClient.delete().from(JdbcScope.class)
                .matching(from(where("id").is(id))).fetch().rowsUpdated();

        return RxJava2Adapter.completableToMono(monoToCompletable(deleteClaim.then(delete).as(trx::transactional)));
    }

}

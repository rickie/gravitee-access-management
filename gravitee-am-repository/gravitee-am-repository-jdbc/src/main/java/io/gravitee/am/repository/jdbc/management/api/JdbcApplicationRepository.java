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
import io.gravitee.am.model.Application;
import io.gravitee.am.model.application.ApplicationOAuthSettings;
import io.gravitee.am.model.application.ApplicationScopeSettings;
import io.gravitee.am.model.application.ApplicationSettings;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication;
import io.gravitee.am.repository.jdbc.management.api.spring.application.SpringApplicationFactorRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.application.SpringApplicationIdentityRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.application.SpringApplicationRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.application.SpringApplicationScopeRepository;
import io.gravitee.am.repository.management.api.ApplicationRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.*;
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
public class JdbcApplicationRepository extends AbstractJdbcRepository implements ApplicationRepository {
    public static final int MAX_CONCURRENCY = 1;

    @Autowired
    private SpringApplicationRepository applicationRepository;

    @Autowired
    private SpringApplicationFactorRepository factorRepository;

    @Autowired
    private SpringApplicationScopeRepository scopeRepository;

    @Autowired
    private SpringApplicationIdentityRepository identityRepository;

    protected Application toEntity(JdbcApplication entity) {
        return mapper.map(entity, Application.class);
    }

    protected JdbcApplication toJdbcEntity(Application entity) {
        return mapper.map(entity, JdbcApplication.class);
    }

    private Single<Application> completeApplication(Application entity) {
        return RxJava2Adapter.monoToSingle(Mono.just(entity).flatMap(app->RxJava2Adapter.singleToMono(identityRepository.findAllByApplicationId(app.getId()).map(JdbcApplication.Identity::getIdentity).toList()).map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<java.lang.String> idps)->{
app.setIdentities(new HashSet<>(idps));
return app;
}))).flatMap(app->RxJava2Adapter.flowableToFlux(factorRepository.findAllByApplicationId(app.getId()).map(JdbcApplication.Factor::getFactor)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<java.lang.String> factors)->{
app.setFactors(new HashSet<>(factors));
return app;
}))).flatMap(app->RxJava2Adapter.flowableToFlux(scopeRepository.findAllByApplicationId(app.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication.ScopeSettings jdbcScopeSettings)->mapper.map(jdbcScopeSettings, ApplicationScopeSettings.class))).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<io.gravitee.am.model.application.ApplicationScopeSettings> scopeSettings)->{
if (app.getSettings() != null && app.getSettings().getOauth() != null) {
app.getSettings().getOauth().setScopeSettings(scopeSettings);
}
return app;
}))));// do not read grant tables, information already present into the settings object
    }

    @Override
    public Flowable<Application> findAll() {
        LOGGER.debug("findAll()");
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findAll()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()))));
    }

    @Override
    public Single<Page<Application>> findAll(int page, int size) {
        LOGGER.debug("findAll({}, {})", page, size);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(dbClient.select()
                .from(JdbcApplication.class)
                .page(PageRequest.of(page, size, Sort.by("id")))
                .as(JdbcApplication.class)
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .flatMap(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()), MAX_CONCURRENCY)).collectList().flatMap(data->RxJava2Adapter.singleToMono(applicationRepository.count()).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long total)->new Page<Application>(data, page, total)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("Unable to retrieve all applications (page={}/size={})", page, size, error))));
    }

    @Override
    public Flowable<Application> findByDomain(String domain) {
        LOGGER.debug("findByDomain({})",domain);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findByDomain(domain)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()))));
    }

    @Override
    public Single<Page<Application>> findByDomain(String domain, int page, int size) {
        LOGGER.debug("findByDomain({}, {}, {})", domain, page, size);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(dbClient.select()
                .from(JdbcApplication.class)
                .matching(from(where("domain").is(domain)))
                .page(PageRequest.of(page, size, Sort.by("id")))
                .as(JdbcApplication.class)
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .flatMap(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()), MAX_CONCURRENCY)).collectList().flatMap(data->RxJava2Adapter.singleToMono(applicationRepository.countByDomain(domain)).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long total)->new Page<Application>(data, page, total)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("Unable to retrieve all applications with domain {} (page={}/size={})", domain, page, size, error))));
    }

    @Override
    public Single<Page<Application>> search(String domain, String query, int page, int size) {
        LOGGER.debug("search({}, {}, {}, {})", domain, query, page, size);

        boolean wildcardMatch = query.contains("*");
        String wildcardQuery = query.replaceAll("\\*+", "%");

        String search = databaseDialectHelper.buildSearchApplicationsQuery(wildcardMatch, page, size);
        String count = databaseDialectHelper.buildCountApplicationsQuery(wildcardMatch);

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(fluxToFlowable(dbClient.execute(search)
                .bind("domain", domain)
                .bind("value", wildcardMatch ? wildcardQuery.toUpperCase() : query.toUpperCase())
                .as(JdbcApplication.class)
                .fetch()
                .all())
                .map(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> completeApplication(app).toFlowable())).collectList().flatMap(data->dbClient.execute(count).bind("domain", domain).bind("value", wildcardMatch ? wildcardQuery.toUpperCase() : query.toUpperCase()).as(Long.class).fetch().first().map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long total)->new Page<Application>(data, page, total)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("Unable to retrieve all applications with domain {} (page={}/size={})", domain, page, size, error))));
    }

    @Override
    public Flowable<Application> findByCertificate(String certificate) {
        LOGGER.debug("findByCertificate({})", certificate);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findByCertificate(certificate)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()))));
    }

    @Override
    public Flowable<Application> findByIdentityProvider(String identityProvider) {
        LOGGER.debug("findByIdentityProvider({})", identityProvider);

        // identity is a keyword with mssql
        return RxJava2Adapter.fluxToFlowable(dbClient.execute("SELECT a.* FROM applications a INNER JOIN application_identities i ON a.id = i.application_id where i." +
                databaseDialectHelper.toSql(SqlIdentifier.quoted("identity")) + " = :identity")
                .bind("identity", identityProvider).as(JdbcApplication.class).fetch().all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()))));
    }

    @Override
    public Flowable<Application> findByFactor(String factor) {
        LOGGER.debug("findByFactor({})", factor);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findAllByFactor(factor)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()))));
    }

    @Override
    public Flowable<Application> findByDomainAndExtensionGrant(String domain, String extensionGrant) {
        LOGGER.debug("findByDomainAndExtensionGrant({}, {})", domain, extensionGrant);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findAllByDomainAndGrant(domain, extensionGrant)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()))));
    }

    @Override
    public Flowable<Application> findByIdIn(List<String> ids) {
        LOGGER.debug("findByIdIn({})", ids);
        if (ids == null || ids.isEmpty()) {
            return RxJava2Adapter.fluxToFlowable(Flux.empty());
        }
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findByIdIn(ids)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()))));
    }

    @Override
    public Single<Long> count() {
        return applicationRepository.count();
    }

    @Override
    public Single<Long> countByDomain(String domain) {
        return applicationRepository.countByDomain(domain);
    }

    @Override
    public Maybe<Application> findByDomainAndClientId(String domain, String clientId) {
        LOGGER.debug("findByDomainAndClientId({}, {})", domain, clientId);
        return RxJava2Adapter.monoToMaybe(dbClient.execute(databaseDialectHelper.buildFindApplicationByDomainAndClient())
                .bind("domain", domain)
                .bind("clientId", clientId)
                .as(JdbcApplication.class)
                .fetch()
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(completeApplication(app)).flux()))).next());
    }

    @Override
    public Maybe<Application> findById(String id) {
        LOGGER.debug("findById({}", id);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(applicationRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->RxJava2Adapter.singleToMono(completeApplication(z))));
    }

    @Override
    public Single<Application> create(Application item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("Create Application with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("applications");

        // doesn't use the class introspection to handle json objects
        insertSpec = addQuotedField(insertSpec,"id", item.getId(), String.class);
        insertSpec = addQuotedField(insertSpec,"type", item.getType() == null ? null : item.getType().name(), String.class);
        insertSpec = addQuotedField(insertSpec,"enabled", item.isEnabled(), Boolean.class);
        insertSpec = addQuotedField(insertSpec,"template", item.isTemplate(), Boolean.class);
        insertSpec = addQuotedField(insertSpec,"name", item.getName(), String.class);
        insertSpec = addQuotedField(insertSpec,"description", item.getDescription(), String.class);
        insertSpec = addQuotedField(insertSpec,"domain", item.getDomain(), String.class);
        insertSpec = addQuotedField(insertSpec,"certificate", item.getCertificate(), String.class);
        insertSpec = addQuotedField(insertSpec,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);
        insertSpec = databaseDialectHelper.addJsonField(insertSpec,"metadata", item.getMetadata());
        insertSpec = databaseDialectHelper.addJsonField(insertSpec,"settings", item.getSettings());

        Mono<Integer> insertAction = insertSpec.fetch().rowsUpdated();

        insertAction = persistChildEntities(insertAction, item);

        return RxJava2Adapter.monoToSingle(insertAction.as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()));
    }

    @Override
    public Single<Application> update(Application item) {
        LOGGER.debug("Update Application with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        final DatabaseClient.GenericUpdateSpec updateSpec = dbClient.update().table("applications");

        // doesn't use the class introspection to handle json objects
        Map<SqlIdentifier, Object> updateFields = new HashMap<>();
        updateFields = addQuotedField(updateFields,"id", item.getId(), String.class);
        updateFields = addQuotedField(updateFields,"type", item.getType() == null ? null : item.getType().name(), String.class);
        updateFields = addQuotedField(updateFields,"enabled", item.isEnabled(), Boolean.class);
        updateFields = addQuotedField(updateFields,"template", item.isTemplate(), Boolean.class);
        updateFields = addQuotedField(updateFields,"name", item.getName(), String.class);
        updateFields = addQuotedField(updateFields,"description", item.getDescription(), String.class);
        updateFields = addQuotedField(updateFields,"domain", item.getDomain(), String.class);
        updateFields = addQuotedField(updateFields,"certificate", item.getCertificate(), String.class);
        updateFields = addQuotedField(updateFields,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);
        updateFields = databaseDialectHelper.addJsonField(updateFields,"metadata", item.getMetadata());
        updateFields = databaseDialectHelper.addJsonField(updateFields,"settings", item.getSettings());

        Mono<Integer> updateAction = updateSpec.using(Update.from(updateFields)).matching(from(where("id").is(item.getId()))).fetch().rowsUpdated();

        updateAction = deleteChildEntities(item.getId()).then(updateAction);
        updateAction = persistChildEntities(updateAction, item);

        return RxJava2Adapter.monoToSingle(updateAction.as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()));
    }

    @Override
    public Completable delete(String id) {
        LOGGER.debug("delete({})", id);
        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> delete = dbClient.delete().from(JdbcApplication.class).matching(from(where("id").is(id))).fetch().rowsUpdated();
        return RxJava2Adapter.monoToCompletable(delete.then(deleteChildEntities(id)).as(trx::transactional).then(RxJava2Adapter.completableToMono(applicationRepository.deleteById(id))));
    }

    private Mono<Integer> deleteChildEntities(String appId) {
        Mono<Integer> identities = dbClient.delete().from(JdbcApplication.Identity.class).matching(from(where("application_id").is(appId))).fetch().rowsUpdated();
        Mono<Integer> factors = dbClient.delete().from(JdbcApplication.Factor.class).matching(from(where("application_id").is(appId))).fetch().rowsUpdated();
        Mono<Integer> grants = dbClient.delete().from(JdbcApplication.Grant.class).matching(from(where("application_id").is(appId))).fetch().rowsUpdated();
        Mono<Integer> scopeSettings = dbClient.delete().from(JdbcApplication.ScopeSettings.class).matching(from(where("application_id").is(appId))).fetch().rowsUpdated();
        return factors.then(identities).then(grants).then(scopeSettings);
    }

    private Mono<Integer> persistChildEntities(Mono<Integer> actionFlow, Application app) {
        final Set<String> identities = app.getIdentities();
        if (identities != null && !identities.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromIterable(identities).concatMap(idp -> {
                JdbcApplication.Identity identity = new JdbcApplication.Identity();
                identity.setIdentity(idp);
                identity.setApplicationId(app.getId());

                DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("application_identities");
                insertSpec = addQuotedField(insertSpec,"application_id", identity.getApplicationId(), String.class);
                insertSpec = addQuotedField(insertSpec,"identity", identity.getIdentity(), String.class);

                return insertSpec.fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        final Set<String> factors = app.getFactors();
        if (factors != null && !factors.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromIterable(factors).concatMap(value -> {
                JdbcApplication.Factor factor = new JdbcApplication.Factor();
                factor.setFactor(value);
                factor.setApplicationId(app.getId());
                return dbClient.insert().into(JdbcApplication.Factor.class).using(factor).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        final List<String> grants = Optional.ofNullable(app.getSettings()).map(ApplicationSettings::getOauth).map(ApplicationOAuthSettings::getGrantTypes).orElse(Collections.emptyList());
        if (grants != null && !grants.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromIterable(grants).concatMap(value -> {
                JdbcApplication.Grant grant = new JdbcApplication.Grant();
                grant.setGrant(value);
                grant.setApplicationId(app.getId());
                return dbClient.insert().into(JdbcApplication.Grant.class).using(grant).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        final List<ApplicationScopeSettings> scopeSettings = Optional.ofNullable(app.getSettings()).map(ApplicationSettings::getOauth).map(ApplicationOAuthSettings::getScopeSettings).orElse(Collections.emptyList());
        if (scopeSettings != null && !scopeSettings.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromIterable(scopeSettings).concatMap(value -> {
                JdbcApplication.ScopeSettings jdbcScopeSettings = mapper.map(value, JdbcApplication.ScopeSettings.class);
                jdbcScopeSettings.setApplicationId(app.getId());
                return dbClient.insert().into(JdbcApplication.ScopeSettings.class).using(jdbcScopeSettings).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return actionFlow;
    }

}

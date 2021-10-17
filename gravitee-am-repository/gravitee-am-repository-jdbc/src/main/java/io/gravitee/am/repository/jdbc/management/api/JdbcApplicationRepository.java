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
import io.gravitee.am.model.Application;
import io.gravitee.am.model.application.ApplicationOAuthSettings;
import io.gravitee.am.model.application.ApplicationScopeSettings;
import io.gravitee.am.model.application.ApplicationSettings;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcApplication.ScopeSettings;
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
import java.util.List;
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

    
private Mono<Application> completeApplication_migrated(Application entity) {
        return Mono.just(entity).flatMap(app->identityRepository.findAllByApplicationId_migrated(app.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcApplication.Identity::getIdentity)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction((List<String> idps)->{
app.setIdentities(new HashSet<>(idps));
return app;
}))).flatMap(app->factorRepository.findAllByApplicationId_migrated(app.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcApplication.Factor::getFactor)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction((List<String> factors)->{
app.setFactors(new HashSet<>(factors));
return app;
}))).flatMap(app->scopeRepository.findAllByApplicationId_migrated(app.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction((ScopeSettings jdbcScopeSettings)->mapper.map(jdbcScopeSettings, ApplicationScopeSettings.class))).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction((List<ApplicationScopeSettings> scopeSettings)->{
if (app.getSettings() != null && app.getSettings().getOauth() != null) {
app.getSettings().getOauth().setScopeSettings(scopeSettings);
}
return app;
})));// do not read grant tables, information already present into the settings object
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Application> findAll_migrated() {
        LOGGER.debug("findAll()");
        return RxJava2Adapter.flowableToFlux(applicationRepository.findAll()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(completeApplication_migrated(app).flux())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Application>> findAll(int page, int size) {
 return RxJava2Adapter.monoToSingle(findAll_migrated(page, size));
}
@Override
    public Mono<Page<Application>> findAll_migrated(int page, int size) {
        LOGGER.debug("findAll({}, {})", page, size);
        return dbClient.select()
                .from(JdbcApplication.class)
                .page(PageRequest.of(page, size, Sort.by("id")))
                .as(JdbcApplication.class)
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> completeApplication_migrated(app).flux()), MAX_CONCURRENCY).collectList().flatMap(data->RxJava2Adapter.singleToMono(applicationRepository.count()).map(RxJavaReactorMigrationUtil.toJdkFunction((Long total)->new Page<Application>(data, page, total)))).doOnError((error) -> LOGGER.error("Unable to retrieve all applications (page={}/size={})", page, size, error));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Application> findByDomain_migrated(String domain) {
        LOGGER.debug("findByDomain({})",domain);
        return applicationRepository.findByDomain_migrated(domain).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(completeApplication_migrated(app).flux())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Application>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<Application>> findByDomain_migrated(String domain, int page, int size) {
        LOGGER.debug("findByDomain({}, {}, {})", domain, page, size);
        return dbClient.select()
                .from(JdbcApplication.class)
                .matching(from(where("domain").is(domain)))
                .page(PageRequest.of(page, size, Sort.by("id")))
                .as(JdbcApplication.class)
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> completeApplication_migrated(app).flux()), MAX_CONCURRENCY).collectList().flatMap(data->applicationRepository.countByDomain_migrated(domain).map(RxJavaReactorMigrationUtil.toJdkFunction((Long total)->new Page<Application>(data, page, total)))).doOnError((error) -> LOGGER.error("Unable to retrieve all applications with domain {} (page={}/size={})", domain, page, size, error));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Application>> search(String domain, String query, int page, int size) {
 return RxJava2Adapter.monoToSingle(search_migrated(domain, query, page, size));
}
@Override
    public Mono<Page<Application>> search_migrated(String domain, String query, int page, int size) {
        LOGGER.debug("search({}, {}, {}, {})", domain, query, page, size);

        boolean wildcardMatch = query.contains("*");
        String wildcardQuery = query.replaceAll("\\*+", "%");

        String search = databaseDialectHelper.buildSearchApplicationsQuery(wildcardMatch, page, size);
        String count = databaseDialectHelper.buildCountApplicationsQuery(wildcardMatch);

        return dbClient.execute(search)
                .bind("domain", domain)
                .bind("value", wildcardMatch ? wildcardQuery.toUpperCase() : query.toUpperCase())
                .as(JdbcApplication.class)
                .fetch()
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(completeApplication_migrated(app).flux()))).collectList().flatMap(data->dbClient.execute(count).bind("domain", domain).bind("value", wildcardMatch ? wildcardQuery.toUpperCase() : query.toUpperCase()).as(Long.class).fetch().first().map(RxJavaReactorMigrationUtil.toJdkFunction((Long total)->new Page<Application>(data, page, total)))).doOnError((error) -> LOGGER.error("Unable to retrieve all applications with domain {} (page={}/size={})", domain, page, size, error));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCertificate_migrated(certificate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByCertificate(String certificate) {
 return RxJava2Adapter.fluxToFlowable(findByCertificate_migrated(certificate));
}
@Override
    public Flux<Application> findByCertificate_migrated(String certificate) {
        LOGGER.debug("findByCertificate({})", certificate);
        return applicationRepository.findByCertificate_migrated(certificate).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(completeApplication_migrated(app).flux())));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdentityProvider_migrated(identityProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByIdentityProvider(String identityProvider) {
 return RxJava2Adapter.fluxToFlowable(findByIdentityProvider_migrated(identityProvider));
}
@Override
    public Flux<Application> findByIdentityProvider_migrated(String identityProvider) {
        LOGGER.debug("findByIdentityProvider({})", identityProvider);

        // identity is a keyword with mssql
        return dbClient.execute("SELECT a.* FROM applications a INNER JOIN application_identities i ON a.id = i.application_id where i." +
                databaseDialectHelper.toSql(SqlIdentifier.quoted("identity")) + " = :identity")
                .bind("identity", identityProvider).as(JdbcApplication.class).fetch().all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(completeApplication_migrated(app).flux())));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByFactor_migrated(factor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByFactor(String factor) {
 return RxJava2Adapter.fluxToFlowable(findByFactor_migrated(factor));
}
@Override
    public Flux<Application> findByFactor_migrated(String factor) {
        LOGGER.debug("findByFactor({})", factor);
        return applicationRepository.findAllByFactor_migrated(factor).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(completeApplication_migrated(app).flux())));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndExtensionGrant_migrated(domain, extensionGrant))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByDomainAndExtensionGrant(String domain, String extensionGrant) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndExtensionGrant_migrated(domain, extensionGrant));
}
@Override
    public Flux<Application> findByDomainAndExtensionGrant_migrated(String domain, String extensionGrant) {
        LOGGER.debug("findByDomainAndExtensionGrant({}, {})", domain, extensionGrant);
        return applicationRepository.findAllByDomainAndGrant_migrated(domain, extensionGrant).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(completeApplication_migrated(app).flux())));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByIdIn(List<String> ids) {
 return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
@Override
    public Flux<Application> findByIdIn_migrated(List<String> ids) {
        LOGGER.debug("findByIdIn({})", ids);
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }
        return applicationRepository.findByIdIn_migrated(ids).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(completeApplication_migrated(app).flux())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.count_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> count() {
 return RxJava2Adapter.monoToSingle(count_migrated());
}
@Override
    public Mono<Long> count_migrated() {
        return RxJava2Adapter.singleToMono(applicationRepository.count());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> countByDomain(String domain) {
 return RxJava2Adapter.monoToSingle(countByDomain_migrated(domain));
}
@Override
    public Mono<Long> countByDomain_migrated(String domain) {
        return applicationRepository.countByDomain_migrated(domain);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientId_migrated(domain, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Application> findByDomainAndClientId(String domain, String clientId) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndClientId_migrated(domain, clientId));
}
@Override
    public Mono<Application> findByDomainAndClientId_migrated(String domain, String clientId) {
        LOGGER.debug("findByDomainAndClientId({}, {})", domain, clientId);
        return dbClient.execute(databaseDialectHelper.buildFindApplicationByDomainAndClient())
                .bind("domain", domain)
                .bind("clientId", clientId)
                .as(JdbcApplication.class)
                .fetch()
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(app -> RxJava2Adapter.fluxToFlowable(completeApplication_migrated(app).flux()))).next();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Application> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Application> findById_migrated(String id) {
        LOGGER.debug("findById({}", id);
        return RxJava2Adapter.maybeToMono(applicationRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(this::completeApplication_migrated);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Application> create(Application item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Application> create_migrated(Application item) {
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

        return insertAction.as(trx::transactional).flatMap(i->this.findById_migrated(item.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Application> update(Application item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Application> update_migrated(Application item) {
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

        return updateAction.as(trx::transactional).flatMap(i->this.findById_migrated(item.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("delete({})", id);
        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> delete = dbClient.delete().from(JdbcApplication.class).matching(from(where("id").is(id))).fetch().rowsUpdated();
        return delete.then(deleteChildEntities(id)).as(trx::transactional).then(RxJava2Adapter.completableToMono(applicationRepository.deleteById(id)));
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

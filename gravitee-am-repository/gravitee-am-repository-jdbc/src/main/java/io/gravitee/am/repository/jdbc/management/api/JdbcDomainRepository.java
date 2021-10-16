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
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.VirtualHost;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcDomain;
import io.gravitee.am.repository.jdbc.management.api.spring.domain.SpringDomainIdentitiesRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.domain.SpringDomainRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.domain.SpringDomainTagRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.domain.SpringDomainVHostsRepository;
import io.gravitee.am.repository.management.api.DomainRepository;
import io.gravitee.am.repository.management.api.search.DomainCriteria;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.query.Criteria;
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
public class JdbcDomainRepository extends AbstractJdbcRepository implements DomainRepository {

    @Autowired
    private SpringDomainRepository domainRepository;
    @Autowired
    private SpringDomainIdentitiesRepository identitiesRepository;
    @Autowired
    private SpringDomainTagRepository tagRepository;
    @Autowired
    private SpringDomainVHostsRepository vHostsRepository;

    protected Domain toDomain(JdbcDomain entity) {
        return mapper.map(entity, Domain.class);
    }

    protected JdbcDomain toJdbcDomain(Domain entity) {
        return mapper.map(entity, JdbcDomain.class);
    }

    protected VirtualHost toVirtualHost(JdbcDomain.Vhost vhost) {
        return mapper.map(vhost, VirtualHost.class);
    }

    protected JdbcDomain.Vhost toJdbcVHost(VirtualHost entity) {
        return mapper.map(entity, JdbcDomain.Vhost.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Domain> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Domain> findAll_migrated() {
        LOGGER.debug("findAll()");
        Flowable<Domain> domains = RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(domainRepository.findAll()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toDomain)));
        return RxJava2Adapter.flowableToFlux(domains).flatMap(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.fluxToFlowable(completeDomain_migrated(ident))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByCriteria_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Domain> findAllByCriteria(DomainCriteria criteria) {
 return RxJava2Adapter.fluxToFlowable(findAllByCriteria_migrated(criteria));
}
@Override
    public Flux<Domain> findAllByCriteria_migrated(DomainCriteria criteria) {

        Criteria whereClause = Criteria.empty();
        Criteria alertEnableClause = Criteria.empty();

        if (criteria.isAlertEnabled().isPresent()) {
            alertEnableClause = where("alert_enabled").is(criteria.isAlertEnabled().get());
        }

        whereClause = whereClause.and(alertEnableClause);

        return dbClient.select()
                .from(JdbcDomain.class)
                .matching(from(whereClause))
                .as(JdbcDomain.class)
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toDomain)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.fluxToFlowable(completeDomain_migrated(ident))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Domain> findByIdIn(Collection<String> ids) {
 return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
@Override
    public Flux<Domain> findByIdIn_migrated(Collection<String> ids) {
        LOGGER.debug("findByIdIn({})", ids);
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }
        Flowable<Domain> domains = RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(domainRepository.findAllById(ids)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toDomain)));
        return RxJava2Adapter.flowableToFlux(domains).flatMap(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.fluxToFlowable(completeDomain_migrated(ident))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByReferenceId_migrated(environmentId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Domain> findAllByReferenceId(String environmentId) {
 return RxJava2Adapter.fluxToFlowable(findAllByReferenceId_migrated(environmentId));
}
@Override
    public Flux<Domain> findAllByReferenceId_migrated(String environmentId) {
        LOGGER.debug("findAllByReferenceId({})", environmentId);
        Flowable<Domain> domains = RxJava2Adapter.fluxToFlowable(domainRepository.findAllByReferenceId_migrated(environmentId, ReferenceType.ENVIRONMENT.name()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toDomain)));
        return RxJava2Adapter.flowableToFlux(domains).flatMap(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.fluxToFlowable(completeDomain_migrated(ident))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Domain> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Domain> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        Flowable<Domain> domains = RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(domainRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toDomain)).flux());
        return RxJava2Adapter.flowableToFlux(domains).flatMap(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.fluxToFlowable(completeDomain_migrated(ident)))).next();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByHrid_migrated(referenceType, referenceId, hrid))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Domain> findByHrid(ReferenceType referenceType, String referenceId, String hrid) {
 return RxJava2Adapter.monoToMaybe(findByHrid_migrated(referenceType, referenceId, hrid));
}
@Override
    public Mono<Domain> findByHrid_migrated(ReferenceType referenceType, String referenceId, String hrid) {
        LOGGER.debug("findByHrid({})", hrid);
        Flowable<Domain> domains = RxJava2Adapter.fluxToFlowable(domainRepository.findByHrid_migrated(referenceId, referenceType.name(), hrid).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toDomain)).flux());
        return RxJava2Adapter.flowableToFlux(domains).flatMap(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.fluxToFlowable(completeDomain_migrated(ident)))).next();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Domain> create(Domain item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Domain> create_migrated(Domain item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create Domain with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> insertAction = dbClient.insert()
                .into(JdbcDomain.class)
                .using(toJdbcDomain(item))
                .fetch().rowsUpdated();
        insertAction = persistChildEntities(insertAction, item);

        return insertAction
                .as(trx::transactional)
                .then(findById_migrated(item.getId()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Domain> update(Domain item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Domain> update_migrated(Domain item) {
        LOGGER.debug("update Domain with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> updateAction = dbClient.update()
                .table(JdbcDomain.class)
                .using(toJdbcDomain(item))
                .fetch().rowsUpdated();

        updateAction = updateAction.then(deleteChildEntities(item.getId()));
        updateAction = persistChildEntities(updateAction, item);

        return updateAction
                .as(trx::transactional)
                .then(findById_migrated(item.getId()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domainId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String domainId) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(domainId));
}
@Override
    public Mono<Void> delete_migrated(String domainId) {
        LOGGER.debug("delete Domain with id {}", domainId);
        TransactionalOperator trx = TransactionalOperator.create(tm);
        return dbClient.delete()
                .from(JdbcDomain.class)
                .matching(from(where("id").is(domainId)))
                .fetch().rowsUpdated()
                .then(deleteChildEntities(domainId))
                .as(trx::transactional).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("unable to delete Domain with id {}", domainId, error))).then();
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.search_migrated(environmentId, query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Domain> search(String environmentId, String query){
 return RxJava2Adapter.fluxToFlowable(search_migrated(environmentId, query));
}
@Override
    public Flux<Domain> search_migrated(String environmentId, String query){
        LOGGER.debug("search({}, {})", environmentId, query);

        boolean wildcardMatch = query.contains("*");
        String wildcardQuery = query.replaceAll("\\*+", "%");

        String search = new StringBuilder("SELECT * FROM domains d WHERE")

                .append(" d.reference_type = :refType AND d.reference_id = :refId")
                .append(" AND UPPER(d.hrid) " + (wildcardMatch ? "LIKE" : "="))
                .append(" :value")
                .toString();

        return dbClient.execute(search)
                .bind("refType", ReferenceType.ENVIRONMENT.name())
                .bind("refId", environmentId)
                .bind("value", wildcardMatch ? wildcardQuery.toUpperCase() : query.toUpperCase())
                .as(JdbcDomain.class)
                .fetch()
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toDomain)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.fluxToFlowable(completeDomain_migrated(ident))));
    }

    
private Flux<Domain> completeDomain_migrated(Domain entity) {
        return Flux.just(entity).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(domain ->
                RxJava2Adapter.fluxToFlowable(identitiesRepository.findAllByDomainId_migrated(domain.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcDomain.Identity::getIdentity)).collectList().flux().map(RxJavaReactorMigrationUtil.toJdkFunction(idps -> {
                    domain.setIdentities(new HashSet<>(idps));
                    return domain;
                }))))).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(domain ->
                RxJava2Adapter.fluxToFlowable(tagRepository.findAllByDomainId_migrated(domain.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcDomain.Tag::getTag)).collectList().flux().map(RxJavaReactorMigrationUtil.toJdkFunction(tags -> {
                    domain.setTags(new HashSet<>(tags));
                    return domain;
                }))))).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(domain ->
                RxJava2Adapter.fluxToFlowable(vHostsRepository.findAllByDomainId_migrated(domain.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toVirtualHost)).collectList().flux().map(RxJavaReactorMigrationUtil.toJdkFunction(vhosts -> {
                    domain.setVhosts(vhosts);
                    return domain;
                })))));
    }

    private Mono<Integer> persistChildEntities(Mono<Integer> actionFlow, Domain item) {
        final Set<String> identities = item.getIdentities();
        if (identities != null && !identities.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromIterable(identities).concatMap(idp -> {
                JdbcDomain.Identity identity = new JdbcDomain.Identity();
                identity.setIdentity(idp);
                identity.setDomainId(item.getId());
                return dbClient.insert().into(JdbcDomain.Identity.class).using(identity).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        final Set<String> tags = item.getTags();
        if (tags != null && !tags.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromIterable(tags).concatMap(tagValue -> {
                JdbcDomain.Tag tag = new JdbcDomain.Tag();
                tag.setTag(tagValue);
                tag.setDomainId(item.getId());
                return dbClient.insert().into(JdbcDomain.Tag.class).using(tag).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        final List<VirtualHost> virtualHosts = item.getVhosts();
        if (virtualHosts != null && !virtualHosts.isEmpty()) {
            actionFlow = actionFlow.then(Flux.fromStream(virtualHosts.stream().map(this::toJdbcVHost)).concatMap(jdbcVHost -> {
                jdbcVHost.setDomainId(item.getId());
                return dbClient.insert().into(JdbcDomain.Vhost.class).using(jdbcVHost).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return actionFlow;
    }

    private Mono<Integer> deleteChildEntities(String domainId) {
        Mono<Integer> deleteVirtualHosts = dbClient.delete().from(JdbcDomain.Vhost.class).matching(from(where("domain_id").is(domainId))).fetch().rowsUpdated();
        Mono<Integer> deleteIdentities = dbClient.delete().from(JdbcDomain.Identity.class).matching(from(where("domain_id").is(domainId))).fetch().rowsUpdated();
        Mono<Integer> deleteTags = dbClient.delete().from(JdbcDomain.Tag.class).matching(from(where("domain_id").is(domainId))).fetch().rowsUpdated();
        return deleteVirtualHosts.then(deleteIdentities).then(deleteTags);
    }
}

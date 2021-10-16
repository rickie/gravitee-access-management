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
import io.gravitee.am.model.Environment;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcEnvironment;
import io.gravitee.am.repository.jdbc.management.api.spring.environment.SpringEnvironmentDomainRestrictionRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.environment.SpringEnvironmentHridsRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.environment.SpringEnvironmentRepository;
import io.gravitee.am.repository.management.api.EnvironmentRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
public class JdbcEnvironmentRepository extends AbstractJdbcRepository implements EnvironmentRepository {

    @Autowired
    private SpringEnvironmentRepository environmentRepository;
    @Autowired
    private SpringEnvironmentDomainRestrictionRepository domainRestrictionRepository;
    @Autowired
    private SpringEnvironmentHridsRepository hridsRepository;

    protected Environment toEnvironment(JdbcEnvironment entity) {
        return mapper.map(entity, Environment.class);
    }

    protected JdbcEnvironment toJdbcEnvironment(Environment entity) {
        return mapper.map(entity, JdbcEnvironment.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Environment> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Environment> findAll_migrated() {
        LOGGER.debug("findAll()");

        final Flowable<Environment> result = RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(environmentRepository.findAll()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEnvironment)))
                .flatMapSingle((io.gravitee.am.model.Environment ident) -> RxJava2Adapter.monoToSingle(retrieveDomainRestrictions_migrated(ident)))
                .flatMapSingle((io.gravitee.am.model.Environment ident) -> RxJava2Adapter.monoToSingle(retrieveHrids_migrated(ident)));

        return RxJava2Adapter.flowableToFlux(result.doOnError((error) -> LOGGER.error("unable to retrieve all environments", error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Environment> findAll(String organizationId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
@Override
    public Flux<Environment> findAll_migrated(String organizationId) {
        LOGGER.debug("findAll({})", organizationId);

        final Flowable<Environment> result = RxJava2Adapter.fluxToFlowable(environmentRepository.findByOrganization_migrated(organizationId).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEnvironment)))
                .flatMapSingle((io.gravitee.am.model.Environment ident) -> RxJava2Adapter.monoToSingle(retrieveDomainRestrictions_migrated(ident)))
                .flatMapSingle((io.gravitee.am.model.Environment ident) -> RxJava2Adapter.monoToSingle(retrieveHrids_migrated(ident)));

        return RxJava2Adapter.flowableToFlux(result.doOnError((error) -> LOGGER.error("unable to retrieve Environments with organizationId {}", organizationId, error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Environment> findById(String id, String organizationId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id, organizationId));
}
@Override
    public Mono<Environment> findById_migrated(String id, String organizationId) {
        LOGGER.debug("findById({},{})", id, organizationId);

        return environmentRepository.findByIdAndOrganization_migrated(id, organizationId).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEnvironment)).flatMap(z->retrieveDomainRestrictions_migrated(z)).flatMap(z->retrieveHrids_migrated(z));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.count_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> count() {
 return RxJava2Adapter.monoToSingle(count_migrated());
}
@Override
    public Mono<Long> count_migrated() {
        return RxJava2Adapter.singleToMono(this.environmentRepository.count());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Environment> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Environment> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);

        Maybe<Environment> result = RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(environmentRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEnvironment)).flatMap(z->retrieveDomainRestrictions_migrated(z)).flatMap(z->retrieveHrids_migrated(z)));

        return RxJava2Adapter.maybeToMono(result).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("unable to retrieve Environment with id {}", id, error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(environment))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Environment> create(Environment environment) {
 return RxJava2Adapter.monoToSingle(create_migrated(environment));
}
@Override
    public Mono<Environment> create_migrated(Environment environment) {
        environment.setId(environment.getId() == null ? RandomString.generate() : environment.getId());
        LOGGER.debug("create Environment with id {}", environment.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Void> insert = dbClient.insert()
                .into(JdbcEnvironment.class)
                .using(toJdbcEnvironment(environment))
                .then();

        final Mono<Void> storeDomainRestrictions = storeDomainRestrictions(environment, false);
        final Mono<Void> storeHrids = storeHrids(environment, false);

        return insert
                .then(storeDomainRestrictions)
                .then(storeHrids)
                .as(trx::transactional)
                .then(findById_migrated(environment.getId()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(environment))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Environment> update(Environment environment) {
 return RxJava2Adapter.monoToSingle(update_migrated(environment));
}
@Override
    public Mono<Environment> update_migrated(Environment environment) {
        LOGGER.debug("update environment with id {}", environment.getId());
        TransactionalOperator trx = TransactionalOperator.create(tm);

        // prepare the update for environment table
        Mono<Void> update = dbClient.update()
                .table(JdbcEnvironment.class)
                .using(toJdbcEnvironment(environment))
                .matching(from(where("id").is(environment.getId()))).then();

        return update
                .then(storeDomainRestrictions(environment, true))
                .then(storeHrids(environment, true))
                .as(trx::transactional)
                .then(findById_migrated(environment.getId()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(environmentId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String environmentId) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(environmentId));
}
@Override
    public Mono<Void> delete_migrated(String environmentId) {
        LOGGER.debug("delete environment with id {}", environmentId);
        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Void> deleteDomainRestrictions = deleteDomainRestrictions(environmentId);
        Mono<Void> deleteHrids = deleteHrids(environmentId);
        Mono<Void> delete = dbClient.delete().from(JdbcEnvironment.class).matching(from(where("id").is(environmentId))).then();

        return delete
                .then(deleteDomainRestrictions)
                .then(deleteHrids)
                .as(trx::transactional);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.retrieveDomainRestrictions_migrated(environment))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Environment> retrieveDomainRestrictions(Environment environment) {
 return RxJava2Adapter.monoToSingle(retrieveDomainRestrictions_migrated(environment));
}
private Mono<Environment> retrieveDomainRestrictions_migrated(Environment environment) {
        return domainRestrictionRepository.findAllByEnvironmentId_migrated(environment.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcEnvironment.DomainRestriction::getDomainRestriction)).collectList().doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(domainRestrictions -> LOGGER.debug("findById({}) fetch {} domainRestrictions", environment.getId(), domainRestrictions.size()))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(environment::setDomainRestrictions)).map(RxJavaReactorMigrationUtil.toJdkFunction(domainRestriction -> environment));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.retrieveHrids_migrated(environment))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Environment> retrieveHrids(Environment environment) {
 return RxJava2Adapter.monoToSingle(retrieveHrids_migrated(environment));
}
private Mono<Environment> retrieveHrids_migrated(Environment environment) {
        return hridsRepository.findAllByEnvironmentId_migrated(environment.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcEnvironment.Hrid::getHrid)).collectList().doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(hrids -> LOGGER.debug("findById({}) fetch {} hrids", environment.getId(), hrids.size()))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(environment::setHrids)).map(RxJavaReactorMigrationUtil.toJdkFunction(hrids -> environment));
    }

    private Mono<Void> storeDomainRestrictions(Environment environment, boolean deleteFirst) {

        Mono<Void> delete = Mono.empty();

        if (deleteFirst) {
            delete = deleteDomainRestrictions(environment.getId());
        }

        final List<String> domainRestrictions = environment.getDomainRestrictions();
        if (domainRestrictions != null && !domainRestrictions.isEmpty()) {
            // concat flows to create domainRestrictions
            return delete.thenMany(Flux.fromIterable(domainRestrictions)
                    .map(domainRestriction -> {
                        JdbcEnvironment.DomainRestriction dbDomainRestriction = new JdbcEnvironment.DomainRestriction();
                        dbDomainRestriction.setDomainRestriction(domainRestriction);
                        dbDomainRestriction.setEnvironmentId(environment.getId());
                        return dbDomainRestriction;
                    })
                    .concatMap(dbDomainRestriction -> dbClient.insert().into(JdbcEnvironment.DomainRestriction.class).using(dbDomainRestriction).then()))
                    .ignoreElements();
        }

        return Mono.empty();
    }

    private Mono<Void> storeHrids(Environment environment, boolean deleteFirst) {

        Mono<Void> delete = Mono.empty();

        if (deleteFirst) {
            delete = deleteHrids(environment.getId());
        }

        final List<String> hrids = environment.getHrids();
        if (hrids != null && !hrids.isEmpty()) {
            final ArrayList<JdbcEnvironment.Hrid> dbHrids = new ArrayList<>();
            for (int i = 0; i < hrids.size(); i++) {
                JdbcEnvironment.Hrid hrid = new JdbcEnvironment.Hrid();
                hrid.setEnvironmentId(environment.getId());
                hrid.setHrid(hrids.get(i));
                hrid.setPos(i);
                dbHrids.add(hrid);
            }
            return delete.thenMany(Flux.fromIterable(dbHrids)).
                    concatMap(hrid -> dbClient.insert().into(JdbcEnvironment.Hrid.class).using(hrid).then())
                    .ignoreElements();
        }

        return Mono.empty();
    }

    private Mono<Void> deleteDomainRestrictions(String environmentId) {
        return dbClient.delete().from(JdbcEnvironment.DomainRestriction.class).matching(from(where("environment_id").is(environmentId))).then();
    }

    private Mono<Void> deleteHrids(String environmentId) {
        return dbClient.delete().from(JdbcEnvironment.Hrid.class).matching(from(where("environment_id").is(environmentId))).then();
    }
}

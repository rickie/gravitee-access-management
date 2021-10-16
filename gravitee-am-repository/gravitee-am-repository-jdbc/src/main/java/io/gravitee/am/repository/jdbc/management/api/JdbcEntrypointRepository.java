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
import static reactor.adapter.rxjava.RxJava2Adapter.monoToCompletable;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Entrypoint;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcEntrypoint;
import io.gravitee.am.repository.jdbc.management.api.spring.entrypoint.SpringEntrypointRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.entrypoint.SpringEntrypointTagRepository;
import io.gravitee.am.repository.management.api.EntrypointRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
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
public class JdbcEntrypointRepository extends AbstractJdbcRepository implements EntrypointRepository {
    @Autowired
    private SpringEntrypointRepository entrypointRepository;

    @Autowired
    private SpringEntrypointTagRepository tagRepository;

    protected Entrypoint toEntity(JdbcEntrypoint entity) {
        return mapper.map(entity, Entrypoint.class);
    }

    protected JdbcEntrypoint toJdbcEntity(Entrypoint entity) {
        return mapper.map(entity, JdbcEntrypoint.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Entrypoint> findById(String id, String organizationId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id, organizationId));
}
@Override
    public Mono<Entrypoint> findById_migrated(String id, String organizationId) {
        LOGGER.debug("findById({}, {})", id, organizationId);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(entrypointRepository.findById_migrated(id, organizationId))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(completeTags_migrated(z)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to retrieve entrypoint with id={} and organization={}",
                        id, organizationId, error)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Entrypoint> findAll(String organizationId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
@Override
    public Flux<Entrypoint> findAll_migrated(String organizationId) {
        LOGGER.debug("findAll({})", organizationId);

        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(entrypointRepository.findAllByOrganization_migrated(organizationId))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(entrypoint -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(completeTags_migrated(entrypoint))).flux()))))
                .doOnError(error -> LOGGER.error("Unable to list all entrypoints with organization {}", organizationId, error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.completeTags_migrated(entrypoint))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Entrypoint> completeTags(Entrypoint entrypoint) {
 return RxJava2Adapter.monoToSingle(completeTags_migrated(entrypoint));
}
private Mono<Entrypoint> completeTags_migrated(Entrypoint entrypoint) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(tagRepository.findAllByEntrypoint_migrated(entrypoint.getId()))).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcEntrypoint.Tag::getTag)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(tags -> {
                    entrypoint.setTags(tags);
                    return entrypoint;
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Entrypoint> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Entrypoint> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(entrypointRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(z->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(completeTags_migrated(z)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to retrieve entrypoint with id={} ", id, error)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Entrypoint> create(Entrypoint item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Entrypoint> create_migrated(Entrypoint item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create Entrypoint with id {}", item.getId());
        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> action = dbClient.insert()
                .into(JdbcEntrypoint.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        final List<String> tags = item.getTags();
        if (tags != null && !tags.isEmpty()) {
            action = action.then(Flux.fromIterable(tags).concatMap(tagValue -> {
                JdbcEntrypoint.Tag tag = new JdbcEntrypoint.Tag();
                tag.setTag(tagValue);
                tag.setEntrypointId(item.getId());
                return dbClient.insert().into(JdbcEntrypoint.Tag.class).using(tag).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(action.as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("unable to create entrypoint with id {}", item.getId(), error)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Entrypoint> update(Entrypoint item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Entrypoint> update_migrated(Entrypoint item) {
        LOGGER.debug("update Entrypoint with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> action = dbClient.update()
                .table(JdbcEntrypoint.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        final List<String> tags = item.getTags();
        if (tags != null & !tags.isEmpty()) {
            action = action.then(Flux.fromIterable(tags).concatMap(tagValue -> {
                JdbcEntrypoint.Tag tag = new JdbcEntrypoint.Tag();
                tag.setTag(tagValue);
                tag.setEntrypointId(item.getId());
                return dbClient.insert().into(JdbcEntrypoint.Tag.class).using(tag).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(deleteTags(item.getId()).then(action).as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("unable to create entrypoint with id {}", item.getId(), error)))));
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
        Mono<Integer> delete = dbClient.delete().from(JdbcEntrypoint.class)
                .matching(from(where("id").is(id)))
                .fetch().rowsUpdated();

        return RxJava2Adapter.completableToMono(deleteTags(id).then(delete).as(trx::transactional).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete entrypoint with id {}", id, error))).as(RxJava2Adapter::monoToCompletable));
    }

    private Mono<Integer> deleteTags(String id) {
        return dbClient.delete().from(JdbcEntrypoint.Tag.class)
                .matching(from(where("entrypoint_id").is(id)))
                .fetch().rowsUpdated();
    }
}

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

import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Reporter;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcReporter;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringReporterRepository;
import io.gravitee.am.repository.management.api.ReporterRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcReporterRepository extends AbstractJdbcRepository implements ReporterRepository {

    @Autowired
    protected SpringReporterRepository reporterRepository;

    protected Reporter toEntity(JdbcReporter entity) {
        return mapper.map(entity, Reporter.class);
    }

    protected JdbcReporter toJdbcEntity(Reporter entity) {
        return mapper.map(entity, JdbcReporter.class);
    }

    @Override
    public Flowable<Reporter> findAll() {
        LOGGER.debug("findAll()");
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(reporterRepository.findAll()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Flowable<Reporter> findByDomain(String domain) {
        LOGGER.debug("findByDomain({})", domain);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(reporterRepository.findByDomain(domain)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Deprecated
@Override
    public Maybe<Reporter> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Reporter> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(reporterRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @Deprecated
@Override
    public Single<Reporter> create(Reporter item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Reporter> create_migrated(Reporter item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("Create Reporter with id {}", item.getId());

        Mono<Integer> insertResult = dbClient.insert()
                .into(JdbcReporter.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(insertResult.flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single())));
    }

    @Deprecated
@Override
    public Single<Reporter> update(Reporter item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Reporter> update_migrated(Reporter item) {
        LOGGER.debug("Update reporter with id '{}'", item.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(reporterRepository.save(toJdbcEntity(item))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("delete({})", id);
        return RxJava2Adapter.completableToMono(reporterRepository.deleteById(id));
    }

}

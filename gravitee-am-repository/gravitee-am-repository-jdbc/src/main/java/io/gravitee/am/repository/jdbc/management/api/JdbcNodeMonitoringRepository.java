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
import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcMonitoring;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringNodeMonitoringRepository;
import io.gravitee.node.api.Monitoring;
import io.gravitee.node.api.NodeMonitoringRepository;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.Instant;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcNodeMonitoringRepository extends AbstractJdbcRepository implements NodeMonitoringRepository {

    @Autowired
    protected SpringNodeMonitoringRepository nodeMonitoringRepository;

    protected Monitoring toEntity(JdbcMonitoring entity) {
        return mapper.map(entity, Monitoring.class);
    }

    protected JdbcMonitoring toJdbcEntity(Monitoring entity) {
        return mapper.map(entity, JdbcMonitoring.class);
    }

    @Override
    public Maybe<Monitoring> findByNodeIdAndType(String nodeId, String type) {
        LOGGER.debug("findByNodeIdAndType({}, {})", nodeId, type);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(nodeMonitoringRepository.findByNodeIdAndType_migrated(nodeId, type))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Flowable<Monitoring> findByTypeAndTimeFrame(String type, long from, long to) {
        LOGGER.debug("findByTypeAndTimeFrame({}, {}, {})", type, from, to);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(nodeMonitoringRepository.findByTypeAndTimeFrame_migrated(type, LocalDateTime.ofInstant(Instant.ofEpochMilli(from), UTC), LocalDateTime.ofInstant(Instant.ofEpochMilli(to), UTC)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Single<Monitoring> create(Monitoring monitoring) {
        LOGGER.debug("Create Monitoring with id {}", monitoring.getId());
        Mono<Integer> insertResult = dbClient.insert()
                .into(JdbcMonitoring.class)
                .using(toJdbcEntity(monitoring))
                .fetch().rowsUpdated();

        return monoToSingle(insertResult.map(result -> monitoring));
    }

    @Override
    public Single<Monitoring> update(Monitoring monitoring) {
        LOGGER.debug("Update monitoring with id '{}'", monitoring.getId());
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(nodeMonitoringRepository.save(toJdbcEntity(monitoring))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }
}

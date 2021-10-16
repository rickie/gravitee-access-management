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
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.alert.AlertNotifier;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcAlertNotifier;
import io.gravitee.am.repository.jdbc.management.api.spring.alert.SpringAlertNotifierRepository;
import io.gravitee.am.repository.management.api.AlertNotifierRepository;
import io.gravitee.am.repository.management.api.search.AlertNotifierCriteria;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class JdbcAlertNotifierRepository extends AbstractJdbcRepository implements AlertNotifierRepository {

    @Autowired
    private SpringAlertNotifierRepository alertNotifierRepository;

    protected AlertNotifier toEntity(JdbcAlertNotifier alertNotifier) {
        return mapper.map(alertNotifier, AlertNotifier.class);
    }

    protected JdbcAlertNotifier toJdbcAlertNotifier(AlertNotifier entity) {
        return mapper.map(entity, JdbcAlertNotifier.class);
    }

    @Deprecated
@Override
    public Maybe<AlertNotifier> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<AlertNotifier> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(this.alertNotifierRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(alertNotifier))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AlertNotifier> create(AlertNotifier alertNotifier) {
 return RxJava2Adapter.monoToSingle(create_migrated(alertNotifier));
}
@Override
    public Mono<AlertNotifier> create_migrated(AlertNotifier alertNotifier) {
        alertNotifier.setId(alertNotifier.getId() == null ? RandomString.generate() : alertNotifier.getId());
        LOGGER.debug("create alert notifier with id {}", alertNotifier.getId());

        return RxJava2Adapter.singleToMono(monoToSingle(dbClient.insert()
                .into(JdbcAlertNotifier.class)
                .using(toJdbcAlertNotifier(alertNotifier))
                .then()
                .then(maybeToMono(findById(alertNotifier.getId())))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(alertNotifier))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AlertNotifier> update(AlertNotifier alertNotifier) {
 return RxJava2Adapter.monoToSingle(update_migrated(alertNotifier));
}
@Override
    public Mono<AlertNotifier> update_migrated(AlertNotifier alertNotifier) {
        LOGGER.debug("update alert notifier with id {}", alertNotifier.getId());

        return RxJava2Adapter.singleToMono(monoToSingle(dbClient.update()
                .table(JdbcAlertNotifier.class)
                .using(toJdbcAlertNotifier(alertNotifier))
                .matching(from(where("id").is(alertNotifier.getId()))).then()
                .then(maybeToMono(findById(alertNotifier.getId())))));
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
        return RxJava2Adapter.completableToMono(this.alertNotifierRepository.deleteById(id));
    }

    @Deprecated
@Override
    public Flowable<AlertNotifier> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<AlertNotifier> findAll_migrated(ReferenceType referenceType, String referenceId) {
        return RxJava2Adapter.flowableToFlux(findByCriteria(referenceType, referenceId, new AlertNotifierCriteria()));
    }

    @Deprecated
@Override
    public Flowable<AlertNotifier> findByCriteria(ReferenceType referenceType, String referenceId, AlertNotifierCriteria criteria) {
 return RxJava2Adapter.fluxToFlowable(findByCriteria_migrated(referenceType, referenceId, criteria));
}
@Override
    public Flux<AlertNotifier> findByCriteria_migrated(ReferenceType referenceType, String referenceId, AlertNotifierCriteria criteria) {

        Criteria whereClause = Criteria.empty();
        Criteria enableClause = Criteria.empty();
        Criteria idsClause = Criteria.empty();

        Criteria referenceClause = where("reference_id").is(referenceId).and(where("reference_type").is(referenceType.name()));

        if (criteria.isEnabled().isPresent()) {
            enableClause = where("enabled").is(criteria.isEnabled().get());
        }

        if (criteria.getIds().isPresent() && !criteria.getIds().get().isEmpty()) {
            idsClause = where("id").in(criteria.getIds().get());
        }

        whereClause = whereClause.and(referenceClause.and(criteria.isLogicalOR() ? idsClause.or(enableClause) : idsClause.and(enableClause)));

        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(dbClient.select()
                .from(JdbcAlertNotifier.class)
                .matching(from(whereClause))
                .as(JdbcAlertNotifier.class)
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .doOnError(error -> LOGGER.error("Unable to retrieve AlertNotifier with referenceId {}, referenceType {} and criteria {}",
                        referenceId, referenceType, criteria, error)));
    }
}

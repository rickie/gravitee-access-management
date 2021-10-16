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
import io.gravitee.am.model.alert.AlertTrigger;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcAlertTrigger;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcOrganization;
import io.gravitee.am.repository.jdbc.management.api.spring.alert.SpringAlertTriggerAlertNotifierRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.alert.SpringAlertTriggerRepository;
import io.gravitee.am.repository.management.api.AlertTriggerRepository;
import io.gravitee.am.repository.management.api.search.AlertTriggerCriteria;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class JdbcAlertTriggerRepository extends AbstractJdbcRepository implements AlertTriggerRepository {

    @Autowired
    private SpringAlertTriggerRepository alertTriggerRepository;

    @Autowired
    private SpringAlertTriggerAlertNotifierRepository alertTriggerAlertNotifierRepository;

    protected AlertTrigger toEntity(JdbcAlertTrigger alertTrigger) {
        AlertTrigger mapped = mapper.map(alertTrigger, AlertTrigger.class);
        if (mapped.getAlertNotifiers() == null) {
            mapped.setAlertNotifiers(new ArrayList<>());
        }
        return mapped;
    }

    protected JdbcAlertTrigger toJdbcAlertTrigger(AlertTrigger entity) {
        return mapper.map(entity, JdbcAlertTrigger.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AlertTrigger> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<AlertTrigger> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);

        Maybe<List<String>> alertNotifierIds = RxJava2Adapter.monoToMaybe(RxJava2Adapter.flowableToFlux(alertTriggerAlertNotifierRepository.findByAlertTriggerId(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcAlertTrigger.AlertNotifier::getAlertNotifierId)).collectList());

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(this.alertTriggerRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).zipWith(RxJava2Adapter.maybeToMono(alertNotifierIds), RxJavaReactorMigrationUtil.toJdkBiFunction((alertTrigger, ids) -> {
                    LOGGER.debug("findById({}) fetch {} alert triggers", alertTrigger.getId(), ids.size());
                    alertTrigger.setAlertNotifiers(ids);
                    return alertTrigger;
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(alertTrigger))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AlertTrigger> create(AlertTrigger alertTrigger) {
 return RxJava2Adapter.monoToSingle(create_migrated(alertTrigger));
}
@Override
    public Mono<AlertTrigger> create_migrated(AlertTrigger alertTrigger) {
        alertTrigger.setId(alertTrigger.getId() == null ? RandomString.generate() : alertTrigger.getId());
        LOGGER.debug("create alert trigger with id {}", alertTrigger.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Void> insert = dbClient.insert()
                .into(JdbcAlertTrigger.class)
                .using(toJdbcAlertTrigger(alertTrigger))
                .then();

        final Mono<Void> storeAlertNotifiers = storeAlertNotifiers(alertTrigger, false);

        return RxJava2Adapter.singleToMono(monoToSingle(insert
                .then(storeAlertNotifiers)
                .as(trx::transactional)
                .then(maybeToMono(findById(alertTrigger.getId())))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(alertTrigger))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AlertTrigger> update(AlertTrigger alertTrigger) {
 return RxJava2Adapter.monoToSingle(update_migrated(alertTrigger));
}
@Override
    public Mono<AlertTrigger> update_migrated(AlertTrigger alertTrigger) {
        LOGGER.debug("update alert trigger with id {}", alertTrigger.getId());
        TransactionalOperator trx = TransactionalOperator.create(tm);

        Mono<Void> update = dbClient.update()
                .table(JdbcAlertTrigger.class)
                .using(toJdbcAlertTrigger(alertTrigger))
                .matching(from(where("id").is(alertTrigger.getId()))).then();

        final Mono<Void> storeAlertNotifiers = storeAlertNotifiers(alertTrigger, true);

        return RxJava2Adapter.singleToMono(monoToSingle(update
                .then(storeAlertNotifiers)
                .as(trx::transactional)
                .then(maybeToMono(findById(alertTrigger.getId())))));
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
        return RxJava2Adapter.completableToMono(this.alertTriggerRepository.deleteById(id));
    }

    @Deprecated
@Override
    public Flowable<AlertTrigger> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<AlertTrigger> findAll_migrated(ReferenceType referenceType, String referenceId) {
        return RxJava2Adapter.flowableToFlux(findByCriteria(referenceType, referenceId, new AlertTriggerCriteria()));
    }

    @Deprecated
@Override
    public Flowable<AlertTrigger> findByCriteria(ReferenceType referenceType, String referenceId, AlertTriggerCriteria criteria) {
 return RxJava2Adapter.fluxToFlowable(findByCriteria_migrated(referenceType, referenceId, criteria));
}
@Override
    public Flux<AlertTrigger> findByCriteria_migrated(ReferenceType referenceType, String referenceId, AlertTriggerCriteria criteria) {

        Map<String, Object> params = new HashMap<>();

        final StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT t.id FROM alert_triggers t ");

        List<String> queryCriteria = new ArrayList<>();

        if (criteria.isEnabled().isPresent()) {
            queryCriteria.add("t.enabled = :enabled");
            params.put("enabled", criteria.isEnabled().get());
        }

        if (criteria.getType().isPresent()) {
            queryCriteria.add("t.type = :type");
            params.put("type", criteria.getType().get().name());
        }

        if (criteria.getAlertNotifierIds().isPresent() && !criteria.getAlertNotifierIds().get().isEmpty()) {
            // Add join when alert notifier ids are provided.
            queryBuilder.append("INNER JOIN alert_triggers_alert_notifiers n ON t.id = n.alert_trigger_id ");

            queryCriteria.add("n.alert_notifier_id IN(:alertNotifierIds)");
            params.put("alertNotifierIds", criteria.getAlertNotifierIds().get());
        }

        // Always add constraint on reference.
        queryBuilder.append("WHERE t.reference_id = :reference_id AND t.reference_type = :reference_type");

        if (!queryCriteria.isEmpty()) {
            queryBuilder.append(" AND (");
            queryBuilder.append(queryCriteria.stream().collect(Collectors.joining(criteria.isLogicalOR() ? " OR " : " AND ")));
            queryBuilder.append(")");
        }

        params.put("reference_id", referenceId);
        params.put("reference_type", referenceType.name());

        DatabaseClient.GenericExecuteSpec execute = dbClient.execute(queryBuilder.toString());

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            execute = execute.bind(entry.getKey(), entry.getValue());
        }

        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(execute
                .as(String.class)
                .fetch().all().flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<String, MaybeSource<AlertTrigger>>toJdkFunction(this::findById).apply(e)))))
                .doOnError(error -> LOGGER.error("Unable to retrieve AlertTrigger with referenceId {}, referenceType {} and criteria {}",
                        referenceId, referenceType, criteria, error)));
    }

    private Mono<Void> storeAlertNotifiers(AlertTrigger alertTrigger, boolean deleteFirst) {

        Mono<Void> delete = Mono.empty();

        if (deleteFirst) {
            delete = deleteAlertNotifiers(alertTrigger.getId());
        }

        final List<String> alertNotifiers = alertTrigger.getAlertNotifiers();
        if (alertNotifiers != null && !alertNotifiers.isEmpty()) {
            return delete.thenMany(Flux.fromIterable(alertNotifiers)
                    .map(alertNotifierId -> {
                        JdbcAlertTrigger.AlertNotifier dbAlertNotifier = new JdbcAlertTrigger.AlertNotifier();
                        dbAlertNotifier.setAlertNotifierId(alertNotifierId);
                        dbAlertNotifier.setAlertTriggerId(alertTrigger.getId());
                        return dbAlertNotifier;
                    })
                    .concatMap(dbAlertNotifier -> dbClient.insert().into(JdbcAlertTrigger.AlertNotifier.class).using(dbAlertNotifier).then()))
                    .ignoreElements();
        }

        return Mono.empty();
    }


    private Mono<Void> deleteAlertNotifiers(String alertTriggerId) {
        return dbClient.delete().from(JdbcAlertTrigger.AlertNotifier.class).matching(from(where("alert_trigger_id").is(alertTriggerId))).then();
    }

}

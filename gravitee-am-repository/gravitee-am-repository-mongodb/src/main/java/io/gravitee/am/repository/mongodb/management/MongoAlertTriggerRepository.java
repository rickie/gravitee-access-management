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
package io.gravitee.am.repository.mongodb.management;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.alert.AlertTrigger;
import io.gravitee.am.model.alert.AlertTriggerType;
import io.gravitee.am.repository.management.api.AlertTriggerRepository;
import io.gravitee.am.repository.management.api.search.AlertTriggerCriteria;
import io.gravitee.am.repository.mongodb.management.internal.model.AlertTriggerMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.bson.conversions.Bson;
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
public class MongoAlertTriggerRepository extends AbstractManagementMongoRepository implements AlertTriggerRepository {

    private MongoCollection<AlertTriggerMongo> collection;

    @PostConstruct
    public void init() {
        collection = mongoOperations.getCollection("alert_triggers", AlertTriggerMongo.class);
        super.init(collection);
    }

    @Deprecated
@Override
    public Maybe<AlertTrigger> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<AlertTrigger> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(collection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @Override
    public Flowable<AlertTrigger> findAll(ReferenceType referenceType, String referenceId) {
        Bson eqReference = and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId));

        return RxJava2Adapter.fluxToFlowable(Flux.from(collection.find(eqReference)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Flowable<AlertTrigger> findByCriteria(ReferenceType referenceType, String referenceId, AlertTriggerCriteria criteria) {
        Bson eqReference = and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId));

        List<Bson> filters = new ArrayList<>();
        if (criteria.isEnabled().isPresent()) {
            filters.add(eq("enabled", criteria.isEnabled().get()));
        }

        if (criteria.getType().isPresent()) {
            filters.add(eq("type", criteria.getType().get().name()));
        }

        if (criteria.getAlertNotifierIds().isPresent() && !criteria.getAlertNotifierIds().get().isEmpty()) {
            filters.add(in("alertNotifiers", criteria.getAlertNotifierIds().get()));
        }

        Bson query = eqReference;
        if (!filters.isEmpty()) {
            query = and(eqReference, criteria.isLogicalOR() ? or(filters) : and(filters));
        }
        return RxJava2Adapter.fluxToFlowable(Flux.from(collection.find(query)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Deprecated
@Override
    public Single<AlertTrigger> create(AlertTrigger alertTrigger) {
 return RxJava2Adapter.monoToSingle(create_migrated(alertTrigger));
}
@Override
    public Mono<AlertTrigger> create_migrated(AlertTrigger alertTrigger) {
        alertTrigger.setId(alertTrigger.getId() == null ? RandomString.generate() : alertTrigger.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(collection.insertOne(convert(alertTrigger)))).flatMap(success->RxJava2Adapter.maybeToMono(findById(alertTrigger.getId())).single())));
    }

    @Deprecated
@Override
    public Single<AlertTrigger> update(AlertTrigger alertTrigger) {
 return RxJava2Adapter.monoToSingle(update_migrated(alertTrigger));
}
@Override
    public Mono<AlertTrigger> update_migrated(AlertTrigger alertTrigger) {
        AlertTriggerMongo alertTriggerMongo = convert(alertTrigger);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(collection.replaceOne(eq(FIELD_ID, alertTriggerMongo.getId()), alertTriggerMongo))).flatMap(updateResult->RxJava2Adapter.maybeToMono(findById(alertTriggerMongo.getId())).single())));
    }

    @Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(collection.deleteOne(eq(FIELD_ID, id)))));
    }

    private AlertTrigger convert(AlertTriggerMongo alertTriggerMongo) {

        AlertTrigger alertTrigger = new AlertTrigger();
        alertTrigger.setId(alertTriggerMongo.getId());
        alertTrigger.setEnabled(alertTriggerMongo.isEnabled());
        alertTrigger.setReferenceType(alertTriggerMongo.getReferenceType() == null ? null : ReferenceType.valueOf(alertTriggerMongo.getReferenceType()));
        alertTrigger.setReferenceId(alertTriggerMongo.getReferenceId());
        alertTrigger.setType(alertTriggerMongo.getType() == null ? null : AlertTriggerType.valueOf(alertTriggerMongo.getType()));
        alertTrigger.setAlertNotifiers(alertTriggerMongo.getAlertNotifiers());
        alertTrigger.setCreatedAt(alertTriggerMongo.getCreatedAt());
        alertTrigger.setUpdatedAt(alertTriggerMongo.getUpdatedAt());

        return alertTrigger;
    }

    private AlertTriggerMongo convert(AlertTrigger alertTrigger) {

        AlertTriggerMongo alertTriggerMongo = new AlertTriggerMongo();
        alertTriggerMongo.setId(alertTrigger.getId());
        alertTriggerMongo.setEnabled(alertTrigger.isEnabled());
        alertTriggerMongo.setReferenceType(alertTrigger.getReferenceType() == null ? null : alertTrigger.getReferenceType().name());
        alertTriggerMongo.setReferenceId(alertTrigger.getReferenceId());
        alertTriggerMongo.setType(alertTrigger.getType() == null ? null : alertTrigger.getType().name());
        alertTriggerMongo.setAlertNotifiers(alertTrigger.getAlertNotifiers());
        alertTriggerMongo.setCreatedAt(alertTrigger.getCreatedAt());
        alertTriggerMongo.setUpdatedAt(alertTrigger.getUpdatedAt());

        return alertTriggerMongo;
    }
}

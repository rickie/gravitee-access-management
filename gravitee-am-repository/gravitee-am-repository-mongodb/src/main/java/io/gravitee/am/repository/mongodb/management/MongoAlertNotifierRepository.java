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
import io.gravitee.am.model.alert.AlertNotifier;
import io.gravitee.am.repository.management.api.AlertNotifierRepository;
import io.gravitee.am.repository.management.api.search.AlertNotifierCriteria;
import io.gravitee.am.repository.mongodb.management.internal.model.AlertNotifierMongo;
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
public class MongoAlertNotifierRepository extends AbstractManagementMongoRepository implements AlertNotifierRepository {

    private MongoCollection<AlertNotifierMongo> collection;

    @PostConstruct
    public void init() {
        collection = mongoOperations.getCollection("alert_notifiers", AlertNotifierMongo.class);
        super.init(collection);
    }

    @Override
    public Maybe<AlertNotifier> findById(String id) {
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(collection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Flowable<AlertNotifier> findAll(ReferenceType referenceType, String referenceId) {
        Bson eqReference = and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId));

        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(collection.find(eqReference)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Flowable<AlertNotifier> findByCriteria(ReferenceType referenceType, String referenceId, AlertNotifierCriteria criteria) {
        Bson eqReference = and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId));

        List<Bson> filters = new ArrayList<>();
        if (criteria.isEnabled().isPresent()) {
            filters.add(eq("enabled", criteria.isEnabled().get()));
        }

        if (criteria.getIds().isPresent() && !criteria.getIds().get().isEmpty()) {
            filters.add(in("_id", criteria.getIds().get()));
        }
        Bson query = eqReference;
        if (!filters.isEmpty()) {
            query = and(eqReference, criteria.isLogicalOR() ? or(filters) : and(filters));
        }
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(collection.find(and(eqReference, query))))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Single<AlertNotifier> create(AlertNotifier alertNotifier) {
        alertNotifier.setId(alertNotifier.getId() == null ? RandomString.generate() : alertNotifier.getId());
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(collection.insertOne(convert(alertNotifier)))).flatMap(success->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(findById(alertNotifier.getId())).single()))));
    }

    @Override
    public Single<AlertNotifier> update(AlertNotifier alertNotifier) {
        AlertNotifierMongo alertNotifierMongo = convert(alertNotifier);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(collection.replaceOne(eq(FIELD_ID, alertNotifierMongo.getId()), alertNotifierMongo))).flatMap(updateResult->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(findById(alertNotifierMongo.getId())).single()))));
    }

    @Override
    public Completable delete(String id) {
        return RxJava2Adapter.monoToCompletable(Mono.from(collection.deleteOne(eq(FIELD_ID, id))));
    }

    private AlertNotifier convert(AlertNotifierMongo alertNotifierMongo) {

        AlertNotifier alertNotifier = new AlertNotifier();
        alertNotifier.setId(alertNotifierMongo.getId());
        alertNotifier.setName(alertNotifierMongo.getName());
        alertNotifier.setEnabled(alertNotifierMongo.isEnabled());
        alertNotifier.setReferenceType(alertNotifierMongo.getReferenceType() == null ? null : ReferenceType.valueOf(alertNotifierMongo.getReferenceType()));
        alertNotifier.setReferenceId(alertNotifierMongo.getReferenceId());
        alertNotifier.setType(alertNotifierMongo.getType());
        alertNotifier.setConfiguration(alertNotifierMongo.getConfiguration());
        alertNotifier.setCreatedAt(alertNotifierMongo.getCreatedAt());
        alertNotifier.setUpdatedAt(alertNotifierMongo.getUpdatedAt());

        return alertNotifier;
    }

    private AlertNotifierMongo convert(AlertNotifier alertNotifier) {

        AlertNotifierMongo alertNotifierMongo = new AlertNotifierMongo();
        alertNotifierMongo.setId(alertNotifier.getId());
        alertNotifierMongo.setName(alertNotifier.getName());
        alertNotifierMongo.setEnabled(alertNotifier.isEnabled());
        alertNotifierMongo.setReferenceType(alertNotifier.getReferenceType() == null ? null : alertNotifier.getReferenceType().name());
        alertNotifierMongo.setReferenceId(alertNotifier.getReferenceId());
        alertNotifierMongo.setType(alertNotifier.getType());
        alertNotifierMongo.setConfiguration(alertNotifier.getConfiguration());
        alertNotifierMongo.setCreatedAt(alertNotifier.getCreatedAt());
        alertNotifierMongo.setUpdatedAt(alertNotifier.getUpdatedAt());

        return alertNotifierMongo;
    }
}

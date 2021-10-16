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

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.flow.Flow;
import io.gravitee.am.model.flow.Type;
import io.gravitee.am.repository.management.api.FlowRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.FlowMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoFlowRepository extends AbstractManagementMongoRepository implements FlowRepository {

    private MongoCollection<FlowMongo> flowsCollection;
    private static final String FIELD_APPLICATION = "application";

    @PostConstruct
    public void init() {
        flowsCollection = mongoOperations.getCollection("flows", FlowMongo.class);
        super.init(flowsCollection);
        super.createIndex(flowsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1));
        super.createIndex(flowsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_APPLICATION, 1));
        super.createIndex(flowsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_ID, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Flow> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<Flow> findAll_migrated(ReferenceType referenceType, String referenceId) {
        return Flux.from(flowsCollection.find(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId)
                        )
                )).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByApplication_migrated(referenceType, referenceId, application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Flow> findByApplication(ReferenceType referenceType, String referenceId, String application) {
 return RxJava2Adapter.fluxToFlowable(findByApplication_migrated(referenceType, referenceId, application));
}
@Override
    public Flux<Flow> findByApplication_migrated(ReferenceType referenceType, String referenceId, String application) {
        return Flux.from(flowsCollection.find(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_APPLICATION, application)
                        )
                )).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Flow> findById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<Flow> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(
                flowsCollection.find(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_ID, id)
                        )
                ).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Flow> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Flow> findById_migrated(String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(flowsCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Flow> create(Flow item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Flow> create_migrated(Flow item) {
        FlowMongo flow = convert(item);
        flow.setId(flow.getId() == null ? RandomString.generate() : flow.getId());
        return RxJava2Adapter.singleToMono(Single.fromPublisher(flowsCollection.insertOne(flow))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(flow.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Flow> update(Flow item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Flow> update_migrated(Flow item) {
        FlowMongo flow = convert(item);
        return RxJava2Adapter.singleToMono(Single.fromPublisher(flowsCollection.replaceOne(eq(FIELD_ID, flow.getId()), flow))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(flow.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(flowsCollection.deleteOne(eq(FIELD_ID, id)));
    }

    private FlowMongo convert(Flow flow) {
        if (flow == null) {
            return null;
        }

        FlowMongo flowMongo = new FlowMongo();
        flowMongo.setId(flow.getId());
        flowMongo.setReferenceType(flow.getReferenceType() == null ? null : flow.getReferenceType().toString());
        flowMongo.setReferenceId(flow.getReferenceId());
        flowMongo.setApplication(flow.getApplication());
        flowMongo.setName(flow.getName());
        flowMongo.setOrder(flow.getOrder());
        flowMongo.setPre(flow.getPre());
        flowMongo.setPost(flow.getPost());
        flowMongo.setEnabled(flow.isEnabled());
        flowMongo.setType(flow.getType() == null ? null : flow.getType().toString());
        flowMongo.setCondition(flow.getCondition());
        flowMongo.setCreatedAt(flow.getCreatedAt());
        flowMongo.setUpdatedAt(flow.getUpdatedAt());
        return flowMongo;
    }

    private Flow convert(FlowMongo flowMongo) {
        if (flowMongo == null) {
            return null;
        }

        Flow flow = new Flow();
        flow.setId(flowMongo.getId());
        flow.setReferenceType(flowMongo.getReferenceType() == null ? null : ReferenceType.valueOf(flowMongo.getReferenceType()));
        flow.setReferenceId(flowMongo.getReferenceId());
        flow.setApplication(flowMongo.getApplication());
        flow.setName(flowMongo.getName());
        flow.setOrder(flowMongo.getOrder());
        flow.setPre(flowMongo.getPre());
        flow.setPost(flowMongo.getPost());
        flow.setEnabled(flowMongo.isEnabled());
        flow.setType(flowMongo.getType() == null ? null : Type.valueOf(flowMongo.getType()));
        flow.setCondition(flowMongo.getCondition());
        flow.setCreatedAt(flowMongo.getCreatedAt());
        flow.setUpdatedAt(flowMongo.getUpdatedAt());

        return flow;
    }

}

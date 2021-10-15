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

import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.SystemTask;
import io.gravitee.am.repository.management.api.SystemTaskRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.SystemTaskMongo;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoSystemTaskRepository extends AbstractManagementMongoRepository implements SystemTaskRepository {
    protected static final String FIELD_OPERATION_ID = "operationId";
    private MongoCollection<SystemTaskMongo> systemTaskCollection;

    @PostConstruct
    public void init() {
        systemTaskCollection = mongoOperations.getCollection("system_tasks", SystemTaskMongo.class);
        super.init(systemTaskCollection);
    }

    @Override
    public Maybe<SystemTask> findById(String id) {
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(Observable.fromPublisher(systemTaskCollection.find(eq(FIELD_ID, id)).first()).firstElement()).map(RxJavaReactorMigrationUtil.toJdkFunction(SystemTaskMongo::convert)));
    }

    @Override
    public Single<SystemTask> create(SystemTask item) {
        SystemTaskMongo task = SystemTaskMongo.convert(item);
        task.setId(task.getId() == null ? RandomString.generate() : task.getId());
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(systemTaskCollection.insertOne(task))).flatMap(success->RxJava2Adapter.singleToMono(findById(task.getId()).toSingle())));
    }

    @Override
    public Single<SystemTask> update(SystemTask item) {
        return RxJava2Adapter.monoToSingle(Mono.error(new IllegalStateException("SystemTask can't be updated without control on the operationId")));
    }

    @Override
    public Single<SystemTask> updateIf(SystemTask item, String operationId) {
        SystemTaskMongo task = SystemTaskMongo.convert(item);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(systemTaskCollection.replaceOne(and(eq(FIELD_ID, task.getId()), eq(FIELD_OPERATION_ID, operationId)), task))).flatMap(updateResult->RxJava2Adapter.singleToMono(findById(task.getId()).toSingle())));
    }

    @Override
    public Completable delete(String id) {
        return RxJava2Adapter.monoToCompletable(Mono.from(systemTaskCollection.deleteOne(eq(FIELD_ID, id))));
    }
}

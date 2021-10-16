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

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.policy.ExtensionPoint;
import io.gravitee.am.model.Policy;
import io.gravitee.am.repository.management.api.PolicyRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.PolicyMongo;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoPolicyRepository extends AbstractManagementMongoRepository implements PolicyRepository {

    public static final String COLLECTION_NAME = "policies";

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Policy> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Policy> findAll_migrated() {
        MongoCollection<PolicyMongo> policiesCollection = mongoOperations.getCollection(COLLECTION_NAME, PolicyMongo.class);
        return Flux.from(policiesCollection.find()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.collectionExists_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Boolean> collectionExists() {
 return RxJava2Adapter.monoToSingle(collectionExists_migrated());
}
@Override
    public Mono<Boolean> collectionExists_migrated() {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToObservable(RxJava2Adapter.observableToFlux(Observable.fromPublisher(mongoOperations.listCollectionNames()), BackpressureStrategy.BUFFER).filter(RxJavaReactorMigrationUtil.toJdkPredicate(collectionName -> collectionName.equalsIgnoreCase(COLLECTION_NAME))))
                .isEmpty()).map(RxJavaReactorMigrationUtil.toJdkFunction(isEmpty -> !isEmpty));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteCollection_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteCollection() {
 return RxJava2Adapter.monoToCompletable(deleteCollection_migrated());
}
@Override
    public Mono<Void> deleteCollection_migrated() {
        return Mono.from(mongoOperations.getCollection(COLLECTION_NAME).drop());
    }

    private Policy convert(PolicyMongo policyMongo) {
        if (policyMongo == null) {
            return null;
        }

        Policy policy = new Policy();
        policy.setId(policyMongo.getId());
        policy.setEnabled(policyMongo.isEnabled());
        policy.setName(policyMongo.getName());
        policy.setType(policyMongo.getType());
        policy.setExtensionPoint(ExtensionPoint.valueOf(policyMongo.getExtensionPoint()));
        policy.setOrder(policyMongo.getOrder());
        policy.setConfiguration(policyMongo.getConfiguration());
        policy.setDomain(policyMongo.getDomain());
        policy.setClient(policyMongo.getClient());
        policy.setCreatedAt(policyMongo.getCreatedAt());
        policy.setUpdatedAt(policyMongo.getUpdatedAt());
        return policy;
    }

}

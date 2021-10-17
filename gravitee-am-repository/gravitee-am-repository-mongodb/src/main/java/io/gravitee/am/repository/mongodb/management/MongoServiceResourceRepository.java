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
import io.gravitee.am.model.resource.ServiceResource;
import io.gravitee.am.repository.management.api.ServiceResourceRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.ServiceResourceMongo;
import io.reactivex.*;

import javax.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoServiceResourceRepository extends AbstractManagementMongoRepository implements ServiceResourceRepository {

    private MongoCollection<ServiceResourceMongo> resourceCollection;

    @PostConstruct
    public void init() {
        resourceCollection = mongoOperations.getCollection("service_resources", ServiceResourceMongo.class);
        super.init(resourceCollection);
        super.createIndex(resourceCollection,new Document(FIELD_REFERENCE_ID, 1).append(FIELD_REFERENCE_TYPE, 1));
    }

    
@Override
    public Flux<ServiceResource> findByReference_migrated(ReferenceType referenceType, String referenceId) {
        return Flux.from(resourceCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ServiceResource> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<ServiceResource> findById_migrated(String id) {
        return Flux.from(resourceCollection.find(eq(FIELD_ID, id)).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ServiceResource> create(ServiceResource item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<ServiceResource> create_migrated(ServiceResource item) {
        ServiceResourceMongo res = convert(item);
        res.setId(res.getId() == null ? RandomString.generate() : res.getId());
        return Mono.from(resourceCollection.insertOne(res)).flatMap(success->findById_migrated(res.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ServiceResource> update(ServiceResource item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<ServiceResource> update_migrated(ServiceResource item) {
        ServiceResourceMongo authenticator = convert(item);
        return Mono.from(resourceCollection.replaceOne(eq(FIELD_ID, authenticator.getId()), authenticator)).flatMap(updateResult->findById_migrated(authenticator.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(resourceCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    private ServiceResource convert(ServiceResourceMongo resMongo) {
        if (resMongo == null) {
            return null;
        }

        ServiceResource res = new ServiceResource();
        res.setId(resMongo.getId());
        res.setName(resMongo.getName());
        res.setType(resMongo.getType());
        res.setConfiguration(resMongo.getConfiguration());
        res.setReferenceId(resMongo.getReferenceId());
        res.setReferenceType(ReferenceType.valueOf(resMongo.getReferenceType()));
        res.setCreatedAt(resMongo.getCreatedAt());
        res.setUpdatedAt(resMongo.getUpdatedAt());
        return res;
    }

    private ServiceResourceMongo convert(ServiceResource res) {
        if (res == null) {
            return null;
        }

        ServiceResourceMongo resMongo = new ServiceResourceMongo();
        resMongo.setId(res.getId());
        resMongo.setName(res.getName());
        resMongo.setType(res.getType());
        resMongo.setConfiguration(res.getConfiguration());
        resMongo.setReferenceId(res.getReferenceId());
        resMongo.setReferenceType(res.getReferenceType().name());
        resMongo.setCreatedAt(res.getCreatedAt());
        resMongo.setUpdatedAt(res.getUpdatedAt());
        return resMongo;
    }
}

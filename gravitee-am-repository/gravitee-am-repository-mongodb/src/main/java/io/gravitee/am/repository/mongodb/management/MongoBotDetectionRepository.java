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
import io.gravitee.am.model.BotDetection;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.management.api.BotDetectionRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.BotDetectionMongo;
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
public class MongoBotDetectionRepository extends AbstractManagementMongoRepository implements BotDetectionRepository {

    public static final String COLLECTION_NAME = "bot_detections";
    private MongoCollection<BotDetectionMongo> botDetectionMongoCollection;

    @PostConstruct
    public void init() {
        botDetectionMongoCollection = mongoOperations.getCollection(COLLECTION_NAME, BotDetectionMongo.class);
        super.init(botDetectionMongoCollection);
        super.createIndex(botDetectionMongoCollection,new Document(FIELD_REFERENCE_ID, 1).append(FIELD_REFERENCE_TYPE, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<BotDetection> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<BotDetection> findAll_migrated() {
        return Flux.from(botDetectionMongoCollection.find()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReference_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<BotDetection> findByReference(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findByReference_migrated(referenceType, referenceId));
}
@Override
    public Flux<BotDetection> findByReference_migrated(ReferenceType referenceType, String referenceId) {
        return Flux.from(botDetectionMongoCollection.find(and(eq(FIELD_REFERENCE_ID, referenceId), eq(FIELD_REFERENCE_TYPE, referenceType.name())))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(botDetectionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<BotDetection> findById(String botDetectionId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(botDetectionId));
}
@Override
    public Mono<BotDetection> findById_migrated(String botDetectionId) {
        return Flux.from(botDetectionMongoCollection.find(eq(FIELD_ID, botDetectionId)).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<BotDetection> create(BotDetection item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<BotDetection> create_migrated(BotDetection item) {
        BotDetectionMongo entity = convert(item);
        entity.setId(entity.getId() == null ? RandomString.generate() : entity.getId());
        return Mono.from(botDetectionMongoCollection.insertOne(entity)).flatMap(success->findById_migrated(entity.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<BotDetection> update(BotDetection item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<BotDetection> update_migrated(BotDetection item) {
        BotDetectionMongo entity = convert(item);
        return Mono.from(botDetectionMongoCollection.replaceOne(eq(FIELD_ID, entity.getId()), entity)).flatMap(updateResult->findById_migrated(entity.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(botDetectionMongoCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    private BotDetection convert(BotDetectionMongo entity) {
        if (entity == null) {
            return null;
        }

        BotDetection bean = new BotDetection();
        bean.setId(entity.getId());
        bean.setName(entity.getName());
        bean.setType(entity.getType());
        bean.setDetectionType(entity.getDetectionType());
        bean.setConfiguration(entity.getConfiguration());
        bean.setReferenceId(entity.getReferenceId());
        bean.setReferenceType(ReferenceType.valueOf(entity.getReferenceType()));
        bean.setCreatedAt(entity.getCreatedAt());
        bean.setUpdatedAt(entity.getUpdatedAt());
        return bean;
    }

    private BotDetectionMongo convert(BotDetection bean) {
        if (bean == null) {
            return null;
        }

        BotDetectionMongo entity = new BotDetectionMongo();
        entity.setId(bean.getId());
        entity.setName(bean.getName());
        entity.setType(bean.getType());
        entity.setDetectionType(bean.getDetectionType());
        entity.setConfiguration(bean.getConfiguration());
        entity.setReferenceType(bean.getReferenceType().name());
        entity.setReferenceId(bean.getReferenceId());
        entity.setCreatedAt(bean.getCreatedAt());
        entity.setUpdatedAt(bean.getUpdatedAt());
        return entity;
    }
}

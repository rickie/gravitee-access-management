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
import io.gravitee.am.model.Entrypoint;
import io.gravitee.am.repository.management.api.EntrypointRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.EntrypointMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import javax.annotation.PostConstruct;
import org.bson.Document;
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
public class MongoEntrypointRepository extends AbstractManagementMongoRepository implements EntrypointRepository {

    private MongoCollection<EntrypointMongo> collection;

    @PostConstruct
    public void init() {
        collection = mongoOperations.getCollection("entrypoints", EntrypointMongo.class);
        super.init(collection);
        super.createIndex(collection, new Document(FIELD_ID, 1).append(FIELD_ORGANIZATION_ID, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Entrypoint> findById(String id, String organizationId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id, organizationId));
}
@Override
    public Mono<Entrypoint> findById_migrated(String id, String organizationId) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(collection.find(and(eq(FIELD_ID, id), eq(FIELD_ORGANIZATION_ID, organizationId))).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Entrypoint> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Entrypoint> findById_migrated(String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(collection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Entrypoint> findAll(String organizationId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
@Override
    public Flux<Entrypoint> findAll_migrated(String organizationId) {
        return Flux.from(collection.find(eq("organizationId", organizationId))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Entrypoint> create(Entrypoint item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Entrypoint> create_migrated(Entrypoint item) {
        EntrypointMongo entrypoint = convert(item);
        entrypoint.setId(entrypoint.getId() == null ? RandomString.generate() : entrypoint.getId());
        return RxJava2Adapter.singleToMono(Single.fromPublisher(collection.insertOne(entrypoint))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(entrypoint.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Entrypoint> update(Entrypoint item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Entrypoint> update_migrated(Entrypoint item) {
        EntrypointMongo entrypoint = convert(item);
        return RxJava2Adapter.singleToMono(Single.fromPublisher(collection.replaceOne(eq(FIELD_ID, entrypoint.getId()), entrypoint))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(entrypoint.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(collection.deleteOne(eq(FIELD_ID, id))).then();
    }

    private Entrypoint convert(EntrypointMongo entrypointMongo) {
        if (entrypointMongo == null) {
            return null;
        }

        Entrypoint entrypoint = new Entrypoint();
        entrypoint.setId(entrypointMongo.getId());
        entrypoint.setName(entrypointMongo.getName());
        entrypoint.setDescription(entrypointMongo.getDescription());
        entrypoint.setUrl(entrypointMongo.getUrl());
        entrypoint.setTags(entrypointMongo.getTags());
        entrypoint.setOrganizationId(entrypointMongo.getOrganizationId());
        entrypoint.setDefaultEntrypoint(entrypointMongo.isDefaultEntrypoint());
        entrypoint.setCreatedAt(entrypointMongo.getCreatedAt());
        entrypoint.setUpdatedAt(entrypointMongo.getUpdatedAt());

        return entrypoint;
    }

    private EntrypointMongo convert(Entrypoint entrypoint) {
        if (entrypoint == null) {
            return null;
        }

        EntrypointMongo entrypointMongo = new EntrypointMongo();
        entrypointMongo.setId(entrypoint.getId());
        entrypointMongo.setName(entrypoint.getName());
        entrypointMongo.setDescription(entrypoint.getDescription());
        entrypointMongo.setUrl(entrypoint.getUrl());
        entrypointMongo.setTags(entrypoint.getTags());
        entrypointMongo.setOrganizationId(entrypoint.getOrganizationId());
        entrypointMongo.setDefaultEntrypoint(entrypoint.isDefaultEntrypoint());
        entrypointMongo.setCreatedAt(entrypoint.getCreatedAt());
        entrypointMongo.setUpdatedAt(entrypoint.getUpdatedAt());

        return entrypointMongo;
    }
}

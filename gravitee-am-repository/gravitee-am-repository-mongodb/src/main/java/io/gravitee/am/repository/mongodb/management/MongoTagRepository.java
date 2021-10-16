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
import io.gravitee.am.model.Tag;
import io.gravitee.am.repository.management.api.TagRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.TagMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoTagRepository extends AbstractManagementMongoRepository implements TagRepository {

    private MongoCollection<TagMongo> tagsCollection;

    @PostConstruct
    public void init() {
        tagsCollection = mongoOperations.getCollection("tags", TagMongo.class);
        super.init(tagsCollection);
        super.createIndex(tagsCollection, new Document(FIELD_ID, 1).append(FIELD_ORGANIZATION_ID, 1));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Tag> findById(String id, String organizationId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id, organizationId));
}
@Override
    public Mono<Tag> findById_migrated(String id, String organizationId) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(tagsCollection.find(and(eq(FIELD_ID, id), eq(FIELD_ORGANIZATION_ID, organizationId))).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Tag> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Tag> findById_migrated(String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(tagsCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Tag> findAll(String organizationId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
@Override
    public Flux<Tag> findAll_migrated(String organizationId) {
        return Flux.from(tagsCollection.find(eq(FIELD_ORGANIZATION_ID, organizationId))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Tag> create(Tag item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Tag> create_migrated(Tag item) {
        TagMongo tag = convert(item);
        tag.setId(tag.getId() == null ? RandomString.generate() : tag.getId());
        return RxJava2Adapter.singleToMono(Single.fromPublisher(tagsCollection.insertOne(tag))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(tag.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Tag> update(Tag item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Tag> update_migrated(Tag item) {
        TagMongo tag = convert(item);
        return RxJava2Adapter.singleToMono(Single.fromPublisher(tagsCollection.replaceOne(eq(FIELD_ID, tag.getId()), tag))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(tag.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(tagsCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    private Tag convert(TagMongo tagMongo) {
        if (tagMongo == null) {
            return null;
        }

        Tag tag = new Tag();
        tag.setId(tagMongo.getId());
        tag.setName(tagMongo.getName());
        tag.setDescription(tagMongo.getDescription());
        tag.setOrganizationId(tagMongo.getOrganizationId());
        tag.setCreatedAt(tagMongo.getCreatedAt());
        tag.setUpdatedAt(tagMongo.getUpdatedAt());

        return tag;
    }

    private TagMongo convert(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagMongo tagMongo = new TagMongo();
        tagMongo.setId(tag.getId());
        tagMongo.setName(tag.getName());
        tagMongo.setDescription(tag.getDescription());
        tagMongo.setOrganizationId(tag.getOrganizationId());
        tagMongo.setCreatedAt(tag.getCreatedAt());
        tagMongo.setUpdatedAt(tag.getUpdatedAt());

        return tagMongo;
    }
}

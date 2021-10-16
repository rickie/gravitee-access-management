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
import static io.gravitee.am.model.ReferenceType.DOMAIN;

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.BasicDBObject;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Group;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.management.api.GroupRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.GroupMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.bson.Document;
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
public class MongoGroupRepository extends AbstractManagementMongoRepository implements GroupRepository {

    private static final String FIELD_MEMBERS = "members";
    private MongoCollection<GroupMongo> groupsCollection;

    @PostConstruct
    public void init() {
        groupsCollection = mongoOperations.getCollection("groups", GroupMongo.class);
        super.init(groupsCollection);
        super.createIndex(groupsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1));
        super.createIndex(groupsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_NAME, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByMember_migrated(memberId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Group> findByMember(String memberId) {
 return RxJava2Adapter.fluxToFlowable(findByMember_migrated(memberId));
}
@Override
    public Flux<Group> findByMember_migrated(String memberId) {
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(groupsCollection.find(eq(FIELD_MEMBERS, memberId))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Group> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<Group> findAll_migrated(ReferenceType referenceType, String referenceId) {
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(groupsCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(referenceType, referenceId, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Group>> findAll(ReferenceType referenceType, String referenceId, int page, int size) {
 return RxJava2Adapter.monoToSingle(findAll_migrated(referenceType, referenceId, page, size));
}
@Override
    public Mono<Page<Group>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size) {
        Single<Long> countOperation = Observable.fromPublisher(groupsCollection.countDocuments(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId)))).first(0l);
        Single<List<Group>> groupsOperation = Observable.fromPublisher(groupsCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId))).sort(new BasicDBObject(FIELD_NAME, 1)).skip(size * page).limit(size)).map(this::convert).collect(LinkedList::new, List::add);
        return RxJava2Adapter.singleToMono(Single.zip(countOperation, groupsOperation, (count, groups) -> new Page<>(groups, page, count)));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Group> findByIdIn(List<String> ids) {
 return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
@Override
    public Flux<Group> findByIdIn_migrated(List<String> ids) {
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(groupsCollection.find(in(FIELD_ID, ids))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByName_migrated(referenceType, referenceId, groupName))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Group> findByName(ReferenceType referenceType, String referenceId, String groupName) {
 return RxJava2Adapter.monoToMaybe(findByName_migrated(referenceType, referenceId, groupName));
}
@Override
    public Mono<Group> findByName_migrated(ReferenceType referenceType, String referenceId, String groupName) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(
                groupsCollection
                        .find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId), eq(FIELD_NAME, groupName)))
                        .limit(1)
                        .first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, group))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Group> findById(ReferenceType referenceType, String referenceId, String group) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, group));
}
@Override
    public Mono<Group> findById_migrated(ReferenceType referenceType, String referenceId, String group) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(groupsCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId), eq(FIELD_ID, group))).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(group))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Group> findById(String group) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(group));
}
@Override
    public Mono<Group> findById_migrated(String group) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(groupsCollection.find(eq(FIELD_ID, group)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> create(Group item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Group> create_migrated(Group item) {
        GroupMongo group = convert(item);
        group.setId(group.getId() == null ? RandomString.generate() : group.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(groupsCollection.insertOne(group))).flatMap(success->RxJava2Adapter.maybeToMono(findById(group.getId())).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> update(Group item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Group> update_migrated(Group item) {
        GroupMongo group = convert(item);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(groupsCollection.replaceOne(eq(FIELD_ID, group.getId()), group))).flatMap(success->RxJava2Adapter.maybeToMono(findById(group.getId())).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(groupsCollection.deleteOne(eq(FIELD_ID, id)))));
    }

    private Group convert(GroupMongo groupMongo) {
        if (groupMongo == null) {
            return null;
        }
        Group group = new Group();
        group.setId(groupMongo.getId());
        group.setReferenceType(ReferenceType.valueOf(groupMongo.getReferenceType()));
        group.setReferenceId(groupMongo.getReferenceId());
        group.setName(groupMongo.getName());
        group.setDescription(groupMongo.getDescription());
        group.setMembers(groupMongo.getMembers());
        group.setRoles(groupMongo.getRoles());
        group.setCreatedAt(groupMongo.getCreatedAt());
        group.setUpdatedAt(groupMongo.getUpdatedAt());
        return group;
    }

    private GroupMongo convert(Group group) {
        if (group == null) {
            return null;
        }
        GroupMongo groupMongo = new GroupMongo();
        groupMongo.setId(group.getId());
        groupMongo.setReferenceType(group.getReferenceType().name());
        groupMongo.setReferenceId(group.getReferenceId());
        groupMongo.setName(group.getName());
        groupMongo.setDescription(group.getDescription());
        groupMongo.setMembers(group.getMembers());
        groupMongo.setRoles(group.getRoles());
        groupMongo.setCreatedAt(group.getCreatedAt());
        groupMongo.setUpdatedAt(group.getUpdatedAt());
        return groupMongo;
    }
}

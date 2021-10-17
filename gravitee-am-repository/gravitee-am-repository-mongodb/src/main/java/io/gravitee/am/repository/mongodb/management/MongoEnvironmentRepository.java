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
import io.gravitee.am.model.Environment;
import io.gravitee.am.repository.management.api.EnvironmentRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.EnvironmentMongo;
import io.reactivex.*;

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
public class MongoEnvironmentRepository extends AbstractManagementMongoRepository implements EnvironmentRepository {

    private MongoCollection<EnvironmentMongo> collection;

    @PostConstruct
    public void init() {
        collection = mongoOperations.getCollection("environments", EnvironmentMongo.class);
        super.init(collection);
        super.createIndex(collection, new Document(FIELD_ID, 1).append(FIELD_ORGANIZATION_ID, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Environment> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Environment> findAll_migrated() {

        return Flux.from(collection.find()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Environment> findAll(String organizationId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
@Override
    public Flux<Environment> findAll_migrated(String organizationId) {

        return Flux.from(collection.find(eq(FIELD_ORGANIZATION_ID, organizationId))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Environment> findById(String id, String organizationId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id, organizationId));
}
@Override
    public Mono<Environment> findById_migrated(String id, String organizationId) {

        return Flux.from(collection.find(and(eq(FIELD_ID, id), eq(FIELD_ORGANIZATION_ID, organizationId))).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Environment> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Environment> findById_migrated(String id) {

        return Flux.from(collection.find(eq(FIELD_ID, id)).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(environment))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Environment> create(Environment environment) {
 return RxJava2Adapter.monoToSingle(create_migrated(environment));
}
@Override
    public Mono<Environment> create_migrated(Environment environment) {

        environment.setId(environment.getId() == null ? RandomString.generate() : environment.getId());

        return Mono.from(collection.insertOne(convert(environment))).flatMap(success->findById_migrated(environment.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(environment))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Environment> update(Environment environment) {
 return RxJava2Adapter.monoToSingle(update_migrated(environment));
}
@Override
    public Mono<Environment> update_migrated(Environment environment) {

        return Mono.from(collection.replaceOne(eq(FIELD_ID, environment.getId()), convert(environment))).flatMap(updateResult->findById_migrated(environment.getId()).single());
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

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.count_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> count() {
 return RxJava2Adapter.monoToSingle(count_migrated());
}
@Override
    public Mono<Long> count_migrated() {

        return Mono.from(collection.countDocuments());
    }

    private Environment convert(EnvironmentMongo environmentMongo) {

        Environment environment = new Environment();
        environment.setId(environmentMongo.getId());
        environment.setHrids(environmentMongo.getHrids());
        environment.setDescription(environmentMongo.getDescription());
        environment.setName(environmentMongo.getName());
        environment.setOrganizationId(environmentMongo.getOrganizationId());
        environment.setDomainRestrictions(environmentMongo.getDomainRestrictions());
        environment.setCreatedAt(environmentMongo.getCreatedAt());
        environment.setUpdatedAt(environmentMongo.getUpdatedAt());

        return environment;
    }

    private EnvironmentMongo convert(Environment environment) {

        EnvironmentMongo environmentMongo = new EnvironmentMongo();
        environmentMongo.setId(environment.getId());
        environmentMongo.setHrids(environment.getHrids());
        environmentMongo.setDescription(environment.getDescription());
        environmentMongo.setName(environment.getName());
        environmentMongo.setOrganizationId(environment.getOrganizationId());
        environmentMongo.setDomainRestrictions(environment.getDomainRestrictions());
        environmentMongo.setCreatedAt(environment.getCreatedAt());
        environmentMongo.setUpdatedAt(environment.getUpdatedAt());

        return environmentMongo;
    }
}

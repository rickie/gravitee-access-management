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
import io.gravitee.am.model.ExtensionGrant;
import io.gravitee.am.repository.management.api.ExtensionGrantRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.ExtensionGrantMongo;
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
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoExtensionGrantRepository extends AbstractManagementMongoRepository implements ExtensionGrantRepository {

    private MongoCollection<ExtensionGrantMongo> extensionGrantsCollection;

    @PostConstruct
    public void init() {
        extensionGrantsCollection = mongoOperations.getCollection("extension_grants", ExtensionGrantMongo.class);
        super.init(extensionGrantsCollection);
        super.createIndex(extensionGrantsCollection, new Document(FIELD_DOMAIN, 1));
        super.createIndex(extensionGrantsCollection,new Document(FIELD_DOMAIN, 1).append(FIELD_NAME, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<ExtensionGrant> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<ExtensionGrant> findByDomain_migrated(String domain) {
        return Flux.from(extensionGrantsCollection.find(eq(FIELD_DOMAIN, domain))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndName_migrated(domain, name))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ExtensionGrant> findByDomainAndName(String domain, String name) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndName_migrated(domain, name));
}
@Override
    public Mono<ExtensionGrant> findByDomainAndName_migrated(String domain, String name) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(extensionGrantsCollection.find(and(eq(FIELD_DOMAIN, domain), eq(FIELD_NAME, name))).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(tokenGranterId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ExtensionGrant> findById(String tokenGranterId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(tokenGranterId));
}
@Override
    public Mono<ExtensionGrant> findById_migrated(String tokenGranterId) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(extensionGrantsCollection.find(eq(FIELD_ID, tokenGranterId)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ExtensionGrant> create(ExtensionGrant item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<ExtensionGrant> create_migrated(ExtensionGrant item) {
        ExtensionGrantMongo extensionGrant = convert(item);
        extensionGrant.setId(extensionGrant.getId() == null ? RandomString.generate() : extensionGrant.getId());
        return RxJava2Adapter.singleToMono(Single.fromPublisher(extensionGrantsCollection.insertOne(extensionGrant))).flatMap(success->findById_migrated(extensionGrant.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ExtensionGrant> update(ExtensionGrant item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<ExtensionGrant> update_migrated(ExtensionGrant item) {
        ExtensionGrantMongo extensionGrant = convert(item);
        return RxJava2Adapter.singleToMono(Single.fromPublisher(extensionGrantsCollection.replaceOne(eq(FIELD_ID, extensionGrant.getId()), extensionGrant))).flatMap(updateResult->findById_migrated(extensionGrant.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(extensionGrantsCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    private ExtensionGrant convert(ExtensionGrantMongo extensionGrantMongo) {
        if (extensionGrantMongo == null) {
            return null;
        }

        ExtensionGrant extensionGrant = new ExtensionGrant();
        extensionGrant.setId(extensionGrantMongo.getId());
        extensionGrant.setName(extensionGrantMongo.getName());
        extensionGrant.setType(extensionGrantMongo.getType());
        extensionGrant.setConfiguration(extensionGrantMongo.getConfiguration());
        extensionGrant.setDomain(extensionGrantMongo.getDomain());
        extensionGrant.setGrantType(extensionGrantMongo.getGrantType());
        extensionGrant.setIdentityProvider(extensionGrantMongo.getIdentityProvider());
        extensionGrant.setCreateUser(extensionGrantMongo.isCreateUser());
        extensionGrant.setUserExists(extensionGrantMongo.isUserExists());
        extensionGrant.setCreatedAt(extensionGrantMongo.getCreatedAt());
        extensionGrant.setUpdatedAt(extensionGrantMongo.getUpdatedAt());
        return extensionGrant;
    }

    private ExtensionGrantMongo convert(ExtensionGrant extensionGrant) {
        if (extensionGrant == null) {
            return null;
        }

        ExtensionGrantMongo extensionGrantMongo = new ExtensionGrantMongo();
        extensionGrantMongo.setId(extensionGrant.getId());
        extensionGrantMongo.setName(extensionGrant.getName());
        extensionGrantMongo.setType(extensionGrant.getType());
        extensionGrantMongo.setConfiguration(extensionGrant.getConfiguration());
        extensionGrantMongo.setDomain(extensionGrant.getDomain());
        extensionGrantMongo.setGrantType(extensionGrant.getGrantType());
        extensionGrantMongo.setIdentityProvider(extensionGrant.getIdentityProvider());
        extensionGrantMongo.setCreateUser(extensionGrant.isCreateUser());
        extensionGrantMongo.setUserExists(extensionGrant.isUserExists());
        extensionGrantMongo.setCreatedAt(extensionGrant.getCreatedAt());
        extensionGrantMongo.setUpdatedAt(extensionGrant.getUpdatedAt());
        return extensionGrantMongo;
    }
}

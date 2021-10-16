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

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.BasicDBObject;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.uma.policy.AccessPolicy;
import io.gravitee.am.model.uma.policy.AccessPolicyType;
import io.gravitee.am.repository.management.api.AccessPolicyRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.uma.AccessPolicyMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
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
public class MongoAccessPolicyRepository extends AbstractManagementMongoRepository implements AccessPolicyRepository {

    private static final String FIELD_RESOURCE = "resource";
    public static final String COLLECTION_NAME = "uma_access_policies";
    private MongoCollection<AccessPolicyMongo> accessPoliciesCollection;

    @PostConstruct
    public void init() {
        accessPoliciesCollection = mongoOperations.getCollection(COLLECTION_NAME, AccessPolicyMongo.class);
        super.init(accessPoliciesCollection);
        super.createIndex(accessPoliciesCollection, new Document(FIELD_DOMAIN, 1));
        super.createIndex(accessPoliciesCollection, new Document(FIELD_RESOURCE, 1));
        super.createIndex(accessPoliciesCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_RESOURCE, 1));
        super.createIndex(accessPoliciesCollection, new Document(FIELD_UPDATED_AT, -1));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<AccessPolicy>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<AccessPolicy>> findByDomain_migrated(String domain, int page, int size) {
        Single<Long> countOperation = Observable.fromPublisher(accessPoliciesCollection.countDocuments(eq(FIELD_DOMAIN, domain))).first(0l);
        Single<List<AccessPolicy>> accessPoliciesOperation = Observable.fromPublisher(accessPoliciesCollection.find(eq(FIELD_DOMAIN, domain)).sort(new BasicDBObject(FIELD_UPDATED_AT, -1)).skip(size * page).limit(size)).map(this::convert).toList();
        return RxJava2Adapter.singleToMono(Single.zip(countOperation, accessPoliciesOperation, (count, accessPolicies) -> new Page<>(accessPolicies, page, count)));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndResource_migrated(domain, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<AccessPolicy> findByDomainAndResource(String domain, String resource) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndResource_migrated(domain, resource));
}
@Override
    public Flux<AccessPolicy> findByDomainAndResource_migrated(String domain, String resource) {
        return Flux.from(accessPoliciesCollection.find(and(eq(FIELD_DOMAIN, domain), eq(FIELD_RESOURCE, resource)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByResources_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<AccessPolicy> findByResources(List<String> resources) {
 return RxJava2Adapter.fluxToFlowable(findByResources_migrated(resources));
}
@Override
    public Flux<AccessPolicy> findByResources_migrated(List<String> resources) {
        return Flux.from(accessPoliciesCollection.find(in(FIELD_RESOURCE, resources))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByResource_migrated(resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> countByResource(String resource) {
 return RxJava2Adapter.monoToSingle(countByResource_migrated(resource));
}
@Override
    public Mono<Long> countByResource_migrated(String resource) {
        return RxJava2Adapter.singleToMono(Observable.fromPublisher(accessPoliciesCollection.countDocuments(eq(FIELD_RESOURCE, resource))).first(0l));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AccessPolicy> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<AccessPolicy> findById_migrated(String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(accessPoliciesCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AccessPolicy> create(AccessPolicy item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<AccessPolicy> create_migrated(AccessPolicy item) {
        AccessPolicyMongo accessPolicy = convert(item);
        accessPolicy.setId(accessPolicy.getId() == null ? RandomString.generate() : accessPolicy.getId());
        return RxJava2Adapter.singleToMono(Single.fromPublisher(accessPoliciesCollection.insertOne(accessPolicy))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(accessPolicy.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AccessPolicy> update(AccessPolicy item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<AccessPolicy> update_migrated(AccessPolicy item) {
        AccessPolicyMongo accessPolicy = convert(item);
        return RxJava2Adapter.singleToMono(Single.fromPublisher(accessPoliciesCollection.replaceOne(eq(FIELD_ID, accessPolicy.getId()), accessPolicy))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(accessPolicy.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(accessPoliciesCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    private AccessPolicy convert(AccessPolicyMongo accessPolicyMongo) {
        if (accessPolicyMongo == null) {
            return null;
        }

        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId(accessPolicyMongo.getId());
        accessPolicy.setType(accessPolicyMongo.getType() != null ? AccessPolicyType.fromString(accessPolicyMongo.getType()) : null);
        accessPolicy.setEnabled(accessPolicyMongo.isEnabled());
        accessPolicy.setName(accessPolicyMongo.getName());
        accessPolicy.setDescription(accessPolicyMongo.getDescription());
        accessPolicy.setOrder(accessPolicyMongo.getOrder());
        accessPolicy.setCondition(accessPolicyMongo.getCondition());
        accessPolicy.setDomain(accessPolicyMongo.getDomain());
        accessPolicy.setResource(accessPolicyMongo.getResource());
        accessPolicy.setCreatedAt(accessPolicyMongo.getCreatedAt());
        accessPolicy.setUpdatedAt(accessPolicyMongo.getUpdatedAt());
        return accessPolicy;
    }

    private AccessPolicyMongo convert(AccessPolicy accessPolicy) {
        if (accessPolicy == null) {
            return null;
        }

        AccessPolicyMongo accessPolicyMongo = new AccessPolicyMongo();
        accessPolicyMongo.setId(accessPolicy.getId());
        accessPolicyMongo.setType(accessPolicy.getType() != null ? accessPolicy.getType().getName() : null);
        accessPolicyMongo.setEnabled(accessPolicy.isEnabled());
        accessPolicyMongo.setName(accessPolicy.getName());
        accessPolicyMongo.setDescription(accessPolicy.getDescription());
        accessPolicyMongo.setOrder(accessPolicy.getOrder());
        accessPolicyMongo.setCondition(accessPolicy.getCondition());
        accessPolicyMongo.setDomain(accessPolicy.getDomain());
        accessPolicyMongo.setResource(accessPolicy.getResource());
        accessPolicyMongo.setCreatedAt(accessPolicy.getCreatedAt());
        accessPolicyMongo.setUpdatedAt(accessPolicy.getUpdatedAt());
        return accessPolicyMongo;
    }
}

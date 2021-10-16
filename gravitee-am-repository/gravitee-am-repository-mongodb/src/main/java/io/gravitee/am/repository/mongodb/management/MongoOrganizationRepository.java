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

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Organization;
import io.gravitee.am.repository.management.api.OrganizationRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.OrganizationMongo;
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
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoOrganizationRepository extends AbstractManagementMongoRepository implements OrganizationRepository {

    private MongoCollection<OrganizationMongo> collection;
    private static final String HRID_KEY = "hrids";

    @PostConstruct
    public void init() {
        collection = mongoOperations.getCollection("organizations", OrganizationMongo.class);
        super.init(collection);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByHrids_migrated(hrids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Organization> findByHrids(List<String> hrids) {
 return RxJava2Adapter.fluxToFlowable(findByHrids_migrated(hrids));
}
@Override
    public Flux<Organization> findByHrids_migrated(List<String> hrids) {
        return Flux.from(collection.find(in(HRID_KEY, hrids))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Organization> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Organization> findById_migrated(String id) {

        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(collection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(organization))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Organization> create(Organization organization) {
 return RxJava2Adapter.monoToSingle(create_migrated(organization));
}
@Override
    public Mono<Organization> create_migrated(Organization organization) {

        organization.setId(organization.getId() == null ? RandomString.generate() : organization.getId());

        return RxJava2Adapter.singleToMono(Single.fromPublisher(collection.insertOne(convert(organization)))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(organization.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(organization))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Organization> update(Organization organization) {
 return RxJava2Adapter.monoToSingle(update_migrated(organization));
}
@Override
    public Mono<Organization> update_migrated(Organization organization) {

        return RxJava2Adapter.singleToMono(Single.fromPublisher(collection.replaceOne(eq(FIELD_ID, organization.getId()), convert(organization)))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(organization.getId()))).single());
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

        return RxJava2Adapter.singleToMono(Single.fromPublisher(collection.countDocuments()));
    }

    private Organization convert(OrganizationMongo organizationMongo) {

        Organization organization = new Organization();
        organization.setId(organizationMongo.getId());
        organization.setHrids(organizationMongo.getHrids());
        organization.setDescription(organizationMongo.getDescription());
        organization.setName(organizationMongo.getName());
        organization.setDomainRestrictions(organizationMongo.getDomainRestrictions());
        organization.setIdentities(organizationMongo.getIdentities());
        organization.setCreatedAt(organizationMongo.getCreatedAt());
        organization.setUpdatedAt(organizationMongo.getUpdatedAt());

        return organization;
    }

    private OrganizationMongo convert(Organization organization) {

        OrganizationMongo organizationMongo = new OrganizationMongo();
        organizationMongo.setId(organization.getId());
        organizationMongo.setHrids(organization.getHrids());
        organizationMongo.setDescription(organization.getDescription());
        organizationMongo.setName(organization.getName());
        organizationMongo.setDomainRestrictions(organization.getDomainRestrictions());
        organizationMongo.setIdentities(organization.getIdentities());
        organizationMongo.setCreatedAt(organization.getCreatedAt());
        organizationMongo.setUpdatedAt(organization.getUpdatedAt());

        return organizationMongo;
    }
}

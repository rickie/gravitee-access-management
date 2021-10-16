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

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Installation;
import io.gravitee.am.model.Organization;
import io.gravitee.am.repository.management.api.InstallationRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.InstallationMongo;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoInstallationRepository extends AbstractManagementMongoRepository implements InstallationRepository {

    private MongoCollection<InstallationMongo> collection;

    @PostConstruct
    public void init() {
        collection = mongoOperations.getCollection("installation", InstallationMongo.class);
        super.init(collection);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Installation> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Installation> findById_migrated(String id) {

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(collection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @Deprecated
@Override
    public Maybe<Installation> find() {
 return RxJava2Adapter.monoToMaybe(find_migrated());
}
@Override
    public Mono<Installation> find_migrated() {

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(collection.find().first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(installation))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Installation> create(Installation installation) {
 return RxJava2Adapter.monoToSingle(create_migrated(installation));
}
@Override
    public Mono<Installation> create_migrated(Installation installation) {

        installation.setId(installation.getId() == null ? RandomString.generate() : installation.getId());

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(collection.insertOne(convert(installation)))).flatMap(success->RxJava2Adapter.maybeToMono(findById(installation.getId())).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(installation))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Installation> update(Installation installation) {
 return RxJava2Adapter.monoToSingle(update_migrated(installation));
}
@Override
    public Mono<Installation> update_migrated(Installation installation) {

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(collection.replaceOne(eq(FIELD_ID, installation.getId()), convert(installation)))).flatMap(updateResult->RxJava2Adapter.maybeToMono(findById(installation.getId())).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(collection.deleteOne(eq(FIELD_ID, id)))));
    }

    private Installation convert(InstallationMongo installationMongo) {

        Installation installation = new Installation();
        installation.setId(installationMongo.getId());
        installation.setAdditionalInformation(installationMongo.getAdditionalInformation());
        installation.setCreatedAt(installationMongo.getCreatedAt());
        installation.setUpdatedAt(installationMongo.getUpdatedAt());

        return installation;
    }

    private InstallationMongo convert(Installation installation) {

        InstallationMongo installationMongo = new InstallationMongo();
        installationMongo.setId(installation.getId());
        installationMongo.setAdditionalInformation(installation.getAdditionalInformation());
        installationMongo.setCreatedAt(installation.getCreatedAt());
        installationMongo.setUpdatedAt(installation.getUpdatedAt());

        return installationMongo;
    }
}

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
import io.gravitee.am.model.Factor;
import io.gravitee.am.repository.management.api.FactorRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.FactorMongo;
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
public class MongoFactorRepository extends AbstractManagementMongoRepository implements FactorRepository {

    private static final String FIELD_FACTOR_TYPE = "factorType";
    private MongoCollection<FactorMongo> factorsCollection;

    @PostConstruct
    public void init() {
        factorsCollection = mongoOperations.getCollection("factors", FactorMongo.class);
        super.init(factorsCollection);
        super.createIndex(factorsCollection,new Document(FIELD_DOMAIN, 1));
        super.createIndex(factorsCollection,new Document(FIELD_DOMAIN, 1).append(FIELD_FACTOR_TYPE, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Factor> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Factor> findAll_migrated() {
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(factorsCollection.find()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Factor> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Factor> findByDomain_migrated(String domain) {
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(factorsCollection.find(eq(FIELD_DOMAIN, domain))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(factorId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Factor> findById(String factorId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(factorId));
}
@Override
    public Mono<Factor> findById_migrated(String factorId) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(factorsCollection.find(eq(FIELD_ID, factorId)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Factor> create(Factor item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Factor> create_migrated(Factor item) {
        FactorMongo authenticator = convert(item);
        authenticator.setId(authenticator.getId() == null ? RandomString.generate() : authenticator.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(factorsCollection.insertOne(authenticator))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(authenticator.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Factor> update(Factor item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Factor> update_migrated(Factor item) {
        FactorMongo authenticator = convert(item);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(factorsCollection.replaceOne(eq(FIELD_ID, authenticator.getId()), authenticator))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(authenticator.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(factorsCollection.deleteOne(eq(FIELD_ID, id)))));
    }

    private Factor convert(FactorMongo factorMongo) {
        if (factorMongo == null) {
            return null;
        }

        Factor factor = new Factor();
        factor.setId(factorMongo.getId());
        factor.setName(factorMongo.getName());
        factor.setType(factorMongo.getType());
        factor.setFactorType(factorMongo.getFactorType());
        factor.setConfiguration(factorMongo.getConfiguration());
        factor.setDomain(factorMongo.getDomain());
        factor.setCreatedAt(factorMongo.getCreatedAt());
        factor.setUpdatedAt(factorMongo.getUpdatedAt());
        return factor;
    }

    private FactorMongo convert(Factor factor) {
        if (factor == null) {
            return null;
        }

        FactorMongo factorMongo = new FactorMongo();
        factorMongo.setId(factor.getId());
        factorMongo.setName(factor.getName());
        factorMongo.setType(factor.getType());
        factorMongo.setFactorType(factor.getFactorType().getType());
        factorMongo.setConfiguration(factor.getConfiguration());
        factorMongo.setDomain(factor.getDomain());
        factorMongo.setCreatedAt(factor.getCreatedAt());
        factorMongo.setUpdatedAt(factor.getUpdatedAt());
        return factorMongo;
    }
}

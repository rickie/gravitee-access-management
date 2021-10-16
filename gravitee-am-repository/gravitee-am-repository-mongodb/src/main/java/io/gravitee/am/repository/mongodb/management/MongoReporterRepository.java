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
import io.gravitee.am.model.Reporter;
import io.gravitee.am.repository.management.api.ReporterRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.ReporterMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.ArrayList;
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
public class MongoReporterRepository extends AbstractManagementMongoRepository implements ReporterRepository {

    private MongoCollection<ReporterMongo> reportersCollection;

    @PostConstruct
    public void init() {
        reportersCollection = mongoOperations.getCollection("reporters", ReporterMongo.class);
        super.init(reportersCollection);
        super.createIndex(reportersCollection, new Document(FIELD_DOMAIN, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Reporter> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Reporter> findAll_migrated() {
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(reportersCollection.find()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Reporter> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Reporter> findByDomain_migrated(String domain) {
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(reportersCollection.find(eq(FIELD_DOMAIN, domain))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Reporter> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Reporter> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(reportersCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Reporter> create(Reporter item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Reporter> create_migrated(Reporter item) {
        ReporterMongo reporter = convert(item);
        reporter.setId(reporter.getId() == null ? RandomString.generate() : reporter.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(reportersCollection.insertOne(reporter))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(reporter.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Reporter> update(Reporter item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Reporter> update_migrated(Reporter item) {
        ReporterMongo reporter = convert(item);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(reportersCollection.replaceOne(eq(FIELD_ID, reporter.getId()), reporter))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(reporter.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(reportersCollection.deleteOne(eq(FIELD_ID, id)))));
    }

    private ReporterMongo convert(Reporter reporter) {
        if (reporter == null) {
            return null;
        }

        ReporterMongo reporterMongo = new ReporterMongo();
        reporterMongo.setId(reporter.getId());
        reporterMongo.setEnabled(reporter.isEnabled());
        reporterMongo.setDomain(reporter.getDomain());
        reporterMongo.setName(reporter.getName());
        reporterMongo.setType(reporter.getType());
        reporterMongo.setDataType(reporter.getDataType());
        reporterMongo.setConfiguration(reporter.getConfiguration());
        reporterMongo.setCreatedAt(reporter.getCreatedAt());
        reporterMongo.setUpdatedAt(reporter.getUpdatedAt());
        return reporterMongo;
    }

    private Reporter convert(ReporterMongo reporterMongo) {
        if (reporterMongo == null) {
            return null;
        }

        Reporter reporter = new Reporter();
        reporter.setId(reporterMongo.getId());
        reporter.setEnabled(reporterMongo.isEnabled());
        reporter.setDomain(reporterMongo.getDomain());
        reporter.setName(reporterMongo.getName());
        reporter.setType(reporterMongo.getType());
        reporter.setDataType(reporterMongo.getDataType());
        reporter.setConfiguration(reporterMongo.getConfiguration());
        reporter.setCreatedAt(reporterMongo.getCreatedAt());
        reporter.setUpdatedAt(reporterMongo.getUpdatedAt());
        return reporter;
    }
}

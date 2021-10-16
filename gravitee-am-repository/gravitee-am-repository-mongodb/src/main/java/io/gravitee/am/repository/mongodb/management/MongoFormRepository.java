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
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Form;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.management.api.FormRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.FormMongo;
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
 * @author Titouan COMPIEGNE (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoFormRepository extends AbstractManagementMongoRepository implements FormRepository {

    private static final String FIELD_TEMPLATE = "template";
    private MongoCollection<FormMongo> formsCollection;

    @PostConstruct
    public void init() {
        formsCollection = mongoOperations.getCollection("forms", FormMongo.class);
        super.init(formsCollection);
        super.createIndex(formsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1));
        super.createIndex(formsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_TEMPLATE, 1));
        super.createIndex(formsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_CLIENT, 1).append(FIELD_TEMPLATE, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Form> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<Form> findAll_migrated(ReferenceType referenceType, String referenceId) {
        return Flux.from(formsCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Form> findAll(ReferenceType referenceType) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
@Override
    public Flux<Form> findAll_migrated(ReferenceType referenceType) {
        return Flux.from(formsCollection.find(eq(FIELD_REFERENCE_TYPE, referenceType.name()))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClient_migrated(referenceType, referenceId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Form> findByClient(ReferenceType referenceType, String referenceId, String client) {
 return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
@Override
    public Flux<Form> findByClient_migrated(ReferenceType referenceType, String referenceId, String client) {
        return Flux.from(formsCollection.find(
                        and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_CLIENT, client))
                )).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(referenceType, referenceId, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Form> findByTemplate(ReferenceType referenceType, String referenceId, String template) {
 return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
@Override
    public Mono<Form> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(
                formsCollection.find(
                        and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_TEMPLATE, template),
                                exists(FIELD_CLIENT, false)))
                        .first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Form> findByClientAndTemplate(ReferenceType referenceType, String referenceId, String client, String template) {
 return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
@Override
    public Mono<Form> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(
                formsCollection.find(
                        and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_CLIENT, client),
                                eq(FIELD_TEMPLATE, template)))
                        .first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Form> findById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<Form> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(formsCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId), eq(FIELD_ID, id))).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Form> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Form> findById_migrated(String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(formsCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Form> create(Form item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Form> create_migrated(Form item) {
        FormMongo page = convert(item);
        page.setId(page.getId() == null ? RandomString.generate() : page.getId());
        return RxJava2Adapter.singleToMono(Single.fromPublisher(formsCollection.insertOne(page))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(page.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Form> update(Form item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Form> update_migrated(Form item) {
        FormMongo page = convert(item);
        return RxJava2Adapter.singleToMono(Single.fromPublisher(formsCollection.replaceOne(eq(FIELD_ID, page.getId()), page))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(page.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(formsCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    private Form convert(FormMongo pageMongo) {
        if (pageMongo == null) {
            return null;
        }
        Form page = new Form();
        page.setId(pageMongo.getId());
        page.setEnabled(pageMongo.isEnabled());
        page.setReferenceType(ReferenceType.valueOf(pageMongo.getReferenceType()));
        page.setReferenceId(pageMongo.getReferenceId());
        page.setClient(pageMongo.getClient());
        page.setTemplate(pageMongo.getTemplate());
        page.setContent(pageMongo.getContent());
        page.setAssets(pageMongo.getAssets());
        page.setCreatedAt(pageMongo.getCreatedAt());
        page.setUpdatedAt(pageMongo.getUpdatedAt());
        return page;
    }

    private FormMongo convert(Form page) {
        if (page == null) {
            return null;
        }

        FormMongo pageMongo = new FormMongo();
        pageMongo.setId(page.getId());
        pageMongo.setEnabled(page.isEnabled());
        pageMongo.setReferenceType(page.getReferenceType().name());
        pageMongo.setReferenceId(page.getReferenceId());
        pageMongo.setClient(page.getClient());
        pageMongo.setTemplate(page.getTemplate());
        pageMongo.setContent(page.getContent());
        pageMongo.setAssets(page.getAssets());
        pageMongo.setCreatedAt(page.getCreatedAt());
        pageMongo.setUpdatedAt(page.getUpdatedAt());
        return pageMongo;
    }
}

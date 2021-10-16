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
import com.mongodb.client.model.IndexOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.model.AuthenticationFlowContext;
import io.gravitee.am.repository.management.api.AuthenticationFlowContextRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.AuthenticationFlowContextMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoAuthenticationFlowContextRepository extends AbstractManagementMongoRepository implements AuthenticationFlowContextRepository {

    private final static String FIELD_TRANSACTION_ID = "transactionId";
    private final static String FIELD_VERSION = "version";
    private static final String FIELD_RESET_TIME = "expire_at";

    private MongoCollection<AuthenticationFlowContextMongo> authContextCollection;

    @PostConstruct
    public void init() {
        authContextCollection = mongoOperations.getCollection("auth_flow_ctx", AuthenticationFlowContextMongo.class);
        super.init(authContextCollection);
        super.createIndex(authContextCollection, new Document(FIELD_TRANSACTION_ID, 1).append(FIELD_VERSION, -1));
        super.createIndex(authContextCollection, new Document(FIELD_RESET_TIME, 1), new IndexOptions().expireAfter(0l, TimeUnit.SECONDS));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AuthenticationFlowContext> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<AuthenticationFlowContext> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(authContextCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findLastByTransactionId_migrated(transactionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AuthenticationFlowContext> findLastByTransactionId(String transactionId) {
 return RxJava2Adapter.monoToMaybe(findLastByTransactionId_migrated(transactionId));
}
@Override
    public Mono<AuthenticationFlowContext> findLastByTransactionId_migrated(String transactionId) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(authContextCollection.find(and(eq(FIELD_TRANSACTION_ID, transactionId), gt(FIELD_RESET_TIME, new Date()))).sort(new BasicDBObject(FIELD_VERSION, -1)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByTransactionId_migrated(transactionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<AuthenticationFlowContext> findByTransactionId(String transactionId) {
 return RxJava2Adapter.fluxToFlowable(findByTransactionId_migrated(transactionId));
}
@Override
    public Flux<AuthenticationFlowContext> findByTransactionId_migrated(String transactionId) {
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(authContextCollection.find(and(eq(FIELD_TRANSACTION_ID, transactionId), gt(FIELD_RESET_TIME, new Date()))).sort(new BasicDBObject(FIELD_VERSION, -1))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(context))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AuthenticationFlowContext> create(AuthenticationFlowContext context) {
 return RxJava2Adapter.monoToSingle(create_migrated(context));
}
@Override
    public Mono<AuthenticationFlowContext> create_migrated(AuthenticationFlowContext context) {
        AuthenticationFlowContextMongo contextMongo = convert(context);
        contextMongo.setId(context.getTransactionId() + "-" + context.getVersion());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(authContextCollection.insertOne(contextMongo))).flatMap(success->RxJava2Adapter.maybeToMono(findById(contextMongo.getId())).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(transactionId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String transactionId) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(transactionId));
}
@Override
    public Mono<Void> delete_migrated(String transactionId) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(authContextCollection.deleteMany(eq(FIELD_TRANSACTION_ID, transactionId)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(transactionId, version))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String transactionId, int version) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(transactionId, version));
}
@Override
    public Mono<Void> delete_migrated(String transactionId, int version) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(authContextCollection.deleteOne(and(eq(FIELD_TRANSACTION_ID, transactionId), eq(FIELD_VERSION, version))))));
    }

    private AuthenticationFlowContext convert(AuthenticationFlowContextMongo entity) {
        AuthenticationFlowContext bean = new AuthenticationFlowContext();
        bean.setTransactionId(entity.getTransactionId());
        bean.setVersion(entity.getVersion());
        bean.setData(entity.getData() == null ? null : entity.getData());
        bean.setCreatedAt(entity.getCreatedAt());
        bean.setExpireAt(entity.getExpireAt());
        return bean;
    }

    private AuthenticationFlowContextMongo convert(AuthenticationFlowContext bean) {
        AuthenticationFlowContextMongo entity = new AuthenticationFlowContextMongo();
        entity.setTransactionId(bean.getTransactionId());
        entity.setVersion(bean.getVersion());
        entity.setData(bean.getData() == null ? null : new Document(bean.getData()));
        entity.setCreatedAt(bean.getCreatedAt());
        entity.setExpireAt(bean.getExpireAt());
        return entity;
    }

}

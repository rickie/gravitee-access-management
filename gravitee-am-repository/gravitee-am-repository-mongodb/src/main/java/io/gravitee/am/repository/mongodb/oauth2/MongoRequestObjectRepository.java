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
package io.gravitee.am.repository.mongodb.oauth2;

import static com.mongodb.client.model.Filters.eq;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.repository.mongodb.oauth2.internal.model.RequestObjectMongo;
import io.gravitee.am.repository.oidc.api.RequestObjectRepository;
import io.gravitee.am.repository.oidc.model.RequestObject;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoRequestObjectRepository extends AbstractOAuth2MongoRepository implements RequestObjectRepository {

    private MongoCollection<RequestObjectMongo> requestObjectCollection;

    private static final String FIELD_EXPIRE_AT = "expire_at";

    @PostConstruct
    public void init() {
        requestObjectCollection = mongoOperations.getCollection("request_objects", RequestObjectMongo.class);
        super.init(requestObjectCollection);

        // expire after index
        super.createIndex(requestObjectCollection, new Document(FIELD_EXPIRE_AT, 1), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
    }

    @Deprecated
@Override
    public Maybe<RequestObject> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<RequestObject> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable
                .fromPublisher(requestObjectCollection.find(eq(FIELD_ID, id)).limit(1).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @Deprecated
@Override
    public Single<RequestObject> create(RequestObject requestObject) {
 return RxJava2Adapter.monoToSingle(create_migrated(requestObject));
}
@Override
    public Mono<RequestObject> create_migrated(RequestObject requestObject) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single
                .fromPublisher(requestObjectCollection.insertOne(convert(requestObject)))).flatMap(success->RxJava2Adapter.maybeToMono(findById(requestObject.getId())).single())));
    }

    @Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(requestObjectCollection.findOneAndDelete(eq(FIELD_ID, id)))));
    }

    private RequestObjectMongo convert(RequestObject requestObject) {
        if (requestObject == null) {
            return null;
        }

        RequestObjectMongo requestObjectMongo = new RequestObjectMongo();
        requestObjectMongo.setId(requestObject.getId());
        requestObjectMongo.setPayload(requestObject.getPayload());
        requestObjectMongo.setDomain(requestObject.getDomain());
        requestObjectMongo.setClient(requestObject.getClient());
        requestObjectMongo.setCreatedAt(requestObject.getCreatedAt());
        requestObjectMongo.setExpireAt(requestObject.getExpireAt());

        return requestObjectMongo;
    }

    private RequestObject convert(RequestObjectMongo requestObjectMongo) {
        if (requestObjectMongo == null) {
            return null;
        }

        RequestObject requestObject = new RequestObject();
        requestObject.setId(requestObjectMongo.getId());
        requestObject.setPayload(requestObjectMongo.getPayload());
        requestObject.setDomain(requestObjectMongo.getDomain());
        requestObject.setClient(requestObjectMongo.getClient());
        requestObject.setCreatedAt(requestObjectMongo.getCreatedAt());
        requestObject.setExpireAt(requestObjectMongo.getExpireAt());

        return requestObject;
    }
}

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

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.repository.mongodb.oauth2.internal.model.PushedAuthorizationRequestMongo;
import io.gravitee.am.repository.oauth2.api.PushedAuthorizationRequestRepository;
import io.gravitee.am.repository.oauth2.model.PushedAuthorizationRequest;
import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoPushedAuthorizationRequestRepository extends AbstractOAuth2MongoRepository implements PushedAuthorizationRequestRepository {

    private MongoCollection<PushedAuthorizationRequestMongo> parCollection;

    private static final String FIELD_EXPIRE_AT = "expire_at";

    @PostConstruct
    public void init() {
        parCollection = mongoOperations.getCollection("pushed_authorization_requests", PushedAuthorizationRequestMongo.class);
        super.init(parCollection);

        // expire after index
        super.createIndex(parCollection, new Document(FIELD_EXPIRE_AT, 1), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<PushedAuthorizationRequest> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<PushedAuthorizationRequest> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable
                .fromPublisher(parCollection.find(eq(FIELD_ID, id)).limit(1).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(par))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<PushedAuthorizationRequest> create(PushedAuthorizationRequest par) {
 return RxJava2Adapter.monoToSingle(create_migrated(par));
}
@Override
    public Mono<PushedAuthorizationRequest> create_migrated(PushedAuthorizationRequest par) {
        par.setId(par.getId() == null ? RandomString.generate() : par.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single
                .fromPublisher(parCollection.insertOne(convert(par)))).flatMap(success->RxJava2Adapter.maybeToMono(findById(par.getId())).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(parCollection.findOneAndDelete(eq(FIELD_ID, id)))));
    }

    private PushedAuthorizationRequestMongo convert(PushedAuthorizationRequest par) {
        if (par == null) {
            return null;
        }

        PushedAuthorizationRequestMongo parMongo = new PushedAuthorizationRequestMongo();
        parMongo.setId(par.getId());
        parMongo.setDomain(par.getDomain());
        parMongo.setClient(par.getClient());
        parMongo.setCreatedAt(par.getCreatedAt());
        parMongo.setExpireAt(par.getExpireAt());

        if (par.getParameters() != null) {
            Document document = new Document();
            par.getParameters().forEach(document::append);
            parMongo.setParameters(document);
        }

        return parMongo;
    }

    private PushedAuthorizationRequest convert(PushedAuthorizationRequestMongo parMongo) {
        if (parMongo == null) {
            return null;
        }

        PushedAuthorizationRequest par = new PushedAuthorizationRequest();
        par.setId(parMongo.getId());
        par.setDomain(parMongo.getDomain());
        par.setClient(parMongo.getClient());
        par.setCreatedAt(parMongo.getCreatedAt());
        par.setExpireAt(parMongo.getExpireAt());

        if (parMongo.getParameters() != null) {
            MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parMongo.getParameters().entrySet().forEach(entry -> parameters.put(entry.getKey(), (List<String>) entry.getValue()));
            par.setParameters(parameters);
        }

        return par;
    }
}

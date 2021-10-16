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
import io.gravitee.am.repository.mongodb.oauth2.internal.model.AuthorizationCodeMongo;
import io.gravitee.am.repository.oauth2.api.AuthorizationCodeRepository;
import io.gravitee.am.repository.oauth2.model.AuthorizationCode;
import io.gravitee.common.util.LinkedMultiValueMap;
import io.gravitee.common.util.MultiValueMap;
import io.reactivex.BackpressureStrategy;
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
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoAuthorizationCodeRepository extends AbstractOAuth2MongoRepository implements AuthorizationCodeRepository {

    private static final String FIELD_TRANSACTION_ID = "transactionId";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_RESET_TIME = "expire_at";
    private MongoCollection<AuthorizationCodeMongo> authorizationCodeCollection;

    @PostConstruct
    public void init() {
        authorizationCodeCollection = mongoOperations.getCollection("authorization_codes", AuthorizationCodeMongo.class);
        super.init(authorizationCodeCollection);
        super.createIndex(authorizationCodeCollection, new Document(FIELD_CODE, 1));
        super.createIndex(authorizationCodeCollection, new Document(FIELD_TRANSACTION_ID, 1));

        // expire after index
        super.createIndex(authorizationCodeCollection, new Document(FIELD_RESET_TIME, 1), new IndexOptions().expireAfter(0l, TimeUnit.SECONDS));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<AuthorizationCode> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
private Mono<AuthorizationCode> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable
                .fromPublisher(authorizationCodeCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(authorizationCode))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AuthorizationCode> create(AuthorizationCode authorizationCode) {
 return RxJava2Adapter.monoToSingle(create_migrated(authorizationCode));
}
@Override
    public Mono<AuthorizationCode> create_migrated(AuthorizationCode authorizationCode) {
        if (authorizationCode.getId() == null) {
            authorizationCode.setId(RandomString.generate());
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single
                .fromPublisher(authorizationCodeCollection.insertOne(convert(authorizationCode)))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(authorizationCode.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.delete_migrated(code))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AuthorizationCode> delete(String code) {
 return RxJava2Adapter.monoToMaybe(delete_migrated(code));
}
@Override
    public Mono<AuthorizationCode> delete_migrated(String code) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(authorizationCodeCollection.findOneAndDelete(eq(FIELD_ID, code))), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByCode_migrated(code))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AuthorizationCode> findByCode(String code) {
 return RxJava2Adapter.monoToMaybe(findByCode_migrated(code));
}
@Override
    public Mono<AuthorizationCode> findByCode_migrated(String code) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(authorizationCodeCollection.find(eq(FIELD_CODE, code)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    private AuthorizationCode convert(AuthorizationCodeMongo authorizationCodeMongo) {
        if (authorizationCodeMongo == null) {
            return null;
        }

        AuthorizationCode authorizationCode = new AuthorizationCode();
        authorizationCode.setId(authorizationCodeMongo.getId());
        authorizationCode.setTransactionId(authorizationCodeMongo.getTransactionId());
        authorizationCode.setContextVersion(authorizationCodeMongo.getContextVersion());
        authorizationCode.setCode(authorizationCodeMongo.getCode());
        authorizationCode.setClientId(authorizationCodeMongo.getClientId());
        authorizationCode.setCreatedAt(authorizationCodeMongo.getCreatedAt());
        authorizationCode.setExpireAt(authorizationCodeMongo.getExpireAt());
        authorizationCode.setSubject(authorizationCodeMongo.getSubject());
        authorizationCode.setScopes(authorizationCodeMongo.getScopes());

        if (authorizationCodeMongo.getRequestParameters() != null) {
            MultiValueMap<String, String> requestParameters = new LinkedMultiValueMap<>();
            authorizationCodeMongo.getRequestParameters().entrySet().forEach(entry -> requestParameters.put(entry.getKey(), (List<String>) entry.getValue()));
            authorizationCode.setRequestParameters(requestParameters);
        }
        return authorizationCode;
    }

    private AuthorizationCodeMongo convert(AuthorizationCode authorizationCode) {
        if (authorizationCode == null) {
            return null;
        }

        AuthorizationCodeMongo authorizationCodeMongo = new AuthorizationCodeMongo();
        authorizationCodeMongo.setId(authorizationCode.getId());
        authorizationCodeMongo.setTransactionId(authorizationCode.getTransactionId());
        authorizationCodeMongo.setContextVersion(authorizationCode.getContextVersion());
        authorizationCodeMongo.setCode(authorizationCode.getCode());
        authorizationCodeMongo.setClientId(authorizationCode.getClientId());
        authorizationCodeMongo.setCreatedAt(authorizationCode.getCreatedAt());
        authorizationCodeMongo.setExpireAt(authorizationCode.getExpireAt());
        authorizationCodeMongo.setSubject(authorizationCode.getSubject());
        authorizationCodeMongo.setScopes(authorizationCode.getScopes());

        if (authorizationCode.getRequestParameters() != null) {
            Document document = new Document();
            authorizationCode.getRequestParameters().forEach(document::append);
            authorizationCodeMongo.setRequestParameters(document);
        }
        return authorizationCodeMongo;
    }
}

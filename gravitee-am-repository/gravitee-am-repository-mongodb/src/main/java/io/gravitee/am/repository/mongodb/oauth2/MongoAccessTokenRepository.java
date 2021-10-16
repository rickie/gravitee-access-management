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

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.repository.mongodb.oauth2.internal.model.AccessTokenMongo;
import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.repository.oauth2.model.AccessToken;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoAccessTokenRepository extends AbstractOAuth2MongoRepository implements AccessTokenRepository {

    private MongoCollection<AccessTokenMongo> accessTokenCollection;

    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_RESET_TIME = "expire_at";
    private static final String FIELD_SUBJECT = "subject";
    private static final String FIELD_AUTHORIZATION_CODE = "authorization_code";

    @PostConstruct
    public void init() {
        accessTokenCollection = mongoOperations.getCollection("access_tokens", AccessTokenMongo.class);
        super.init(accessTokenCollection);
        super.createIndex(accessTokenCollection, new Document(FIELD_TOKEN, 1));
        super.createIndex(accessTokenCollection, new Document(FIELD_CLIENT, 1));
        super.createIndex(accessTokenCollection, new Document(FIELD_AUTHORIZATION_CODE, 1));
        super.createIndex(accessTokenCollection, new Document(FIELD_SUBJECT, 1));
        super.createIndex(accessTokenCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_CLIENT, 1).append(FIELD_SUBJECT, 1));

        // expire after index
        super.createIndex(accessTokenCollection, new Document(FIELD_RESET_TIME, 1), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<AccessToken> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
private Mono<AccessToken> findById_migrated(String id) {
        return RxJava2Adapter.observableToFlux(Observable
                .fromPublisher(accessTokenCollection.find(eq(FIELD_ID, id)).limit(1).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByToken_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AccessToken> findByToken(String token) {
 return RxJava2Adapter.monoToMaybe(findByToken_migrated(token));
}
@Override
    public Mono<AccessToken> findByToken_migrated(String token) {
        return RxJava2Adapter.observableToFlux(Observable
                .fromPublisher(accessTokenCollection.find(eq(FIELD_TOKEN, token)).limit(1).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(accessToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AccessToken> create(AccessToken accessToken) {
 return RxJava2Adapter.monoToSingle(create_migrated(accessToken));
}
@Override
    public Mono<AccessToken> create_migrated(AccessToken accessToken) {
        return RxJava2Adapter.singleToMono(Single
                .fromPublisher(accessTokenCollection.insertOne(convert(accessToken)))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(accessToken.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.bulkWrite_migrated(accessTokens))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable bulkWrite(List<AccessToken> accessTokens) {
 return RxJava2Adapter.monoToCompletable(bulkWrite_migrated(accessTokens));
}
@Override
    public Mono<Void> bulkWrite_migrated(List<AccessToken> accessTokens) {
        return Mono.from(accessTokenCollection.bulkWrite(convert(accessTokens)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String token) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(token));
}
@Override
    public Mono<Void> delete_migrated(String token) {
        return Mono.from(accessTokenCollection.findOneAndDelete(eq(FIELD_TOKEN, token)));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToObservable(this.findByClientIdAndSubject_migrated(clientId, subject))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Observable<AccessToken> findByClientIdAndSubject(String clientId, String subject) {
 return RxJava2Adapter.fluxToObservable(findByClientIdAndSubject_migrated(clientId, subject));
}
@Override
    public Flux<AccessToken> findByClientIdAndSubject_migrated(String clientId, String subject) {
        return RxJava2Adapter.observableToFlux(Observable
                .fromPublisher(accessTokenCollection.find(and(eq(FIELD_CLIENT, clientId), eq(FIELD_SUBJECT, subject))))
                .map(this::convert), BackpressureStrategy.BUFFER);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToObservable(this.findByClientId_migrated(clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Observable<AccessToken> findByClientId(String clientId) {
 return RxJava2Adapter.fluxToObservable(findByClientId_migrated(clientId));
}
@Override
    public Flux<AccessToken> findByClientId_migrated(String clientId) {
        return RxJava2Adapter.observableToFlux(Observable
                .fromPublisher(accessTokenCollection.find(eq(FIELD_CLIENT, clientId)))
                .map(this::convert), BackpressureStrategy.BUFFER);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToObservable(this.findByAuthorizationCode_migrated(authorizationCode))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Observable<AccessToken> findByAuthorizationCode(String authorizationCode) {
 return RxJava2Adapter.fluxToObservable(findByAuthorizationCode_migrated(authorizationCode));
}
@Override
    public Flux<AccessToken> findByAuthorizationCode_migrated(String authorizationCode) {
        return RxJava2Adapter.observableToFlux(Observable
                .fromPublisher(accessTokenCollection.find(eq(FIELD_AUTHORIZATION_CODE, authorizationCode)))
                .map(this::convert), BackpressureStrategy.BUFFER);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByClientId_migrated(clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> countByClientId(String clientId) {
 return RxJava2Adapter.monoToSingle(countByClientId_migrated(clientId));
}
@Override
    public Mono<Long> countByClientId_migrated(String clientId) {
        return RxJava2Adapter.singleToMono(Single.fromPublisher(accessTokenCollection.countDocuments(eq(FIELD_CLIENT, clientId))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByUserId_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByUserId(String userId) {
 return RxJava2Adapter.monoToCompletable(deleteByUserId_migrated(userId));
}
@Override
    public Mono<Void> deleteByUserId_migrated(String userId) {
        return Mono.from(accessTokenCollection.deleteMany(eq(FIELD_SUBJECT, userId)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainIdClientIdAndUserId_migrated(domainId, clientId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByDomainIdClientIdAndUserId(String domainId, String clientId, String userId) {
 return RxJava2Adapter.monoToCompletable(deleteByDomainIdClientIdAndUserId_migrated(domainId, clientId, userId));
}
@Override
    public Mono<Void> deleteByDomainIdClientIdAndUserId_migrated(String domainId, String clientId, String userId) {
        return Mono.from(accessTokenCollection.deleteMany(and(eq(FIELD_DOMAIN, domainId), eq(FIELD_CLIENT, clientId), eq(FIELD_SUBJECT, userId))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainIdAndUserId_migrated(domainId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByDomainIdAndUserId(String domainId, String userId) {
 return RxJava2Adapter.monoToCompletable(deleteByDomainIdAndUserId_migrated(domainId, userId));
}
@Override
    public Mono<Void> deleteByDomainIdAndUserId_migrated(String domainId, String userId) {
        return Mono.from(accessTokenCollection.deleteMany(and(eq(FIELD_DOMAIN, domainId), eq(FIELD_SUBJECT, userId))));
    }

    private List<WriteModel<AccessTokenMongo>> convert(List<AccessToken> accessTokens) {
        return accessTokens.stream().map(accessToken -> new InsertOneModel<>(convert(accessToken))).collect(Collectors.toList());
    }

    private AccessTokenMongo convert(AccessToken accessToken) {
        if (accessToken == null) {
            return null;
        }

        AccessTokenMongo accessTokenMongo = new AccessTokenMongo();
        accessTokenMongo.setId(accessToken.getId());
        accessTokenMongo.setToken(accessToken.getToken());
        accessTokenMongo.setDomain(accessToken.getDomain());
        accessTokenMongo.setClient(accessToken.getClient());
        accessTokenMongo.setSubject(accessToken.getSubject());
        accessTokenMongo.setAuthorizationCode(accessToken.getAuthorizationCode());
        accessTokenMongo.setRefreshToken(accessToken.getRefreshToken());
        accessTokenMongo.setCreatedAt(accessToken.getCreatedAt());
        accessTokenMongo.setExpireAt(accessToken.getExpireAt());

        return accessTokenMongo;
    }

    private AccessToken convert(AccessTokenMongo accessTokenMongo) {
        if (accessTokenMongo == null) {
            return null;
        }

        AccessToken accessToken = new AccessToken();
        accessToken.setId(accessTokenMongo.getId());
        accessToken.setToken(accessTokenMongo.getToken());
        accessToken.setDomain(accessTokenMongo.getDomain());
        accessToken.setClient(accessTokenMongo.getClient());
        accessToken.setSubject(accessTokenMongo.getSubject());
        accessToken.setAuthorizationCode(accessTokenMongo.getAuthorizationCode());
        accessToken.setRefreshToken(accessTokenMongo.getRefreshToken());
        accessToken.setCreatedAt(accessTokenMongo.getCreatedAt());
        accessToken.setExpireAt(accessTokenMongo.getExpireAt());

        return accessToken;
    }
}

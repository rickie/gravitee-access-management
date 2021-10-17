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
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.repository.mongodb.oauth2.internal.model.RefreshTokenMongo;
import io.gravitee.am.repository.oauth2.api.RefreshTokenRepository;
import io.gravitee.am.repository.oauth2.model.RefreshToken;

import io.reactivex.Completable;


import io.reactivex.Single;
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
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoRefreshTokenRepository extends AbstractOAuth2MongoRepository implements RefreshTokenRepository {

    private MongoCollection<RefreshTokenMongo> refreshTokenCollection;
    private static final String FIELD_RESET_TIME = "expire_at";
    private static final String FIELD_TOKEN = "token";
    private static final String FIELD_SUBJECT = "subject";

    @PostConstruct
    public void init() {
        refreshTokenCollection = mongoOperations.getCollection("refresh_tokens", RefreshTokenMongo.class);
        super.init(refreshTokenCollection);
        super.createIndex(refreshTokenCollection, new Document(FIELD_TOKEN, 1));
        super.createIndex(refreshTokenCollection, new Document(FIELD_SUBJECT, 1));
        super.createIndex(refreshTokenCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_CLIENT, 1).append(FIELD_SUBJECT, 1));

        // expire after index
        super.createIndex(refreshTokenCollection, new Document(FIELD_RESET_TIME, 1), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
    }

    
private Mono<RefreshToken> findById_migrated(String id) {
        return Flux.from(refreshTokenCollection.find(eq(FIELD_ID, id)).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    
@Override
    public Mono<RefreshToken> findByToken_migrated(String token) {
        return Flux.from(refreshTokenCollection.find(eq(FIELD_TOKEN, token)).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(refreshToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<RefreshToken> create(RefreshToken refreshToken) {
 return RxJava2Adapter.monoToSingle(create_migrated(refreshToken));
}
@Override
    public Mono<RefreshToken> create_migrated(RefreshToken refreshToken) {
        if (refreshToken.getId() == null) {
            refreshToken.setId(RandomString.generate());
        }

        return Mono.from(refreshTokenCollection.insertOne(convert(refreshToken))).flatMap(success->findById_migrated(refreshToken.getId()).single());
    }

    
@Override
    public Mono<Void> bulkWrite_migrated(List<RefreshToken> refreshTokens) {
        return Mono.from(refreshTokenCollection.bulkWrite(convert(refreshTokens))).then();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String token) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(token));
}
@Override
    public Mono<Void> delete_migrated(String token) {
        return Mono.from(refreshTokenCollection.deleteOne(eq(FIELD_TOKEN, token))).then();
    }

    
@Override
    public Mono<Void> deleteByUserId_migrated(String userId) {
        return Mono.from(refreshTokenCollection.deleteMany(eq(FIELD_SUBJECT, userId))).then();
    }

    
@Override
    public Mono<Void> deleteByDomainIdClientIdAndUserId_migrated(String domainId, String clientId, String userId) {
        return Mono.from(refreshTokenCollection.deleteMany(and(eq(FIELD_DOMAIN, domainId), eq(FIELD_CLIENT, clientId), eq(FIELD_SUBJECT, userId)))).then();
    }

    
@Override
    public Mono<Void> deleteByDomainIdAndUserId_migrated(String domainId, String userId) {
        return Mono.from(refreshTokenCollection.deleteMany(and(eq(FIELD_DOMAIN, domainId), eq(FIELD_SUBJECT, userId)))).then();
    }

    private List<WriteModel<RefreshTokenMongo>> convert(List<RefreshToken> refreshTokens) {
        return refreshTokens.stream().map(refreshToken -> new InsertOneModel<>(convert(refreshToken))).collect(Collectors.toList());
    }

    private RefreshTokenMongo convert(RefreshToken refreshToken) {
        if (refreshToken == null) {
            return null;
        }

        RefreshTokenMongo refreshTokenMongo = new RefreshTokenMongo();
        refreshTokenMongo.setId(refreshToken.getId());
        refreshTokenMongo.setToken(refreshToken.getToken());
        refreshTokenMongo.setDomain(refreshToken.getDomain());
        refreshTokenMongo.setClient(refreshToken.getClient());
        refreshTokenMongo.setSubject(refreshToken.getSubject());
        refreshTokenMongo.setCreatedAt(refreshToken.getCreatedAt());
        refreshTokenMongo.setExpireAt(refreshToken.getExpireAt());

        return refreshTokenMongo;
    }

    private RefreshToken convert(RefreshTokenMongo refreshTokenMongo) {
        if (refreshTokenMongo == null) {
            return null;
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(refreshTokenMongo.getId());
        refreshToken.setToken(refreshTokenMongo.getToken());
        refreshToken.setDomain(refreshTokenMongo.getDomain());
        refreshToken.setClient(refreshTokenMongo.getClient());
        refreshToken.setSubject(refreshTokenMongo.getSubject());
        refreshToken.setCreatedAt(refreshTokenMongo.getCreatedAt());
        refreshToken.setExpireAt(refreshTokenMongo.getExpireAt());

        return refreshToken;
    }
}

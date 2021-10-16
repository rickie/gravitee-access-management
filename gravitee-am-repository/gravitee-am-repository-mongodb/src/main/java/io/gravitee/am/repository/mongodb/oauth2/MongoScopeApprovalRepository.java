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

import com.mongodb.client.model.IndexOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.repository.mongodb.oauth2.internal.model.ScopeApprovalMongo;
import io.gravitee.am.repository.oauth2.api.ScopeApprovalRepository;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoScopeApprovalRepository extends AbstractOAuth2MongoRepository implements ScopeApprovalRepository {

    private static final String FIELD_TRANSACTION_ID = "transactionId";
    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_CLIENT_ID = "clientId";
    private static final String FIELD_EXPIRES_AT = "expiresAt";
    private static final String FIELD_SCOPE = "scope";
    private MongoCollection<ScopeApprovalMongo> scopeApprovalsCollection;

    @PostConstruct
    public void init() {
        scopeApprovalsCollection = mongoOperations.getCollection("scope_approvals", ScopeApprovalMongo.class);
        super.init(scopeApprovalsCollection);
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_TRANSACTION_ID, 1));
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_USER_ID, 1));
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_CLIENT_ID, 1).append(FIELD_USER_ID, 1));
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_CLIENT_ID, 1).append(FIELD_USER_ID, 1).append(FIELD_SCOPE, 1));

        // expire after index
        super.createIndex(scopeApprovalsCollection, new Document(FIELD_EXPIRES_AT, 1),  new IndexOptions().expireAfter(0l, TimeUnit.SECONDS));
    }

    @Override
    public Flowable<ScopeApproval> findByDomainAndUserAndClient(String domain, String userId, String clientId) {
        return RxJava2Adapter.fluxToFlowable(Flux.from(scopeApprovalsCollection.find(and(eq(FIELD_DOMAIN, domain), eq(FIELD_CLIENT_ID, clientId), eq(FIELD_USER_ID, userId)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Flowable<ScopeApproval> findByDomainAndUser(String domain, String user) {
        return RxJava2Adapter.fluxToFlowable(Flux.from(scopeApprovalsCollection.find(and(eq(FIELD_DOMAIN, domain), eq(FIELD_USER_ID, user)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Deprecated
@Override
    public Maybe<ScopeApproval> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<ScopeApproval> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(scopeApprovalsCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @Deprecated
@Override
    public Single<ScopeApproval> create(ScopeApproval scopeApproval) {
 return RxJava2Adapter.monoToSingle(create_migrated(scopeApproval));
}
@Override
    public Mono<ScopeApproval> create_migrated(ScopeApproval scopeApproval) {
        ScopeApprovalMongo scopeApprovalMongo = convert(scopeApproval);
        scopeApprovalMongo.setId(scopeApprovalMongo.getId() == null ? RandomString.generate() : scopeApprovalMongo.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(scopeApprovalsCollection.insertOne(scopeApprovalMongo))).flatMap(success->RxJava2Adapter.singleToMono(_findById(scopeApprovalMongo.getId())))));
    }

    @Deprecated
@Override
    public Single<ScopeApproval> update(ScopeApproval scopeApproval) {
 return RxJava2Adapter.monoToSingle(update_migrated(scopeApproval));
}
@Override
    public Mono<ScopeApproval> update_migrated(ScopeApproval scopeApproval) {
        ScopeApprovalMongo scopeApprovalMongo = convert(scopeApproval);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(scopeApprovalsCollection.replaceOne(
                and(eq(FIELD_DOMAIN, scopeApproval.getDomain()),
                        eq(FIELD_CLIENT_ID, scopeApproval.getClientId()),
                        eq(FIELD_USER_ID, scopeApproval.getUserId()),
                        eq(FIELD_SCOPE, scopeApproval.getScope()))
                , scopeApprovalMongo))).flatMap(updateResult->RxJava2Adapter.singleToMono(_findById(scopeApprovalMongo.getId())))));
    }

    @Override
    public Single<ScopeApproval> upsert(ScopeApproval scopeApproval) {
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(scopeApprovalsCollection.find(
                and(eq(FIELD_DOMAIN, scopeApproval.getDomain()),
                        eq(FIELD_CLIENT_ID, scopeApproval.getClientId()),
                        eq(FIELD_USER_ID, scopeApproval.getUserId()),
                        eq(FIELD_SCOPE, scopeApproval.getScope()))).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()))
                .flatMapSingle(optionalApproval -> {
                    if (!optionalApproval.isPresent()) {
                        scopeApproval.setCreatedAt(new Date());
                        scopeApproval.setUpdatedAt(scopeApproval.getCreatedAt());
                        return create(scopeApproval);
                    } else {
                        scopeApproval.setId(optionalApproval.get().getId());
                        scopeApproval.setUpdatedAt(new Date());
                        return update(scopeApproval);
                    }
                });
    }

    @Override
    public Completable deleteByDomainAndScopeKey(String domain, String scope) {
        return RxJava2Adapter.monoToCompletable(Mono.from(scopeApprovalsCollection.deleteMany(
                and(eq(FIELD_DOMAIN, domain), eq(FIELD_SCOPE, scope)))));
    }

    @Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(scopeApprovalsCollection.deleteOne(eq(FIELD_ID, id)))));
    }

    @Override
    public Completable deleteByDomainAndUserAndClient(String domain, String user, String client) {
        return RxJava2Adapter.monoToCompletable(Mono.from(scopeApprovalsCollection.deleteMany(
                and(eq(FIELD_DOMAIN, domain), eq(FIELD_USER_ID, user), eq(FIELD_CLIENT_ID, client)))));
    }

    @Override
    public Completable deleteByDomainAndUser(String domain, String user) {
        return RxJava2Adapter.monoToCompletable(Mono.from(scopeApprovalsCollection.deleteMany(
                and(eq(FIELD_DOMAIN, domain), eq(FIELD_USER_ID, user)))));
    }

    @Deprecated
private Single<ScopeApproval> _findById(String id) {
 return RxJava2Adapter.monoToSingle(_findById_migrated(id));
}
private Mono<ScopeApproval> _findById_migrated(String id) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(scopeApprovalsCollection.find(eq(FIELD_ID, id)).first())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    private ScopeApproval convert(ScopeApprovalMongo scopeApprovalMongo) {
        if (scopeApprovalMongo == null) {
            return null;
        }

        ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId(scopeApprovalMongo.getId());
        scopeApproval.setTransactionId(scopeApprovalMongo.getTransactionId());
        scopeApproval.setClientId(scopeApprovalMongo.getClientId());
        scopeApproval.setUserId(scopeApprovalMongo.getUserId());
        scopeApproval.setScope(scopeApprovalMongo.getScope());
        scopeApproval.setExpiresAt(scopeApprovalMongo.getExpiresAt());
        scopeApproval.setStatus(ScopeApproval.ApprovalStatus.valueOf(scopeApprovalMongo.getStatus().toUpperCase()));
        scopeApproval.setDomain(scopeApprovalMongo.getDomain());
        scopeApproval.setCreatedAt(scopeApprovalMongo.getCreatedAt());
        scopeApproval.setUpdatedAt(scopeApprovalMongo.getUpdatedAt());

        return scopeApproval;
    }

    private ScopeApprovalMongo convert(ScopeApproval scopeApproval) {
        if (scopeApproval == null) {
            return null;
        }

        ScopeApprovalMongo scopeApprovalMongo = new ScopeApprovalMongo();
        scopeApprovalMongo.setId(scopeApproval.getId());
        scopeApprovalMongo.setTransactionId(scopeApproval.getTransactionId());
        scopeApprovalMongo.setClientId(scopeApproval.getClientId());
        scopeApprovalMongo.setUserId(scopeApproval.getUserId());
        scopeApprovalMongo.setScope(scopeApproval.getScope());
        scopeApprovalMongo.setExpiresAt(scopeApproval.getExpiresAt());
        scopeApprovalMongo.setStatus(scopeApproval.getStatus().name().toUpperCase());
        scopeApprovalMongo.setDomain(scopeApproval.getDomain());
        scopeApprovalMongo.setCreatedAt(scopeApproval.getCreatedAt());
        scopeApprovalMongo.setUpdatedAt(scopeApproval.getUpdatedAt());

        return scopeApprovalMongo;
    }
}

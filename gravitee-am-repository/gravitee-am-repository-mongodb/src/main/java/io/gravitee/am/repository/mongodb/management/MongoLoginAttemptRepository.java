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

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.LoginAttempt;
import io.gravitee.am.repository.management.api.LoginAttemptRepository;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.gravitee.am.repository.mongodb.management.internal.model.LoginAttemptMongo;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoLoginAttemptRepository extends AbstractManagementMongoRepository implements LoginAttemptRepository {

    private static final String FIELD_IDP = "identityProvider";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_RESET_TIME = "expireAt";
    private MongoCollection<LoginAttemptMongo> loginAttemptsCollection;

    @PostConstruct
    public void init() {
        loginAttemptsCollection = mongoOperations.getCollection("login_attempts", LoginAttemptMongo.class);
        super.init(loginAttemptsCollection);
        super.createIndex(loginAttemptsCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_CLIENT, 1).append(FIELD_USERNAME, 1));

        // expire after index
        super.createIndex(loginAttemptsCollection, new Document(FIELD_RESET_TIME, 1), new IndexOptions().expireAfter(0L, TimeUnit.SECONDS));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<LoginAttempt> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<LoginAttempt> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(loginAttemptsCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @Deprecated
@Override
    public Maybe<LoginAttempt> findByCriteria(LoginAttemptCriteria criteria) {
 return RxJava2Adapter.monoToMaybe(findByCriteria_migrated(criteria));
}
@Override
    public Mono<LoginAttempt> findByCriteria_migrated(LoginAttemptCriteria criteria) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(loginAttemptsCollection.find(query(criteria)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<LoginAttempt> create(LoginAttempt item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<LoginAttempt> create_migrated(LoginAttempt item) {
        LoginAttemptMongo loginAttempt = convert(item);
        loginAttempt.setId(loginAttempt.getId() == null ? RandomString.generate() : loginAttempt.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(loginAttemptsCollection.insertOne(loginAttempt))).flatMap(success->RxJava2Adapter.maybeToMono(findById(loginAttempt.getId())).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<LoginAttempt> update(LoginAttempt item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<LoginAttempt> update_migrated(LoginAttempt item) {
        LoginAttemptMongo loginAttempt = convert(item);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(loginAttemptsCollection.replaceOne(eq(FIELD_ID, loginAttempt.getId()), loginAttempt))).flatMap(success->RxJava2Adapter.maybeToMono(findById(loginAttempt.getId())).single())));
    }

    @Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(loginAttemptsCollection.deleteOne(eq(FIELD_ID, id)))));
    }

    @Deprecated
@Override
    public Completable delete(LoginAttemptCriteria criteria) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(criteria));
}
@Override
    public Mono<Void> delete_migrated(LoginAttemptCriteria criteria) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.from(loginAttemptsCollection.deleteOne(query(criteria)))));
    }

    private Bson query(LoginAttemptCriteria criteria) {
        List<Bson> filters = new ArrayList<>();
        // domain
        if (criteria.domain() != null && !criteria.domain().isEmpty()) {
            filters.add(eq(FIELD_DOMAIN, criteria.domain()));
        }
        // client
        if (criteria.client() != null && !criteria.client().isEmpty()) {
            filters.add(eq(FIELD_CLIENT, criteria.client()));
        }
        // idp
        if (criteria.identityProvider() != null && !criteria.identityProvider().isEmpty()) {
            filters.add(eq(FIELD_IDP, criteria.identityProvider()));
        }
        // username
        if (criteria.username() != null && !criteria.username().isEmpty()) {
            filters.add(eq(FIELD_USERNAME, criteria.username()));
        }
        // build query
        Bson query = (filters.isEmpty()) ? new BasicDBObject() : and(filters);
        return query;
    }

    private LoginAttempt convert(LoginAttemptMongo loginAttemptMongo) {
        if (loginAttemptMongo == null) {
            return null;
        }
        LoginAttempt loginAttempt = new LoginAttempt();
        loginAttempt.setId(loginAttemptMongo.getId());
        loginAttempt.setDomain(loginAttemptMongo.getDomain());
        loginAttempt.setClient(loginAttemptMongo.getClient());
        loginAttempt.setIdentityProvider(loginAttemptMongo.getIdentityProvider());
        loginAttempt.setUsername(loginAttemptMongo.getUsername());
        loginAttempt.setAttempts(loginAttemptMongo.getAttempts());
        loginAttempt.setExpireAt(loginAttemptMongo.getExpireAt());
        loginAttempt.setCreatedAt(loginAttemptMongo.getCreatedAt());
        loginAttempt.setUpdatedAt(loginAttemptMongo.getUpdatedAt());
        return loginAttempt;
    }

    private LoginAttemptMongo convert(LoginAttempt loginAttempt) {
        if (loginAttempt == null) {
            return null;
        }
        LoginAttemptMongo loginAttemptMongo = new LoginAttemptMongo();
        loginAttemptMongo.setId(loginAttempt.getId());
        loginAttemptMongo.setDomain(loginAttempt.getDomain());
        loginAttemptMongo.setClient(loginAttempt.getClient());
        loginAttemptMongo.setIdentityProvider(loginAttempt.getIdentityProvider());
        loginAttemptMongo.setUsername(loginAttempt.getUsername());
        loginAttemptMongo.setAttempts(loginAttempt.getAttempts());
        loginAttemptMongo.setExpireAt(loginAttempt.getExpireAt());
        loginAttemptMongo.setCreatedAt(loginAttempt.getCreatedAt());
        loginAttemptMongo.setUpdatedAt(loginAttempt.getUpdatedAt());
        return loginAttemptMongo;
    }
}

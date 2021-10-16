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
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.repository.management.api.ScopeRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.ScopeMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.bson.conversions.Bson;
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
public class MongoScopeRepository extends AbstractManagementMongoRepository implements ScopeRepository {

    private static final String FIELD_KEY = "key";
    private MongoCollection<ScopeMongo> scopesCollection;

    @PostConstruct
    public void init() {
        scopesCollection = mongoOperations.getCollection("scopes", ScopeMongo.class);
        super.init(scopesCollection);
        super.createIndex(scopesCollection, new Document(FIELD_DOMAIN, 1));
        super.createIndex(scopesCollection, new Document(FIELD_DOMAIN, 1).append(FIELD_KEY, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Scope> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Scope> findById_migrated(String id) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(scopesCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Scope> create(Scope item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Scope> create_migrated(Scope item) {
        ScopeMongo scope = convert(item);
        scope.setId(scope.getId() == null ? RandomString.generate() : scope.getId());
        return RxJava2Adapter.singleToMono(Single.fromPublisher(scopesCollection.insertOne(scope))).flatMap(success->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(scope.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Scope> update(Scope item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Scope> update_migrated(Scope item) {
        ScopeMongo scope = convert(item);
        return RxJava2Adapter.singleToMono(Single.fromPublisher(scopesCollection.replaceOne(eq(FIELD_ID, scope.getId()), scope))).flatMap(updateResult->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findById_migrated(scope.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(scopesCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Scope>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<Scope>> findByDomain_migrated(String domain, int page, int size) {
        Bson mongoQuery = eq(FIELD_DOMAIN, domain);
        Single<Long> countOperation = Observable.fromPublisher(scopesCollection.countDocuments(mongoQuery)).first(0l);
        Single<List<Scope>> scopesOperation = Observable.fromPublisher(scopesCollection.find(mongoQuery).skip(size * page).limit(size)).map(this::convert).toList();
        return RxJava2Adapter.singleToMono(Single.zip(countOperation, scopesOperation, (count, scope) -> new Page<Scope>(scope, page, count)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Scope>> search(String domain, String query, int page, int size) {
 return RxJava2Adapter.monoToSingle(search_migrated(domain, query, page, size));
}
@Override
    public Mono<Page<Scope>> search_migrated(String domain, String query, int page, int size) {
        Bson searchQuery = eq(FIELD_KEY, query);

        // if query contains wildcard, use the regex query
        if (query.contains("*")) {
            String compactQuery = query.replaceAll("\\*+", ".*");
            String regex = "^" + compactQuery;
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            searchQuery = new BasicDBObject(FIELD_KEY, pattern);
        }

        Bson mongoQuery = and(
                eq(FIELD_DOMAIN, domain), searchQuery);

        Single<Long> countOperation = Observable.fromPublisher(scopesCollection.countDocuments(mongoQuery)).first(0l);
        Single<List<Scope>> scopesOperation = Observable.fromPublisher(scopesCollection.find(mongoQuery).sort(new BasicDBObject(FIELD_KEY, 1)).skip(size * page).limit(size)).map(this::convert).toList();
        return RxJava2Adapter.singleToMono(Single.zip(countOperation, scopesOperation, (count, scopes) -> new Page<>(scopes, page, count)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndKey_migrated(domain, key))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Scope> findByDomainAndKey(String domain, String key) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndKey_migrated(domain, key));
}
@Override
    public Mono<Scope> findByDomainAndKey_migrated(String domain, String key) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(scopesCollection.find(and(eq(FIELD_DOMAIN, domain), eq(FIELD_KEY, key))).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndKeys_migrated(domain, keys))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Scope> findByDomainAndKeys(String domain, List<String> keys) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndKeys_migrated(domain, keys));
}
@Override
    public Flux<Scope> findByDomainAndKeys_migrated(String domain, List<String> keys) {
        return Flux.from(scopesCollection.find(and(eq(FIELD_DOMAIN, domain), in(FIELD_KEY, keys)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    private Scope convert(ScopeMongo scopeMongo) {
        if (scopeMongo == null) {
            return null;
        }

        Scope scope = new Scope();
        scope.setId(scopeMongo.getId());
        scope.setKey(scopeMongo.getKey());
        scope.setName(scopeMongo.getName());
        scope.setDescription(scopeMongo.getDescription());
        scope.setIconUri(scopeMongo.getIconUri());
        scope.setDomain(scopeMongo.getDomain());
        scope.setSystem(scopeMongo.isSystem());
        scope.setClaims(scopeMongo.getClaims());
        scope.setExpiresIn(scopeMongo.getExpiresIn());
        scope.setCreatedAt(scopeMongo.getCreatedAt());
        scope.setUpdatedAt(scopeMongo.getUpdatedAt());
        scope.setDiscovery(scopeMongo.isDiscovery());
        scope.setParameterized(scopeMongo.isParameterized());

        return scope;
    }

    private ScopeMongo convert(Scope scope) {
        if (scope == null) {
            return null;
        }

        ScopeMongo scopeMongo = new ScopeMongo();
        scopeMongo.setId(scope.getId());
        scopeMongo.setKey(scope.getKey());
        scopeMongo.setName(scope.getName());
        scopeMongo.setDescription(scope.getDescription());
        scopeMongo.setIconUri(scope.getIconUri());
        scopeMongo.setDomain(scope.getDomain());
        scopeMongo.setSystem(scope.isSystem());
        scopeMongo.setClaims(scope.getClaims());
        scopeMongo.setExpiresIn(scope.getExpiresIn());
        scopeMongo.setCreatedAt(scope.getCreatedAt());
        scopeMongo.setUpdatedAt(scope.getUpdatedAt());
        scopeMongo.setDiscovery(scope.isDiscovery());
        scopeMongo.setParameterized(scope.isParameterized());

        return scopeMongo;
    }
}

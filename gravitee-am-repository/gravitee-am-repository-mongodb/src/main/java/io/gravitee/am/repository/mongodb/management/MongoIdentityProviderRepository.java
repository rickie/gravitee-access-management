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
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import com.google.errorprone.annotations.InlineMe;

import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.IdentityProviderRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.IdentityProviderMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import java.util.*;

import javax.annotation.PostConstruct;

import org.bson.BsonArray;
import org.bson.BsonString;
import org.bson.BsonValue;
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
public class MongoIdentityProviderRepository extends AbstractManagementMongoRepository implements IdentityProviderRepository {

    private MongoCollection<IdentityProviderMongo> identitiesCollection;

    @PostConstruct
    public void init() {
        identitiesCollection = mongoOperations.getCollection("identities", IdentityProviderMongo.class);
        super.init(identitiesCollection);
        super.createIndex(identitiesCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<IdentityProvider> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<IdentityProvider> findAll_migrated(ReferenceType referenceType, String referenceId) {
        return Flux.from(identitiesCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<IdentityProvider> findAll(ReferenceType referenceType) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
@Override
    public Flux<IdentityProvider> findAll_migrated(ReferenceType referenceType) {
        return Flux.from(identitiesCollection.find(eq(FIELD_REFERENCE_TYPE, referenceType.name()))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<IdentityProvider> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<IdentityProvider> findAll_migrated() {
        return Flux.from(identitiesCollection.find()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(identityProviderId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<IdentityProvider> findById(String identityProviderId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(identityProviderId));
}
@Override
    public Mono<IdentityProvider> findById_migrated(String identityProviderId) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(identitiesCollection.find(eq(FIELD_ID, identityProviderId)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, identityProviderId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<IdentityProvider> findById(ReferenceType referenceType, String referenceId, String identityProviderId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, identityProviderId));
}
@Override
    public Mono<IdentityProvider> findById_migrated(ReferenceType referenceType, String referenceId, String identityProviderId) {
        return RxJava2Adapter.observableToFlux(Observable.fromPublisher(identitiesCollection.find(and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId), eq(FIELD_ID, identityProviderId))).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<IdentityProvider> create(IdentityProvider item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<IdentityProvider> create_migrated(IdentityProvider item) {
        Optional<IdentityProviderMongo> optionalIdp = convert(item);
        if (optionalIdp.isPresent()) {
            var identityProvider = optionalIdp.get();
            final String id = identityProvider.getId() == null ? RandomString.generate() : identityProvider.getId();
            identityProvider.setId(id);
            return RxJava2Adapter.singleToMono(Single.fromPublisher(identitiesCollection.insertOne(identityProvider))).flatMap(success->findById_migrated(identityProvider.getId()).single());
        }
        return Mono.error(new TechnicalException("Identity provider must be present for create"));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<IdentityProvider> update(IdentityProvider item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<IdentityProvider> update_migrated(IdentityProvider item) {
        Optional<IdentityProviderMongo> optionalIdp = convert(item);
        if (optionalIdp.isPresent()) {
            var identityProvider = optionalIdp.get();
            return RxJava2Adapter.singleToMono(Single.fromPublisher(identitiesCollection.replaceOne(eq(FIELD_ID, identityProvider.getId()), identityProvider))).flatMap(updateResult->findById_migrated(identityProvider.getId()).single());
        }
        return Mono.error(new TechnicalException("Identity provider must be present for update"));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(identitiesCollection.deleteOne(eq(FIELD_ID, id))).then();
    }

    private IdentityProvider convert(IdentityProviderMongo identityProviderMongo) {
        if (isNull(identityProviderMongo)) {
            return null;
        }

        var identityProvider = new IdentityProvider();
        identityProvider.setId(identityProviderMongo.getId());
        identityProvider.setName(identityProviderMongo.getName());
        identityProvider.setType(identityProviderMongo.getType());
        identityProvider.setConfiguration(identityProviderMongo.getConfiguration());
        identityProvider.setMappers((Map) identityProviderMongo.getMappers());

        if (identityProviderMongo.getRoleMapper() != null) {
            Map<String, String[]> roleMapper = new HashMap<>(identityProviderMongo.getRoleMapper().size());
            identityProviderMongo.getRoleMapper().forEach((key, value) -> {
                List lstValue = (List) value;
                String[] arr = new String[lstValue.size()];
                lstValue.toArray(arr);
                roleMapper.put(key, arr);
            });
            identityProvider.setRoleMapper(roleMapper);
        }

        identityProvider.setReferenceType(identityProviderMongo.getReferenceType());
        identityProvider.setReferenceId(identityProviderMongo.getReferenceId());
        identityProvider.setExternal(identityProviderMongo.isExternal());
        identityProvider.setDomainWhitelist(
                ofNullable(identityProviderMongo.getDomainWhitelist()).orElse(new BsonArray())
                        .stream().map(BsonValue::asString).map(BsonString::getValue)
                        .collect(toList()));
        identityProvider.setCreatedAt(identityProviderMongo.getCreatedAt());
        identityProvider.setUpdatedAt(identityProviderMongo.getUpdatedAt());
        return identityProvider;
    }

    private Optional<IdentityProviderMongo> convert(IdentityProvider identityProvider) {
        return ofNullable(identityProvider).map(Objects::nonNull).map(idp -> {
            var identityProviderMongo = new IdentityProviderMongo();
            identityProviderMongo.setId(identityProvider.getId());
            identityProviderMongo.setName(identityProvider.getName());
            identityProviderMongo.setType(identityProvider.getType());
            identityProviderMongo.setConfiguration(identityProvider.getConfiguration());
            identityProviderMongo.setReferenceType(identityProvider.getReferenceType());
            identityProviderMongo.setReferenceId(identityProvider.getReferenceId());
            identityProviderMongo.setExternal(identityProvider.isExternal());
            identityProviderMongo.setCreatedAt(identityProvider.getCreatedAt());
            identityProviderMongo.setUpdatedAt(identityProvider.getUpdatedAt());
            var mappers = new Document((Map) ofNullable(identityProvider.getMappers()).filter(Objects::nonNull).orElse(Map.of()));
            var roleMapper = new Document(convert(ofNullable(identityProvider.getRoleMapper()).filter(Objects::nonNull).orElse(Map.of())));
            identityProviderMongo.setMappers(mappers);
            identityProviderMongo.setRoleMapper(roleMapper);
            identityProviderMongo.setDomainWhitelist(
                    ofNullable(identityProvider.getDomainWhitelist()).orElse(List.of()).stream()
                            .map(BsonString::new)
                            .collect(toCollection(BsonArray::new)));
            return identityProviderMongo;
        });
    }

    private Document convert(Map<String, String[]> map) {
        Document document = new Document();
        map.forEach((k, v) -> document.append(k, Arrays.asList(v)));
        return document;
    }
}

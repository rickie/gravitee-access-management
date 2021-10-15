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

import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Credential;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.management.api.CredentialRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.CredentialMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
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
public class MongoCredentialRepository extends AbstractManagementMongoRepository implements CredentialRepository {

    private static final String FIELD_USER_ID = "userId";
    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_CREDENTIAL_ID = "credentialId";
    private static final String FIELD_AAGUID = "aaguid";
    private MongoCollection<CredentialMongo> credentialsCollection;

    @PostConstruct
    public void init() {
        credentialsCollection = mongoOperations.getCollection("webauthn_credentials", CredentialMongo.class);
        super.init(credentialsCollection);
        super.createIndex(credentialsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_USER_ID, 1));
        super.createIndex(credentialsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_USERNAME, 1));
        super.createIndex(credentialsCollection, new Document(FIELD_REFERENCE_TYPE, 1).append(FIELD_REFERENCE_ID, 1).append(FIELD_CREDENTIAL_ID, 1));
    }

    @Override
    public Flowable<Credential> findByUserId(ReferenceType referenceType, String referenceId, String userId) {
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(credentialsCollection.find(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_USER_ID, userId)
                        )
                )))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Flowable<Credential> findByUsername(ReferenceType referenceType, String referenceId, String username) {
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(credentialsCollection.find(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_USERNAME, username)
                        )
                )))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Flowable<Credential> findByCredentialId(ReferenceType referenceType, String referenceId, String credentialId) {
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(credentialsCollection.find(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_CREDENTIAL_ID, credentialId)
                        )
                )))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Maybe<Credential> findById(String id) {
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(credentialsCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Single<Credential> create(Credential item) {
        CredentialMongo credential = convert(item);
        credential.setId(credential.getId() == null ? RandomString.generate() : credential.getId());
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(credentialsCollection.insertOne(credential))).flatMap(success->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(findById(credential.getId())).single()))));
    }

    @Override
    public Single<Credential> update(Credential item) {
        CredentialMongo credential = convert(item);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(credentialsCollection.replaceOne(eq(FIELD_ID, credential.getId()), credential))).flatMap(updateResult->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(findById(credential.getId())).single()))));
    }

    @Override
    public Completable delete(String id) {
        return RxJava2Adapter.monoToCompletable(Mono.from(credentialsCollection.deleteOne(eq(FIELD_ID, id))));
    }

    @Override
    public Completable deleteByUserId(ReferenceType referenceType, String referenceId, String userId) {
        return RxJava2Adapter.monoToCompletable(Mono.from(credentialsCollection.deleteMany(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_USER_ID, userId)
                        )
                )));
    }

    @Override
    public Completable deleteByAaguid(ReferenceType referenceType, String referenceId, String aaguid) {
        return RxJava2Adapter.monoToCompletable(Mono.from(credentialsCollection.deleteMany(
                        and(
                                eq(FIELD_REFERENCE_TYPE, referenceType.name()),
                                eq(FIELD_REFERENCE_ID, referenceId),
                                eq(FIELD_AAGUID, aaguid)
                        )
                )));
    }

    private Credential convert(CredentialMongo credentialMongo) {
        if (credentialMongo == null) {
            return null;
        }

        Credential credential = new Credential();
        credential.setId(credentialMongo.getId());
        credential.setReferenceType(credentialMongo.getReferenceType());
        credential.setReferenceId(credentialMongo.getReferenceId());
        credential.setUserId(credentialMongo.getUserId());
        credential.setUsername(credentialMongo.getUsername());
        credential.setCredentialId(credentialMongo.getCredentialId());
        credential.setPublicKey(credentialMongo.getPublicKey());
        credential.setCounter(credentialMongo.getCounter());
        credential.setAaguid(credentialMongo.getAaguid());
        credential.setAttestationStatementFormat(credentialMongo.getAttestationStatementFormat());
        credential.setAttestationStatement(credentialMongo.getAttestationStatement());
        credential.setIpAddress(credentialMongo.getIpAddress());
        credential.setUserAgent(credentialMongo.getUserAgent());
        credential.setCreatedAt(credentialMongo.getCreatedAt());
        credential.setUpdatedAt(credentialMongo.getUpdatedAt());
        credential.setAccessedAt(credentialMongo.getAccessedAt());
        return credential;
    }

    private CredentialMongo convert(Credential credential) {
        if (credential == null) {
            return null;
        }

        CredentialMongo credentialMongo = new CredentialMongo();
        credentialMongo.setId(credential.getId());
        credentialMongo.setReferenceType(credential.getReferenceType());
        credentialMongo.setReferenceId(credential.getReferenceId());
        credentialMongo.setUserId(credential.getUserId());
        credentialMongo.setUsername(credential.getUsername());
        credentialMongo.setCredentialId(credential.getCredentialId());
        credentialMongo.setPublicKey(credential.getPublicKey());
        if (credential.getCounter() != null) {
            credentialMongo.setCounter(credential.getCounter());
        }
        credentialMongo.setAaguid(credential.getAaguid());
        credentialMongo.setAttestationStatementFormat(credential.getAttestationStatementFormat());
        credentialMongo.setAttestationStatement(credential.getAttestationStatement());
        credentialMongo.setIpAddress(credential.getIpAddress());
        credentialMongo.setUserAgent(credential.getUserAgent());
        credentialMongo.setCreatedAt(credential.getCreatedAt());
        credentialMongo.setUpdatedAt(credential.getUpdatedAt());
        credentialMongo.setAccessedAt(credential.getAccessedAt());
        return credentialMongo;
    }
}

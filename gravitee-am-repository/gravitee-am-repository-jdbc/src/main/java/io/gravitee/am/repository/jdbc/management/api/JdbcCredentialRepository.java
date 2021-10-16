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
package io.gravitee.am.repository.jdbc.management.api;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.from;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToCompletable;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Credential;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcCredential;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringCredentialRepository;
import io.gravitee.am.repository.management.api.CredentialRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcCredentialRepository extends AbstractJdbcRepository implements CredentialRepository {
    @Autowired
    private SpringCredentialRepository credentialRepository;

    protected Credential toEntity(JdbcCredential entity) {
        return mapper.map(entity, Credential.class);
    }

    protected JdbcCredential toJdbcEntity(Credential entity) {
        return mapper.map(entity, JdbcCredential.class);
    }

    @Override
    public Flowable<Credential> findByUserId(ReferenceType referenceType, String referenceId, String userId) {
        LOGGER.debug("findByUserId({},{},{})", referenceType, referenceId, userId);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(credentialRepository.findByUserId(referenceType.name(), referenceId, userId)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Flowable<Credential> findByUsername(ReferenceType referenceType, String referenceId, String username) {
        LOGGER.debug("findByUsername({},{},{})", referenceType, referenceId, username);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(credentialRepository.findByUsername(referenceType.name(), referenceId, username)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));
    }

    @Override
    public Flowable<Credential> findByCredentialId(ReferenceType referenceType, String referenceId, String credentialId) {
        LOGGER.debug("findByCredentialId({},{},{})", referenceType, referenceId, credentialId);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(credentialRepository.findByCredentialId(referenceType.name(), referenceId, credentialId)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)));

    }

    @Deprecated
@Override
    public Maybe<Credential> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Credential> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(credentialRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to retrieve credential for Id {}", id, error)))));
    }

    @Deprecated
@Override
    public Single<Credential> create(Credential item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Credential> create_migrated(Credential item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create credential with id {}", item.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcCredential.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(action.flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single()).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("unable to create credential with id {}", item.getId(), error)))));
    }

    @Deprecated
@Override
    public Single<Credential> update(Credential item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Credential> update_migrated(Credential item) {
        LOGGER.debug("update credential with id {}", item.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.credentialRepository.save(toJdbcEntity(item))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("unable to create credential with id {}", item.getId(), error)))));
    }

    @Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("delete({})", id);
        return RxJava2Adapter.completableToMono(credentialRepository.deleteById(id).as(RxJava2Adapter::completableToMono).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete credential for Id {}", id, error))).as(RxJava2Adapter::monoToCompletable));

    }

    @Override
    public Completable deleteByUserId(ReferenceType referenceType, String referenceId, String userId) {
        LOGGER.debug("deleteByUserId({})", userId);
        return dbClient.delete()
                .from(JdbcCredential.class)
                .matching(from(
                        where("reference_type").is(referenceType.name())
                                .and(where("reference_id").is(referenceId))
                                .and(where("user_id").is(userId))))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete credential for userId {}", userId, error))).as(RxJava2Adapter::monoToCompletable);
    }

    @Override
    public Completable deleteByAaguid(ReferenceType referenceType, String referenceId, String aaguid) {
        LOGGER.debug("deleteByAaguid({})", aaguid);
        return dbClient.delete()
                .from(JdbcCredential.class)
                .matching(from(
                        where("reference_type").is(referenceType.name())
                                .and(where("reference_id").is(referenceId))
                                .and(where("aaguid").is(aaguid))))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete credential for aaguid {}", aaguid, error))).as(RxJava2Adapter::monoToCompletable);
    }
}

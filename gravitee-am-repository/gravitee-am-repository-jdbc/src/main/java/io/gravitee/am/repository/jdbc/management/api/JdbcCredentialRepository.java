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



import com.google.errorprone.annotations.InlineMe;
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
import reactor.core.publisher.Flux;
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

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByUserId_migrated(referenceType, referenceId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Credential> findByUserId(ReferenceType referenceType, String referenceId, String userId) {
 return RxJava2Adapter.fluxToFlowable(findByUserId_migrated(referenceType, referenceId, userId));
}
@Override
    public Flux<Credential> findByUserId_migrated(ReferenceType referenceType, String referenceId, String userId) {
        LOGGER.debug("findByUserId({},{},{})", referenceType, referenceId, userId);
        return credentialRepository.findByUserId_migrated(referenceType.name(), referenceId, userId).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByUsername_migrated(referenceType, referenceId, username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Credential> findByUsername(ReferenceType referenceType, String referenceId, String username) {
 return RxJava2Adapter.fluxToFlowable(findByUsername_migrated(referenceType, referenceId, username));
}
@Override
    public Flux<Credential> findByUsername_migrated(ReferenceType referenceType, String referenceId, String username) {
        LOGGER.debug("findByUsername({},{},{})", referenceType, referenceId, username);
        return credentialRepository.findByUsername_migrated(referenceType.name(), referenceId, username).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCredentialId_migrated(referenceType, referenceId, credentialId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Credential> findByCredentialId(ReferenceType referenceType, String referenceId, String credentialId) {
 return RxJava2Adapter.fluxToFlowable(findByCredentialId_migrated(referenceType, referenceId, credentialId));
}
@Override
    public Flux<Credential> findByCredentialId_migrated(ReferenceType referenceType, String referenceId, String credentialId) {
        LOGGER.debug("findByCredentialId({},{},{})", referenceType, referenceId, credentialId);
        return credentialRepository.findByCredentialId_migrated(referenceType.name(), referenceId, credentialId).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));

    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Credential> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Credential> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(credentialRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to retrieve credential for Id {}", id, error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
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

        return action.flatMap(i->this.findById_migrated(item.getId()).single()).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("unable to create credential with id {}", item.getId(), error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Credential> update(Credential item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Credential> update_migrated(Credential item) {
        LOGGER.debug("update credential with id {}", item.getId());
        return RxJava2Adapter.singleToMono(this.credentialRepository.save(toJdbcEntity(item))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("unable to create credential with id {}", item.getId(), error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("delete({})", id);
        return credentialRepository.deleteById(id).as(RxJava2Adapter::completableToMono).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete credential for Id {}", id, error)));

    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByUserId_migrated(referenceType, referenceId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByUserId(ReferenceType referenceType, String referenceId, String userId) {
 return RxJava2Adapter.monoToCompletable(deleteByUserId_migrated(referenceType, referenceId, userId));
}
@Override
    public Mono<Void> deleteByUserId_migrated(ReferenceType referenceType, String referenceId, String userId) {
        LOGGER.debug("deleteByUserId({})", userId);
        return dbClient.delete()
                .from(JdbcCredential.class)
                .matching(from(
                        where("reference_type").is(referenceType.name())
                                .and(where("reference_id").is(referenceId))
                                .and(where("user_id").is(userId))))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete credential for userId {}", userId, error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByAaguid_migrated(referenceType, referenceId, aaguid))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByAaguid(ReferenceType referenceType, String referenceId, String aaguid) {
 return RxJava2Adapter.monoToCompletable(deleteByAaguid_migrated(referenceType, referenceId, aaguid));
}
@Override
    public Mono<Void> deleteByAaguid_migrated(ReferenceType referenceType, String referenceId, String aaguid) {
        LOGGER.debug("deleteByAaguid({})", aaguid);
        return dbClient.delete()
                .from(JdbcCredential.class)
                .matching(from(
                        where("reference_type").is(referenceType.name())
                                .and(where("reference_id").is(referenceId))
                                .and(where("aaguid").is(aaguid))))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete credential for aaguid {}", aaguid, error)));
    }
}

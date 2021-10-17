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
package io.gravitee.am.service.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Credential;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.management.api.CredentialRepository;
import io.gravitee.am.service.CredentialService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.CredentialNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.reactivex.*;
import io.reactivex.Completable;

import io.reactivex.Maybe;
import io.reactivex.Single;


import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
public class CredentialServiceImpl implements CredentialService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialServiceImpl.class);

    @Lazy
    @Autowired
    private CredentialRepository credentialRepository;

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Credential> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Credential> findById_migrated(String id) {
        LOGGER.debug("Find credential by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(credentialRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a credential using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a credential using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByUserId_migrated(referenceType, referenceId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Credential> findByUserId(ReferenceType referenceType, String referenceId, String userId) {
 return RxJava2Adapter.fluxToFlowable(findByUserId_migrated(referenceType, referenceId, userId));
}
@Override
    public Flux<Credential> findByUserId_migrated(ReferenceType referenceType, String referenceId, String userId) {
        LOGGER.debug("Find credentials by {} {} and user id: {}", referenceType, referenceId, userId);
        return credentialRepository.findByUserId_migrated(referenceType, referenceId, userId).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a credential using {} {} and user id: {}", referenceType, referenceId, userId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a credential using %s %s and user id: %s", referenceType, referenceId, userId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByUsername_migrated(referenceType, referenceId, username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Credential> findByUsername(ReferenceType referenceType, String referenceId, String username) {
 return RxJava2Adapter.fluxToFlowable(findByUsername_migrated(referenceType, referenceId, username));
}
@Override
    public Flux<Credential> findByUsername_migrated(ReferenceType referenceType, String referenceId, String username) {
        LOGGER.debug("Find credentials by {} {} and username: {}", referenceType, referenceId, username);
        return credentialRepository.findByUsername_migrated(referenceType, referenceId, username).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a credential using {} {} and username: {}", referenceType, referenceId, username, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a credential using %s %s and username: %s", referenceType, referenceId, username), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCredentialId_migrated(referenceType, referenceId, credentialId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Credential> findByCredentialId(ReferenceType referenceType, String referenceId, String credentialId) {
 return RxJava2Adapter.fluxToFlowable(findByCredentialId_migrated(referenceType, referenceId, credentialId));
}
@Override
    public Flux<Credential> findByCredentialId_migrated(ReferenceType referenceType, String referenceId, String credentialId) {
        LOGGER.debug("Find credentials by {} {} and credential ID: {}", referenceType, referenceId, credentialId);
        return credentialRepository.findByCredentialId_migrated(referenceType, referenceId, credentialId).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a credential using {} {} and credential ID: {}", referenceType, referenceId, credentialId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a credential using %s %s and credential ID: %s", referenceType, referenceId, credentialId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(credential))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Credential> create(Credential credential) {
 return RxJava2Adapter.monoToSingle(create_migrated(credential));
}
@Override
    public Mono<Credential> create_migrated(Credential credential) {
        LOGGER.debug("Create a new credential {}", credential);
        return credentialRepository.create_migrated(credential).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Credential>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create a credential", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a credential", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(credential))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Credential> update(Credential credential) {
 return RxJava2Adapter.monoToSingle(update_migrated(credential));
}
@Override
    public Mono<Credential> update_migrated(Credential credential) {
        LOGGER.debug("Update a credential {}", credential);
        return credentialRepository.findById_migrated(credential.getId()).switchIfEmpty(Mono.error(new CredentialNotFoundException(credential.getId()))).flatMap(y->credentialRepository.update_migrated(credential)).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Credential>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a credential", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a credential", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.update_migrated(referenceType, referenceId, credentialId, credential))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable update(ReferenceType referenceType, String referenceId, String credentialId, Credential credential) {
 return RxJava2Adapter.monoToCompletable(update_migrated(referenceType, referenceId, credentialId, credential));
}
@Override
    public Mono<Void> update_migrated(ReferenceType referenceType, String referenceId, String credentialId, Credential credential) {
        LOGGER.debug("Update a credential {}", credentialId);
        return credentialRepository.findByCredentialId_migrated(referenceType, referenceId, credentialId).flatMap(e->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Credential, Single<Credential>>toJdkFunction(credentialToUpdate -> {
                    // update only business values (i.e not set via the vert.x authenticator object)
                    credentialToUpdate.setUserId(credential.getUserId());
                    credentialToUpdate.setIpAddress(credential.getIpAddress());
                    credentialToUpdate.setUserAgent(credential.getUserAgent());
                    credentialToUpdate.setUpdatedAt(new Date());
                    credentialToUpdate.setAccessedAt(credentialToUpdate.getUpdatedAt());
                    return RxJava2Adapter.monoToSingle(credentialRepository.update_migrated(credentialToUpdate));
                }).apply(e))).ignoreElements().then();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("Delete credential {}", id);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(credentialRepository.findById_migrated(id).switchIfEmpty(Mono.error(new CredentialNotFoundException(id))).flatMap(email->credentialRepository.delete_migrated(id)).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to delete credential: {}", id, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete credential: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByUserId_migrated(referenceType, referenceId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByUserId(ReferenceType referenceType, String referenceId, String userId) {
 return RxJava2Adapter.monoToCompletable(deleteByUserId_migrated(referenceType, referenceId, userId));
}
@Override
    public Mono<Void> deleteByUserId_migrated(ReferenceType referenceType, String referenceId, String userId) {
        LOGGER.debug("Delete credentials by {} {} and user id: {}", referenceType, referenceId, userId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(credentialRepository.deleteByUserId_migrated(referenceType, referenceId, userId))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to delete credentials using {} {} and user id: {}", referenceType, referenceId, userId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error has occurred while trying to delete credentials using: %s %s and user id: %s", referenceType, referenceId, userId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByAaguid_migrated(referenceType, referenceId, aaguid))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByAaguid(ReferenceType referenceType, String referenceId, String aaguid) {
 return RxJava2Adapter.monoToCompletable(deleteByAaguid_migrated(referenceType, referenceId, aaguid));
}
@Override
    public Mono<Void> deleteByAaguid_migrated(ReferenceType referenceType, String referenceId, String aaguid) {
        LOGGER.debug("Delete credentials by {} {} and aaguid: {}", referenceType, referenceId, aaguid);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(credentialRepository.deleteByAaguid_migrated(referenceType, referenceId, aaguid))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to delete credentials using {} {} and aaguid: {}", referenceType, referenceId, aaguid, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error has occurred while trying to delete credentials using: %s %s and aaguid: %s", referenceType, referenceId, aaguid), ex)));
                }));
    }
}

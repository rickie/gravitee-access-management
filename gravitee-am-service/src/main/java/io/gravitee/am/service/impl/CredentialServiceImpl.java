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

import io.gravitee.am.model.Credential;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.management.api.CredentialRepository;
import io.gravitee.am.service.CredentialService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.CredentialNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.reactivex.*;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import java.util.Date;
import java.util.List;
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

    @Override
    public Maybe<Credential> findById(String id) {
        LOGGER.debug("Find credential by ID: {}", id);
        return credentialRepository.findById(id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a credential using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a credential using its ID: %s", id), ex)));
                });
    }

    @Override
    public Flowable<Credential> findByUserId(ReferenceType referenceType, String referenceId, String userId) {
        LOGGER.debug("Find credentials by {} {} and user id: {}", referenceType, referenceId, userId);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(credentialRepository.findByUserId(referenceType, referenceId, userId)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a credential using {} {} and user id: {}", referenceType, referenceId, userId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a credential using %s %s and user id: %s", referenceType, referenceId, userId), ex)));
                })));
    }

    @Override
    public Flowable<Credential> findByUsername(ReferenceType referenceType, String referenceId, String username) {
        LOGGER.debug("Find credentials by {} {} and username: {}", referenceType, referenceId, username);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(credentialRepository.findByUsername(referenceType, referenceId, username)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a credential using {} {} and username: {}", referenceType, referenceId, username, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a credential using %s %s and username: %s", referenceType, referenceId, username), ex)));
                })));
    }

    @Override
    public Flowable<Credential> findByCredentialId(ReferenceType referenceType, String referenceId, String credentialId) {
        LOGGER.debug("Find credentials by {} {} and credential ID: {}", referenceType, referenceId, credentialId);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(credentialRepository.findByCredentialId(referenceType, referenceId, credentialId)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a credential using {} {} and credential ID: {}", referenceType, referenceId, credentialId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a credential using %s %s and credential ID: %s", referenceType, referenceId, credentialId), ex)));
                })));
    }

    @Override
    public Single<Credential> create(Credential credential) {
        LOGGER.debug("Create a new credential {}", credential);
        return credentialRepository.create(credential)
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create a credential", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a credential", ex)));
                });
    }

    @Override
    public Single<Credential> update(Credential credential) {
        LOGGER.debug("Update a credential {}", credential);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(credentialRepository.findById(credential.getId())).switchIfEmpty(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.error(new CredentialNotFoundException(credential.getId()))))))
                .flatMapSingle(__ -> credentialRepository.update(credential))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a credential", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a credential", ex)));
                });
    }

    @Override
    public Completable update(ReferenceType referenceType, String referenceId, String credentialId, Credential credential) {
        LOGGER.debug("Update a credential {}", credentialId);
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.flowableToFlux(credentialRepository.findByCredentialId(referenceType, referenceId, credentialId)
                .flatMapSingle(credentialToUpdate -> {
                    // update only business values (i.e not set via the vert.x authenticator object)
                    credentialToUpdate.setUserId(credential.getUserId());
                    credentialToUpdate.setIpAddress(credential.getIpAddress());
                    credentialToUpdate.setUserAgent(credential.getUserAgent());
                    credentialToUpdate.setUpdatedAt(new Date());
                    credentialToUpdate.setAccessedAt(credentialToUpdate.getUpdatedAt());
                    return credentialRepository.update(credentialToUpdate);
                })).ignoreElements().then());
    }

    @Override
    public Completable delete(String id) {
        LOGGER.debug("Delete credential {}", id);
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(credentialRepository.findById(id)).switchIfEmpty(RxJava2Adapter.maybeToMono(Maybe.wrap(Maybe.error(new CredentialNotFoundException(id))))).flatMap(email->RxJava2Adapter.completableToMono(credentialRepository.delete(id))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to delete credential: {}", id, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete credential: %s", id), ex)));
                });
    }

    @Override
    public Completable deleteByUserId(ReferenceType referenceType, String referenceId, String userId) {
        LOGGER.debug("Delete credentials by {} {} and user id: {}", referenceType, referenceId, userId);
        return credentialRepository.deleteByUserId(referenceType, referenceId, userId)
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to delete credentials using {} {} and user id: {}", referenceType, referenceId, userId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error has occurred while trying to delete credentials using: %s %s and user id: %s", referenceType, referenceId, userId), ex)));
                });
    }

    @Override
    public Completable deleteByAaguid(ReferenceType referenceType, String referenceId, String aaguid) {
        LOGGER.debug("Delete credentials by {} {} and aaguid: {}", referenceType, referenceId, aaguid);
        return credentialRepository.deleteByAaguid(referenceType, referenceId, aaguid)
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error has occurred while trying to delete credentials using {} {} and aaguid: {}", referenceType, referenceId, aaguid, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error has occurred while trying to delete credentials using: %s %s and aaguid: %s", referenceType, referenceId, aaguid), ex)));
                });
    }
}

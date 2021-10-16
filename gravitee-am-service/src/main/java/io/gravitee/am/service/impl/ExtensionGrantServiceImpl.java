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

import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.event.Action;
import io.gravitee.am.common.event.Type;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.ExtensionGrant;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.ExtensionGrantRepository;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.ExtensionGrantService;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewExtensionGrant;
import io.gravitee.am.service.model.UpdateExtensionGrant;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.ExtensionGrantAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
public class ExtensionGrantServiceImpl implements ExtensionGrantService {

    /**
     * Logger.
     */
    private final Logger LOGGER = LoggerFactory.getLogger(ExtensionGrantServiceImpl.class);

    @Lazy
    @Autowired
    private ExtensionGrantRepository extensionGrantRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @Deprecated
@Override
    public Maybe<ExtensionGrant> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<ExtensionGrant> findById_migrated(String id) {
        LOGGER.debug("Find extension grant by ID: {}", id);
        return RxJava2Adapter.maybeToMono(extensionGrantRepository.findById(id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find an extension grant using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find an extension grant using its ID: %s", id), ex)));
                }));
    }

    @Deprecated
@Override
    public Flowable<ExtensionGrant> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<ExtensionGrant> findByDomain_migrated(String domain) {
        LOGGER.debug("Find extension grants by domain: {}", domain);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(extensionGrantRepository.findByDomain(domain)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find extension grants by domain", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find extension grants by domain", ex)));
                }))));
    }

    @Deprecated
@Override
    public Single<ExtensionGrant> create(String domain, NewExtensionGrant newExtensionGrant, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newExtensionGrant, principal));
}
@Override
    public Mono<ExtensionGrant> create_migrated(String domain, NewExtensionGrant newExtensionGrant, User principal) {
        LOGGER.debug("Create a new extension grant {} for domain {}", newExtensionGrant, domain);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(extensionGrantRepository.findByDomainAndName(domain, newExtensionGrant.getName())).hasElement().flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Boolean, SingleSource<ExtensionGrant>>toJdkFunction(empty -> {
                    if (!empty) {
                        throw new ExtensionGrantAlreadyExistsException(newExtensionGrant.getName());
                    } else {
                        String extensionGrantId = RandomString.generate();
                        ExtensionGrant extensionGrant = new ExtensionGrant();
                        extensionGrant.setId(extensionGrantId);
                        extensionGrant.setDomain(domain);
                        extensionGrant.setName(newExtensionGrant.getName());
                        extensionGrant.setGrantType(newExtensionGrant.getGrantType());
                        extensionGrant.setIdentityProvider(newExtensionGrant.getIdentityProvider());
                        extensionGrant.setCreateUser(newExtensionGrant.isCreateUser());
                        extensionGrant.setUserExists(newExtensionGrant.isUserExists());
                        extensionGrant.setType(newExtensionGrant.getType());
                        extensionGrant.setConfiguration(newExtensionGrant.getConfiguration());
                        extensionGrant.setCreatedAt(new Date());
                        extensionGrant.setUpdatedAt(extensionGrant.getCreatedAt());

                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(extensionGrantRepository.create(extensionGrant)).flatMap(x->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<ExtensionGrant, SingleSource<ExtensionGrant>>toJdkFunction(extensionGrant1 -> {
                                    // create event for sync process
                                    Event event = new Event(Type.EXTENSION_GRANT, new Payload(extensionGrant1.getId(), ReferenceType.DOMAIN, extensionGrant1.getDomain(), Action.CREATE));
                                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(extensionGrant1)));
                                }).apply(x)))));

                    }
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a extension grant", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a extension grant", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(extensionGrant -> auditService.report(AuditBuilder.builder(ExtensionGrantAuditBuilder.class).principal(principal).type(EventType.EXTENSION_GRANT_CREATED).extensionGrant(extensionGrant)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ExtensionGrantAuditBuilder.class).principal(principal).type(EventType.EXTENSION_GRANT_CREATED).throwable(throwable))))));
    }

    @Deprecated
@Override
    public Single<ExtensionGrant> update(String domain, String id, UpdateExtensionGrant updateExtensionGrant, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateExtensionGrant, principal));
}
@Override
    public Mono<ExtensionGrant> update_migrated(String domain, String id, UpdateExtensionGrant updateExtensionGrant, User principal) {
        LOGGER.debug("Update a extension grant {} for domain {}", id, domain);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(extensionGrantRepository.findById(id)).switchIfEmpty(Mono.error(new ExtensionGrantNotFoundException(id))))
                .flatMapSingle(tokenGranter -> RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(extensionGrantRepository.findByDomainAndName(domain, updateExtensionGrant.getName())).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()).single().flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Optional<ExtensionGrant>, SingleSource<ExtensionGrant>>toJdkFunction(existingTokenGranter -> {
                            if (existingTokenGranter.isPresent() && !existingTokenGranter.get().getId().equals(id)) {
                                throw new ExtensionGrantAlreadyExistsException("Extension grant with the same name already exists");
                            }
                            return RxJava2Adapter.monoToSingle(Mono.just(tokenGranter));
                        }).apply(v))))))).flatMap(v->RxJava2Adapter.singleToMono((Single<ExtensionGrant>)RxJavaReactorMigrationUtil.toJdkFunction((Function<ExtensionGrant, Single<ExtensionGrant>>)oldExtensionGrant -> {
                    ExtensionGrant extensionGrantToUpdate = new ExtensionGrant(oldExtensionGrant);
                    extensionGrantToUpdate.setName(updateExtensionGrant.getName());
                    extensionGrantToUpdate.setGrantType(updateExtensionGrant.getGrantType() != null ? updateExtensionGrant.getGrantType() : oldExtensionGrant.getGrantType());
                    extensionGrantToUpdate.setIdentityProvider(updateExtensionGrant.getIdentityProvider());
                    extensionGrantToUpdate.setCreateUser(updateExtensionGrant.isCreateUser());
                    extensionGrantToUpdate.setUserExists(updateExtensionGrant.isUserExists());
                    extensionGrantToUpdate.setConfiguration(updateExtensionGrant.getConfiguration());
                    extensionGrantToUpdate.setUpdatedAt(new Date());

                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(extensionGrantRepository.update(extensionGrantToUpdate)).flatMap(z->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<ExtensionGrant, SingleSource<ExtensionGrant>>toJdkFunction(extensionGrant -> {
                                // create event for sync process
                                Event event = new Event(Type.EXTENSION_GRANT, new Payload(extensionGrant.getId(), ReferenceType.DOMAIN, extensionGrant.getDomain(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(extensionGrant)));
                            }).apply(z)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(extensionGrant -> auditService.report(AuditBuilder.builder(ExtensionGrantAuditBuilder.class).principal(principal).type(EventType.EXTENSION_GRANT_UPDATED).oldValue(oldExtensionGrant).extensionGrant(extensionGrant)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ExtensionGrantAuditBuilder.class).principal(principal).type(EventType.EXTENSION_GRANT_UPDATED).throwable(throwable)))));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a extension grant", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a extension grant", ex)));
                }));
    }

    @Deprecated
@Override
    public Completable delete(String domain, String extensionGrantId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(domain, extensionGrantId, principal));
}
@Override
    public Mono<Void> delete_migrated(String domain, String extensionGrantId, User principal) {
        LOGGER.debug("Delete extension grant {}", extensionGrantId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(extensionGrantRepository.findById(extensionGrantId)).switchIfEmpty(Mono.error(new ExtensionGrantNotFoundException(extensionGrantId))))
                // check for clients using this extension grant
                .flatMapSingle(extensionGrant -> RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(applicationService.findByDomainAndExtensionGrant(domain, extensionGrant.getGrantType() + "~" + extensionGrant.getId())).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Set<Application>, SingleSource<ExtensionGrant>>toJdkFunction(applications -> {
                            if (applications.size() > 0) {
                                throw new ExtensionGrantWithApplicationsException();
                            }
                            // backward compatibility, check for old clients configuration
                            return Single.zip(
                                    applicationService.findByDomainAndExtensionGrant(domain, extensionGrant.getGrantType()),
                                    RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(findByDomain(domain)).collectList()),
                                    (clients1, extensionGrants) -> {
                                        if (clients1.size() == 0) {
                                            return extensionGrant;
                                        }
                                        // if clients use this grant_type, check it is the oldest one
                                        Date minDate = Collections.min(extensionGrants.stream().map(ExtensionGrant::getCreatedAt).collect(Collectors.toList()));
                                        if (extensionGrant.getCreatedAt().equals(minDate)) {
                                            throw new ExtensionGrantWithApplicationsException();
                                        } else {
                                            return extensionGrant;
                                        }
                                    });
                        }).apply(v))))))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<ExtensionGrant, CompletableSource>)extensionGrant -> {
                    // create event for sync process
                    Event event = new Event(Type.EXTENSION_GRANT, new Payload(extensionGrantId, ReferenceType.DOMAIN, domain, Action.DELETE));
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(extensionGrantRepository.delete(extensionGrantId)).then(RxJava2Adapter.singleToMono(eventService.create(event))))
                            .toCompletable()
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(ExtensionGrantAuditBuilder.class).principal(principal).type(EventType.EXTENSION_GRANT_DELETED).extensionGrant(extensionGrant)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ExtensionGrantAuditBuilder.class).principal(principal).type(EventType.EXTENSION_GRANT_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to extension grant: {}", extensionGrantId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete extension grant: %s", extensionGrantId), ex)));
                }));
    }

}

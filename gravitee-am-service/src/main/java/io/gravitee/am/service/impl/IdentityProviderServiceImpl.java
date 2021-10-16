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
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.event.Action;
import io.gravitee.am.common.event.Type;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.IdentityProviderRepository;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.IdentityProviderService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.IdentityProviderNotFoundException;
import io.gravitee.am.service.exception.IdentityProviderWithApplicationsException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewIdentityProvider;
import io.gravitee.am.service.model.UpdateIdentityProvider;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.IdentityProviderAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.ArrayList;
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
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class IdentityProviderServiceImpl implements IdentityProviderService {

    /**
     * Logger.
     */
    private final Logger LOGGER = LoggerFactory.getLogger(IdentityProviderServiceImpl.class);

    @Lazy
    @Autowired
    private IdentityProviderRepository identityProviderRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<IdentityProvider> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<IdentityProvider> findAll_migrated() {
        LOGGER.debug("Find all identity providers");
        return identityProviderRepository.findAll_migrated().onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find all identity providers", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find all identity providers", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<IdentityProvider> findById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<IdentityProvider> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
        LOGGER.debug("Find identity provider by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(identityProviderRepository.findById_migrated(referenceType, referenceId, id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find an identity provider using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find an identity provider using its ID: %s", id), ex)));
                })).switchIfEmpty(Mono.error(new IdentityProviderNotFoundException(id)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<IdentityProvider> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<IdentityProvider> findById_migrated(String id) {
        LOGGER.debug("Find identity provider by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(identityProviderRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find an identity provider using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find an identity provider using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<IdentityProvider> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<IdentityProvider> findAll_migrated(ReferenceType referenceType, String referenceId) {
        LOGGER.debug("Find identity providers by {}: {}", referenceType, referenceId);
        return identityProviderRepository.findAll_migrated(referenceType, referenceId).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find identity providers by domain", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find identity providers by " + referenceType.name(), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<IdentityProvider> findAll(ReferenceType referenceType) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
@Override
    public Flux<IdentityProvider> findAll_migrated(ReferenceType referenceType) {
        LOGGER.debug("Find identity providers by type {}", referenceType);
        return identityProviderRepository.findAll_migrated(referenceType);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<IdentityProvider> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<IdentityProvider> findByDomain_migrated(String domain) {
        return findAll_migrated(ReferenceType.DOMAIN, domain);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<IdentityProvider> create(ReferenceType referenceType, String referenceId, NewIdentityProvider newIdentityProvider, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newIdentityProvider, principal));
}
@Override
    public Mono<IdentityProvider> create_migrated(ReferenceType referenceType, String referenceId, NewIdentityProvider newIdentityProvider, User principal) {
        LOGGER.debug("Create a new identity provider {} for {} {}", newIdentityProvider, referenceType, referenceId);

        var identityProvider = new IdentityProvider();
        identityProvider.setId(newIdentityProvider.getId() == null ? RandomString.generate() : newIdentityProvider.getId());
        identityProvider.setReferenceType(referenceType);
        identityProvider.setReferenceId(referenceId);
        identityProvider.setName(newIdentityProvider.getName());
        identityProvider.setType(newIdentityProvider.getType());
        identityProvider.setConfiguration(newIdentityProvider.getConfiguration());
        identityProvider.setExternal(newIdentityProvider.isExternal());
        identityProvider.setDomainWhitelist(newIdentityProvider.getDomainWhitelist());
        identityProvider.setCreatedAt(new Date());
        identityProvider.setUpdatedAt(identityProvider.getCreatedAt());

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(identityProviderRepository.create_migrated(identityProvider).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<IdentityProvider, SingleSource<IdentityProvider>>toJdkFunction(identityProvider1 -> {
                    // create event for sync process
                    Event event = new Event(Type.IDENTITY_PROVIDER, new Payload(identityProvider1.getId(), identityProvider1.getReferenceType(), identityProvider1.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(identityProvider1)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to create an identity provider", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create an identity provider", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(identityProvider1 -> auditService.report(AuditBuilder.builder(IdentityProviderAuditBuilder.class).principal(principal).type(EventType.IDENTITY_PROVIDER_CREATED).identityProvider(identityProvider1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(IdentityProviderAuditBuilder.class).principal(principal).type(EventType.IDENTITY_PROVIDER_CREATED).throwable(throwable))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<IdentityProvider> create(String domain, NewIdentityProvider newIdentityProvider, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newIdentityProvider, principal));
}
@Override
    public Mono<IdentityProvider> create_migrated(String domain, NewIdentityProvider newIdentityProvider, User principal) {

        return create_migrated(ReferenceType.DOMAIN, domain, newIdentityProvider, principal);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<IdentityProvider> update(ReferenceType referenceType, String referenceId, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateIdentityProvider, principal));
}
@Override
    public Mono<IdentityProvider> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {
        LOGGER.debug("Update an identity provider {} for {} {}", id, referenceType, referenceId);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(identityProviderRepository.findById_migrated(referenceType, referenceId, id).switchIfEmpty(Mono.error(new IdentityProviderNotFoundException(id))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<IdentityProvider, SingleSource<IdentityProvider>>toJdkFunction(oldIdentity -> {
                    IdentityProvider identityToUpdate = new IdentityProvider(oldIdentity);
                    identityToUpdate.setName(updateIdentityProvider.getName());
                    identityToUpdate.setConfiguration(updateIdentityProvider.getConfiguration());
                    identityToUpdate.setMappers(updateIdentityProvider.getMappers());
                    identityToUpdate.setRoleMapper(updateIdentityProvider.getRoleMapper());
                    identityToUpdate.setDomainWhitelist(updateIdentityProvider.getDomainWhitelist());
                    identityToUpdate.setUpdatedAt(new Date());

                    return RxJava2Adapter.monoToSingle(identityProviderRepository.update_migrated(identityToUpdate).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<IdentityProvider, SingleSource<IdentityProvider>>toJdkFunction(identityProvider1 -> {
                                // create event for sync process
                                Event event = new Event(Type.IDENTITY_PROVIDER, new Payload(identityProvider1.getId(), identityProvider1.getReferenceType(), identityProvider1.getReferenceId(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(identityProvider1)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(identityProvider1 -> auditService.report(AuditBuilder.builder(IdentityProviderAuditBuilder.class).principal(principal).type(EventType.IDENTITY_PROVIDER_UPDATED).oldValue(oldIdentity).identityProvider(identityProvider1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(IdentityProviderAuditBuilder.class).principal(principal).type(EventType.IDENTITY_PROVIDER_UPDATED).throwable(throwable)))));
                }).apply(y)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update an identity provider", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update an identity provider", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateIdentityProvider, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<IdentityProvider> update(String domain, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateIdentityProvider, principal));
}
@Override
    public Mono<IdentityProvider> update_migrated(String domain, String id, UpdateIdentityProvider updateIdentityProvider, User principal) {

        return update_migrated(ReferenceType.DOMAIN, domain, id, updateIdentityProvider, principal);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, identityProviderId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(ReferenceType referenceType, String referenceId, String identityProviderId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, identityProviderId, principal));
}
@Override
    public Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String identityProviderId, User principal) {
        LOGGER.debug("Delete identity provider {}", identityProviderId);

        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(identityProviderRepository.findById_migrated(referenceType, referenceId, identityProviderId).switchIfEmpty(Mono.error(new IdentityProviderNotFoundException(identityProviderId))).flatMap(y->RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(applicationService.findByIdentityProvider_migrated(identityProviderId)).count()).flatMap((java.lang.Long v)->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long applications)->{
if (applications > 0) {
throw new IdentityProviderWithApplicationsException();
}
return RxJava2Adapter.monoToSingle(Mono.just(y));
}).apply(v))))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<IdentityProvider, CompletableSource>)identityProvider -> {

                    // create event for sync process
                    Event event = new Event(Type.IDENTITY_PROVIDER, new Payload(identityProviderId, referenceType, referenceId, Action.DELETE));

                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(identityProviderRepository.delete_migrated(identityProviderId).then(eventService.create_migrated(event))).toCompletable()
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(IdentityProviderAuditBuilder.class).principal(principal).type(EventType.IDENTITY_PROVIDER_DELETED).identityProvider(identityProvider)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(IdentityProviderAuditBuilder.class).principal(principal).type(EventType.IDENTITY_PROVIDER_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete identity provider: {}", identityProviderId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete identity provider: %s", identityProviderId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, identityProviderId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String domain, String identityProviderId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(domain, identityProviderId, principal));
}
@Override
    public Mono<Void> delete_migrated(String domain, String identityProviderId, User principal) {

        return delete_migrated(ReferenceType.DOMAIN, domain, identityProviderId, principal);
    }
}

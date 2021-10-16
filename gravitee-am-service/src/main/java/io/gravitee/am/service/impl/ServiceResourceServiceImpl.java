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
import io.gravitee.am.model.Factor;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.model.resource.ServiceResource;
import io.gravitee.am.repository.management.api.ServiceResourceRepository;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.FactorService;
import io.gravitee.am.service.ServiceResourceService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.ServiceResourceCurrentlyUsedException;
import io.gravitee.am.service.exception.ServiceResourceNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewServiceResource;
import io.gravitee.am.service.model.UpdateServiceResource;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.ServiceResourceAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
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
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ServiceResourceServiceImpl implements ServiceResourceService {

    /**
     * Logger.
     */
    private final Logger LOGGER = LoggerFactory.getLogger(ServiceResourceServiceImpl.class);

    @Lazy
    @Autowired
    private ServiceResourceRepository serviceResourceRepository;

    @Autowired
    private FactorService factorService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ServiceResource> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<ServiceResource> findById_migrated(String id) {
        LOGGER.debug("Find resource by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(serviceResourceRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a resource using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a resource using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<ServiceResource> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<ServiceResource> findByDomain_migrated(String domain) {
        LOGGER.debug("Find resources by domain: {}", domain);
        return serviceResourceRepository.findByReference_migrated(ReferenceType.DOMAIN, domain).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find resources by domain", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find resources by domain", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newServiceResource, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ServiceResource> create(String domain, NewServiceResource newServiceResource, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newServiceResource, principal));
}
@Override
    public Mono<ServiceResource> create_migrated(String domain, NewServiceResource newServiceResource, User principal) {
        LOGGER.debug("Create a new resource {} for domain {}", newServiceResource, domain);
        ServiceResource resource = new ServiceResource();
        resource.setId(newServiceResource.getId() == null ? RandomString.generate() : newServiceResource.getId());
        resource.setReferenceId(domain);
        resource.setReferenceType(ReferenceType.DOMAIN);
        resource.setName(newServiceResource.getName());
        resource.setType(newServiceResource.getType());
        resource.setConfiguration(newServiceResource.getConfiguration());
        resource.setCreatedAt(new Date());
        resource.setUpdatedAt(resource.getCreatedAt());

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(serviceResourceRepository.create_migrated(resource).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<ServiceResource, SingleSource<ServiceResource>>toJdkFunction(resource1 -> {
                    // send sync event to refresh plugins that are using this resource
                    Event event = new Event(Type.RESOURCE, new Payload(resource1.getId(), resource1.getReferenceType(), resource1.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(resource1)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a resource", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a resource", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(factor1 -> auditService.report(AuditBuilder.builder(ServiceResourceAuditBuilder.class).principal(principal).type(EventType.RESOURCE_CREATED).resource(factor1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ServiceResourceAuditBuilder.class).principal(principal).type(EventType.RESOURCE_CREATED).throwable(throwable))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateResource, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ServiceResource> update(String domain, String id, UpdateServiceResource updateResource, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateResource, principal));
}
@Override
    public Mono<ServiceResource> update_migrated(String domain, String id, UpdateServiceResource updateResource, User principal) {
        LOGGER.debug("Update a resource {} for domain {}", id, domain);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(serviceResourceRepository.findById_migrated(id).switchIfEmpty(Mono.error(new ServiceResourceNotFoundException(id))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<ServiceResource, SingleSource<ServiceResource>>toJdkFunction(oldServiceResource -> {
                    ServiceResource factorToUpdate = new ServiceResource(oldServiceResource);
                    factorToUpdate.setName(updateResource.getName());
                    factorToUpdate.setConfiguration(updateResource.getConfiguration());
                    factorToUpdate.setUpdatedAt(new Date());

                    return RxJava2Adapter.monoToSingle(serviceResourceRepository.update_migrated(factorToUpdate).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<ServiceResource, SingleSource<ServiceResource>>toJdkFunction(resource1 -> {
                                // send sync event to refresh plugins that are using this resource
                                Event event = new Event(Type.RESOURCE, new Payload(resource1.getId(), resource1.getReferenceType(), resource1.getReferenceId(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(resource1)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(factor1 -> auditService.report(AuditBuilder.builder(ServiceResourceAuditBuilder.class).principal(principal).type(EventType.RESOURCE_UPDATED).oldValue(oldServiceResource).resource(factor1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ServiceResourceAuditBuilder.class).principal(principal).type(EventType.RESOURCE_UPDATED).throwable(throwable)))));
                }).apply(y)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a resource", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a resource", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, resourceId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String domain, String resourceId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(domain, resourceId, principal));
}
@Override
    public Mono<Void> delete_migrated(String domain, String resourceId, User principal) {
        LOGGER.debug("Delete resource {}", resourceId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(serviceResourceRepository.findById_migrated(resourceId).switchIfEmpty(Mono.error(new ServiceResourceNotFoundException(resourceId))).flatMap(y->factorService.findByDomain_migrated(domain).filter(RxJavaReactorMigrationUtil.toJdkPredicate((io.gravitee.am.model.Factor factor)->factor.getConfiguration() != null && factor.getConfiguration().contains("\"" + resourceId + "\""))).collectList().flatMap((java.util.List<io.gravitee.am.model.Factor> v)->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<io.gravitee.am.model.Factor> factors)->{
if (factors.isEmpty()) {
return RxJava2Adapter.monoToSingle(Mono.just(y));
} else {
return RxJava2Adapter.monoToSingle(Mono.error(new ServiceResourceCurrentlyUsedException(resourceId, factors.get(0).getName(), "MultiFactor Authentication")));
}
}).apply(v))))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<ServiceResource, CompletableSource>)resource -> {
                            Event event = new Event(Type.RESOURCE, new Payload(resource.getId(), resource.getReferenceType(), resource.getReferenceId(), Action.DELETE));
                            return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(serviceResourceRepository.delete_migrated(resourceId).then(eventService.create_migrated(event)).then())
                                    .doOnComplete(() -> auditService.report(AuditBuilder.builder(ServiceResourceAuditBuilder.class).principal(principal).type(EventType.RESOURCE_DELETED).resource(resource)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ServiceResourceAuditBuilder.class).principal(principal).type(EventType.RESOURCE_DELETED).throwable(throwable)))));
                        }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete resource: {}", resourceId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete resource: %s", resourceId), ex)));
                }));
    }
}

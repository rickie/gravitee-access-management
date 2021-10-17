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
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Entrypoint;
import io.gravitee.am.model.Organization;
import io.gravitee.am.repository.management.api.EntrypointRepository;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EntrypointService;
import io.gravitee.am.service.OrganizationService;
import io.gravitee.am.service.exception.EntrypointNotFoundException;
import io.gravitee.am.service.exception.InvalidEntrypointException;
import io.gravitee.am.service.model.NewEntrypoint;
import io.gravitee.am.service.model.UpdateEntrypoint;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.EntrypointAuditBuilder;
import io.gravitee.am.service.validators.VirtualHostValidator;
import io.gravitee.common.utils.UUID;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class EntrypointServiceImpl implements EntrypointService {

    private final Logger LOGGER = LoggerFactory.getLogger(EntrypointServiceImpl.class);

    private final EntrypointRepository entrypointRepository;

    private final OrganizationService organizationService;

    private final AuditService auditService;

    public EntrypointServiceImpl(@Lazy EntrypointRepository entrypointRepository,
                                 @Lazy OrganizationService organizationService,
                                 AuditService auditService) {
        this.entrypointRepository = entrypointRepository;
        this.organizationService = organizationService;
        this.auditService = auditService;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(id, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Entrypoint> findById(String id, String organizationId) {
 return RxJava2Adapter.monoToSingle(findById_migrated(id, organizationId));
}
@Override
    public Mono<Entrypoint> findById_migrated(String id, String organizationId) {

        LOGGER.debug("Find entrypoint by id {} and organizationId {}", id, organizationId);

        return entrypointRepository.findById_migrated(id, organizationId).switchIfEmpty(Mono.error(new EntrypointNotFoundException(id)));
    }

    
@Override
    public Flux<Entrypoint> findAll_migrated(String organizationId) {

        LOGGER.debug("Find all entrypoints by organizationId {}", organizationId);

        return entrypointRepository.findAll_migrated(organizationId);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(organizationId, newEntrypoint, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Entrypoint> create(String organizationId, NewEntrypoint newEntrypoint, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(organizationId, newEntrypoint, principal));
}
@Override
    public Mono<Entrypoint> create_migrated(String organizationId, NewEntrypoint newEntrypoint, User principal) {

        LOGGER.debug("Create a new entrypoint {} for organization {}", newEntrypoint, organizationId);

        Entrypoint toCreate = new Entrypoint();
        toCreate.setOrganizationId(organizationId);
        toCreate.setName(newEntrypoint.getName());
        toCreate.setDescription(newEntrypoint.getDescription());
        toCreate.setUrl(newEntrypoint.getUrl());
        toCreate.setTags(newEntrypoint.getTags());

        return createInternal_migrated(toCreate, principal);
    }

    
@Override
    public Flux<Entrypoint> createDefaults_migrated(Organization organization) {

        List<Single<Entrypoint>> toCreateObsList = new ArrayList<>();

        if (CollectionUtils.isEmpty(organization.getDomainRestrictions())) {
            Entrypoint toCreate = new Entrypoint();
            toCreate.setName("Default");
            toCreate.setDescription("Default entrypoint");
            toCreate.setUrl("https://auth.company.com");
            toCreate.setTags(Collections.emptyList());
            toCreate.setOrganizationId(organization.getId());
            toCreate.setDefaultEntrypoint(true);

            toCreateObsList.add(RxJava2Adapter.monoToSingle(createInternal_migrated(toCreate, null)));
        } else {
            for (int i = 0; i < organization.getDomainRestrictions().size(); i++) {
                Entrypoint toCreate = new Entrypoint();
                String domainRestriction = organization.getDomainRestrictions().get(i);
                toCreate.setName(domainRestriction);
                toCreate.setDescription("Entrypoint " + domainRestriction);
                toCreate.setUrl("https://" + domainRestriction);
                toCreate.setTags(Collections.emptyList());
                toCreate.setOrganizationId(organization.getId());
                toCreate.setDefaultEntrypoint(i == 0);
                toCreateObsList.add(RxJava2Adapter.monoToSingle(createInternal_migrated(toCreate, null)));
            }
        }

        return RxJava2Adapter.flowableToFlux(Single.mergeDelayError(toCreateObsList));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(entrypointId, organizationId, updateEntrypoint, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Entrypoint> update(String entrypointId, String organizationId, UpdateEntrypoint updateEntrypoint, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(entrypointId, organizationId, updateEntrypoint, principal));
}
@Override
    public Mono<Entrypoint> update_migrated(String entrypointId, String organizationId, UpdateEntrypoint updateEntrypoint, User principal) {

        LOGGER.debug("Update an existing entrypoint {}", updateEntrypoint);

        return findById_migrated(entrypointId, organizationId).flatMap(v->RxJava2Adapter.singleToMono((Single<Entrypoint>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Entrypoint, Single<Entrypoint>>)oldEntrypoint -> {
                    Entrypoint toUpdate = new Entrypoint(oldEntrypoint);
                    toUpdate.setName(updateEntrypoint.getName());
                    toUpdate.setDescription(updateEntrypoint.getDescription());
                    toUpdate.setUrl(updateEntrypoint.getUrl());
                    toUpdate.setTags(updateEntrypoint.getTags());
                    toUpdate.setUpdatedAt(new Date());

                    return RxJava2Adapter.monoToSingle(validate_migrated(toUpdate, oldEntrypoint).then(entrypointRepository.update_migrated(toUpdate).doOnSuccess(updated -> auditService.report(AuditBuilder.builder(EntrypointAuditBuilder.class).principal(principal).type(EventType.ENTRYPOINT_UPDATED).entrypoint(updated).oldValue(oldEntrypoint))).doOnError(throwable -> auditService.report(AuditBuilder.builder(EntrypointAuditBuilder.class).principal(principal).type(EventType.ENTRYPOINT_UPDATED).throwable(throwable)))));
                }).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id, organizationId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id, String organizationId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id, organizationId, principal));
}
@Override
    public Mono<Void> delete_migrated(String id, String organizationId, User principal) {

        LOGGER.debug("Delete entrypoint by id {} and organizationId {}", id, organizationId);

        return findById_migrated(id, organizationId).flatMap(entrypoint->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(entrypointRepository.delete_migrated(id)).doOnComplete(()->auditService.report(AuditBuilder.builder(EntrypointAuditBuilder.class).principal(principal).type(EventType.ENTRYPOINT_DELETED).entrypoint(entrypoint)))).doOnError((Throwable throwable)->auditService.report(AuditBuilder.builder(EntrypointAuditBuilder.class).principal(principal).type(EventType.ENTRYPOINT_DELETED).throwable(throwable)))).then();
    }

    
private Mono<Entrypoint> createInternal_migrated(Entrypoint toCreate, User principal) {

        Date now = new Date();

        toCreate.setId(UUID.random().toString());
        toCreate.setCreatedAt(now);
        toCreate.setUpdatedAt(now);

        return validate_migrated(toCreate).then(entrypointRepository.create_migrated(toCreate).doOnSuccess(entrypoint -> auditService.report(AuditBuilder.builder(EntrypointAuditBuilder.class).entrypoint(entrypoint).principal(principal).type(EventType.ENTRYPOINT_CREATED))).doOnError(throwable -> auditService.report(AuditBuilder.builder(EntrypointAuditBuilder.class).referenceId(toCreate.getOrganizationId()).principal(principal).type(EventType.ENTRYPOINT_CREATED).throwable(throwable))));
    }

    
private Mono<Void> validate_migrated(Entrypoint entrypoint) {
        return validate_migrated(entrypoint, null);
    }

    
private Mono<Void> validate_migrated(Entrypoint entrypoint, Entrypoint oldEntrypoint) {

        if (oldEntrypoint != null && oldEntrypoint.isDefaultEntrypoint()) {
            // Only the url of the default entrypoint can be updated.
            if (!entrypoint.getName().equals(oldEntrypoint.getName())
                    || !entrypoint.getDescription().equals(oldEntrypoint.getDescription())
                    || !entrypoint.getTags().equals(oldEntrypoint.getTags())) {
                return Mono.error(new InvalidEntrypointException("Only the url of the default entrypoint can be updated."));
            }
        }

        try {
            // Try to instantiate uri to check if it's a valid endpoint url.
            URL url = new URL(entrypoint.getUrl());
            if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) {
                throw new MalformedURLException();
            }

            return organizationService.findById_migrated(entrypoint.getOrganizationId()).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Organization, CompletableSource>)organization -> {
                        String hostWithoutPort = url.getHost().split(":")[0];
                        if (!VirtualHostValidator.isValidDomainOrSubDomain(hostWithoutPort, organization.getDomainRestrictions())) {
                            return RxJava2Adapter.monoToCompletable(Mono.error(new InvalidEntrypointException("Host [" + hostWithoutPort + "] must be a subdomain of " + organization.getDomainRestrictions())));
                        }

                        return RxJava2Adapter.monoToCompletable(Mono.empty());
                    }).apply(y)))).then();
        } catch (MalformedURLException e) {
            return Mono.error(new InvalidEntrypointException("Entrypoint must have a valid url."));
        }
    }
}
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
import io.gravitee.am.model.Organization;
import io.gravitee.am.repository.management.api.OrganizationRepository;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EntrypointService;
import io.gravitee.am.service.OrganizationService;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.exception.OrganizationNotFoundException;
import io.gravitee.am.service.model.NewOrganization;
import io.gravitee.am.service.model.PatchOrganization;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.OrganizationAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import java.util.Collections;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationServiceImpl.class);

    private final OrganizationRepository organizationRepository;

    private final RoleService roleService;

    private final EntrypointService entrypointService;

    private final AuditService auditService;

    public OrganizationServiceImpl(@Lazy OrganizationRepository organizationRepository,
                                   RoleService roleService,
                                   EntrypointService entrypointService,
                                   AuditService auditService) {
        this.organizationRepository = organizationRepository;
        this.roleService = roleService;
        this.entrypointService = entrypointService;
        this.auditService = auditService;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Organization> findById(String id) {
 return RxJava2Adapter.monoToSingle(findById_migrated(id));
}
@Override
    public Mono<Organization> findById_migrated(String id) {
        LOGGER.debug("Find organization by id: {}", id);
        return organizationRepository.findById_migrated(id).switchIfEmpty(Mono.error(new OrganizationNotFoundException(id)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.createDefault_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Organization> createDefault() {
 return RxJava2Adapter.monoToMaybe(createDefault_migrated());
}
@Override
    public Mono<Organization> createDefault_migrated() {

        Organization organization = new Organization();
        organization.setId(Organization.DEFAULT);
        organization.setHrids(Collections.singletonList(Organization.DEFAULT.toLowerCase()));
        organization.setName("Default organization");
        organization.setDescription("Default organization");
        organization.setDomainRestrictions(Collections.emptyList());

        // No need to create default organization if one or more organizations already exist.
        return organizationRepository.count_migrated().filter(aLong -> aLong == 0).flatMap(z->createInternal_migrated(organization, null));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(organizationId, newOrganization, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Organization> createOrUpdate(String organizationId, NewOrganization newOrganization, User byUser) {
 return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(organizationId, newOrganization, byUser));
}
@Override
    public Mono<Organization> createOrUpdate_migrated(String organizationId, NewOrganization newOrganization, User byUser) {

        return organizationRepository.findById_migrated(organizationId).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<Organization, MaybeSource<Organization>>toJdkFunction(organization -> {
                    Organization toUpdate = new Organization(organization);
                    toUpdate.setName(newOrganization.getName());
                    toUpdate.setDescription(newOrganization.getDescription());
                    toUpdate.setDomainRestrictions(newOrganization.getDomainRestrictions());
                    toUpdate.setHrids(newOrganization.getHrids());

                    return RxJava2Adapter.monoToMaybe(updateInternal_migrated(toUpdate, byUser, organization));
                }).apply(v)))).switchIfEmpty(RxJava2Adapter.singleToMono(Single.defer(() -> {
                    Organization toCreate = new Organization();
                    toCreate.setId(organizationId);
                    toCreate.setHrids(newOrganization.getHrids());
                    toCreate.setName(newOrganization.getName());
                    toCreate.setDescription(newOrganization.getDescription());
                    toCreate.setDomainRestrictions(newOrganization.getDomainRestrictions());

                    return RxJava2Adapter.monoToSingle(createInternal_migrated(toCreate, byUser));
                })));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(organizationId, patchOrganization, updatedBy))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Organization> update(String organizationId, PatchOrganization patchOrganization, User updatedBy) {
 return RxJava2Adapter.monoToSingle(update_migrated(organizationId, patchOrganization, updatedBy));
}
@Override
    public Mono<Organization> update_migrated(String organizationId, PatchOrganization patchOrganization, User updatedBy) {

        return findById_migrated(organizationId).flatMap(organization->updateInternal_migrated(patchOrganization.patch(organization), updatedBy, organization));
    }

    
private Mono<Organization> createInternal_migrated(Organization toCreate, User owner) {

        Date now = new Date();

        toCreate.setCreatedAt(now);
        toCreate.setUpdatedAt(now);

        // Creates an organization and set ownership.
        return organizationRepository.create_migrated(toCreate).flatMap(createdOrganization->RxJava2Adapter.completableToMono(Completable.mergeArrayDelayError(RxJava2Adapter.monoToCompletable(entrypointService.createDefaults_migrated(createdOrganization).ignoreElements().then()), RxJava2Adapter.monoToCompletable(roleService.createDefaultRoles_migrated(createdOrganization.getId())))).then(Mono.just(createdOrganization))).doOnSuccess(organization -> auditService.report(AuditBuilder.builder(OrganizationAuditBuilder.class).type(EventType.ORGANIZATION_CREATED).organization(organization).principal(owner))).doOnError(throwable -> auditService.report(AuditBuilder.builder(OrganizationAuditBuilder.class).type(EventType.ORGANIZATION_CREATED).organization(toCreate).principal(owner).throwable(throwable)));
    }

    
private Mono<Organization> updateInternal_migrated(Organization organization, User updatedBy, Organization previous) {

        organization.setUpdatedAt(new Date());

        return organizationRepository.update_migrated(organization).doOnSuccess(updated -> auditService.report(AuditBuilder.builder(OrganizationAuditBuilder.class).type(EventType.ORGANIZATION_UPDATED).organization(updated).principal(updatedBy).oldValue(previous))).doOnError(throwable -> auditService.report(AuditBuilder.builder(OrganizationAuditBuilder.class).type(EventType.ORGANIZATION_UPDATED).organization(previous).principal(updatedBy).throwable(throwable)));
    }
}
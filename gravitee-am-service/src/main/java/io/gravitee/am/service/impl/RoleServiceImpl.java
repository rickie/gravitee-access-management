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

import static io.gravitee.am.model.Acl.*;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.event.Action;
import io.gravitee.am.common.event.Type;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Platform;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.model.permissions.SystemRole;
import io.gravitee.am.repository.management.api.RoleRepository;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewRole;
import io.gravitee.am.service.model.UpdateRole;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.RoleAuditBuilder;
import io.reactivex.*;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.*;
import java.util.HashSet;
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
public class RoleServiceImpl implements RoleService {

    private final Logger LOGGER = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Lazy
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private EventService eventService;

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllAssignable_migrated(referenceType, referenceId, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Role> findAllAssignable(ReferenceType referenceType, String referenceId, ReferenceType assignableType) {
 return RxJava2Adapter.fluxToFlowable(findAllAssignable_migrated(referenceType, referenceId, assignableType));
}
@Override
    public Flux<Role> findAllAssignable_migrated(ReferenceType referenceType, String referenceId, ReferenceType assignableType) {
        LOGGER.debug("Find roles by {}: {} assignable to {}", referenceType, referenceId, assignableType);

        // Organization roles must be zipped with system roles to get a complete list of all roles.
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.merge(RxJava2Adapter.fluxToFlowable(findAllSystem_migrated(assignableType)), RxJava2Adapter.fluxToFlowable(roleRepository.findAll_migrated(referenceType, referenceId))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(role -> assignableType == null || assignableType == role.getAssignableType())).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find roles by {}: {} assignable to {}", referenceType, referenceId, assignableType, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error occurs while trying to find roles by %s %s assignable to %s", referenceType, referenceId, assignableType), ex)));
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Set<Role>> findByDomain(String domain) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain));
}
@Override
    public Mono<Set<Role>> findByDomain_migrated(String domain) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(roleRepository.findAll_migrated(ReferenceType.DOMAIN, domain))
                .collect(HashSet::new, Set::add));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Role>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<Role>> findByDomain_migrated(String domain, int page, int size) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.findAll_migrated(ReferenceType.DOMAIN, domain, page, size)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.searchByDomain_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Role>> searchByDomain(String domain, String query, int page, int size) {
 return RxJava2Adapter.monoToSingle(searchByDomain_migrated(domain, query, page, size));
}
@Override
    public Mono<Page<Role>> searchByDomain_migrated(String domain, String query, int page, int size) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.search_migrated(ReferenceType.DOMAIN, domain, query, page, size)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Role> findById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<Role> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
        LOGGER.debug("Find role by ID: {}", id);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(roleRepository.findById_migrated(referenceType, referenceId, id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a role using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a role using its ID: %s", id), ex)));
                })).switchIfEmpty(Mono.error(new RoleNotFoundException(id)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Role> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Role> findById_migrated(String id) {
        LOGGER.debug("Find role by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(roleRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a role using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a role using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findSystemRole_migrated(systemRole, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Role> findSystemRole(SystemRole systemRole, ReferenceType assignableType) {
 return RxJava2Adapter.monoToMaybe(findSystemRole_migrated(systemRole, assignableType));
}
@Override
    public Mono<Role> findSystemRole_migrated(SystemRole systemRole, ReferenceType assignableType) {
        LOGGER.debug("Find system role : {} for the type : {}", systemRole.name(), assignableType);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(roleRepository.findByNameAndAssignableType_migrated(ReferenceType.PLATFORM, Platform.DEFAULT, systemRole.name(), assignableType))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(Role::isSystem)))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find system role : {} for type : {}", systemRole.name(), assignableType, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find system role : %s for type : %s", systemRole.name(), assignableType), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findRolesByName_migrated(referenceType, referenceId, assignableType, roleNames))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Role> findRolesByName(ReferenceType referenceType, String referenceId, ReferenceType assignableType, List<String> roleNames) {
 return RxJava2Adapter.fluxToFlowable(findRolesByName_migrated(referenceType, referenceId, assignableType, roleNames));
}
@Override
    public Flux<Role> findRolesByName_migrated(ReferenceType referenceType, String referenceId, ReferenceType assignableType, List<String> roleNames) {
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(roleRepository.findByNamesAndAssignableType_migrated(referenceType, referenceId, roleNames, assignableType))).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    String joinedRoles = roleNames.stream().collect(Collectors.joining(", "));
                    LOGGER.error("An error occurs while trying to find roles : {}", joinedRoles, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find roles : %s", joinedRoles), ex)));
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findDefaultRole_migrated(organizationId, defaultRole, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Role> findDefaultRole(String organizationId, DefaultRole defaultRole, ReferenceType assignableType) {
 return RxJava2Adapter.monoToMaybe(findDefaultRole_migrated(organizationId, defaultRole, assignableType));
}
@Override
    public Mono<Role> findDefaultRole_migrated(String organizationId, DefaultRole defaultRole, ReferenceType assignableType) {
        LOGGER.debug("Find default role {} of organization {} for the type {}", defaultRole.name(), organizationId, assignableType);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(roleRepository.findByNameAndAssignableType_migrated(ReferenceType.ORGANIZATION, organizationId, defaultRole.name(), assignableType))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(Role::isDefaultRole)))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find default role {} of organization {} for the type {}", defaultRole.name(), organizationId, assignableType, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find default role %s of organization %s for type %s", defaultRole.name(), organizationId, assignableType), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Set<Role>> findByIdIn(List<String> ids) {
 return RxJava2Adapter.monoToSingle(findByIdIn_migrated(ids));
}
@Override
    public Mono<Set<Role>> findByIdIn_migrated(List<String> ids) {
        LOGGER.debug("Find roles by ids: {}", ids);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(roleRepository.findByIdIn_migrated(ids)).collect(() -> (Set<Role>)new HashSet<Role>(), Set::add)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find roles by ids", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find roles by ids", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newRole, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Role> create(ReferenceType referenceType, String referenceId, NewRole newRole, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newRole, principal));
}
@Override
    public Mono<Role> create_migrated(ReferenceType referenceType, String referenceId, NewRole newRole, User principal) {
        LOGGER.debug("Create a new role {} for {} {}", newRole, referenceType, referenceId);

        String roleId = RandomString.generate();

        // check if role name is unique
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(checkRoleUniqueness_migrated(newRole.getName(), roleId, referenceType, referenceId))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Set<Role>, SingleSource<Role>>toJdkFunction(__ -> {
                    Role role = new Role();
                    role.setId(roleId);
                    role.setReferenceType(referenceType);
                    role.setReferenceId(referenceId);
                    role.setName(newRole.getName());
                    role.setDescription(newRole.getDescription());
                    role.setAssignableType(newRole.getAssignableType());
                    role.setPermissionAcls(new HashMap<>());
                    role.setOauthScopes(new ArrayList<>());
                    role.setCreatedAt(new Date());
                    role.setUpdatedAt(role.getCreatedAt());
                    return RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role));
                }).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Role, SingleSource<Role>>toJdkFunction(role -> {
                    Event event = new Event(Type.ROLE, new Payload(role.getId(), role.getReferenceType(), role.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event))).flatMap(__->Mono.just(role)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a role", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a role", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(role -> auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).principal(principal).type(EventType.ROLE_CREATED).role(role)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).principal(principal).type(EventType.ROLE_CREATED).throwable(throwable))))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newRole, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Role> create(String domain, NewRole newRole, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newRole, principal));
}
@Override
    public Mono<Role> create_migrated(String domain, NewRole newRole, User principal) {

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(create_migrated(ReferenceType.DOMAIN, domain, newRole, principal)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateRole, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Role> update(ReferenceType referenceType, String referenceId, String id, UpdateRole updateRole, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateRole, principal));
}
@Override
    public Mono<Role> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateRole updateRole, User principal) {
        LOGGER.debug("Update a role {} for {} {}", id, referenceType, referenceId);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Role, SingleSource<Role>>toJdkFunction(role -> {
                    if (role.isSystem()) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new SystemRoleUpdateException(role.getName())));
                    }

                    if(role.isDefaultRole() && !role.getName().equals(updateRole.getName())) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new DefaultRoleUpdateException(role.getName())));
                    }

                    return RxJava2Adapter.monoToSingle(Mono.just(role));
                }).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono((Single<Role>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Role, Single<Role>>)oldRole -> {
                    // check if role name is unique
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(checkRoleUniqueness_migrated(updateRole.getName(), oldRole.getId(), referenceType, referenceId))).flatMap(t->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Set<Role>, SingleSource<Role>>toJdkFunction(irrelevant -> {
                                Role roleToUpdate = new Role(oldRole);
                                roleToUpdate.setName(updateRole.getName());
                                roleToUpdate.setDescription(updateRole.getDescription());
                                roleToUpdate.setPermissionAcls(Permission.unflatten(updateRole.getPermissions()));
                                roleToUpdate.setOauthScopes(updateRole.getOauthScopes());
                                roleToUpdate.setUpdatedAt(new Date());
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.update_migrated(roleToUpdate))).flatMap(x->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Role, SingleSource<Role>>toJdkFunction(role -> {
                                            Event event = new Event(Type.ROLE, new Payload(role.getId(), role.getReferenceType(), role.getReferenceId(), Action.UPDATE));
                                            return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event))).flatMap(__->Mono.just(role)));
                                        }).apply(x)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(role -> auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).principal(principal).type(EventType.ROLE_UPDATED).oldValue(oldRole).role(role)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).principal(principal).type(EventType.ROLE_UPDATED).throwable(throwable)))));
                            }).apply(t)))));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a role", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a role", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateRole, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Role> update(String domain, String id, UpdateRole updateRole, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateRole, principal));
}
@Override
    public Mono<Role> update_migrated(String domain, String id, UpdateRole updateRole, User principal) {

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(update_migrated(ReferenceType.DOMAIN, domain, id, updateRole, principal)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, roleId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(ReferenceType referenceType, String referenceId, String roleId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, roleId, principal));
}
@Override
    public Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String roleId, User principal) {
        LOGGER.debug("Delete role {}", roleId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(roleRepository.findById_migrated(referenceType, referenceId, roleId))).switchIfEmpty(Mono.error(new RoleNotFoundException(roleId))).map(RxJavaReactorMigrationUtil.toJdkFunction(role -> {
                    if (role.isSystem()) {
                        throw new SystemRoleDeleteException(roleId);
                    }
                    return role;
                })).flatMap(role->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(roleRepository.delete_migrated(roleId))).then(RxJava2Adapter.completableToMono(Completable.fromSingle(RxJava2Adapter.monoToSingle(eventService.create_migrated(new Event(Type.ROLE, new Payload(role.getId(), role.getReferenceType(), role.getReferenceId(), Action.DELETE)))))))).doOnComplete(()->auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).principal(principal).type(EventType.ROLE_DELETED).role(role)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).principal(principal).type(EventType.ROLE_DELETED).throwable(throwable))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete role: {}", roleId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete role: %s", roleId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.createOrUpdateSystemRoles_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable createOrUpdateSystemRoles() {
 return RxJava2Adapter.monoToCompletable(createOrUpdateSystemRoles_migrated());
}
@Override
    public Mono<Void> createOrUpdateSystemRoles_migrated() {

        List<Role> roles = buildSystemRoles();

        return RxJava2Adapter.completableToMono(Observable.fromIterable(roles)
                .flatMapCompletable((io.gravitee.am.model.Role ident) -> RxJava2Adapter.monoToCompletable(upsert_migrated(ident))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.createDefaultRoles_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable createDefaultRoles(String organizationId) {
 return RxJava2Adapter.monoToCompletable(createDefaultRoles_migrated(organizationId));
}
@Override
    public Mono<Void> createDefaultRoles_migrated(String organizationId) {

        List<Role> roles = buildDefaultRoles(organizationId);

        return RxJava2Adapter.completableToMono(Observable.fromIterable(roles)
                .flatMapCompletable((io.gravitee.am.model.Role ident) -> RxJava2Adapter.monoToCompletable(upsert_migrated(ident))));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.upsert_migrated(role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable upsert(Role role) {
 return RxJava2Adapter.monoToCompletable(upsert_migrated(role));
}
private Mono<Void> upsert_migrated(Role role) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(roleRepository.findByNameAndAssignableType_migrated(role.getReferenceType(), role.getReferenceId(), role.getName(), role.getAssignableType()))).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::ofNullable)).defaultIfEmpty(Optional.empty()).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Optional<Role>, CompletableSource>)optRole -> {
                    if (!optRole.isPresent()) {
                        LOGGER.debug("Create a system role {}", role.getAssignableType() + ":" + role.getName());
                        role.setCreatedAt(new Date());
                        role.setUpdatedAt(role.getCreatedAt());
                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Role, SingleSource<Role>>toJdkFunction(role1 -> {
                                    Event event = new Event(Type.ROLE, new Payload(role1.getId(), role1.getReferenceType(), role1.getReferenceId(), Action.CREATE));
                                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event))).flatMap(__->Mono.just(role1)));
                                }).apply(v)))))
                                .onErrorResumeNext(ex -> {
                                    if (ex instanceof AbstractManagementException) {
                                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                                    }
                                    LOGGER.error("An error occurs while trying to create a system role {}", role.getAssignableType() + ":" + role.getName(), ex);
                                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a role", ex)));
                                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(role1 -> auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).type(EventType.ROLE_CREATED).role(role1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).type(EventType.ROLE_CREATED).throwable(throwable)))))
                                .toCompletable();
                    } else {
                        // check if permission set has changed
                        Role currentRole = optRole.get();
                        if (permissionsAreEquals(currentRole, role)) {
                            return RxJava2Adapter.monoToCompletable(Mono.empty());
                        }
                        LOGGER.debug("Update a system role {}", role.getAssignableType() + ":" + role.getName());
                        // update the role
                        role.setId(currentRole.getId());
                        role.setPermissionAcls(role.getPermissionAcls());
                        role.setUpdatedAt(new Date());
                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.update_migrated(role))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Role, SingleSource<Role>>toJdkFunction(role1 -> {
                                    Event event = new Event(Type.ROLE, new Payload(role1.getId(), role1.getReferenceType(), role1.getReferenceId(), Action.UPDATE));
                                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event))).flatMap(__->Mono.just(role1)));
                                }).apply(v)))))
                                .onErrorResumeNext(ex -> {
                                    if (ex instanceof AbstractManagementException) {
                                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                                    }
                                    LOGGER.error("An error occurs while trying to update a system role {}", role.getAssignableType() + ":" + role.getName(), ex);
                                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a role", ex)));
                                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(role1 -> auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).type(EventType.ROLE_UPDATED).oldValue(currentRole).role(role1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(RoleAuditBuilder.class).type(EventType.ROLE_UPDATED).throwable(throwable)))))
                                .toCompletable();
                    }
                }).apply(y)))).then()));

    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.checkRoleUniqueness_migrated(roleName, roleId, referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Set<Role>> checkRoleUniqueness(String roleName, String roleId, ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.monoToSingle(checkRoleUniqueness_migrated(roleName, roleId, referenceType, referenceId));
}
private Mono<Set<Role>> checkRoleUniqueness_migrated(String roleName, String roleId, ReferenceType referenceType, String referenceId) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(roleRepository.findAll_migrated(referenceType, referenceId))
                .collect(HashSet<Role>::new, Set::add)).flatMap(v->RxJava2Adapter.singleToMono((Single<Set<Role>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<HashSet<Role>, Single<Set<Role>>>)roles -> {
                    if (roles.stream()
                            .filter(role -> !role.getId().equals(roleId))
                            .anyMatch(role -> role.getName().equals(roleName))) {
                        throw new RoleAlreadyExistsException(roleName);
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(roles));
                }).apply(v)))));
    }

    private boolean permissionsAreEquals(Role role1, Role role2) {

        return Objects.equals(role1.getPermissionAcls(), role2.getPermissionAcls())
                && Objects.equals(role1.getOauthScopes(), role2.getOauthScopes());
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllSystem_migrated(assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Flowable<Role> findAllSystem(ReferenceType assignableType) {
 return RxJava2Adapter.fluxToFlowable(findAllSystem_migrated(assignableType));
}
private Flux<Role> findAllSystem_migrated(ReferenceType assignableType) {
        LOGGER.debug("Find all global system roles");

        // Exclude roles internal only and non assignable roles.
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(roleRepository.findAll_migrated(ReferenceType.PLATFORM, Platform.DEFAULT))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(role -> role.isSystem() && !role.isInternalOnly())).filter(RxJavaReactorMigrationUtil.toJdkPredicate(role -> assignableType == null || role.getAssignableType() == assignableType))));
    }

    private static List<Role> buildSystemRoles() {

        List<Role> roles = new ArrayList<>();

        // Create PRIMARY_OWNER roles and PLATFORM_ADMIN role.
        Map<Permission, Set<Acl>> platformAdminPermissions = Permission.allPermissionAcls(ReferenceType.PLATFORM);
        Map<Permission, Set<Acl>> organizationPrimaryOwnerPermissions = Permission.allPermissionAcls(ReferenceType.ORGANIZATION);
        Map<Permission, Set<Acl>> environmentPrimaryOwnerPermissions = Permission.allPermissionAcls(ReferenceType.ENVIRONMENT);
        Map<Permission, Set<Acl>> domainPrimaryOwnerPermissions = Permission.allPermissionAcls(ReferenceType.DOMAIN);
        Map<Permission, Set<Acl>> applicationPrimaryOwnerPermissions = Permission.allPermissionAcls(ReferenceType.APPLICATION);

        organizationPrimaryOwnerPermissions.put(Permission.ORGANIZATION, Acl.of(READ));
        organizationPrimaryOwnerPermissions.put(Permission.ORGANIZATION_SETTINGS, Acl.of(READ, UPDATE));
        organizationPrimaryOwnerPermissions.put(Permission.ORGANIZATION_AUDIT, Acl.of(READ, LIST));
        organizationPrimaryOwnerPermissions.put(Permission.ENVIRONMENT, Acl.of(READ, LIST));

        environmentPrimaryOwnerPermissions.put(Permission.ENVIRONMENT, Acl.of(READ));

        domainPrimaryOwnerPermissions.put(Permission.DOMAIN, Acl.of(READ, UPDATE, DELETE));
        domainPrimaryOwnerPermissions.put(Permission.DOMAIN_SETTINGS, Acl.of(READ, UPDATE));
        domainPrimaryOwnerPermissions.put(Permission.DOMAIN_AUDIT, Acl.of(READ, LIST));

        applicationPrimaryOwnerPermissions.put(Permission.APPLICATION, Acl.of(READ, UPDATE, DELETE));

        roles.add(buildSystemRole(SystemRole.PLATFORM_ADMIN.name(), ReferenceType.PLATFORM, platformAdminPermissions));
        roles.add(buildSystemRole(SystemRole.ORGANIZATION_PRIMARY_OWNER.name(), ReferenceType.ORGANIZATION, organizationPrimaryOwnerPermissions));
        roles.add(buildSystemRole(SystemRole.ENVIRONMENT_PRIMARY_OWNER.name(), ReferenceType.ENVIRONMENT, environmentPrimaryOwnerPermissions));
        roles.add(buildSystemRole(SystemRole.DOMAIN_PRIMARY_OWNER.name(), ReferenceType.DOMAIN, domainPrimaryOwnerPermissions));
        roles.add(buildSystemRole(SystemRole.APPLICATION_PRIMARY_OWNER.name(), ReferenceType.APPLICATION, applicationPrimaryOwnerPermissions));

        return roles;
    }

    private List<Role> buildDefaultRoles(String organizationId) {

        List<Role> roles = new ArrayList<>();

        // Create OWNER and USER roles.
        Map<Permission, Set<Acl>> organizationOwnerPermissions = Permission.allPermissionAcls(ReferenceType.ORGANIZATION);
        Map<Permission, Set<Acl>> environmentOwnerPermissions = Permission.allPermissionAcls(ReferenceType.ENVIRONMENT);
        Map<Permission, Set<Acl>> domainOwnerPermissions = Permission.allPermissionAcls(ReferenceType.DOMAIN);
        Map<Permission, Set<Acl>> applicationOwnerPermissions = Permission.allPermissionAcls(ReferenceType.APPLICATION);

        organizationOwnerPermissions.put(Permission.ORGANIZATION, Acl.of(READ));
        organizationOwnerPermissions.put(Permission.ORGANIZATION_SETTINGS, Acl.of(READ, UPDATE));
        organizationOwnerPermissions.put(Permission.ORGANIZATION_AUDIT, Acl.of(READ, LIST));
        organizationOwnerPermissions.put(Permission.ENVIRONMENT, Acl.of(READ, LIST));

        environmentOwnerPermissions.put(Permission.ENVIRONMENT, Acl.of(READ));

        domainOwnerPermissions.put(Permission.DOMAIN, Acl.of(READ, UPDATE));
        domainOwnerPermissions.put(Permission.DOMAIN_SETTINGS, Acl.of(READ, UPDATE));
        domainOwnerPermissions.put(Permission.DOMAIN_AUDIT, Acl.of(READ, LIST));

        applicationOwnerPermissions.put(Permission.APPLICATION, Acl.of(READ, UPDATE));

        roles.add(buildDefaultRole(DefaultRole.ORGANIZATION_OWNER.name(), ReferenceType.ORGANIZATION, organizationId, organizationOwnerPermissions));
        roles.add(buildDefaultRole(DefaultRole.ENVIRONMENT_OWNER.name(), ReferenceType.ENVIRONMENT, organizationId, environmentOwnerPermissions));
        roles.add(buildDefaultRole(DefaultRole.DOMAIN_OWNER.name(), ReferenceType.DOMAIN, organizationId, domainOwnerPermissions));
        roles.add(buildDefaultRole(DefaultRole.APPLICATION_OWNER.name(), ReferenceType.APPLICATION, organizationId, applicationOwnerPermissions));

        // Create USER roles.
        Map<Permission, Set<Acl>> organizationUserPermissions = new HashMap<>();
        Map<Permission, Set<Acl>> environmentUserPermissions = new HashMap<>();
        Map<Permission, Set<Acl>> domainUserPermissions = new HashMap<>();
        Map<Permission, Set<Acl>> applicationUserPermissions = new HashMap<>();

        organizationUserPermissions.put(Permission.ORGANIZATION, Acl.of(READ));
        organizationUserPermissions.put(Permission.ORGANIZATION_GROUP, Acl.of(LIST));
        organizationUserPermissions.put(Permission.ORGANIZATION_ROLE, Acl.of(LIST));
        organizationUserPermissions.put(Permission.ORGANIZATION_TAG, Acl.of(LIST));
        organizationUserPermissions.put(Permission.ENVIRONMENT, Acl.of(LIST));

        environmentUserPermissions.put(Permission.ENVIRONMENT, Acl.of(READ));
        environmentUserPermissions.put(Permission.DOMAIN, Acl.of(LIST));

        domainUserPermissions.put(Permission.DOMAIN, Acl.of(READ));
        domainUserPermissions.put(Permission.DOMAIN_SCOPE, Acl.of(LIST));
        domainUserPermissions.put(Permission.DOMAIN_EXTENSION_GRANT, Acl.of(LIST));
        domainUserPermissions.put(Permission.DOMAIN_CERTIFICATE, Acl.of(LIST));
        domainUserPermissions.put(Permission.DOMAIN_IDENTITY_PROVIDER, Acl.of(LIST));
        domainUserPermissions.put(Permission.DOMAIN_FACTOR, Acl.of(LIST));
        domainUserPermissions.put(Permission.DOMAIN_RESOURCE, Acl.of(LIST));
        domainUserPermissions.put(Permission.APPLICATION, Acl.of(LIST));
        domainUserPermissions.put(Permission.DOMAIN_BOT_DETECTION, Acl.of(LIST));

        applicationUserPermissions.put(Permission.APPLICATION, Acl.of(READ));

        roles.add(buildDefaultRole(DefaultRole.ORGANIZATION_USER.name(), ReferenceType.ORGANIZATION, organizationId, organizationUserPermissions));
        roles.add(buildDefaultRole(DefaultRole.ENVIRONMENT_USER.name(), ReferenceType.ENVIRONMENT, organizationId, environmentUserPermissions));
        roles.add(buildDefaultRole(DefaultRole.DOMAIN_USER.name(), ReferenceType.DOMAIN, organizationId, domainUserPermissions));
        roles.add(buildDefaultRole(DefaultRole.APPLICATION_USER.name(), ReferenceType.APPLICATION, organizationId, applicationUserPermissions));

        return roles;
    }

    private static Role buildSystemRole(String name, ReferenceType assignableType, Map<Permission, Set<Acl>> permissions) {

        Role systemRole = buildRole(name, assignableType, ReferenceType.PLATFORM, Platform.DEFAULT, permissions);
        systemRole.setSystem(true);

        return systemRole;
    }

    private static Role buildDefaultRole(String name, ReferenceType assignableType, String organizationId, Map<Permission, Set<Acl>> permissions) {

        Role defaultRole = buildRole(name, assignableType, ReferenceType.ORGANIZATION, organizationId, permissions);
        defaultRole.setDefaultRole(true);

        return defaultRole;
    }

    private static Role buildRole(String name, ReferenceType assignableType, ReferenceType referenceType, String referenceId, Map<Permission, Set<Acl>> permissions) {

        Role role = new Role();
        role.setId(RandomString.generate());
        role.setName(name);
        role.setAssignableType(assignableType);
        role.setReferenceType(referenceType);
        role.setReferenceId(referenceId);
        role.setPermissionAcls(permissions);

        return role;
    }
}

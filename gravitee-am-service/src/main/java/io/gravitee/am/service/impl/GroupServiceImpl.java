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
import io.gravitee.am.model.Group;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.GroupRepository;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewGroup;
import io.gravitee.am.service.model.UpdateGroup;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.GroupAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
public class GroupServiceImpl implements GroupService {

    private final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Lazy
    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrganizationUserService organizationUserService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private EventService eventService;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(referenceType, referenceId, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Group>> findAll(ReferenceType referenceType, String referenceId, int page, int size) {
 return RxJava2Adapter.monoToSingle(findAll_migrated(referenceType, referenceId, page, size));
}
@Override
    public Mono<Page<Group>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size) {
        LOGGER.debug("Find groups by {}: {}", referenceType, referenceId);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(groupRepository.findAll_migrated(referenceType, referenceId, page, size))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find groups by {} {}", referenceType, referenceId, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find users by %s %s", referenceType, referenceId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Group>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<Group>> findByDomain_migrated(String domain, int page, int size) {
        return findAll_migrated(ReferenceType.DOMAIN, domain, page, size);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Group> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<Group> findAll_migrated(ReferenceType referenceType, String referenceId) {
        LOGGER.debug("Find groups by {}: {}", referenceType, referenceId);
        return groupRepository.findAll_migrated(referenceType, referenceId).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find groups by {} {}", referenceType, referenceId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error occurs while trying to find users by %s %s", referenceType, referenceId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Group> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Group> findByDomain_migrated(String domain) {
        return findAll_migrated(ReferenceType.DOMAIN, domain);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByName_migrated(referenceType, referenceId, groupName))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Group> findByName(ReferenceType referenceType, String referenceId, String groupName) {
 return RxJava2Adapter.monoToMaybe(findByName_migrated(referenceType, referenceId, groupName));
}
@Override
    public Mono<Group> findByName_migrated(ReferenceType referenceType, String referenceId, String groupName) {
        LOGGER.debug("Find group by {} and name: {} {}", referenceType, referenceId, groupName);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(groupRepository.findByName_migrated(referenceType, referenceId, groupName))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a group using its name: {} for the {} {}", groupName, referenceType, referenceId, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using its name: %s for the %s %s", groupName, referenceType, referenceId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByMember_migrated(memberId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Group> findByMember(String memberId) {
 return RxJava2Adapter.fluxToFlowable(findByMember_migrated(memberId));
}
@Override
    public Flux<Group> findByMember_migrated(String memberId) {
        LOGGER.debug("Find groups by member : {}", memberId);
        return groupRepository.findByMember_migrated(memberId).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a groups using member ", memberId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using member: %s", memberId), ex)));
                }));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> findById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<Group> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
        LOGGER.debug("Find group by id : {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(groupRepository.findById_migrated(referenceType, referenceId, id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a group using its id {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a group using its id: %s", id), ex)));
                })).switchIfEmpty(Mono.error(new GroupNotFoundException(id)));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Group> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Group> findById_migrated(String id) {
        LOGGER.debug("Find group by id : {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(groupRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a group using its ID {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a group using its ID: %s", id), ex)));
                }));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findMembers_migrated(referenceType, referenceId, groupId, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<User>> findMembers(ReferenceType referenceType, String referenceId, String groupId, int page, int size) {
 return RxJava2Adapter.monoToSingle(findMembers_migrated(referenceType, referenceId, groupId, page, size));
}
@Override
    public Mono<Page<User>> findMembers_migrated(ReferenceType referenceType, String referenceId, String groupId, int page, int size) {
        LOGGER.debug("Find members for group : {}", groupId);
        return findById_migrated(referenceType, referenceId, groupId).flatMap(v->RxJava2Adapter.singleToMono((Single<Page<User>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Group, Single<Page<User>>>)group -> {
                    if (group.getMembers() == null || group.getMembers().isEmpty()) {
                        return RxJava2Adapter.monoToSingle(Mono.just(new Page<>(null, page, size)));
                    } else {
                        // get members
                        List<String> sortedMembers = group.getMembers().stream().sorted().collect(Collectors.toList());
                        List<String> pagedMemberIds = sortedMembers.subList(Math.min(sortedMembers.size(), page), Math.min(sortedMembers.size(), page + size));
                        CommonUserService service = (group.getReferenceType() == ReferenceType.ORGANIZATION ? organizationUserService : userService);
                        return RxJava2Adapter.monoToSingle(service.findByIdIn_migrated(pagedMemberIds).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(users -> new Page<>(users, page, pagedMemberIds.size()))));
                    }
                }).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Group> findByIdIn(List<String> ids) {
 return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
@Override
    public Flux<Group> findByIdIn_migrated(List<String> ids) {
        LOGGER.debug("Find groups for ids : {}", ids);
        return groupRepository.findByIdIn_migrated(ids).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a group using ids {}", ids, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a group using ids: %s", ids), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newGroup, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> create(ReferenceType referenceType, String referenceId, NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newGroup, principal));
}
@Override
    public Mono<Group> create_migrated(ReferenceType referenceType, String referenceId, NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {
        LOGGER.debug("Create a new group {} for {} {}", newGroup.getName(), referenceType, referenceId);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(findByName_migrated(referenceType, referenceId, newGroup.getName()).hasElement().map(RxJavaReactorMigrationUtil.toJdkFunction(isEmpty -> {
                    if (!isEmpty) {
                        throw new GroupAlreadyExistsException(newGroup.getName());
                    } else {
                        String groupId = RandomString.generate();
                        Group group = new Group();
                        group.setId(groupId);
                        group.setReferenceType(referenceType);
                        group.setReferenceId(referenceId);
                        group.setName(newGroup.getName());
                        group.setDescription(newGroup.getDescription());
                        group.setMembers(newGroup.getMembers());
                        group.setCreatedAt(new Date());
                        group.setUpdatedAt(group.getCreatedAt());
                        return group;
                    }
                })).flatMap(v->setMembers_migrated(v)).flatMap(v->groupRepository.create_migrated(v)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.Group, SingleSource<io.gravitee.am.model.Group>>toJdkFunction(group -> {
                    Event event = new Event(Type.GROUP, new Payload(group.getId(), group.getReferenceType(), group.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(group)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    } else {
                        LOGGER.error("An error occurs while trying to create a group", ex);
                        return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a group", ex)));
                    }
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(group -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_CREATED).group(group)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_CREATED).throwable(throwable))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newGroup, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> create(String domain, NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newGroup, principal));
}
@Override
    public Mono<Group> create_migrated(String domain, NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {

        return create_migrated(ReferenceType.DOMAIN, domain, newGroup, principal);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateGroup, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> update(ReferenceType referenceType, String referenceId, String id, UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateGroup, principal));
}
@Override
    public Mono<Group> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {
        LOGGER.debug("Update a group {} for {} {}", id, referenceType, referenceId);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id).flatMap(e->groupRepository.findByName_migrated(referenceType, referenceId, updateGroup.getName()).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()).map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.Optional<io.gravitee.am.model.Group> optionalGroup)->{
if (optionalGroup.isPresent() && !optionalGroup.get().getId().equals(id)) {
throw new GroupAlreadyExistsException(updateGroup.getName());
}
return e;
}))))
                .flatMapSingle(oldGroup -> {
                    Group groupToUpdate = new Group(oldGroup);
                    groupToUpdate.setName(updateGroup.getName());
                    groupToUpdate.setDescription(updateGroup.getDescription());
                    groupToUpdate.setMembers(updateGroup.getMembers());
                    groupToUpdate.setUpdatedAt(new Date());

                    // set members and update
                    return RxJava2Adapter.monoToSingle(setMembers_migrated(groupToUpdate).flatMap(v->groupRepository.update_migrated(v)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.Group, SingleSource<io.gravitee.am.model.Group>>toJdkFunction(group -> {
                                Event event = new Event(Type.GROUP, new Payload(group.getId(), group.getReferenceType(), group.getReferenceId(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(group)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(group -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_UPDATED).oldValue(oldGroup).group(group)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_UPDATED).throwable(throwable)))));

                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a group", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a group", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateGroup, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> update(String domain, String id, UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateGroup, principal));
}
@Override
    public Mono<Group> update_migrated(String domain, String id, UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {

        return update_migrated(ReferenceType.DOMAIN, domain, id, updateGroup, principal);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, groupId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(ReferenceType referenceType, String referenceId, String groupId, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, groupId, principal));
}
@Override
    public Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String groupId, io.gravitee.am.identityprovider.api.User principal) {
        LOGGER.debug("Delete group {}", groupId);

        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(findById_migrated(referenceType, referenceId, groupId).flatMap(group->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(groupRepository.delete_migrated(groupId).then(RxJava2Adapter.completableToMono(Completable.fromSingle(RxJava2Adapter.monoToSingle(eventService.create_migrated(new Event(Type.DOMAIN, new Payload(group.getId(), group.getReferenceType(), group.getReferenceId(), Action.DELETE)))))))).doOnComplete(()->auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_DELETED).group(group)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_DELETED).throwable(throwable))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to delete group: {}", groupId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete group: %s", groupId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.assignRoles_migrated(referenceType, referenceId, groupId, roles, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> assignRoles(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(assignRoles_migrated(referenceType, referenceId, groupId, roles, principal));
}
@Override
    public Mono<Group> assignRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
        return assignRoles0_migrated(referenceType, referenceId, groupId, roles, principal, false);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.revokeRoles_migrated(referenceType, referenceId, groupId, roles, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> revokeRoles(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, groupId, roles, principal));
}
@Override
    public Mono<Group> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
        return assignRoles0_migrated(referenceType, referenceId, groupId, roles, principal, true);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.assignRoles0_migrated(referenceType, referenceId, groupId, roles, principal, revoke))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Group> assignRoles0(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal, boolean revoke) {
 return RxJava2Adapter.monoToSingle(assignRoles0_migrated(referenceType, referenceId, groupId, roles, principal, revoke));
}
private Mono<Group> assignRoles0_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal, boolean revoke) {
        return findById_migrated(referenceType, referenceId, groupId).flatMap(v->RxJava2Adapter.singleToMono((Single<Group>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Group, Single<Group>>)oldGroup -> {
                    Group groupToUpdate = new Group(oldGroup);
                    // remove existing roles from the group
                    if (revoke) {
                        if (groupToUpdate.getRoles() != null) {
                            groupToUpdate.getRoles().removeAll(roles);
                        }
                    } else {
                        groupToUpdate.setRoles(roles);
                    }
                    // check roles
                    return RxJava2Adapter.monoToSingle(checkRoles_migrated(roles).then(Mono.defer(()->groupRepository.update_migrated(groupToUpdate))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(group1 -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_ROLES_ASSIGNED).oldValue(oldGroup).group(group1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_ROLES_ASSIGNED).throwable(throwable)))));
                }).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.setMembers_migrated(group))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Group> setMembers(Group group) {
 return RxJava2Adapter.monoToSingle(setMembers_migrated(group));
}
private Mono<Group> setMembers_migrated(Group group) {
        List<String> userMembers = group.getMembers() != null ? group.getMembers().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList()) : null;
        if (userMembers != null && !userMembers.isEmpty()) {
            CommonUserService service = (group.getReferenceType() == ReferenceType.ORGANIZATION ? organizationUserService : userService);
            return service.findByIdIn_migrated(userMembers).map(RxJavaReactorMigrationUtil.toJdkFunction(User::getId)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(userIds -> {
                        group.setMembers(userIds);
                        return group;
                    }));
        }
        return Mono.just(group);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkRoles_migrated(roles))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable checkRoles(List<String> roles) {
 return RxJava2Adapter.monoToCompletable(checkRoles_migrated(roles));
}
private Mono<Void> checkRoles_migrated(List<String> roles) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(roleService.findByIdIn_migrated(roles).map(RxJavaReactorMigrationUtil.toJdkFunction(roles1 -> {
                    if (roles1.size() != roles.size()) {
                        // find difference between the two list
                        roles.removeAll(roles1.stream().map(Role::getId).collect(Collectors.toList()));
                        throw new RoleNotFoundException(String.join(",", roles));
                    }
                    return roles1;
                }))).toCompletable());
    }
}

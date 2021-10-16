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

    @Override
    public Single<Page<Group>> findAll(ReferenceType referenceType, String referenceId, int page, int size) {
        LOGGER.debug("Find groups by {}: {}", referenceType, referenceId);
        return groupRepository.findAll(referenceType, referenceId, page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find groups by {} {}", referenceType, referenceId, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find users by %s %s", referenceType, referenceId), ex)));
                });
    }

    @Override
    public Single<Page<Group>> findByDomain(String domain, int page, int size) {
        return findAll(ReferenceType.DOMAIN, domain, page, size);
    }

    @Override
    public Flowable<Group> findAll(ReferenceType referenceType, String referenceId) {
        LOGGER.debug("Find groups by {}: {}", referenceType, referenceId);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(groupRepository.findAll(referenceType, referenceId)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find groups by {} {}", referenceType, referenceId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error occurs while trying to find users by %s %s", referenceType, referenceId), ex)));
                })));
    }

    @Override
    public Flowable<Group> findByDomain(String domain) {
        return findAll(ReferenceType.DOMAIN, domain);
    }

    @Override
    public Maybe<Group> findByName(ReferenceType referenceType, String referenceId, String groupName) {
        LOGGER.debug("Find group by {} and name: {} {}", referenceType, referenceId, groupName);
        return groupRepository.findByName(referenceType, referenceId, groupName)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a group using its name: {} for the {} {}", groupName, referenceType, referenceId, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using its name: %s for the %s %s", groupName, referenceType, referenceId), ex)));
                });
    }

    @Override
    public Flowable<Group> findByMember(String memberId) {
        LOGGER.debug("Find groups by member : {}", memberId);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(groupRepository.findByMember(memberId)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a groups using member ", memberId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using member: %s", memberId), ex)));
                })));
    }


    @Override
    public Single<Group> findById(ReferenceType referenceType, String referenceId, String id) {
        LOGGER.debug("Find group by id : {}", id);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(groupRepository.findById(referenceType, referenceId, id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a group using its id {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a group using its id: %s", id), ex)));
                })).switchIfEmpty(Mono.error(new GroupNotFoundException(id))));
    }


    @Override
    public Maybe<Group> findById(String id) {
        LOGGER.debug("Find group by id : {}", id);
        return groupRepository.findById(id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a group using its ID {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a group using its ID: %s", id), ex)));
                });
    }


    @Override
    public Single<Page<User>> findMembers(ReferenceType referenceType, String referenceId, String groupId, int page, int size) {
        LOGGER.debug("Find members for group : {}", groupId);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(findById(referenceType, referenceId, groupId)).flatMap(v->RxJava2Adapter.singleToMono((Single<Page<User>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Group, Single<Page<User>>>)group -> {
                    if (group.getMembers() == null || group.getMembers().isEmpty()) {
                        return RxJava2Adapter.monoToSingle(Mono.just(new Page<>(null, page, size)));
                    } else {
                        // get members
                        List<String> sortedMembers = group.getMembers().stream().sorted().collect(Collectors.toList());
                        List<String> pagedMemberIds = sortedMembers.subList(Math.min(sortedMembers.size(), page), Math.min(sortedMembers.size(), page + size));
                        CommonUserService service = (group.getReferenceType() == ReferenceType.ORGANIZATION ? organizationUserService : userService);
                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(service.findByIdIn(pagedMemberIds)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(users -> new Page<>(users, page, pagedMemberIds.size()))));
                    }
                }).apply(v))));
    }

    @Override
    public Flowable<Group> findByIdIn(List<String> ids) {
        LOGGER.debug("Find groups for ids : {}", ids);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(groupRepository.findByIdIn(ids)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a group using ids {}", ids, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a group using ids: %s", ids), ex)));
                })));
    }

    @Override
    public Single<Group> create(ReferenceType referenceType, String referenceId, NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {
        LOGGER.debug("Create a new group {} for {} {}", newGroup.getName(), referenceType, referenceId);

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(findByName(referenceType, referenceId, newGroup.getName())).hasElement().map(RxJavaReactorMigrationUtil.toJdkFunction(isEmpty -> {
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
                })).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.Group, SingleSource<io.gravitee.am.model.Group>>toJdkFunction(this::setMembers).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.Group, SingleSource<io.gravitee.am.model.Group>>toJdkFunction(groupRepository::create).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.Group, SingleSource<io.gravitee.am.model.Group>>toJdkFunction(group -> {
                    Event event = new Event(Type.GROUP, new Payload(group.getId(), group.getReferenceType(), group.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(group)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    } else {
                        LOGGER.error("An error occurs while trying to create a group", ex);
                        return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a group", ex)));
                    }
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(group -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_CREATED).group(group)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_CREATED).throwable(throwable)))));
    }

    @Override
    public Single<Group> create(String domain, NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {

        return create(ReferenceType.DOMAIN, domain, newGroup, principal);
    }

    @Override
    public Single<Group> update(ReferenceType referenceType, String referenceId, String id, UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {
        LOGGER.debug("Update a group {} for {} {}", id, referenceType, referenceId);

        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(findById(referenceType, referenceId, id)).flatMap(e->RxJava2Adapter.maybeToMono(groupRepository.findByName(referenceType, referenceId, updateGroup.getName())).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()).map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.Optional<io.gravitee.am.model.Group> optionalGroup)->{
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
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(setMembers(groupToUpdate)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.Group, SingleSource<io.gravitee.am.model.Group>>toJdkFunction(groupRepository::update).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.Group, SingleSource<io.gravitee.am.model.Group>>toJdkFunction(group -> {
                                Event event = new Event(Type.GROUP, new Payload(group.getId(), group.getReferenceType(), group.getReferenceId(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(group)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(group -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_UPDATED).oldValue(oldGroup).group(group)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_UPDATED).throwable(throwable)))));

                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a group", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a group", ex)));
                });
    }

    @Override
    public Single<Group> update(String domain, String id, UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {

        return update(ReferenceType.DOMAIN, domain, id, updateGroup, principal);
    }

    @Override
    public Completable delete(ReferenceType referenceType, String referenceId, String groupId, io.gravitee.am.identityprovider.api.User principal) {
        LOGGER.debug("Delete group {}", groupId);

        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(findById(referenceType, referenceId, groupId)).flatMap(group->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(groupRepository.delete(groupId)).then(RxJava2Adapter.completableToMono(Completable.fromSingle(eventService.create(new Event(Type.DOMAIN, new Payload(group.getId(), group.getReferenceType(), group.getReferenceId(), Action.DELETE))))))).doOnComplete(()->auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_DELETED).group(group)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_DELETED).throwable(throwable))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to delete group: {}", groupId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete group: %s", groupId), ex)));
                });
    }

    @Override
    public Single<Group> assignRoles(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
        return assignRoles0(referenceType, referenceId, groupId, roles, principal, false);
    }

    @Override
    public Single<Group> revokeRoles(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
        return assignRoles0(referenceType, referenceId, groupId, roles, principal, true);
    }

    @Deprecated
private Single<Group> assignRoles0(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal, boolean revoke) {
 return RxJava2Adapter.monoToSingle(assignRoles0_migrated(referenceType, referenceId, groupId, roles, principal, revoke));
}
private Mono<Group> assignRoles0_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal, boolean revoke) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(findById(referenceType, referenceId, groupId)).flatMap(v->RxJava2Adapter.singleToMono((Single<Group>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Group, Single<Group>>)oldGroup -> {
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
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkRoles(roles)).then(Mono.defer(()->RxJava2Adapter.singleToMono(groupRepository.update(groupToUpdate)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(group1 -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_ROLES_ASSIGNED).oldValue(oldGroup).group(group1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(GroupAuditBuilder.class).principal(principal).type(EventType.GROUP_ROLES_ASSIGNED).throwable(throwable)))));
                }).apply(v)))));
    }

    @Deprecated
private Single<Group> setMembers(Group group) {
 return RxJava2Adapter.monoToSingle(setMembers_migrated(group));
}
private Mono<Group> setMembers_migrated(Group group) {
        List<String> userMembers = group.getMembers() != null ? group.getMembers().stream().filter(Objects::nonNull).distinct().collect(Collectors.toList()) : null;
        if (userMembers != null && !userMembers.isEmpty()) {
            CommonUserService service = (group.getReferenceType() == ReferenceType.ORGANIZATION ? organizationUserService : userService);
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(service.findByIdIn(userMembers)).map(RxJavaReactorMigrationUtil.toJdkFunction(User::getId)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(userIds -> {
                        group.setMembers(userIds);
                        return group;
                    }))));
        }
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(group)));
    }

    @Deprecated
private Completable checkRoles(List<String> roles) {
 return RxJava2Adapter.monoToCompletable(checkRoles_migrated(roles));
}
private Mono<Void> checkRoles_migrated(List<String> roles) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(roleService.findByIdIn(roles)).map(RxJavaReactorMigrationUtil.toJdkFunction(roles1 -> {
                    if (roles1.size() != roles.size()) {
                        // find difference between the two list
                        roles.removeAll(roles1.stream().map(Role::getId).collect(Collectors.toList()));
                        throw new RoleNotFoundException(String.join(",", roles));
                    }
                    return roles1;
                }))).toCompletable());
    }
}

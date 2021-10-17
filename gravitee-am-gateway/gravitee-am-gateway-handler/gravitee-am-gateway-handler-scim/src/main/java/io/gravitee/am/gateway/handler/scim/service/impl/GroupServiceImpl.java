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
package io.gravitee.am.gateway.handler.scim.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.gateway.handler.scim.exception.SCIMException;
import io.gravitee.am.gateway.handler.scim.exception.UniquenessException;
import io.gravitee.am.gateway.handler.scim.model.*;
import io.gravitee.am.gateway.handler.scim.model.Group;
import io.gravitee.am.gateway.handler.scim.model.ListResponse;
import io.gravitee.am.gateway.handler.scim.service.GroupService;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.management.api.GroupRepository;
import io.gravitee.am.repository.management.api.UserRepository;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.GroupNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.reactivex.*;
import io.reactivex.Completable;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class GroupServiceImpl implements GroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Domain domain;

    @Autowired
    private ObjectMapper objectMapper;

    
@Override
    public Mono<ListResponse<Group>> list_migrated(int page, int size, String baseUrl) {
        LOGGER.debug("Find groups by domain : {}", domain.getId());

        return groupRepository.findAll_migrated(ReferenceType.DOMAIN, domain.getId(), page, size).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Page<io.gravitee.am.model.Group>, SingleSource<ListResponse<io.gravitee.am.gateway.handler.scim.model.Group>>>toJdkFunction(groupPage -> {
                    // A negative value SHALL be interpreted as "0".
                    // A value of "0" indicates that no resource results are to be returned except for "totalResults".
                    if (size <= 0) {
                        return RxJava2Adapter.monoToSingle(Mono.just(new ListResponse<Group>(null, groupPage.getCurrentPage() + 1, groupPage.getTotalCount(), 0)));
                    } else {
                        // SCIM use 1-based index (increment current page)
                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToObservable(Flux.fromIterable(groupPage.getData()))
                                .map(group -> convert(group, baseUrl, true))
                                // set members
                                .flatMapSingle(group -> RxJava2Adapter.monoToSingle(setMembers_migrated(group, baseUrl)))
                                .toList()).map(RxJavaReactorMigrationUtil.toJdkFunction(groups -> new ListResponse<>(groups, groupPage.getCurrentPage() + 1, groupPage.getTotalCount(), groups.size()))));
                    }
                }).apply(v)))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<ListResponse<Group>>>toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find groups by domain {}", domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find groups by domain %s", domain), ex)));
                }).apply(err)));
    }

    
@Override
    public Flux<Group> findByMember_migrated(String memberId) {
        LOGGER.debug("Find groups by member : {}", memberId);
        return groupRepository.findByMember_migrated(memberId).map(RxJavaReactorMigrationUtil.toJdkFunction(group -> convert(group, null, true))).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a groups using member ", memberId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using member: %s", memberId), ex)));
                }));
    }

    
@Override
    public Mono<Group> get_migrated(String groupId, String baseUrl) {
        LOGGER.debug("Find group by id : {}", groupId);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(groupRepository.findById_migrated(groupId).map(RxJavaReactorMigrationUtil.toJdkFunction(group -> convert(group, baseUrl, false))).flatMap(z->setMembers_migrated(z, baseUrl)))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a group using its ID", groupId, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using its ID: %s", groupId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(group, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> create(Group group, String baseUrl) {
 return RxJava2Adapter.monoToSingle(create_migrated(group, baseUrl));
}
@Override
    public Mono<Group> create_migrated(Group group, String baseUrl) {
        LOGGER.debug("Create a new group {} for domain {}", group.getDisplayName(), domain.getName());

        // check if user is unique
        return groupRepository.findByName_migrated(ReferenceType.DOMAIN, domain.getId(), group.getDisplayName()).hasElement().map(RxJavaReactorMigrationUtil.toJdkFunction(isEmpty -> {
                    if (!isEmpty) {
                        throw new UniquenessException("Group with display name [" + group.getDisplayName()+ "] already exists");
                    }
                    return true;
                })).flatMap(__->setMembers_migrated(group, baseUrl)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.gateway.handler.scim.model.Group, SingleSource<io.gravitee.am.model.Group>>toJdkFunction(group1 -> {
                    io.gravitee.am.model.Group groupModel = convert(group1);
                    // set technical ID
                    groupModel.setId(RandomString.generate());
                    groupModel.setReferenceType(ReferenceType.DOMAIN);
                    groupModel.setReferenceId(domain.getId());
                    groupModel.setCreatedAt(new Date());
                    groupModel.setUpdatedAt(groupModel.getCreatedAt());
                    return RxJava2Adapter.monoToSingle(groupRepository.create_migrated(groupModel));
                }).apply(v)))).map(RxJavaReactorMigrationUtil.toJdkFunction(group1 -> convert(group1, baseUrl, true))).flatMap(group1->setMembers_migrated(group1, baseUrl)).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Group>>toJdkFunction(ex -> {
                    if (ex instanceof SCIMException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    } else {
                        LOGGER.error("An error occurs while trying to router a group", ex);
                        return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to router a group", ex)));
                    }
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(groupId, group, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> update(String groupId, Group group, String baseUrl) {
 return RxJava2Adapter.monoToSingle(update_migrated(groupId, group, baseUrl));
}
@Override
    public Mono<Group> update_migrated(String groupId, Group group, String baseUrl) {
        LOGGER.debug("Update a group {} for domain {}", groupId, domain.getName());
        return groupRepository.findById_migrated(groupId).switchIfEmpty(Mono.error(new GroupNotFoundException(groupId))).flatMap(y->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(groupRepository.findByName_migrated(ReferenceType.DOMAIN, domain.getId(), group.getDisplayName()).map(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.Group group1)->{
if (!y.getId().equals(group1.getId())) {
throw new UniquenessException("Group with display name [" + group.getDisplayName() + "] already exists");
}
return y;
})).defaultIfEmpty(y)).flatMapSingle((io.gravitee.am.model.Group irrelevant)->RxJava2Adapter.monoToSingle(setMembers_migrated(group, baseUrl)))).flatMap((io.gravitee.am.gateway.handler.scim.model.Group v)->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.gateway.handler.scim.model.Group group1)->{
io.gravitee.am.model.Group groupToUpdate = convert(group1);
groupToUpdate.setId(y.getId());
groupToUpdate.setReferenceType(y.getReferenceType());
groupToUpdate.setReferenceId(y.getReferenceId());
groupToUpdate.setCreatedAt(y.getCreatedAt());
groupToUpdate.setUpdatedAt(new Date());
return RxJava2Adapter.monoToSingle(groupRepository.update_migrated(groupToUpdate));
}).apply(v)))).map(RxJavaReactorMigrationUtil.toJdkFunction(group1 -> convert(group1, baseUrl, false))).flatMap(group1->setMembers_migrated(group1, baseUrl)).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Group>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof SCIMException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    } else {
                        LOGGER.error("An error occurs while trying to update a group", ex);
                        return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a group", ex)));
                    }
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(groupId, patchOp, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Group> patch(String groupId, PatchOp patchOp, String baseUrl) {
 return RxJava2Adapter.monoToSingle(patch_migrated(groupId, patchOp, baseUrl));
}
@Override
    public Mono<Group> patch_migrated(String groupId, PatchOp patchOp, String baseUrl) {
        LOGGER.debug("Patch a group {} for domain {}", groupId, domain.getName());
        return get_migrated(groupId, baseUrl).switchIfEmpty(Mono.error(new GroupNotFoundException(groupId))).flatMap(v->RxJava2Adapter.singleToMono((Single<Group>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Group, Single<Group>>)group -> {
                    ObjectNode node = objectMapper.convertValue(group, ObjectNode.class);
                    patchOp.getOperations().forEach(operation -> operation.apply(node));
                    return RxJava2Adapter.monoToSingle(update_migrated(groupId, objectMapper.treeToValue(node, Group.class), baseUrl));
                }).apply(v))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Group>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    } else {
                        LOGGER.error("An error has occurred when trying to delete group: {}", groupId, ex);
                        return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                                String.format("An error has occurred when trying to delete group: %s", groupId), ex)));
                    }
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(groupId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String groupId) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(groupId));
}
@Override
    public Mono<Void> delete_migrated(String groupId) {
        LOGGER.debug("Delete group {}", groupId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(groupRepository.findById_migrated(groupId).switchIfEmpty(Mono.error(new GroupNotFoundException(groupId))).flatMap(user->groupRepository.delete_migrated(groupId)).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    } else {
                        LOGGER.error("An error occurs while trying to delete group: {}", groupId, ex);
                        return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                                String.format("An error occurs while trying to delete group: %s", groupId), ex)));
                    }
                }));
    }

    
private Mono<Group> setMembers_migrated(Group group, String baseUrl) {
        Set<Member> members = group.getMembers() != null ? new HashSet<>(group.getMembers()) : null;
        if (members != null && !members.isEmpty()) {
            List<String> memberIds = group.getMembers().stream().map(Member::getValue).collect(Collectors.toList());
            return userRepository.findByIdIn_migrated(memberIds).map(RxJavaReactorMigrationUtil.toJdkFunction(user -> {
                        String display = (user.getDisplayName() != null) ? user.getDisplayName()
                                : (user.getFirstName() != null) ? user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "")
                                : user.getUsername();
                        String usersBaseUrl = baseUrl.substring(0, baseUrl.lastIndexOf("/Groups")).concat("/Users");
                        Member member = new Member();
                        member.setValue(user.getId());
                        member.setDisplay(display);
                        member.setRef(usersBaseUrl + "/" + user.getId());
                        return member;
                    })).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(existingMembers -> {
                        group.setMembers(existingMembers);
                        return group;
                    }));
        } else {
            return Mono.just(group);
        }
    }

    private Group convert(io.gravitee.am.model.Group group, String baseUrl, boolean listing) {
        Group scimGroup = new Group();
        scimGroup.setSchemas(Group.SCHEMAS);
        scimGroup.setId(group.getId());
        scimGroup.setDisplayName(group.getName());

        // members
        if (group.getMembers() != null && !group.getMembers().isEmpty()) {
            scimGroup.setMembers(group.getMembers().stream().map(userId -> {
                Member member = new Member();
                member.setValue(userId);
                return member;
            }).collect(Collectors.toList()));
        }

        // Meta
        Meta meta = new Meta();
        if (group.getCreatedAt() != null) {
            meta.setCreated(group.getCreatedAt().toInstant().toString());
        }
        if (group.getUpdatedAt() != null) {
            meta.setLastModified(group.getUpdatedAt().toInstant().toString());
        }
        meta.setResourceType(Group.RESOURCE_TYPE);
        if (baseUrl != null) {
            meta.setLocation(baseUrl + (listing ?  "/" + scimGroup.getId() : ""));
        }
        scimGroup.setMeta(meta);

        return scimGroup;
    }

    private io.gravitee.am.model.Group convert(Group scimGroup) {
        io.gravitee.am.model.Group group = new io.gravitee.am.model.Group();
        group.setId(scimGroup.getId());
        group.setName(scimGroup.getDisplayName());

        if (scimGroup.getMembers() != null) {
            group.setMembers(scimGroup.getMembers().stream().map(Member::getValue).collect(Collectors.toList()));
        }
        return group;
    }
}

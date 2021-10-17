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
import io.gravitee.am.model.*;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.model.membership.Member;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.model.permissions.SystemRole;
import io.gravitee.am.repository.management.api.MembershipRepository;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewMembership;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.DomainAuditBuilder;
import io.gravitee.am.service.reporter.builder.management.MembershipAuditBuilder;
import io.reactivex.Completable;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;

import java.util.*;
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
public class MembershipServiceImpl implements MembershipService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MembershipServiceImpl.class);

    @Lazy
    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private OrganizationUserService orgUserService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private EventService eventService;

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Membership> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Membership> findById_migrated(String id) {
        LOGGER.debug("Find membership by ID {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(membershipRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find membership by id {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find membership by ID %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCriteria_migrated(referenceType, referenceId, criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Membership> findByCriteria(ReferenceType referenceType, String referenceId, MembershipCriteria criteria) {
 return RxJava2Adapter.fluxToFlowable(findByCriteria_migrated(referenceType, referenceId, criteria));
}
@Override
    public Flux<Membership> findByCriteria_migrated(ReferenceType referenceType, String referenceId, MembershipCriteria criteria) {

        LOGGER.debug("Find memberships by reference type {} and reference id {} and criteria {}", referenceType, referenceId, criteria);

        return membershipRepository.findByCriteria_migrated(referenceType, referenceId, criteria);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReference_migrated(referenceId, referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Membership> findByReference(String referenceId, ReferenceType referenceType) {
 return RxJava2Adapter.fluxToFlowable(findByReference_migrated(referenceId, referenceType));
}
@Override
    public Flux<Membership> findByReference_migrated(String referenceId, ReferenceType referenceType) {
        LOGGER.debug("Find memberships by reference id {} and reference type {}", referenceId, referenceType);
        return membershipRepository.findByReference_migrated(referenceId, referenceType).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find memberships by reference id {} and reference type {}", referenceId, referenceType, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find memberships by reference id %s and reference type %s", referenceId, referenceType), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByMember_migrated(memberId, memberType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Membership> findByMember(String memberId, MemberType memberType) {
 return RxJava2Adapter.fluxToFlowable(findByMember_migrated(memberId, memberType));
}
@Override
    public Flux<Membership> findByMember_migrated(String memberId, MemberType memberType) {
        LOGGER.debug("Find memberships by member id {} and member type {}", memberId, memberType);
        return membershipRepository.findByMember_migrated(memberId, memberType).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find memberships by member id {} and member type {}", memberId, memberType, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find memberships by member id %s and member type %s", memberId, memberType), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.addOrUpdate_migrated(organizationId, membership, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Membership> addOrUpdate(String organizationId, Membership membership, User principal) {
 return RxJava2Adapter.monoToSingle(addOrUpdate_migrated(organizationId, membership, principal));
}
@Override
    public Mono<Membership> addOrUpdate_migrated(String organizationId, Membership membership, User principal) {
        LOGGER.debug("Add or update membership {}", membership);

        return checkMember_migrated(organizationId, membership).then(checkRole_migrated(organizationId, membership)).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId()).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()))
                        .flatMapSingle(optMembership -> {
                            if (!optMembership.isPresent()) {
                                // add membership
                                Membership newMembership = new Membership();
                                newMembership.setId(RandomString.generate());
                                newMembership.setDomain(membership.getDomain());
                                newMembership.setMemberId(membership.getMemberId());
                                newMembership.setMemberType(membership.getMemberType());
                                newMembership.setReferenceId(membership.getReferenceId());
                                newMembership.setReferenceType(membership.getReferenceType());
                                newMembership.setRoleId(membership.getRoleId());
                                newMembership.setCreatedAt(new Date());
                                newMembership.setUpdatedAt(newMembership.getCreatedAt());
                                return RxJava2Adapter.monoToSingle(createInternal_migrated(newMembership, principal));
                            } else {
                                // update membership
                                Membership oldMembership = optMembership.get();
                                Membership updateMembership = new Membership(oldMembership);
                                updateMembership.setRoleId(membership.getRoleId());
                                updateMembership.setUpdatedAt(new Date());
                                return RxJava2Adapter.monoToSingle(membershipRepository.update_migrated(updateMembership).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Membership, SingleSource<Membership>>toJdkFunction(membership1 -> {
                                            Event event = new Event(Type.MEMBERSHIP, new Payload(membership1.getId(), membership1.getReferenceType(), membership1.getReferenceId(), Action.UPDATE));
                                            return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(membership1)));
                                        }).apply(v)))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Membership>>toJdkFunction(ex -> {
                                            if (ex instanceof AbstractManagementException) {
                                                return RxJava2Adapter.monoToSingle(Mono.error(ex));
                                            }
                                            LOGGER.error("An error occurs while trying to update membership {}", oldMembership, ex);
                                            return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to update membership %s", oldMembership), ex)));
                                        }).apply(err))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(membership1 -> auditService.report(AuditBuilder.builder(MembershipAuditBuilder.class).principal(principal).type(EventType.MEMBERSHIP_UPDATED).oldValue(oldMembership).membership(membership1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.MEMBERSHIP_UPDATED).throwable(throwable)))));
                            }
                        })));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.setPlatformAdmin_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Membership> setPlatformAdmin(String userId) {
 return RxJava2Adapter.monoToSingle(setPlatformAdmin_migrated(userId));
}
@Override
    public Mono<Membership> setPlatformAdmin_migrated(String userId) {

        MembershipCriteria criteria = new MembershipCriteria();
        criteria.setUserId(userId);

        return roleService.findSystemRole_migrated(SystemRole.PLATFORM_ADMIN, ReferenceType.PLATFORM).switchIfEmpty(Mono.error(new RoleNotFoundException(SystemRole.PLATFORM_ADMIN.name()))).flatMap(role->findByCriteria_migrated(ReferenceType.PLATFORM, Platform.DEFAULT, criteria).next().switchIfEmpty(RxJava2Adapter.singleToMono(Single.defer(()->{
final Date now = new Date();
Membership membership = new Membership();
membership.setRoleId(role.getId());
membership.setMemberType(MemberType.USER);
membership.setMemberId(userId);
membership.setReferenceType(ReferenceType.PLATFORM);
membership.setReferenceId(Platform.DEFAULT);
membership.setCreatedAt(now);
membership.setUpdatedAt(now);
return RxJava2Adapter.monoToSingle(createInternal_migrated(membership, null));
}))));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getMetadata_migrated(memberships))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Map<String, Map<String, Object>>> getMetadata(List<Membership> memberships) {
 return RxJava2Adapter.monoToSingle(getMetadata_migrated(memberships));
}
@Override
    public Mono<Map<String,Map<String,Object>>> getMetadata_migrated(List<Membership> memberships) {
        if (memberships == null || memberships.isEmpty()) {
            return Mono.just(Collections.emptyMap());
        }

        List<String> userIds = memberships.stream().filter(membership -> MemberType.USER == membership.getMemberType()).map(Membership::getMemberId).distinct().collect(Collectors.toList());
        List<String> groupIds = memberships.stream().filter(membership -> MemberType.GROUP == membership.getMemberType()).map(Membership::getMemberId).distinct().collect(Collectors.toList());
        List<String> roleIds = memberships.stream().map(Membership::getRoleId).distinct().collect(Collectors.toList());

        return RxJava2Adapter.singleToMono(Single.zip(RxJava2Adapter.fluxToFlowable(orgUserService.findByIdIn_migrated(userIds)).toMap(io.gravitee.am.model.User::getId, this::convert),
                RxJava2Adapter.fluxToFlowable(groupService.findByIdIn_migrated(groupIds)).toMap(Group::getId, this::convert),
                RxJava2Adapter.monoToSingle(roleService.findByIdIn_migrated(roleIds)), (users, groups, roles) -> {
            Map<String, Map<String, Object>> metadata = new HashMap<>();
            metadata.put("users", (Map)users);
            metadata.put("groups", (Map)groups);
            metadata.put("roles", roles.stream().collect(Collectors.toMap(Role::getId, this::filter)));
            return metadata;
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(membershipId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String membershipId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(membershipId, principal));
}
@Override
    public Mono<Void> delete_migrated(String membershipId, User principal) {
        LOGGER.debug("Delete membership {}", membershipId);

        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(membershipRepository.findById_migrated(membershipId).switchIfEmpty(Mono.error(new MembershipNotFoundException(membershipId))).flatMap(membership->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(membershipRepository.delete_migrated(membershipId).then(RxJava2Adapter.completableToMono(Completable.fromSingle(RxJava2Adapter.monoToSingle(eventService.create_migrated(new Event(Type.MEMBERSHIP, new Payload(membership.getId(), membership.getReferenceType(), membership.getReferenceId(), Action.DELETE)))))))).doOnComplete(()->auditService.report(AuditBuilder.builder(MembershipAuditBuilder.class).principal(principal).type(EventType.MEMBERSHIP_DELETED).membership(membership)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((Throwable throwable)->auditService.report(AuditBuilder.builder(MembershipAuditBuilder.class).principal(principal).type(EventType.MEMBERSHIP_DELETED).throwable(throwable))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to delete membership: {}", membershipId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete membership: %s", membershipId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.addDomainUserRoleIfNecessary_migrated(organizationId, environmentId, domainId, newMembership, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable addDomainUserRoleIfNecessary(String organizationId, String environmentId, String domainId, NewMembership newMembership, User principal) {
 return RxJava2Adapter.monoToCompletable(addDomainUserRoleIfNecessary_migrated(organizationId, environmentId, domainId, newMembership, principal));
}
@Override
    public Mono<Void> addDomainUserRoleIfNecessary_migrated(String organizationId, String environmentId, String domainId, NewMembership newMembership, User principal) {

        MembershipCriteria criteria = convert(newMembership);

        return this.findByCriteria_migrated(ReferenceType.DOMAIN, domainId, criteria).switchIfEmpty(Flux.defer(RxJavaReactorMigrationUtil.callableAsSupplier(() -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(roleService.findDefaultRole_migrated(organizationId, DefaultRole.DOMAIN_USER, ReferenceType.DOMAIN))
                        .flatMapSingle(role -> {
                            final Membership domainMembership = new Membership();
                            domainMembership.setMemberId(newMembership.getMemberId());
                            domainMembership.setMemberType(newMembership.getMemberType());
                            domainMembership.setRoleId(role.getId());
                            domainMembership.setReferenceId(domainId);
                            domainMembership.setReferenceType(ReferenceType.DOMAIN);
                            return RxJava2Adapter.monoToSingle(this.createInternal_migrated(domainMembership, principal));
                        })).flux())))).ignoreElements().then().then(addEnvironmentUserRoleIfNecessary_migrated(organizationId, environmentId, newMembership, principal));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.addEnvironmentUserRoleIfNecessary_migrated(organizationId, environmentId, newMembership, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable addEnvironmentUserRoleIfNecessary(String organizationId, String environmentId, NewMembership newMembership, User principal) {
 return RxJava2Adapter.monoToCompletable(addEnvironmentUserRoleIfNecessary_migrated(organizationId, environmentId, newMembership, principal));
}
@Override
    public Mono<Void> addEnvironmentUserRoleIfNecessary_migrated(String organizationId, String environmentId, NewMembership newMembership, User principal) {

        MembershipCriteria criteria = convert(newMembership);

        return this.findByCriteria_migrated(ReferenceType.ENVIRONMENT, environmentId, criteria).switchIfEmpty(Flux.defer(RxJavaReactorMigrationUtil.callableAsSupplier(() -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(roleService.findDefaultRole_migrated(organizationId, DefaultRole.ENVIRONMENT_USER, ReferenceType.ENVIRONMENT))
                        .flatMapSingle(role -> {
                            final Membership environmentMembership = new Membership();
                            environmentMembership.setMemberId(newMembership.getMemberId());
                            environmentMembership.setMemberType(newMembership.getMemberType());
                            environmentMembership.setRoleId(role.getId());
                            environmentMembership.setReferenceId(environmentId);
                            environmentMembership.setReferenceType(ReferenceType.ENVIRONMENT);
                            return RxJava2Adapter.monoToSingle(this.createInternal_migrated(environmentMembership, principal));
                        })).flux())))).ignoreElements().then();
    }

    
private Mono<Membership> createInternal_migrated(Membership membership, User principal) {
        return membershipRepository.create_migrated(membership).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Membership, SingleSource<Membership>>toJdkFunction(membership1 -> {
                    Event event = new Event(Type.MEMBERSHIP, new Payload(membership1.getId(), membership1.getReferenceType(), membership1.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(membership1)));
                }).apply(v)))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Membership>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create membership {}", membership, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to create membership %s", membership), ex)));
                }).apply(err))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(membership1 -> auditService.report(AuditBuilder.builder(MembershipAuditBuilder.class).principal(principal).type(EventType.MEMBERSHIP_CREATED).membership(membership1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.MEMBERSHIP_CREATED).throwable(throwable))));
    }

    private Member convert(io.gravitee.am.model.User user) {
        Member member = new Member();
        member.setId(user.getId());
        member.setDisplayName(user.getDisplayName());
        return member;
    }

    private Member convert(Group group) {
        Member member = new Member();
        member.setId(group.getId());
        member.setDisplayName(group.getName());
        return member;
    }

    private MembershipCriteria convert(NewMembership newMembership) {

        MembershipCriteria criteria = new MembershipCriteria();

        if (newMembership.getMemberType() == MemberType.USER) {
            criteria.setUserId(newMembership.getMemberId());
        } else {
            criteria.setGroupIds(Collections.singletonList(newMembership.getMemberId()));
        }
        return criteria;
    }

    private Role filter(Role role) {
        Role filteredRole = new Role();
        filteredRole.setId(role.getId());
        filteredRole.setName(role.getName());
        return filteredRole;
    }

    
private Mono<Void> checkMember_migrated(String organizationId, Membership membership) {

        if (MemberType.USER == membership.getMemberType()) {
            return orgUserService.findById_migrated(ReferenceType.ORGANIZATION, organizationId, membership.getMemberId()).then();
        } else {
            return groupService.findById_migrated(ReferenceType.ORGANIZATION, organizationId, membership.getMemberId()).then();
        }
    }

    
private Mono<Void> checkRole_migrated(String organizationId, Membership membership) {
        return roleService.findById_migrated(membership.getRoleId()).switchIfEmpty(Mono.error(new RoleNotFoundException(membership.getRoleId()))).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<Role, MaybeSource<Role>>toJdkFunction(role -> {
                    // If role is a 'PRIMARY_OWNER' role, need to check if it is already assigned or not.
                    if (role.isSystem() && role.getName().endsWith("_PRIMARY_OWNER")) {

                        if (membership.getMemberType() == MemberType.GROUP) {
                            return RxJava2Adapter.monoToMaybe(Mono.error(new InvalidRoleException("This role cannot be assigned to a group")));
                        }

                        MembershipCriteria criteria = new MembershipCriteria();
                        criteria.setRoleId(membership.getRoleId());
                        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(membershipRepository.findByCriteria_migrated(membership.getReferenceType(), membership.getReferenceId(), criteria).filter(RxJavaReactorMigrationUtil.toJdkPredicate(existingMembership -> !existingMembership.isMember(membership.getMemberType(), membership.getMemberId())))) // Exclude the member himself if he is already the primary owner.
                                .count()).flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<Long, MaybeSource<Role>>toJdkFunction(count -> count >= 1 ? RxJava2Adapter.monoToMaybe(Mono.error(new SinglePrimaryOwnerException(membership.getReferenceType()))) : RxJava2Adapter.monoToMaybe(Mono.just(role))).apply(e)))));
                    }

                    return RxJava2Adapter.monoToMaybe(Mono.just(role));
                }).apply(v)))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(role1 -> role1.getAssignableType() == membership.getReferenceType() &&
                        // Role can be either a system role, either an organization role, either a domain role.
                        (role1.isSystem()
                                || (role1.getReferenceType() == ReferenceType.ORGANIZATION && organizationId.equals(role1.getReferenceId()))
                                || (role1.getReferenceType() == membership.getReferenceType() && membership.getReferenceId().equals(role1.getReferenceId()))))).switchIfEmpty(Mono.error(new InvalidRoleException("Invalid role"))).then();
    }
}

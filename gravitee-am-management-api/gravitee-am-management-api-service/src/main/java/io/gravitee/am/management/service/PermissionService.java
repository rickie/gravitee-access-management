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
package io.gravitee.am.management.service;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.service.permissions.PermissionAcls;
import io.gravitee.am.model.*;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.InvalidUserException;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class PermissionService {

    private final MembershipService membershipService;
    private final GroupService groupService;
    private final RoleService roleService;
    private final EnvironmentService environmentService;
    private final DomainService domainService;
    private final ApplicationService applicationService;
    private final Map<String, Boolean> consistencyCache;

    public PermissionService(MembershipService membershipService,
                             GroupService groupService,
                             RoleService roleService,
                             EnvironmentService environmentService,
                             DomainService domainService,
                             ApplicationService applicationService) {
        this.membershipService = membershipService;
        this.groupService = groupService;
        this.roleService = roleService;
        this.environmentService = environmentService;
        this.domainService = domainService;
        this.applicationService = applicationService;
        this.consistencyCache = new ConcurrentHashMap<>();
    }

    public Single<Map<Permission, Set<Acl>>> findAllPermissions(User user, ReferenceType referenceType, String referenceId) {

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(findMembershipPermissions(user, Collections.singletonMap(referenceType, referenceId).entrySet().stream())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::aclsPerPermission)));
    }

    public Single<Boolean> hasPermission(User user, PermissionAcls permissions) {

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(haveConsistentReferenceIds(permissions)).flatMap(v->RxJava2Adapter.singleToMono((Single<Boolean>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, Single<Boolean>>)consistent -> {
                    if (consistent) {
                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(findMembershipPermissions(user, permissions.referenceStream())).map(RxJavaReactorMigrationUtil.toJdkFunction(permissions::match)));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(false));
                }).apply(v))));
    }

    protected Single<Boolean> haveConsistentReferenceIds(PermissionAcls permissionAcls) {

        try {
            Map<ReferenceType, String> referenceMap = permissionAcls.referenceStream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (referenceMap.size() == 1) {
                // There is only one type. Consistency is ok.
                return RxJava2Adapter.monoToSingle(Mono.just(true));
            }

            // When checking acls for multiple types in same time, we need to check if tuples [ReferenceType - ReferenceId] are consistent each other.
            // Ex: when we check for DOMAIN_READ permission on domain X or DOMAIN_READ environment Y, we need to make sure that domain X is effectively attached to domain Y and grand permission by inheritance.
            String applicationId = referenceMap.get(ReferenceType.APPLICATION);
            String domainId = referenceMap.get(ReferenceType.DOMAIN);
            String environmentId = referenceMap.get(ReferenceType.ENVIRONMENT);
            String organizationId = referenceMap.get(ReferenceType.ORGANIZATION);

            String key = StringUtils.arrayToDelimitedString(new String[]{applicationId, domainId, environmentId, organizationId}, "#");

            if(consistencyCache.containsKey(key)) {
                return RxJava2Adapter.monoToSingle(Mono.just(consistencyCache.get(key)));
            }

            List<Single<Boolean>> obs = new ArrayList<>();

            if (applicationId != null) {
                obs.add(isApplicationIdConsistent(applicationId, domainId, environmentId, organizationId));
            }

            if (domainId != null) {
                obs.add(isDomainIdConsistent(domainId, environmentId, organizationId));
            }

            if (environmentId != null) {
                obs.add(isEnvironmentIdConsistent(environmentId, organizationId));
            }

            return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(Single.merge(obs)).all(RxJavaReactorMigrationUtil.toJdkPredicate(consistent -> consistent)))
                    .onErrorResumeNext(RxJava2Adapter.monoToSingle(Mono.just(false)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(consistent -> consistencyCache.put(key, consistent))));
        } catch (Exception e){
            return RxJava2Adapter.monoToSingle(Mono.just(false));
        }
    }

    private Single<Boolean> isApplicationIdConsistent(String applicationId, String domainId, String environmentId, String organizationId) {

        if(domainId == null && environmentId == null && organizationId == null) {
            return RxJava2Adapter.monoToSingle(Mono.just(true));
        }

        return applicationService.findById(applicationId)
                .flatMapSingle(application -> {
                    if (domainId != null) {
                        return RxJava2Adapter.monoToSingle(Mono.just(application.getDomain().equals(domainId)));
                    } else {
                        // Need to fetch the domain to check if it belongs to the environment / organization.
                        return isDomainIdConsistent(application.getDomain(), environmentId, organizationId);
                    }
                });
    }

    private Single<Boolean> isDomainIdConsistent(String domainId, String environmentId, String organizationId) {

        if(environmentId == null && organizationId == null) {
            return RxJava2Adapter.monoToSingle(Mono.just(true));
        }

        return domainService.findById(domainId)
                .flatMapSingle(domain -> {
                    if (environmentId != null) {
                        return RxJava2Adapter.monoToSingle(Mono.just(domain.getReferenceId().equals(environmentId) && domain.getReferenceType() == ReferenceType.ENVIRONMENT));
                    } else {
                        // Need to fetch the environment to check if it belongs to the organization.
                        return isEnvironmentIdConsistent(domain.getReferenceId(), organizationId);
                    }
                });
    }

    private Single<Boolean> isEnvironmentIdConsistent(String environmentId, String organizationId) {

        if (organizationId == null) {
            return RxJava2Adapter.monoToSingle(Mono.just(true));
        }

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(environmentService.findById(environmentId, organizationId)).map(RxJavaReactorMigrationUtil.toJdkFunction(environment -> true)))
                .onErrorResumeNext(RxJava2Adapter.monoToSingle(Mono.just(false)));
    }

    private Single<Map<Membership, Map<Permission, Set<Acl>>>> findMembershipPermissions(User user, Stream<Map.Entry<ReferenceType, String>> referenceStream) {

        if (user.getId() == null) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidUserException("Specified user is invalid")));
        }

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(groupService.findByMember(user.getId())
                .map(Group::getId)).collectList().flatMap(v->RxJava2Adapter.singleToMono((Single<Map<Membership, Map<Permission, Set<Acl>>>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<String>, Single<Map<Membership, Map<Permission, Set<Acl>>>>>)userGroupIds -> {
                    MembershipCriteria criteria = new MembershipCriteria();
                    criteria.setUserId(user.getId());
                    criteria.setGroupIds(userGroupIds.isEmpty() ? null : userGroupIds);
                    criteria.setLogicalOR(true);

                    // Get all user and group memberships.
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Flowable.merge(referenceStream.map(p -> membershipService.findByCriteria(p.getKey(), p.getValue(), criteria)).collect(Collectors.toList()))
                            .toList()).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<List<Membership>, SingleSource<Map<Membership, Map<Permission, Set<Acl>>>>>toJdkFunction(allMemberships -> {

                                if (allMemberships.isEmpty()) {
                                    return Single.just(Collections.emptyMap());
                                }

                                // Get all roles.
                                return roleService.findByIdIn(allMemberships.stream().map(Membership::getRoleId).collect(Collectors.toList()))
                                        .map(allRoles -> permissionsPerMembership(allMemberships, allRoles));
                            }).apply(v)))));
                }).apply(v))));
    }

    private Map<Membership, Map<Permission, Set<Acl>>> permissionsPerMembership(List<Membership> allMemberships, Set<Role> allRoles) {

        Map<String, Role> allRolesById = allRoles.stream().collect(Collectors.toMap(Role::getId, role -> role));
        Map<Membership, Map<Permission, Set<Acl>>> rolesPerMembership = allMemberships.stream().collect(Collectors.toMap(membership -> membership, o -> new HashMap<>()));

        rolesPerMembership.forEach((membership, permissions) -> {
            Role role = allRolesById.get(membership.getRoleId());

            // Need to check the membership role is well assigned (ie: the role is assignable with the membership type).
            if (role != null && role.getAssignableType() == membership.getReferenceType()) {
                Map<Permission, Set<Acl>> rolePermissions = role.getPermissionAcls();

                // Compute membership permission acls.
                rolePermissions.forEach((permission, acls) -> {
                    permissions.merge(permission, acls, (acls1, acls2) -> {
                        acls1.addAll(acls2);
                        return acls1;
                    });
                });
            }
        });

        return rolesPerMembership;
    }

    private Map<Permission, Set<Acl>> aclsPerPermission(Map<Membership, Map<Permission, Set<Acl>>> rolesPerMembership) {

        Map<Permission, Set<Acl>> permissions = new HashMap<>();

        rolesPerMembership.forEach((membership, membershipPermissions) ->
                membershipPermissions.forEach((permission, acls) -> {

                    // Compute acls of same Permission.
                    if (permissions.containsKey(permission)) {
                        permissions.get(permission).addAll(acls);
                    } else {
                        permissions.put(permission, new HashSet<>(acls));
                    }
                }));

        return permissions;
    }
}
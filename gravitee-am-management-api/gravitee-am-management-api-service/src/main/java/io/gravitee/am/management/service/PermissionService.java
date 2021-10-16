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

import com.google.errorprone.annotations.InlineMe;
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

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAllPermissions_migrated(user, referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Single<Map<Permission, Set<Acl>>> findAllPermissions(User user, ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.monoToSingle(findAllPermissions_migrated(user, referenceType, referenceId));
}
public Mono<Map<Permission,Set<Acl>>> findAllPermissions_migrated(User user, ReferenceType referenceType, String referenceId) {

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(findMembershipPermissions_migrated(user, Collections.singletonMap(referenceType, referenceId).entrySet().stream()))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::aclsPerPermission));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.hasPermission_migrated(user, permissions))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Single<Boolean> hasPermission(User user, PermissionAcls permissions) {
 return RxJava2Adapter.monoToSingle(hasPermission_migrated(user, permissions));
}
public Mono<Boolean> hasPermission_migrated(User user, PermissionAcls permissions) {

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(haveConsistentReferenceIds_migrated(permissions))).flatMap(v->RxJava2Adapter.singleToMono((Single<Boolean>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, Single<Boolean>>)consistent -> {
                    if (consistent) {
                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(findMembershipPermissions_migrated(user, permissions.referenceStream()))).map(RxJavaReactorMigrationUtil.toJdkFunction(permissions::match)));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(false));
                }).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.haveConsistentReferenceIds_migrated(permissionAcls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Single<Boolean> haveConsistentReferenceIds(PermissionAcls permissionAcls) {
 return RxJava2Adapter.monoToSingle(haveConsistentReferenceIds_migrated(permissionAcls));
}
protected Mono<Boolean> haveConsistentReferenceIds_migrated(PermissionAcls permissionAcls) {

        try {
            Map<ReferenceType, String> referenceMap = permissionAcls.referenceStream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (referenceMap.size() == 1) {
                // There is only one type. Consistency is ok.
                return Mono.just(true);
            }

            // When checking acls for multiple types in same time, we need to check if tuples [ReferenceType - ReferenceId] are consistent each other.
            // Ex: when we check for DOMAIN_READ permission on domain X or DOMAIN_READ environment Y, we need to make sure that domain X is effectively attached to domain Y and grand permission by inheritance.
            String applicationId = referenceMap.get(ReferenceType.APPLICATION);
            String domainId = referenceMap.get(ReferenceType.DOMAIN);
            String environmentId = referenceMap.get(ReferenceType.ENVIRONMENT);
            String organizationId = referenceMap.get(ReferenceType.ORGANIZATION);

            String key = StringUtils.arrayToDelimitedString(new String[]{applicationId, domainId, environmentId, organizationId}, "#");

            if(consistencyCache.containsKey(key)) {
                return Mono.just(consistencyCache.get(key));
            }

            List<Single<Boolean>> obs = new ArrayList<>();

            if (applicationId != null) {
                obs.add(RxJava2Adapter.monoToSingle(isApplicationIdConsistent_migrated(applicationId, domainId, environmentId, organizationId)));
            }

            if (domainId != null) {
                obs.add(RxJava2Adapter.monoToSingle(isDomainIdConsistent_migrated(domainId, environmentId, organizationId)));
            }

            if (environmentId != null) {
                obs.add(RxJava2Adapter.monoToSingle(isEnvironmentIdConsistent_migrated(environmentId, organizationId)));
            }

            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(Single.merge(obs)).all(RxJavaReactorMigrationUtil.toJdkPredicate(consistent -> consistent)))
                    .onErrorResumeNext(RxJava2Adapter.monoToSingle(Mono.just(false)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(consistent -> consistencyCache.put(key, consistent)));
        } catch (Exception e){
            return Mono.just(false);
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.isApplicationIdConsistent_migrated(applicationId, domainId, environmentId, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Boolean> isApplicationIdConsistent(String applicationId, String domainId, String environmentId, String organizationId) {
 return RxJava2Adapter.monoToSingle(isApplicationIdConsistent_migrated(applicationId, domainId, environmentId, organizationId));
}
private Mono<Boolean> isApplicationIdConsistent_migrated(String applicationId, String domainId, String environmentId, String organizationId) {

        if(domainId == null && environmentId == null && organizationId == null) {
            return Mono.just(true);
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(applicationService.findById_migrated(applicationId))
                .flatMapSingle(application -> {
                    if (domainId != null) {
                        return RxJava2Adapter.monoToSingle(Mono.just(application.getDomain().equals(domainId)));
                    } else {
                        // Need to fetch the domain to check if it belongs to the environment / organization.
                        return RxJava2Adapter.monoToSingle(isDomainIdConsistent_migrated(application.getDomain(), environmentId, organizationId));
                    }
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.isDomainIdConsistent_migrated(domainId, environmentId, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Boolean> isDomainIdConsistent(String domainId, String environmentId, String organizationId) {
 return RxJava2Adapter.monoToSingle(isDomainIdConsistent_migrated(domainId, environmentId, organizationId));
}
private Mono<Boolean> isDomainIdConsistent_migrated(String domainId, String environmentId, String organizationId) {

        if(environmentId == null && organizationId == null) {
            return Mono.just(true);
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domainId))
                .flatMapSingle(domain -> {
                    if (environmentId != null) {
                        return RxJava2Adapter.monoToSingle(Mono.just(domain.getReferenceId().equals(environmentId) && domain.getReferenceType() == ReferenceType.ENVIRONMENT));
                    } else {
                        // Need to fetch the environment to check if it belongs to the organization.
                        return RxJava2Adapter.monoToSingle(isEnvironmentIdConsistent_migrated(domain.getReferenceId(), organizationId));
                    }
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.isEnvironmentIdConsistent_migrated(environmentId, organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Boolean> isEnvironmentIdConsistent(String environmentId, String organizationId) {
 return RxJava2Adapter.monoToSingle(isEnvironmentIdConsistent_migrated(environmentId, organizationId));
}
private Mono<Boolean> isEnvironmentIdConsistent_migrated(String environmentId, String organizationId) {

        if (organizationId == null) {
            return Mono.just(true);
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(environmentService.findById_migrated(environmentId, organizationId).map(RxJavaReactorMigrationUtil.toJdkFunction(environment -> true)))
                .onErrorResumeNext(RxJava2Adapter.monoToSingle(Mono.just(false))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findMembershipPermissions_migrated(user, referenceStream))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Map<Membership, Map<Permission, Set<Acl>>>> findMembershipPermissions(User user, Stream<Map.Entry<ReferenceType, String>> referenceStream) {
 return RxJava2Adapter.monoToSingle(findMembershipPermissions_migrated(user, referenceStream));
}
private Mono<Map<Membership,Map<Permission,Set<Acl>>>> findMembershipPermissions_migrated(User user, Stream<Map.Entry<ReferenceType, String>> referenceStream) {

        if (user.getId() == null) {
            return Mono.error(new InvalidUserException("Specified user is invalid"));
        }

        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(groupService.findByMember_migrated(user.getId()))).map(RxJavaReactorMigrationUtil.toJdkFunction(Group::getId)).collectList().flatMap(v->RxJava2Adapter.singleToMono((Single<Map<Membership, Map<Permission, Set<Acl>>>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<String>, Single<Map<Membership, Map<Permission, Set<Acl>>>>>)userGroupIds -> {
                    MembershipCriteria criteria = new MembershipCriteria();
                    criteria.setUserId(user.getId());
                    criteria.setGroupIds(userGroupIds.isEmpty() ? null : userGroupIds);
                    criteria.setLogicalOR(true);

                    // Get all user and group memberships.
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(Flowable.merge(referenceStream.map(p -> RxJava2Adapter.fluxToFlowable(membershipService.findByCriteria_migrated(p.getKey(), p.getValue(), criteria))).collect(Collectors.toList()))).collectList().flatMap(z->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<List<Membership>, SingleSource<Map<Membership, Map<Permission, Set<Acl>>>>>toJdkFunction(allMemberships -> {

                                if (allMemberships.isEmpty()) {
                                    return RxJava2Adapter.monoToSingle(Mono.just(Collections.emptyMap()));
                                }

                                // Get all roles.
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleService.findByIdIn_migrated(allMemberships.stream().map(Membership::getRoleId).collect(Collectors.toList())))).map(RxJavaReactorMigrationUtil.toJdkFunction(allRoles -> permissionsPerMembership(allMemberships, allRoles))));
                            }).apply(z)))));
                }).apply(v)));
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
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

import static io.gravitee.am.management.service.permissions.Permissions.*;
import static io.gravitee.am.management.service.permissions.Permissions.of;
import static io.gravitee.am.model.Acl.CREATE;
import static io.gravitee.am.model.Acl.READ;
import static io.gravitee.am.model.permissions.Permission.*;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.model.*;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.EnvironmentNotFoundException;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class PermissionServiceTest {

    private static final String ORGANIZATION_ID = "orga#1";
    public static final String USER_ID = "user#1";
    public static final String ROLE_ID = "role#1";
    public static final String GROUP_ID = "group#1";
    public static final String DOMAIN_ID = "domain#1";
    public static final String ENVIRONMENT_ID = "environment#1";
    private static final String ROLE_ID2 = "role#2";
    private static final String ROLE_ID3 = "role#3";
    public static final String APPLICATION_ID = "application#1";

    @Mock
    private MembershipService membershipService;

    @Mock
    private GroupService groupService;

    @Mock
    private RoleService roleService;

    @Mock
    private EnvironmentService environmentService;

    @Mock
    private DomainService domainService;

    @Mock
    private ApplicationService applicationService;

    private PermissionService cut;

    @Before
    public void before() {
        cut = new PermissionService(membershipService, groupService, roleService, environmentService, domainService, applicationService);
    }

    @Test
    public void hasPermission_fromUserMemberships() {

        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        Membership membership = new Membership();
        membership.setMemberType(MemberType.USER);
        membership.setMemberId(USER_ID);
        membership.setReferenceType(ReferenceType.ORGANIZATION);
        membership.setReferenceId(ORGANIZATION_ID);
        membership.setRoleId(ROLE_ID);

        Role role = new Role();
        role.setId(ROLE_ID);
        role.setAssignableType(ReferenceType.ORGANIZATION);
        role.setPermissionAcls(Permission.of(ORGANIZATION, READ));

        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ORGANIZATION), eq(ORGANIZATION_ID), argThat(criteria -> criteria.getUserId().get().equals(user.getId())
                && !criteria.getGroupIds().isPresent()
                && criteria.isLogicalOR()))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(membership))));
        when(roleService.findByIdIn_migrated(Arrays.asList(membership.getRoleId()))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Collections.singleton(role)))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(user, of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, Permission.ORGANIZATION, READ)))
                .test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(true);
    }

    @Test
    public void hasPermission_fromGroupMemberships() {

        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        Membership membership = new Membership();
        membership.setMemberType(MemberType.GROUP);
        membership.setMemberId(GROUP_ID);
        membership.setReferenceType(ReferenceType.ORGANIZATION);
        membership.setReferenceId(ORGANIZATION_ID);
        membership.setRoleId(ROLE_ID);

        Role role = new Role();
        role.setId(ROLE_ID);
        role.setAssignableType(ReferenceType.ORGANIZATION);
        role.setPermissionAcls(Permission.of(ORGANIZATION, READ));

        Group group = new Group();
        group.setId(GROUP_ID);
        group.setReferenceType(ReferenceType.ORGANIZATION);
        group.setReferenceId(ORGANIZATION_ID);
        group.setMembers(Arrays.asList(user.getId()));

        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(group))));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ORGANIZATION), eq(ORGANIZATION_ID), argThat(criteria -> criteria.getUserId().get().equals(user.getId())
                && criteria.getGroupIds().get().equals(Arrays.asList(group.getId()))
                && criteria.isLogicalOR()))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(membership))));
        when(roleService.findByIdIn_migrated(Arrays.asList(membership.getRoleId()))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Collections.singleton(role)))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(user, of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, Permission.ORGANIZATION, READ)))
                .test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(true);
    }

    @Test
    public void hasPermissionResource_noMembership() {

        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ORGANIZATION), eq(ORGANIZATION_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(user, of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, Permission.ORGANIZATION, READ)))
                .test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(false);
    }

    @Test
    public void hasPermission_hasAllPermissions() {

        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        Membership organizationMembership = new Membership();
        organizationMembership.setMemberType(MemberType.USER);
        organizationMembership.setMemberId(USER_ID);
        organizationMembership.setReferenceType(ReferenceType.ORGANIZATION);
        organizationMembership.setReferenceId(ORGANIZATION_ID);
        organizationMembership.setRoleId(ROLE_ID);

        Membership environmentMembership = new Membership();
        environmentMembership.setMemberType(MemberType.USER);
        environmentMembership.setMemberId(USER_ID);
        environmentMembership.setReferenceType(ReferenceType.ENVIRONMENT);
        environmentMembership.setReferenceId(ENVIRONMENT_ID);
        environmentMembership.setRoleId(ROLE_ID2);

        Membership domainMembership = new Membership();
        domainMembership.setMemberType(MemberType.USER);
        domainMembership.setMemberId(USER_ID);
        domainMembership.setReferenceType(ReferenceType.DOMAIN);
        domainMembership.setReferenceId(DOMAIN_ID);
        domainMembership.setRoleId(ROLE_ID3);

        Role organizationRole = new Role();
        organizationRole.setId(ROLE_ID);
        organizationRole.setAssignableType(ReferenceType.ORGANIZATION);
        organizationRole.setPermissionAcls(Permission.of(DOMAIN, READ));

        Role environmentRole = new Role();
        environmentRole.setId(ROLE_ID2);
        environmentRole.setAssignableType(ReferenceType.ENVIRONMENT);
        environmentRole.setPermissionAcls(Permission.of(DOMAIN, READ));

        Role domainRole = new Role();
        domainRole.setId(ROLE_ID3);
        domainRole.setAssignableType(ReferenceType.DOMAIN);
        domainRole.setPermissionAcls(Permission.of(DOMAIN, READ));

        Domain domain = new Domain();
        domain.setReferenceType(ReferenceType.ENVIRONMENT);
        domain.setReferenceId(ENVIRONMENT_ID);

        Environment environment = new Environment();
        environment.setOrganizationId(ORGANIZATION_ID);

        when(domainService.findById_migrated(eq(DOMAIN_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(domain))));
        when(environmentService.findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(environment))));
        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ORGANIZATION), eq(ORGANIZATION_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(organizationMembership))));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ENVIRONMENT), eq(ENVIRONMENT_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(environmentMembership))));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(domainMembership))));
        when(roleService.findByIdIn_migrated(Arrays.asList(organizationMembership.getRoleId(), environmentMembership.getRoleId(), domainMembership.getRoleId()))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new HashSet<>(Arrays.asList(organizationRole, environmentRole, domainRole))))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(user, and(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, DOMAIN, READ),
                        of(ReferenceType.ENVIRONMENT, ENVIRONMENT_ID, DOMAIN, READ),
                        of(ReferenceType.DOMAIN, DOMAIN_ID, Permission.DOMAIN, READ)))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(true);
    }

    @Test
    public void hasPermission_hasNotAllPermissions() {

        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        Membership organizationMembership = new Membership();
        organizationMembership.setMemberType(MemberType.USER);
        organizationMembership.setMemberId(USER_ID);
        organizationMembership.setReferenceType(ReferenceType.ORGANIZATION);
        organizationMembership.setReferenceId(ORGANIZATION_ID);
        organizationMembership.setRoleId(ROLE_ID);

        Membership domainMembership = new Membership();
        domainMembership.setMemberType(MemberType.USER);
        domainMembership.setMemberId(USER_ID);
        domainMembership.setReferenceType(ReferenceType.DOMAIN);
        domainMembership.setReferenceId(DOMAIN_ID);
        domainMembership.setRoleId(ROLE_ID2);

        Role organizationRole = new Role();
        organizationRole.setId(ROLE_ID);
        organizationRole.setAssignableType(ReferenceType.ORGANIZATION);
        organizationRole.setPermissionAcls(Permission.of(ORGANIZATION, READ));

        Role domainRole = new Role();
        domainRole.setId(ROLE_ID2);
        domainRole.setAssignableType(ReferenceType.DOMAIN);
        domainRole.setPermissionAcls(Permission.of(DOMAIN, CREATE));

        Domain domain = new Domain();
        domain.setReferenceType(ReferenceType.ENVIRONMENT);
        domain.setReferenceId(ENVIRONMENT_ID);

        Environment environment = new Environment();
        environment.setOrganizationId(ORGANIZATION_ID);

        when(domainService.findById_migrated(eq(DOMAIN_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(domain))));
        when(environmentService.findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(environment))));
        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ORGANIZATION), eq(ORGANIZATION_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(organizationMembership))));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(domainMembership))));
        when(roleService.findByIdIn_migrated(Arrays.asList(organizationMembership.getRoleId(), domainMembership.getRoleId()))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new HashSet<>(Arrays.asList(organizationRole, domainRole))))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(user, and(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, Permission.ORGANIZATION, READ),
                        of(ReferenceType.DOMAIN, DOMAIN_ID, Permission.DOMAIN, READ)))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(false);
    }

    @Test
    public void hasPermission_hasOneOfPermissions() {

        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        Membership organizationMembership = new Membership();
        organizationMembership.setMemberType(MemberType.USER);
        organizationMembership.setMemberId(USER_ID);
        organizationMembership.setReferenceType(ReferenceType.ORGANIZATION);
        organizationMembership.setReferenceId(ORGANIZATION_ID);
        organizationMembership.setRoleId(ROLE_ID);

        Membership domainMembership = new Membership();
        domainMembership.setMemberType(MemberType.USER);
        domainMembership.setMemberId(USER_ID);
        domainMembership.setReferenceType(ReferenceType.DOMAIN);
        domainMembership.setReferenceId(DOMAIN_ID);
        domainMembership.setRoleId(ROLE_ID2);

        Role organizationRole = new Role();
        organizationRole.setId(ROLE_ID);
        organizationRole.setAssignableType(ReferenceType.ORGANIZATION);
        organizationRole.setPermissionAcls(Permission.of(ORGANIZATION, READ));

        Role domainRole = new Role();
        domainRole.setId(ROLE_ID2);
        domainRole.setAssignableType(ReferenceType.DOMAIN);
        domainRole.setPermissionAcls(Permission.of(DOMAIN, CREATE));

        Domain domain = new Domain();
        domain.setReferenceType(ReferenceType.ENVIRONMENT);
        domain.setReferenceId(ENVIRONMENT_ID);

        Environment environment = new Environment();
        environment.setOrganizationId(ORGANIZATION_ID);

        when(domainService.findById_migrated(eq(DOMAIN_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(domain))));
        when(environmentService.findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(environment))));
        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ORGANIZATION), eq(ORGANIZATION_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(organizationMembership))));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(domainMembership))));
        when(roleService.findByIdIn_migrated(Arrays.asList(organizationMembership.getRoleId(), domainMembership.getRoleId()))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new HashSet<>(Arrays.asList(organizationRole, domainRole))))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(user, or(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, Permission.ORGANIZATION, READ),
                        of(ReferenceType.DOMAIN, DOMAIN_ID, Permission.DOMAIN, READ)))).test(); // OR instead of AND

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(true);
    }

    @Test
    public void hasPermission_hasPermissionsOnAnotherReference() {

        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        Membership membership = new Membership();
        membership.setMemberType(MemberType.USER);
        membership.setMemberId(USER_ID);
        membership.setReferenceType(ReferenceType.ORGANIZATION);
        membership.setReferenceId(ORGANIZATION_ID);
        membership.setRoleId(ROLE_ID);

        Role role = new Role();
        role.setId(ROLE_ID);
        role.setAssignableType(ReferenceType.ORGANIZATION);
        role.setPermissionAcls(Permission.of(DOMAIN, READ, CREATE)); // The permission create is set on organization but expected on a domain.

        Domain domain = new Domain();
        domain.setReferenceType(ReferenceType.ENVIRONMENT);
        domain.setReferenceId(ENVIRONMENT_ID);

        Environment environment = new Environment();
        environment.setOrganizationId(ORGANIZATION_ID);

        when(domainService.findById_migrated(eq(DOMAIN_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(domain))));
        when(environmentService.findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(environment))));
        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ORGANIZATION), eq(ORGANIZATION_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(membership))));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(roleService.findByIdIn_migrated(Arrays.asList(membership.getRoleId()))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new HashSet<>(Arrays.asList(role))))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(user, and(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, DOMAIN, READ),
                        of(ReferenceType.DOMAIN, DOMAIN_ID, Permission.DOMAIN, CREATE)))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(false);
    }

    @Test
    public void hasPermission_hasPermissionsButNotAssignableToType() {

        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        Membership membership = new Membership();
        membership.setMemberType(MemberType.USER);
        membership.setMemberId(USER_ID);
        membership.setReferenceType(ReferenceType.APPLICATION);
        membership.setReferenceId(APPLICATION_ID);
        membership.setRoleId(ROLE_ID);

        Role role = new Role();
        role.setId(ROLE_ID);
        role.setAssignableType(ReferenceType.ORGANIZATION);// The role is assignable to organization only by affected to an application.
        role.setPermissionAcls(Permission.of(APPLICATION, READ));

        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.APPLICATION), eq(APPLICATION_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(membership))));
        when(roleService.findByIdIn_migrated(Arrays.asList(membership.getRoleId()))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new HashSet<>(Arrays.asList(role))))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(user, of(ReferenceType.APPLICATION, APPLICATION_ID, APPLICATION, READ)))
                .test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(false);
    }

    @Test
    public void hasPermission_checkNotRelevant() {

        try {
            RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(null, of(ReferenceType.APPLICATION, APPLICATION_ID, ORGANIZATION, READ)));
        } catch (IllegalArgumentException iae) {
            assertTrue(iae.getMessage().contains("not relevant"));
        }
    }

    @Test
    public void hasPermission_aclsFromDifferentGroupAndUser() {

        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        Membership organizationMembership = new Membership();
        organizationMembership.setMemberType(MemberType.USER);
        organizationMembership.setMemberId(USER_ID);
        organizationMembership.setReferenceType(ReferenceType.ORGANIZATION);
        organizationMembership.setReferenceId(ORGANIZATION_ID);
        organizationMembership.setRoleId(ROLE_ID);

        Membership groupMembership = new Membership();
        groupMembership.setMemberType(MemberType.GROUP);
        groupMembership.setMemberId(GROUP_ID);
        groupMembership.setReferenceType(ReferenceType.ORGANIZATION);
        groupMembership.setReferenceId(ORGANIZATION_ID);
        groupMembership.setRoleId(ROLE_ID2);

        Role organizationRole = new Role();
        organizationRole.setId(ROLE_ID);
        organizationRole.setAssignableType(ReferenceType.ORGANIZATION);
        organizationRole.setPermissionAcls(Permission.of(ORGANIZATION, READ)); // READ permission come from role associated to user.

        Role groupRole = new Role();
        groupRole.setId(ROLE_ID2);
        groupRole.setAssignableType(ReferenceType.ORGANIZATION);
        groupRole.setPermissionAcls(Permission.of(ORGANIZATION, CREATE)); // CREATE permission come from role associated to group of the user.

        Group group = new Group();
        group.setId(GROUP_ID);
        group.setReferenceType(ReferenceType.ORGANIZATION);
        group.setReferenceId(ORGANIZATION_ID);
        group.setMembers(Arrays.asList(user.getId()));

        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(group))));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ORGANIZATION), eq(ORGANIZATION_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(organizationMembership, groupMembership))));
        when(roleService.findByIdIn_migrated(Arrays.asList(organizationMembership.getRoleId(), groupMembership.getRoleId()))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new HashSet<>(Arrays.asList(organizationRole, groupRole))))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.hasPermission_migrated(user, of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, Permission.ORGANIZATION, READ, CREATE))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(true);
    }

    @Test
    public void findAllPermission() {

        // Note: findAllPermission is based on same business logic than hasPermissions.
        DefaultUser user = new DefaultUser("user");
        user.setId(USER_ID);

        Membership organizationMembership = new Membership();
        organizationMembership.setMemberType(MemberType.USER);
        organizationMembership.setMemberId(USER_ID);
        organizationMembership.setReferenceType(ReferenceType.ORGANIZATION);
        organizationMembership.setReferenceId(ORGANIZATION_ID);
        organizationMembership.setRoleId(ROLE_ID);

        Membership groupMembership = new Membership();
        groupMembership.setMemberType(MemberType.GROUP);
        groupMembership.setMemberId(GROUP_ID);
        groupMembership.setReferenceType(ReferenceType.ORGANIZATION);
        groupMembership.setReferenceId(ORGANIZATION_ID);
        groupMembership.setRoleId(ROLE_ID2);

        Role organizationRole = new Role();
        organizationRole.setId(ROLE_ID);
        organizationRole.setAssignableType(ReferenceType.ORGANIZATION);
        organizationRole.setPermissionAcls(Permission.of(ORGANIZATION, READ)); // READ permission come from role associated to user.

        Role groupRole = new Role();
        groupRole.setId(ROLE_ID2);
        groupRole.setAssignableType(ReferenceType.ORGANIZATION);
        groupRole.setPermissionAcls(Permission.of(ORGANIZATION, CREATE)); // CREATE permission come from role associated to group of the user.

        Group group = new Group();
        group.setId(GROUP_ID);
        group.setReferenceType(ReferenceType.ORGANIZATION);
        group.setReferenceId(ORGANIZATION_ID);
        group.setMembers(Arrays.asList(user.getId()));

        when(groupService.findByMember_migrated(user.getId())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(group))));
        when(membershipService.findByCriteria_migrated(eq(ReferenceType.ORGANIZATION), eq(ORGANIZATION_ID), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(organizationMembership, groupMembership))));
        when(roleService.findByIdIn_migrated(Arrays.asList(organizationMembership.getRoleId(), groupMembership.getRoleId()))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new HashSet<>(Arrays.asList(organizationRole, groupRole))))));

        TestObserver<Map<Permission, Set<Acl>>> obs = RxJava2Adapter.monoToSingle(cut.findAllPermissions_migrated(user, ReferenceType.ORGANIZATION, ORGANIZATION_ID)).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(permissions -> permissions.get(ORGANIZATION).containsAll(new HashSet<>(Arrays.asList(READ, CREATE))));
    }


    @Test
    public void haveConsistentIds() {

        Application application = new Application();
        application.setDomain(DOMAIN_ID);

        Domain domain = new Domain();
        domain.setReferenceType(ReferenceType.ENVIRONMENT);
        domain.setReferenceId(ENVIRONMENT_ID);

        Environment environment = new Environment();
        environment.setOrganizationId(ORGANIZATION_ID);

        when(applicationService.findById_migrated(eq(APPLICATION_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(application))));
        when(domainService.findById_migrated(eq(DOMAIN_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(domain))));
        when(environmentService.findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(environment))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.haveConsistentReferenceIds_migrated(or(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, APPLICATION, READ),
                of(ReferenceType.ENVIRONMENT, ENVIRONMENT_ID, APPLICATION, READ),
                of(ReferenceType.DOMAIN, DOMAIN_ID, APPLICATION, READ),
                of(ReferenceType.APPLICATION, APPLICATION_ID, APPLICATION, READ)))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(true);
    }

    @Test
    public void haveConsistentIds_applicationIdNotConsistent() {

        Application application = new Application();
        application.setDomain("OTHER_DOMAIN");

        Domain domain = new Domain();
        domain.setReferenceType(ReferenceType.ENVIRONMENT);
        domain.setReferenceId(ENVIRONMENT_ID);

        Environment environment = new Environment();
        environment.setOrganizationId(ORGANIZATION_ID);

        when(applicationService.findById_migrated(eq(APPLICATION_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(application))));
        when(domainService.findById_migrated(eq(DOMAIN_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(domain))));
        when(environmentService.findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(environment))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.haveConsistentReferenceIds_migrated(or(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, APPLICATION, READ),
                of(ReferenceType.ENVIRONMENT, ENVIRONMENT_ID, APPLICATION, READ),
                of(ReferenceType.DOMAIN, DOMAIN_ID, APPLICATION, READ),
                of(ReferenceType.APPLICATION, APPLICATION_ID, APPLICATION, READ)))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(false);
    }


    @Test
    public void haveConsistentIds_domainIdNotConsistent() {

        Application application = new Application();
        application.setDomain(DOMAIN_ID);

        Domain domain = new Domain();
        domain.setReferenceType(ReferenceType.ENVIRONMENT);
        domain.setReferenceId("OTHER_ENVIRONMENT");

        Environment environment = new Environment();
        environment.setOrganizationId(ORGANIZATION_ID);

        when(applicationService.findById_migrated(eq(APPLICATION_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(application))));
        when(domainService.findById_migrated(eq(DOMAIN_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(domain))));
        when(environmentService.findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(environment))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.haveConsistentReferenceIds_migrated(or(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, APPLICATION, READ),
                of(ReferenceType.ENVIRONMENT, ENVIRONMENT_ID, APPLICATION, READ),
                of(ReferenceType.DOMAIN, DOMAIN_ID, APPLICATION, READ),
                of(ReferenceType.APPLICATION, APPLICATION_ID, APPLICATION, READ)))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(false);
    }

    @Test
    public void haveConsistentIds_environmentIdNotConsistent() {

        Application application = new Application();
        application.setDomain(DOMAIN_ID);

        Domain domain = new Domain();
        domain.setReferenceType(ReferenceType.ENVIRONMENT);
        domain.setReferenceId(ENVIRONMENT_ID);

        when(applicationService.findById_migrated(eq(APPLICATION_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(application))));
        when(domainService.findById_migrated(eq(DOMAIN_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(domain))));
        when(environmentService.findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new EnvironmentNotFoundException(ENVIRONMENT_ID)))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.haveConsistentReferenceIds_migrated(or(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, APPLICATION, READ),
                of(ReferenceType.ENVIRONMENT, ENVIRONMENT_ID, APPLICATION, READ),
                of(ReferenceType.DOMAIN, DOMAIN_ID, APPLICATION, READ),
                of(ReferenceType.APPLICATION, APPLICATION_ID, APPLICATION, READ)))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(false);
    }

    @Test
    public void haveConsistentIds_onlyOneReferenceType() {

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.haveConsistentReferenceIds_migrated(of(ReferenceType.APPLICATION, APPLICATION_ID, APPLICATION, READ))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(true);

        verifyZeroInteractions(applicationService);
        verifyZeroInteractions(domainService);
        verifyZeroInteractions(environmentService);
    }

    @Test
    public void haveConsistentIds_cached() {

        Application application = new Application();
        application.setDomain(DOMAIN_ID);

        Domain domain = new Domain();
        domain.setReferenceType(ReferenceType.ENVIRONMENT);
        domain.setReferenceId(ENVIRONMENT_ID);

        Environment environment = new Environment();
        environment.setOrganizationId(ORGANIZATION_ID);

        when(applicationService.findById_migrated(eq(APPLICATION_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(application))));
        when(domainService.findById_migrated(eq(DOMAIN_ID))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(domain))));
        when(environmentService.findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(environment))));

        TestObserver<Boolean> obs = RxJava2Adapter.monoToSingle(cut.haveConsistentReferenceIds_migrated(or(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, APPLICATION, READ),
                of(ReferenceType.ENVIRONMENT, ENVIRONMENT_ID, APPLICATION, READ),
                of(ReferenceType.DOMAIN, DOMAIN_ID, APPLICATION, READ),
                of(ReferenceType.APPLICATION, APPLICATION_ID, APPLICATION, READ)))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(true);

        verify(applicationService, times(1)).findById_migrated(eq(APPLICATION_ID));
        verify(domainService, times(1)).findById_migrated(eq(DOMAIN_ID));
        verify(environmentService, times(1)).findById_migrated(eq(ENVIRONMENT_ID), eq(ORGANIZATION_ID));

        // Second call should hit the cache.
        obs = RxJava2Adapter.monoToSingle(cut.haveConsistentReferenceIds_migrated(or(of(ReferenceType.ORGANIZATION, ORGANIZATION_ID, APPLICATION, READ),
                of(ReferenceType.ENVIRONMENT, ENVIRONMENT_ID, APPLICATION, READ),
                of(ReferenceType.DOMAIN, DOMAIN_ID, APPLICATION, READ),
                of(ReferenceType.APPLICATION, APPLICATION_ID, APPLICATION, READ)))).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(true);

        verifyNoMoreInteractions(applicationService, domainService, environmentService);
    }

}
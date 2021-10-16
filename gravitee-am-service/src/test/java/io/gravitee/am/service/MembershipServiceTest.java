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
package io.gravitee.am.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.model.*;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.model.permissions.SystemRole;
import io.gravitee.am.repository.management.api.MembershipRepository;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.impl.MembershipServiceImpl;
import io.gravitee.am.service.model.NewMembership;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class MembershipServiceTest {

    public static final String ORGANIZATION_ID = "orga#1";
    public static final String DOMAIN_ID = "master-domain";

    @InjectMocks
    private MembershipService membershipService = new MembershipServiceImpl();

    @Mock
    private OrganizationUserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private EventService eventService;

    @Mock
    private GroupService groupService;

    @Mock
    private AuditService auditService;

    @Mock
    private MembershipRepository membershipRepository;

    @Test
    public void shouldCreate_userMembership() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("user-id");
        membership.setMemberType(MemberType.USER);
        membership.setRoleId("role-id");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role role = new Role();
        role.setId("role-id");
        role.setReferenceId(DOMAIN_ID);
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setAssignableType(ReferenceType.DOMAIN);

        when(userService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(user))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        when(membershipRepository.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Membership()))));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void shouldCreate_primaryOwner() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("user-id");
        membership.setMemberType(MemberType.USER);
        membership.setRoleId("role-primary-owner");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role role = new Role();
        role.setId("role-primary-owner");
        role.setName(SystemRole.DOMAIN_PRIMARY_OWNER.name());
        role.setReferenceId(Platform.DEFAULT);
        role.setSystem(true);
        role.setReferenceType(ReferenceType.PLATFORM);
        role.setAssignableType(ReferenceType.DOMAIN);

        when(userService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(user))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        when(membershipRepository.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Membership()))));
        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), argThat(criteria -> criteria.getRoleId().isPresent()))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void shouldCreate_groupMembership() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("group-id");
        membership.setMemberType(MemberType.GROUP);
        membership.setRoleId("role-id");

        Role role = new Role();
        role.setId("role-id");
        role.setReferenceId(DOMAIN_ID);
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setAssignableType(ReferenceType.DOMAIN);

        Group group = new Group();
        group.setReferenceId(DOMAIN_ID);
        group.setReferenceType(ReferenceType.DOMAIN);

        when(groupService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(group))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        when(membershipRepository.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Membership()))));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void shouldNotCreate_memberNotFound() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("user-id");
        membership.setMemberType(MemberType.USER);
        membership.setRoleId("role-id");

        Role role = new Role();
        role.setId("role-id");
        role.setReferenceId(DOMAIN_ID);
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setAssignableType(ReferenceType.DOMAIN);

        when(userService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new UserNotFoundException("user-id")))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(UserNotFoundException.class);

        verify(membershipRepository, never()).create_migrated(any());
    }

    @Test
    public void shouldNotCreate_primaryOwnerAlreadyAssigned() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("user-id");
        membership.setMemberType(MemberType.USER);
        membership.setRoleId("role-primary-owner");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role role = new Role();
        role.setId("role-primary-owner");
        role.setName(SystemRole.DOMAIN_PRIMARY_OWNER.name());
        role.setReferenceId(Platform.DEFAULT);
        role.setSystem(true);
        role.setReferenceType(ReferenceType.PLATFORM);
        role.setAssignableType(ReferenceType.DOMAIN);

        when(userService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(user))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), argThat(criteria -> criteria.getRoleId().isPresent()))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(new Membership()))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(SinglePrimaryOwnerException.class);
    }

    @Test
    public void shouldNotCreate_primaryOwnerCantBeAssignedToGroup() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("group-id");
        membership.setMemberType(MemberType.GROUP);
        membership.setRoleId("role-primary-owner");

        Group group = new Group();
        group.setReferenceId(ORGANIZATION_ID);
        group.setReferenceType(ReferenceType.ORGANIZATION);

        Role role = new Role();
        role.setId("role-primary-owner");
        role.setName(SystemRole.DOMAIN_PRIMARY_OWNER.name());
        role.setReferenceId(Platform.DEFAULT);
        role.setSystem(true);
        role.setReferenceType(ReferenceType.PLATFORM);
        role.setAssignableType(ReferenceType.DOMAIN);

        when(groupService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(group))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(InvalidRoleException.class);
    }

    @Test
    public void shouldNotCreate_groupNotFound() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("group-id");
        membership.setMemberType(MemberType.GROUP);
        membership.setRoleId("role-id");

        Role role = new Role();
        role.setId("role-id");
        role.setReferenceId(DOMAIN_ID);
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setAssignableType(ReferenceType.DOMAIN);

        when(groupService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new GroupNotFoundException("group-id")))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(GroupNotFoundException.class);

        verify(membershipRepository, never()).create_migrated(any());
    }

    @Test
    public void shouldNotCreate_roleNotFound() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("user-id");
        membership.setMemberType(MemberType.USER);
        membership.setRoleId("role-id");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role role = new Role();
        role.setId("role-id");
        role.setReferenceId("master-domain");
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setAssignableType(ReferenceType.DOMAIN);

        when(userService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(user))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(RoleNotFoundException.class);

        verify(membershipRepository, never()).create_migrated(any());
    }

    @Test
    public void shouldNotCreate_invalidRoleScope() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("user-id");
        membership.setMemberType(MemberType.USER);
        membership.setRoleId("role-id");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role role = new Role();
        role.setId("role-id");
        role.setReferenceId(DOMAIN_ID);
        role.setReferenceType(ReferenceType.DOMAIN);
        // Scope application can't be use for domain.
        role.setAssignableType(ReferenceType.APPLICATION);

        when(userService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(user))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidRoleException.class);

        verify(membershipRepository, never()).create_migrated(any());
    }

    @Test
    public void shouldNotCreate_invalidDomain() {

        Membership membership = new Membership();
        membership.setReferenceId(DOMAIN_ID);
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("user-id");
        membership.setMemberType(MemberType.USER);
        membership.setRoleId("role-id");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role role = new Role();
        role.setId("role-id");
        // Role is not on the same domain.
        role.setReferenceId("domain#2");
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setAssignableType(ReferenceType.DOMAIN);

        when(userService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(user))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidRoleException.class);

        verify(membershipRepository, never()).create_migrated(any());
    }

    @Test
    public void shouldNotCreate_invalidOrganization() {

        Membership membership = new Membership();
        membership.setReferenceId("master-domain");
        membership.setReferenceType(ReferenceType.DOMAIN);
        membership.setMemberId("user-id");
        membership.setMemberType(MemberType.USER);
        membership.setRoleId("role-id");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        // Role is not a system and belongs to another organization.
        Role role = new Role();
        role.setId("role-id");
        role.setReferenceId("orga#2");
        role.setReferenceType(ReferenceType.ORGANIZATION);
        role.setAssignableType(ReferenceType.DOMAIN);

        when(userService.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, membership.getMemberId())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(user))));
        when(roleService.findById_migrated(role.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(membershipRepository.findByReferenceAndMember_migrated(membership.getReferenceType(), membership.getReferenceId(), membership.getMemberType(), membership.getMemberId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(ORGANIZATION_ID, membership)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidRoleException.class);

        verify(membershipRepository, never()).create_migrated(any());
    }

    @Test
    public void shouldAddEnvironmentUserRole() {

        NewMembership membership = new NewMembership();
        membership.setMemberType(MemberType.USER);
        membership.setMemberId("user#1");

        DefaultUser principal = new DefaultUser("username");
        principal.setId("user#1");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role environmentUserRole = new Role();
        environmentUserRole.setId("role#1");

        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.ENVIRONMENT), eq("env#1"), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(roleService.findDefaultRole_migrated("orga#1", DefaultRole.ENVIRONMENT_USER, ReferenceType.ENVIRONMENT)).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(environmentUserRole))));
        when(membershipRepository.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Membership()))));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        TestObserver<Void> completable = RxJava2Adapter.monoToCompletable(membershipService.addEnvironmentUserRoleIfNecessary_migrated("orga#1", "env#1", membership, principal)).test();

        completable.awaitTerminalEvent();
        completable.assertNoErrors();
        completable.assertComplete();
    }

    @Test
    public void shouldNotAddEnvironmentUserRole_userAlreadyHasMembership() {

        NewMembership membership = new NewMembership();
        membership.setMemberType(MemberType.USER);
        membership.setMemberId("user#1");

        DefaultUser principal = new DefaultUser("username");
        principal.setId("user#1");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role environmentUserRole = new Role();
        environmentUserRole.setId("role#1");

        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.ENVIRONMENT), eq("env#1"), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(new Membership()))));

        TestObserver<Void> completable = RxJava2Adapter.monoToCompletable(membershipService.addEnvironmentUserRoleIfNecessary_migrated("orga#1", "env#1", membership, principal)).test();

        completable.awaitTerminalEvent();
        completable.assertNoErrors();
        completable.assertComplete();

        verify(membershipRepository, times(0)).create_migrated(any());
        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldAddDomainUserRole() {

        NewMembership membership = new NewMembership();
        membership.setMemberType(MemberType.USER);
        membership.setMemberId("user#1");

        DefaultUser principal = new DefaultUser("username");
        principal.setId("user#1");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role environmentUserRole = new Role();
        environmentUserRole.setId("role#1");

        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.DOMAIN), eq("domain#1"), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(roleService.findDefaultRole_migrated("orga#1", DefaultRole.DOMAIN_USER, ReferenceType.DOMAIN)).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(environmentUserRole))));
        when(membershipRepository.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Membership()))));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));
        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.ENVIRONMENT), eq("env#1"), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(roleService.findDefaultRole_migrated("orga#1", DefaultRole.ENVIRONMENT_USER, ReferenceType.ENVIRONMENT)).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(environmentUserRole))));

        TestObserver<Void> completable = RxJava2Adapter.monoToCompletable(membershipService.addDomainUserRoleIfNecessary_migrated("orga#1", "env#1", "domain#1", membership, principal)).test();

        completable.awaitTerminalEvent();
        completable.assertNoErrors();
        completable.assertComplete();
    }

    @Test
    public void shouldNotAddDomainUserRole_userAlreadyHasMembership() {

        NewMembership membership = new NewMembership();
        membership.setMemberType(MemberType.USER);
        membership.setMemberId("user#1");

        DefaultUser principal = new DefaultUser("username");
        principal.setId("user#1");

        User user = new User();
        user.setReferenceId(ORGANIZATION_ID);
        user.setReferenceType(ReferenceType.ORGANIZATION);

        Role environmentUserRole = new Role();
        environmentUserRole.setId("role#1");

        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.ENVIRONMENT), eq("env#1"), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(new Membership()))));
        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.DOMAIN), eq("domain#1"), any(MembershipCriteria.class))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(new Membership()))));

        TestObserver<Void> completable = RxJava2Adapter.monoToCompletable(membershipService.addDomainUserRoleIfNecessary_migrated("orga#1", "env#1", "domain#1", membership, principal)).test();

        completable.awaitTerminalEvent();
        completable.assertNoErrors();
        completable.assertComplete();

        verify(membershipRepository, times(0)).create_migrated(any());
        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldSetPlatformAdmin() {

        final String userId = "userId";
        final Role platformAdminRole = new Role();
        platformAdminRole.setId("platform-admin");
        when(roleService.findSystemRole_migrated(SystemRole.PLATFORM_ADMIN, ReferenceType.PLATFORM)).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(platformAdminRole))));
        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.PLATFORM), eq(Platform.DEFAULT), argThat(criteria -> criteria != null && criteria.getUserId().get().equals(userId)))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(membershipRepository.create_migrated(any(Membership.class))).thenAnswer((i->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(i.getArgument(0))))));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        final TestObserver<Membership> obs = RxJava2Adapter.monoToSingle(membershipService.setPlatformAdmin_migrated(userId)).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(membership -> {
            assertEquals(MemberType.USER, membership.getMemberType());
            assertEquals(userId, membership.getMemberId());
            assertEquals(ReferenceType.PLATFORM, membership.getReferenceType());
            assertEquals(Platform.DEFAULT, membership.getReferenceId());
            return true;
        });
    }

    @Test
    public void shouldNotSetPlatformAdmin_roleNotFound() {

        final String userId = "userId";
        when(roleService.findSystemRole_migrated(SystemRole.PLATFORM_ADMIN, ReferenceType.PLATFORM)).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        final TestObserver<Membership> obs = RxJava2Adapter.monoToSingle(membershipService.setPlatformAdmin_migrated(userId)).test();

        obs.awaitTerminalEvent();
        obs.assertError(RoleNotFoundException.class);
    }

    @Test
    public void shouldNotSetPlatformAdmin_alreadySet() {

        final String userId = "userId";
        final Membership alreadyExisting = new Membership();
        final Role platformAdminRole = new Role();
        platformAdminRole.setId("platform-admin");
        when(roleService.findSystemRole_migrated(SystemRole.PLATFORM_ADMIN, ReferenceType.PLATFORM)).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(platformAdminRole))));
        when(membershipRepository.findByCriteria_migrated(eq(ReferenceType.PLATFORM), eq(Platform.DEFAULT), argThat(criteria -> criteria != null && criteria.getUserId().get().equals(userId)))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(alreadyExisting))));

        final TestObserver<Membership> obs = RxJava2Adapter.monoToSingle(membershipService.setPlatformAdmin_migrated(userId)).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(alreadyExisting);

        verify(membershipRepository, times(0)).create_migrated(any(Membership.class));
        verifyZeroInteractions(eventService);
        verifyZeroInteractions(auditService);
    }
}

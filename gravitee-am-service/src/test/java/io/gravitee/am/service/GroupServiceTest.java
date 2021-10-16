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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.Group;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.GroupRepository;
import io.gravitee.am.service.exception.GroupAlreadyExistsException;
import io.gravitee.am.service.exception.GroupNotFoundException;
import io.gravitee.am.service.exception.RoleNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.impl.GroupServiceImpl;
import io.gravitee.am.service.model.NewGroup;
import io.gravitee.am.service.model.UpdateGroup;




import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupServiceTest {

    @InjectMocks
    private GroupService groupService = new GroupServiceImpl();

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserService userService;

    @Mock
    private OrganizationUserService organizationUserService;

    @Mock
    private AuditService auditService;

    @Mock
    private RoleService roleService;

    @Mock
    private EventService eventService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(groupRepository.findById_migrated("my-group")).thenReturn(Mono.just(new Group()));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(groupService.findById_migrated("my-group")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_groupNotFound() {
        when(groupRepository.findById_migrated("my-group")).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(groupService.findById_migrated("my-group")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(groupRepository.findById_migrated("my-group")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(groupService.findById_migrated("my-group")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }


    @Test
    public void shouldFindByDomain() {
        when(groupRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(Flux.just(new Group()));
        TestObserver<List<Group>> testObserver = RxJava2Adapter.monoToSingle(groupService.findByDomain_migrated(DOMAIN).collectList()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(groups -> groups.size() == 1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(groupRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestSubscriber testSubscriber = RxJava2Adapter.fluxToFlowable(groupService.findByDomain_migrated(DOMAIN)).test();

        testSubscriber.assertError(TechnicalManagementException.class);
        testSubscriber.assertNotComplete();
    }

    @Test
    public void shouldFindByDomainPagination() {
        Page pagedGroups = new Page(Collections.singleton(new Group()), 1, 1);
        when(groupRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN, 1, 1)).thenReturn(Mono.just(pagedGroups));
        TestObserver<Page<Group>> testObserver = RxJava2Adapter.monoToSingle(groupService.findByDomain_migrated(DOMAIN, 1, 1)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.getData().size() == 1);
    }

    @Test
    public void shouldFindByDomainPagination_technicalException() {
        when(groupRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN, 1, 1)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(groupService.findByDomain_migrated(DOMAIN, 1, 1)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        NewGroup newGroup = Mockito.mock(NewGroup.class);
        Group group = new Group();
        group.setReferenceType(ReferenceType.DOMAIN);
        group.setReferenceId(DOMAIN);

        when(newGroup.getName()).thenReturn("name");
        when(groupRepository.findByName_migrated(ReferenceType.DOMAIN, DOMAIN, newGroup.getName())).thenReturn(Mono.empty());
        when(groupRepository.create_migrated(any(Group.class))).thenReturn(Mono.just(group));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(groupService.create_migrated(DOMAIN, newGroup)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(groupRepository, times(1)).create_migrated(any(Group.class));
    }

    @Test
    public void shouldCreate_technicalException() {
        NewGroup newGroup = Mockito.mock(NewGroup.class);
        when(newGroup.getName()).thenReturn("name");
        when(groupRepository.findByName_migrated(ReferenceType.DOMAIN, DOMAIN, newGroup.getName())).thenReturn(Mono.empty());
        when(groupRepository.create_migrated(any(Group.class))).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(groupService.create_migrated(DOMAIN, newGroup)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate_alreadyExists() {
        NewGroup newGroup = Mockito.mock(NewGroup.class);
        when(newGroup.getName()).thenReturn("names");
        when(groupRepository.findByName_migrated(ReferenceType.DOMAIN, DOMAIN, newGroup.getName())).thenReturn(Mono.just(new Group()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(groupService.create_migrated(DOMAIN, newGroup)).subscribe(testObserver);

        testObserver.assertError(GroupAlreadyExistsException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldUpdate() {
        UpdateGroup updateGroup = Mockito.mock(UpdateGroup.class);
        Group group = new Group();
        group.setReferenceType(ReferenceType.DOMAIN);
        group.setReferenceId(DOMAIN);

        when(updateGroup.getName()).thenReturn("name");
        when(groupRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group")).thenReturn(Mono.just(group));
        when(groupRepository.findByName_migrated(ReferenceType.DOMAIN, DOMAIN, updateGroup.getName())).thenReturn(Mono.empty());
        when(groupRepository.update_migrated(any(Group.class))).thenReturn(Mono.just(group));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(groupService.update_migrated(DOMAIN, "my-updateGroup", updateGroup)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(groupRepository, times(1)).findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group");
        verify(groupRepository, times(1)).update_migrated(any(Group.class));
    }

    @Test
    public void shouldUpdate_technicalException() {
        UpdateGroup updateGroup = Mockito.mock(UpdateGroup.class);
        when(groupRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group")).thenReturn(Mono.just(new Group()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(groupService.update_migrated(DOMAIN, "my-updateGroup", updateGroup)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldUpdate_groupNotFound() {
        UpdateGroup updateGroup = Mockito.mock(UpdateGroup.class);
        when(groupRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group")).thenReturn(Mono.empty());

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(groupService.update_migrated(DOMAIN, "my-updateGroup", updateGroup)).subscribe(testObserver);

        testObserver.assertError(GroupNotFoundException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete() {
        when(groupRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group")).thenReturn(Mono.just(new Group()));
        when(groupRepository.delete_migrated("my-group")).thenReturn(Mono.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(groupService.delete_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(groupRepository, times(1)).delete_migrated("my-group");
    }

    @Test
    public void shouldDelete_technicalException() {
        when(groupRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group")).thenReturn(Mono.just(new Group()));
        when(groupRepository.delete_migrated("my-group")).thenReturn(Mono.error(TechnicalException::new));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToCompletable(groupService.delete_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_groupNotFound() {
        when(groupRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group")).thenReturn(Mono.empty());

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToCompletable(groupService.delete_migrated(ReferenceType.DOMAIN, DOMAIN, "my-group")).subscribe(testObserver);

        testObserver.assertError(GroupNotFoundException.class);
        testObserver.assertNotComplete();

        verify(groupRepository, never()).delete_migrated("my-group");
    }

    @Test
    public void shouldAssignRoles() {
        List<String> rolesIds = Arrays.asList("role-1", "role-2");

        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group-id");

        Set<Role> roles = new HashSet<>();
        Role role1 = new Role();
        role1.setId("role-1");
        Role role2 = new Role();
        role2.setId("role-2");
        roles.add(role1);
        roles.add(role2);

        when(groupRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("group-id"))).thenReturn(Mono.just(group));
        when(roleService.findByIdIn_migrated(rolesIds)).thenReturn(Mono.just(roles));
        when(groupRepository.update_migrated(any())).thenReturn(Mono.just(new Group()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(groupService.assignRoles_migrated(ReferenceType.DOMAIN, DOMAIN, group.getId(), rolesIds)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(groupRepository, times(1)).update_migrated(any());
    }

    @Test
    public void shouldAssignRoles_roleNotFound() {
        List<String> rolesIds = Arrays.asList("role-1", "role-2");

        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group-id");

        Set<Role> roles = new HashSet<>();
        Role role1 = new Role();
        role1.setId("role-1");
        Role role2 = new Role();
        role2.setId("role-2");
        roles.add(role1);
        roles.add(role2);

        when(groupRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("group-id"))).thenReturn(Mono.just(group));
        when(roleService.findByIdIn_migrated(rolesIds)).thenReturn(Mono.just(Collections.emptySet()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(groupService.assignRoles_migrated(ReferenceType.DOMAIN, DOMAIN, group.getId(), rolesIds)).test();
        testObserver.assertNotComplete();
        testObserver.assertError(RoleNotFoundException.class);
        verify(groupRepository, never()).update_migrated(any());
    }

    @Test
    public void shouldRevokeRole() {
        List<String> rolesIds = Arrays.asList("role-1", "role-2");

        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group-id");

        Set<Role> roles = new HashSet<>();
        Role role1 = new Role();
        role1.setId("role-1");
        Role role2 = new Role();
        role2.setId("role-2");
        roles.add(role1);
        roles.add(role2);

        when(groupRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("group-id"))).thenReturn(Mono.just(group));
        when(roleService.findByIdIn_migrated(rolesIds)).thenReturn(Mono.just(roles));
        when(groupRepository.update_migrated(any())).thenReturn(Mono.just(new Group()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(groupService.revokeRoles_migrated(ReferenceType.DOMAIN, DOMAIN, group.getId(), rolesIds)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(groupRepository, times(1)).update_migrated(any());
    }

    @Test
    public void shouldRevokeRoles_roleNotFound() {
        List<String> rolesIds = Arrays.asList("role-1", "role-2");

        Group group = mock(Group.class);
        when(group.getId()).thenReturn("group-id");

        Set<Role> roles = new HashSet<>();
        Role role1 = new Role();
        role1.setId("role-1");
        Role role2 = new Role();
        role2.setId("role-2");
        roles.add(role1);
        roles.add(role2);

        when(groupRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("group-id"))).thenReturn(Mono.just(group));
        when(roleService.findByIdIn_migrated(rolesIds)).thenReturn(Mono.just(Collections.emptySet()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(groupService.revokeRoles_migrated(ReferenceType.DOMAIN, DOMAIN, group.getId(), rolesIds)).test();
        testObserver.assertNotComplete();
        testObserver.assertError(RoleNotFoundException.class);
        verify(groupRepository, never()).update_migrated(any());
    }

    @Test
    public void shouldFindMembersFromDomainUsers() {
        Group group = mock(Group.class);
        when(group.getReferenceType()).thenReturn(ReferenceType.DOMAIN);
        when(group.getMembers()).thenReturn(Arrays.asList("userid"));

        when(groupRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("group-id"))).thenReturn(Mono.just(group));
        when(userService.findByIdIn_migrated(any())).thenReturn(Flux.just(new User()));

        final TestObserver<Page<User>> observer = RxJava2Adapter.monoToSingle(groupService.findMembers_migrated(ReferenceType.DOMAIN, DOMAIN, "group-id", 0, 0)).test();
        observer.awaitTerminalEvent();

        verify(userService).findByIdIn_migrated(any());
        verify(organizationUserService, never()).findByIdIn_migrated(any());
    }

    @Test
    public void shouldFindMembersFromOrganizationUsers() {
        Group group = mock(Group.class);
        when(group.getReferenceType()).thenReturn(ReferenceType.ORGANIZATION);
        when(group.getMembers()).thenReturn(Arrays.asList("userid"));

        when(groupRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("group-id"))).thenReturn(Mono.just(group));
        when(organizationUserService.findByIdIn_migrated(any())).thenReturn(Flux.just(new User()));

        final TestObserver<Page<User>> observer = RxJava2Adapter.monoToSingle(groupService.findMembers_migrated(ReferenceType.DOMAIN, DOMAIN, "group-id", 0, 0)).test();
        observer.awaitTerminalEvent();

        verify(organizationUserService).findByIdIn_migrated(any());
        verify(userService, never()).findByIdIn_migrated(any());
    }
}

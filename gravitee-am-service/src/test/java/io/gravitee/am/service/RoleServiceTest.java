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

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.RoleRepository;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.impl.RoleServiceImpl;
import io.gravitee.am.service.model.NewRole;
import io.gravitee.am.service.model.UpdateRole;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
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
public class RoleServiceTest {

    public static final String ORGANIZATION_ID = "orga#1";
    @InjectMocks
    private RoleService roleService = new RoleServiceImpl();

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private EventService eventService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(roleRepository.findById_migrated("my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(new Role()))));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(roleService.findById_migrated("my-role")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingRole() {
        when(roleRepository.findById_migrated("my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(roleService.findById_migrated("my-role")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(roleRepository.findById_migrated("my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)))));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(roleService.findById_migrated("my-role")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomain() {
        when(roleRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(new Role()))));
        TestObserver<Set<Role>> testObserver = RxJava2Adapter.monoToSingle(roleService.findByDomain_migrated(DOMAIN)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(roles -> roles.size() == 1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(roleRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalManagementException::new)))));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(roleService.findByDomain_migrated(DOMAIN)).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByIdsIn() {
        when(roleRepository.findByIdIn_migrated(Arrays.asList("my-role"))).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(new Role()))));
        TestObserver<Set<Role>> testObserver = RxJava2Adapter.monoToSingle(roleService.findByIdIn_migrated(Arrays.asList("my-role"))).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(roles -> roles.size() == 1);
    }

    @Test
    public void shouldFindByIdsIn_technicalException() {
        when(roleRepository.findByIdIn_migrated(anyList())).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)))));

        TestObserver testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(roleService.findByIdIn_migrated(Arrays.asList("my-role"))).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        NewRole newRole = Mockito.mock(NewRole.class);
        when(roleRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        Role role = new Role();
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setReferenceId("domain#1");
        when(roleRepository.create_migrated(any(Role.class))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(role))));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(roleService.create_migrated(DOMAIN, newRole)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(roleRepository, times(1)).findAll_migrated(ReferenceType.DOMAIN, DOMAIN);
        verify(roleRepository, times(1)).create_migrated(any(Role.class));
    }

    @Test
    public void shouldCreate_technicalException() {
        NewRole newRole = Mockito.mock(NewRole.class);
        when(roleRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)))));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(roleService.create_migrated(DOMAIN, newRole)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(roleRepository, never()).create_migrated(any(Role.class));
    }

    @Test
    public void shouldCreate_uniquenessException() {
        NewRole newRole = Mockito.mock(NewRole.class);
        when(newRole.getName()).thenReturn("existing-role-name");

        Role role = new Role();
        role.setId("existing-role-id");
        role.setName("existing-role-name");
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setReferenceId("domain#1");

        when(roleRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(role))));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(roleService.create_migrated(DOMAIN, newRole)).subscribe(testObserver);

        testObserver.assertError(RoleAlreadyExistsException.class);
        testObserver.assertNotComplete();

        verify(roleRepository, never()).create_migrated(any(Role.class));
    }

    @Test
    public void shouldUpdate() {
        UpdateRole updateRole = Mockito.mock(UpdateRole.class);
        Role role = new Role();
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setReferenceId("domain#1");
        when(roleRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(roleRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(roleRepository.update_migrated(any(Role.class))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(role))));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(roleService.update_migrated(DOMAIN, "my-updateRole", updateRole)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(roleRepository, times(1)).findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role");
        verify(roleRepository, times(1)).findAll_migrated(ReferenceType.DOMAIN, DOMAIN);
        verify(roleRepository, times(1)).update_migrated(any(Role.class));
    }

    @Test
    public void shouldUpdate_defaultRolePermissions() {
        UpdateRole updateRole = new UpdateRole();
        updateRole.setName(DefaultRole.DOMAIN_USER.name());
        updateRole.setPermissions(Permission.flatten(Collections.singletonMap(Permission.DOMAIN, Collections.singleton(Acl.READ))));

        Role role = new Role();
        role.setName(DefaultRole.DOMAIN_USER.name());
        role.setDefaultRole(true); // should be able to update a default role.
        role.setReferenceType(ReferenceType.ORGANIZATION);
        role.setReferenceId(ORGANIZATION_ID);

        when(roleRepository.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, "my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(roleRepository.findAll_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(roleRepository.update_migrated(argThat(r -> r.getPermissionAcls().equals(Permission.unflatten(updateRole.getPermissions()))))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(role))));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(roleService.update_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, "my-role", updateRole, null)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(roleRepository, times(1)).findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, "my-role");
        verify(roleRepository, times(1)).findAll_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID);
        verify(roleRepository, times(1)).update_migrated(any(Role.class));
    }

    @Test
    public void shouldUpdate_technicalException() {
        UpdateRole updateRole = Mockito.mock(UpdateRole.class);
        when(roleRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)))));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(roleService.update_migrated(DOMAIN, "my-updateRole", updateRole)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(roleRepository, never()).findAll_migrated(ReferenceType.DOMAIN, DOMAIN);
        verify(roleRepository, never()).update_migrated(any(Role.class));
    }

    @Test
    public void shouldUpdate_uniquenessException() {
        UpdateRole updateRole = Mockito.mock(UpdateRole.class);
        when(updateRole.getName()).thenReturn("existing-role-name");

        Role role = new Role();
        role.setId("existing-role-id");
        role.setName("existing-role-name");
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setReferenceId("domain#1");

        when(roleRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(new Role()))));
        when(roleRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(role))));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(roleService.update_migrated(DOMAIN, "my-updateRole", updateRole)).subscribe(testObserver);

        testObserver.assertError(RoleAlreadyExistsException.class);
        testObserver.assertNotComplete();

        verify(roleRepository, never()).create_migrated(any(Role.class));
    }

    @Test
    public void shouldUpdate_roleNotFound() {
        UpdateRole updateRole = Mockito.mock(UpdateRole.class);
        when(roleRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(roleService.update_migrated(DOMAIN, "my-updateRole", updateRole)).subscribe(testObserver);

        testObserver.assertError(RoleNotFoundException.class);
        testObserver.assertNotComplete();

        verify(roleRepository, never()).findAll_migrated(ReferenceType.DOMAIN, DOMAIN);
        verify(roleRepository, never()).create_migrated(any(Role.class));
    }

    @Test
    public void shouldNotUpdate_systemRole() {
        UpdateRole updateRole = new UpdateRole();

        Role role = new Role();
        role.setSystem(true);
        role.setReferenceType(ReferenceType.ORGANIZATION);
        role.setReferenceId(ORGANIZATION_ID);

        when(roleRepository.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, "my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(roleService.update_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, "my-role", updateRole, null)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(SystemRoleUpdateException.class);

        verify(roleRepository, times(1)).findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, "my-role");
        verify(roleRepository, never()).findAll_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID);
        verify(roleRepository, never()).update_migrated(any(Role.class));
    }

    @Test
    public void shouldNotUpdate_defaultRoleName() {

        UpdateRole updateRole = new UpdateRole();
        updateRole.setName("new name");

        Role role = new Role();
        role.setId("my-role");
        role.setName(DefaultRole.DOMAIN_USER.name());
        role.setDefaultRole(true);
        role.setReferenceType(ReferenceType.ORGANIZATION);
        role.setReferenceId(ORGANIZATION_ID);

        when(roleRepository.findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, "my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(roleService.update_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, "my-role", updateRole, null)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(DefaultRoleUpdateException.class);

        verify(roleRepository, times(1)).findById_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, "my-role");
        verify(roleRepository, never()).findAll_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID);
        verify(roleRepository, never()).update_migrated(any(Role.class));
    }

    @Test
    public void shouldDelete_notExistingRole() {
        when(roleRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(roleService.delete_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role")).test();

        testObserver.assertError(RoleNotFoundException.class);
        testObserver.assertNotComplete();

        verify(roleRepository, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldDelete_technicalException() {

        when(eventService.create_migrated(any(Event.class))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));
        when(roleRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-role"))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(new Role()))));
        when(roleRepository.delete_migrated(anyString())).thenReturn(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(TechnicalException::new))));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(roleService.delete_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role")).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldNotDelete_systemRole() {
        Role role = Mockito.mock(Role.class);
        when(role.isSystem()).thenReturn(true);
        when(roleRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-role"))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(roleService.delete_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(SystemRoleDeleteException.class);

        verify(roleRepository, never()).delete_migrated("my-role");
    }

    @Test
    public void shouldDelete() {
        Role role = new Role();
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setReferenceId(DOMAIN);
        when(roleRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-role"))).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(role))));
        when(roleRepository.delete_migrated("my-role")).thenReturn(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty())));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(roleService.delete_migrated(ReferenceType.DOMAIN, DOMAIN, "my-role")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(roleRepository, times(1)).delete_migrated("my-role");
    }
}

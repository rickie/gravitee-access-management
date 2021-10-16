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
package io.gravitee.am.management.service.impl.upgrades;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.management.service.impl.upgrades.helpers.MembershipHelper;
import io.gravitee.am.model.*;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.service.*;
import io.gravitee.am.service.model.NewIdentityProvider;
import io.gravitee.am.service.model.PatchOrganization;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultOrganizationUpgraderTest {

    @Mock
    private OrganizationService organizationService;

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private OrganizationUserService userService;

    @Mock
    private MembershipHelper membershipHelper;

    @Mock
    private RoleService roleService;

    @Mock
    private DomainService domainService;

    @Mock
    private Environment environment;

    private DefaultOrganizationUpgrader cut;

    @Before
    public void before() {
        when(environment.getProperty("security.defaultAdmin", boolean.class, true)).thenReturn(true);
        cut = new DefaultOrganizationUpgrader(organizationService, identityProviderService, userService, membershipHelper, roleService, domainService, environment, null);
    }

    @Test
    public void shouldCreateDefaultOrganization() {

        IdentityProvider idp = new IdentityProvider();
        idp.setId("test");

        User adminUser = new User();
        adminUser.setId("admin-id");

        final Organization organization = new Organization();
        when(organizationService.createDefault_migrated()).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(organization))));
        when(identityProviderService.create_migrated(eq(ReferenceType.ORGANIZATION), eq(Organization.DEFAULT), any(NewIdentityProvider.class), isNull())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(idp))));
        when(organizationService.update_migrated(eq(Organization.DEFAULT), any(PatchOrganization.class), isNull())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(organization))));
        when(userService.create_migrated(argThat(user -> !user.isInternal()
                && user.getUsername().equals("admin")
                && user.getSource().equals(idp.getId())
                && user.getReferenceType() == ReferenceType.ORGANIZATION
                && user.getReferenceId().equals(Organization.DEFAULT)))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(adminUser))));
        when(domainService.findById_migrated("admin")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        doNothing().when(membershipHelper).setOrganizationPrimaryOwnerRole(argThat(user -> user.getId().equals(adminUser.getId())));
        when(organizationService.findById_migrated(Organization.DEFAULT)).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(organization))));
        when(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION, Organization.DEFAULT)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));

        assertTrue(cut.upgrade());
        verify(membershipHelper, times(1)).setPlatformAdminRole();
    }

    @Test
    public void shouldNotUpdateIdentityProviderRoleMapper_noInlineIdp() {

        IdentityProvider idp = new IdentityProvider();
        idp.setId("test");
        idp.setType("idpType");

        Organization defaultOrganization = new Organization();
        defaultOrganization.setId(Organization.DEFAULT);
        defaultOrganization.setIdentities(Arrays.asList("test"));

        when(organizationService.findById_migrated(Organization.DEFAULT)).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(defaultOrganization))));
        when(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION, Organization.DEFAULT)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(idp))));

        when(organizationService.createDefault_migrated()).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        assertTrue(cut.upgrade());
        verify(membershipHelper, times(1)).setPlatformAdminRole();
    }

    @Test
    public void shouldNotUpdateIdentityProviderRoleMapper_adminUserRemoved() {

        IdentityProvider idp = new IdentityProvider();
        idp.setId("inlineIdpId");
        idp.setType("inline-am-idp");
        idp.setExternal(false);
        idp.setConfiguration("{}"); // no admin user.
        idp.setRoleMapper(Collections.singletonMap("role1", new String[]{"username=test"}));

        Organization defaultOrganization = new Organization();
        defaultOrganization.setId(Organization.DEFAULT);
        defaultOrganization.setIdentities(Arrays.asList("inlineIdpId"));

        when(organizationService.findById_migrated(Organization.DEFAULT)).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(defaultOrganization))));
        when(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION, Organization.DEFAULT)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(idp))));

        when(organizationService.createDefault_migrated()).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        assertTrue(cut.upgrade());
        verify(membershipHelper, times(1)).setPlatformAdminRole();
    }

    @Test
    public void shouldNotUpdateIdentityProviderRoleMapper_roleMapperIsAlreadySet() {

        IdentityProvider idp = new IdentityProvider();
        idp.setId("inlineIdpId");
        idp.setType("inline-am-idp");
        idp.setExternal(false);
        idp.setConfiguration(DefaultOrganizationUpgrader.DEFAULT_INLINE_IDP_CONFIG);
        idp.setRoleMapper(Collections.singletonMap("role1", new String[]{"username=test"})); // RoleMapper already set.

        final Role adminRole = new Role();
        adminRole.setId("role-id");

        Organization defaultOrganization = new Organization();
        defaultOrganization.setId(Organization.DEFAULT);
        defaultOrganization.setIdentities(Arrays.asList("inlineIdpId"));

        when(organizationService.findById_migrated(Organization.DEFAULT)).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(defaultOrganization))));
        when(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION, Organization.DEFAULT)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(idp))));

        when(organizationService.createDefault_migrated()).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        assertTrue(cut.upgrade());
        verify(membershipHelper, times(1)).setPlatformAdminRole();
    }

    @Test
    public void shouldCreateAdminUser_noAdminUser() {

        IdentityProvider idp = new IdentityProvider();
        idp.setId("inlineIdpId");
        idp.setType("inline-am-idp");
        idp.setExternal(false);
        idp.setConfiguration(DefaultOrganizationUpgrader.DEFAULT_INLINE_IDP_CONFIG);
        idp.setRoleMapper(new HashMap<>());

        User adminUser = new User();
        adminUser.setId("admin-id");

        Organization defaultOrganization = new Organization();
        defaultOrganization.setId(Organization.DEFAULT);
        defaultOrganization.setIdentities(Arrays.asList("inlineIdpId"));

        when(organizationService.findById_migrated(Organization.DEFAULT)).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(defaultOrganization))));
        when(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION, Organization.DEFAULT)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(idp))));
        when(userService.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, Organization.DEFAULT, "admin", idp.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        when(userService.create_migrated(any(User.class))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(adminUser))));
        doNothing().when(membershipHelper).setOrganizationPrimaryOwnerRole(argThat(user -> user.getId().equals(adminUser.getId())));

        when(organizationService.createDefault_migrated()).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        assertTrue(cut.upgrade());
        verify(membershipHelper, times(1)).setPlatformAdminRole();
    }

    @Test
    public void shouldNotUpdateAdminUser_adminAlreadyExist() {

        IdentityProvider idp = new IdentityProvider();
        idp.setId("inlineIdpId");
        idp.setType("inline-am-idp");
        idp.setExternal(false);
        idp.setConfiguration(DefaultOrganizationUpgrader.DEFAULT_INLINE_IDP_CONFIG);
        idp.setRoleMapper(new HashMap<>());

        User adminUser = new User();
        adminUser.setId("adminId");
        adminUser.setUsername("admin");
        adminUser.setLoginsCount(10L);
        adminUser.setRoles(Arrays.asList("role-id"));

        Organization defaultOrganization = new Organization();
        defaultOrganization.setId(Organization.DEFAULT);
        defaultOrganization.setIdentities(Arrays.asList("inlineIdpId"));

        when(organizationService.findById_migrated(Organization.DEFAULT)).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(defaultOrganization))));
        when(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION, Organization.DEFAULT)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(idp))));
        when(userService.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, Organization.DEFAULT, "admin", idp.getId())).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(adminUser)))); // Admin already exists.
        when(organizationService.createDefault_migrated()).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        assertTrue(cut.upgrade());
        verify(membershipHelper, times(1)).setPlatformAdminRole();
    }

    @Test
    public void shouldCreateDefaultOrganization_technicalError() {

        when(organizationService.createDefault_migrated()).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)))));

        assertFalse(cut.upgrade());
    }

    @Test
    public void shouldCreateSystemRoles_setOwnerRoleToExistingUsers() {

        int totalUsers = 22;

        User user = new User();
        user.setId("user-1");

        Role adminRole = new Role();
        adminRole.setId("role-1");

        Organization organization = new Organization();
        organization.setId("orga-id");

        List<User> users = Stream.iterate(0, i -> i++).limit(10).map(i -> user)
                .collect(Collectors.toList());

        when(organizationService.createDefault_migrated()).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(organization))));
        when(organizationService.update_migrated(any(), any(), any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(organization))));
        when(domainService.findById_migrated("admin")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(new Domain()))));
        when(domainService.delete_migrated("admin")).thenReturn(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty())));

        when(roleService.findDefaultRole_migrated(Organization.DEFAULT, DefaultRole.ORGANIZATION_OWNER, ReferenceType.ORGANIZATION))
                .thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(adminRole)))); // Role has been created.

        when(userService.findAll_migrated(eq(ReferenceType.ORGANIZATION), eq(Organization.DEFAULT), eq(0), anyInt()))
                .thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Page<>(users, 0, totalUsers)))));

        when(userService.findAll_migrated(eq(ReferenceType.ORGANIZATION), eq(Organization.DEFAULT), eq(1), anyInt()))
                .thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Page<>(users, 1, totalUsers)))));

        when(userService.findAll_migrated(eq(ReferenceType.ORGANIZATION), eq(Organization.DEFAULT), eq(2), anyInt()))
                .thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Page<>(Arrays.asList(user, user), 2, totalUsers)))));

        doNothing().when(membershipHelper).setOrganizationRole(eq(user), eq(adminRole));

        when(organizationService.findById_migrated(Organization.DEFAULT)).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(organization))));
        when(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION, Organization.DEFAULT)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));

        cut.upgrade();

        verify(membershipHelper, times(totalUsers)).setOrganizationRole(eq(user), eq(adminRole));
        verify(membershipHelper, times(1)).setPlatformAdminRole();
    }
}

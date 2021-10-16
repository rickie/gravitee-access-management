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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.UserProvider;
import io.gravitee.am.jwt.JWTBuilder;
import io.gravitee.am.management.service.impl.UserServiceImpl;
import io.gravitee.am.model.*;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.application.ApplicationSettings;
import io.gravitee.am.service.*;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.gravitee.am.service.validators.PasswordValidator;
import io.gravitee.am.service.validators.UserValidator;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    public static final String DOMAIN_ID = "domain#1";

    @InjectMocks
    private final UserService userService = new UserServiceImpl();

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private IdentityProviderManager identityProviderManager;

    @Mock
    private AuditService auditService;

    @Mock
    private DomainService domainService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private io.gravitee.am.service.UserService commonUserService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private RoleService roleService;

    @Mock
    private JWTBuilder jwtBuilder;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailManager emailManager;

    @Mock
    private MembershipService membershipService;

    @Spy
    private UserValidator userValidator = new UserValidator();

    @Before
    public void setUp() {
        ((UserServiceImpl) userService).setExpireAfter(24 * 3600);
    }

    @Test
    public void shouldCreateUser_invalid_identity_provider() {
        String domainId = "domain";
        String clientId = "clientId";

        Domain domain = new Domain();
        domain.setId(domainId);

        NewUser newUser = new NewUser();
        newUser.setUsername("username");
        newUser.setSource("unknown-idp");
        newUser.setPassword("myPassword");
        newUser.setClient(clientId);

        when(commonUserService.findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(identityProviderManager.getUserProvider_migrated(anyString())).thenReturn(Mono.empty());

        RxJava2Adapter.monoToSingle(userService.create_migrated(domain, newUser, null))
                .test()
                .assertNotComplete()
                .assertError(UserProviderNotFoundException.class);
        verify(commonUserService, never()).create_migrated(any());
    }

    @Test
    public void shouldNotCreateUser_unknown_client() {
        String domainId = "domain";
        String clientId = "clientId";

        Domain domain = new Domain();
        domain.setId(domainId);

        NewUser newUser = new NewUser();
        newUser.setUsername("username");
        newUser.setSource("idp");
        newUser.setClient(clientId);
        newUser.setPassword("myPassword");

        when(identityProviderManager.getUserProvider_migrated(anyString())).thenReturn(Mono.just(mock(UserProvider.class)));
        when(commonUserService.findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(applicationService.findById_migrated(newUser.getClient())).thenReturn(Mono.empty());
        when(applicationService.findByDomainAndClientId_migrated(domainId, newUser.getClient())).thenReturn(Mono.empty());

        RxJava2Adapter.monoToSingle(userService.create_migrated(domain, newUser, null))
                .test()
                .assertNotComplete()
                .assertError(ClientNotFoundException.class);
        verify(commonUserService, never()).create_migrated(any());
    }

    @Test
    public void shouldNotCreateUser_invalid_client() {
        String domainId = "domain";

        Domain domain = new Domain();
        domain.setId(domainId);

        NewUser newUser = new NewUser();
        newUser.setUsername("username");
        newUser.setSource("idp");
        newUser.setClient("client");
        newUser.setPassword("MyPassword");

        Application application = mock(Application.class);
        when(application.getDomain()).thenReturn("other-domain");

        when(commonUserService.findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(identityProviderManager.getUserProvider_migrated(anyString())).thenReturn(Mono.just(mock(UserProvider.class)));
        when(applicationService.findById_migrated(newUser.getClient())).thenReturn(Mono.just(application));

        RxJava2Adapter.monoToSingle(userService.create_migrated(domain, newUser, null))
                .test()
                .assertNotComplete()
                .assertError(ClientNotFoundException.class);
        verify(commonUserService, never()).create_migrated(any());
    }

    @Test
    public void shouldNotCreateUser_user_already_exists() {
        String domainId = "domain";
        String clientId = "clientId";

        Domain domain = new Domain();
        domain.setId(domainId);

        NewUser newUser = new NewUser();
        newUser.setUsername("username");
        newUser.setSource("idp");
        newUser.setPassword("MyPassword");
        newUser.setClient(clientId);

        when(commonUserService.findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString())).thenReturn(Mono.just(new User()));

        RxJava2Adapter.monoToSingle(userService.create_migrated(domain, newUser, null))
                .test()
                .assertNotComplete()
                .assertError(UserAlreadyExistsException.class);
        verify(commonUserService, never()).create_migrated(any());
    }

    @Test
    public void shouldPreRegisterUser() throws InterruptedException {

        String domainId = "domain";

        AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDynamicUserRegistration(false);

        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setAccountSettings(accountSettings);

        NewUser newUser = new NewUser();
        newUser.setUsername("username");
        newUser.setSource("idp");
        newUser.setClient("client");
        newUser.setPreRegistration(true);

        User preRegisteredUser = new User();
        preRegisteredUser.setId("userId");
        preRegisteredUser.setReferenceId("domain");
        preRegisteredUser.setPreRegistration(true);

        UserProvider userProvider = mock(UserProvider.class);
        doReturn(Mono.just(new DefaultUser(newUser.getUsername()))).when(userProvider).create_migrated(any());

        Application client = new Application();
        client.setDomain("domain");
        when(domainService.findById_migrated(domainId)).thenReturn(Mono.just(domain));
        when(commonUserService.findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(identityProviderManager.getUserProvider_migrated(anyString())).thenReturn(Mono.just(userProvider));
        when(applicationService.findById_migrated(newUser.getClient())).thenReturn(Mono.just(client));
        when(commonUserService.create_migrated(any())).thenReturn(Mono.just(preRegisteredUser));
        when(commonUserService.findById_migrated(any(), anyString(), anyString())).thenReturn(Mono.just(preRegisteredUser));

        RxJava2Adapter.monoToSingle(userService.create_migrated(domain, newUser, null))
                .test()
                .assertComplete()
                .assertNoErrors();
        verify(commonUserService, times(1)).create_migrated(any());
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        verify(commonUserService).create_migrated(argument.capture());

        // Wait few ms to let time to background thread to be executed.
        Thread.sleep(500);
        verify(emailService).send(any(Domain.class), eq(null), eq(Template.REGISTRATION_CONFIRMATION), any(User.class));

        Assert.assertNull(argument.getValue().getRegistrationUserUri());
        Assert.assertNull(argument.getValue().getRegistrationAccessToken());
    }

    @Test
    public void shouldPreRegisterUser_dynamicUserRegistration_domainLevel() {

        String domainId = "domain";

        AccountSettings accountSettings;
        accountSettings = new AccountSettings();
        accountSettings.setDynamicUserRegistration(true);

        Domain domain = new Domain();
        domain.setId(domainId);
        domain.setAccountSettings(accountSettings);

        NewUser newUser = new NewUser();
        newUser.setUsername("username");
        newUser.setSource("idp");
        newUser.setClient("client");
        newUser.setPreRegistration(true);

        UserProvider userProvider = mock(UserProvider.class);
        doReturn(Mono.just(new DefaultUser(newUser.getUsername()))).when(userProvider).create_migrated(any());

        Application client = new Application();
        client.setDomain("domain");

        when(jwtBuilder.sign(any())).thenReturn("token");
        when(commonUserService.findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(identityProviderManager.getUserProvider_migrated(anyString())).thenReturn(Mono.just(userProvider));
        when(applicationService.findById_migrated(newUser.getClient())).thenReturn(Mono.just(client));
        when(commonUserService.create_migrated(any())).thenReturn(Mono.just(new User()));
        when(domainService.buildUrl(any(Domain.class), eq("/confirmRegistration"))).thenReturn("http://localhost:8092/test/confirmRegistration");
        when(emailService.getEmailTemplate(eq(Template.REGISTRATION_CONFIRMATION), any())).thenReturn(new Email());

        RxJava2Adapter.monoToSingle(userService.create_migrated(domain, newUser, null))
                .test()
                .assertComplete()
                .assertNoErrors();
        verify(commonUserService, times(1)).create_migrated(any());
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        verify(commonUserService).create_migrated(argument.capture());

        Assert.assertNotNull(argument.getValue().getRegistrationUserUri());
        assertEquals("http://localhost:8092/test/confirmRegistration", argument.getValue().getRegistrationUserUri());

        Assert.assertNotNull(argument.getValue().getRegistrationAccessToken());
        assertEquals("token", argument.getValue().getRegistrationAccessToken());
    }

    @Test
    public void shouldPreRegisterUser_dynamicUserRegistration_clientLevel() {

        String domainId = "domain";

        AccountSettings accountSettings = new AccountSettings();
        accountSettings.setDynamicUserRegistration(true);
        accountSettings.setInherited(false);

        Domain domain = new Domain();
        domain.setId(domainId);

        NewUser newUser = new NewUser();
        newUser.setUsername("username");
        newUser.setSource("idp");
        newUser.setClient("client");
        newUser.setPreRegistration(true);

        UserProvider userProvider = mock(UserProvider.class);
        doReturn(Mono.just(new DefaultUser(newUser.getUsername()))).when(userProvider).create_migrated(any());

        Application client = new Application();
        client.setDomain("domain");

        ApplicationSettings settings = new ApplicationSettings();
        settings.setAccount(accountSettings);
        client.setSettings(settings);

        when(jwtBuilder.sign(any())).thenReturn("token");
        when(commonUserService.findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString())).thenReturn(Mono.empty());
        when(identityProviderManager.getUserProvider_migrated(anyString())).thenReturn(Mono.just(userProvider));
        when(applicationService.findById_migrated(newUser.getClient())).thenReturn(Mono.just(client));
        when(commonUserService.create_migrated(any())).thenReturn(Mono.just(new User()));
        when(domainService.buildUrl(any(Domain.class), eq("/confirmRegistration"))).thenReturn("http://localhost:8092/test/confirmRegistration");
        when(emailService.getEmailTemplate(eq(Template.REGISTRATION_CONFIRMATION), any())).thenReturn(new Email());

        RxJava2Adapter.monoToSingle(userService.create_migrated(domain, newUser, null))
                .test()
                .assertComplete()
                .assertNoErrors();
        verify(commonUserService, times(1)).create_migrated(any());
        ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        verify(commonUserService).create_migrated(argument.capture());

        Assert.assertNotNull(argument.getValue().getRegistrationUserUri());
        assertEquals("http://localhost:8092/test/confirmRegistration", argument.getValue().getRegistrationUserUri());

        Assert.assertNotNull(argument.getValue().getRegistrationAccessToken());
        assertEquals("token", argument.getValue().getRegistrationAccessToken());
    }

    @Test
    public void shouldNotUpdateUser_unknown_client() {
        String domain = "domain";
        String id = "id";

        User user = new User();
        user.setSource("idp");

        UpdateUser updateUser = new UpdateUser();
        updateUser.setClient("client");

        when(commonUserService.findById_migrated(eq(ReferenceType.DOMAIN), eq(domain), eq(id))).thenReturn(Mono.just(user));
        when(identityProviderManager.getUserProvider_migrated(anyString())).thenReturn(Mono.just(mock(UserProvider.class)));
        when(applicationService.findById_migrated(updateUser.getClient())).thenReturn(Mono.empty());
        when(applicationService.findByDomainAndClientId_migrated(domain, updateUser.getClient())).thenReturn(Mono.empty());

        RxJava2Adapter.monoToSingle(userService.update_migrated(domain, id, updateUser))
                .test()
                .assertNotComplete()
                .assertError(ClientNotFoundException.class);
    }

    @Test
    public void shouldNotUpdateUser_invalid_client() {
        String domain = "domain";
        String id = "id";

        User user = new User();
        user.setSource("idp");

        UpdateUser updateUser = new UpdateUser();
        updateUser.setClient("client");

        Application application = new Application();
        application.setDomain("other-domain");

        when(commonUserService.findById_migrated(eq(ReferenceType.DOMAIN), eq(domain), eq(id))).thenReturn(Mono.just(user));
        when(identityProviderManager.getUserProvider_migrated(anyString())).thenReturn(Mono.just(mock(UserProvider.class)));
        when(applicationService.findById_migrated(updateUser.getClient())).thenReturn(Mono.just(application));

        RxJava2Adapter.monoToSingle(userService.update_migrated(domain, id, updateUser))
                .test()
                .assertNotComplete()
                .assertError(ClientNotFoundException.class);
    }

    @Test
    public void shouldResetPassword_externalIdEmpty() {

        Domain domain = new Domain();
        domain.setId("domain");
        String password = "password";

        User user = new User();
        user.setId("user-id");
        user.setSource("idp-id");

        io.gravitee.am.identityprovider.api.User idpUser = mock(DefaultUser.class);
        when(idpUser.getId()).thenReturn("idp-id");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByUsername_migrated(user.getUsername())).thenReturn(Mono.just(idpUser));
        when(userProvider.update_migrated(anyString(), any())).thenReturn(Mono.just(idpUser));

        doReturn(true).when(passwordValidator).isValid(password);
        when(commonUserService.findById_migrated(eq(ReferenceType.DOMAIN), eq(domain.getId()), eq("user-id"))).thenReturn(Mono.just(user));
        when(identityProviderManager.getUserProvider_migrated(user.getSource())).thenReturn(Mono.just(userProvider));
        when(commonUserService.update_migrated(any())).thenReturn(Mono.just(user));
        when(loginAttemptService.reset_migrated(any())).thenReturn(Mono.empty());

        RxJava2Adapter.monoToCompletable(userService.resetPassword_migrated(domain, user.getId(), password, null))
                .test()
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void shouldResetPassword_idpUserNotFound() {
        Domain domain = new Domain();
        domain.setId("domain");
        String password = "password";

        User user = new User();
        user.setId("user-id");
        user.setSource("idp-id");

        io.gravitee.am.identityprovider.api.User idpUser = mock(DefaultUser.class);
        when(idpUser.getId()).thenReturn("idp-id");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByUsername_migrated(user.getUsername())).thenReturn(Mono.empty());
        when(userProvider.create_migrated(any())).thenReturn(Mono.just(idpUser));

        doReturn(true).when(passwordValidator).isValid(password);
        when(commonUserService.findById_migrated(eq(ReferenceType.DOMAIN), eq(domain.getId()), eq("user-id"))).thenReturn(Mono.just(user));
        when(identityProviderManager.getUserProvider_migrated(user.getSource())).thenReturn(Mono.just(userProvider));
        when(commonUserService.update_migrated(any())).thenReturn(Mono.just(user));
        when(loginAttemptService.reset_migrated(any())).thenReturn(Mono.empty());

        RxJava2Adapter.monoToCompletable(userService.resetPassword_migrated(domain, user.getId(), password, null))
                .test()
                .assertComplete()
                .assertNoErrors();
        verify(userProvider, times(1)).create_migrated(any());
    }

    @Test
    public void shouldAssignRoles() {
        List<String> rolesIds = Arrays.asList("role-1", "role-2");

        User user = mock(User.class);
        when(user.getId()).thenReturn("user-id");

        Set<Role> roles = new HashSet<>();
        Role role1 = new Role();
        role1.setId("role-1");
        Role role2 = new Role();
        role2.setId("role-2");
        roles.add(role1);
        roles.add(role2);

        when(commonUserService.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), eq("user-id"))).thenReturn(Mono.just(user));
        when(roleService.findByIdIn_migrated(rolesIds)).thenReturn(Mono.just(roles));
        when(commonUserService.update_migrated(any())).thenReturn(Mono.just(new User()));

        RxJava2Adapter.monoToSingle(userService.assignRoles_migrated(ReferenceType.DOMAIN, DOMAIN_ID, user.getId(), rolesIds))
                .test()
                .assertComplete()
                .assertNoErrors();
        verify(commonUserService, times(1)).update_migrated(any());
    }

    @Test
    public void shouldAssignRoles_roleNotFound() {
        List<String> rolesIds = Arrays.asList("role-1", "role-2");

        User user = new User();
        user.setId("user-id");
        user.setSource("idp-id");

        when(commonUserService.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), eq("user-id"))).thenReturn(Mono.just(user));
        when(identityProviderManager.userProviderExists(user.getSource())).thenReturn(true);
        when(roleService.findByIdIn_migrated(rolesIds)).thenReturn(Mono.just(Collections.emptySet()));

        RxJava2Adapter.monoToSingle(userService.assignRoles_migrated(ReferenceType.DOMAIN, DOMAIN_ID, user.getId(), rolesIds))
                .test()
                .assertNotComplete()
                .assertError(RoleNotFoundException.class);
        verify(commonUserService, never()).update_migrated(any());
    }

    @Test
    public void shouldRevokeRole() {
        List<String> rolesIds = Arrays.asList("role-1", "role-2");

        User user = new User();
        user.setId("user-id");
        user.setSource("idp-id");

        Set<Role> roles = new HashSet<>();
        Role role1 = new Role();
        role1.setId("role-1");
        Role role2 = new Role();
        role2.setId("role-2");
        roles.add(role1);
        roles.add(role2);

        when(commonUserService.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), eq("user-id"))).thenReturn(Mono.just(user));
        when(identityProviderManager.userProviderExists(user.getSource())).thenReturn(true);
        when(roleService.findByIdIn_migrated(rolesIds)).thenReturn(Mono.just(roles));
        when(commonUserService.update_migrated(any())).thenReturn(Mono.just(new User()));

        RxJava2Adapter.monoToSingle(userService.revokeRoles_migrated(ReferenceType.DOMAIN, DOMAIN_ID, user.getId(), rolesIds))
                .test()
                .assertComplete()
                .assertNoErrors();
        verify(commonUserService, times(1)).update_migrated(any());
    }

    @Test
    public void shouldRevokeRoles_roleNotFound() {
        List<String> rolesIds = Arrays.asList("role-1", "role-2");

        User user = new User();
        user.setId("user-id");
        user.setSource("idp-id");

        when(commonUserService.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN_ID), eq("user-id"))).thenReturn(Mono.just(user));
        when(identityProviderManager.userProviderExists(user.getSource())).thenReturn(true);
        when(roleService.findByIdIn_migrated(rolesIds)).thenReturn(Mono.just(Collections.emptySet()));

        RxJava2Adapter.monoToSingle(userService.revokeRoles_migrated(ReferenceType.DOMAIN, DOMAIN_ID, user.getId(), rolesIds))
                .test()
                .assertNotComplete()
                .assertError(RoleNotFoundException.class);
        verify(commonUserService, never()).update_migrated(any());
    }

    @Test
    public void shouldNotCreate_invalid_password() {

        Domain domain = new Domain();
        domain.setId("domainId");
        String password = "myPassword";
        NewUser newUser = new NewUser();
        newUser.setUsername("Username");
        newUser.setSource("source");
        newUser.setPassword(password);

        doReturn(Mono.empty()).when(commonUserService).findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString());
        when(identityProviderManager.getUserProvider_migrated(anyString())).thenReturn(Mono.just(mock(UserProvider.class)));
        RxJava2Adapter.monoToSingle(userService.create_migrated(domain, newUser, null))
                .test()
                .assertNotComplete()
                .assertError(InvalidPasswordException.class);
        verify(passwordValidator, times(1)).isValid(password);
    }

    @Test
    public void shouldNotResetPassword_invalid_password() {
        Domain domain = new Domain();
        domain.setId("domain");
        String password = "password";

        User user = new User();
        user.setId("user-id");
        user.setSource("idp-id");

        when(commonUserService.findById_migrated(eq(ReferenceType.DOMAIN), eq(domain.getId()), eq("user-id"))).thenReturn(Mono.just(user));

        RxJava2Adapter.monoToCompletable(userService.resetPassword_migrated(domain, user.getId(), password, null))
                .test()
                .assertNotComplete()
                .assertError(InvalidPasswordException.class);
        verify(passwordValidator, times(1)).isValid(password);
    }
}

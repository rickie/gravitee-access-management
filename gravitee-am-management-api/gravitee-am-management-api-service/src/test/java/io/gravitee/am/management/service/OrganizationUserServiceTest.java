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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.identityprovider.api.UserProvider;
import io.gravitee.am.management.service.impl.OrganizationUserServiceImpl;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.Organization;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.MembershipService;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.validators.PasswordValidator;
import io.gravitee.am.service.validators.UserValidator;




import io.reactivex.observers.TestObserver;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class OrganizationUserServiceTest {

    @InjectMocks
    private final OrganizationUserService organizationUserService = new OrganizationUserServiceImpl();

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private IdentityProviderManager identityProviderManager;

    @Mock
    private AuditService auditService;

    @Mock
    private io.gravitee.am.service.OrganizationUserService commonUserService;

    @Mock
    private MembershipService membershipService;

    @Spy
    private UserValidator userValidator = new UserValidator();

    @Test
    public void shouldDeleteUser_without_membership() {
        String organization = "DEFAULT";
        String userId = "user-id";

        User user = new User();
        user.setId(userId);
        user.setSource("source-idp");

        when(commonUserService.findById_migrated(any(), any(), any())).thenReturn(Mono.just(user));
        when(identityProviderManager.getUserProvider_migrated(any())).thenReturn(Mono.empty());
        when(commonUserService.delete_migrated(anyString())).thenReturn(Mono.empty());
        when(membershipService.findByMember_migrated(any(), any())).thenReturn(Flux.empty());

        RxJava2Adapter.monoToCompletable(organizationUserService.delete_migrated(ReferenceType.ORGANIZATION, organization, userId))
                .test()
                .assertComplete()
                .assertNoErrors();
        verify(commonUserService, times(1)).delete_migrated(any());
        verify(membershipService, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldDeleteUser_with_memberships() {
        String organization = "DEFAULT";
        String userId = "user-id";

        User user = new User();
        user.setId(userId);
        user.setSource("source-idp");

        Membership m1 = mock(Membership.class);
        when(m1.getId()).thenReturn("m1");
        Membership m2 = mock(Membership.class);
        when(m2.getId()).thenReturn("m2");
        Membership m3 = mock(Membership.class);
        when(m3.getId()).thenReturn("m3");

        when(commonUserService.findById_migrated(any(), any(), any())).thenReturn(Mono.just(user));
        when(identityProviderManager.getUserProvider_migrated(any())).thenReturn(Mono.empty());
        when(commonUserService.delete_migrated(anyString())).thenReturn(Mono.empty());
        when(membershipService.findByMember_migrated(any(), any())).thenReturn(Flux.just(m1, m2, m3));
        when(membershipService.delete_migrated(anyString())).thenReturn(Mono.empty());

        RxJava2Adapter.monoToCompletable(organizationUserService.delete_migrated(ReferenceType.ORGANIZATION, organization, userId))
                .test()
                .assertComplete()
                .assertNoErrors();
        verify(commonUserService, times(1)).delete_migrated(any());
        verify(membershipService, times(3)).delete_migrated(anyString());
    }

    private NewUser newOrganizationUser() {
        NewUser newUser = new NewUser();
        newUser.setUsername("userid");
        newUser.setFirstName("userid");
        newUser.setLastName("userid");
        newUser.setEmail("userid");
        newUser.setPassword("Test123!");
        newUser.setSource("gravitee");
        return newUser;
    }

    @Test
    public void shouldCreateOrganizationUser() {
        NewUser newUser = newOrganizationUser();
        newUser.setSource("gravitee");
        newUser.setPassword("Test123!");
        newUser.setEmail("email@acme.fr");
        when(passwordValidator.isValid(any())).thenReturn(true);
        when(commonUserService.findByUsernameAndSource_migrated(any(), any(), anyString(), anyString())).thenReturn(Mono.empty());
        final UserProvider provider = mock(UserProvider.class);
        when(identityProviderManager.getUserProvider_migrated(any())).thenReturn(Mono.just(provider));
        when(provider.create_migrated(any())).thenReturn(Mono.just(mock(io.gravitee.am.identityprovider.api.User.class)));

        doReturn(Mono.empty()).when(userValidator).validate_migrated(any());
        when(commonUserService.create_migrated(any())).thenReturn(Mono.just(mock(User.class)));
        when(commonUserService.setRoles_migrated(any())).thenReturn(Mono.empty());

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(organizationUserService.createGraviteeUser_migrated(new Organization(), newUser, null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();

        verify(provider).create_migrated(any());
        verify(commonUserService).create_migrated(argThat(user -> user.getPassword() != null));
        verify(commonUserService).setRoles_migrated(any());
    }

    @Test
    public void shouldNotCreateOrganizationUser_NotGraviteeSource() {
        NewUser newUser = newOrganizationUser();
        newUser.setSource("toto");
        newUser.setPassword("Test123!");
        newUser.setEmail("email@acme.fr");

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(organizationUserService.createGraviteeUser_migrated(new Organization(), newUser, null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(UserInvalidException.class);

        verify(commonUserService, never()).create_migrated(argThat(user -> user.getPassword() == null));
        verify(commonUserService, never()).setRoles_migrated(any());
    }

    @Test
    public void shouldNotCreateOrganizationUser_UserAlreadyExist() {
        NewUser newUser = newOrganizationUser();

        Organization organization = new Organization();
        organization.setId("orgaid");

        when(commonUserService.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, organization.getId(), newUser.getUsername(), newUser.getSource())).thenReturn(Mono.just(new User()));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(organizationUserService.createGraviteeUser_migrated(organization, newUser, null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(UserAlreadyExistsException.class);
    }

    @Test
    public void shouldNotCreateOrganizationUser_UnknownProvider() {
        NewUser newUser = newOrganizationUser();

        Organization organization = new Organization();
        organization.setId("orgaid");

        when(commonUserService.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, organization.getId(), newUser.getUsername(), newUser.getSource())).thenReturn(Mono.empty());
        when(identityProviderManager.getUserProvider_migrated(newUser.getSource())).thenReturn(Mono.empty());

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(organizationUserService.createGraviteeUser_migrated(organization, newUser, null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(UserProviderNotFoundException.class);
    }

    @Test
    public void shouldNotCreateOrganizationUser_noPassword() {
        NewUser newUser = newOrganizationUser();
        newUser.setPassword(null);

        when(commonUserService.findByUsernameAndSource_migrated(any(), any(),  any(),  any())).thenReturn(Mono.empty());
        when(identityProviderManager.getUserProvider_migrated(any())).thenReturn(Mono.just(mock(UserProvider.class)));
        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(organizationUserService.createGraviteeUser_migrated(new Organization(), newUser, null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(InvalidPasswordException.class);
    }

    @Test
    public void shouldUpdateUser_byExternalId() {

        NewUser newUser = new NewUser();
        newUser.setExternalId("user#1");
        newUser.setSource("source");
        newUser.setUsername("Username");
        newUser.setFirstName("Firstname");
        newUser.setLastName("Lastname");
        newUser.setEmail("email@gravitee.io");

        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("info1", "value1");
        additionalInformation.put("info2", "value2");
        additionalInformation.put(StandardClaims.PICTURE, "https://gravitee.io/my-picture");
        newUser.setAdditionalInformation(additionalInformation);

        User user = new User();
        user.setId("user#1");
        when(commonUserService.findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, "orga#1", newUser.getExternalId(), newUser.getSource())).thenReturn(Mono.just(user));
        when(commonUserService.update_migrated(any(User.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestObserver<User> obs = RxJava2Adapter.monoToSingle(organizationUserService.createOrUpdate_migrated(ReferenceType.ORGANIZATION, "orga#1", newUser)).test();

        obs.awaitTerminalEvent();
        obs.assertNoErrors();
        obs.assertValue(updatedUser -> {
            assertEquals(updatedUser.getId(), user.getId());
            assertEquals(updatedUser.getFirstName(), newUser.getFirstName());
            assertEquals(updatedUser.getLastName(), newUser.getLastName());
            assertEquals(updatedUser.getEmail(), newUser.getEmail());
            assertEquals(updatedUser.getAdditionalInformation(), newUser.getAdditionalInformation());
            assertEquals(updatedUser.getPicture(), newUser.getAdditionalInformation().get(StandardClaims.PICTURE));

            return true;
        });
    }

    @Test
    public void shouldUpdateUser_byUsername() {

        NewUser newUser = new NewUser();
        newUser.setExternalId("user#1");
        newUser.setSource("source");
        newUser.setUsername("Username");
        newUser.setFirstName("Firstname");
        newUser.setLastName("Lastname");
        newUser.setEmail("email@gravitee.io");

        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("info1", "value1");
        additionalInformation.put("info2", "value2");
        additionalInformation.put(StandardClaims.PICTURE, "https://gravitee.io/my-picture");
        newUser.setAdditionalInformation(additionalInformation);

        User user = new User();
        user.setId("user#1");
        when(commonUserService.findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, "orga#1", newUser.getExternalId(), newUser.getSource())).thenReturn(Mono.empty());
        when(commonUserService.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, "orga#1", newUser.getUsername(), newUser.getSource())).thenReturn(Mono.just(user));
        when(commonUserService.update_migrated(any(User.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestObserver<User> obs = RxJava2Adapter.monoToSingle(organizationUserService.createOrUpdate_migrated(ReferenceType.ORGANIZATION, "orga#1", newUser)).test();

        obs.awaitTerminalEvent();
        obs.assertNoErrors();
        obs.assertValue(updatedUser -> {
            assertEquals(updatedUser.getId(), user.getId());
            assertEquals(updatedUser.getFirstName(), newUser.getFirstName());
            assertEquals(updatedUser.getLastName(), newUser.getLastName());
            assertEquals(updatedUser.getEmail(), newUser.getEmail());
            assertEquals(updatedUser.getAdditionalInformation(), newUser.getAdditionalInformation());
            assertEquals(updatedUser.getPicture(), newUser.getAdditionalInformation().get(StandardClaims.PICTURE));

            return true;
        });
    }

    @Test
    public void shouldCreateUser() {

        NewUser newUser = new NewUser();
        newUser.setExternalId("user#1");
        newUser.setSource("source");
        newUser.setUsername("Username");
        newUser.setFirstName("Firstname");
        newUser.setLastName("Lastname");
        newUser.setEmail("email@gravitee.io");

        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("info1", "value1");
        additionalInformation.put("info2", "value2");
        additionalInformation.put(StandardClaims.PICTURE, "https://gravitee.io/my-picture");
        newUser.setAdditionalInformation(additionalInformation);

        when(commonUserService.findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, "orga#1", newUser.getExternalId(), newUser.getSource())).thenReturn(Mono.empty());
        when(commonUserService.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, "orga#1", newUser.getUsername(), newUser.getSource())).thenReturn(Mono.empty());
        when(commonUserService.create_migrated(any(User.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestObserver<User> obs = RxJava2Adapter.monoToSingle(organizationUserService.createOrUpdate_migrated(ReferenceType.ORGANIZATION, "orga#1", newUser)).test();

        obs.awaitTerminalEvent();
        obs.assertNoErrors();
        obs.assertValue(updatedUser -> {
            assertNotNull(updatedUser.getId());
            assertEquals(updatedUser.getFirstName(), newUser.getFirstName());
            assertEquals(updatedUser.getLastName(), newUser.getLastName());
            assertEquals(updatedUser.getEmail(), newUser.getEmail());
            assertEquals(updatedUser.getAdditionalInformation(), newUser.getAdditionalInformation());
            assertEquals(updatedUser.getPicture(), newUser.getAdditionalInformation().get(StandardClaims.PICTURE));

            return true;
        });
    }

    @Test
    public void shouldNotResetPassword_MissingPwd() {
        final TestObserver<Void> testObserver = RxJava2Adapter.monoToCompletable(organizationUserService.resetPassword_migrated("org#1", new User(), null, null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(InvalidPasswordException.class);
    }

    @Test
    public void shouldNotResetPassword_invalidPwd() {
        when(passwordValidator.isValid(anyString())).thenReturn(false);
        final TestObserver<Void> testObserver = RxJava2Adapter.monoToCompletable(organizationUserService.resetPassword_migrated("org#1", new User(), "simple", null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(InvalidPasswordException.class);
    }

    @Test
    public void shouldResetPassword() {
        when(passwordValidator.isValid(anyString())).thenReturn(true);

        final User user = new User();
        user.setUsername("username");
        user.setSource("gravitee");

        when(commonUserService.update_migrated(any())).thenReturn(Mono.just(user));

        final TestObserver<Void> testObserver = RxJava2Adapter.monoToCompletable(organizationUserService.resetPassword_migrated("org#1", user, "Test123!", null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();

        verify(commonUserService).update_migrated(any());
    }

    @Test
    public void shouldNotResetPassword_InvalidSource() {
        when(passwordValidator.isValid(anyString())).thenReturn(true);

        final User user = new User();
        user.setUsername("username");
        user.setSource("invalid");

        final TestObserver<Void> testObserver = RxJava2Adapter.monoToCompletable(organizationUserService.resetPassword_migrated("org#1", user, "Test123!", null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(InvalidUserException.class);

        verify(commonUserService, never()).update_migrated(any());
    }

}

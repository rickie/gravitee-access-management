/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.gateway.handler.root.service.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.audit.Status;
import io.gravitee.am.common.exception.authentication.AccountInactiveException;
import io.gravitee.am.gateway.handler.common.auth.idp.IdentityProviderManager;
import io.gravitee.am.gateway.handler.common.email.EmailService;
import io.gravitee.am.gateway.handler.root.service.user.impl.UserServiceImpl;
import io.gravitee.am.gateway.handler.root.service.user.model.ForgotPasswordParameters;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.UserProvider;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.EnrollmentSettings;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.MFASettings;
import io.gravitee.am.model.PasswordHistory;
import io.gravitee.am.model.User;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.idp.ApplicationIdentityProvider;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.reporter.api.audit.model.Audit;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.CredentialService;
import io.gravitee.am.service.LoginAttemptService;
import io.gravitee.am.service.TokenService;
import io.gravitee.am.service.exception.EnforceUserIdentityException;
import io.gravitee.am.service.exception.PasswordHistoryException;
import io.gravitee.am.service.exception.UserInvalidException;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.gravitee.am.service.impl.PasswordHistoryService;
import io.gravitee.am.service.validators.email.EmailValidator;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@SuppressWarnings("ALL")
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks private UserService userService = new UserServiceImpl();

    @Mock private Domain domain;

    @Mock private EmailValidator emailValidator;

    @Mock private EmailService emailService;

    @Mock private IdentityProviderManager identityProviderManager;

    @Mock private io.gravitee.am.gateway.handler.common.user.UserService commonUserService;

    @Mock private LoginAttemptService loginAttemptService;

    @Mock private CredentialService credentialService;

    @Mock private TokenService tokenService;

    @Mock private AuditService auditService;

    @Mock private PasswordHistoryService passwordHistoryService;

    @Before
    public void before() {
        doReturn(true).when(emailValidator).validate(anyString());
        when(passwordHistoryService.addPasswordToHistory(any(), any(), any(), any(), any(), any()))
                .thenReturn(Maybe.just(new PasswordHistory()));
    }

    @Test
    public void resetShouldReturnErrorWhenPasswordAlreadyInHistory() {
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.isInactive()).thenReturn(true);
        when(user.getSource()).thenReturn("default-idp");

        io.gravitee.am.identityprovider.api.User idpUser =
                mock(io.gravitee.am.identityprovider.api.DefaultUser.class);

        AccountSettings accountSettings = mock(AccountSettings.class);
        when(accountSettings.isCompleteRegistrationWhenResetPassword()).thenReturn(true);

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByUsername(user.getUsername())).thenReturn(Maybe.just(idpUser));

        when(domain.getAccountSettings()).thenReturn(accountSettings);
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(passwordHistoryService.addPasswordToHistory(any(), any(), any(), any(), any(), any()))
                .thenReturn(Maybe.error(PasswordHistoryException::passwordAlreadyInHistory));

        var testObserver = userService.resetPassword(mock(Client.class), user).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(PasswordHistoryException.class);
    }

    @Test
    public void shouldNotResetPassword_userInactive() {
        Client client = mock(Client.class);
        User user = mock(User.class);

        when(user.isInactive()).thenReturn(true);

        var testObserver = userService.resetPassword(client, user).test();
        testObserver.assertNotComplete();
        testObserver.assertError(AccountInactiveException.class);
    }

    @Test
    public void shouldResetPassword_userInactive_forceRegistration() {
        Client client = mock(Client.class);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.isInactive()).thenReturn(true);
        when(user.getSource()).thenReturn("default-idp");

        io.gravitee.am.identityprovider.api.User idpUser =
                mock(io.gravitee.am.identityprovider.api.DefaultUser.class);
        when(idpUser.getId()).thenReturn("idp-id");

        AccountSettings accountSettings = mock(AccountSettings.class);
        when(accountSettings.isCompleteRegistrationWhenResetPassword()).thenReturn(true);

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByUsername(user.getUsername())).thenReturn(Maybe.just(idpUser));
        when(userProvider.updatePassword(any(), any())).thenReturn(Single.just(idpUser));

        when(domain.getAccountSettings()).thenReturn(accountSettings);
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(commonUserService.update(any())).thenReturn(Single.just(user));
        when(commonUserService.enhance(any())).thenReturn(Single.just(user));
        when(loginAttemptService.reset(any())).thenReturn(Completable.complete());

        var testObserver = userService.resetPassword(client, user).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(credentialService, never()).deleteByUserId(any(), any(), any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldResetPassword_userActive() {
        Client client = mock(Client.class);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.isInactive()).thenReturn(false);
        when(user.getSource()).thenReturn("default-idp");

        io.gravitee.am.identityprovider.api.User idpUser =
                mock(io.gravitee.am.identityprovider.api.DefaultUser.class);
        when(idpUser.getId()).thenReturn("idp-id");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByUsername(user.getUsername())).thenReturn(Maybe.just(idpUser));
        when(userProvider.updatePassword(any(), any())).thenReturn(Single.just(idpUser));

        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(commonUserService.update(any())).thenReturn(Single.just(user));
        when(commonUserService.enhance(any())).thenReturn(Single.just(user));
        when(loginAttemptService.reset(any())).thenReturn(Completable.complete());

        var testObserver = userService.resetPassword(client, user).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(credentialService, never()).deleteByUserId(any(), any(), any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldResetPassword_externalIdEmpty() {
        Client client = mock(Client.class);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.isInactive()).thenReturn(false);
        when(user.getSource()).thenReturn("default-idp");

        io.gravitee.am.identityprovider.api.User idpUser =
                mock(io.gravitee.am.identityprovider.api.DefaultUser.class);
        when(idpUser.getId()).thenReturn("idp-id");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByUsername(user.getUsername())).thenReturn(Maybe.just(idpUser));
        when(userProvider.updatePassword(any(), any())).thenReturn(Single.just(idpUser));

        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(commonUserService.update(any())).thenReturn(Single.just(user));
        when(commonUserService.enhance(any())).thenReturn(Single.just(user));
        when(loginAttemptService.reset(any())).thenReturn(Completable.complete());

        var testObserver = userService.resetPassword(client, user).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(credentialService, never()).deleteByUserId(any(), any(), any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldResetPassword_idpUserNotFound() {
        AccountSettings accountSettings = mock(AccountSettings.class);
        when(accountSettings.isInherited()).thenReturn(false);
        when(accountSettings.isCompleteRegistrationWhenResetPassword()).thenReturn(true);
        Client client = mock(Client.class);
        when(client.getAccountSettings()).thenReturn(accountSettings);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.isInactive()).thenReturn(false);
        when(user.getSource()).thenReturn("default-idp");

        io.gravitee.am.identityprovider.api.User idpUser =
                mock(io.gravitee.am.identityprovider.api.DefaultUser.class);
        when(idpUser.getId()).thenReturn("idp-id");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByUsername(user.getUsername())).thenReturn(Maybe.empty());
        when(userProvider.create(any())).thenReturn(Single.just(idpUser));

        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(commonUserService.update(any())).thenReturn(Single.just(user));
        when(commonUserService.enhance(any())).thenReturn(Single.just(user));
        when(loginAttemptService.reset(any())).thenReturn(Completable.complete());

        var testObserver = userService.resetPassword(client, user).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(userProvider, times(1)).create(any());
        verify(credentialService, never()).deleteByUserId(any(), any(), any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldNotForgotPassword_userInactive() {
        when(domain.getId()).thenReturn("domain-id");

        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(null);

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.isInactive()).thenReturn(true);
        when(user.getSource()).thenReturn("idp-id");

        UserProvider userProvider = mock(UserProvider.class);

        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertNotComplete();
        testObserver.assertError(AccountInactiveException.class);

        verify(credentialService, never()).deleteByUserId(any(), any(), any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldNotForgotPassword_wrongIdp() {
        when(domain.getId()).thenReturn("domain-id");

        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(null);

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.getSource()).thenReturn("idp-id");

        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));
        when(identityProviderManager.getUserProvider(user.getSource())).thenReturn(Maybe.empty());

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertNotComplete();
        testObserver.assertError(UserInvalidException.class);
    }

    @Test
    public void shouldForgotPassword_userInactive_forceRegistration() {
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(null);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.isInactive()).thenReturn(true);
        when(user.getSource()).thenReturn("idp-id");
        when(user.isEnabled()).thenReturn(true);

        UserProvider userProvider = mock(UserProvider.class);

        AccountSettings accountSettings = mock(AccountSettings.class);
        when(accountSettings.isCompleteRegistrationWhenResetPassword()).thenReturn(true);

        when(domain.getId()).thenReturn("domain-id");
        when(domain.getAccountSettings()).thenReturn(accountSettings);

        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(userProvider.findByUsername("username"))
                .thenReturn(Maybe.just(new DefaultUser("username")));
        when(commonUserService.update(any())).thenReturn(Single.just(user));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(credentialService, never()).deleteByUserId(any(), any(), any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldForgotPassword_userActive() {
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(null);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.isInactive()).thenReturn(false);
        when(user.getSource()).thenReturn("idp-id");
        when(user.isEnabled()).thenReturn(true);

        UserProvider userProvider = mock(UserProvider.class);

        when(domain.getId()).thenReturn("domain-id");
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(userProvider.findByUsername("username"))
                .thenReturn(Maybe.just(new DefaultUser("username")));
        when(commonUserService.update(any())).thenReturn(Single.just(user));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldForgotPassword_userActive_noUpdate() {
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(null);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.isInactive()).thenReturn(false);
        when(user.getSource()).thenReturn("idp-id");
        when(user.isEnabled()).thenReturn(true);

        UserProvider userProvider = mock(UserProvider.class);

        when(domain.getId()).thenReturn("domain-id");
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(userProvider.findByUsername("username")).thenReturn(Maybe.empty());

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(commonUserService, never()).update(any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldForgotPassword_MultipleMatch_NoMultiFieldForm() {
        Client client = mock(Client.class);
        when(client.getId()).thenReturn("client-id");
        when(client.getIdentityProviders()).thenReturn(null);
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.isInactive()).thenReturn(false);
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.isEnabled()).thenReturn(true);

        UserProvider userProvider = mock(UserProvider.class);

        when(domain.getId()).thenReturn("domain-id");
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Arrays.asList(user, user)));
        when(commonUserService.update(any())).thenReturn(Single.just(user));
        when(userProvider.findByUsername("username"))
                .thenReturn(Maybe.just(new DefaultUser("username")));

        var testObserver =
                userService
                        .forgotPassword(
                                new ForgotPasswordParameters(user.getEmail(), false, false),
                                client,
                                mock(io.gravitee.am.identityprovider.api.User.class))
                        .test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldNotForgotPassword_MultipleMatch_ConfirmIdentityForm() {
        Client client = mock(Client.class);
        when(client.getId()).thenReturn("client-id");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");

        when(domain.getId()).thenReturn("domain-id");
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Arrays.asList(user, user)));

        var testObserver =
                userService
                        .forgotPassword(
                                new ForgotPasswordParameters(user.getEmail(), true, true),
                                client,
                                mock(io.gravitee.am.identityprovider.api.User.class))
                        .test();

        testObserver.assertNotComplete();
        testObserver.assertError(EnforceUserIdentityException.class);
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldForgotPassword_MultipleMatch_onlyOne_filtered() throws Exception {
        Client client = mock(Client.class);
        when(client.getId()).thenReturn("client-id");
        final String localClientId = "idp-client-id";
        when(client.getIdentityProviders())
                .thenReturn(getApplicationIdentityProviders(localClientId));
        final IdentityProvider provider = new IdentityProvider();
        provider.setId(localClientId);
        when(identityProviderManager.getIdentityProvider(localClientId)).thenReturn(provider);

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.getSource()).thenReturn(localClientId);
        User user2 = mock(User.class);
        when(user2.getSource()).thenReturn("other-idp-client-id");
        when(user.isEnabled()).thenReturn(true);

        when(domain.getId()).thenReturn("domain-id");
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Arrays.asList(user, user2)));

        UserProvider userProvider = mock(UserProvider.class);
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(userProvider.findByUsername(any()))
                .thenReturn(Maybe.just(new DefaultUser("username")));
        when(commonUserService.update(any())).thenReturn(Single.just(user));

        TestObserver testObserver =
                userService
                        .forgotPassword(
                                new ForgotPasswordParameters(user.getEmail(), true, true),
                                client,
                                mock(io.gravitee.am.identityprovider.api.User.class))
                        .test();

        // wait for the email service execution
        Thread.sleep(1000);

        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        verify(tokenService, never()).deleteByUserId(any());
        verify(emailService).send(any(), any(), any());
        verify(auditService)
                .report(
                        argThat(
                                builder -> {
                                    final Audit audit = builder.build(new ObjectMapper());
                                    return audit.getType()
                                                    .equals(EventType.FORGOT_PASSWORD_REQUESTED)
                                            && audit.getOutcome()
                                                    .getStatus()
                                                    .equals(Status.SUCCESS);
                                }));
    }

    @Test
    public void shouldForgotPassword_MultipleMatch_OneToExclude_ConfirmIdentityForm()
            throws Exception {
        Client client = mock(Client.class);
        when(client.getId()).thenReturn("client-id");
        when(client.getIdentityProviders())
                .thenReturn(getApplicationIdentityProviders("idp-client-id"));

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.getSource()).thenReturn("idp-client-id");
        User user2 = mock(User.class);
        when(user2.getSource()).thenReturn("other-idp-client-id");

        when(domain.getId()).thenReturn("domain-id");
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Arrays.asList(user, user2, user)));

        TestObserver testObserver =
                userService
                        .forgotPassword(
                                new ForgotPasswordParameters(user.getEmail(), true, true),
                                client,
                                mock(io.gravitee.am.identityprovider.api.User.class))
                        .test();

        // wait for the email service execution
        Thread.sleep(1000);

        testObserver.awaitTerminalEvent();
        testObserver.assertError(EnforceUserIdentityException.class);
        verify(tokenService, never()).deleteByUserId(any());
        verify(emailService, never()).send(any(), any(), any());
        verify(auditService)
                .report(
                        argThat(
                                builder -> {
                                    final Audit audit = builder.build(new ObjectMapper());
                                    return audit.getType()
                                                    .equals(EventType.FORGOT_PASSWORD_REQUESTED)
                                            && audit.getOutcome()
                                                    .getStatus()
                                                    .equals(Status.FAILURE);
                                }));
    }

    @Test
    public void shouldForgotPassword_userNotFound_fallback_idp() {
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(getApplicationIdentityProviders("idp-1"));

        io.gravitee.am.identityprovider.api.User idpUser =
                mock(io.gravitee.am.identityprovider.api.User.class);
        when(idpUser.getId()).thenReturn("idp-id");
        when(idpUser.getEmail()).thenReturn("test@test.com");
        when(idpUser.getUsername()).thenReturn("idp-username");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByEmail(user.getEmail())).thenReturn(Maybe.just(idpUser));

        when(domain.getId()).thenReturn("domain-id");

        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.emptyList()));
        when(identityProviderManager.getUserProvider("idp-1")).thenReturn(Maybe.just(userProvider));
        when(commonUserService.create(any())).thenReturn(Single.just(user));
        when(commonUserService.findByDomainAndUsernameAndSource(
                        anyString(), anyString(), anyString()))
                .thenReturn(Maybe.empty());
        when(commonUserService.findByDomainAndExternalIdAndSource(
                        anyString(), anyString(), anyString()))
                .thenReturn(Maybe.empty());

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(commonUserService, times(1)).create(any());
        verify(commonUserService, never()).update(any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldForgotPassword_userNotFound_fallback_idp_update_user() {
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(getApplicationIdentityProviders("idp-1"));

        io.gravitee.am.identityprovider.api.User idpUser =
                mock(io.gravitee.am.identityprovider.api.User.class);
        when(idpUser.getEmail()).thenReturn("test@test.com");
        when(idpUser.getUsername()).thenReturn("idp-username");

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByEmail(user.getEmail())).thenReturn(Maybe.just(idpUser));

        when(domain.getId()).thenReturn("domain-id");

        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.emptyList()));
        when(identityProviderManager.getUserProvider("idp-1")).thenReturn(Maybe.just(userProvider));
        when(commonUserService.update(any())).thenReturn(Single.just(user));
        when(commonUserService.findByDomainAndUsernameAndSource(
                        anyString(), anyString(), anyString()))
                .thenReturn(Maybe.just(user));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(commonUserService, never())
                .findByDomainAndExternalIdAndSource(anyString(), anyString(), anyString());
        verify(commonUserService, never()).create(any());
        verify(commonUserService, times(1)).update(any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldNotForgotPassword_userNotFound_noIdp_client() {
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(new TreeSet<>());

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");

        when(domain.getId()).thenReturn("domain-id");
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.emptyList()));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertNotComplete();
        testObserver.assertError(UserNotFoundException.class);
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldNotForgotPassword_userNotFound_idpNotFound() {
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(getApplicationIdentityProviders("idp-1"));

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");

        when(domain.getId()).thenReturn("domain-id");

        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.emptyList()));
        when(identityProviderManager.getUserProvider("idp-1")).thenReturn(Maybe.empty());

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertNotComplete();
        testObserver.assertError(UserNotFoundException.class);
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldNotForgotPassword_userNotFound() {
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(getApplicationIdentityProviders("idp-1"));

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByEmail(user.getEmail())).thenReturn(Maybe.empty());

        when(domain.getId()).thenReturn("domain-id");

        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.emptyList()));
        when(identityProviderManager.getUserProvider("idp-1")).thenReturn(Maybe.just(userProvider));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertNotComplete();
        testObserver.assertError(UserNotFoundException.class);
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldResetPassword_delete_passwordless_devices() {
        AccountSettings accountSettings = mock(AccountSettings.class);
        when(accountSettings.isDeletePasswordlessDevicesAfterResetPassword()).thenReturn(true);

        Client client = mock(Client.class);
        when(client.getAccountSettings()).thenReturn(accountSettings);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.isInactive()).thenReturn(false);
        when(user.getSource()).thenReturn("default-idp");

        io.gravitee.am.identityprovider.api.User idpUser =
                mock(io.gravitee.am.identityprovider.api.DefaultUser.class);
        when(idpUser.getId()).thenReturn("idp-id");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByUsername(user.getUsername())).thenReturn(Maybe.just(idpUser));
        when(userProvider.updatePassword(any(), any())).thenReturn(Single.just(idpUser));

        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(commonUserService.update(any())).thenReturn(Single.just(user));
        when(commonUserService.enhance(any())).thenReturn(Single.just(user));
        when(loginAttemptService.reset(any())).thenReturn(Completable.complete());
        when(credentialService.deleteByUserId(any(), any(), any()))
                .thenReturn(Completable.complete());

        var testObserver = userService.resetPassword(client, user).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(credentialService, times(1)).deleteByUserId(any(), any(), any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void shouldResetPassword_Invalidate_Tokens() {
        AccountSettings accountSettings = mock(AccountSettings.class);
        when(accountSettings.isResetPasswordInvalidateTokens()).thenReturn(true);

        Client client = mock(Client.class);
        when(client.getAccountSettings()).thenReturn(accountSettings);

        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.isInactive()).thenReturn(false);
        when(user.getSource()).thenReturn("default-idp");

        io.gravitee.am.identityprovider.api.User idpUser =
                mock(io.gravitee.am.identityprovider.api.DefaultUser.class);
        when(idpUser.getId()).thenReturn("idp-id");

        UserProvider userProvider = mock(UserProvider.class);
        when(userProvider.findByUsername(user.getUsername())).thenReturn(Maybe.just(idpUser));
        when(userProvider.updatePassword(any(), any())).thenReturn(Single.just(idpUser));

        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(commonUserService.update(any())).thenReturn(Single.just(user));
        when(commonUserService.enhance(any())).thenReturn(Single.just(user));
        when(loginAttemptService.reset(any())).thenReturn(Completable.complete());
        when(tokenService.deleteByUserId(any())).thenReturn(Completable.complete());

        var testObserver = userService.resetPassword(client, user).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(tokenService, times(1)).deleteByUserId(any());
        verify(credentialService, never()).deleteByUserId(any(), any(), any());
    }

    @Test
    public void mustSetEnrolSkipTime() {
        Client client = mock(Client.class);
        var enrollmentSettings = new EnrollmentSettings();
        enrollmentSettings.setForceEnrollment(false);
        enrollmentSettings.setSkipTimeSeconds(7200L);
        var mfaSettings = new MFASettings();
        mfaSettings.setEnrollment(enrollmentSettings);
        var user = mock(User.class);

        when(client.getMfaSettings()).thenReturn(mfaSettings);
        when(user.getMfaEnrollmentSkippedAt()).thenReturn(null);

        doReturn(Single.just(user)).when(commonUserService).update(user);

        userService.setMfaEnrollmentSkippedTime(client, user);

        verify(commonUserService, times(1)).update(user);
    }

    @Test
    public void mustNotSetEnrolSkipTime_settingsNotActive() {
        Client client = mock(Client.class);
        var enrollmentSettings = new EnrollmentSettings();
        enrollmentSettings.setForceEnrollment(true);
        enrollmentSettings.setSkipTimeSeconds(7200L);
        var mfaSettings = new MFASettings();
        mfaSettings.setEnrollment(enrollmentSettings);
        var user = mock(User.class);

        when(client.getMfaSettings()).thenReturn(mfaSettings);

        userService.setMfaEnrollmentSkippedTime(client, user);

        verify(commonUserService, times(0)).update(user);
    }

    @Test
    public void mustNotSetEnrolSkipTime_settingsUserIsNull() {
        Client client = mock(Client.class);
        var enrollmentSettings = new EnrollmentSettings();
        enrollmentSettings.setForceEnrollment(false);
        enrollmentSettings.setSkipTimeSeconds(7200L);
        var mfaSettings = new MFASettings();
        mfaSettings.setEnrollment(enrollmentSettings);

        when(client.getMfaSettings()).thenReturn(mfaSettings);

        userService.setMfaEnrollmentSkippedTime(client, null);

        verify(commonUserService, times(0)).update(any());
    }

    @Test
    public void mustNotSetEnrolSkipTime_settingsUserExpiredAlreadySet() {
        Client client = mock(Client.class);
        var enrollmentSettings = new EnrollmentSettings();
        enrollmentSettings.setForceEnrollment(false);
        enrollmentSettings.setSkipTimeSeconds(
                new Date(System.currentTimeMillis() + 86400L).getTime());
        var mfaSettings = new MFASettings();
        mfaSettings.setEnrollment(enrollmentSettings);

        var user = mock(User.class);

        when(client.getMfaSettings()).thenReturn(mfaSettings);
        when(user.getMfaEnrollmentSkippedAt()).thenReturn(new Date());

        userService.setMfaEnrollmentSkippedTime(client, user);

        verify(commonUserService, times(0)).update(any());
    }

    @Test
    public void shouldNotForgotPassword_client_has_no_Idp() {
        Client client = mock(Client.class);
        User user = mock(User.class);

        when(client.getIdentityProviders()).thenReturn(new TreeSet<>());
        when(domain.getId()).thenReturn("domain-id");
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertNotComplete();
        testObserver.assertError(UserNotFoundException.class);
    }

    @Test
    public void shouldNotForgotPassword_client_Idp_not_match_user_Idp() {
        when(domain.getId()).thenReturn("domain-id");

        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(new TreeSet<>());

        User user = mock(User.class);
        when(user.getSource()).thenReturn("idp-id");

        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));

        IdentityProvider identityProvider = mock(IdentityProvider.class);
        when(identityProvider.getId()).thenReturn("id-that-does-not-match");
        ApplicationIdentityProvider applicationIdentityProvider =
                mock(ApplicationIdentityProvider.class);
        when(applicationIdentityProvider.getIdentity()).thenReturn("some-id");
        when(identityProviderManager.getIdentityProvider(anyString())).thenReturn(identityProvider);
        TreeSet<ApplicationIdentityProvider> applicationIdentityProviders = new TreeSet<>();
        applicationIdentityProviders.add(applicationIdentityProvider);
        when(client.getIdentityProviders()).thenReturn(applicationIdentityProviders);

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertNotComplete();
        testObserver.assertError(UserNotFoundException.class);
    }

    @Test
    public void shouldForgotPassword_client_Idp_match_user_idp() {
        Client client = mock(Client.class);
        UserProvider userProvider = mock(UserProvider.class);
        User user = mock(User.class);
        AccountSettings accountSettings = mock(AccountSettings.class);
        IdentityProvider identityProvider = mock(IdentityProvider.class);
        ApplicationIdentityProvider applicationIdentityProvider =
                mock(ApplicationIdentityProvider.class);
        TreeSet<ApplicationIdentityProvider> applicationIdentityProviders = new TreeSet<>();
        applicationIdentityProviders.add(applicationIdentityProvider);

        when(user.getUsername()).thenReturn("username");
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.isInactive()).thenReturn(true);
        when(user.isEnabled()).thenReturn(true);
        when(user.getSource()).thenReturn("idp-id");
        when(accountSettings.isCompleteRegistrationWhenResetPassword()).thenReturn(true);
        when(domain.getId()).thenReturn("domain-id");
        when(domain.getAccountSettings()).thenReturn(accountSettings);
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(userProvider.findByUsername("username"))
                .thenReturn(Maybe.just(new DefaultUser("username")));
        when(commonUserService.update(any())).thenReturn(Single.just(user));
        when(identityProvider.getId()).thenReturn("some-id");
        when(applicationIdentityProvider.getIdentity()).thenReturn("some-id");
        when(identityProviderManager.getIdentityProvider(anyString())).thenReturn(identityProvider);
        when(client.getIdentityProviders()).thenReturn(applicationIdentityProviders);

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(credentialService, never()).deleteByUserId(any(), any(), any());
        verify(tokenService, never()).deleteByUserId(any());
    }

    @Test
    public void forgotPasswordShoudFail_userDisabled_RegistrationCompleted() {
        when(domain.getId()).thenReturn("domain-id");
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(null);
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.isEnabled()).thenReturn(false);
        when(user.isInactive()).thenReturn(false);

        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));

        UserProvider userProvider = mock(UserProvider.class);
        when(identityProviderManager.getUserProvider(any())).thenReturn(Maybe.just(userProvider));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(AccountInactiveException.class);
        verify(commonUserService, never()).update(any());
    }

    @Test
    public void
            forgotPasswordShouldFail_userDisabled_userInactive_forgotPasswordNotConfirmRegister() {
        when(domain.getId()).thenReturn("domain-id");
        Client client = mock(Client.class);
        when(client.getIdentityProviders()).thenReturn(null);
        User user = mock(User.class);
        when(user.getUsername()).thenReturn("username");
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.isInactive()).thenReturn(true);
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));

        UserProvider userProvider = mock(UserProvider.class);
        when(identityProviderManager.getUserProvider(any())).thenReturn(Maybe.just(userProvider));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(AccountInactiveException.class);
        verify(commonUserService, never()).update(any());
    }

    @Test
    public void
            forgotPasswordShouldFail_userDisabled_userInactive_forgotPasswordAllowConfirmRegister() {
        when(domain.getId()).thenReturn("domain-id");
        Client client = mock(Client.class);
        final var settings = new AccountSettings();
        settings.setCompleteRegistrationWhenResetPassword(true);
        settings.setInherited(false);
        when(client.getAccountSettings()).thenReturn(settings);
        when(client.getIdentityProviders()).thenReturn(null);
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.isEnabled()).thenReturn(false);
        when(user.isInactive()).thenReturn(true);
        when(commonUserService.findByDomainAndCriteria(
                        eq(domain.getId()), any(FilterCriteria.class)))
                .thenReturn(Single.just(Collections.singletonList(user)));

        UserProvider userProvider = mock(UserProvider.class);
        when(identityProviderManager.getUserProvider(user.getSource()))
                .thenReturn(Maybe.just(userProvider));
        when(userProvider.findByUsername(any())).thenReturn(Maybe.just(new DefaultUser()));
        when(commonUserService.update(any())).thenReturn(Single.just(new User()));

        var testObserver = userService.forgotPassword(user.getEmail(), client).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        verify(commonUserService).update(any());
    }

    private SortedSet<ApplicationIdentityProvider> getApplicationIdentityProviders(
            String... identities) {
        var set = new TreeSet<ApplicationIdentityProvider>();
        Arrays.stream(identities)
                .forEach(
                        identity -> {
                            var patchAppIdp = new ApplicationIdentityProvider();
                            patchAppIdp.setIdentity(identity);
                            set.add(patchAppIdp);
                        });
        return set;
    }
}

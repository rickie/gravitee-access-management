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
package io.gravitee.am.gateway.handler.common.auth;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


import io.gravitee.am.common.exception.authentication.AccountDisabledException;
import io.gravitee.am.common.exception.authentication.BadCredentialsException;
import io.gravitee.am.common.exception.authentication.InternalAuthenticationServiceException;
import io.gravitee.am.common.exception.authentication.UsernameNotFoundException;
import io.gravitee.am.gateway.handler.common.auth.event.AuthenticationEvent;
import io.gravitee.am.gateway.handler.common.auth.idp.IdentityProviderManager;
import io.gravitee.am.gateway.handler.common.auth.user.UserAuthenticationService;
import io.gravitee.am.gateway.handler.common.auth.user.impl.UserAuthenticationManagerImpl;
import io.gravitee.am.gateway.handler.common.user.UserService;
import io.gravitee.am.identityprovider.api.Authentication;
import io.gravitee.am.identityprovider.api.AuthenticationContext;
import io.gravitee.am.identityprovider.api.AuthenticationProvider;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.User;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.service.LoginAttemptService;
import io.gravitee.common.event.EventManager;


import io.reactivex.observers.TestObserver;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAuthenticationManagerTest {

  @InjectMocks
  private UserAuthenticationManagerImpl userAuthenticationManager =
      new UserAuthenticationManagerImpl();

  @Mock private UserAuthenticationService userAuthenticationService;

  @Mock private Domain domain;

  @Mock private IdentityProviderManager identityProviderManager;

  @Mock private EventManager eventManager;

  @Mock private LoginAttemptService loginAttemptService;

  @Mock private UserService userService;

  @Test
  public void shouldNotAuthenticateUser_noIdentityProvider() {
    Client client = new Client();
    client.setClientId("client-id");
    client.setIdentities(Collections.emptySet());

    TestObserver<User> observer =
        RxJava2Adapter.monoToSingle(userAuthenticationManager.authenticate_migrated(client, new Authentication() {
                  @Override
                  public Object getCredentials() {
                    return null;
                  }

                  @Override
                  public Object getPrincipal() {
                    return null;
                  }

                  @Override
                  public AuthenticationContext getContext() {
                    return null;
                  }
                }))
            .test();
    observer.assertNotComplete();
    observer.assertError(InternalAuthenticationServiceException.class);
    verifyZeroInteractions(userAuthenticationService);
  }

  @Test
  public void shouldAuthenticateUser_singleIdentityProvider() {
    Client client = new Client();
    client.setClientId("client-id");
    client.setIdentities(Collections.singleton("idp-1"));

    IdentityProvider identityProvider = new IdentityProvider();
    identityProvider.setId("idp-1");
    when(identityProviderManager.getIdentityProvider("idp-1")).thenReturn(identityProvider);

    when(userAuthenticationService.connect_migrated(any(), eq(true)))
        .then(
            invocation -> {
              io.gravitee.am.identityprovider.api.User idpUser = invocation.getArgument(0);
              User user = new User();
              user.setUsername(idpUser.getUsername());
              return Mono.just(user);
            });

    when(identityProviderManager.get_migrated("idp-1"))
        .thenReturn(
            Mono.just(
                    new AuthenticationProvider() {
                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(Authentication authentication) {
                        return Mono.just(new DefaultUser("username"));
                      }

                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(String username) {
                        return Mono.empty();
                      }
                    }));

    TestObserver<User> observer =
        RxJava2Adapter.monoToSingle(userAuthenticationManager.authenticate_migrated(client, new Authentication() {
                  @Override
                  public Object getCredentials() {
                    return null;
                  }

                  @Override
                  public Object getPrincipal() {
                    return "username";
                  }

                  @Override
                  public AuthenticationContext getContext() {
                    return null;
                  }
                }))
            .test();

    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValue(user -> user.getUsername().equals("username"));
    verify(eventManager, times(1)).publishEvent(eq(AuthenticationEvent.SUCCESS), any());
  }

  @Test
  public void shouldAuthenticateUser_singleIdentityProvider_throwException() {
    Client client = new Client();
    client.setClientId("client-id");
    client.setIdentities(Collections.singleton("idp-1"));

    IdentityProvider identityProvider = new IdentityProvider();
    identityProvider.setId("idp-1");
    when(identityProviderManager.getIdentityProvider("idp-1")).thenReturn(identityProvider);

    when(identityProviderManager.get_migrated("idp-1"))
        .thenReturn(
            Mono.just(
                    new AuthenticationProvider() {
                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(Authentication authentication) {
                        throw new BadCredentialsException();
                      }

                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(String username) {
                        return Mono.empty();
                      }
                    }));

    TestObserver<User> observer =
        RxJava2Adapter.monoToSingle(userAuthenticationManager.authenticate_migrated(client, new Authentication() {
                  @Override
                  public Object getCredentials() {
                    return null;
                  }

                  @Override
                  public Object getPrincipal() {
                    return "username";
                  }

                  @Override
                  public AuthenticationContext getContext() {
                    return null;
                  }
                }))
            .test();

    verifyZeroInteractions(userAuthenticationService);
    observer.assertError(BadCredentialsException.class);
    verify(eventManager, times(1)).publishEvent(eq(AuthenticationEvent.FAILURE), any());
  }

  @Test
  public void shouldAuthenticateUser_multipleIdentityProvider() {
    Client client = new Client();
    client.setClientId("client-id");
    client.setIdentities(new LinkedHashSet<>(Arrays.asList("idp-1", "idp-2")));

    IdentityProvider identityProvider = new IdentityProvider();
    identityProvider.setId("idp-1");

    IdentityProvider identityProvider2 = new IdentityProvider();
    identityProvider2.setId("idp-2");

    when(userAuthenticationService.connect_migrated(any(), eq(true)))
        .then(
            invocation -> {
              io.gravitee.am.identityprovider.api.User idpUser = invocation.getArgument(0);
              User user = new User();
              user.setUsername(idpUser.getUsername());
              return Mono.just(user);
            });

    when(identityProviderManager.getIdentityProvider("idp-1")).thenReturn(identityProvider);
    when(identityProviderManager.get_migrated("idp-1"))
        .thenReturn(
            Mono.just(
                    new AuthenticationProvider() {
                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(Authentication authentication) {
                        throw new BadCredentialsException();
                      }

                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(String username) {
                        return Mono.empty();
                      }
                    }));

    when(identityProviderManager.getIdentityProvider("idp-2")).thenReturn(identityProvider2);
    when(identityProviderManager.get_migrated("idp-2"))
        .thenReturn(
            Mono.just(
                    new AuthenticationProvider() {
                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(Authentication authentication) {
                        return Mono.just(new DefaultUser("username"));
                      }

                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(String username) {
                        return Mono.empty();
                      }
                    }));

    TestObserver<User> observer =
        RxJava2Adapter.monoToSingle(userAuthenticationManager.authenticate_migrated(client, new Authentication() {
                  @Override
                  public Object getCredentials() {
                    return null;
                  }

                  @Override
                  public Object getPrincipal() {
                    return "username";
                  }

                  @Override
                  public AuthenticationContext getContext() {
                    return null;
                  }
                }))
            .test();

    observer.assertNoErrors();
    observer.assertComplete();
    observer.assertValue(user -> user.getUsername().equals("username"));
    verify(eventManager, times(1)).publishEvent(eq(AuthenticationEvent.SUCCESS), any());
  }

  @Test
  public void shouldNotAuthenticateUser_accountDisabled() {
    Client client = new Client();
    client.setClientId("client-id");
    client.setIdentities(Collections.singleton("idp-1"));

    IdentityProvider identityProvider = new IdentityProvider();
    identityProvider.setId("idp-1");
    when(identityProviderManager.getIdentityProvider("idp-1")).thenReturn(identityProvider);

    when(userAuthenticationService.connect_migrated(any(), eq(true)))
        .then(
            invocation -> {
              io.gravitee.am.identityprovider.api.User idpUser = invocation.getArgument(0);
              return Mono.error(new AccountDisabledException(idpUser.getUsername()));
            });

    when(identityProviderManager.get_migrated("idp-1"))
        .thenReturn(
            Mono.just(
                    new AuthenticationProvider() {
                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(Authentication authentication) {
                        return Mono.just(new DefaultUser("username"));
                      }

                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(String username) {
                        return Mono.empty();
                      }
                    }));

    TestObserver<User> observer =
        RxJava2Adapter.monoToSingle(userAuthenticationManager.authenticate_migrated(client, new Authentication() {
                  @Override
                  public Object getCredentials() {
                    return null;
                  }

                  @Override
                  public Object getPrincipal() {
                    return "username";
                  }

                  @Override
                  public AuthenticationContext getContext() {
                    return null;
                  }
                }))
            .test();

    observer.assertError(AccountDisabledException.class);
    verify(eventManager, times(1)).publishEvent(eq(AuthenticationEvent.FAILURE), any());
  }

  @Test
  public void shouldNotAuthenticateUser_onlyExternalProvider() {
    Client client = new Client();
    client.setClientId("client-id");
    client.setIdentities(Collections.singleton("idp-1"));

    IdentityProvider identityProvider = new IdentityProvider();
    identityProvider.setId("idp-1");
    identityProvider.setExternal(true);
    when(identityProviderManager.getIdentityProvider("idp-1")).thenReturn(identityProvider);

    TestObserver<User> observer = RxJava2Adapter.monoToSingle(userAuthenticationManager.authenticate_migrated(client, null)).test();
    observer.assertNotComplete();
    observer.assertError(InternalAuthenticationServiceException.class);
    verifyZeroInteractions(userAuthenticationService);
  }

  @Test
  public void shouldNotAuthenticateUser_unknownUserFromIdp_loginAttempt_enabled() {
    AccountSettings accountSettings = new AccountSettings();
    accountSettings.setInherited(false);
    accountSettings.setLoginAttemptsDetectionEnabled(true);
    accountSettings.setMaxLoginAttempts(1);

    Client client = new Client();
    client.setClientId("client-id");
    client.setIdentities(Collections.singleton("idp-1"));
    client.setAccountSettings(accountSettings);

    IdentityProvider identityProvider = new IdentityProvider();
    identityProvider.setId("idp-1");
    when(identityProviderManager.getIdentityProvider("idp-1")).thenReturn(identityProvider);
    when(identityProviderManager.get_migrated("idp-1"))
        .thenReturn(
            Mono.just(
                    new AuthenticationProvider() {
                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(Authentication authentication) {
                        return Mono.error(new UsernameNotFoundException("username"));
                      }

                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(String username) {
                        return Mono.empty();
                      }
                    }));

    when(loginAttemptService.checkAccount_migrated(any(), any()))
        .thenReturn(Mono.empty());
    TestObserver<User> observer =
        RxJava2Adapter.monoToSingle(userAuthenticationManager.authenticate_migrated(client, new Authentication() {
                  @Override
                  public Object getCredentials() {
                    return null;
                  }

                  @Override
                  public Object getPrincipal() {
                    return "username";
                  }

                  @Override
                  public AuthenticationContext getContext() {
                    return null;
                  }
                }))
            .test();

    observer.assertError(BadCredentialsException.class);
    verify(userService, never())
        .findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString());
    verify(loginAttemptService, never()).loginFailed_migrated(any(), any());
    verify(userAuthenticationService, never()).lockAccount_migrated(any(), any(), any(), any());
    verify(eventManager, times(1)).publishEvent(eq(AuthenticationEvent.FAILURE), any());
  }

  @Test
  public void shouldNotAuthenticateUser_unknownUserFromAM_loginAttempt_enabled() {
    AccountSettings accountSettings = new AccountSettings();
    accountSettings.setInherited(false);
    accountSettings.setLoginAttemptsDetectionEnabled(true);
    accountSettings.setMaxLoginAttempts(1);

    Client client = new Client();
    client.setClientId("client-id");
    client.setIdentities(Collections.singleton("idp-1"));
    client.setAccountSettings(accountSettings);

    IdentityProvider identityProvider = new IdentityProvider();
    identityProvider.setId("idp-1");
    when(identityProviderManager.getIdentityProvider("idp-1")).thenReturn(identityProvider);
    when(identityProviderManager.get_migrated("idp-1"))
        .thenReturn(
            Mono.just(
                    new AuthenticationProvider() {
                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(Authentication authentication) {
                        return Mono.error(new BadCredentialsException("username"));
                      }

                      

                      @Override
                      public Mono<io.gravitee.am.identityprovider.api.User>
                          loadUserByUsername_migrated(String username) {
                        return Mono.empty();
                      }
                    }));

    when(domain.getId()).thenReturn("domain-id");
    when(userService.findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString()))
        .thenReturn(Mono.empty());
    when(loginAttemptService.checkAccount_migrated(any(), any()))
        .thenReturn(Mono.empty());
    TestObserver<User> observer =
        RxJava2Adapter.monoToSingle(userAuthenticationManager.authenticate_migrated(client, new Authentication() {
                  @Override
                  public Object getCredentials() {
                    return null;
                  }

                  @Override
                  public Object getPrincipal() {
                    return "username";
                  }

                  @Override
                  public AuthenticationContext getContext() {
                    return null;
                  }
                }))
            .test();

    observer.assertError(BadCredentialsException.class);
    verify(userService, times(1))
        .findByDomainAndUsernameAndSource_migrated(anyString(), anyString(), anyString());
    verify(loginAttemptService, never()).loginFailed_migrated(any(), any());
    verify(userAuthenticationService, never()).lockAccount_migrated(any(), any(), any(), any());
    verify(eventManager, times(1)).publishEvent(eq(AuthenticationEvent.FAILURE), any());
  }
}

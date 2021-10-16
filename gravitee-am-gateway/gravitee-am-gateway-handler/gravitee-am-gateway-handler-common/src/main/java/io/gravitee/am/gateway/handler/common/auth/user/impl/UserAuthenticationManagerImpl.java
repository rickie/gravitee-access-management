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
package io.gravitee.am.gateway.handler.common.auth.user.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.exception.authentication.*;
import io.gravitee.am.common.oauth2.Parameters;
import io.gravitee.am.gateway.handler.common.auth.AuthenticationDetails;
import io.gravitee.am.gateway.handler.common.auth.event.AuthenticationEvent;
import io.gravitee.am.gateway.handler.common.auth.idp.IdentityProviderManager;
import io.gravitee.am.gateway.handler.common.auth.user.EndUserAuthentication;
import io.gravitee.am.gateway.handler.common.auth.user.UserAuthenticationManager;
import io.gravitee.am.gateway.handler.common.auth.user.UserAuthenticationService;
import io.gravitee.am.gateway.handler.common.user.UserService;
import io.gravitee.am.identityprovider.api.Authentication;
import io.gravitee.am.identityprovider.api.AuthenticationProvider;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.SimpleAuthenticationContext;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.LoginAttempt;
import io.gravitee.am.model.User;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.gravitee.am.service.LoginAttemptService;
import io.gravitee.common.event.EventManager;
import io.gravitee.gateway.api.Request;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserAuthenticationManagerImpl implements UserAuthenticationManager {

    private final Logger logger = LoggerFactory.getLogger(UserAuthenticationManagerImpl.class);

    @Autowired
    private Domain domain;

    @Autowired
    private IdentityProviderManager identityProviderManager;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private UserAuthenticationService userAuthenticationService;

    @Autowired
    private UserService userService;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.authenticate_migrated(client, authentication, preAuthenticated))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> authenticate(Client client, Authentication authentication, boolean preAuthenticated) {
 return RxJava2Adapter.monoToSingle(authenticate_migrated(client, authentication, preAuthenticated));
}
@Override
    public Mono<User> authenticate_migrated(Client client, Authentication authentication, boolean preAuthenticated) {
        logger.debug("Trying to authenticate [{}]", authentication);

        // Get identity providers associated to a client
        // For each idp, try to authenticate a user
        // Try to authenticate while the user can not be authenticated
        // If user can't be authenticated, send an exception

        // Skip external identity provider for authentication with credentials.
        List<String> identities = client.getIdentities() != null ?
                client.getIdentities()
                        .stream()
                        .map(identityProviderManager::getIdentityProvider)
                        .filter(idp -> idp != null && !idp.isExternal())
                        .map(IdentityProvider::getId)
                        .collect(Collectors.toList()) : null;
        if (identities == null || identities.isEmpty()) {
            logger.error("No identity provider found for client : " + client.getClientId());
            return Mono.error(new InternalAuthenticationServiceException("No identity provider found for client : " + client.getClientId()));
        }

        return RxJava2Adapter.singleToMono(Observable.fromIterable(identities)
                .flatMapMaybe(authProvider -> RxJava2Adapter.monoToMaybe(authenticate0_migrated(client, authentication, authProvider, preAuthenticated)))
                .takeUntil(userAuthentication -> userAuthentication.getUser() != null || userAuthentication.getLastException() instanceof AccountLockedException)
                .lastOrError()).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.gateway.handler.common.auth.user.impl.UserAuthenticationManagerImpl.UserAuthentication, SingleSource<io.gravitee.am.model.User>>toJdkFunction(userAuthentication -> {
                    io.gravitee.am.identityprovider.api.User user = userAuthentication.getUser();
                    if (user == null) {
                        Throwable lastException = userAuthentication.getLastException();
                        if (lastException != null) {
                            if (lastException instanceof BadCredentialsException) {
                                return RxJava2Adapter.monoToSingle(Mono.error(new BadCredentialsException("The credentials you entered are invalid", lastException)));
                            } else if (lastException instanceof UsernameNotFoundException) {
                                // if an IdP return UsernameNotFoundException, convert it as BadCredentials in order to avoid helping attackers
                                return RxJava2Adapter.monoToSingle(Mono.error(new BadCredentialsException("The credentials you entered are invalid", lastException)));
                            } else if (lastException instanceof AccountStatusException) {
                                return RxJava2Adapter.monoToSingle(Mono.error(lastException));
                            }  else if (lastException instanceof NegotiateContinueException) {
                                return RxJava2Adapter.monoToSingle(Mono.error(lastException));
                            } else {
                                logger.error("An error occurs during user authentication", lastException);
                                return RxJava2Adapter.monoToSingle(Mono.error(new InternalAuthenticationServiceException("Unable to validate credentials. The user account you are trying to access may be experiencing a problem.", lastException)));
                            }
                        } else {
                            // if an IdP return null user, throw BadCredentials in order to avoid helping attackers
                            return RxJava2Adapter.monoToSingle(Mono.error(new BadCredentialsException("The credentials you entered are invalid")));
                        }
                    } else {
                        // complete user connection
                        return RxJava2Adapter.monoToSingle(connect_migrated(user));
                    }
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(user -> eventManager.publishEvent(AuthenticationEvent.SUCCESS, new AuthenticationDetails(authentication, domain, client, user)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> eventManager.publishEvent(AuthenticationEvent.FAILURE, new AuthenticationDetails(authentication, domain, client, throwable))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadUserByUsername_migrated(client, username, request))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> loadUserByUsername(Client client, String username, Request request) {
 return RxJava2Adapter.monoToMaybe(loadUserByUsername_migrated(client, username, request));
}
@Override
    public Mono<User> loadUserByUsername_migrated(Client client, String username, Request request) {
        logger.debug("Trying to load user [{}]", username);

        // Get identity providers associated to a client
        // For each idp, try to find the user while it can not be found
        // If user can't be found, send an exception

        // Skip external identity provider for authentication with credentials.
        List<String> identities = client.getIdentities() != null ?
                client.getIdentities()
                        .stream()
                        .map(identityProviderManager::getIdentityProvider)
                        .filter(idp -> idp != null && !idp.isExternal())
                        .map(IdentityProvider::getId)
                        .collect(Collectors.toList()) : null;

        if (identities == null || identities.isEmpty()) {
            logger.error("No identity provider found for client : " + client.getClientId());
            return Mono.error(new InternalAuthenticationServiceException("No identity provider found for client : " + client.getClientId()));
        }

        final Authentication authentication = new EndUserAuthentication(username, null, new SimpleAuthenticationContext(request));
        return RxJava2Adapter.singleToMono(Observable.fromIterable(identities)
                .flatMapMaybe(authProvider -> RxJava2Adapter.monoToMaybe(loadUserByUsername0_migrated(client, authentication, authProvider, true)))
                .takeUntil(userAuthentication -> userAuthentication.getUser() != null)
                .lastOrError()).flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<UserAuthenticationManagerImpl.UserAuthentication, MaybeSource<User>>)userAuthentication -> {
                    io.gravitee.am.identityprovider.api.User user = userAuthentication.getUser();
                    if (user == null) {
                        Throwable lastException = userAuthentication.getLastException();
                        if (lastException != null) {
                            if (lastException instanceof UsernameNotFoundException) {
                                return RxJava2Adapter.monoToMaybe(Mono.error(new UsernameNotFoundException("Invalid or unknown user")));
                            } else {
                                logger.error("An error occurs during user authentication", lastException);
                                return RxJava2Adapter.monoToMaybe(Mono.error(new InternalAuthenticationServiceException("Unable to validate credentials. The user account you are trying to access may be experiencing a problem.", lastException)));
                            }
                        } else {
                            return RxJava2Adapter.monoToMaybe(Mono.error(new UsernameNotFoundException("No user found for registered providers")));
                        }
                    } else {
                        // complete user connection
                        return RxJava2Adapter.monoToMaybe(userAuthenticationService.loadPreAuthenticatedUser_migrated(user));
                    }
                }).apply(e))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadPreAuthenticatedUser_migrated(subject, request))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> loadPreAuthenticatedUser(String subject, Request request) {
 return RxJava2Adapter.monoToMaybe(loadPreAuthenticatedUser_migrated(subject, request));
}
@Override
    public Mono<User> loadPreAuthenticatedUser_migrated(String subject, Request request) {
        return userAuthenticationService.loadPreAuthenticatedUser_migrated(subject, request);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.connect_migrated(user, afterAuthentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> connect(io.gravitee.am.identityprovider.api.User user, boolean afterAuthentication) {
 return RxJava2Adapter.monoToSingle(connect_migrated(user, afterAuthentication));
}
@Override
    public Mono<User> connect_migrated(io.gravitee.am.identityprovider.api.User user, boolean afterAuthentication) {
        return userAuthenticationService.connect_migrated(user, afterAuthentication);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.authenticate0_migrated(client, authentication, authProvider, preAuthenticated))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<UserAuthentication> authenticate0(Client client, Authentication authentication, String authProvider, boolean preAuthenticated) {
 return RxJava2Adapter.monoToMaybe(authenticate0_migrated(client, authentication, authProvider, preAuthenticated));
}
private Mono<UserAuthentication> authenticate0_migrated(Client client, Authentication authentication, String authProvider, boolean preAuthenticated) {
        return preAuthentication_migrated(client, authentication, authProvider).then(loadUserByUsername0_migrated(client, authentication, authProvider, preAuthenticated)).flatMap(z->postAuthentication_migrated(client, authentication, authProvider, z).then(Mono.just(z)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.loadUserByUsername0_migrated(client, authentication, authProvider, preAuthenticated))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<UserAuthentication> loadUserByUsername0(Client client, Authentication authentication, String authProvider, boolean preAuthenticated) {
 return RxJava2Adapter.monoToMaybe(loadUserByUsername0_migrated(client, authentication, authProvider, preAuthenticated));
}
private Mono<UserAuthentication> loadUserByUsername0_migrated(Client client, Authentication authentication, String authProvider, boolean preAuthenticated) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(identityProviderManager.get_migrated(authProvider).switchIfEmpty(Mono.error(new BadCredentialsException("Unable to load authentication provider " + authProvider + ", an error occurred during the initialization stage"))).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<AuthenticationProvider, MaybeSource<io.gravitee.am.identityprovider.api.User>>toJdkFunction(authenticationProvider -> {
                    logger.debug("Authentication attempt using identity provider {} ({})", authenticationProvider, authenticationProvider.getClass().getName());
                    return RxJava2Adapter.monoToMaybe(Mono.just(preAuthenticated).flatMap(y->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<Boolean, MaybeSource<io.gravitee.am.identityprovider.api.User>>toJdkFunction(preAuth -> {
                                if (preAuth) {
                                    final String username = authentication.getPrincipal().toString();
                                    return RxJava2Adapter.monoToMaybe(userService.findByDomainAndUsernameAndSource_migrated(domain.getId(), username, authProvider).switchIfEmpty(Mono.error(new UsernameNotFoundException(username))).flatMap(a->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, MaybeSource<io.gravitee.am.identityprovider.api.User>>toJdkFunction(user -> {
                                                final Authentication enhanceAuthentication = new EndUserAuthentication(user, null, authentication.getContext());
                                                return RxJava2Adapter.monoToMaybe(authenticationProvider.loadPreAuthenticatedUser_migrated(enhanceAuthentication));
                                            }).apply(a)))));
                                } else {
                                    return RxJava2Adapter.monoToMaybe(authenticationProvider.loadUserByUsername_migrated(authentication));
                                }
                            }).apply(y)))).switchIfEmpty(Mono.error(new UsernameNotFoundException(authentication.getPrincipal().toString()))));
                }).apply(v)))).map(RxJavaReactorMigrationUtil.toJdkFunction(user -> {
                    logger.debug("Successfully Authenticated: " + authentication.getPrincipal() + " with provider authentication provider " + authProvider);
                    Map<String, Object> additionalInformation = user.getAdditionalInformation() == null ? new HashMap<>() : new HashMap<>(user.getAdditionalInformation());
                    additionalInformation.put("source", authProvider);
                    additionalInformation.put(Parameters.CLIENT_ID, client.getId());
                    ((DefaultUser ) user).setAdditionalInformation(additionalInformation);
                    return new UserAuthentication(user, null);
                })))
                .onErrorResumeNext(error -> {
                    logger.debug("Unable to authenticate [{}] with authentication provider [{}]", authentication.getPrincipal(), authProvider, error);
                    return RxJava2Adapter.monoToMaybe(Mono.just(new UserAuthentication(null, error)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.preAuthentication_migrated(client, authentication, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable preAuthentication(Client client, Authentication authentication, String source) {
 return RxJava2Adapter.monoToCompletable(preAuthentication_migrated(client, authentication, source));
}
private Mono<Void> preAuthentication_migrated(Client client, Authentication authentication, String source) {
        return preAuthentication_migrated(client, authentication.getPrincipal().toString(), source);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.postAuthentication_migrated(client, authentication, source, userAuthentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable postAuthentication(Client client, Authentication authentication, String source, UserAuthentication userAuthentication) {
 return RxJava2Adapter.monoToCompletable(postAuthentication_migrated(client, authentication, source, userAuthentication));
}
private Mono<Void> postAuthentication_migrated(Client client, Authentication authentication, String source, UserAuthentication userAuthentication) {
        return postAuthentication_migrated(client, authentication.getPrincipal().toString(), source, userAuthentication);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.preAuthentication_migrated(client, username, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable preAuthentication(Client client, String username, String source) {
 return RxJava2Adapter.monoToCompletable(preAuthentication_migrated(client, username, source));
}
private Mono<Void> preAuthentication_migrated(Client client, String username, String source) {
        final AccountSettings accountSettings = AccountSettings.getInstance(domain, client);
        if (accountSettings != null && accountSettings.isLoginAttemptsDetectionEnabled()) {
            LoginAttemptCriteria criteria = new LoginAttemptCriteria.Builder()
                    .domain(domain.getId())
                    .client(client.getId())
                    .identityProvider(source)
                    .username(username)
                    .build();
            return loginAttemptService.checkAccount_migrated(criteria, accountSettings).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Optional<LoginAttempt>, CompletableSource>)optLoginAttempt -> {
                        if (optLoginAttempt.isPresent() && optLoginAttempt.get().isAccountLocked(accountSettings.getMaxLoginAttempts())) {
                            Map<String, String> details = new HashMap<>();
                            details.put("attempt_id", optLoginAttempt.get().getId());
                            return RxJava2Adapter.monoToCompletable(Mono.error(new AccountLockedException("User " + username + " is locked", details)));
                        }
                        return RxJava2Adapter.monoToCompletable(Mono.empty());
                    }).apply(y)))).then();
        }
        return Mono.empty();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.postAuthentication_migrated(client, username, source, userAuthentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable postAuthentication(Client client, String username, String source, UserAuthentication userAuthentication) {
 return RxJava2Adapter.monoToCompletable(postAuthentication_migrated(client, username, source, userAuthentication));
}
private Mono<Void> postAuthentication_migrated(Client client, String username, String source, UserAuthentication userAuthentication) {
        final AccountSettings accountSettings = AccountSettings.getInstance(domain, client);
        if (accountSettings != null && accountSettings.isLoginAttemptsDetectionEnabled()) {
            LoginAttemptCriteria criteria = new LoginAttemptCriteria.Builder()
                    .domain(domain.getId())
                    .client(client.getId())
                    .identityProvider(source)
                    .username(username)
                    .build();
            // no exception clear login attempt
            if (userAuthentication.getLastException() == null) {
                return loginAttemptService.loginSucceeded_migrated(criteria);
            } else if (userAuthentication.getLastException() instanceof BadCredentialsException) {
                // do not execute login attempt feature for non existing users
                // normally the IdP should respond with Maybe.empty() or UsernameNotFoundException
                // but we can't control custom IdP that's why we have to check user existence
                return userService.findByDomainAndUsernameAndSource_migrated(criteria.domain(), criteria.username(), criteria.identityProvider()).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<User, CompletableSource>)user -> {
                            return RxJava2Adapter.monoToCompletable(loginAttemptService.loginFailed_migrated(criteria, accountSettings).flatMap(z->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<LoginAttempt, CompletableSource>toJdkFunction(loginAttempt -> {
                                        if (loginAttempt.isAccountLocked(accountSettings.getMaxLoginAttempts())) {
                                            return RxJava2Adapter.monoToCompletable(userAuthenticationService.lockAccount_migrated(criteria, accountSettings, client, user));
                                        }
                                        return RxJava2Adapter.monoToCompletable(Mono.empty());
                                    }).apply(z)))).then());
                        }).apply(y)))).then();
            }
        }
        return Mono.empty();
    }

    private class UserAuthentication {
        private io.gravitee.am.identityprovider.api.User user;
        private Throwable lastException;

        public UserAuthentication() {
        }

        public UserAuthentication(io.gravitee.am.identityprovider.api.User user, Throwable lastException) {
            this.user = user;
            this.lastException = lastException;
        }

        public io.gravitee.am.identityprovider.api.User getUser() {
            return user;
        }

        public Throwable getLastException() {
            return lastException;
        }
    }
}

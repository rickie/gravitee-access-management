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

import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.exception.authentication.AccountDisabledException;
import io.gravitee.am.common.oauth2.Parameters;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.common.oidc.idtoken.Claims;
import io.gravitee.am.gateway.handler.common.auth.idp.IdentityProviderManager;
import io.gravitee.am.gateway.handler.common.auth.user.EndUserAuthentication;
import io.gravitee.am.gateway.handler.common.auth.user.UserAuthenticationService;
import io.gravitee.am.gateway.handler.common.email.EmailService;
import io.gravitee.am.gateway.handler.common.user.UserService;
import io.gravitee.am.gateway.handler.common.utils.ConstantKeys;
import io.gravitee.am.identityprovider.api.Authentication;
import io.gravitee.am.identityprovider.api.AuthenticationProvider;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.SimpleAuthenticationContext;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Template;
import io.gravitee.am.model.User;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.UserAuditBuilder;
import io.gravitee.gateway.api.Request;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserAuthenticationServiceImpl implements UserAuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthenticationServiceImpl.class);
    private static final String SOURCE_FIELD = "source";

    @Autowired
    private Domain domain;

    @Autowired
    private UserService userService;

    @Autowired
    private IdentityProviderManager identityProviderManager;

    @Autowired
    private AuditService auditService;

    @Autowired
    private EmailService emailService;

    @Override
    public Single<User> connect(io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication) {
        // save or update the user
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(saveOrUpdate(principal, afterAuthentication)).flatMap(user->RxJava2Adapter.completableToMono(checkAccountStatus(user)).then(Mono.defer(()->RxJava2Adapter.singleToMono(userService.enhance(user))))));
    }

    @Override
    public Maybe<User> loadPreAuthenticatedUser(String subject, Request request) {
        // find user by its technical id
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(userService
                .findById(subject)).switchIfEmpty(Mono.error(new UserNotFoundException(subject))).flatMap(z->RxJava2Adapter.maybeToMono(identityProviderManager.get(z.getSource())).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<AuthenticationProvider, MaybeSource<io.gravitee.am.identityprovider.api.User>>toJdkFunction((io.gravitee.am.identityprovider.api.AuthenticationProvider authenticationProvider)->{
SimpleAuthenticationContext authenticationContext = new SimpleAuthenticationContext(request);
final Authentication authentication = new EndUserAuthentication(z, null, authenticationContext);
return authenticationProvider.loadPreAuthenticatedUser(authentication);
}).apply(v)))).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.identityprovider.api.User, MaybeSource<io.gravitee.am.model.User>>toJdkFunction((io.gravitee.am.identityprovider.api.User idpUser)->{
Map<String, Object> additionalInformation = idpUser.getAdditionalInformation() == null ? new HashMap<>() : new HashMap<>(idpUser.getAdditionalInformation());
additionalInformation.put(SOURCE_FIELD, z.getSource());
additionalInformation.put(Parameters.CLIENT_ID, z.getClient());
((DefaultUser)idpUser).setAdditionalInformation(additionalInformation);
return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(update(z, idpUser, false)).flatMap(a->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(userService::enhance).apply(a)))));
}).apply(v)))).switchIfEmpty(Mono.defer(()->RxJava2Adapter.singleToMono(userService.enhance(z))))));
    }

    @Override
    public Maybe<User> loadPreAuthenticatedUser(io.gravitee.am.identityprovider.api.User principal) {
        String source = (String) principal.getAdditionalInformation().get(SOURCE_FIELD);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(userService.findByDomainAndExternalIdAndSource(domain.getId(), principal.getId(), source)).switchIfEmpty(Mono.defer(()->RxJava2Adapter.maybeToMono(userService.findByDomainAndUsernameAndSource(domain.getId(), principal.getUsername(), source)))));
    }

    @Override
    public Completable lockAccount(LoginAttemptCriteria criteria, AccountSettings accountSettings, Client client, User user) {
        if (user == null) {
            return RxJava2Adapter.monoToCompletable(Mono.empty());
        }

        // update user status
        user.setAccountNonLocked(false);
        user.setAccountLockedAt(new Date());
        user.setAccountLockedUntil(new Date(System.currentTimeMillis() + (accountSettings.getAccountBlockedDuration() * 1000)));

        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(userService.update(user)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(user1 -> {
                    // send an email if option is enabled
                    if (user1.getEmail() != null && accountSettings.isSendRecoverAccountEmail()) {
                        new Thread(() -> emailService.send(Template.BLOCKED_ACCOUNT, user1, client)).start();
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(user));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(user1 -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).type(EventType.USER_LOCKED).domain(criteria.domain()).client(criteria.client()).principal(null).user(user1)))).then());
    }

    @Deprecated
private Single<User> saveOrUpdate(io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication) {
 return RxJava2Adapter.monoToSingle(saveOrUpdate_migrated(principal, afterAuthentication));
}
private Mono<User> saveOrUpdate_migrated(io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication) {
        String source = (String) principal.getAdditionalInformation().get(SOURCE_FIELD);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(userService.findByDomainAndExternalIdAndSource(domain.getId(), principal.getId(), source)).switchIfEmpty(Mono.defer(()->RxJava2Adapter.maybeToMono(userService.findByDomainAndUsernameAndSource(domain.getId(), principal.getUsername(), source)))).switchIfEmpty(Mono.error(new UserNotFoundException(principal.getUsername()))))
                .flatMapSingle(existingUser -> update(existingUser, principal, afterAuthentication))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof UserNotFoundException) {
                        return create(principal, afterAuthentication);
                    }
                    return RxJava2Adapter.monoToSingle(Mono.error(ex));
                }));
    }

    /**
     * Check the user account status
     * @param user Authenticated user
     * @return Completable.complete() or Completable.error(error) if account status is not ok
     */
    @Deprecated
private Completable checkAccountStatus(User user) {
 return RxJava2Adapter.monoToCompletable(checkAccountStatus_migrated(user));
}
private Mono<Void> checkAccountStatus_migrated(User user) {
        if (!user.isEnabled()) {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new AccountDisabledException("Account is disabled for user " + user.getUsername()))));
        }
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty()));
    }

    /**
     * Update user information with data from the identity provider user
     * @param existingUser existing user in the repository
     * @param principal user from the identity provider
     * @param afterAuthentication if update operation is called after a sign in operation
     * @return updated user
     */
    @Deprecated
private Single<User> update(User existingUser, io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication) {
 return RxJava2Adapter.monoToSingle(update_migrated(existingUser, principal, afterAuthentication));
}
private Mono<User> update_migrated(User existingUser, io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication) {
        LOGGER.debug("Updating user: username[%s]", principal.getUsername());
        // set external id
        existingUser.setExternalId(principal.getId());
        if (afterAuthentication) {
            existingUser.setLoggedAt(new Date());
            existingUser.setLoginsCount(existingUser.getLoginsCount() + 1);
        }
        // set roles
        if (existingUser.getRoles() == null) {
            existingUser.setRoles(principal.getRoles());
        } else if (principal.getRoles() != null) {
            // filter roles
            principal.getRoles().removeAll(existingUser.getRoles());
            existingUser.getRoles().addAll(principal.getRoles());
        }
        Map<String, Object> additionalInformation = principal.getAdditionalInformation();
        if (afterAuthentication && !additionalInformation.containsKey(ConstantKeys.OIDC_PROVIDER_ID_TOKEN_KEY) && existingUser.getAdditionalInformation() != null) {
            // remove the op_id_token from existing user profile to avoid keep this information
            // if the singleSignOut is disabled
            existingUser.getAdditionalInformation().remove(ConstantKeys.OIDC_PROVIDER_ID_TOKEN_KEY);
        }
        extractAdditionalInformation(existingUser, additionalInformation);
        return RxJava2Adapter.singleToMono(userService.update(existingUser));
    }

    /**
     * Create user with data from the identity provider user
     * @param principal user from the identity provider
     * @param afterAuthentication if create operation is called after a sign in operation
     * @return created user
     */
    @Deprecated
private Single<User> create(io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication) {
 return RxJava2Adapter.monoToSingle(create_migrated(principal, afterAuthentication));
}
private Mono<User> create_migrated(io.gravitee.am.identityprovider.api.User principal, boolean afterAuthentication) {
        LOGGER.debug("Creating a new user: username[%s]", principal.getUsername());
        final User newUser = new User();
        // set external id
        newUser.setExternalId(principal.getId());
        newUser.setUsername(principal.getUsername());
        newUser.setEmail(principal.getEmail());
        newUser.setFirstName(principal.getFirstName());
        newUser.setLastName(principal.getLastName());
        newUser.setReferenceType(ReferenceType.DOMAIN);
        newUser.setReferenceId(domain.getId());
        if (afterAuthentication) {
            newUser.setLoggedAt(new Date());
            newUser.setLoginsCount(1L);
        }
        newUser.setRoles(principal.getRoles());

        Map<String, Object> additionalInformation = principal.getAdditionalInformation();
        extractAdditionalInformation(newUser, additionalInformation);
        return RxJava2Adapter.singleToMono(userService.create(newUser));
    }

    private void extractAdditionalInformation(User user, Map<String, Object> additionalInformation) {
        if (additionalInformation != null) {
            Map<String, Object> extraInformation = user.getAdditionalInformation() != null ? new HashMap<>(user.getAdditionalInformation()) : new HashMap<>();
            extraInformation.putAll(additionalInformation);
            if (user.getLoggedAt() != null) {
                extraInformation.put(Claims.auth_time, user.getLoggedAt().getTime() / 1000);
            }
            extraInformation.put(StandardClaims.PREFERRED_USERNAME, user.getUsername());
            user.setSource((String) extraInformation.remove(SOURCE_FIELD));
            user.setClient((String) extraInformation.remove(Parameters.CLIENT_ID));
            user.setAdditionalInformation(extraInformation);
        }
    }
}

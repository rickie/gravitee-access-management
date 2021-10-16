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
package io.gravitee.am.management.service.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.jwt.Claims;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.jwt.JWTBuilder;
import io.gravitee.am.management.service.EmailService;
import io.gravitee.am.management.service.UserService;
import io.gravitee.am.model.*;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Email;
import io.gravitee.am.model.User;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.UserAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component("managementUserService")
public class UserServiceImpl extends AbstractUserService<io.gravitee.am.service.UserService> implements UserService {

    private static final String DEFAULT_IDP_PREFIX = "default-idp-";

    @Value("${user.registration.token.expire-after:86400}")
    private Integer expireAfter;

    @Autowired
    private EmailService emailService;

    @Autowired
    @Qualifier("managementJwtBuilder")
    private JWTBuilder jwtBuilder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DomainService domainService;

    @Autowired
    protected io.gravitee.am.service.UserService userService;

    @Override
    protected io.gravitee.am.service.UserService getUserService() {
        return this.userService;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<User>> search(ReferenceType referenceType, String referenceId, String query, int page, int size) {
 return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, query, page, size));
}
@Override
    public Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, String query, int page, int size) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.search_migrated(referenceType, referenceId, query, page, size)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, filterCriteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<User>> search(ReferenceType referenceType, String referenceId, FilterCriteria filterCriteria, int page, int size) {
 return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, filterCriteria, page, size));
}
@Override
    public Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, FilterCriteria filterCriteria, int page, int size) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.search_migrated(referenceType, referenceId, filterCriteria, page, size)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(referenceType, referenceId, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<User>> findAll(ReferenceType referenceType, String referenceId, int page, int size) {
 return RxJava2Adapter.monoToSingle(findAll_migrated(referenceType, referenceId, page, size));
}
@Override
    public Mono<Page<User>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.findAll_migrated(referenceType, referenceId, page, size)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<User>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<User>> findByDomain_migrated(String domain, int page, int size) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(findAll_migrated(ReferenceType.DOMAIN, domain, page, size))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(userPage -> userPage.getData().forEach(this::setInternalStatus)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<User> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(userService.findById_migrated(id))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::setInternalStatus))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newUser, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> create(Domain domain, NewUser newUser, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newUser, principal));
}
@Override
    public Mono<User> create_migrated(Domain domain, NewUser newUser, io.gravitee.am.identityprovider.api.User principal) {
        // user must have a password in no pre registration mode
        if (newUser.getPassword() == null) {
            if (!newUser.isPreRegistration()) {
                return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new UserInvalidException("Field [password] is required"))));
            }
        }

        // set user idp source
        if (newUser.getSource() == null) {
            newUser.setSource(DEFAULT_IDP_PREFIX + domain.getId());
        }

        // check user
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(userService.findByDomainAndUsernameAndSource_migrated(domain.getId(), newUser.getUsername(), newUser.getSource()))).hasElement().flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, Single<User>>)isEmpty -> {
                    if (!isEmpty) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new UserAlreadyExistsException(newUser.getUsername())));
                    } else {
                        // check user provider
                        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(identityProviderManager.getUserProvider_migrated(newUser.getSource()))).switchIfEmpty(Mono.error(new UserProviderNotFoundException(newUser.getSource()))))
                                .flatMapSingle(userProvider -> {
                                    // check client
                                    return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(checkClientFunction().apply(domain.getId(), newUser.getClient())).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()))
                                            .flatMapSingle(optClient -> {
                                                Application client = optClient.orElse(null);
                                                newUser.setDomain(domain.getId());
                                                newUser.setClient(client != null ? client.getId() : null);
                                                // user is flagged as internal user
                                                newUser.setInternal(true);
                                                if (newUser.isPreRegistration()) {
                                                    newUser.setPassword(null);
                                                    newUser.setRegistrationCompleted(false);
                                                    newUser.setEnabled(false);
                                                } else {
                                                    String password = newUser.getPassword();
                                                    if (password != null && isInvalidUserPassword(password, client, domain)) {
                                                        return RxJava2Adapter.monoToSingle(Mono.error(InvalidPasswordException.of("Field [password] is invalid", "invalid_password_value")));
                                                    }
                                                    newUser.setRegistrationCompleted(true);
                                                    newUser.setEnabled(true);
                                                    newUser.setDomain(domain.getId());
                                                }

                                                // store user in its identity provider:
                                                // - perform first validation of user to avoid error status 500 when the IDP is based on relational databases
                                                // - in case of error, trace the event otherwise continue the creation process
                                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(userValidator.validate_migrated(transform(newUser)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_CREATED).throwable(throwable)))).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userProvider.create_migrated(convert(newUser))))).map(RxJavaReactorMigrationUtil.toJdkFunction(idpUser -> {
                                                            // AM 'users' collection is not made for authentication (but only management stuff)
                                                            // clear password
                                                            newUser.setPassword(null);
                                                            // set external id
                                                            newUser.setExternalId(idpUser.getId());
                                                            return newUser;
                                                        })))
                                                        // if a user is already in the identity provider but not in the AM users collection,
                                                        // it means that the user is coming from a pre-filled AM compatible identity provider (user creation enabled)
                                                        // try to create the user with the idp user information
                                                        .onErrorResumeNext(ex -> {
                                                            if (ex instanceof UserAlreadyExistsException) {
                                                                return RxJava2Adapter.monoToMaybe(userProvider.findByUsername_migrated(newUser.getUsername()))
                                                                        // double check user existence for case sensitive
                                                                        .flatMapSingle(idpUser -> RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(userService.findByDomainAndUsernameAndSource_migrated(domain.getId(), idpUser.getUsername(), newUser.getSource()))).hasElement().map(RxJavaReactorMigrationUtil.toJdkFunction(empty -> {
                                                                                    if (!empty) {
                                                                                        throw new UserAlreadyExistsException(newUser.getUsername());
                                                                                    } else {
                                                                                        // AM 'users' collection is not made for authentication (but only management stuff)
                                                                                        // clear password
                                                                                        newUser.setPassword(null);
                                                                                        // set external id
                                                                                        newUser.setExternalId(idpUser.getId());
                                                                                        // set username
                                                                                        newUser.setUsername(idpUser.getUsername());
                                                                                        return newUser;
                                                                                    }
                                                                                }))));
                                                            } else {
                                                                return RxJava2Adapter.monoToSingle(Mono.error(ex));
                                                            }
                                                        })).flatMap(x->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.service.model.NewUser, SingleSource<io.gravitee.am.model.User>>toJdkFunction(newUser1 -> {
                                                            User user = transform(newUser1);
                                                            AccountSettings accountSettings = AccountSettings.getInstance(domain, client);
                                                            if (newUser.isPreRegistration() && accountSettings != null && accountSettings.isDynamicUserRegistration()) {
                                                                user.setRegistrationUserUri(domainService.buildUrl(domain, "/confirmRegistration"));
                                                                user.setRegistrationAccessToken(getUserRegistrationToken(user));
                                                            }
                                                            return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.create_migrated(user))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(user1 -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_CREATED).user(user1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_CREATED).throwable(throwable)))));
                                                        }).apply(x)))).flatMap(z->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(user -> {
                                                            // end pre-registration user if required
                                                            AccountSettings accountSettings = AccountSettings.getInstance(domain, client);
                                                            if (newUser.isPreRegistration() && (accountSettings == null || !accountSettings.isDynamicUserRegistration())) {
                                                                return sendRegistrationConfirmation(user.getReferenceId(), user.getId(), principal).toSingleDefault(user);
                                                            } else {
                                                                return RxJava2Adapter.monoToSingle(Mono.just(user));
                                                            }
                                                        }).apply(z)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::setInternalStatus)));
                                            });
                                });
                    }
                }).apply(v)))));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateUser, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> update(String domain, String id, UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateUser, principal));
}
@Override
    public Mono<User> update_migrated(String domain, String id, UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.singleToMono(update(ReferenceType.DOMAIN, domain, id, updateUser, principal));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.updateStatus_migrated(domain, id, status, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> updateStatus(String domain, String id, boolean status, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(updateStatus_migrated(domain, id, status, principal));
}
@Override
    public Mono<User> updateStatus_migrated(String domain, String id, boolean status, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.singleToMono(updateStatus(ReferenceType.DOMAIN, domain, id, status, principal));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.resetPassword_migrated(domain, userId, password, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable resetPassword(Domain domain, String userId, String password, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToCompletable(resetPassword_migrated(domain, userId, password, principal));
}
@Override
    public Mono<Void> resetPassword_migrated(Domain domain, String userId, String password, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.findById_migrated(ReferenceType.DOMAIN, domain.getId(), userId))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(user -> {
                    // get client for password settings
                    return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(checkClientFunction().apply(domain.getId(), user.getClient())).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()))
                            .flatMapSingle(optClient -> {
                                // check user password
                                if (isInvalidUserPassword(password, optClient.orElse(null), domain)) {
                                    return RxJava2Adapter.monoToSingle(Mono.error(InvalidPasswordException.of("Field [password] is invalid", "invalid_password_value")));
                                }
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(identityProviderManager.getUserProvider_migrated(user.getSource()))).switchIfEmpty(Mono.error(new UserProviderNotFoundException(user.getSource()))))
                                        .flatMapSingle(userProvider -> {
                                            // update idp user
                                            return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(userProvider.findByUsername_migrated(user.getUsername()))).switchIfEmpty(Mono.error(new UserNotFoundException(user.getUsername()))))
                                                    .flatMapSingle(idpUser -> {
                                                        // set password
                                                        ((DefaultUser) idpUser).setCredentials(password);
                                                        return RxJava2Adapter.monoToSingle(userProvider.update_migrated(idpUser.getId(), idpUser));
                                                    })
                                                    .onErrorResumeNext(ex -> {
                                                        if (ex instanceof UserNotFoundException) {
                                                            // idp user not found, create its account
                                                            user.setPassword(password);
                                                            return RxJava2Adapter.monoToSingle(userProvider.create_migrated(convert(user)));
                                                        }
                                                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                                                    });
                                        })).flatMap(a->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.identityprovider.api.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(idpUser -> {
                                            // update 'users' collection for management and audit purpose
                                            // if user was in pre-registration mode, end the registration process
                                            if (user.isPreRegistration()) {
                                                user.setRegistrationCompleted(true);
                                                user.setEnabled(true);
                                            }
                                            user.setPassword(null);
                                            user.setExternalId(idpUser.getId());
                                            user.setLastPasswordReset(new Date());
                                            user.setUpdatedAt(new Date());
                                            return RxJava2Adapter.monoToSingle(userService.update_migrated(user));
                                        }).apply(a)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(user1 -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_PASSWORD_RESET).user(user)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_PASSWORD_RESET).throwable(throwable)))));
                            });
                }).apply(v)))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<User, CompletableSource>)user -> {
                    // reset login attempts in case of reset password action
                    LoginAttemptCriteria criteria = new LoginAttemptCriteria.Builder()
                            .domain(user.getReferenceId())
                            .client(user.getClient())
                            .username(user.getUsername())
                            .build();
                    return RxJava2Adapter.monoToCompletable(loginAttemptService.reset_migrated(criteria));
                }).apply(y)))).then()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.sendRegistrationConfirmation_migrated(domainId, userId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable sendRegistrationConfirmation(String domainId, String userId, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToCompletable(sendRegistrationConfirmation_migrated(domainId, userId, principal));
}
@Override
    public Mono<Void> sendRegistrationConfirmation_migrated(String domainId, String userId, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domainId))).switchIfEmpty(Mono.error(new DomainNotFoundException(domainId))).flatMap(domain1->RxJava2Adapter.singleToMono(findById(ReferenceType.DOMAIN, domainId, userId)).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<User, CompletableSource>toJdkFunction((io.gravitee.am.model.User user)->{
if (!user.isPreRegistration()) {
return RxJava2Adapter.monoToCompletable(Mono.error(new UserInvalidException("Pre-registration is disabled for the user " + userId)));
}
if (user.isPreRegistration() && user.isRegistrationCompleted()) {
return RxJava2Adapter.monoToCompletable(Mono.error(new UserInvalidException("Registration is completed for the user " + userId)));
}
return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(checkClientFunction().apply(user.getReferenceId(), user.getClient())).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer((java.util.Optional<io.gravitee.am.model.Application> optClient)->new Thread(()->emailService.send(domain1, optClient.orElse(null), Template.REGISTRATION_CONFIRMATION, user)).start())).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer((java.util.Optional<io.gravitee.am.model.Application> __)->auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.REGISTRATION_CONFIRMATION_REQUESTED).user(user)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.REGISTRATION_CONFIRMATION_REQUESTED).throwable(throwable)))).then());
}).apply(y)))).then()).then()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.unlock_migrated(referenceType, referenceId, userId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable unlock(ReferenceType referenceType, String referenceId, String userId, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToCompletable(unlock_migrated(referenceType, referenceId, userId, principal));
}
@Override
    public Mono<Void> unlock_migrated(ReferenceType referenceType, String referenceId, String userId, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(findById(referenceType, referenceId, userId)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(user -> {
                    user.setAccountNonLocked(true);
                    user.setAccountLockedAt(null);
                    user.setAccountLockedUntil(null);
                    // reset login attempts and update user
                    LoginAttemptCriteria criteria = new LoginAttemptCriteria.Builder()
                            .domain(user.getReferenceId())
                            .client(user.getClient())
                            .username(user.getUsername())
                            .build();
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(loginAttemptService.reset_migrated(criteria))).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.update_migrated(user)))));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(user1 -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_UNLOCKED).user(user1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_UNLOCKED).throwable(throwable)))).then()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.assignRoles_migrated(referenceType, referenceId, userId, roles, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> assignRoles(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(assignRoles_migrated(referenceType, referenceId, userId, roles, principal));
}
@Override
    public Mono<User> assignRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(assignRoles0_migrated(referenceType, referenceId, userId, roles, principal, false)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.revokeRoles_migrated(referenceType, referenceId, userId, roles, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> revokeRoles(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, userId, roles, principal));
}
@Override
    public Mono<User> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(assignRoles0_migrated(referenceType, referenceId, userId, roles, principal, true)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enrollFactors_migrated(userId, factors, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> enrollFactors(String userId, List<EnrolledFactor> factors, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(enrollFactors_migrated(userId, factors, principal));
}
@Override
    public Mono<User> enrollFactors_migrated(String userId, List<EnrolledFactor> factors, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(userService.findById_migrated(userId))).switchIfEmpty(Mono.error(new UserNotFoundException(userId))))
                .flatMapSingle(oldUser -> {
                    User userToUpdate = new User(oldUser);
                    userToUpdate.setFactors(factors);
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.update_migrated(userToUpdate))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(user1 -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_UPDATED).user(user1).oldValue(oldUser)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_UPDATED).throwable(throwable)))));
                }));
    }

    public void setExpireAfter(Integer expireAfter) {
        this.expireAfter = expireAfter;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.assignRoles0_migrated(referenceType, referenceId, userId, roles, principal, revoke))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<User> assignRoles0(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal, boolean revoke) {
 return RxJava2Adapter.monoToSingle(assignRoles0_migrated(referenceType, referenceId, userId, roles, principal, revoke));
}
private Mono<User> assignRoles0_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal, boolean revoke) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(findById(referenceType, referenceId, userId)).flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<User, Single<User>>)oldUser -> {
                    User userToUpdate = new User(oldUser);
                    // remove existing roles from the user
                    if (revoke) {
                        if (userToUpdate.getRoles() != null) {
                            userToUpdate.getRoles().removeAll(roles);
                        }
                    } else {
                        userToUpdate.setRoles(roles);
                    }
                    // check roles
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(checkRoles_migrated(roles))).then(Mono.defer(()->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.update_migrated(userToUpdate))))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(user1 -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_ROLES_ASSIGNED).oldValue(oldUser).user(user1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_ROLES_ASSIGNED).throwable(throwable)))));
                }).apply(v)))));
    }

    protected BiFunction<String, String, Maybe<Application>> checkClientFunction() {
        return (domain, client) -> {
            if (client == null) {
                return RxJava2Adapter.monoToMaybe(Mono.empty());
            }
            return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(applicationService.findById_migrated(client))).switchIfEmpty(Mono.defer(()->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(applicationService.findByDomainAndClientId_migrated(domain, client))))).switchIfEmpty(Mono.error(new ClientNotFoundException(client))).map(RxJavaReactorMigrationUtil.toJdkFunction(app1 -> {
                        if (!domain.equals(app1.getDomain())) {
                            throw new ClientNotFoundException(client);
                        }
                        return app1;
                    })));
        };
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkRoles_migrated(roles))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable checkRoles(List<String> roles) {
 return RxJava2Adapter.monoToCompletable(checkRoles_migrated(roles));
}
private Mono<Void> checkRoles_migrated(List<String> roles) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleService.findByIdIn_migrated(roles))).map(RxJavaReactorMigrationUtil.toJdkFunction(roles1 -> {
                    if (roles1.size() != roles.size()) {
                        // find difference between the two list
                        roles.removeAll(roles1.stream().map(Role::getId).collect(Collectors.toList()));
                        throw new RoleNotFoundException(String.join(",", roles));
                    }
                    return roles1;
                }))).toCompletable());
    }

    private boolean isInvalidUserPassword(String password, Application application, Domain domain) {
        return PasswordSettings.getInstance(application, domain)
                .map(ps -> !passwordValidator.isValid(password, ps))
                .orElseGet(() -> !passwordValidator.isValid(password));
    }

    private String getUserRegistrationToken(User user) {
        // fetch email to get the custom expiresAfter time
        Email email = emailService.getEmailTemplate(Template.REGISTRATION_CONFIRMATION, user);
        return getUserRegistrationToken(user, email.getExpiresAfter());
    }

    private String getUserRegistrationToken(User user, Integer expiresAfter) {
        // generate a JWT to store user's information and for security purpose
        final Map<String, Object> claims = new HashMap<>();
        Instant now = Instant.now();
        claims.put(Claims.iat, now.getEpochSecond());
        claims.put(Claims.exp, now.plusSeconds((expiresAfter != null ? expiresAfter : expireAfter)).getEpochSecond());
        claims.put(Claims.sub, user.getId());
        if (user.getClient() != null) {
            claims.put(Claims.aud, user.getClient());
        }
        return jwtBuilder.sign(new JWT(claims));
    }

    private User transform(NewUser newUser) {
        return transform(newUser, ReferenceType.DOMAIN, newUser.getDomain());
    }
}

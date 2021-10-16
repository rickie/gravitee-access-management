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
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.UserProvider;
import io.gravitee.am.management.service.CommonUserService;
import io.gravitee.am.management.service.IdentityProviderManager;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.MembershipService;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.gravitee.am.service.exception.UserProviderNotFoundException;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.UserAuditBuilder;
import io.gravitee.am.service.validators.PasswordValidator;
import io.gravitee.am.service.validators.UserValidator;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractUserService<T extends io.gravitee.am.service.CommonUserService> implements CommonUserService {

    @Autowired
    protected IdentityProviderManager identityProviderManager;

    @Autowired
    protected PasswordValidator passwordValidator;

    @Autowired
    protected UserValidator userValidator;

    @Autowired
    protected AuditService auditService;

    @Autowired
    protected MembershipService membershipService;

    protected abstract  BiFunction<String, String, Maybe<Application>> checkClientFunction();

    protected abstract T getUserService();
    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> findById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<User> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getUserService().findById_migrated(referenceType, referenceId, id))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::setInternalStatus))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateUser, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> update(ReferenceType referenceType, String referenceId, String id, UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateUser, principal));
}
@Override
    public Mono<User> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.singleToMono(this.update(referenceType, referenceId, id, updateUser, principal, checkClientFunction()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateUser, principal, checkClient))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<User> update(ReferenceType referenceType, String referenceId, String id, UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal, BiFunction<String, String, Maybe<Application>> checkClient) {
 return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateUser, principal, checkClient));
}
private Mono<User> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal, BiFunction<String, String, Maybe<Application>> checkClient) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(userValidator.validate_migrated(updateUser))).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getUserService().findById_migrated(referenceType, referenceId, id))).flatMap(user->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(identityProviderManager.getUserProvider_migrated(user.getSource()))).switchIfEmpty(Mono.error(new UserProviderNotFoundException(user.getSource())))).flatMapSingle((io.gravitee.am.identityprovider.api.UserProvider userProvider)->{
String client = updateUser.getClient() != null ? updateUser.getClient() : user.getClient();
if (client != null && referenceType == ReferenceType.DOMAIN) {
return checkClient.apply(referenceId, client).flatMapSingle((io.gravitee.am.model.Application client1)->{
updateUser.setClient(client1.getId());
return RxJava2Adapter.monoToSingle(Mono.just(userProvider));
});
}
return RxJava2Adapter.monoToSingle(Mono.just(userProvider));
})).flatMap(userProvider->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(userProvider.findByUsername_migrated(user.getUsername()))).switchIfEmpty(Mono.error(new UserNotFoundException(user.getUsername())))).flatMapSingle((io.gravitee.am.identityprovider.api.User idpUser)->RxJava2Adapter.monoToSingle(userProvider.update_migrated(idpUser.getId(), convert(user.getUsername(), updateUser)))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.identityprovider.api.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction((io.gravitee.am.identityprovider.api.User idpUser)->{
updateUser.setExternalId(idpUser.getId());
return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getUserService().update_migrated(referenceType, referenceId, id, updateUser))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::setInternalStatus)));
}).apply(v))))).onErrorResumeNext((java.lang.Throwable ex)->{
if (ex instanceof UserNotFoundException) {
return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getUserService().update_migrated(referenceType, referenceId, id, updateUser))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::setInternalStatus)));
}
return RxJava2Adapter.monoToSingle(Mono.error(ex));
})).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer((io.gravitee.am.model.User user1)->auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_UPDATED).oldValue(user).user(user1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_UPDATED).throwable(throwable))))))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.updateStatus_migrated(referenceType, referenceId, id, status, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> updateStatus(ReferenceType referenceType, String referenceId, String id, boolean status, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(updateStatus_migrated(referenceType, referenceId, id, status, principal));
}
@Override
    public Mono<User> updateStatus_migrated(ReferenceType referenceType, String referenceId, String id, boolean status, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getUserService().findById_migrated(referenceType, referenceId, id))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(user -> {
                    user.setEnabled(status);
                    return RxJava2Adapter.monoToSingle(getUserService().update_migrated(user));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(user1 -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type((status ? EventType.USER_ENABLED : EventType.USER_DISABLED)).user(user1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type((status ? EventType.USER_ENABLED : EventType.USER_DISABLED)).throwable(throwable))))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, userId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(ReferenceType referenceType, String referenceId, String userId, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, userId, principal));
}
@Override
    public Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String userId, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getUserService().findById_migrated(referenceType, referenceId, userId))).flatMap(user->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(identityProviderManager.getUserProvider_migrated(user.getSource()))).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::ofNullable)).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Optional<UserProvider>, CompletableSource>)(java.util.Optional<io.gravitee.am.identityprovider.api.UserProvider> optUserProvider)->{
if (!optUserProvider.isPresent()) {
return RxJava2Adapter.monoToCompletable(Mono.empty());
}
if (user.getExternalId() == null || user.getExternalId().isEmpty()) {
return RxJava2Adapter.monoToCompletable(Mono.empty());
}
return RxJava2Adapter.monoToCompletable(optUserProvider.get().delete_migrated(user.getExternalId())).onErrorResumeNext((java.lang.Throwable ex)->{
if (ex instanceof UserNotFoundException) {
return RxJava2Adapter.monoToCompletable(Mono.empty());
}
return RxJava2Adapter.monoToCompletable(Mono.error(ex));
});
}).apply(y)))).then(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(getUserService().delete_migrated(userId)))).then(RxJava2Adapter.completableToMono(Completable.wrap((ReferenceType.ORGANIZATION != referenceType) ? RxJava2Adapter.monoToCompletable(Mono.empty()) : RxJava2Adapter.monoToCompletable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(membershipService.findByMember_migrated(userId, MemberType.USER))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<Membership, CompletableSource>toJdkFunction((io.gravitee.am.model.Membership membership)->RxJava2Adapter.monoToCompletable(membershipService.delete_migrated(membership.getId()))).apply(y)))).then()))))).doOnComplete(()->auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_DELETED).user(user)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(UserAuditBuilder.class).principal(principal).type(EventType.USER_DELETED).throwable(throwable))))).then()));
    }

    protected io.gravitee.am.identityprovider.api.User convert(NewUser newUser) {
        DefaultUser user = new DefaultUser(newUser.getUsername());
        user.setCredentials(newUser.getPassword());

        Map<String, Object> additionalInformation = new HashMap<>();
        if (newUser.getFirstName() != null) {
            user.setFirstName(newUser.getFirstName());
            additionalInformation.put(StandardClaims.GIVEN_NAME, newUser.getFirstName());
        }
        if (newUser.getLastName() != null) {
            user.setLastName(newUser.getLastName());
            additionalInformation.put(StandardClaims.FAMILY_NAME, newUser.getLastName());
        }
        if (newUser.getEmail() != null) {
            user.setEmail(newUser.getEmail());
            additionalInformation.put(StandardClaims.EMAIL, newUser.getEmail());
        }
        if (newUser.getAdditionalInformation() != null) {
            newUser.getAdditionalInformation().forEach(additionalInformation::putIfAbsent);
        }
        user.setAdditionalInformation(additionalInformation);

        return user;
    }

    protected User transform(NewUser newUser, ReferenceType referenceType, String referenceId) {
        User user = new User();
        user.setId(RandomString.generate());
        user.setExternalId(newUser.getExternalId());
        user.setReferenceId(referenceId);
        user.setReferenceType(referenceType);
        user.setClient(newUser.getClient());
        user.setEnabled(newUser.isEnabled());
        user.setUsername(newUser.getUsername());
        user.setFirstName(newUser.getFirstName());
        user.setLastName(newUser.getLastName());
        user.setEmail(newUser.getEmail());
        user.setSource(newUser.getSource());
        user.setInternal(newUser.isInternal());
        user.setPreRegistration(newUser.isPreRegistration());
        user.setRegistrationCompleted(newUser.isRegistrationCompleted());
        user.setAdditionalInformation(newUser.getAdditionalInformation());
        user.setCreatedAt(new Date());
        user.setUpdatedAt(user.getCreatedAt());
        return user;
    }

    protected void updateInfos(User user, NewUser newUser) {
        user.setFirstName(newUser.getFirstName());
        user.setLastName(newUser.getLastName());
        user.setEmail(newUser.getEmail());
        user.setAdditionalInformation(newUser.getAdditionalInformation());
    }

    protected io.gravitee.am.identityprovider.api.User convert(String username, UpdateUser updateUser) {
        // update additional information
        DefaultUser user = new DefaultUser(username);
        Map<String, Object> additionalInformation = new HashMap<>();
        if (updateUser.getFirstName() != null) {
            user.setFirstName(updateUser.getFirstName());
            additionalInformation.put(StandardClaims.GIVEN_NAME, updateUser.getFirstName());
        }
        if (updateUser.getLastName() != null) {
            user.setLastName(updateUser.getLastName());
            additionalInformation.put(StandardClaims.FAMILY_NAME, updateUser.getLastName());
        }
        if (updateUser.getEmail() != null) {
            user.setEmail(updateUser.getEmail());
            additionalInformation.put(StandardClaims.EMAIL, updateUser.getEmail());
        }
        if (updateUser.getAdditionalInformation() != null) {
            updateUser.getAdditionalInformation().forEach(additionalInformation::putIfAbsent);
        }
        user.setAdditionalInformation(additionalInformation);
        return user;
    }

    protected io.gravitee.am.identityprovider.api.User convert(User user) {
        DefaultUser idpUser = new DefaultUser(user.getUsername());
        idpUser.setCredentials(user.getPassword());

        Map<String, Object> additionalInformation = new HashMap<>();
        if (user.getFirstName() != null) {
            idpUser.setFirstName(user.getFirstName());
            additionalInformation.put(StandardClaims.GIVEN_NAME, user.getFirstName());
        }
        if (user.getLastName() != null) {
            idpUser.setLastName(user.getLastName());
            additionalInformation.put(StandardClaims.FAMILY_NAME, user.getLastName());
        }
        if (user.getEmail() != null) {
            idpUser.setEmail(user.getEmail());
            additionalInformation.put(StandardClaims.EMAIL, user.getEmail());
        }
        if (user.getAdditionalInformation() != null) {
            user.getAdditionalInformation().forEach(additionalInformation::putIfAbsent);
        }
        idpUser.setAdditionalInformation(additionalInformation);
        return idpUser;
    }

    protected User setInternalStatus(User user) {
        user.setInternal(identityProviderManager.userProviderExists(user.getSource()));
        return user;
    }
}

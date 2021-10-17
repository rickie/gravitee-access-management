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
package io.gravitee.am.service.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.event.Action;
import io.gravitee.am.common.event.Type;
import io.gravitee.am.common.utils.RandomString;

import io.gravitee.am.model.Group;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.CommonUserRepository;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.gravitee.am.service.utils.UserFactorUpdater;
import io.gravitee.am.service.validators.UserValidator;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractUserService<T extends CommonUserRepository> implements CommonUserService {

    protected final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected UserValidator userValidator;

    @Autowired
    protected EventService eventService;

    @Autowired
    protected CredentialService credentialService;

    @Autowired
    protected RoleService roleService;

    @Autowired
    protected GroupService groupService;

    protected abstract T getUserRepository();

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<User> findByIdIn(List<String> ids) {
 return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
@Override
    public Flux<User> findByIdIn_migrated(List<String> ids) {
        String userIds = String.join(",", ids);
        LOGGER.debug("Find users by ids: {}", userIds);
        return getUserRepository().findByIdIn_migrated(ids).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find users by ids {}", userIds, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error occurs while trying to find users by ids %s", userIds), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(referenceType, referenceId, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<User>> findAll(ReferenceType referenceType, String referenceId, int page, int size) {
 return RxJava2Adapter.monoToSingle(findAll_migrated(referenceType, referenceId, page, size));
}
@Override
    public Mono<Page<User>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size) {
        LOGGER.debug("Find users by {}: {}", referenceType, referenceId);
        return getUserRepository().findAll_migrated(referenceType, referenceId, page, size).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Page<User>>>toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find users by {} {}", referenceType, referenceId, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find users by %s %s", referenceType, referenceId), ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<User>> search(ReferenceType referenceType, String referenceId, String query, int page, int size) {
 return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, query, page, size));
}
@Override
    public Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, String query, int page, int size) {
        LOGGER.debug("Search users for {} {} with query {}", referenceType, referenceId, query);
        return getUserRepository().search_migrated(referenceType, referenceId, query, page, size).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Page<User>>>toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to search users for {} {} and query {}", referenceType, referenceId, query, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find users for %s %s and query %s", referenceType, referenceId, query), ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, filterCriteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<User>> search(ReferenceType referenceType, String referenceId, FilterCriteria filterCriteria, int page, int size) {
 return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, filterCriteria, page, size));
}
@Override
    public Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, FilterCriteria filterCriteria, int page, int size) {
        LOGGER.debug("Search users for {} {} with filter {}", referenceType, referenceId, filterCriteria);
        return getUserRepository().search_migrated(referenceType, referenceId, filterCriteria, page, size).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Page<User>>>toJdkFunction(ex -> {
                    if (ex instanceof IllegalArgumentException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidParameterException(ex.getMessage())));
                    }
                    LOGGER.error("An error occurs while trying to search users for {} {} and filter {}", referenceType, referenceId, filterCriteria, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find users for %s %s and filter %s", referenceType, referenceId, filterCriteria), ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> findById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<User> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
        LOGGER.debug("Find user by id : {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(getUserRepository().findById_migrated(referenceType, referenceId, id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a user using its ID {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using its ID: %s", id), ex)));
                })).switchIfEmpty(Mono.error(new UserNotFoundException(id)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsernameAndSource_migrated(referenceType, referenceId, username, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> findByUsernameAndSource(ReferenceType referenceType, String referenceId, String username, String source) {
 return RxJava2Adapter.monoToMaybe(findByUsernameAndSource_migrated(referenceType, referenceId, username, source));
}
@Override
    public Mono<User> findByUsernameAndSource_migrated(ReferenceType referenceType, String referenceId, String username, String source) {
        LOGGER.debug("Find user by {} {}, username and source: {} {}", referenceType, referenceId, username, source);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(getUserRepository().findByUsernameAndSource_migrated(referenceType, referenceId, username, source))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a user using its username: {} for the {} {}  and source {}", username, referenceType, referenceId, source, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using its username: %s for the %s %s and source %s", username, referenceType, referenceId, source), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByExternalIdAndSource_migrated(referenceType, referenceId, externalId, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> findByExternalIdAndSource(ReferenceType referenceType, String referenceId, String externalId, String source) {
 return RxJava2Adapter.monoToMaybe(findByExternalIdAndSource_migrated(referenceType, referenceId, externalId, source));
}
@Override
    public Mono<User> findByExternalIdAndSource_migrated(ReferenceType referenceType, String referenceId, String externalId, String source) {
        LOGGER.debug("Find user by {} {}, externalId and source: {} {}", referenceType, referenceId, externalId, source);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(getUserRepository().findByExternalIdAndSource_migrated(referenceType, referenceId, externalId, source))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a user using its externalId: {} for the {} {} and source {}", externalId, referenceType, referenceId, source, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using its externalId: %s for the %s %s and source %s", externalId, referenceType, referenceId, source), ex)));
                }));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> create(ReferenceType referenceType, String referenceId, NewUser newUser) {
 return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newUser));
}
@Override
    public Mono<User> create_migrated(ReferenceType referenceType, String referenceId, NewUser newUser) {
        LOGGER.debug("Create a new user {} for {} {}", newUser, referenceType, referenceId);
        return getUserRepository().findByUsernameAndSource_migrated(referenceType, referenceId, newUser.getUsername(), newUser.getSource()).hasElement().flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, Single<User>>)isEmpty -> {
                    if (!isEmpty) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new UserAlreadyExistsException(newUser.getUsername())));
                    } else {
                        String userId = RandomString.generate();

                        User user = new User();
                        user.setId(userId);
                        user.setExternalId(newUser.getExternalId());
                        user.setReferenceType(referenceType);
                        user.setReferenceId(referenceId);
                        user.setClient(newUser.getClient());
                        user.setUsername(newUser.getUsername());
                        user.setFirstName(newUser.getFirstName());
                        user.setLastName(newUser.getLastName());
                        if (user.getFirstName() != null) {
                            user.setDisplayName(user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : ""));
                        }
                        user.setEmail(newUser.getEmail());
                        user.setSource(newUser.getSource());
                        user.setInternal(true);
                        user.setPreRegistration(newUser.isPreRegistration());
                        user.setRegistrationCompleted(newUser.isRegistrationCompleted());
                        user.setAdditionalInformation(newUser.getAdditionalInformation());
                        user.setCreatedAt(new Date());
                        user.setUpdatedAt(user.getCreatedAt());
                        return RxJava2Adapter.monoToSingle(create_migrated(user));
                    }
                }).apply(v))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<User>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    } else {
                        LOGGER.error("An error occurs while trying to create a user", ex);
                        return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a user", ex)));
                    }
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> create(User user) {
 return RxJava2Adapter.monoToSingle(create_migrated(user));
}
@Override
    public Mono<User> create_migrated(User user) {

        LOGGER.debug("Create a user {}", user);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(user.getCreatedAt());

        return userValidator.validate_migrated(user).then(getUserRepository().create_migrated(user)).flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<User, Single<User>>)user1 -> {
                    // create event for sync process
                    Event event = new Event(Type.USER, new Payload(user1.getId(), user1.getReferenceType(), user1.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(user1)));
                }).apply(v))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<User>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create a user", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a user", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> update(ReferenceType referenceType, String referenceId, String id, UpdateUser updateUser) {
 return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateUser));
}
@Override
    public Mono<User> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateUser updateUser) {
        LOGGER.debug("Update a user {} for {} {}", id, referenceType, referenceId);

        return getUserRepository().findById_migrated(referenceType, referenceId, id).switchIfEmpty(Mono.error(new UserNotFoundException(id))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<User, SingleSource<User>>toJdkFunction(oldUser -> {
                    User tmpUser = new User();
                    tmpUser.setEmail(updateUser.getEmail());
                    tmpUser.setAdditionalInformation(updateUser.getAdditionalInformation());
                    UserFactorUpdater.updateFactors(oldUser.getFactors(), oldUser, tmpUser);

                    oldUser.setClient(updateUser.getClient());
                    oldUser.setExternalId(updateUser.getExternalId());
                    oldUser.setFirstName(updateUser.getFirstName());
                    oldUser.setLastName(updateUser.getLastName());
                    oldUser.setDisplayName(updateUser.getDisplayName());
                    oldUser.setEmail(updateUser.getEmail());
                    oldUser.setEnabled(updateUser.isEnabled());
                    oldUser.setLoggedAt(updateUser.getLoggedAt());
                    oldUser.setLoginsCount(updateUser.getLoginsCount());
                    oldUser.setUpdatedAt(new Date());
                    oldUser.setAdditionalInformation(updateUser.getAdditionalInformation());

                    return RxJava2Adapter.monoToSingle(update_migrated(oldUser));
                }).apply(y)))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<User>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a user", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a user", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String userId) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(userId));
}
@Override
    public Mono<Void> delete_migrated(String userId) {
        LOGGER.debug("Delete user {}", userId);

        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(getUserRepository().findById_migrated(userId).switchIfEmpty(Mono.error(new UserNotFoundException(userId))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<User, CompletableSource>)user -> {
                    // create event for sync process
                    Event event = new Event(Type.USER, new Payload(user.getId(), user.getReferenceType(), user.getReferenceId(), Action.DELETE));
                    /// delete WebAuthn credentials
                    return RxJava2Adapter.monoToCompletable(credentialService.findByUserId_migrated(user.getReferenceType(), user.getReferenceId(), user.getId()).flatMap(v->credentialService.delete_migrated(v.getId())).then().then(getUserRepository().delete_migrated(userId)).then(eventService.create_migrated(event).then()));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete user: {}", userId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete user: %s", userId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enhance_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> enhance(User user) {
 return RxJava2Adapter.monoToSingle(enhance_migrated(user));
}
@Override
    public Mono<User> enhance_migrated(User user) {
        LOGGER.debug("Enhance user {}", user.getId());

        // fetch user groups
        return groupService.findByMember_migrated(user.getId()).collectList().flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Group>, Single<User>>)groups -> {
                    Set<String> roles = new HashSet<>();
                    if (groups != null && !groups.isEmpty()) {
                        // set groups
                        user.setGroups(groups.stream().map(Group::getName).collect(Collectors.toList()));
                        // set groups roles
                        roles.addAll(groups
                                .stream()
                                .filter(group -> group.getRoles() != null && !group.getRoles().isEmpty())
                                .flatMap(group -> group.getRoles().stream())
                                .collect(Collectors.toSet()));
                    }
                    // get user roles
                    if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                        roles.addAll(user.getRoles());
                    }
                    // fetch roles information and enhance user data
                    if (!roles.isEmpty()) {
                        return RxJava2Adapter.monoToSingle(roleService.findByIdIn_migrated(new ArrayList<>(roles)).map(RxJavaReactorMigrationUtil.toJdkFunction(roles1 -> {
                                    user.setRolesPermissions(roles1);
                                    return user;
                                })));

                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(user));
                }).apply(v))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<User>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to enhance user {}", user.getId(), ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to enhance user %s", user.getId()), ex)));
                }).apply(err)));
    }

}

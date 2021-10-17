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
package io.gravitee.am.gateway.handler.scim.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.common.scim.filter.Filter;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.gateway.handler.common.auth.idp.IdentityProviderManager;
import io.gravitee.am.gateway.handler.scim.exception.InvalidValueException;
import io.gravitee.am.gateway.handler.scim.exception.SCIMException;
import io.gravitee.am.gateway.handler.scim.exception.UniquenessException;
import io.gravitee.am.gateway.handler.scim.model.*;
import io.gravitee.am.gateway.handler.scim.model.ListResponse;
import io.gravitee.am.gateway.handler.scim.model.User;
import io.gravitee.am.gateway.handler.scim.service.GroupService;
import io.gravitee.am.gateway.handler.scim.service.UserService;
import io.gravitee.am.identityprovider.api.DefaultUser;

import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.management.api.UserRepository;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.utils.UserFactorUpdater;
import io.gravitee.am.service.validators.PasswordValidator;
import io.gravitee.am.service.validators.UserValidator;
import io.reactivex.Completable;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.*;
import java.util.stream.Collectors;
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
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String DEFAULT_IDP_PREFIX = "default-idp-";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private Domain domain;

    @Autowired
    private IdentityProviderManager identityProviderManager;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordValidator passwordValidator;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.list_migrated(filter, page, size, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ListResponse<User>> list(Filter filter, int page, int size, String baseUrl) {
 return RxJava2Adapter.monoToSingle(list_migrated(filter, page, size, baseUrl));
}
@Override
    public Mono<ListResponse<User>> list_migrated(Filter filter, int page, int size, String baseUrl) {
        LOGGER.debug("Find users by domain: {}", domain.getId());
        Single<Page<io.gravitee.am.model.User>> findUsers = filter != null ?
                RxJava2Adapter.monoToSingle(userRepository.search_migrated(ReferenceType.DOMAIN, domain.getId(), FilterCriteria.convert(filter), page, size)) :
                RxJava2Adapter.monoToSingle(userRepository.findAll_migrated(ReferenceType.DOMAIN, domain.getId(), page, size));

        return RxJava2Adapter.singleToMono(findUsers).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Page<io.gravitee.am.model.User>, SingleSource<ListResponse<io.gravitee.am.gateway.handler.scim.model.User>>>toJdkFunction(userPage -> {
                    // A negative value SHALL be interpreted as "0".
                    // A value of "0" indicates that no resource results are to be returned except for "totalResults".
                    if (size <= 0) {
                        return RxJava2Adapter.monoToSingle(Mono.just(new ListResponse<User>(null, userPage.getCurrentPage() + 1, userPage.getTotalCount(), 0)));
                    } else {
                        // SCIM use 1-based index (increment current page)
                        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Observable.fromIterable(userPage.getData())
                                .map(user1 -> convert(user1, baseUrl, true))
                                // set groups
                                .flatMapSingle((io.gravitee.am.gateway.handler.scim.model.User ident) -> RxJava2Adapter.monoToSingle(setGroups_migrated(ident)))
                                .toList()).map(RxJavaReactorMigrationUtil.toJdkFunction(users -> new ListResponse<>(users, userPage.getCurrentPage() + 1, userPage.getTotalCount(), users.size()))));
                    }
                }).apply(v)))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<ListResponse<User>>>toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find users for the security domain {}", domain.getName(), ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find users the security domain %s", domain.getName()), ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.get_migrated(userId, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> get(String userId, String baseUrl) {
 return RxJava2Adapter.monoToMaybe(get_migrated(userId, baseUrl));
}
@Override
    public Mono<User> get_migrated(String userId, String baseUrl) {
        LOGGER.debug("Find user by id : {}", userId);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(userRepository.findById_migrated(userId).map(RxJavaReactorMigrationUtil.toJdkFunction(user1 -> convert(user1, baseUrl, false))).flatMap(this::setGroups_migrated))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a user using its ID {}", userId, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a user using its ID: %s", userId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> create(User user, String baseUrl) {
 return RxJava2Adapter.monoToSingle(create_migrated(user, baseUrl));
}
@Override
    public Mono<User> create_migrated(User user, String baseUrl) {
        LOGGER.debug("Create a new user {} for domain {}", user.getUserName(), domain.getName());

        // set user idp source
        final String source = user.getSource() == null ? DEFAULT_IDP_PREFIX + domain.getId() : user.getSource();

        // check password
        if (isInvalidUserPassword(user)) {
            return Mono.error(new InvalidValueException("Field [password] is invalid"));
        }

        // check if user is unique
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(userRepository.findByUsernameAndSource_migrated(ReferenceType.DOMAIN, domain.getId(), user.getUserName(), source).hasElement().map(RxJavaReactorMigrationUtil.toJdkFunction(isEmpty -> {
                    if (!isEmpty) {
                        throw new UniquenessException("User with username [" + user.getUserName() + "] already exists");
                    }
                    return true;
                })).flatMap(__->checkRoles_migrated(user.getRoles())).then().then(Mono.defer(()->identityProviderManager.getUserProvider_migrated(source))).switchIfEmpty(Mono.error(new UserProviderNotFoundException(source))))
                .flatMapSingle(userProvider -> {
                    io.gravitee.am.model.User userModel = convert(user);
                    // set technical ID
                    userModel.setId(RandomString.generate());
                    userModel.setReferenceType(ReferenceType.DOMAIN);
                    userModel.setReferenceId(domain.getId());
                    userModel.setSource(source);
                    userModel.setInternal(true);
                    userModel.setCreatedAt(new Date());
                    userModel.setUpdatedAt(userModel.getCreatedAt());
                    userModel.setEnabled(userModel.getPassword() != null);

                    // store user in its identity provider
                    return RxJava2Adapter.monoToSingle(userValidator.validate_migrated(userModel).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userProvider.create_migrated(convert(userModel)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.identityprovider.api.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(idpUser -> {
                                // AM 'users' collection is not made for authentication (but only management stuff)
                                // clear password
                                userModel.setPassword(null);
                                // set external id
                                userModel.setExternalId(idpUser.getId());
                                return RxJava2Adapter.monoToSingle(userRepository.create_migrated(userModel));
                            }).apply(v)))))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<io.gravitee.am.model.User>>toJdkFunction(ex -> {
                                if (ex instanceof UserAlreadyExistsException) {
                                    return RxJava2Adapter.monoToSingle(Mono.error(new UniquenessException("User with username [" + user.getUserName() + "] already exists")));
                                }
                                return RxJava2Adapter.monoToSingle(Mono.error(ex));
                            }).apply(err)))))));
                })).map(RxJavaReactorMigrationUtil.toJdkFunction(user1 -> convert(user1, baseUrl, true))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<User>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractNotFoundException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidValueException(ex.getMessage())));
                    }

                    if (ex instanceof SCIMException || ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a user", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a user", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(userId, user, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> update(String userId, User user, String baseUrl) {
 return RxJava2Adapter.monoToSingle(update_migrated(userId, user, baseUrl));
}
@Override
    public Mono<User> update_migrated(String userId, User user, String baseUrl) {
        LOGGER.debug("Update a user {} for domain {}", user.getUserName(), domain.getName());

        // check password
        if (isInvalidUserPassword(user)) {
            return Mono.error(new InvalidValueException("Field [password] is invalid"));
        }

        return userRepository.findById_migrated(userId).switchIfEmpty(Mono.error(new UserNotFoundException(userId))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(existingUser -> {
                    // check roles
                    return RxJava2Adapter.monoToSingle(checkRoles_migrated(user.getRoles()).then(RxJava2Adapter.singleToMono(Single.defer(() -> {
                                io.gravitee.am.model.User userToUpdate = convert(user);
                                // set immutable attribute
                                userToUpdate.setId(existingUser.getId());
                                userToUpdate.setExternalId(existingUser.getExternalId());
                                userToUpdate.setUsername(existingUser.getUsername());
                                userToUpdate.setReferenceType(existingUser.getReferenceType());
                                userToUpdate.setReferenceId(existingUser.getReferenceId());
                                userToUpdate.setSource(existingUser.getSource());
                                userToUpdate.setCreatedAt(existingUser.getCreatedAt());
                                userToUpdate.setUpdatedAt(new Date());
                                userToUpdate.setFactors(existingUser.getFactors());

                                UserFactorUpdater.updateFactors(existingUser.getFactors(), existingUser, userToUpdate);

                                return RxJava2Adapter.monoToSingle(userValidator.validate_migrated(userToUpdate).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(identityProviderManager.getUserProvider_migrated(userToUpdate.getSource()).switchIfEmpty(Mono.error(new UserProviderNotFoundException(userToUpdate.getSource()))))
                                        .flatMapSingle(userProvider -> {
                                            // no idp user check if we need to create it
                                            if (userToUpdate.getExternalId() == null) {
                                                return RxJava2Adapter.monoToSingle(userProvider.create_migrated(convert(userToUpdate)));
                                            } else {
                                                return RxJava2Adapter.monoToSingle(userProvider.update_migrated(userToUpdate.getExternalId(), convert(userToUpdate)));
                                            }
                                        })).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.identityprovider.api.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(idpUser -> {
                                            // AM 'users' collection is not made for authentication (but only management stuff)
                                            // clear password
                                            userToUpdate.setPassword(null);
                                            // set external id
                                            userToUpdate.setExternalId(idpUser.getId());
                                            // if password has been changed, update last update date
                                            if (user.getPassword() != null) {
                                                userToUpdate.setLastPasswordReset(new Date());
                                            }
                                            return RxJava2Adapter.monoToSingle(userRepository.update_migrated(userToUpdate));
                                        }).apply(v)))))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<io.gravitee.am.model.User>>toJdkFunction(ex -> {
                                            if (ex instanceof UserNotFoundException || ex instanceof UserInvalidException) {
                                                // idp user does not exist, only update AM user
                                                // clear password
                                                userToUpdate.setPassword(null);
                                                return RxJava2Adapter.monoToSingle(userRepository.update_migrated(userToUpdate));
                                            }
                                            return RxJava2Adapter.monoToSingle(Mono.error(ex));
                                        }).apply(err)))))));
                            }))));
                }).apply(y)))).map(RxJavaReactorMigrationUtil.toJdkFunction(user1 -> convert(user1, baseUrl, false))).flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<User, Single<User>>)(io.gravitee.am.gateway.handler.scim.model.User ident) -> RxJava2Adapter.monoToSingle(setGroups_migrated(ident))).apply(v))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<User>>toJdkFunction(ex -> {
                    if (ex instanceof SCIMException || ex instanceof UserNotFoundException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    if (ex instanceof AbstractNotFoundException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidValueException(ex.getMessage())));
                    }

                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a user", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a user", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(userId, patchOp, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> patch(String userId, PatchOp patchOp, String baseUrl) {
 return RxJava2Adapter.monoToSingle(patch_migrated(userId, patchOp, baseUrl));
}
@Override
    public Mono<User> patch_migrated(String userId, PatchOp patchOp, String baseUrl) {
        LOGGER.debug("Patch user {}", userId);
        return get_migrated(userId, baseUrl).switchIfEmpty(Mono.error(new UserNotFoundException(userId))).flatMap(v->RxJava2Adapter.singleToMono((Single<User>)RxJavaReactorMigrationUtil.toJdkFunction((Function<User, Single<User>>)user -> {
                    ObjectNode node = objectMapper.convertValue(user, ObjectNode.class);
                    patchOp.getOperations().forEach(operation -> operation.apply(node));
                    User userToPatch = objectMapper.treeToValue(node, User.class);

                    // check password
                    if (isInvalidUserPassword(userToPatch)) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidValueException("Field [password] is invalid")));
                    }

                    return RxJava2Adapter.monoToSingle(update_migrated(userId, userToPatch, baseUrl));
                }).apply(v))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<User>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    } else {
                        LOGGER.error("An error has occurred when trying to patch user: {}", userId, ex);
                        return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                                String.format("An error has occurred when trying to patch user: %s", userId), ex)));
                    }
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
        return userRepository.findById_migrated(userId).switchIfEmpty(Mono.error(new UserNotFoundException(userId))).flatMap(user->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(identityProviderManager.getUserProvider_migrated(user.getSource()).switchIfEmpty(Mono.error(new UserProviderNotFoundException(user.getSource()))).flatMap(userProvider->userProvider.delete_migrated(user.getExternalId())).then(userRepository.delete_migrated(userId))).onErrorResumeNext((java.lang.Throwable ex)->{
if (ex instanceof UserNotFoundException) {
return RxJava2Adapter.monoToCompletable(userRepository.delete_migrated(userId));
}
return RxJava2Adapter.monoToCompletable(Mono.error(ex));
}).onErrorResumeNext((java.lang.Throwable ex)->{
if (ex instanceof AbstractManagementException) {
return RxJava2Adapter.monoToCompletable(Mono.error(ex));
} else {
LOGGER.error("An error occurs while trying to delete user: {}", userId, ex);
return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to delete user: %s", userId), ex)));
}
}))).then();
    }

    private boolean isInvalidUserPassword(User user) {
        String password = user.getPassword();
        if (password == null) {
            return false;
        }
        return Optional.ofNullable(domain.getPasswordSettings())
                .map(ps -> !passwordValidator.isValid(password, ps))
                .orElseGet(() -> !passwordValidator.isValid(password));
    }

    
private Mono<User> setGroups_migrated(User scimUser) {
        // fetch groups
        return groupService.findByMember_migrated(scimUser.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(group -> {
                    Member member = new Member();
                    member.setValue(group.getId());
                    member.setDisplay(group.getDisplayName());
                    return member;
                })).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(scimGroups -> {
                    if (!scimGroups.isEmpty()) {
                        scimUser.setGroups(scimGroups);
                        return scimUser;
                    } else {
                        return scimUser;
                    }
                }));
    }

    
private Mono<Void> checkRoles_migrated(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Mono.empty();
        }

        return roleService.findByIdIn_migrated(roles).map(RxJavaReactorMigrationUtil.toJdkFunction(roles1 -> {
                    if (roles1.size() != roles.size()) {
                        // find difference between the two list
                        roles.removeAll(roles1.stream().map(Role::getId).collect(Collectors.toList()));
                        throw new RoleNotFoundException(String.join(",", roles));
                    }
                    return roles1;
                })).then();
    }

    private User convert(io.gravitee.am.model.User user, String baseUrl, boolean listing) {
        Map<String, Object> additionalInformation = user.getAdditionalInformation() != null ? user.getAdditionalInformation() : Collections.emptyMap();

        User scimUser = new User();
        scimUser.setSchemas(User.SCHEMAS);
        scimUser.setId(user.getId());
        scimUser.setExternalId(user.getExternalId());
        scimUser.setUserName(user.getUsername());

        Name name = new Name();
        name.setGivenName(user.getFirstName());
        name.setFamilyName(user.getLastName());
        name.setMiddleName(get(additionalInformation, StandardClaims.MIDDLE_NAME, String.class));
        scimUser.setName(name.isNull() ? null : name);
        scimUser.setDisplayName(user.getDisplayName());
        scimUser.setNickName(user.getNickName());

        scimUser.setProfileUrl(get(additionalInformation, StandardClaims.PROFILE, String.class));
        scimUser.setTitle(user.getTitle());
        scimUser.setUserType(user.getType());
        scimUser.setPreferredLanguage(user.getPreferredLanguage());
        scimUser.setLocale(get(additionalInformation, StandardClaims.LOCALE, String.class));
        scimUser.setTimezone(get(additionalInformation, StandardClaims.ZONEINFO, String.class));
        scimUser.setActive(user.isEnabled());
        scimUser.setEmails(toScimAttributes(user.getEmails()));
        // set primary email
        if (user.getEmail() != null) {
            Attribute attribute = new Attribute();
            attribute.setValue(user.getEmail());
            attribute.setPrimary(true);
            if (scimUser.getEmails() != null) {
                Optional<Attribute> optional = scimUser.getEmails().stream().filter(attribute1 -> attribute1.getValue().equals(attribute.getValue())).findFirst();
                if (!optional.isPresent()) {
                    scimUser.setEmails(Collections.singletonList(attribute));
                }
            } else {
                scimUser.setEmails(Collections.singletonList(attribute));
            }
        }
        scimUser.setPhoneNumbers(toScimAttributes(user.getPhoneNumbers()));
        scimUser.setIms(toScimAttributes(user.getIms()));
        scimUser.setPhotos(toScimAttributes(user.getPhotos()));
        scimUser.setAddresses(toScimAddresses(user.getAddresses()));
        scimUser.setEntitlements(user.getEntitlements());
        scimUser.setRoles(user.getRoles());
        scimUser.setX509Certificates(toScimCertificates(user.getX509Certificates()));

        // Meta
        Meta meta = new Meta();
        if (user.getCreatedAt() != null) {
            meta.setCreated(user.getCreatedAt().toInstant().toString());
        }
        if (user.getUpdatedAt() != null) {
            meta.setLastModified(user.getUpdatedAt().toInstant().toString());
        }
        meta.setResourceType(User.RESOURCE_TYPE);
        meta.setLocation(baseUrl + (listing ? "/" + scimUser.getId() : ""));
        scimUser.setMeta(meta);
        return scimUser;
    }

    private io.gravitee.am.model.User convert(User scimUser) {
        io.gravitee.am.model.User user = new io.gravitee.am.model.User();
        Map<String, Object> additionalInformation = new HashMap();
        if (scimUser.getExternalId() != null) {
            user.setExternalId(scimUser.getExternalId());
            additionalInformation.put(StandardClaims.SUB, scimUser.getExternalId());
        }
        user.setUsername(scimUser.getUserName());
        if (scimUser.getName() != null) {
            Name name = scimUser.getName();
            if (name.getGivenName() != null) {
                user.setFirstName(name.getGivenName());
                additionalInformation.put(StandardClaims.GIVEN_NAME, name.getGivenName());
            }
            if (name.getFamilyName() != null) {
                user.setLastName(name.getFamilyName());
                additionalInformation.put(StandardClaims.FAMILY_NAME, name.getFamilyName());
            }
            if (name.getMiddleName() != null) {
                additionalInformation.put(StandardClaims.MIDDLE_NAME, name.getMiddleName());
            }
        }
        user.setDisplayName(scimUser.getDisplayName());
        user.setNickName(scimUser.getNickName());
        if (scimUser.getProfileUrl() != null) {
            additionalInformation.put(StandardClaims.PROFILE, scimUser.getProfileUrl());
        }
        user.setTitle(scimUser.getTitle());
        user.setType(scimUser.getUserType());
        user.setPreferredLanguage(scimUser.getPreferredLanguage());
        if (scimUser.getLocale() != null) {
            additionalInformation.put(StandardClaims.LOCALE, scimUser.getLocale());
        }
        if (scimUser.getTimezone() != null) {
            additionalInformation.put(StandardClaims.ZONEINFO, scimUser.getTimezone());
        }
        user.setEnabled(scimUser.isActive());
        user.setPassword(scimUser.getPassword());
        if (scimUser.getEmails() != null && !scimUser.getEmails().isEmpty()) {
            List<Attribute> emails = scimUser.getEmails();
            user.setEmail(emails.stream().filter(attribute -> Boolean.TRUE.equals(attribute.isPrimary())).findFirst().orElse(emails.get(0)).getValue());
            user.setEmails(toModelAttributes(emails));
        }
        user.setPhoneNumbers(toModelAttributes(scimUser.getPhoneNumbers()));
        user.setIms(toModelAttributes(scimUser.getIms()));
        if (scimUser.getPhotos() != null && !scimUser.getPhotos().isEmpty()) {
            List<Attribute> photos = scimUser.getPhotos();
            additionalInformation.put(StandardClaims.PICTURE, photos.stream().filter(attribute -> Boolean.TRUE.equals(attribute.isPrimary())).findFirst().orElse(photos.get(0)).getValue());
            user.setPhotos(toModelAttributes(photos));
        }
        user.setAddresses(toModelAddresses(scimUser.getAddresses()));
        user.setEntitlements(scimUser.getEntitlements());
        user.setRoles(scimUser.getRoles());
        user.setX509Certificates(toModelCertificates(scimUser.getX509Certificates()));

        // set additional information
        user.setAdditionalInformation(additionalInformation);
        return user;
    }

    private <T> T get(Map<String, Object> additionalInformation, String key, Class<T> valueType) {
        if (!additionalInformation.containsKey(key)) {
            return null;
        }
        try {
            return (T) additionalInformation.get(key);
        } catch (ClassCastException e) {
            LOGGER.debug("An error occurs while retrieving {} information from user", key, e);
            return null;
        }
    }

    private io.gravitee.am.identityprovider.api.User convert(io.gravitee.am.model.User user) {
        DefaultUser idpUser = new DefaultUser(user.getUsername());
        idpUser.setId(user.getExternalId());
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

    private List<io.gravitee.am.model.scim.Attribute> toModelAttributes(List<Attribute> scimAttributes) {
        if (scimAttributes == null) {
            return null;
        }
        return scimAttributes
                .stream()
                .map(scimAttribute -> {
                    io.gravitee.am.model.scim.Attribute modelAttribute = new io.gravitee.am.model.scim.Attribute();
                    modelAttribute.setPrimary(scimAttribute.isPrimary());
                    modelAttribute.setValue(scimAttribute.getValue());
                    modelAttribute.setType(scimAttribute.getType());
                    return modelAttribute;
                }).collect(Collectors.toList());
    }

    private List<Attribute> toScimAttributes(List<io.gravitee.am.model.scim.Attribute> modelAttributes) {
        if (modelAttributes == null) {
            return null;
        }
        return modelAttributes
                .stream()
                .map(modelAttribute -> {
                    Attribute scimAttribute = new Attribute();
                    scimAttribute.setPrimary(modelAttribute.isPrimary());
                    scimAttribute.setValue(modelAttribute.getValue());
                    scimAttribute.setType(modelAttribute.getType());
                    return scimAttribute;
                }).collect(Collectors.toList());
    }

    private List<io.gravitee.am.model.scim.Address> toModelAddresses(List<Address> scimAddresses) {
        if (scimAddresses == null) {
            return null;
        }
        return scimAddresses
                .stream()
                .map(scimAddress -> {
                    io.gravitee.am.model.scim.Address modelAddress = new io.gravitee.am.model.scim.Address();
                    modelAddress.setType(scimAddress.getType());
                    modelAddress.setFormatted(scimAddress.getFormatted());
                    modelAddress.setStreetAddress(scimAddress.getStreetAddress());
                    modelAddress.setCountry(scimAddress.getCountry());
                    modelAddress.setLocality(scimAddress.getLocality());
                    modelAddress.setPostalCode(scimAddress.getPostalCode());
                    modelAddress.setRegion(scimAddress.getRegion());
                    modelAddress.setPrimary(scimAddress.isPrimary());
                    return modelAddress;
                }).collect(Collectors.toList());
    }

    private List<Address> toScimAddresses(List<io.gravitee.am.model.scim.Address> modelAddresses) {
        if (modelAddresses == null) {
            return null;
        }
        return modelAddresses
                .stream()
                .map(modelAddress -> {
                    Address scimAddress = new Address();
                    scimAddress.setType(modelAddress.getType());
                    scimAddress.setFormatted(modelAddress.getFormatted());
                    scimAddress.setStreetAddress(modelAddress.getStreetAddress());
                    scimAddress.setCountry(modelAddress.getCountry());
                    scimAddress.setLocality(modelAddress.getLocality());
                    scimAddress.setPostalCode(modelAddress.getPostalCode());
                    scimAddress.setRegion(modelAddress.getRegion());
                    scimAddress.setPrimary(modelAddress.isPrimary());
                    return scimAddress;
                }).collect(Collectors.toList());
    }

    private List<io.gravitee.am.model.scim.Certificate> toModelCertificates(List<Certificate> scimCertificates) {
        if (scimCertificates == null) {
            return null;
        }
        return scimCertificates
                .stream()
                .map(scimCertificate -> {
                    io.gravitee.am.model.scim.Certificate modelCertificate = new io.gravitee.am.model.scim.Certificate();
                    modelCertificate.setValue(scimCertificate.getValue());
                    return modelCertificate;
                }).collect(Collectors.toList());
    }

    private List<Certificate> toScimCertificates(List<io.gravitee.am.model.scim.Certificate> modelCertificates) {
        if (modelCertificates == null) {
            return null;
        }
        return modelCertificates
                .stream()
                .map(modelCertificate -> {
                    Certificate scimCertificate = new Certificate();
                    scimCertificate.setValue(modelCertificate.getValue());
                    return scimCertificate;
                }).collect(Collectors.toList());
    }
}

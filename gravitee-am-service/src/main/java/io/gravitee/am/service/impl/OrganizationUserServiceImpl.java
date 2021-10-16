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
import io.gravitee.am.model.*;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.repository.management.api.OrganizationUserRepository;
import io.gravitee.am.service.GroupService;
import io.gravitee.am.service.MembershipService;
import io.gravitee.am.service.OrganizationUserService;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.RoleNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class OrganizationUserServiceImpl extends AbstractUserService implements OrganizationUserService {

    @Lazy
    @Autowired
    private OrganizationUserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private GroupService groupService;

    @Autowired
    protected MembershipService membershipService;

    @Override
    protected OrganizationUserRepository getUserRepository() {
        return this.userRepository;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.setRoles_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Completable setRoles(io.gravitee.am.model.User user) {
 return RxJava2Adapter.monoToCompletable(setRoles_migrated(user));
}
public Mono<Void> setRoles_migrated(io.gravitee.am.model.User user) {
        return setRoles_migrated(null, user);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.setRoles_migrated(principal, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Completable setRoles(io.gravitee.am.identityprovider.api.User principal, io.gravitee.am.model.User user) {
 return RxJava2Adapter.monoToCompletable(setRoles_migrated(principal, user));
}
public Mono<Void> setRoles_migrated(io.gravitee.am.identityprovider.api.User principal, io.gravitee.am.model.User user) {

        final Maybe<Role> defaultRoleObs = RxJava2Adapter.monoToMaybe(roleService.findDefaultRole_migrated(user.getReferenceId(), DefaultRole.ORGANIZATION_USER, ReferenceType.ORGANIZATION));
        Maybe<Role> roleObs = defaultRoleObs;

        if (principal != null && principal.getRoles() != null && !principal.getRoles().isEmpty()) {
            // We allow only one role in AM portal. Get the first (should not append).
            String roleId = principal.getRoles().get(0);

            roleObs = RxJava2Adapter.monoToMaybe(roleService.findById_migrated(user.getReferenceType(), user.getReferenceId(), roleId))
                    .onErrorResumeNext(throwable -> {
                        if (throwable instanceof RoleNotFoundException) {
                            return RxJava2Adapter.monoToMaybe(roleService.findById_migrated(ReferenceType.PLATFORM, Platform.DEFAULT, roleId).switchIfEmpty(RxJava2Adapter.maybeToMono(defaultRoleObs)))
                                    .onErrorResumeNext(defaultRoleObs);
                        } else {
                            return defaultRoleObs;
                        }
                    });
        }

        Membership membership = new Membership();
        membership.setMemberType(MemberType.USER);
        membership.setMemberId(user.getId());
        membership.setReferenceType(user.getReferenceType());
        membership.setReferenceId(user.getReferenceId());

        return RxJava2Adapter.maybeToMono(roleObs).switchIfEmpty(Mono.error(new TechnicalManagementException(String.format("Cannot add user membership to organization %s. Unable to find ORGANIZATION_USER role", user.getReferenceId())))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Role, CompletableSource>)role -> {
                    membership.setRoleId(role.getId());
                    return RxJava2Adapter.monoToCompletable(membershipService.addOrUpdate_migrated(user.getReferenceId(), membership).then());
                }).apply(y)))).then();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> update(User user) {
 return RxJava2Adapter.monoToSingle(update_migrated(user));
}
@Override
    public Mono<User> update_migrated(User user) {
        LOGGER.debug("Update a user {}", user);
        // updated date
        user.setUpdatedAt(new Date());
        return userValidator.validate_migrated(user).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(getUserRepository().findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, user.getReferenceId(), user.getUsername(), user.getSource()))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<User, SingleSource<User>>toJdkFunction(oldUser -> {

                        user.setId(oldUser.getId());
                        user.setReferenceType(oldUser.getReferenceType());
                        user.setReferenceId(oldUser.getReferenceId());
                        user.setUsername(oldUser.getUsername());
                        if (user.getFirstName() != null) {
                            user.setDisplayName(user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : ""));
                        }
                        user.setSource(oldUser.getSource());
                        user.setInternal(oldUser.isInternal());
                        user.setUpdatedAt(new Date());
                        if (user.getLoginsCount() < oldUser.getLoginsCount()) {
                            user.setLoggedAt(oldUser.getLoggedAt());
                            user.setLoginsCount(oldUser.getLoginsCount());
                        }

                        return RxJava2Adapter.monoToSingle(getUserRepository().update_migrated(user));
                }).apply(y)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(user1 -> {
                    // create event for sync process
                    Event event = new Event(Type.USER, new Payload(user1.getId(), user1.getReferenceType(), user1.getReferenceId(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(user1)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a user", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a user", ex)));
                })));
    }
}

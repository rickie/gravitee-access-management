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
package io.gravitee.am.management.handlers.management.api.authentication.service.impl;


import io.gravitee.am.common.jwt.Claims;
import io.gravitee.am.common.oidc.CustomClaims;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.SimpleAuthenticationContext;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.authentication.provider.security.EndUserAuthentication;
import io.gravitee.am.management.handlers.management.api.authentication.service.AuthenticationService;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.Organization;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.MembershipService;
import io.gravitee.am.service.OrganizationUserService;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.AuthenticationAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AuthenticationServiceImpl implements AuthenticationService {

    /**
     * Constant to use while setting identity provider used to authenticate a user
     */
    public static final String SOURCE = "source";

    @Autowired
    private OrganizationUserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private AuditService auditService;

    @Override
    public User onAuthenticationSuccess(Authentication auth) {
        final DefaultUser principal = (DefaultUser) auth.getPrincipal();

        final EndUserAuthentication authentication = new EndUserAuthentication(principal.getUsername(), null, new SimpleAuthenticationContext());
        Map<String, String> details = auth.getDetails() == null ? new HashMap<>() : new HashMap<>((Map<String, String>) auth.getDetails());

        details.putIfAbsent(Claims.organization, Organization.DEFAULT);

        String organizationId = details.get(Claims.organization);

        final String source = details.get(SOURCE);
        io.gravitee.am.model.User endUser = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, organizationId, principal.getId(), source).switchIfEmpty(Mono.defer(()->userService.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, organizationId, principal.getUsername(), source))).switchIfEmpty(Mono.error(new UserNotFoundException(principal.getUsername()))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(existingUser -> {
                    existingUser.setSource(details.get(SOURCE));
                    existingUser.setLoggedAt(new Date());
                    existingUser.setLoginsCount(existingUser.getLoginsCount() + 1);
                    if (existingUser.getAdditionalInformation() != null) {
                        existingUser.getAdditionalInformation().putAll(principal.getAdditionalInformation());
                    } else {
                        existingUser.setAdditionalInformation(new HashMap<>(principal.getAdditionalInformation()));
                    }
                    return RxJava2Adapter.monoToSingle(userService.update_migrated(existingUser).flatMap(user->updateRoles_migrated(principal, existingUser).then(Mono.just(user))));
                }).apply(y)))))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<io.gravitee.am.model.User>>toJdkFunction(ex -> {
                    if (ex instanceof UserNotFoundException) {
                        final io.gravitee.am.model.User newUser = new io.gravitee.am.model.User();
                        newUser.setInternal(false);
                        newUser.setExternalId(principal.getId());
                        newUser.setUsername(principal.getUsername());
                        newUser.setSource(details.get(SOURCE));
                        newUser.setReferenceType(ReferenceType.ORGANIZATION);
                        newUser.setReferenceId(organizationId);
                        newUser.setLoggedAt(new Date());
                        newUser.setLoginsCount(1l);
                        newUser.setAdditionalInformation(principal.getAdditionalInformation());
                        return RxJava2Adapter.monoToSingle(userService.create_migrated(newUser).flatMap(user->userService.setRoles_migrated(principal, user).then(Mono.just(user))));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.error(ex));
                }).apply(err))).flatMap(userService::enhance_migrated).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(user -> auditService.report(AuditBuilder.builder(AuthenticationAuditBuilder.class).principal(authentication).referenceType(ReferenceType.ORGANIZATION)
                        .referenceId(organizationId).user(user)))).block();

        principal.setId(endUser.getId());
        principal.setUsername(endUser.getUsername());

        if (endUser.getAdditionalInformation()!= null) {
            principal.getAdditionalInformation().putAll(endUser.getAdditionalInformation());
        }

        principal.getAdditionalInformation().put(StandardClaims.SUB, endUser.getId());
        principal.getAdditionalInformation().put(StandardClaims.PREFERRED_USERNAME, endUser.getUsername());
        principal.getAdditionalInformation().put(Claims.organization, endUser.getReferenceId());
        principal.getAdditionalInformation().put("login_count", endUser.getLoginsCount());
        principal.getAdditionalInformation().computeIfAbsent(StandardClaims.EMAIL, val -> endUser.getEmail());
        principal.getAdditionalInformation().computeIfAbsent(StandardClaims.NAME, val -> endUser.getDisplayName());

        // set roles
        Set<String> roles = endUser.getRoles() != null ? new HashSet<>(endUser.getRoles()) : new HashSet<>();
        if (principal.getRoles() != null) {
            roles.addAll(principal.getRoles());
        }

        principal.getAdditionalInformation().put(CustomClaims.ROLES, roles);

        return principal;
    }


    
private Mono<Void> updateRoles_migrated(User principal, io.gravitee.am.model.User existingUser) {
        // no role defined, continue
        if (principal.getRoles() == null || principal.getRoles().isEmpty()) {
            return Mono.empty();
        }

        // role to update if it's different from the current one
        final String roleId = principal.getRoles().get(0);

        // update membership if necessary
        return membershipService.findByMember_migrated(existingUser.getId(), MemberType.USER).filter(RxJavaReactorMigrationUtil.toJdkPredicate(membership -> ReferenceType.ORGANIZATION == membership.getReferenceType())).next().map(RxJavaReactorMigrationUtil.toJdkFunction(membership -> !membership.getRoleId().equals(roleId))).switchIfEmpty(Mono.just(false)).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, CompletableSource>)mustChangeOrganizationRole -> {

                    if (!mustChangeOrganizationRole) {
                        return RxJava2Adapter.monoToCompletable(Mono.empty());
                    }

                    Membership membership = new Membership();
                    membership.setMemberType(MemberType.USER);
                    membership.setMemberId(existingUser.getId());
                    membership.setReferenceType(existingUser.getReferenceType());
                    membership.setReferenceId(existingUser.getReferenceId());
                    membership.setRoleId(roleId);

                    // check role and then update membership
                    return RxJava2Adapter.monoToCompletable(roleService.findById_migrated(existingUser.getReferenceType(), existingUser.getReferenceId(), roleId).flatMap(__->membershipService.addOrUpdate_migrated(existingUser.getReferenceId(), membership)).then());
                }).apply(y)))).then();
    }
}

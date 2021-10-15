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
package io.gravitee.am.management.handlers.management.api.resources;

import static io.gravitee.am.management.service.permissions.Permissions.of;
import static io.gravitee.am.management.service.permissions.Permissions.or;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.service.PermissionService;
import io.gravitee.am.management.service.permissions.PermissionAcls;
import io.gravitee.am.management.service.permissions.Permissions;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.permissions.Permission;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.util.*;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractResource {

    @Context
    protected SecurityContext securityContext;

    @Autowired
    protected PermissionService permissionService;

    protected User getAuthenticatedUser() {
        if (isAuthenticated()) {
            return (User) ((UsernamePasswordAuthenticationToken) securityContext.getUserPrincipal()).getPrincipal();
        }
        return null;
    }

    protected boolean isAuthenticated() {
        return securityContext.getUserPrincipal() != null;
    }

    protected Completable checkPermission(ReferenceType referenceType, String referenceId, Permission permission, Acl... acls) {

        return checkPermissions(getAuthenticatedUser(), Permissions.of(referenceType, referenceId, permission, acls));
    }

    protected Completable checkAnyPermission(String organizationId, String environmentId, String domainId, String applicationId, Permission permission, Acl... acls) {

        return checkPermissions(getAuthenticatedUser(), or(of(ReferenceType.APPLICATION, applicationId, permission, acls),
                of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }


    protected Completable checkAnyPermission(User authenticatedUser, String organizationId, String environmentId, String domainId, Permission permission, Acl... acls) {

        return checkPermissions(authenticatedUser, or(of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    protected Completable checkAnyPermission(String organizationId, String environmentId, String domainId, Permission permission, Acl... acls) {

        return checkPermissions(getAuthenticatedUser(), or(of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    protected Completable checkAnyPermission(String organizationId, String environmentId, Permission permission, Acl... acls) {

        return checkPermissions(getAuthenticatedUser(), or(of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    private Completable checkPermissions(User authenticatedUser, PermissionAcls permissionAcls) {

        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(hasPermission(authenticatedUser, permissionAcls)).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, CompletableSource>)this::checkPermission).apply(y)))).then());
    }

    protected Single<Boolean> hasPermission(User user, ReferenceType referenceType, String referenceId, Permission permission, Acl... acls) {

        return hasPermission(user, Permissions.of(referenceType, referenceId, permission, acls));
    }

    protected Single<Boolean> hasAnyPermission(User user, String organizationId, String environmentId, String domainId, String applicationId, Permission permission, Acl... acls) {

        return hasPermission(user, or(of(ReferenceType.APPLICATION, applicationId, permission, acls),
                of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    protected Single<Boolean> hasAnyPermission(User user, String organizationId, String environmentId, String domainId, Permission permission, Acl... acls) {

        return hasPermission(user, or(of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    protected Single<Boolean> hasPermission(User user, PermissionAcls permissionAcls) {

        return permissionService.hasPermission(user, permissionAcls);
    }

    protected Boolean hasPermission(Map<Permission, Set<Acl>> permissions, Permission permission, Acl acl) {

        return permissions.getOrDefault(permission, emptySet()).contains(acl);
    }

    protected Boolean hasAnyPermission(Map<ReferenceType, Map<Permission, Set<Acl>>> permissions, Permission permission, Acl acl) {

        return hasPermission(permissions.getOrDefault(ReferenceType.APPLICATION, emptyMap()), permission, acl)
                || hasPermission(permissions.getOrDefault(ReferenceType.DOMAIN, emptyMap()), permission, acl)
                || hasPermission(permissions.getOrDefault(ReferenceType.ENVIRONMENT, emptyMap()), permission, acl)
                || hasPermission(permissions.getOrDefault(ReferenceType.ORGANIZATION, emptyMap()), permission, acl);
    }

    protected Completable checkPermission(Map<Permission, Set<Acl>> permissions, Permission permission, Acl acl) {

        return checkPermission(permissions.getOrDefault(permission, emptySet()).contains(acl));
    }

    protected Single<Map<ReferenceType, Map<Permission, Set<Acl>>>> findAllPermissions(User user, String organizationId, String environmentId, String domainId) {

        return findAllPermissions(user, organizationId, environmentId, domainId, null);
    }

    protected Single<Map<ReferenceType, Map<Permission, Set<Acl>>>> findAllPermissions(User user, String organizationId, String environmentId, String domainId, String applicationId) {

        List<Single<Map<Permission, Set<Acl>>>> permissionObs = new ArrayList<>();

        permissionObs.add(applicationId != null ? permissionService.findAllPermissions(user, ReferenceType.APPLICATION, applicationId) : RxJava2Adapter.monoToSingle(Mono.just(emptyMap())));
        permissionObs.add(domainId != null ? permissionService.findAllPermissions(user, ReferenceType.DOMAIN, domainId) : RxJava2Adapter.monoToSingle(Mono.just(emptyMap())));
        permissionObs.add(environmentId != null ? permissionService.findAllPermissions(user, ReferenceType.ENVIRONMENT, environmentId) : RxJava2Adapter.monoToSingle(Mono.just(emptyMap())));
        permissionObs.add(organizationId != null ? permissionService.findAllPermissions(user, ReferenceType.ORGANIZATION, organizationId) : RxJava2Adapter.monoToSingle(Mono.just(emptyMap())));

        return Single.zip(permissionObs, objects -> {
            Map<ReferenceType, Map<Permission, Set<Acl>>> permissionsPerType = new HashMap<>();
            permissionsPerType.put(ReferenceType.APPLICATION, (Map<Permission, Set<Acl>>) objects[0]);
            permissionsPerType.put(ReferenceType.DOMAIN, (Map<Permission, Set<Acl>>) objects[1]);
            permissionsPerType.put(ReferenceType.ENVIRONMENT, (Map<Permission, Set<Acl>>) objects[2]);
            permissionsPerType.put(ReferenceType.ORGANIZATION, (Map<Permission, Set<Acl>>) objects[3]);

            return permissionsPerType;
        });
    }

    private Completable checkPermission(Boolean hasPermission) {

        if (!hasPermission) {
            return RxJava2Adapter.monoToCompletable(Mono.error(new ForbiddenException("Permission denied")));
        }

        return RxJava2Adapter.monoToCompletable(Mono.empty());
    }
}

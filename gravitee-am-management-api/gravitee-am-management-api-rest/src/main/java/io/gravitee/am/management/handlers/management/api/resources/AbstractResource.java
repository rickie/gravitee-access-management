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

import com.google.errorprone.annotations.InlineMe;
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

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkPermission_migrated(referenceType, referenceId, permission, acls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Completable checkPermission(ReferenceType referenceType, String referenceId, Permission permission, Acl... acls) {
 return RxJava2Adapter.monoToCompletable(checkPermission_migrated(referenceType, referenceId, permission, acls));
}
protected Mono<Void> checkPermission_migrated(ReferenceType referenceType, String referenceId, Permission permission, Acl... acls) {

        return checkPermissions_migrated(getAuthenticatedUser(), Permissions.of(referenceType, referenceId, permission, acls));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkAnyPermission_migrated(organizationId, environmentId, domainId, applicationId, permission, acls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Completable checkAnyPermission(String organizationId, String environmentId, String domainId, String applicationId, Permission permission, Acl... acls) {
 return RxJava2Adapter.monoToCompletable(checkAnyPermission_migrated(organizationId, environmentId, domainId, applicationId, permission, acls));
}
protected Mono<Void> checkAnyPermission_migrated(String organizationId, String environmentId, String domainId, String applicationId, Permission permission, Acl... acls) {

        return checkPermissions_migrated(getAuthenticatedUser(), or(of(ReferenceType.APPLICATION, applicationId, permission, acls),
                of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkAnyPermission_migrated(authenticatedUser, organizationId, environmentId, domainId, permission, acls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Completable checkAnyPermission(User authenticatedUser, String organizationId, String environmentId, String domainId, Permission permission, Acl... acls) {
 return RxJava2Adapter.monoToCompletable(checkAnyPermission_migrated(authenticatedUser, organizationId, environmentId, domainId, permission, acls));
}
protected Mono<Void> checkAnyPermission_migrated(User authenticatedUser, String organizationId, String environmentId, String domainId, Permission permission, Acl... acls) {

        return checkPermissions_migrated(authenticatedUser, or(of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkAnyPermission_migrated(organizationId, environmentId, domainId, permission, acls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Completable checkAnyPermission(String organizationId, String environmentId, String domainId, Permission permission, Acl... acls) {
 return RxJava2Adapter.monoToCompletable(checkAnyPermission_migrated(organizationId, environmentId, domainId, permission, acls));
}
protected Mono<Void> checkAnyPermission_migrated(String organizationId, String environmentId, String domainId, Permission permission, Acl... acls) {

        return checkPermissions_migrated(getAuthenticatedUser(), or(of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkAnyPermission_migrated(organizationId, environmentId, permission, acls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Completable checkAnyPermission(String organizationId, String environmentId, Permission permission, Acl... acls) {
 return RxJava2Adapter.monoToCompletable(checkAnyPermission_migrated(organizationId, environmentId, permission, acls));
}
protected Mono<Void> checkAnyPermission_migrated(String organizationId, String environmentId, Permission permission, Acl... acls) {

        return checkPermissions_migrated(getAuthenticatedUser(), or(of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkPermissions_migrated(authenticatedUser, permissionAcls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable checkPermissions(User authenticatedUser, PermissionAcls permissionAcls) {
 return RxJava2Adapter.monoToCompletable(checkPermissions_migrated(authenticatedUser, permissionAcls));
}
private Mono<Void> checkPermissions_migrated(User authenticatedUser, PermissionAcls permissionAcls) {

        return hasPermission_migrated(authenticatedUser, permissionAcls).flatMap(ident->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(checkPermission_migrated(ident)))).then();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.hasPermission_migrated(user, referenceType, referenceId, permission, acls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Single<Boolean> hasPermission(User user, ReferenceType referenceType, String referenceId, Permission permission, Acl... acls) {
 return RxJava2Adapter.monoToSingle(hasPermission_migrated(user, referenceType, referenceId, permission, acls));
}
protected Mono<Boolean> hasPermission_migrated(User user, ReferenceType referenceType, String referenceId, Permission permission, Acl... acls) {

        return hasPermission_migrated(user, Permissions.of(referenceType, referenceId, permission, acls));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.hasAnyPermission_migrated(user, organizationId, environmentId, domainId, applicationId, permission, acls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Single<Boolean> hasAnyPermission(User user, String organizationId, String environmentId, String domainId, String applicationId, Permission permission, Acl... acls) {
 return RxJava2Adapter.monoToSingle(hasAnyPermission_migrated(user, organizationId, environmentId, domainId, applicationId, permission, acls));
}
protected Mono<Boolean> hasAnyPermission_migrated(User user, String organizationId, String environmentId, String domainId, String applicationId, Permission permission, Acl... acls) {

        return hasPermission_migrated(user, or(of(ReferenceType.APPLICATION, applicationId, permission, acls),
                of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.hasAnyPermission_migrated(user, organizationId, environmentId, domainId, permission, acls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Single<Boolean> hasAnyPermission(User user, String organizationId, String environmentId, String domainId, Permission permission, Acl... acls) {
 return RxJava2Adapter.monoToSingle(hasAnyPermission_migrated(user, organizationId, environmentId, domainId, permission, acls));
}
protected Mono<Boolean> hasAnyPermission_migrated(User user, String organizationId, String environmentId, String domainId, Permission permission, Acl... acls) {

        return hasPermission_migrated(user, or(of(ReferenceType.DOMAIN, domainId, permission, acls),
                of(ReferenceType.ENVIRONMENT, environmentId, permission, acls),
                of(ReferenceType.ORGANIZATION, organizationId, permission, acls)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.hasPermission_migrated(user, permissionAcls))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Single<Boolean> hasPermission(User user, PermissionAcls permissionAcls) {
 return RxJava2Adapter.monoToSingle(hasPermission_migrated(user, permissionAcls));
}
protected Mono<Boolean> hasPermission_migrated(User user, PermissionAcls permissionAcls) {

        return permissionService.hasPermission_migrated(user, permissionAcls);
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

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkPermission_migrated(permissions, permission, acl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Completable checkPermission(Map<Permission, Set<Acl>> permissions, Permission permission, Acl acl) {
 return RxJava2Adapter.monoToCompletable(checkPermission_migrated(permissions, permission, acl));
}
protected Mono<Void> checkPermission_migrated(Map<Permission, Set<Acl>> permissions, Permission permission, Acl acl) {

        return checkPermission_migrated(permissions.getOrDefault(permission, emptySet()).contains(acl));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAllPermissions_migrated(user, organizationId, environmentId, domainId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Single<Map<ReferenceType, Map<Permission, Set<Acl>>>> findAllPermissions(User user, String organizationId, String environmentId, String domainId) {
 return RxJava2Adapter.monoToSingle(findAllPermissions_migrated(user, organizationId, environmentId, domainId));
}
protected Mono<Map<ReferenceType,Map<Permission,Set<Acl>>>> findAllPermissions_migrated(User user, String organizationId, String environmentId, String domainId) {

        return findAllPermissions_migrated(user, organizationId, environmentId, domainId, null);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAllPermissions_migrated(user, organizationId, environmentId, domainId, applicationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Single<Map<ReferenceType, Map<Permission, Set<Acl>>>> findAllPermissions(User user, String organizationId, String environmentId, String domainId, String applicationId) {
 return RxJava2Adapter.monoToSingle(findAllPermissions_migrated(user, organizationId, environmentId, domainId, applicationId));
}
protected Mono<Map<ReferenceType,Map<Permission,Set<Acl>>>> findAllPermissions_migrated(User user, String organizationId, String environmentId, String domainId, String applicationId) {

        List<Single<Map<Permission, Set<Acl>>>> permissionObs = new ArrayList<>();

        permissionObs.add(applicationId != null ? RxJava2Adapter.monoToSingle(permissionService.findAllPermissions_migrated(user, ReferenceType.APPLICATION, applicationId)) : RxJava2Adapter.monoToSingle(Mono.just(emptyMap())));
        permissionObs.add(domainId != null ? RxJava2Adapter.monoToSingle(permissionService.findAllPermissions_migrated(user, ReferenceType.DOMAIN, domainId)) : RxJava2Adapter.monoToSingle(Mono.just(emptyMap())));
        permissionObs.add(environmentId != null ? RxJava2Adapter.monoToSingle(permissionService.findAllPermissions_migrated(user, ReferenceType.ENVIRONMENT, environmentId)) : RxJava2Adapter.monoToSingle(Mono.just(emptyMap())));
        permissionObs.add(organizationId != null ? RxJava2Adapter.monoToSingle(permissionService.findAllPermissions_migrated(user, ReferenceType.ORGANIZATION, organizationId)) : RxJava2Adapter.monoToSingle(Mono.just(emptyMap())));

        return RxJava2Adapter.singleToMono(Single.zip(permissionObs, objects -> {
            Map<ReferenceType, Map<Permission, Set<Acl>>> permissionsPerType = new HashMap<>();
            permissionsPerType.put(ReferenceType.APPLICATION, (Map<Permission, Set<Acl>>) objects[0]);
            permissionsPerType.put(ReferenceType.DOMAIN, (Map<Permission, Set<Acl>>) objects[1]);
            permissionsPerType.put(ReferenceType.ENVIRONMENT, (Map<Permission, Set<Acl>>) objects[2]);
            permissionsPerType.put(ReferenceType.ORGANIZATION, (Map<Permission, Set<Acl>>) objects[3]);

            return permissionsPerType;
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.checkPermission_migrated(hasPermission))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable checkPermission(Boolean hasPermission) {
 return RxJava2Adapter.monoToCompletable(checkPermission_migrated(hasPermission));
}
private Mono<Void> checkPermission_migrated(Boolean hasPermission) {

        if (!hasPermission) {
            return Mono.error(new ForbiddenException("Permission denied"));
        }

        return Mono.empty();
    }
}

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
package io.gravitee.am.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;

import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.model.permissions.SystemRole;
import io.gravitee.am.service.model.NewRole;
import io.gravitee.am.service.model.UpdateRole;




import java.util.List;

import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface RoleService {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllAssignable_migrated(referenceType, referenceId, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Role> findAllAssignable(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.model.ReferenceType assignableType) {
    return RxJava2Adapter.fluxToFlowable(findAllAssignable_migrated(referenceType, referenceId, assignableType));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Role> findAllAssignable_migrated(ReferenceType referenceType, String referenceId, ReferenceType assignableType) {
    return RxJava2Adapter.flowableToFlux(findAllAssignable(referenceType, referenceId, assignableType));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.Set<io.gravitee.am.model.Role>> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain));
}
default reactor.core.publisher.Mono<java.util.Set<io.gravitee.am.model.Role>> findByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Role>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Role>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.searchByDomain_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Role>> searchByDomain(java.lang.String domain, java.lang.String query, int page, int size) {
    return RxJava2Adapter.monoToSingle(searchByDomain_migrated(domain, query, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Role>> searchByDomain_migrated(String domain, String query, int page, int size) {
    return RxJava2Adapter.singleToMono(searchByDomain(domain, query, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Role> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(findById(referenceType, referenceId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Role> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findSystemRole_migrated(systemRole, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Role> findSystemRole(io.gravitee.am.model.permissions.SystemRole systemRole, io.gravitee.am.model.ReferenceType assignableType) {
    return RxJava2Adapter.monoToMaybe(findSystemRole_migrated(systemRole, assignableType));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> findSystemRole_migrated(SystemRole systemRole, ReferenceType assignableType) {
    return RxJava2Adapter.maybeToMono(findSystemRole(systemRole, assignableType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findRolesByName_migrated(referenceType, referenceId, assignableType, roleNames))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Role> findRolesByName(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.model.ReferenceType assignableType, java.util.List<java.lang.String> roleNames) {
    return RxJava2Adapter.fluxToFlowable(findRolesByName_migrated(referenceType, referenceId, assignableType, roleNames));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Role> findRolesByName_migrated(ReferenceType referenceType, String referenceId, ReferenceType assignableType, List<String> roleNames) {
    return RxJava2Adapter.flowableToFlux(findRolesByName(referenceType, referenceId, assignableType, roleNames));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findDefaultRole_migrated(organizationId, defaultRole, assignableType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Role> findDefaultRole(java.lang.String organizationId, io.gravitee.am.model.permissions.DefaultRole defaultRole, io.gravitee.am.model.ReferenceType assignableType) {
    return RxJava2Adapter.monoToMaybe(findDefaultRole_migrated(organizationId, defaultRole, assignableType));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> findDefaultRole_migrated(String organizationId, DefaultRole defaultRole, ReferenceType assignableType) {
    return RxJava2Adapter.maybeToMono(findDefaultRole(organizationId, defaultRole, assignableType));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.Set<io.gravitee.am.model.Role>> findByIdIn(java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.monoToSingle(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Mono<java.util.Set<io.gravitee.am.model.Role>> findByIdIn_migrated(List<String> ids) {
    return RxJava2Adapter.singleToMono(findByIdIn(ids));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newRole, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Role> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.service.model.NewRole newRole, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newRole, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> create_migrated(ReferenceType referenceType, String referenceId, NewRole newRole, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newRole, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, role, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Role> create(java.lang.String domain, io.gravitee.am.service.model.NewRole role, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, role, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> create_migrated(String domain, NewRole role, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, role, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateRole, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Role> update(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id, io.gravitee.am.service.model.UpdateRole updateRole, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateRole, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateRole updateRole, User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateRole, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, role, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Role> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateRole role, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, role, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Role> update_migrated(String domain, String id, UpdateRole role, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, role, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, roleId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String roleId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, roleId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(ReferenceType referenceType, String referenceId, String roleId, User principal) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, roleId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.createOrUpdateSystemRoles_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable createOrUpdateSystemRoles() {
    return RxJava2Adapter.monoToCompletable(createOrUpdateSystemRoles_migrated());
}
default reactor.core.publisher.Mono<java.lang.Void> createOrUpdateSystemRoles_migrated() {
    return RxJava2Adapter.completableToMono(createOrUpdateSystemRoles());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Role> create(java.lang.String domain, io.gravitee.am.service.model.NewRole role) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, role));
}default Mono<Role> create_migrated(String domain, NewRole role) {
        return RxJava2Adapter.singleToMono(create(domain, role, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Role> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateRole role) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, role));
}default Mono<Role> update_migrated(String domain, String id, UpdateRole role) {
        return RxJava2Adapter.singleToMono(update(domain, id, role, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, roleId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String roleId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, roleId));
}default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String roleId) {
        return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, roleId, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.createDefaultRoles_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable createDefaultRoles(java.lang.String organizationId) {
    return RxJava2Adapter.monoToCompletable(createDefaultRoles_migrated(organizationId));
}
default reactor.core.publisher.Mono<java.lang.Void> createDefaultRoles_migrated(String organizationId) {
    return RxJava2Adapter.completableToMono(createDefaultRoles(organizationId));
}
}

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
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.model.permissions.SystemRole;
import io.gravitee.am.service.model.NewRole;
import io.gravitee.am.service.model.UpdateRole;
import io.reactivex.Completable;

import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface RoleService {

      
Flux<Role> findAllAssignable_migrated(ReferenceType referenceType, String referenceId, ReferenceType assignableType);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Set<Role>> findByDomain(String domain) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain));
}
default Mono<Set<Role>> findByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<Role>> findByDomain(String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default Mono<Page<Role>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      
Mono<Page<Role>> searchByDomain_migrated(String domain, String query, int page, int size);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Role> findById(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
default Mono<Role> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(findById(referenceType, referenceId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Role> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Role> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Mono<Role> findSystemRole_migrated(SystemRole systemRole, ReferenceType assignableType);

      
Flux<Role> findRolesByName_migrated(ReferenceType referenceType, String referenceId, ReferenceType assignableType, List<String> roleNames);

      
Mono<Role> findDefaultRole_migrated(String organizationId, DefaultRole defaultRole, ReferenceType assignableType);

      
Mono<Set<Role>> findByIdIn_migrated(List<String> ids);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newRole, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Role> create(ReferenceType referenceType, String referenceId, NewRole newRole, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newRole, principal));
}
default Mono<Role> create_migrated(ReferenceType referenceType, String referenceId, NewRole newRole, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newRole, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, role, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Role> create(String domain, NewRole role, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, role, principal));
}
default Mono<Role> create_migrated(String domain, NewRole role, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, role, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateRole, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Role> update(ReferenceType referenceType, String referenceId, String id, UpdateRole updateRole, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateRole, principal));
}
default Mono<Role> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateRole updateRole, User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateRole, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, role, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Role> update(String domain, String id, UpdateRole role, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, role, principal));
}
default Mono<Role> update_migrated(String domain, String id, UpdateRole role, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, role, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, roleId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(ReferenceType referenceType, String referenceId, String roleId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, roleId, principal));
}
default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String roleId, User principal) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, roleId, principal));
}

      
Mono<Void> createOrUpdateSystemRoles_migrated();

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Role> create(String domain, NewRole role) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, role));
}default Mono<Role> create_migrated(String domain, NewRole role) {
        return RxJava2Adapter.singleToMono(create(domain, role, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Role> update(String domain, String id, UpdateRole role) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, role));
}default Mono<Role> update_migrated(String domain, String id, UpdateRole role) {
        return RxJava2Adapter.singleToMono(update(domain, id, role, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, roleId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(ReferenceType referenceType, String referenceId, String roleId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, roleId));
}default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String roleId) {
        return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, roleId, null));
    }

      
Mono<Void> createDefaultRoles_migrated(String organizationId);
}

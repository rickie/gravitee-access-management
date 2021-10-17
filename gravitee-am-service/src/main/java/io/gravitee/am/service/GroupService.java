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
import io.gravitee.am.model.Group;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.service.model.NewGroup;
import io.gravitee.am.service.model.UpdateGroup;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface GroupService {

      
Mono<Page<Group>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<Group>> findByDomain(String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default Mono<Page<Group>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      
Mono<Page<io.gravitee.am.model.User>> findMembers_migrated(ReferenceType referenceType, String referenceId, String groupId, int page, int size);

      
Flux<Group> findAll_migrated(ReferenceType referenceType, String referenceId);

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Group> findByDomain(String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default Flux<Group> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      
Flux<Group> findByMember_migrated(String userId);

      
Flux<Group> findByIdIn_migrated(List<String> ids);

      
Mono<Group> findByName_migrated(ReferenceType referenceType, String referenceId, String groupName);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> findById(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
default Mono<Group> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(findById(referenceType, referenceId, id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Group> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Group> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newGroup, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> create(ReferenceType referenceType, String referenceId, NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newGroup, principal));
}
default Mono<Group> create_migrated(ReferenceType referenceType, String referenceId, NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newGroup, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, group, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> create(String domain, NewGroup group, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, group, principal));
}
default Mono<Group> create_migrated(String domain, NewGroup group, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(create(domain, group, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateGroup, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> update(ReferenceType referenceType, String referenceId, String id, UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateGroup, principal));
}
default Mono<Group> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateGroup, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, group, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> update(String domain, String id, UpdateGroup group, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, group, principal));
}
default Mono<Group> update_migrated(String domain, String id, UpdateGroup group, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, group, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, groupId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(ReferenceType referenceType, String referenceId, String groupId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, groupId, principal));
}
default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String groupId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, groupId, principal));
}

      
Mono<Group> assignRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.revokeRoles_migrated(referenceType, referenceId, groupId, roles, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> revokeRoles(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, groupId, roles, principal));
}
default Mono<Group> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(revokeRoles(referenceType, referenceId, groupId, roles, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, group))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> create(String domain, NewGroup group) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, group));
}default Mono<Group> create_migrated(String domain, NewGroup group) {
        return RxJava2Adapter.singleToMono(create(domain, group, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, group))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> update(String domain, String id, UpdateGroup group) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, group));
}default Mono<Group> update_migrated(String domain, String id, UpdateGroup group) {
        return RxJava2Adapter.singleToMono(update(domain, id, group, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, groupId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(ReferenceType referenceType, String referenceId, String groupId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, groupId));
}default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String groupId) {
        return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, groupId, null));
    }

      Mono<Group> assignRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.revokeRoles_migrated(referenceType, referenceId, groupId, roles))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> revokeRoles(ReferenceType referenceType, String referenceId, String groupId, List<String> roles) {
    return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, groupId, roles));
}default Mono<Group> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles) {
        return RxJava2Adapter.singleToMono(revokeRoles(referenceType, referenceId, groupId, roles, null));
    }

}

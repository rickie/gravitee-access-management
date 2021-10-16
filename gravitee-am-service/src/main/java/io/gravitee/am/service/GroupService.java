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

import io.gravitee.am.model.Group;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.service.model.NewGroup;
import io.gravitee.am.service.model.UpdateGroup;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface GroupService {

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Group>> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, int page, int size) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(referenceType, referenceId, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Group>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size) {
    return RxJava2Adapter.singleToMono(findAll(referenceType, referenceId, page, size));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.Group>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.Group>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> findMembers(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String groupId, int page, int size) {
    return RxJava2Adapter.monoToSingle(findMembers_migrated(referenceType, referenceId, groupId, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> findMembers_migrated(ReferenceType referenceType, String referenceId, String groupId, int page, int size) {
    return RxJava2Adapter.singleToMono(findMembers(referenceType, referenceId, groupId, page, size));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Group> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Group> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Group> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Group> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Group> findByMember(java.lang.String userId) {
    return RxJava2Adapter.fluxToFlowable(findByMember_migrated(userId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Group> findByMember_migrated(String userId) {
    return RxJava2Adapter.flowableToFlux(findByMember(userId));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Group> findByIdIn(java.util.List<java.lang.String> ids) {
    return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Group> findByIdIn_migrated(List<String> ids) {
    return RxJava2Adapter.flowableToFlux(findByIdIn(ids));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Group> findByName(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String groupName) {
    return RxJava2Adapter.monoToMaybe(findByName_migrated(referenceType, referenceId, groupName));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Group> findByName_migrated(ReferenceType referenceType, String referenceId, String groupName) {
    return RxJava2Adapter.maybeToMono(findByName(referenceType, referenceId, groupName));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Group> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(findById(referenceType, referenceId, id));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Group> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Group> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.service.model.NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newGroup, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Group> create_migrated(ReferenceType referenceType, String referenceId, NewGroup newGroup, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newGroup, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> create(java.lang.String domain, io.gravitee.am.service.model.NewGroup group, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, group, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Group> create_migrated(String domain, NewGroup group, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(create(domain, group, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> update(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id, io.gravitee.am.service.model.UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateGroup, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Group> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateGroup updateGroup, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateGroup, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateGroup group, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, group, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Group> update_migrated(String domain, String id, UpdateGroup group, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, group, principal));
}

      @Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String groupId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, groupId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(ReferenceType referenceType, String referenceId, String groupId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, groupId, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> assignRoles(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String groupId, java.util.List<java.lang.String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(assignRoles_migrated(referenceType, referenceId, groupId, roles, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Group> assignRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(assignRoles(referenceType, referenceId, groupId, roles, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> revokeRoles(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String groupId, java.util.List<java.lang.String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, groupId, roles, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Group> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(revokeRoles(referenceType, referenceId, groupId, roles, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> create(java.lang.String domain, io.gravitee.am.service.model.NewGroup group) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, group));
}default Mono<Group> create_migrated(String domain, NewGroup group) {
        return RxJava2Adapter.singleToMono(create(domain, group, null));
    }

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateGroup group) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, group));
}default Mono<Group> update_migrated(String domain, String id, UpdateGroup group) {
        return RxJava2Adapter.singleToMono(update(domain, id, group, null));
    }

      @Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String groupId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, groupId));
}default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String groupId) {
        return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, groupId, null));
    }

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> assignRoles(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String groupId, java.util.List<java.lang.String> roles) {
    return RxJava2Adapter.monoToSingle(assignRoles_migrated(referenceType, referenceId, groupId, roles));
}default Mono<Group> assignRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles) {
        return RxJava2Adapter.singleToMono(assignRoles(referenceType, referenceId, groupId, roles, null));
    }

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Group> revokeRoles(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String groupId, java.util.List<java.lang.String> roles) {
    return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, groupId, roles));
}default Mono<Group> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String groupId, List<String> roles) {
        return RxJava2Adapter.singleToMono(revokeRoles(referenceType, referenceId, groupId, roles, null));
    }

}

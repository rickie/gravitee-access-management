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
package io.gravitee.am.gateway.handler.scim.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.scim.model.Group;
import io.gravitee.am.gateway.handler.scim.model.ListResponse;
import io.gravitee.am.gateway.handler.scim.model.PatchOp;
import io.reactivex.Completable;


import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface GroupService {

      
Mono<ListResponse<Group>> list_migrated(int page, int size, String baseUrl);

      
Flux<Group> findByMember_migrated(String memberId);

      
Mono<Group> get_migrated(String groupId, String baseUrl);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(group, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> create(Group group, String baseUrl) {
    return RxJava2Adapter.monoToSingle(create_migrated(group, baseUrl));
}
default Mono<Group> create_migrated(Group group, String baseUrl) {
    return RxJava2Adapter.singleToMono(create(group, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(groupId, group, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> update(String groupId, Group group, String baseUrl) {
    return RxJava2Adapter.monoToSingle(update_migrated(groupId, group, baseUrl));
}
default Mono<Group> update_migrated(String groupId, Group group, String baseUrl) {
    return RxJava2Adapter.singleToMono(update(groupId, group, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(groupId, patchOp, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Group> patch(String groupId, PatchOp patchOp, String baseUrl) {
    return RxJava2Adapter.monoToSingle(patch_migrated(groupId, patchOp, baseUrl));
}
default Mono<Group> patch_migrated(String groupId, PatchOp patchOp, String baseUrl) {
    return RxJava2Adapter.singleToMono(patch(groupId, patchOp, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(groupId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String groupId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(groupId));
}
default Mono<Void> delete_migrated(String groupId) {
    return RxJava2Adapter.completableToMono(delete(groupId));
}
}

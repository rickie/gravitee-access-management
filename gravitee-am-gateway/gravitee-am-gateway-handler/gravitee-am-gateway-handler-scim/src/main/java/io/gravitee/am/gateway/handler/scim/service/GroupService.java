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
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface GroupService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.list_migrated(page, size, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.scim.model.ListResponse<io.gravitee.am.gateway.handler.scim.model.Group>> list(int page, int size, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToSingle(list_migrated(page, size, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.ListResponse<io.gravitee.am.gateway.handler.scim.model.Group>> list_migrated(int page, int size, String baseUrl) {
    return RxJava2Adapter.singleToMono(list(page, size, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByMember_migrated(memberId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.gateway.handler.scim.model.Group> findByMember(java.lang.String memberId) {
    return RxJava2Adapter.fluxToFlowable(findByMember_migrated(memberId));
}
default reactor.core.publisher.Flux<io.gravitee.am.gateway.handler.scim.model.Group> findByMember_migrated(String memberId) {
    return RxJava2Adapter.flowableToFlux(findByMember(memberId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.get_migrated(groupId, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.gateway.handler.scim.model.Group> get(java.lang.String groupId, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToMaybe(get_migrated(groupId, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.Group> get_migrated(String groupId, String baseUrl) {
    return RxJava2Adapter.maybeToMono(get(groupId, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(group, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.scim.model.Group> create(io.gravitee.am.gateway.handler.scim.model.Group group, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToSingle(create_migrated(group, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.Group> create_migrated(Group group, String baseUrl) {
    return RxJava2Adapter.singleToMono(create(group, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(groupId, group, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.scim.model.Group> update(java.lang.String groupId, io.gravitee.am.gateway.handler.scim.model.Group group, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToSingle(update_migrated(groupId, group, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.Group> update_migrated(String groupId, Group group, String baseUrl) {
    return RxJava2Adapter.singleToMono(update(groupId, group, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(groupId, patchOp, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.scim.model.Group> patch(java.lang.String groupId, io.gravitee.am.gateway.handler.scim.model.PatchOp patchOp, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToSingle(patch_migrated(groupId, patchOp, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.Group> patch_migrated(String groupId, PatchOp patchOp, String baseUrl) {
    return RxJava2Adapter.singleToMono(patch(groupId, patchOp, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(groupId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String groupId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(groupId));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String groupId) {
    return RxJava2Adapter.completableToMono(delete(groupId));
}
}

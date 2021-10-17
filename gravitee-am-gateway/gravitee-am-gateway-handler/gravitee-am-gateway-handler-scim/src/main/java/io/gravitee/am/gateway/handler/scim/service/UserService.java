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
import io.gravitee.am.common.scim.filter.Filter;
import io.gravitee.am.gateway.handler.scim.model.ListResponse;
import io.gravitee.am.gateway.handler.scim.model.PatchOp;
import io.gravitee.am.gateway.handler.scim.model.User;
import io.reactivex.Completable;

import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserService {

      
Mono<ListResponse<User>> list_migrated(Filter filter, int page, int size, String baseUrl);

      
Mono<User> get_migrated(String userId, String baseUrl);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> create(User user, String baseUrl) {
    return RxJava2Adapter.monoToSingle(create_migrated(user, baseUrl));
}
default Mono<User> create_migrated(User user, String baseUrl) {
    return RxJava2Adapter.singleToMono(create(user, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(userId, user, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> update(String userId, User user, String baseUrl) {
    return RxJava2Adapter.monoToSingle(update_migrated(userId, user, baseUrl));
}
default Mono<User> update_migrated(String userId, User user, String baseUrl) {
    return RxJava2Adapter.singleToMono(update(userId, user, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(userId, patchOp, baseUrl))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> patch(String userId, PatchOp patchOp, String baseUrl) {
    return RxJava2Adapter.monoToSingle(patch_migrated(userId, patchOp, baseUrl));
}
default Mono<User> patch_migrated(String userId, PatchOp patchOp, String baseUrl) {
    return RxJava2Adapter.singleToMono(patch(userId, patchOp, baseUrl));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String userId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(userId));
}
default Mono<Void> delete_migrated(String userId) {
    return RxJava2Adapter.completableToMono(delete(userId));
}
}

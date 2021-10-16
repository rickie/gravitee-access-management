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

import io.gravitee.am.common.scim.filter.Filter;
import io.gravitee.am.gateway.handler.scim.model.ListResponse;
import io.gravitee.am.gateway.handler.scim.model.PatchOp;
import io.gravitee.am.gateway.handler.scim.model.User;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserService {

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.scim.model.ListResponse<io.gravitee.am.gateway.handler.scim.model.User>> list(io.gravitee.am.common.scim.filter.Filter filter, int page, int size, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToSingle(list_migrated(filter, page, size, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.ListResponse<io.gravitee.am.gateway.handler.scim.model.User>> list_migrated(Filter filter, int page, int size, String baseUrl) {
    return RxJava2Adapter.singleToMono(list(filter, page, size, baseUrl));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.gateway.handler.scim.model.User> get(java.lang.String userId, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToMaybe(get_migrated(userId, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.User> get_migrated(String userId, String baseUrl) {
    return RxJava2Adapter.maybeToMono(get(userId, baseUrl));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.scim.model.User> create(io.gravitee.am.gateway.handler.scim.model.User user, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToSingle(create_migrated(user, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.User> create_migrated(User user, String baseUrl) {
    return RxJava2Adapter.singleToMono(create(user, baseUrl));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.scim.model.User> update(java.lang.String userId, io.gravitee.am.gateway.handler.scim.model.User user, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToSingle(update_migrated(userId, user, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.User> update_migrated(String userId, User user, String baseUrl) {
    return RxJava2Adapter.singleToMono(update(userId, user, baseUrl));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.scim.model.User> patch(java.lang.String userId, io.gravitee.am.gateway.handler.scim.model.PatchOp patchOp, java.lang.String baseUrl) {
    return RxJava2Adapter.monoToSingle(patch_migrated(userId, patchOp, baseUrl));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.scim.model.User> patch_migrated(String userId, PatchOp patchOp, String baseUrl) {
    return RxJava2Adapter.singleToMono(patch(userId, patchOp, baseUrl));
}

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String userId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(userId));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String userId) {
    return RxJava2Adapter.completableToMono(delete(userId));
}
}

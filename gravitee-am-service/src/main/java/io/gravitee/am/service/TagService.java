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

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Tag;
import io.gravitee.am.service.model.NewTag;
import io.gravitee.am.service.model.UpdateTag;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface TagService {

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Tag> findById(java.lang.String id, java.lang.String organizationId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id, organizationId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Tag> findById_migrated(String id, String organizationId) {
    return RxJava2Adapter.maybeToMono(findById(id, organizationId));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Tag> findAll(java.lang.String organizationId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(organizationId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Tag> findAll_migrated(String organizationId) {
    return RxJava2Adapter.flowableToFlux(findAll(organizationId));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Tag> create(io.gravitee.am.service.model.NewTag tag, java.lang.String organizationId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(tag, organizationId, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Tag> create_migrated(NewTag tag, String organizationId, User principal) {
    return RxJava2Adapter.singleToMono(create(tag, organizationId, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Tag> update(java.lang.String tagId, java.lang.String organizationId, io.gravitee.am.service.model.UpdateTag tag, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(tagId, organizationId, tag, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Tag> update_migrated(String tagId, String organizationId, UpdateTag tag, User principal) {
    return RxJava2Adapter.singleToMono(update(tagId, organizationId, tag, principal));
}

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String tagId, java.lang.String organizationId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(tagId, organizationId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String tagId, String organizationId, User principal) {
    return RxJava2Adapter.completableToMono(delete(tagId, organizationId, principal));
}
}

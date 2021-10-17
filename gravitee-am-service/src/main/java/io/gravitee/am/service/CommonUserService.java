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
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.reactivex.Completable;


import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CommonUserService {

      
Flux<User> findByIdIn_migrated(List<String> ids);

      
Mono<Page<User>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size);

      
Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, String query, int page, int size);

      
Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, FilterCriteria filterCriteria, int page, int size);

      
Mono<User> findByUsernameAndSource_migrated(ReferenceType referenceType, String referenceId, String username, String source);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> findById(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, id));
}
default Mono<User> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.singleToMono(findById(referenceType, referenceId, id));
}

      
Mono<User> findByExternalIdAndSource_migrated(ReferenceType referenceType, String referenceId, String externalId, String source);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> create(ReferenceType referenceType, String referenceId, NewUser newUser) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newUser));
}
default Mono<User> create_migrated(ReferenceType referenceType, String referenceId, NewUser newUser) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> create(User user) {
    return RxJava2Adapter.monoToSingle(create_migrated(user));
}
default Mono<User> create_migrated(User user) {
    return RxJava2Adapter.singleToMono(create(user));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> update(ReferenceType referenceType, String referenceId, String id, UpdateUser updateUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateUser));
}
default Mono<User> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateUser updateUser) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> update(User user) {
    return RxJava2Adapter.monoToSingle(update_migrated(user));
}
default Mono<User> update_migrated(User user) {
    return RxJava2Adapter.singleToMono(update(user));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String userId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(userId));
}
default Mono<Void> delete_migrated(String userId) {
    return RxJava2Adapter.completableToMono(delete(userId));
}

      
Mono<User> enhance_migrated(User user);
}

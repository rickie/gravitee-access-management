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
package io.gravitee.am.repository.management.api;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.common.CrudRepository;
import io.gravitee.am.repository.management.api.search.FilterCriteria;

import io.reactivex.Maybe;

import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CommonUserRepository extends CrudRepository<User, String> {

      
Flux<User> findAll_migrated(ReferenceType referenceType, String referenceId);

      
Mono<Page<User>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size);

      
Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, String query, int page, int size);

      
Mono<Page<User>> search_migrated(ReferenceType referenceType, String referenceId, FilterCriteria criteria, int page, int size);

      
Mono<User> findByUsernameAndSource_migrated(ReferenceType referenceType, String referenceId, String username, String source);

      
Mono<User> findByExternalIdAndSource_migrated(ReferenceType referenceType, String referenceId, String externalId, String source);

      
Flux<User> findByIdIn_migrated(List<String> ids);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<User> findById(ReferenceType referenceType, String referenceId, String userId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, userId));
}
default Mono<User> findById_migrated(ReferenceType referenceType, String referenceId, String userId) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, userId));
}

}
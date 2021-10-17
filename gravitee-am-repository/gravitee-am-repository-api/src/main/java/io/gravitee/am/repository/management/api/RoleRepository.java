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
import io.gravitee.am.model.Role;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.repository.common.CrudRepository;
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
public interface RoleRepository extends CrudRepository<Role, String> {

      
Flux<Role> findAll_migrated(ReferenceType referenceType, String referenceId);

      
Mono<Page<Role>> findAll_migrated(ReferenceType referenceType, String referenceId, int page, int size);

      
Mono<Page<Role>> search_migrated(ReferenceType referenceType, String referenceId, String query, int page, int size);

      
Flux<Role> findByIdIn_migrated(List<String> ids);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, role))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Role> findById(ReferenceType referenceType, String referenceId, String role) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, role));
}
default Mono<Role> findById_migrated(ReferenceType referenceType, String referenceId, String role) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, role));
}

      
Mono<Role> findByNameAndAssignableType_migrated(ReferenceType referenceType, String referenceId, String name, ReferenceType assignableType);

      
Flux<Role> findByNamesAndAssignableType_migrated(ReferenceType referenceType, String referenceId, List<String> name, ReferenceType assignableType);
}

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
import io.gravitee.am.model.Email;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.common.CrudRepository;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EmailRepository extends CrudRepository<Email, String> {

      
Flux<Email> findAll_migrated();

      
Flux<Email> findAll_migrated(ReferenceType referenceType, String referenceId);

      
Flux<Email> findByClient_migrated(ReferenceType referenceType, String referenceId, String client);

      
Mono<Email> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template);

      
Mono<Email> findByDomainAndTemplate_migrated(String domain, String template);

      
Mono<Email> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template);

      
Mono<Email> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Email> findById(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
default Mono<Email> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, id));
}
}

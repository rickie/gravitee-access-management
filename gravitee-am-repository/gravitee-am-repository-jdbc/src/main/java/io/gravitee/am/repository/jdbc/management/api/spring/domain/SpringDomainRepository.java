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
package io.gravitee.am.repository.jdbc.management.api.spring.domain;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcDomain;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringDomainRepository extends RxJava2CrudRepository<JdbcDomain, String> {
      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByReferenceId_migrated(refId, refType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JdbcDomain> findAllByReferenceId(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType) {
    return RxJava2Adapter.fluxToFlowable(findAllByReferenceId_migrated(refId, refType));
}
default Flux<JdbcDomain> findAllByReferenceId_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType) {
    return RxJava2Adapter.flowableToFlux(findAllByReferenceId(refId, refType));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByHrid_migrated(refId, refType, hrid))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JdbcDomain> findByHrid(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "hrid")
String hrid) {
    return RxJava2Adapter.monoToMaybe(findByHrid_migrated(refId, refType, hrid));
}
default Mono<JdbcDomain> findByHrid_migrated(@Param(value = "refId")
String refId, @Param(value = "refType")
String refType, @Param(value = "hrid")
String hrid) {
    return RxJava2Adapter.maybeToMono(findByHrid(refId, refType, hrid));
}
}

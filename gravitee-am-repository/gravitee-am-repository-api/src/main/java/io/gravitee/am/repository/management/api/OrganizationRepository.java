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
import io.gravitee.am.model.Organization;
import io.gravitee.am.repository.common.CrudRepository;


import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface OrganizationRepository extends CrudRepository<Organization, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.count_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> count() {
    return RxJava2Adapter.monoToSingle(count_migrated());
}
default reactor.core.publisher.Mono<java.lang.Long> count_migrated() {
    return RxJava2Adapter.singleToMono(count());
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByHrids_migrated(hrids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Organization> findByHrids(java.util.List<java.lang.String> hrids) {
    return RxJava2Adapter.fluxToFlowable(findByHrids_migrated(hrids));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Organization> findByHrids_migrated(List<String> hrids) {
    return RxJava2Adapter.flowableToFlux(findByHrids(hrids));
}
}

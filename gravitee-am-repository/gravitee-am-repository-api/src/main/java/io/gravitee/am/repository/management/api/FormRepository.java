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
import io.gravitee.am.model.Form;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.common.CrudRepository;




import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface FormRepository extends CrudRepository<Form, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Form> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Form> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Form> findAll(io.gravitee.am.model.ReferenceType referenceType) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Form> findAll_migrated(ReferenceType referenceType) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClient_migrated(referenceType, referenceId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Form> findByClient(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String client) {
    return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Form> findByClient_migrated(ReferenceType referenceType, String referenceId, String client) {
    return RxJava2Adapter.flowableToFlux(findByClient(referenceType, referenceId, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(referenceType, referenceId, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Form> findByTemplate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template) {
    return RxJava2Adapter.maybeToMono(findByTemplate(referenceType, referenceId, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Form> findByClientAndTemplate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String client, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template) {
    return RxJava2Adapter.maybeToMono(findByClientAndTemplate(referenceType, referenceId, client, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Form> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, id));
}

}

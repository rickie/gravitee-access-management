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
package io.gravitee.am.management.service;

import com.google.errorprone.annotations.InlineMe;



import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface PolicyPluginService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.service.model.plugin.PolicyPlugin>> findAll() {
    return RxJava2Adapter.monoToSingle(findAll_migrated());
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.service.model.plugin.PolicyPlugin>> findAll_migrated() {
    return RxJava2Adapter.singleToMono(findAll());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(expand))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.service.model.plugin.PolicyPlugin>> findAll(java.util.List<java.lang.String> expand) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(expand));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.service.model.plugin.PolicyPlugin>> findAll_migrated(List<String> expand) {
    return RxJava2Adapter.singleToMono(findAll(expand));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(policyId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.service.model.plugin.PolicyPlugin> findById(java.lang.String policyId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(policyId));
}
default reactor.core.publisher.Mono<io.gravitee.am.service.model.plugin.PolicyPlugin> findById_migrated(String policyId) {
    return RxJava2Adapter.maybeToMono(findById(policyId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getSchema_migrated(policyId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<java.lang.String> getSchema(java.lang.String policyId) {
    return RxJava2Adapter.monoToMaybe(getSchema_migrated(policyId));
}
default reactor.core.publisher.Mono<java.lang.String> getSchema_migrated(String policyId) {
    return RxJava2Adapter.maybeToMono(getSchema(policyId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getIcon_migrated(policyId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<java.lang.String> getIcon(java.lang.String policyId) {
    return RxJava2Adapter.monoToMaybe(getIcon_migrated(policyId));
}
default reactor.core.publisher.Mono<java.lang.String> getIcon_migrated(String policyId) {
    return RxJava2Adapter.maybeToMono(getIcon(policyId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getDocumentation_migrated(policyId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<java.lang.String> getDocumentation(java.lang.String policyId) {
    return RxJava2Adapter.monoToMaybe(getDocumentation_migrated(policyId));
}
default reactor.core.publisher.Mono<java.lang.String> getDocumentation_migrated(String policyId) {
    return RxJava2Adapter.maybeToMono(getDocumentation(policyId));
}
}

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
import io.gravitee.am.service.model.plugin.PolicyPlugin;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface PolicyPluginService {

      
Mono<List<PolicyPlugin>> findAll_migrated();

      
Mono<List<PolicyPlugin>> findAll_migrated(List<String> expand);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(policyId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<PolicyPlugin> findById(String policyId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(policyId));
}
default Mono<PolicyPlugin> findById_migrated(String policyId) {
    return RxJava2Adapter.maybeToMono(findById(policyId));
}

      
Mono<String> getSchema_migrated(String policyId);

      
Mono<String> getIcon_migrated(String policyId);

      
Mono<String> getDocumentation_migrated(String policyId);
}

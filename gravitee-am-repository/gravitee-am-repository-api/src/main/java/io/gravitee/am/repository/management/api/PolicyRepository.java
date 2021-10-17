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


import io.gravitee.am.model.Policy;




import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * NOTE: only use for the PoliciesToFlowsUpgrader Upgrader
 * Use the {@link io.gravitee.am.repository.management.api.FlowRepository} for the flow management
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Deprecated
public interface PolicyRepository {

      
Flux<Policy> findAll_migrated();

      
Mono<Boolean> collectionExists_migrated();

      
Mono<Void> deleteCollection_migrated();
}

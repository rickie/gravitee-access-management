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
import io.gravitee.am.model.SystemTask;
import io.gravitee.am.repository.common.CrudRepository;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface SystemTaskRepository extends CrudRepository<SystemTask, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.updateIf_migrated(item, operationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.SystemTask> updateIf(io.gravitee.am.model.SystemTask item, java.lang.String operationId) {
    return RxJava2Adapter.monoToSingle(updateIf_migrated(item, operationId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.SystemTask> updateIf_migrated(SystemTask item, String operationId) {
    return RxJava2Adapter.singleToMono(updateIf(item, operationId));
}
}

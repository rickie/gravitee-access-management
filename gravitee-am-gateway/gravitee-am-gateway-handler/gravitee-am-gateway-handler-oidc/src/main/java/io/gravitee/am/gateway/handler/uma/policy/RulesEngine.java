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
package io.gravitee.am.gateway.handler.uma.policy;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.gateway.api.ExecutionContext;
import io.reactivex.Completable;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface RulesEngine {

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.fire_migrated(rules, executionContext))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable fire(java.util.List<io.gravitee.am.gateway.handler.uma.policy.Rule> rules, io.gravitee.gateway.api.ExecutionContext executionContext) {
    return RxJava2Adapter.monoToCompletable(fire_migrated(rules, executionContext));
}
default reactor.core.publisher.Mono<java.lang.Void> fire_migrated(List<Rule> rules, ExecutionContext executionContext) {
    return RxJava2Adapter.completableToMono(fire(rules, executionContext));
}
}

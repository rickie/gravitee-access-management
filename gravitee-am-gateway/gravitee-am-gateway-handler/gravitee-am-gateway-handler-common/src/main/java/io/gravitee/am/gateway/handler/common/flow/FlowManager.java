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
package io.gravitee.am.gateway.handler.common.flow;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.policy.ExtensionPoint;
import io.gravitee.am.gateway.policy.Policy;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.common.service.Service;
import io.reactivex.Single;
import java.util.List;
import java.util.function.Predicate;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface FlowManager extends Service {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByExtensionPoint_migrated(extensionPoint, client, filter))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.gateway.policy.Policy>> findByExtensionPoint(io.gravitee.am.common.policy.ExtensionPoint extensionPoint, io.gravitee.am.model.oidc.Client client, io.gravitee.am.gateway.handler.common.flow.FlowPredicate filter) {
    return RxJava2Adapter.monoToSingle(findByExtensionPoint_migrated(extensionPoint, client, filter));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.gateway.policy.Policy>> findByExtensionPoint_migrated(ExtensionPoint extensionPoint, Client client, FlowPredicate filter) {
    return RxJava2Adapter.singleToMono(findByExtensionPoint(extensionPoint, client, filter));
}

}

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
package io.gravitee.am.gateway.handler.oauth2.service.introspection;

import com.google.errorprone.annotations.InlineMe;

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface IntrospectionService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.introspect_migrated(request))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.oauth2.service.introspection.IntrospectionResponse> introspect(io.gravitee.am.gateway.handler.oauth2.service.introspection.IntrospectionRequest request) {
    return RxJava2Adapter.monoToSingle(introspect_migrated(request));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.oauth2.service.introspection.IntrospectionResponse> introspect_migrated(IntrospectionRequest request) {
    return RxJava2Adapter.singleToMono(introspect(request));
}
}

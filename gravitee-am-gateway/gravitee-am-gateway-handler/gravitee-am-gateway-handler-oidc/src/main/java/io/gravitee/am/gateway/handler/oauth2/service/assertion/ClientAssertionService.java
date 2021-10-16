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
package io.gravitee.am.gateway.handler.oauth2.service.assertion;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Maybe;
import io.vertx.reactivex.ext.web.RoutingContext;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * Client assertion as described for <a href="https://tools.ietf.org/html/rfc7521#section-4.2">oauth2 assertion framework</a>
 * and <a href="https://openid.net/specs/openid-connect-core-1_0.html#ClientAuthentication">openid client authentication specs</a>
 *
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface ClientAssertionService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.assertClient_migrated(assertionType, assertion, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.oidc.Client> assertClient(java.lang.String assertionType, java.lang.String assertion, java.lang.String basePath) {
    return RxJava2Adapter.monoToMaybe(assertClient_migrated(assertionType, assertion, basePath));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> assertClient_migrated(String assertionType, String assertion, String basePath) {
    return RxJava2Adapter.maybeToMono(assertClient(assertionType, assertion, basePath));
}
}

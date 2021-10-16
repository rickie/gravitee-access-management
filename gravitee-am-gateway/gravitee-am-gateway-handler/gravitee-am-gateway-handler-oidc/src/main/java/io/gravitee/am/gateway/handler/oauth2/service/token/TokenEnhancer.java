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
package io.gravitee.am.gateway.handler.oauth2.service.token;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.gateway.api.ExecutionContext;

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface TokenEnhancer {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enhance_migrated(accessToken, oAuth2Request, client, endUser, executionContext))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.oauth2.service.token.Token> enhance(io.gravitee.am.gateway.handler.oauth2.service.token.Token accessToken, io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request oAuth2Request, io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User endUser, io.gravitee.gateway.api.ExecutionContext executionContext) {
    return RxJava2Adapter.monoToSingle(enhance_migrated(accessToken, oAuth2Request, client, endUser, executionContext));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.oauth2.service.token.Token> enhance_migrated(Token accessToken, OAuth2Request oAuth2Request, Client client, User endUser, ExecutionContext executionContext) {
    return RxJava2Adapter.singleToMono(enhance(accessToken, oAuth2Request, client, endUser, executionContext));
}
}

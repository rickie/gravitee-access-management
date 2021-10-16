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
package io.gravitee.am.gateway.handler.oidc.service.flow;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.oauth2.service.request.AuthorizationRequest;
import io.gravitee.am.gateway.handler.oauth2.service.response.AuthorizationResponse;
import io.gravitee.am.model.AuthenticationFlowContext;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * OpenID Connect performs authentication to log in the End-User or to determine that the End-User is already logged in.
 * OpenID Connect returns the result of the Authentication performed by the Server to the Client in a secure manner so that the Client can rely on it.
 *
 * For this reason, the Client is called Relying Party (RP) in this case.
 *
 * The Authentication result is returned in an ID Token. It has Claims expressing such information as the Issuer, the Subject Identifier, when the authentication expires, etc.
 *
 * Authentication can follow one of three paths: the Authorization Code Flow (response_type=code), the Implicit Flow (response_type=id_token token or response_type=id_token), or the Hybrid Flow (using other Response Type values defined in OAuth 2.0 Multiple Response Type Encoding Practices [OAuth.Responses]).
 *
 * The flows determine how the ID Token and Access Token are returned to the Client.
 *
 * See <a href="https://openid.net/specs/openid-connect-core-1_0.html#Authentication">3. Authentication</a>
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface Flow {

    boolean handle(String responseType);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.run_migrated(authorizationRequest, client, endUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.oauth2.service.response.AuthorizationResponse> run(io.gravitee.am.gateway.handler.oauth2.service.request.AuthorizationRequest authorizationRequest, io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User endUser) {
    return RxJava2Adapter.monoToSingle(run_migrated(authorizationRequest, client, endUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.oauth2.service.response.AuthorizationResponse> run_migrated(AuthorizationRequest authorizationRequest, Client client, User endUser) {
    return RxJava2Adapter.singleToMono(run(authorizationRequest, client, endUser));
}
}

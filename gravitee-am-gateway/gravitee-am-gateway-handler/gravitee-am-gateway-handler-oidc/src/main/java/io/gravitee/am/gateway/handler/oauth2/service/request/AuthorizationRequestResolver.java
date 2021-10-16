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
package io.gravitee.am.gateway.handler.oauth2.service.request;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AuthorizationRequestResolver extends AbstractRequestResolver<AuthorizationRequest> {

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.resolve_migrated(authorizationRequest, client, endUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Single<AuthorizationRequest> resolve(AuthorizationRequest authorizationRequest, Client client, User endUser) {
 return RxJava2Adapter.monoToSingle(resolve_migrated(authorizationRequest, client, endUser));
}
public Mono<AuthorizationRequest> resolve_migrated(AuthorizationRequest authorizationRequest, Client client, User endUser) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(resolveAuthorizedScopes_migrated(authorizationRequest, client, endUser))).flatMap(request->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(resolveRedirectUri_migrated(request, client))))));
    }

    /**
     * redirect_uri request parameter is OPTIONAL, but the RFC (rfc6749) assumes that
     * the request fails due to a missing, invalid, or mismatching redirection URI.
     * If no redirect_uri request parameter is supplied, the client must at least have one registered redirect uri
     *
     * See <a href="https://tools.ietf.org/html/rfc6749#section-4.1.2.1">4.1.2.1. Error Response</a>
     *
     * @param authorizationRequest the authorization request to resolve
     * @param client the client which trigger the request
     * @return the authorization request
     */
    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.resolveRedirectUri_migrated(authorizationRequest, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Single<AuthorizationRequest> resolveRedirectUri(AuthorizationRequest authorizationRequest, Client client) {
 return RxJava2Adapter.monoToSingle(resolveRedirectUri_migrated(authorizationRequest, client));
}
public Mono<AuthorizationRequest> resolveRedirectUri_migrated(AuthorizationRequest authorizationRequest, Client client) {
        final String requestedRedirectUri = authorizationRequest.getRedirectUri();
        final List<String> registeredClientRedirectUris = client.getRedirectUris();
        // no redirect_uri parameter supplied, return the first client registered redirect uri
        if (requestedRedirectUri == null || requestedRedirectUri.isEmpty()) {
            authorizationRequest.setRedirectUri(registeredClientRedirectUris.iterator().next());
        }
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(authorizationRequest)));
    }
}

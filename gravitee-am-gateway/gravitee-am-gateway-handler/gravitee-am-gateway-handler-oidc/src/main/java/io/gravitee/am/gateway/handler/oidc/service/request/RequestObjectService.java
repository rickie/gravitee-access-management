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
package io.gravitee.am.gateway.handler.oidc.service.request;

import com.google.errorprone.annotations.InlineMe;

import io.gravitee.am.model.oidc.Client;

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Teams
 */
public interface RequestObjectService {

    /**
     * The URN prefix used by AS when storing Request Object
     */
    String RESOURCE_OBJECT_URN_PREFIX = "urn:ros:";

    /**
     * Validate encryption, signature and read the content of the JWT token.
     *
     * @param request
     * @param client
     * @param encRequired true if the request object has to be encrypted (JWE)
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.readRequestObject_migrated(request, client, encRequired))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<com.nimbusds.jwt.JWT> readRequestObject(java.lang.String request, io.gravitee.am.model.oidc.Client client, boolean encRequired) {
    return RxJava2Adapter.monoToSingle(readRequestObject_migrated(request, client, encRequired));
}
default reactor.core.publisher.Mono<com.nimbusds.jwt.JWT> readRequestObject_migrated(String request, Client client, boolean encRequired) {
    return RxJava2Adapter.singleToMono(readRequestObject(request, client, encRequired));
}

    /**
     * Validate encryption, signature and read the content of the JWT token from the URI.
     *
     * @param requestUri
     * @param client
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.readRequestObjectFromURI_migrated(requestUri, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<com.nimbusds.jwt.JWT> readRequestObjectFromURI(java.lang.String requestUri, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToSingle(readRequestObjectFromURI_migrated(requestUri, client));
}
default reactor.core.publisher.Mono<com.nimbusds.jwt.JWT> readRequestObjectFromURI_migrated(String requestUri, Client client) {
    return RxJava2Adapter.singleToMono(readRequestObjectFromURI(requestUri, client));
}

    /**
     * Register a request object for a given Client
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.registerRequestObject_migrated(request, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.oidc.service.request.RequestObjectRegistrationResponse> registerRequestObject(io.gravitee.am.gateway.handler.oidc.service.request.RequestObjectRegistrationRequest request, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToSingle(registerRequestObject_migrated(request, client));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.oidc.service.request.RequestObjectRegistrationResponse> registerRequestObject_migrated(RequestObjectRegistrationRequest request, Client client) {
    return RxJava2Adapter.singleToMono(registerRequestObject(request, client));
}
}

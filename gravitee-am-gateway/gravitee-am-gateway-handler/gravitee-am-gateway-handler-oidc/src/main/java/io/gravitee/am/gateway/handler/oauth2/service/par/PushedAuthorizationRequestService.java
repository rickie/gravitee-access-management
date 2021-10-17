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
package io.gravitee.am.gateway.handler.oauth2.service.par;

import com.google.errorprone.annotations.InlineMe;
import com.nimbusds.jwt.JWT;
import io.gravitee.am.gateway.handler.oauth2.service.par.PushedAuthorizationRequestResponse;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDProviderMetadata;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.oauth2.model.PushedAuthorizationRequest;
import io.reactivex.Completable;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface PushedAuthorizationRequestService {
    /**
     * The URN prefix used by AS when storing the Authorization Request parameters
     */
    String PAR_URN_PREFIX = "urn:ietf:params:oauth:request_uri:";

    /**
     * Read authorization parameters from the registered URI.
     * If the parameters contains the request one, the JWT is validated first and returned as it.
     * If request parameter is missing, JWT is build from other parameters as a PlainJWT
     * (with the aud claim targeting initialized with the value provider through the issuer OIDC metadata)
     *
     * @param requestUri
     * @param client
     * @return
     */
      
Mono<JWT> readFromURI_migrated(String requestUri, Client client, OpenIDProviderMetadata oidcMetadata);

    /**
     * Register a request object for a given Client
     * @return
     */
      
Mono<PushedAuthorizationRequestResponse> registerParameters_migrated(PushedAuthorizationRequest par, Client client);

    /**
     * Delete the PushedAuthorizationRequest entry from the repository
     *
     * @param uriIdentifier
     * @return
     */
      
Mono<Void> deleteRequestUri_migrated(String uriIdentifier);
}

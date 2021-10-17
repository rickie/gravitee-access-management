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
package io.gravitee.am.gateway.handler.oidc.service.jwe;

import com.google.errorprone.annotations.InlineMe;
import com.nimbusds.jwt.JWT;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface JWEService {

    /**
     * Encode raw JWT to JWT signed representation using id_token_encrypted_response_alg Client preferences.
     * @param signedJwt Signed JWT to encrypt
     * @param client client which want to encrypt the token
     * @return JWT encrypted string representation
     */
      
Mono<String> encryptIdToken_migrated(String signedJwt, Client client);

    /**
     * Encode raw JWT to JWT signed representation using userinfo_encrypted_response_alg Client preferences.
     * @param signedJwt Signed JWT to encrypt
     * @param client client which want to encrypt the token
     * @return JWT encrypted string representation
     */
      
Mono<String> encryptUserinfo_migrated(String signedJwt, Client client);

    /**
     *
     * @param jwt
     * @param client
     * @param encRequired true if the jwt has to be encrypted
     * @return the decoded JWT or an error if encRequired is true and the JWT isn't encoded
     */
      
Mono<JWT> decrypt_migrated(String jwt, Client client, boolean encRequired);

    /**
     * Decrypt JWT send by RP.
     * This decryption action will use a private key provided by the domain jwks
     *
     * @param jwt
     * @param encRequired true if the jwt has to be encrypted
     * @return the decoded JWT or an error if encRequired is true and the JWT isn't encoded
     */
      
Mono<JWT> decrypt_migrated(String jwt, boolean encRequired);

      
Mono<Boolean> isEncrypted_migrated(String jwt);

    /**
     * Encode raw JWT to JWT signed representation using authorization_encrypted_response_alg Client preferences.
     * @param signedJwt Signed JWT to encrypt
     * @param client client which want to encrypt the token
     * @return JWT encrypted string representation
     */
      
Mono<String> encryptAuthorization_migrated(String signedJwt, Client client);
}

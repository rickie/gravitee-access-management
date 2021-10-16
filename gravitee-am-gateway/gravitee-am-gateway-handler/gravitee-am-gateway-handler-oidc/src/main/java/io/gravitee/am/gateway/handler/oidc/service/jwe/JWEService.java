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
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encryptIdToken_migrated(signedJwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.String> encryptIdToken(java.lang.String signedJwt, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToSingle(encryptIdToken_migrated(signedJwt, client));
}
default reactor.core.publisher.Mono<java.lang.String> encryptIdToken_migrated(String signedJwt, Client client) {
    return RxJava2Adapter.singleToMono(encryptIdToken(signedJwt, client));
}

    /**
     * Encode raw JWT to JWT signed representation using userinfo_encrypted_response_alg Client preferences.
     * @param signedJwt Signed JWT to encrypt
     * @param client client which want to encrypt the token
     * @return JWT encrypted string representation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encryptUserinfo_migrated(signedJwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.String> encryptUserinfo(java.lang.String signedJwt, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToSingle(encryptUserinfo_migrated(signedJwt, client));
}
default reactor.core.publisher.Mono<java.lang.String> encryptUserinfo_migrated(String signedJwt, Client client) {
    return RxJava2Adapter.singleToMono(encryptUserinfo(signedJwt, client));
}

    /**
     *
     * @param jwt
     * @param client
     * @param encRequired true if the jwt has to be encrypted
     * @return the decoded JWT or an error if encRequired is true and the JWT isn't encoded
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.decrypt_migrated(jwt, client, encRequired))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<com.nimbusds.jwt.JWT> decrypt(java.lang.String jwt, io.gravitee.am.model.oidc.Client client, boolean encRequired) {
    return RxJava2Adapter.monoToSingle(decrypt_migrated(jwt, client, encRequired));
}
default reactor.core.publisher.Mono<com.nimbusds.jwt.JWT> decrypt_migrated(String jwt, Client client, boolean encRequired) {
    return RxJava2Adapter.singleToMono(decrypt(jwt, client, encRequired));
}

    /**
     * Decrypt JWT send by RP.
     * This decryption action will use a private key provided by the domain jwks
     *
     * @param jwt
     * @param encRequired true if the jwt has to be encrypted
     * @return the decoded JWT or an error if encRequired is true and the JWT isn't encoded
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.decrypt_migrated(jwt, encRequired))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<com.nimbusds.jwt.JWT> decrypt(java.lang.String jwt, boolean encRequired) {
    return RxJava2Adapter.monoToSingle(decrypt_migrated(jwt, encRequired));
}
default reactor.core.publisher.Mono<com.nimbusds.jwt.JWT> decrypt_migrated(String jwt, boolean encRequired) {
    return RxJava2Adapter.singleToMono(decrypt(jwt, encRequired));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.isEncrypted_migrated(jwt))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Boolean> isEncrypted(java.lang.String jwt) {
    return RxJava2Adapter.monoToSingle(isEncrypted_migrated(jwt));
}
default reactor.core.publisher.Mono<java.lang.Boolean> isEncrypted_migrated(String jwt) {
    return RxJava2Adapter.singleToMono(isEncrypted(jwt));
}

    /**
     * Encode raw JWT to JWT signed representation using authorization_encrypted_response_alg Client preferences.
     * @param signedJwt Signed JWT to encrypt
     * @param client client which want to encrypt the token
     * @return JWT encrypted string representation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encryptAuthorization_migrated(signedJwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.String> encryptAuthorization(java.lang.String signedJwt, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToSingle(encryptAuthorization_migrated(signedJwt, client));
}
default reactor.core.publisher.Mono<java.lang.String> encryptAuthorization_migrated(String signedJwt, Client client) {
    return RxJava2Adapter.singleToMono(encryptAuthorization(signedJwt, client));
}
}

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
package io.gravitee.am.gateway.handler.common.jwt;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.gateway.certificate.CertificateProvider;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;


/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface JWTService {

    /**
     * Encode raw JWT to JWT signed string representation
     * @param jwt JWT to encode
     * @param certificateProvider certificate provider used to sign the token
     * @return JWT signed string representation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encode_migrated(jwt, certificateProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<String> encode(JWT jwt, CertificateProvider certificateProvider) {
    return RxJava2Adapter.monoToSingle(encode_migrated(jwt, certificateProvider));
}
default Mono<String> encode_migrated(JWT jwt, CertificateProvider certificateProvider) {
    return RxJava2Adapter.singleToMono(encode(jwt, certificateProvider));
}

    /**
     * Encode raw JWT to JWT signed string representation
     * @param jwt JWT to encode
     * @param client client which want to sign the token
     * @return JWT signed string representation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encode_migrated(jwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<String> encode(JWT jwt, Client client) {
    return RxJava2Adapter.monoToSingle(encode_migrated(jwt, client));
}
default Mono<String> encode_migrated(JWT jwt, Client client) {
    return RxJava2Adapter.singleToMono(encode(jwt, client));
}

    /**
     * Encode raw JWT to JWT signed representation using userinfo_signed_response_alg Client preferences.
     * @param jwt JWT to encode
     * @param client client which want to sign the token
     * @return JWT signed string representation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encodeUserinfo_migrated(jwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<String> encodeUserinfo(JWT jwt, Client client) {
    return RxJava2Adapter.monoToSingle(encodeUserinfo_migrated(jwt, client));
}
default Mono<String> encodeUserinfo_migrated(JWT jwt, Client client) {
    return RxJava2Adapter.singleToMono(encodeUserinfo(jwt, client));
}

    /**
     * Encode raw JWT to JWT signed representation using authorization_signed_response_alg Client preferences.
     * @param jwt JWT to encode
     * @param client client which want to sign the token
     * @return JWT signed string representation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encodeAuthorization_migrated(jwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<String> encodeAuthorization(JWT jwt, Client client) {
    return RxJava2Adapter.monoToSingle(encodeAuthorization_migrated(jwt, client));
}
default Mono<String> encodeAuthorization_migrated(JWT jwt, Client client) {
    return RxJava2Adapter.singleToMono(encodeAuthorization(jwt, client));
}

    /**
     * Decode JWT signed string representation to JWT
     * @param jwt JWT to decode
     * @param client client which want to decode the token
     * @return JWT object
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.decodeAndVerify_migrated(jwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<JWT> decodeAndVerify(String jwt, Client client) {
    return RxJava2Adapter.monoToSingle(decodeAndVerify_migrated(jwt, client));
}
default Mono<JWT> decodeAndVerify_migrated(String jwt, Client client) {
    return RxJava2Adapter.singleToMono(decodeAndVerify(jwt, client));
}

    /**
     * Decode JWT signed string representation to JWT using the specified certificate provider.
     * @param jwt JWT to decode
     * @param certificateProvider the certificate provider to use to verify jwt signature.
     * @return JWT object
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.decodeAndVerify_migrated(jwt, certificateProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<JWT> decodeAndVerify(String jwt, CertificateProvider certificateProvider) {
    return RxJava2Adapter.monoToSingle(decodeAndVerify_migrated(jwt, certificateProvider));
}
default Mono<JWT> decodeAndVerify_migrated(String jwt, CertificateProvider certificateProvider) {
    return RxJava2Adapter.singleToMono(decodeAndVerify(jwt, certificateProvider));
}

    /**
     * Decode JWT signed string representation to JWT without signature verification
     * @param jwt JWT to decode
     * @return JWT object
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.decode_migrated(jwt))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<JWT> decode(String jwt) {
    return RxJava2Adapter.monoToSingle(decode_migrated(jwt));
}
default Mono<JWT> decode_migrated(String jwt) {
    return RxJava2Adapter.singleToMono(decode(jwt));
}
}

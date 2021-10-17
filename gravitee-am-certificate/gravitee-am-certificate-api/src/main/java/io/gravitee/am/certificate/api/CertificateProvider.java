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
package io.gravitee.am.certificate.api;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.certificate.api.CertificateKey;
import io.gravitee.am.model.jose.JWK;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CertificateProvider {

      
Mono<io.gravitee.am.certificate.api.Key> key_migrated();

      
Mono<String> publicKey_migrated();

      
Flux<JWK> privateKey_migrated();

      
Flux<JWK> keys_migrated();

    CertificateMetadata certificateMetadata();

    String signatureAlgorithm();

    default Certificate certificate() {
        return null;
    }

      default Mono<List<CertificateKey>> publicKeys_migrated() {
        return Mono.just(Collections.emptyList());
    }
}

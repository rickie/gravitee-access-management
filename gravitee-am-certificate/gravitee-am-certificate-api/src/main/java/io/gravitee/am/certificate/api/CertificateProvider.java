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

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.key_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.certificate.api.Key> key() {
    return RxJava2Adapter.monoToSingle(key_migrated());
}
default Mono<io.gravitee.am.certificate.api.Key> key_migrated() {
    return RxJava2Adapter.singleToMono(key());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.publicKey_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<String> publicKey() {
    return RxJava2Adapter.monoToSingle(publicKey_migrated());
}
default Mono<String> publicKey_migrated() {
    return RxJava2Adapter.singleToMono(publicKey());
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.privateKey_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JWK> privateKey() {
    return RxJava2Adapter.fluxToFlowable(privateKey_migrated());
}
default Flux<JWK> privateKey_migrated() {
    return RxJava2Adapter.flowableToFlux(privateKey());
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.keys_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<JWK> keys() {
    return RxJava2Adapter.fluxToFlowable(keys_migrated());
}
default Flux<JWK> keys_migrated() {
    return RxJava2Adapter.flowableToFlux(keys());
}

    CertificateMetadata certificateMetadata();

    String signatureAlgorithm();

    default Certificate certificate() {
        return null;
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.publicKeys_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<CertificateKey>> publicKeys() {
    return RxJava2Adapter.monoToSingle(publicKeys_migrated());
}default Mono<List<CertificateKey>> publicKeys_migrated() {
        return Mono.just(Collections.emptyList());
    }
}

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
package io.gravitee.am.gateway.handler.common.certificate;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.certificate.CertificateProvider;
import io.gravitee.common.service.Service;
import io.reactivex.Maybe;
import java.util.Collection;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CertificateManager extends io.gravitee.am.certificate.api.CertificateManager, Service {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.get_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<CertificateProvider> get(String id) {
    return RxJava2Adapter.monoToMaybe(get_migrated(id));
}
default Mono<CertificateProvider> get_migrated(String id) {
    return RxJava2Adapter.maybeToMono(get(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByAlgorithm_migrated(algorithm))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<CertificateProvider> findByAlgorithm(String algorithm) {
    return RxJava2Adapter.monoToMaybe(findByAlgorithm_migrated(algorithm));
}
default Mono<CertificateProvider> findByAlgorithm_migrated(String algorithm) {
    return RxJava2Adapter.maybeToMono(findByAlgorithm(algorithm));
}

    Collection<CertificateProvider> providers();

    CertificateProvider defaultCertificateProvider();

    CertificateProvider noneAlgorithmCertificateProvider();
}

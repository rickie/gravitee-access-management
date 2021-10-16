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
package io.gravitee.am.service;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.service.model.NewCertificate;
import io.gravitee.am.service.model.UpdateCertificate;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CertificateService {

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Certificate> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Certificate> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Certificate> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Certificate> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Certificate> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Certificate> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

    /**
     * This method is used to create a default certificate (mainly used when creating a new domain).
     * @return
     */
      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Certificate> create(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Certificate> create_migrated(String domain) {
    return RxJava2Adapter.singleToMono(create(domain));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Certificate> create(java.lang.String domain, io.gravitee.am.service.model.NewCertificate newCertificate, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newCertificate, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Certificate> create_migrated(String domain, NewCertificate newCertificate, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newCertificate, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Certificate> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateCertificate updateCertificate, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateCertificate, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Certificate> update_migrated(String domain, String id, UpdateCertificate updateCertificate, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateCertificate, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Certificate> update(io.gravitee.am.model.Certificate certificate) {
    return RxJava2Adapter.monoToSingle(update_migrated(certificate));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Certificate> update_migrated(Certificate certificate) {
    return RxJava2Adapter.singleToMono(update(certificate));
}

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String certificateId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(certificateId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String certificateId, User principal) {
    return RxJava2Adapter.completableToMono(delete(certificateId, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Certificate> create(java.lang.String domain, io.gravitee.am.service.model.NewCertificate newCertificate) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newCertificate));
}default Mono<Certificate> create_migrated(String domain, NewCertificate newCertificate) {
        return RxJava2Adapter.singleToMono(create(domain, newCertificate, null));
    }

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Certificate> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateCertificate updateCertificate) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateCertificate));
}default Mono<Certificate> update_migrated(String domain, String id, UpdateCertificate updateCertificate) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateCertificate, null));
    }

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String certificateId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(certificateId));
}default Mono<Void> delete_migrated(String certificateId) {
        return RxJava2Adapter.completableToMono(delete(certificateId, null));
    }

}

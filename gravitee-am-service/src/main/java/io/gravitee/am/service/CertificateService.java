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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.service.model.NewCertificate;
import io.gravitee.am.service.model.UpdateCertificate;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CertificateService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Certificate> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Certificate> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Flux<Certificate> findAll_migrated();

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Certificate> findByDomain(String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default Flux<Certificate> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

    /**
     * This method is used to create a default certificate (mainly used when creating a new domain).
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Certificate> create(String domain) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain));
}
default Mono<Certificate> create_migrated(String domain) {
    return RxJava2Adapter.singleToMono(create(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newCertificate, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Certificate> create(String domain, NewCertificate newCertificate, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newCertificate, principal));
}
default Mono<Certificate> create_migrated(String domain, NewCertificate newCertificate, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newCertificate, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateCertificate, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Certificate> update(String domain, String id, UpdateCertificate updateCertificate, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateCertificate, principal));
}
default Mono<Certificate> update_migrated(String domain, String id, UpdateCertificate updateCertificate, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateCertificate, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(certificate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Certificate> update(Certificate certificate) {
    return RxJava2Adapter.monoToSingle(update_migrated(certificate));
}
default Mono<Certificate> update_migrated(Certificate certificate) {
    return RxJava2Adapter.singleToMono(update(certificate));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(certificateId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String certificateId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(certificateId, principal));
}
default Mono<Void> delete_migrated(String certificateId, User principal) {
    return RxJava2Adapter.completableToMono(delete(certificateId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newCertificate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Certificate> create(String domain, NewCertificate newCertificate) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newCertificate));
}default Mono<Certificate> create_migrated(String domain, NewCertificate newCertificate) {
        return RxJava2Adapter.singleToMono(create(domain, newCertificate, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateCertificate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Certificate> update(String domain, String id, UpdateCertificate updateCertificate) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateCertificate));
}default Mono<Certificate> update_migrated(String domain, String id, UpdateCertificate updateCertificate) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateCertificate, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(certificateId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String certificateId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(certificateId));
}default Mono<Void> delete_migrated(String certificateId) {
        return RxJava2Adapter.completableToMono(delete(certificateId, null));
    }

}

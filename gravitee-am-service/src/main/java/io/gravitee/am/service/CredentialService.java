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
import io.gravitee.am.model.Credential;
import io.gravitee.am.model.ReferenceType;
import io.reactivex.Completable;

import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CredentialService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Credential> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Credential> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Flux<Credential> findByUserId_migrated(ReferenceType referenceType, String referenceId, String userId);

      
Flux<Credential> findByUsername_migrated(ReferenceType referenceType, String referenceId, String username);

      
Flux<Credential> findByCredentialId_migrated(ReferenceType referenceType, String referenceId, String credentialId);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(credential))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Credential> create(Credential credential) {
    return RxJava2Adapter.monoToSingle(create_migrated(credential));
}
default Mono<Credential> create_migrated(Credential credential) {
    return RxJava2Adapter.singleToMono(create(credential));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(credential))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Credential> update(Credential credential) {
    return RxJava2Adapter.monoToSingle(update_migrated(credential));
}
default Mono<Credential> update_migrated(Credential credential) {
    return RxJava2Adapter.singleToMono(update(credential));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.update_migrated(referenceType, referenceId, credentialId, credential))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable update(ReferenceType referenceType, String referenceId, String credentialId, Credential credential) {
    return RxJava2Adapter.monoToCompletable(update_migrated(referenceType, referenceId, credentialId, credential));
}
default Mono<Void> update_migrated(ReferenceType referenceType, String referenceId, String credentialId, Credential credential) {
    return RxJava2Adapter.completableToMono(update(referenceType, referenceId, credentialId, credential));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
default Mono<Void> delete_migrated(String id) {
    return RxJava2Adapter.completableToMono(delete(id));
}

      
Mono<Void> deleteByUserId_migrated(ReferenceType referenceType, String referenceId, String userId);

      
Mono<Void> deleteByAaguid_migrated(ReferenceType referenceType, String referenceId, String aaguid);

}

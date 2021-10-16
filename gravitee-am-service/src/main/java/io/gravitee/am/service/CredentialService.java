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





import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CredentialService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Credential> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Credential> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByUserId_migrated(referenceType, referenceId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Credential> findByUserId(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String userId) {
    return RxJava2Adapter.fluxToFlowable(findByUserId_migrated(referenceType, referenceId, userId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Credential> findByUserId_migrated(ReferenceType referenceType, String referenceId, String userId) {
    return RxJava2Adapter.flowableToFlux(findByUserId(referenceType, referenceId, userId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByUsername_migrated(referenceType, referenceId, username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Credential> findByUsername(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String username) {
    return RxJava2Adapter.fluxToFlowable(findByUsername_migrated(referenceType, referenceId, username));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Credential> findByUsername_migrated(ReferenceType referenceType, String referenceId, String username) {
    return RxJava2Adapter.flowableToFlux(findByUsername(referenceType, referenceId, username));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCredentialId_migrated(referenceType, referenceId, credentialId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Credential> findByCredentialId(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String credentialId) {
    return RxJava2Adapter.fluxToFlowable(findByCredentialId_migrated(referenceType, referenceId, credentialId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Credential> findByCredentialId_migrated(ReferenceType referenceType, String referenceId, String credentialId) {
    return RxJava2Adapter.flowableToFlux(findByCredentialId(referenceType, referenceId, credentialId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(credential))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Credential> create(io.gravitee.am.model.Credential credential) {
    return RxJava2Adapter.monoToSingle(create_migrated(credential));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Credential> create_migrated(Credential credential) {
    return RxJava2Adapter.singleToMono(create(credential));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(credential))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Credential> update(io.gravitee.am.model.Credential credential) {
    return RxJava2Adapter.monoToSingle(update_migrated(credential));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Credential> update_migrated(Credential credential) {
    return RxJava2Adapter.singleToMono(update(credential));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.update_migrated(referenceType, referenceId, credentialId, credential))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable update(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String credentialId, io.gravitee.am.model.Credential credential) {
    return RxJava2Adapter.monoToCompletable(update_migrated(referenceType, referenceId, credentialId, credential));
}
default reactor.core.publisher.Mono<java.lang.Void> update_migrated(ReferenceType referenceType, String referenceId, String credentialId, Credential credential) {
    return RxJava2Adapter.completableToMono(update(referenceType, referenceId, credentialId, credential));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String id) {
    return RxJava2Adapter.completableToMono(delete(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByUserId_migrated(referenceType, referenceId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable deleteByUserId(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String userId) {
    return RxJava2Adapter.monoToCompletable(deleteByUserId_migrated(referenceType, referenceId, userId));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteByUserId_migrated(ReferenceType referenceType, String referenceId, String userId) {
    return RxJava2Adapter.completableToMono(deleteByUserId(referenceType, referenceId, userId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByAaguid_migrated(referenceType, referenceId, aaguid))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable deleteByAaguid(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String aaguid) {
    return RxJava2Adapter.monoToCompletable(deleteByAaguid_migrated(referenceType, referenceId, aaguid));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteByAaguid_migrated(ReferenceType referenceType, String referenceId, String aaguid) {
    return RxJava2Adapter.completableToMono(deleteByAaguid(referenceType, referenceId, aaguid));
}

}

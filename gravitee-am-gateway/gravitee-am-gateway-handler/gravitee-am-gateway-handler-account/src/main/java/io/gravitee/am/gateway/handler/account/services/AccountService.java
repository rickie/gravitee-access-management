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
package io.gravitee.am.gateway.handler.account.services;

import com.google.errorprone.annotations.InlineMe;


import io.gravitee.am.model.User;

import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.reporter.api.audit.AuditReportableCriteria;





import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AccountService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.get_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> get(java.lang.String userId) {
    return RxJava2Adapter.monoToMaybe(get_migrated(userId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> get_migrated(String userId) {
    return RxJava2Adapter.maybeToMono(get(userId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getActivity_migrated(user, criteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.reporter.api.audit.model.Audit>> getActivity(io.gravitee.am.model.User user, io.gravitee.am.reporter.api.audit.AuditReportableCriteria criteria, int page, int size) {
    return RxJava2Adapter.monoToSingle(getActivity_migrated(user, criteria, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.reporter.api.audit.model.Audit>> getActivity_migrated(User user, AuditReportableCriteria criteria, int page, int size) {
    return RxJava2Adapter.singleToMono(getActivity(user, criteria, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> update(io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(update_migrated(user));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> update_migrated(User user) {
    return RxJava2Adapter.singleToMono(update(user));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.upsertFactor_migrated(userId, enrolledFactor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> upsertFactor(java.lang.String userId, io.gravitee.am.model.factor.EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(upsertFactor_migrated(userId, enrolledFactor, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> upsertFactor_migrated(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(upsertFactor(userId, enrolledFactor, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.removeFactor_migrated(userId, factorId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable removeFactor(java.lang.String userId, java.lang.String factorId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(removeFactor_migrated(userId, factorId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> removeFactor_migrated(String userId, String factorId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(removeFactor(userId, factorId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getFactors_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.Factor>> getFactors(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(getFactors_migrated(domain));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.Factor>> getFactors_migrated(String domain) {
    return RxJava2Adapter.singleToMono(getFactors(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getFactor_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Factor> getFactor(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(getFactor_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Factor> getFactor_migrated(String id) {
    return RxJava2Adapter.maybeToMono(getFactor(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getWebAuthnCredentials_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.Credential>> getWebAuthnCredentials(io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(getWebAuthnCredentials_migrated(user));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.Credential>> getWebAuthnCredentials_migrated(User user) {
    return RxJava2Adapter.singleToMono(getWebAuthnCredentials(user));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getWebAuthnCredential_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Credential> getWebAuthnCredential(java.lang.String id) {
    return RxJava2Adapter.monoToSingle(getWebAuthnCredential_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Credential> getWebAuthnCredential_migrated(String id) {
    return RxJava2Adapter.singleToMono(getWebAuthnCredential(id));
}
}

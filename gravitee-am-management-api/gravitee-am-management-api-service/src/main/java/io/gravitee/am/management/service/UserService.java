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
package io.gravitee.am.management.service;

import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Organization;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserService extends CommonUserService {

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.User>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.User> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> create(io.gravitee.am.model.Domain domain, io.gravitee.am.service.model.NewUser newUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newUser, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> create_migrated(Domain domain, NewUser newUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newUser, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateUser, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> update_migrated(String domain, String id, UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateUser, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> updateStatus(java.lang.String domain, java.lang.String id, boolean status, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(updateStatus_migrated(domain, id, status, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> updateStatus_migrated(String domain, String id, boolean status, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(updateStatus(domain, id, status, principal));
}

      @Deprecated  
default io.reactivex.Completable resetPassword(io.gravitee.am.model.Domain domain, java.lang.String userId, java.lang.String password, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(resetPassword_migrated(domain, userId, password, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> resetPassword_migrated(Domain domain, String userId, String password, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(resetPassword(domain, userId, password, principal));
}

      @Deprecated  
default io.reactivex.Completable sendRegistrationConfirmation(java.lang.String domain, java.lang.String userId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(sendRegistrationConfirmation_migrated(domain, userId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> sendRegistrationConfirmation_migrated(String domain, String userId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(sendRegistrationConfirmation(domain, userId, principal));
}

      @Deprecated  
default io.reactivex.Completable unlock(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String userId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(unlock_migrated(referenceType, referenceId, userId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> unlock_migrated(ReferenceType referenceType, String referenceId, String userId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(unlock(referenceType, referenceId, userId, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> assignRoles(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String userId, java.util.List<java.lang.String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(assignRoles_migrated(referenceType, referenceId, userId, roles, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> assignRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(assignRoles(referenceType, referenceId, userId, roles, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> revokeRoles(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String userId, java.util.List<java.lang.String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, userId, roles, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(revokeRoles(referenceType, referenceId, userId, roles, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> enrollFactors(java.lang.String userId, java.util.List<io.gravitee.am.model.factor.EnrolledFactor> factors, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(enrollFactors_migrated(userId, factors, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> enrollFactors_migrated(String userId, List<EnrolledFactor> factors, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(enrollFactors(userId, factors, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateUser updateUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateUser));
}default Mono<User> update_migrated(String domain, String id, UpdateUser updateUser) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateUser, null));
    }

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> updateStatus(java.lang.String domain, java.lang.String userId, boolean status) {
    return RxJava2Adapter.monoToSingle(updateStatus_migrated(domain, userId, status));
}default Mono<User> updateStatus_migrated(String domain, String userId, boolean status) {
        return RxJava2Adapter.singleToMono(updateStatus(domain, userId, status, null));
    }
      @Deprecated  
default io.reactivex.Completable unlock(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String userId) {
    return RxJava2Adapter.monoToCompletable(unlock_migrated(referenceType, referenceId, userId));
}default Mono<Void> unlock_migrated(ReferenceType referenceType, String referenceId, String userId) {
        return RxJava2Adapter.completableToMono(unlock(referenceType, referenceId, userId, null));
    }

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> assignRoles(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String userId, java.util.List<java.lang.String> roles) {
    return RxJava2Adapter.monoToSingle(assignRoles_migrated(referenceType, referenceId, userId, roles));
}default Mono<User> assignRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles) {
        return RxJava2Adapter.singleToMono(assignRoles(referenceType, referenceId, userId, roles, null));
    }

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> revokeRoles(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String userId, java.util.List<java.lang.String> roles) {
    return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, userId, roles));
}default Mono<User> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles) {
        return RxJava2Adapter.singleToMono(revokeRoles(referenceType, referenceId, userId, roles, null));
    }

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> enrollFactors(java.lang.String userId, java.util.List<io.gravitee.am.model.factor.EnrolledFactor> factors) {
    return RxJava2Adapter.monoToSingle(enrollFactors_migrated(userId, factors));
}default Mono<User> enrollFactors_migrated(String userId, List<EnrolledFactor> factors) {
        return RxJava2Adapter.singleToMono(enrollFactors(userId, factors, null));
    }

}

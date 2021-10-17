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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;

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

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<io.gravitee.am.model.User>> findByDomain(String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default Mono<Page<io.gravitee.am.model.User>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<io.gravitee.am.model.User> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<io.gravitee.am.model.User> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newUser, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> create(Domain domain, NewUser newUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newUser, principal));
}
default Mono<io.gravitee.am.model.User> create_migrated(Domain domain, NewUser newUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newUser, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateUser, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> update(String domain, String id, UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateUser, principal));
}
default Mono<io.gravitee.am.model.User> update_migrated(String domain, String id, UpdateUser updateUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateUser, principal));
}

      
Mono<io.gravitee.am.model.User> updateStatus_migrated(String domain, String id, boolean status, io.gravitee.am.identityprovider.api.User principal);

      
Mono<Void> resetPassword_migrated(Domain domain, String userId, String password, io.gravitee.am.identityprovider.api.User principal);

      
Mono<Void> sendRegistrationConfirmation_migrated(String domain, String userId, io.gravitee.am.identityprovider.api.User principal);

      
Mono<Void> unlock_migrated(ReferenceType referenceType, String referenceId, String userId, io.gravitee.am.identityprovider.api.User principal);

      
Mono<io.gravitee.am.model.User> assignRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.revokeRoles_migrated(referenceType, referenceId, userId, roles, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> revokeRoles(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, userId, roles, principal));
}
default Mono<io.gravitee.am.model.User> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(revokeRoles(referenceType, referenceId, userId, roles, principal));
}

      
Mono<io.gravitee.am.model.User> enrollFactors_migrated(String userId, List<EnrolledFactor> factors, io.gravitee.am.identityprovider.api.User principal);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> update(String domain, String id, UpdateUser updateUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateUser));
}default Mono<User> update_migrated(String domain, String id, UpdateUser updateUser) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateUser, null));
    }

      Mono<User> updateStatus_migrated(String domain, String userId, boolean status);
      Mono<Void> unlock_migrated(ReferenceType referenceType, String referenceId, String userId);

      Mono<User> assignRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.revokeRoles_migrated(referenceType, referenceId, userId, roles))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> revokeRoles(ReferenceType referenceType, String referenceId, String userId, List<String> roles) {
    return RxJava2Adapter.monoToSingle(revokeRoles_migrated(referenceType, referenceId, userId, roles));
}default Mono<User> revokeRoles_migrated(ReferenceType referenceType, String referenceId, String userId, List<String> roles) {
        return RxJava2Adapter.singleToMono(revokeRoles(referenceType, referenceId, userId, roles, null));
    }

      Mono<User> enrollFactors_migrated(String userId, List<EnrolledFactor> factors);

}

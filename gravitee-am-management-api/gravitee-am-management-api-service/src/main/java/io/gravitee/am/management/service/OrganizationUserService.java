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
import io.gravitee.am.model.Organization;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.service.model.NewUser;


import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface OrganizationUserService extends CommonUserService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, newUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> createOrUpdate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.service.model.NewUser newUser) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, newUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, NewUser newUser) {
    return RxJava2Adapter.singleToMono(createOrUpdate(referenceType, referenceId, newUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createGraviteeUser_migrated(organization, newUser, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> createGraviteeUser(io.gravitee.am.model.Organization organization, io.gravitee.am.service.model.NewUser newUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(createGraviteeUser_migrated(organization, newUser, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> createGraviteeUser_migrated(Organization organization, NewUser newUser, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(createGraviteeUser(organization, newUser, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.resetPassword_migrated(organizationId, user, password, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable resetPassword(java.lang.String organizationId, io.gravitee.am.model.User user, java.lang.String password, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(resetPassword_migrated(organizationId, user, password, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> resetPassword_migrated(String organizationId, User user, String password, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(resetPassword(organizationId, user, password, principal));
}
}

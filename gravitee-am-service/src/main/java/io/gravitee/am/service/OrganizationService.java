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

import io.gravitee.am.service.model.NewOrganization;
import io.gravitee.am.service.model.PatchOrganization;





import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface OrganizationService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Organization> findById(java.lang.String organizationId) {
    return RxJava2Adapter.monoToSingle(findById_migrated(organizationId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Organization> findById_migrated(String organizationId) {
    return RxJava2Adapter.singleToMono(findById(organizationId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.createDefault_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Organization> createDefault() {
    return RxJava2Adapter.monoToMaybe(createDefault_migrated());
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Organization> createDefault_migrated() {
    return RxJava2Adapter.maybeToMono(createDefault());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(organizationId, newOrganization, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Organization> createOrUpdate(java.lang.String organizationId, io.gravitee.am.service.model.NewOrganization newOrganization, io.gravitee.am.identityprovider.api.User byUser) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(organizationId, newOrganization, byUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Organization> createOrUpdate_migrated(String organizationId, NewOrganization newOrganization, User byUser) {
    return RxJava2Adapter.singleToMono(createOrUpdate(organizationId, newOrganization, byUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(organizationId, patchOrganization, authenticatedUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Organization> update(java.lang.String organizationId, io.gravitee.am.service.model.PatchOrganization patchOrganization, io.gravitee.am.identityprovider.api.User authenticatedUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(organizationId, patchOrganization, authenticatedUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Organization> update_migrated(String organizationId, PatchOrganization patchOrganization, User authenticatedUser) {
    return RxJava2Adapter.singleToMono(update(organizationId, patchOrganization, authenticatedUser));
}
}
 
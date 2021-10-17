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
import io.gravitee.am.model.Organization;
import io.gravitee.am.service.model.NewOrganization;
import io.gravitee.am.service.model.PatchOrganization;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface OrganizationService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(organizationId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Organization> findById(String organizationId) {
    return RxJava2Adapter.monoToSingle(findById_migrated(organizationId));
}
default Mono<Organization> findById_migrated(String organizationId) {
    return RxJava2Adapter.singleToMono(findById(organizationId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.createDefault_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Organization> createDefault() {
    return RxJava2Adapter.monoToMaybe(createDefault_migrated());
}
default Mono<Organization> createDefault_migrated() {
    return RxJava2Adapter.maybeToMono(createDefault());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(organizationId, newOrganization, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Organization> createOrUpdate(String organizationId, NewOrganization newOrganization, User byUser) {
    return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(organizationId, newOrganization, byUser));
}
default Mono<Organization> createOrUpdate_migrated(String organizationId, NewOrganization newOrganization, User byUser) {
    return RxJava2Adapter.singleToMono(createOrUpdate(organizationId, newOrganization, byUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(organizationId, patchOrganization, authenticatedUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Organization> update(String organizationId, PatchOrganization patchOrganization, User authenticatedUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(organizationId, patchOrganization, authenticatedUser));
}
default Mono<Organization> update_migrated(String organizationId, PatchOrganization patchOrganization, User authenticatedUser) {
    return RxJava2Adapter.singleToMono(update(organizationId, patchOrganization, authenticatedUser));
}
}
 
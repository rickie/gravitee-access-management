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
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.gravitee.am.service.model.NewMembership;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface MembershipService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Membership> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Membership> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Flux<Membership> findByCriteria_migrated(ReferenceType referenceType, String referenceId, MembershipCriteria criteria);

      
Flux<Membership> findByReference_migrated(String referenceId, ReferenceType referenceType);

      
Flux<Membership> findByMember_migrated(String memberId, MemberType memberType);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.addOrUpdate_migrated(organizationId, membership, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Membership> addOrUpdate(String organizationId, Membership membership, User principal) {
    return RxJava2Adapter.monoToSingle(addOrUpdate_migrated(organizationId, membership, principal));
}
default Mono<Membership> addOrUpdate_migrated(String organizationId, Membership membership, User principal) {
    return RxJava2Adapter.singleToMono(addOrUpdate(organizationId, membership, principal));
}

      
Mono<Membership> setPlatformAdmin_migrated(String userId);

      
Mono<Map<String, Map<String, Object>>> getMetadata_migrated(List<Membership> memberships);

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(membershipId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String membershipId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(membershipId, principal));
}
default Mono<Void> delete_migrated(String membershipId, User principal) {
    return RxJava2Adapter.completableToMono(delete(membershipId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.addOrUpdate_migrated(organizationId, membership))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Membership> addOrUpdate(String organizationId, Membership membership) {
    return RxJava2Adapter.monoToSingle(addOrUpdate_migrated(organizationId, membership));
}default Mono<Membership> addOrUpdate_migrated(String organizationId, Membership membership) {
        return RxJava2Adapter.singleToMono(addOrUpdate(organizationId, membership, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(membershipId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String membershipId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(membershipId));
}default Mono<Void> delete_migrated(String membershipId) {
        return RxJava2Adapter.completableToMono(delete(membershipId, null));
    }

    /**
     * When adding membership to an application, some permissions are necessary on the application's domain.
     * These permissions are available through the DOMAIN_USER.
     * For convenience, to limit the number of actions an administrator must do to affect role on an application, the group or user will also inherit the DOMAIN_USER role on the application's domain.
     *
     * If the group or user already has a role on the domain, nothing is done.
     *
     * @see #addDomainUserRoleIfNecessary(String, String, String, NewMembership, User)
     */
      
Mono<Void> addDomainUserRoleIfNecessary_migrated(String organizationId, String environmentId, String domainId, NewMembership newMembership, User principal);

    /**
     * When adding membership to a domain, some permissions are necessary on the domain's environment.
     * These permissions are available through the ENVIRONMENT_USER.
     * For convenience, to limit the number of actions an administrator must do to affect role on a domain, the group or user will also inherit the ENVIRONMENT_USER role on the domain's environment.
     *
     * If the group or user already has a role on the environment, nothing is done.
     */
      
Mono<Void> addEnvironmentUserRoleIfNecessary_migrated(String organizationId, String environmentId, NewMembership newMembership, User principal);
}

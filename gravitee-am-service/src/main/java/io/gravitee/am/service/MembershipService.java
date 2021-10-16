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
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface MembershipService {

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Membership> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Membership> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Membership> findByCriteria(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.repository.management.api.search.MembershipCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findByCriteria_migrated(referenceType, referenceId, criteria));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Membership> findByCriteria_migrated(ReferenceType referenceType, String referenceId, MembershipCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findByCriteria(referenceType, referenceId, criteria));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Membership> findByReference(java.lang.String referenceId, io.gravitee.am.model.ReferenceType referenceType) {
    return RxJava2Adapter.fluxToFlowable(findByReference_migrated(referenceId, referenceType));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Membership> findByReference_migrated(String referenceId, ReferenceType referenceType) {
    return RxJava2Adapter.flowableToFlux(findByReference(referenceId, referenceType));
}

      @Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Membership> findByMember(java.lang.String memberId, io.gravitee.am.model.membership.MemberType memberType) {
    return RxJava2Adapter.fluxToFlowable(findByMember_migrated(memberId, memberType));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Membership> findByMember_migrated(String memberId, MemberType memberType) {
    return RxJava2Adapter.flowableToFlux(findByMember(memberId, memberType));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Membership> addOrUpdate(java.lang.String organizationId, io.gravitee.am.model.Membership membership, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(addOrUpdate_migrated(organizationId, membership, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Membership> addOrUpdate_migrated(String organizationId, Membership membership, User principal) {
    return RxJava2Adapter.singleToMono(addOrUpdate(organizationId, membership, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Membership> setPlatformAdmin(java.lang.String userId) {
    return RxJava2Adapter.monoToSingle(setPlatformAdmin_migrated(userId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Membership> setPlatformAdmin_migrated(String userId) {
    return RxJava2Adapter.singleToMono(setPlatformAdmin(userId));
}

      @Deprecated  
default io.reactivex.Single<java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Object>>> getMetadata(java.util.List<io.gravitee.am.model.Membership> memberships) {
    return RxJava2Adapter.monoToSingle(getMetadata_migrated(memberships));
}
default reactor.core.publisher.Mono<java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Object>>> getMetadata_migrated(List<Membership> memberships) {
    return RxJava2Adapter.singleToMono(getMetadata(memberships));
}

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String membershipId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(membershipId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String membershipId, User principal) {
    return RxJava2Adapter.completableToMono(delete(membershipId, principal));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Membership> addOrUpdate(java.lang.String organizationId, io.gravitee.am.model.Membership membership) {
    return RxJava2Adapter.monoToSingle(addOrUpdate_migrated(organizationId, membership));
}default Mono<Membership> addOrUpdate_migrated(String organizationId, Membership membership) {
        return RxJava2Adapter.singleToMono(addOrUpdate(organizationId, membership, null));
    }

      @Deprecated  
default io.reactivex.Completable delete(java.lang.String membershipId) {
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
      @Deprecated  
default io.reactivex.Completable addDomainUserRoleIfNecessary(java.lang.String organizationId, java.lang.String environmentId, java.lang.String domainId, io.gravitee.am.service.model.NewMembership newMembership, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(addDomainUserRoleIfNecessary_migrated(organizationId, environmentId, domainId, newMembership, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> addDomainUserRoleIfNecessary_migrated(String organizationId, String environmentId, String domainId, NewMembership newMembership, User principal) {
    return RxJava2Adapter.completableToMono(addDomainUserRoleIfNecessary(organizationId, environmentId, domainId, newMembership, principal));
}

    /**
     * When adding membership to a domain, some permissions are necessary on the domain's environment.
     * These permissions are available through the ENVIRONMENT_USER.
     * For convenience, to limit the number of actions an administrator must do to affect role on a domain, the group or user will also inherit the ENVIRONMENT_USER role on the domain's environment.
     *
     * If the group or user already has a role on the environment, nothing is done.
     */
      @Deprecated  
default io.reactivex.Completable addEnvironmentUserRoleIfNecessary(java.lang.String organizationId, java.lang.String environmentId, io.gravitee.am.service.model.NewMembership newMembership, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(addEnvironmentUserRoleIfNecessary_migrated(organizationId, environmentId, newMembership, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> addEnvironmentUserRoleIfNecessary_migrated(String organizationId, String environmentId, NewMembership newMembership, User principal) {
    return RxJava2Adapter.completableToMono(addEnvironmentUserRoleIfNecessary(organizationId, environmentId, newMembership, principal));
}
}

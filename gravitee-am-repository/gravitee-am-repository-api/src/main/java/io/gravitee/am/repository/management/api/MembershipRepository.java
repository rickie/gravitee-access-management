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
package io.gravitee.am.repository.management.api;

import io.gravitee.am.model.Membership;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.repository.common.CrudRepository;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface MembershipRepository extends CrudRepository<Membership, String> {

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
default io.reactivex.Flowable<io.gravitee.am.model.Membership> findByCriteria(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.repository.management.api.search.MembershipCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findByCriteria_migrated(referenceType, referenceId, criteria));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Membership> findByCriteria_migrated(ReferenceType referenceType, String referenceId, MembershipCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findByCriteria(referenceType, referenceId, criteria));
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Membership> findByReferenceAndMember(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.model.membership.MemberType memberType, java.lang.String memberId) {
    return RxJava2Adapter.monoToMaybe(findByReferenceAndMember_migrated(referenceType, referenceId, memberType, memberId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Membership> findByReferenceAndMember_migrated(ReferenceType referenceType, String referenceId, MemberType memberType, String memberId) {
    return RxJava2Adapter.maybeToMono(findByReferenceAndMember(referenceType, referenceId, memberType, memberId));
}
}

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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.repository.common.CrudRepository;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface MembershipRepository extends CrudRepository<Membership, String> {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReference_migrated(referenceId, referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Membership> findByReference(String referenceId, ReferenceType referenceType) {
    return RxJava2Adapter.fluxToFlowable(findByReference_migrated(referenceId, referenceType));
}
default Flux<Membership> findByReference_migrated(String referenceId, ReferenceType referenceType) {
    return RxJava2Adapter.flowableToFlux(findByReference(referenceId, referenceType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByMember_migrated(memberId, memberType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Membership> findByMember(String memberId, MemberType memberType) {
    return RxJava2Adapter.fluxToFlowable(findByMember_migrated(memberId, memberType));
}
default Flux<Membership> findByMember_migrated(String memberId, MemberType memberType) {
    return RxJava2Adapter.flowableToFlux(findByMember(memberId, memberType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCriteria_migrated(referenceType, referenceId, criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Membership> findByCriteria(ReferenceType referenceType, String referenceId, MembershipCriteria criteria) {
    return RxJava2Adapter.fluxToFlowable(findByCriteria_migrated(referenceType, referenceId, criteria));
}
default Flux<Membership> findByCriteria_migrated(ReferenceType referenceType, String referenceId, MembershipCriteria criteria) {
    return RxJava2Adapter.flowableToFlux(findByCriteria(referenceType, referenceId, criteria));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByReferenceAndMember_migrated(referenceType, referenceId, memberType, memberId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Membership> findByReferenceAndMember(ReferenceType referenceType, String referenceId, MemberType memberType, String memberId) {
    return RxJava2Adapter.monoToMaybe(findByReferenceAndMember_migrated(referenceType, referenceId, memberType, memberId));
}
default Mono<Membership> findByReferenceAndMember_migrated(ReferenceType referenceType, String referenceId, MemberType memberType, String memberId) {
    return RxJava2Adapter.maybeToMono(findByReferenceAndMember(referenceType, referenceId, memberType, memberId));
}
}

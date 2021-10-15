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
package io.gravitee.am.repository.mongodb.management;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.repository.management.api.MembershipRepository;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.gravitee.am.repository.mongodb.management.internal.model.MembershipMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoMembershipRepository extends AbstractManagementMongoRepository implements MembershipRepository {

    private static final String FIELD_MEMBER_ID = "memberId";
    private static final String FIELD_MEMBER_TYPE = "memberType";
    private static final String FIELD_ROLE = "role";
    private MongoCollection<MembershipMongo> membershipsCollection;

    @PostConstruct
    public void init() {
        membershipsCollection = mongoOperations.getCollection("memberships", MembershipMongo.class);
        super.init(membershipsCollection);
        super.createIndex(membershipsCollection, new Document(FIELD_REFERENCE_ID, 1).append(FIELD_REFERENCE_TYPE, 1));
        super.createIndex(membershipsCollection, new Document(FIELD_REFERENCE_ID, 1).append(FIELD_MEMBER_ID, 1));
        super.createIndex(membershipsCollection, new Document(FIELD_MEMBER_ID, 1).append(FIELD_MEMBER_TYPE, 1));
    }

    @Override
    public Flowable<Membership> findByReference(String referenceId, ReferenceType referenceType) {
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(membershipsCollection.find(and(eq(FIELD_REFERENCE_ID, referenceId), eq(FIELD_REFERENCE_TYPE, referenceType.name())))))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Flowable<Membership> findByMember(String memberId, MemberType memberType) {
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.from(membershipsCollection.find(and(eq(FIELD_MEMBER_ID, memberId), eq(FIELD_MEMBER_TYPE, memberType.name())))))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Flowable<Membership> findByCriteria(ReferenceType referenceType, String referenceId, MembershipCriteria criteria) {

        Bson eqReference = and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId));
        Bson eqGroupId = null;
        Bson eqUserId = null;

        if (criteria.getGroupIds().isPresent()) {
            eqGroupId = and(eq(FIELD_MEMBER_TYPE, MemberType.GROUP.name()), in(FIELD_MEMBER_ID, criteria.getGroupIds().get()));
        }

        if (criteria.getUserId().isPresent()) {
            eqUserId = and(eq(FIELD_MEMBER_TYPE, MemberType.USER.name()), eq(FIELD_MEMBER_ID, criteria.getUserId().get()));
        }

        if (criteria.getRoleId().isPresent()) {
            eqUserId = eq(FIELD_ROLE, criteria.getRoleId().get());
        }

        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(toBsonFilter(criteria.isLogicalOR(), eqGroupId, eqUserId)
                .map(filter -> and(eqReference, filter))
                .switchIfEmpty(Single.just(eqReference))).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(filter -> Flowable.fromPublisher(membershipsCollection.find(filter)))))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Maybe<Membership> findByReferenceAndMember(ReferenceType referenceType, String referenceId, MemberType memberType, String memberId) {
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(membershipsCollection.find(
                and(eq(FIELD_REFERENCE_TYPE, referenceType.name()), eq(FIELD_REFERENCE_ID, referenceId),
                        eq(FIELD_MEMBER_TYPE, memberType.name()), eq(FIELD_MEMBER_ID, memberId))).first()), BackpressureStrategy.BUFFER).next())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Maybe<Membership> findById(String id) {
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(membershipsCollection.find(eq(FIELD_ID, id)).first()), BackpressureStrategy.BUFFER).next())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Single<Membership> create(Membership item) {
        MembershipMongo membership = convert(item);
        membership.setId(membership.getId() == null ? RandomString.generate() : membership.getId());
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(membershipsCollection.insertOne(membership))).map(RxJavaReactorMigrationUtil.toJdkFunction(success -> convert(membership))));
    }

    @Override
    public Single<Membership> update(Membership item) {
        MembershipMongo membership = convert(item);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(membershipsCollection.replaceOne(eq(FIELD_ID, membership.getId()), membership))).map(RxJavaReactorMigrationUtil.toJdkFunction(success -> convert(membership))));
    }

    @Override
    public Completable delete(String id) {
        return RxJava2Adapter.monoToCompletable(Mono.from(membershipsCollection.deleteOne(eq(FIELD_ID, id))));
    }

    private Membership convert(MembershipMongo membershipMongo) {
        Membership membership = new Membership();
        membership.setId(membershipMongo.getId());
        membership.setDomain(membershipMongo.getDomain());
        membership.setMemberId(membershipMongo.getMemberId());
        membership.setMemberType(MemberType.valueOf(membershipMongo.getMemberType()));
        membership.setReferenceId(membershipMongo.getReferenceId());
        membership.setReferenceType(ReferenceType.valueOf(membershipMongo.getReferenceType()));
        membership.setRoleId(membershipMongo.getRole());
        membership.setCreatedAt(membershipMongo.getCreatedAt());
        membership.setUpdatedAt(membershipMongo.getUpdatedAt());
        return membership;
    }

    private MembershipMongo convert(Membership membership) {
        MembershipMongo membershipMongo = new MembershipMongo();
        membershipMongo.setId(membership.getId());
        membershipMongo.setDomain(membership.getDomain());
        membershipMongo.setMemberId(membership.getMemberId());
        membershipMongo.setMemberType(membership.getMemberType().name());
        membershipMongo.setReferenceId(membership.getReferenceId());
        membershipMongo.setReferenceType(membership.getReferenceType().name());
        membershipMongo.setRole(membership.getRoleId());
        membershipMongo.setCreatedAt(membership.getCreatedAt());
        membershipMongo.setUpdatedAt(membership.getUpdatedAt());
        return membershipMongo;
    }
}

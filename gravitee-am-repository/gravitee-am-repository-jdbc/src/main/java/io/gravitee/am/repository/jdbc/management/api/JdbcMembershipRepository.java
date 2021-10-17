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
package io.gravitee.am.repository.jdbc.management.api;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.from;



import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcMembership;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringMembershipRepository;
import io.gravitee.am.repository.management.api.MembershipRepository;
import io.gravitee.am.repository.management.api.search.MembershipCriteria;
import io.reactivex.Completable;

import io.reactivex.Maybe;
import io.reactivex.Single;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcMembershipRepository extends AbstractJdbcRepository implements MembershipRepository {
    @Autowired
    private SpringMembershipRepository membershipRepository;

    protected Membership toEntity(JdbcMembership entity) {
        return mapper.map(entity, Membership.class);
    }

    protected JdbcMembership toJdbcEntity(Membership entity) {
        return mapper.map(entity, JdbcMembership.class);
    }

    
@Override
    public Flux<Membership> findByReference_migrated(String referenceId, ReferenceType referenceType) {
        LOGGER.debug("findByReference({},{})", referenceId, referenceType);
        return this.membershipRepository.findByReference_migrated(referenceId, referenceType.name()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    
@Override
    public Flux<Membership> findByMember_migrated(String memberId, MemberType memberType) {
        LOGGER.debug("findByMember({},{})", memberId, memberType);
        return this.membershipRepository.findByMember_migrated(memberId, memberType.name()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    
@Override
    public Flux<Membership> findByCriteria_migrated(ReferenceType referenceType, String referenceId, MembershipCriteria criteria) {
        LOGGER.debug("findByCriteria({},{},{}", referenceType, referenceId, criteria);
        Criteria whereClause = Criteria.empty();
        Criteria groupClause = Criteria.empty();
        Criteria userClause = Criteria.empty();

        Criteria referenceClause = where("reference_id").is(referenceId).and(where("reference_type").is(referenceType.name()));

        if (criteria.getGroupIds().isPresent()) {
            groupClause = where("member_id").in(criteria.getGroupIds().get()).and(where("member_type").is(MemberType.GROUP.name()));
        }

        if (criteria.getUserId().isPresent()) {
            userClause = where("member_id").is(criteria.getUserId().get()).and(where("member_type").is(MemberType.USER.name()));
        }

        if (criteria.getRoleId().isPresent()) {
            userClause = where("role_id").is(criteria.getRoleId().get());
        }

        whereClause = whereClause.and(referenceClause.and(criteria.isLogicalOR() ? userClause.or(groupClause) : userClause.and(groupClause)));

        return dbClient.select()
                .from(JdbcMembership.class)
                .matching(from(whereClause))
                .as(JdbcMembership.class)
                .all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    
@Override
    public Mono<Membership> findByReferenceAndMember_migrated(ReferenceType referenceType, String referenceId, MemberType memberType, String memberId) {
        LOGGER.debug("findByReferenceAndMember({},{},{},{})", referenceType,referenceId,memberType,memberId);
        return this.membershipRepository.findByReferenceAndMember_migrated(referenceId, referenceType.name(), memberId, memberType.name()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Membership> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Membership> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(membershipRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Membership> create(Membership item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Membership> create_migrated(Membership item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create Membership with id {}", item.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcMembership.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        return action.flatMap(i->this.findById_migrated(item.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Membership> update(Membership item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Membership> update_migrated(Membership item) {
        LOGGER.debug("update membership with id {}", item.getId());
        return RxJava2Adapter.singleToMono(membershipRepository.save(toJdbcEntity(item))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("delete({})", id);
        return RxJava2Adapter.completableToMono(membershipRepository.deleteById(id));
    }
}

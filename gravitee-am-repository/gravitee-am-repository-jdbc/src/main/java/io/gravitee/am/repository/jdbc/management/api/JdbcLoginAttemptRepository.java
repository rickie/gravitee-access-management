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

import static java.time.ZoneOffset.UTC;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.from;
import static reactor.adapter.rxjava.RxJava2Adapter.*;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.LoginAttempt;
import io.gravitee.am.repository.jdbc.exceptions.RepositoryIllegalQueryException;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcLoginAttempt;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringLoginAttemptRepository;
import io.gravitee.am.repository.management.api.LoginAttemptRepository;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcLoginAttemptRepository extends AbstractJdbcRepository implements LoginAttemptRepository {
    @Autowired
    protected SpringLoginAttemptRepository loginAttemptRepository;

    protected LoginAttempt toEntity(JdbcLoginAttempt entity) {
        return mapper.map(entity, LoginAttempt.class);
    }

    protected JdbcLoginAttempt toJdbcEntity(LoginAttempt entity) {
        return mapper.map(entity, JdbcLoginAttempt.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByCriteria_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<LoginAttempt> findByCriteria(LoginAttemptCriteria criteria) {
 return RxJava2Adapter.monoToMaybe(findByCriteria_migrated(criteria));
}
@Override
    public Mono<LoginAttempt> findByCriteria_migrated(LoginAttemptCriteria criteria) {
        LOGGER.debug("findByCriteria({})", criteria);

        Criteria whereClause = buildWhereClause(criteria);

        DatabaseClient.TypedSelectSpec<JdbcLoginAttempt> from = dbClient.select()
                .from(JdbcLoginAttempt.class)
                .page(PageRequest.of(0, 1, Sort.by("id")));

        whereClause = whereClause.and(
                where("expire_at").greaterThan(LocalDateTime.now(UTC))
                .or(where("expire_at").isNull()));
        from = from.matching(from(whereClause));

        return from.as(JdbcLoginAttempt.class).first().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    private Criteria buildWhereClause(LoginAttemptCriteria criteria) {
        Criteria whereClause = Criteria.empty();
        // domain
        if (criteria.domain() != null && !criteria.domain().isEmpty()) {
            whereClause = whereClause.and(where("domain").is(criteria.domain()));
        }
        // client
        if (criteria.client() != null && !criteria.client().isEmpty()) {
            whereClause = whereClause.and(where("client").is(criteria.client()));
        }
        // idp
        if (criteria.identityProvider() != null && !criteria.identityProvider().isEmpty()) {
            whereClause = whereClause.and(where("identity_provider").is(criteria.identityProvider()));
        }
        // username
        if (criteria.username() != null && !criteria.username().isEmpty()) {
            whereClause = whereClause.and(where("username").is(criteria.username()));
        }
        return whereClause;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(LoginAttemptCriteria criteria) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(criteria));
}
@Override
    public Mono<Void> delete_migrated(LoginAttemptCriteria criteria) {
        LOGGER.debug("delete({})", criteria);

        Criteria whereClause = buildWhereClause(criteria);

        if (!whereClause.isEmpty()) {
            return dbClient.delete().from(JdbcLoginAttempt.class).matching(from(whereClause)).then();
        }

        throw new RepositoryIllegalQueryException("Unable to delete from LoginAttempt without criteria");
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<LoginAttempt> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<LoginAttempt> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.maybeToMono(loginAttemptRepository.findById(id)).filter(bean -> bean.getExpireAt() == null || bean.getExpireAt().isAfter(now)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<LoginAttempt> create(LoginAttempt item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<LoginAttempt> create_migrated(LoginAttempt item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create LoginAttempt with id {}", item.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcLoginAttempt.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        return action.flatMap(i->RxJava2Adapter.maybeToMono(loginAttemptRepository.findById(item.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<LoginAttempt> update(LoginAttempt item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<LoginAttempt> update_migrated(LoginAttempt item) {
        LOGGER.debug("update loginAttempt with id '{}'", item.getId());
        return RxJava2Adapter.singleToMono(loginAttemptRepository.save(toJdbcEntity(item))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
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
        return RxJava2Adapter.completableToMono(loginAttemptRepository.deleteById(id));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.purgeExpiredData_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Completable purgeExpiredData() {
 return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}
public Mono<Void> purgeExpiredData_migrated() {
        LOGGER.debug("purgeExpiredData()");
        LocalDateTime now = LocalDateTime.now(UTC);
        return dbClient.delete().from(JdbcLoginAttempt.class).matching(where("expire_at").lessThan(now)).then();
    }
}

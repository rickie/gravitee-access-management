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
package io.gravitee.am.repository.jdbc.oauth2.api;

import static java.time.ZoneOffset.UTC;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.from;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToCompletable;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcScopeApproval;
import io.gravitee.am.repository.jdbc.oauth2.api.spring.SpringScopeApprovalRepository;
import io.gravitee.am.repository.oauth2.api.ScopeApprovalRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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
public class JdbcScopeApprovalRepository extends AbstractJdbcRepository implements ScopeApprovalRepository {

    @Autowired
    private SpringScopeApprovalRepository scopeApprovalRepository;

    protected ScopeApproval toEntity(JdbcScopeApproval entity) {
        return mapper.map(entity, ScopeApproval.class);
    }

    protected JdbcScopeApproval toJdbcEntity(ScopeApproval entity) {
        return mapper.map(entity, JdbcScopeApproval.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndUserAndClient_migrated(domain, userId, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<ScopeApproval> findByDomainAndUserAndClient(String domain, String userId, String clientId) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndUserAndClient_migrated(domain, userId, clientId));
}
@Override
    public Flux<ScopeApproval> findByDomainAndUserAndClient_migrated(String domain, String userId, String clientId) {
        LOGGER.debug("findByDomainAndUserAndClient({}, {}, {})", domain, userId, clientId);
        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(scopeApprovalRepository.findByDomainAndUserAndClient_migrated(domain, userId, clientId))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(bean -> bean.getExpiresAt() == null || bean.getExpiresAt().isAfter(now))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndUser_migrated(domain, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<ScopeApproval> findByDomainAndUser(String domain, String user) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndUser_migrated(domain, user));
}
@Override
    public Flux<ScopeApproval> findByDomainAndUser_migrated(String domain, String user) {
        LOGGER.debug("findByDomainAndUser({}, {}, {})", domain, user);
        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(scopeApprovalRepository.findByDomainAndUser_migrated(domain, user))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(bean -> bean.getExpiresAt() == null || bean.getExpiresAt().isAfter(now))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.upsert_migrated(scopeApproval))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ScopeApproval> upsert(ScopeApproval scopeApproval) {
 return RxJava2Adapter.monoToSingle(upsert_migrated(scopeApproval));
}
@Override
    public Mono<ScopeApproval> upsert_migrated(ScopeApproval scopeApproval) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(scopeApprovalRepository.findByDomainAndUserAndClientAndScope_migrated(scopeApproval.getDomain(), scopeApproval.getUserId(), scopeApproval.getClientId(), scopeApproval.getScope()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()))
                .flatMapSingle(optionalApproval -> {
                    if (!optionalApproval.isPresent()) {
                        scopeApproval.setCreatedAt(new Date());
                        scopeApproval.setUpdatedAt(scopeApproval.getCreatedAt());
                        return RxJava2Adapter.monoToSingle(create_migrated(scopeApproval));
                    } else {
                        scopeApproval.setId(optionalApproval.get().getId());
                        scopeApproval.setUpdatedAt(new Date());
                        return RxJava2Adapter.monoToSingle(update_migrated(scopeApproval));
                    }
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainAndScopeKey_migrated(domain, scope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByDomainAndScopeKey(String domain, String scope) {
 return RxJava2Adapter.monoToCompletable(deleteByDomainAndScopeKey_migrated(domain, scope));
}
@Override
    public Mono<Void> deleteByDomainAndScopeKey_migrated(String domain, String scope) {
        LOGGER.debug("deleteByDomainAndScopeKey({}, {})", domain, scope);
        return dbClient.delete()
                .from(JdbcScopeApproval.class)
                .matching(from(where("domain").is(domain)
                        .and(where("scope").is(scope))))
                .fetch().rowsUpdated();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainAndUserAndClient_migrated(domain, user, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByDomainAndUserAndClient(String domain, String user, String client) {
 return RxJava2Adapter.monoToCompletable(deleteByDomainAndUserAndClient_migrated(domain, user, client));
}
@Override
    public Mono<Void> deleteByDomainAndUserAndClient_migrated(String domain, String user, String client) {
        LOGGER.debug("deleteByDomainAndUserAndClient({}, {}, {})", domain, user, client);
        return dbClient.delete()
                .from(JdbcScopeApproval.class)
                .matching(from(where("domain").is(domain)
                        .and(where("user_id").is(user)
                        .and(where("client_id").is(client)))))
                .fetch().rowsUpdated();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainAndUser_migrated(domain, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByDomainAndUser(String domain, String user) {
 return RxJava2Adapter.monoToCompletable(deleteByDomainAndUser_migrated(domain, user));
}
@Override
    public Mono<Void> deleteByDomainAndUser_migrated(String domain, String user) {
        LOGGER.debug("deleteByDomainAndUser({}, {})", domain, user);
        return dbClient.delete()
                .from(JdbcScopeApproval.class)
                .matching(from(where("domain").is(domain)
                        .and(where("user_id").is(user))))
                .fetch().rowsUpdated();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ScopeApproval> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<ScopeApproval> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.maybeToMono(scopeApprovalRepository.findById(id)).filter(RxJavaReactorMigrationUtil.toJdkPredicate(bean -> bean.getExpiresAt() == null || bean.getExpiresAt().isAfter(now))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ScopeApproval> create(ScopeApproval item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<ScopeApproval> create_migrated(ScopeApproval item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("Create ScopeApproval with id {}", item.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcScopeApproval.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        return action.flatMap(i->RxJava2Adapter.maybeToMono(scopeApprovalRepository.findById(item.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ScopeApproval> update(ScopeApproval item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<ScopeApproval> update_migrated(ScopeApproval item) {
        LOGGER.debug("Update ScopeApproval with id {}", item.getId());
        return RxJava2Adapter.singleToMono(scopeApprovalRepository.save(toJdbcEntity(item))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("Delete ScopeApproval with id {}", id);
        return RxJava2Adapter.completableToMono(scopeApprovalRepository.deleteById(id));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.purgeExpiredData_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Completable purgeExpiredData() {
 return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}
public Mono<Void> purgeExpiredData_migrated() {
        LOGGER.debug("purgeExpiredData()");
        LocalDateTime now = LocalDateTime.now(UTC);
        return dbClient.delete().from(JdbcScopeApproval.class).matching(where("expires_at").lessThan(now)).then();
    }
}

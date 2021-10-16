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



import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcRefreshToken;
import io.gravitee.am.repository.jdbc.oauth2.api.spring.SpringRefreshTokenRepository;
import io.gravitee.am.repository.oauth2.api.RefreshTokenRepository;
import io.gravitee.am.repository.oauth2.model.RefreshToken;
import io.reactivex.Completable;

import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.List;
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
public class JdbcRefreshTokenRepository extends AbstractJdbcRepository implements RefreshTokenRepository {

    @Autowired
    private SpringRefreshTokenRepository refreshTokenRepository;

    protected RefreshToken toEntity(JdbcRefreshToken entity) {
        return mapper.map(entity, RefreshToken.class);
    }

    protected JdbcRefreshToken toJdbcEntity(RefreshToken entity) {
        return mapper.map(entity, JdbcRefreshToken.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByToken_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<RefreshToken> findByToken(String token) {
 return RxJava2Adapter.monoToMaybe(findByToken_migrated(token));
}
@Override
    public Mono<RefreshToken> findByToken_migrated(String token) {
        LOGGER.debug("findByToken({token})", token);
        return refreshTokenRepository.findByToken_migrated(token, LocalDateTime.now(UTC)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to retrieve RefreshToken", error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(refreshToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<RefreshToken> create(RefreshToken refreshToken) {
 return RxJava2Adapter.monoToSingle(create_migrated(refreshToken));
}
@Override
    public Mono<RefreshToken> create_migrated(RefreshToken refreshToken) {
        refreshToken.setId(refreshToken.getId() == null ? RandomString.generate() : refreshToken.getId());
        LOGGER.debug("Create refreshToken with id {}", refreshToken.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcRefreshToken.class)
                .using(toJdbcEntity(refreshToken))
                .fetch().rowsUpdated();

        return action.flatMap(i->RxJava2Adapter.maybeToMono(refreshTokenRepository.findById(refreshToken.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).single()).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("Unable to create refreshToken with id {}", refreshToken.getId(), error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.bulkWrite_migrated(refreshTokens))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable bulkWrite(List<RefreshToken> refreshTokens) {
 return RxJava2Adapter.monoToCompletable(bulkWrite_migrated(refreshTokens));
}
@Override
    public Mono<Void> bulkWrite_migrated(List<RefreshToken> refreshTokens) {
        return Flux.fromIterable(refreshTokens).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(refreshToken -> RxJava2Adapter.fluxToFlowable(create_migrated(refreshToken).flux()))).ignoreElements().then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to bulk load refresh tokens", error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String token) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(token));
}
@Override
    public Mono<Void> delete_migrated(String token) {
        LOGGER.debug("delete({})", token);
        return dbClient.delete()
                .from(JdbcRefreshToken.class)
                .matching(from(where("token").is(token)))
                .fetch().rowsUpdated().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete RefreshToken", error))).then();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByUserId_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByUserId(String userId) {
 return RxJava2Adapter.monoToCompletable(deleteByUserId_migrated(userId));
}
@Override
    public Mono<Void> deleteByUserId_migrated(String userId) {
        LOGGER.debug("deleteByUserId({})", userId);
        return dbClient.delete()
                .from(JdbcRefreshToken.class)
                .matching(from(where("subject").is(userId)))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete refresh token with subject {}", userId, error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainIdClientIdAndUserId_migrated(domainId, clientId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByDomainIdClientIdAndUserId(String domainId, String clientId, String userId) {
 return RxJava2Adapter.monoToCompletable(deleteByDomainIdClientIdAndUserId_migrated(domainId, clientId, userId));
}
@Override
    public Mono<Void> deleteByDomainIdClientIdAndUserId_migrated(String domainId, String clientId, String userId) {
        LOGGER.debug("deleteByDomainIdClientIdAndUserId({},{},{})", domainId, clientId, userId);
        return dbClient.delete()
                .from(JdbcRefreshToken.class)
                .matching(from(where("subject").is(userId)
                                .and(where("domain").is(domainId))
                                .and(where("client").is(clientId))))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete refresh token with domain {}, client {} and subject {}",
                        domainId, clientId, userId, error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainIdAndUserId_migrated(domainId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByDomainIdAndUserId(String domainId, String userId) {
 return RxJava2Adapter.monoToCompletable(deleteByDomainIdAndUserId_migrated(domainId, userId));
}
@Override
    public Mono<Void> deleteByDomainIdAndUserId_migrated(String domainId, String userId) {
        LOGGER.debug("deleteByDomainIdAndUserId({},{})", domainId, userId);
        return dbClient.delete()
                .from(JdbcRefreshToken.class)
                .matching(from(where("subject").is(userId)
                                .and(where("domain").is(domainId))))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete refresh token with domain {} and subject {}",
                        domainId, userId, error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.purgeExpiredData_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Completable purgeExpiredData() {
 return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}
public Mono<Void> purgeExpiredData_migrated() {
        LOGGER.debug("purgeExpiredData()");
        LocalDateTime now = LocalDateTime.now(UTC);
        return dbClient.delete().from(JdbcRefreshToken.class).matching(where("expire_at").lessThan(now)).then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to purge refresh tokens", error)));
    }
}

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
import static reactor.adapter.rxjava.RxJava2Adapter.*;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;

import io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcAccessToken;
import io.gravitee.am.repository.jdbc.oauth2.api.spring.SpringAccessTokenRepository;
import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.repository.oauth2.model.AccessToken;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
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
public class JdbcAccessTokenRepository extends AbstractJdbcRepository implements AccessTokenRepository {
    @Autowired
    private SpringAccessTokenRepository accessTokenRepository;

    protected AccessToken toEntity(JdbcAccessToken entity) {
        return mapper.map(entity, AccessToken.class);
    }

    protected JdbcAccessToken toJdbcEntity(AccessToken entity) {
        return mapper.map(entity, JdbcAccessToken.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByToken_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AccessToken> findByToken(String token) {
 return RxJava2Adapter.monoToMaybe(findByToken_migrated(token));
}
@Override
    public Mono<AccessToken> findByToken_migrated(String token) {
        LOGGER.debug("findByToken({})", token);
        return accessTokenRepository.findByToken_migrated(token, LocalDateTime.now(UTC)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to retrieve AccessToken", error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(accessToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AccessToken> create(AccessToken accessToken) {
 return RxJava2Adapter.monoToSingle(create_migrated(accessToken));
}
@Override
    public Mono<AccessToken> create_migrated(AccessToken accessToken) {
        accessToken.setId(accessToken.getId() == null ? RandomString.generate() : accessToken.getId());
        LOGGER.debug("Create accessToken with id {}", accessToken.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcAccessToken.class)
                .using(toJdbcEntity(accessToken))
                .fetch().rowsUpdated();

        return action.flatMap(i->RxJava2Adapter.maybeToMono(accessTokenRepository.findById(accessToken.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).single()).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("Unable to create accessToken with id {}", accessToken.getId(), error)));
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
        return RxJava2Adapter.completableToMono(Completable.fromMaybe(RxJava2Adapter.monoToMaybe(findByToken_migrated(token).flatMap(z->dbClient.delete().from(JdbcAccessToken.class).matching(from(where("token").is(token))).fetch().rowsUpdated().map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Integer i)->z))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete AccessToken", error))))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.bulkWrite_migrated(accessTokens))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable bulkWrite(List<AccessToken> accessTokens) {
 return RxJava2Adapter.monoToCompletable(bulkWrite_migrated(accessTokens));
}
@Override
    public Mono<Void> bulkWrite_migrated(List<AccessToken> accessTokens) {
        return Flux.fromIterable(accessTokens).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(accessToken -> RxJava2Adapter.fluxToFlowable(create_migrated(accessToken).flux()))).ignoreElements().then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to bulk load access tokens", error)));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToObservable(this.findByClientIdAndSubject_migrated(clientId, subject))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Observable<AccessToken> findByClientIdAndSubject(String clientId, String subject) {
 return RxJava2Adapter.fluxToObservable(findByClientIdAndSubject_migrated(clientId, subject));
}
@Override
    public Flux<AccessToken> findByClientIdAndSubject_migrated(String clientId, String subject) {
        LOGGER.debug("findByClientIdAndSubject({}, {})", clientId, subject);
        return RxJava2Adapter.observableToFlux(RxJava2Adapter.fluxToFlowable(accessTokenRepository.findByClientIdAndSubject_migrated(clientId, subject, LocalDateTime.now(UTC)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .toObservable()
                .doOnError(error -> LOGGER.error("Unable to retrieve access tokens with client {} and subject {}",
                        clientId, subject, error)), BackpressureStrategy.BUFFER);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToObservable(this.findByClientId_migrated(clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Observable<AccessToken> findByClientId(String clientId) {
 return RxJava2Adapter.fluxToObservable(findByClientId_migrated(clientId));
}
@Override
    public Flux<AccessToken> findByClientId_migrated(String clientId) {
        LOGGER.debug("findByClientId({})", clientId);
        return RxJava2Adapter.observableToFlux(RxJava2Adapter.fluxToFlowable(accessTokenRepository.findByClientId_migrated(clientId, LocalDateTime.now(UTC)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .toObservable()
                .doOnError(error -> LOGGER.error("Unable to retrieve access tokens with client {}",
                        clientId, error)), BackpressureStrategy.BUFFER);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToObservable(this.findByAuthorizationCode_migrated(authorizationCode))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Observable<AccessToken> findByAuthorizationCode(String authorizationCode) {
 return RxJava2Adapter.fluxToObservable(findByAuthorizationCode_migrated(authorizationCode));
}
@Override
    public Flux<AccessToken> findByAuthorizationCode_migrated(String authorizationCode) {
        LOGGER.debug("findByAuthorizationCode({})", authorizationCode);
        return RxJava2Adapter.observableToFlux(RxJava2Adapter.fluxToFlowable(accessTokenRepository.findByAuthorizationCode_migrated(authorizationCode, LocalDateTime.now(UTC)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .toObservable()
                .doOnError(error -> LOGGER.error("Unable to retrieve access tokens with authorization code {}",
                        authorizationCode, error)), BackpressureStrategy.BUFFER);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByClientId_migrated(clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> countByClientId(String clientId) {
 return RxJava2Adapter.monoToSingle(countByClientId_migrated(clientId));
}
@Override
    public Mono<Long> countByClientId_migrated(String clientId) {
        return accessTokenRepository.countByClientId_migrated(clientId, LocalDateTime.now(UTC));
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
                .from(JdbcAccessToken.class)
                .matching(from(where("subject").is(userId)))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete access tokens with subject {}",
                userId, error)));
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
                .from(JdbcAccessToken.class)
                .matching(from(
                        where("subject").is(userId)
                                .and(where("domain").is(domainId))
                                .and(where("client").is(clientId))))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete access token with domain {}, client {} and subject {}",
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
                .from(JdbcAccessToken.class)
                .matching(from(
                        where("subject").is(userId)
                                .and(where("domain").is(domainId))))
                .then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete access tokens with domain {} and subject {}",
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
        return dbClient.delete().from(JdbcAccessToken.class).matching(where("expire_at").lessThan(now)).then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to purge access tokens", error)));
    }
}

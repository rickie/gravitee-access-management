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

    
@Override
    public Mono<AccessToken> findByToken_migrated(String token) {
        LOGGER.debug("findByToken({})", token);
        return accessTokenRepository.findByToken_migrated(token, LocalDateTime.now(UTC)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(error -> LOGGER.error("Unable to retrieve AccessToken", error));
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

        return action.flatMap(i->RxJava2Adapter.maybeToMono(accessTokenRepository.findById(accessToken.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).single()).doOnError((error) -> LOGGER.error("Unable to create accessToken with id {}", accessToken.getId(), error));
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
        return RxJava2Adapter.completableToMono(Completable.fromMaybe(RxJava2Adapter.monoToMaybe(findByToken_migrated(token).flatMap(z->dbClient.delete().from(JdbcAccessToken.class).matching(from(where("token").is(token))).fetch().rowsUpdated().map(RxJavaReactorMigrationUtil.toJdkFunction((Integer i)->z))).doOnError(error -> LOGGER.error("Unable to delete AccessToken", error)))));
    }

    
@Override
    public Mono<Void> bulkWrite_migrated(List<AccessToken> accessTokens) {
        return Flux.fromIterable(accessTokens).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(accessToken -> RxJava2Adapter.fluxToFlowable(create_migrated(accessToken).flux()))).ignoreElements().then().doOnError(error -> LOGGER.error("Unable to bulk load access tokens", error));
    }

    
@Override
    public Flux<AccessToken> findByClientIdAndSubject_migrated(String clientId, String subject) {
        LOGGER.debug("findByClientIdAndSubject({}, {})", clientId, subject);
        return RxJava2Adapter.observableToFlux(RxJava2Adapter.fluxToFlowable(accessTokenRepository.findByClientIdAndSubject_migrated(clientId, subject, LocalDateTime.now(UTC)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .toObservable()
                .doOnError(error -> LOGGER.error("Unable to retrieve access tokens with client {} and subject {}",
                        clientId, subject, error)), BackpressureStrategy.BUFFER);
    }

    
@Override
    public Flux<AccessToken> findByClientId_migrated(String clientId) {
        LOGGER.debug("findByClientId({})", clientId);
        return RxJava2Adapter.observableToFlux(RxJava2Adapter.fluxToFlowable(accessTokenRepository.findByClientId_migrated(clientId, LocalDateTime.now(UTC)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .toObservable()
                .doOnError(error -> LOGGER.error("Unable to retrieve access tokens with client {}",
                        clientId, error)), BackpressureStrategy.BUFFER);
    }

    
@Override
    public Flux<AccessToken> findByAuthorizationCode_migrated(String authorizationCode) {
        LOGGER.debug("findByAuthorizationCode({})", authorizationCode);
        return RxJava2Adapter.observableToFlux(RxJava2Adapter.fluxToFlowable(accessTokenRepository.findByAuthorizationCode_migrated(authorizationCode, LocalDateTime.now(UTC)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .toObservable()
                .doOnError(error -> LOGGER.error("Unable to retrieve access tokens with authorization code {}",
                        authorizationCode, error)), BackpressureStrategy.BUFFER);
    }

    
@Override
    public Mono<Long> countByClientId_migrated(String clientId) {
        return accessTokenRepository.countByClientId_migrated(clientId, LocalDateTime.now(UTC));
    }

    
@Override
    public Mono<Void> deleteByUserId_migrated(String userId) {
        LOGGER.debug("deleteByUserId({})", userId);
        return dbClient.delete()
                .from(JdbcAccessToken.class)
                .matching(from(where("subject").is(userId)))
                .then().doOnError(error -> LOGGER.error("Unable to delete access tokens with subject {}",
                userId, error));
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
                .then().doOnError(error -> LOGGER.error("Unable to delete access token with domain {}, client {} and subject {}",
                        domainId, clientId, userId, error));
    }

    
@Override
    public Mono<Void> deleteByDomainIdAndUserId_migrated(String domainId, String userId) {
        LOGGER.debug("deleteByDomainIdAndUserId({},{})", domainId, userId);
        return dbClient.delete()
                .from(JdbcAccessToken.class)
                .matching(from(
                        where("subject").is(userId)
                                .and(where("domain").is(domainId))))
                .then().doOnError(error -> LOGGER.error("Unable to delete access tokens with domain {} and subject {}",
                        domainId, userId, error));
    }

    
public Mono<Void> purgeExpiredData_migrated() {
        LOGGER.debug("purgeExpiredData()");
        LocalDateTime now = LocalDateTime.now(UTC);
        return dbClient.delete().from(JdbcAccessToken.class).matching(where("expire_at").lessThan(now)).then().doOnError(error -> LOGGER.error("Unable to purge access tokens", error));
    }
}

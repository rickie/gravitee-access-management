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



import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.oauth2.api.model.JdbcPushedAuthorizationRequest;
import io.gravitee.am.repository.jdbc.oauth2.api.spring.SpringPushedAuthorizationRequestRepository;
import io.gravitee.am.repository.oauth2.api.PushedAuthorizationRequestRepository;
import io.gravitee.am.repository.oauth2.model.PushedAuthorizationRequest;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcPushedAuthorizationRequestRepository extends AbstractJdbcRepository implements PushedAuthorizationRequestRepository {

    @Autowired
    private SpringPushedAuthorizationRequestRepository parRepository;

    protected PushedAuthorizationRequest toEntity(JdbcPushedAuthorizationRequest entity) {
        return mapper.map(entity, PushedAuthorizationRequest.class);
    }

    protected JdbcPushedAuthorizationRequest toJdbcEntity(PushedAuthorizationRequest entity) {
        return mapper.map(entity, JdbcPushedAuthorizationRequest.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<PushedAuthorizationRequest> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<PushedAuthorizationRequest> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.maybeToMono(parRepository.findById(id)).filter(bean -> bean.getExpireAt() == null || bean.getExpireAt().isAfter(now)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(error -> LOGGER.error("Unable to retrieve PushedAuthorizationRequest with id {}", id, error));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(par))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<PushedAuthorizationRequest> create(PushedAuthorizationRequest par) {
 return RxJava2Adapter.monoToSingle(create_migrated(par));
}
@Override
    public Mono<PushedAuthorizationRequest> create_migrated(PushedAuthorizationRequest par) {
        par.setId(par.getId() == null ? RandomString.generate() : par.getId());
        LOGGER.debug("Create PushedAuthorizationRequest with id {}", par.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcPushedAuthorizationRequest.class)
                .using(toJdbcEntity(par))
                .fetch().rowsUpdated();

        return action.flatMap(i->RxJava2Adapter.maybeToMono(parRepository.findById(par.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).single()).doOnError((error) -> LOGGER.error("Unable to create PushedAuthorizationRequest with id {}", par.getId(), error));
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
        return parRepository.deleteById(id).as(RxJava2Adapter::completableToMono).doOnError(error -> LOGGER.error("Unable to delete PushedAuthorizationRequest with id {}", id, error));
    }

    
public Mono<Void> purgeExpiredData_migrated() {
        LOGGER.debug("purgeExpiredData()");
        LocalDateTime now = LocalDateTime.now(UTC);
        return dbClient.delete()
                .from(JdbcPushedAuthorizationRequest.class)
                .matching(where("expire_at")
                        .lessThan(now)).then().doOnError(error -> LOGGER.error("Unable to purge PushedAuthorizationRequest", error));
    }
}

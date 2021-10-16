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
package io.gravitee.am.repository.jdbc.oauth2.oidc;

import static java.time.ZoneOffset.UTC;
import static org.springframework.data.relational.core.query.Criteria.where;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToCompletable;
import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcLoginAttempt;
import io.gravitee.am.repository.jdbc.oauth2.oidc.model.JdbcRequestObject;
import io.gravitee.am.repository.jdbc.oauth2.oidc.spring.SpringRequestObjectRepository;
import io.gravitee.am.repository.oidc.api.RequestObjectRepository;
import io.gravitee.am.repository.oidc.model.RequestObject;
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
public class JdbcRequestObjectRepository extends AbstractJdbcRepository implements RequestObjectRepository {

    @Autowired
    private SpringRequestObjectRepository requestObjectRepository;

    protected RequestObject toEntity(JdbcRequestObject entity) {
        return mapper.map(entity, RequestObject.class);
    }

    protected JdbcRequestObject toJdbcEntity(RequestObject entity) {
        return mapper.map(entity, JdbcRequestObject.class);
    }

    @Deprecated
@Override
    public Maybe<RequestObject> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<RequestObject> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(requestObjectRepository.findById(id)).filter(RxJavaReactorMigrationUtil.toJdkPredicate(bean -> bean.getExpireAt() == null || bean.getExpireAt().isAfter(now))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to retrieve RequestObject with id {}", id, error)))));
    }

    @Deprecated
@Override
    public Single<RequestObject> create(RequestObject requestObject) {
 return RxJava2Adapter.monoToSingle(create_migrated(requestObject));
}
@Override
    public Mono<RequestObject> create_migrated(RequestObject requestObject) {
        requestObject.setId(requestObject.getId() == null ? RandomString.generate() : requestObject.getId());
        LOGGER.debug("Create requestObject with id {}", requestObject.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcRequestObject.class)
                .using(toJdbcEntity(requestObject))
                .fetch().rowsUpdated();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(action.flatMap(i->RxJava2Adapter.maybeToMono(requestObjectRepository.findById(requestObject.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).single()).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((error) -> LOGGER.error("Unable to create requestObject with id {}", requestObject.getId(), error)))));
    }

    @Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("delete({})", id);
        return RxJava2Adapter.completableToMono(requestObjectRepository.deleteById(id).as(RxJava2Adapter::completableToMono).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to delete RequestObject with id {}", id, error))).as(RxJava2Adapter::monoToCompletable));
    }

    @Deprecated
public Completable purgeExpiredData() {
 return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}
public Mono<Void> purgeExpiredData_migrated() {
        LOGGER.debug("purgeExpiredData()");
        LocalDateTime now = LocalDateTime.now(UTC);
        return RxJava2Adapter.completableToMono(dbClient.delete().from(JdbcRequestObject.class).matching(where("expire_at").lessThan(now)).then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(error -> LOGGER.error("Unable to purge RequestObjects", error))).as(RxJava2Adapter::monoToCompletable));
    }
}

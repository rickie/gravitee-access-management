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

import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.resource.ServiceResource;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcServiceResource;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringServiceResourceRepository;
import io.gravitee.am.repository.management.api.ServiceResourceRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
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
public class JdbcServiceResourceRepository extends AbstractJdbcRepository implements ServiceResourceRepository {

    @Autowired
    protected SpringServiceResourceRepository serviceResourceRepository;

    protected ServiceResource toEntity(JdbcServiceResource entity) {
        return mapper.map(entity, ServiceResource.class);
    }

    protected JdbcServiceResource toJdbcEntity(ServiceResource entity) {
        return mapper.map(entity, JdbcServiceResource.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ServiceResource> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<ServiceResource> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(serviceResourceRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ServiceResource> create(ServiceResource item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<ServiceResource> create_migrated(ServiceResource item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("Create Reporter with id {}", item.getId());

        Mono<Integer> insertResult = dbClient.insert()
                .into(JdbcServiceResource.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        return insertResult.flatMap(i->this.findById_migrated(item.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ServiceResource> update(ServiceResource item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<ServiceResource> update_migrated(ServiceResource item) {
        LOGGER.debug("Update resource with id '{}'", item.getId());
        return RxJava2Adapter.singleToMono(serviceResourceRepository.save(toJdbcEntity(item))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
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
        return RxJava2Adapter.completableToMono(serviceResourceRepository.deleteById(id));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByReference_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<ServiceResource> findByReference(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findByReference_migrated(referenceType, referenceId));
}
@Override
    public Flux<ServiceResource> findByReference_migrated(ReferenceType referenceType, String referenceId) {
        LOGGER.debug("findByReference({}, {})", referenceType, referenceId);
        return serviceResourceRepository.findByReference_migrated(referenceType.name(), referenceId).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }
}

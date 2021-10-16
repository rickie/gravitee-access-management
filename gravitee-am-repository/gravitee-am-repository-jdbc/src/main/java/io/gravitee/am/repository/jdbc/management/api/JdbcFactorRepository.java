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
import io.gravitee.am.model.Factor;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcFactor;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringFactorRepository;
import io.gravitee.am.repository.management.api.FactorRepository;
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
public class JdbcFactorRepository extends AbstractJdbcRepository implements FactorRepository {

    @Autowired
    private SpringFactorRepository factorRepository;

    protected Factor toEntity(JdbcFactor entity) {
        return mapper.map(entity, Factor.class);
    }

    protected JdbcFactor toJdbcEntity(Factor entity) {
        return mapper.map(entity, JdbcFactor.class);
    }

    @Deprecated
@Override
    public Flowable<Factor> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Factor> findAll_migrated() {
        LOGGER.debug("findAll()");
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(factorRepository.findAll()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @Deprecated
@Override
    public Flowable<Factor> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Factor> findByDomain_migrated(String domain) {
        LOGGER.debug("findByDomain({})", domain);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(factorRepository.findByDomain(domain)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Factor> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Factor> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(factorRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Factor> create(Factor item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Factor> create_migrated(Factor item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create factor with id {}", item.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcFactor.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(action.flatMap(i->RxJava2Adapter.maybeToMono(this.findById(item.getId())).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Factor> update(Factor item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Factor> update_migrated(Factor item) {
        LOGGER.debug("update factor with id {}", item.getId());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.factorRepository.save(toJdbcEntity(item))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
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
        return RxJava2Adapter.completableToMono(factorRepository.deleteById(id));
    }
}

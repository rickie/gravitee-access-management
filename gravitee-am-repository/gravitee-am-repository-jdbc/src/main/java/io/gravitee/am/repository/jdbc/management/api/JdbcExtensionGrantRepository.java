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
import io.gravitee.am.model.ExtensionGrant;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcExtensionGrant;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringExtensionGrantRepository;
import io.gravitee.am.repository.management.api.ExtensionGrantRepository;
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
public class JdbcExtensionGrantRepository extends AbstractJdbcRepository implements ExtensionGrantRepository {

    @Autowired
    private SpringExtensionGrantRepository extensionGrantRepository;

    protected ExtensionGrant toEntity(JdbcExtensionGrant entity) {
        return mapper.map(entity, ExtensionGrant.class);
    }

    protected JdbcExtensionGrant toJdbcEntity(ExtensionGrant entity) {
        return mapper.map(entity, JdbcExtensionGrant.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<ExtensionGrant> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<ExtensionGrant> findByDomain_migrated(String domain) {
        LOGGER.debug("findByDomain({})", domain);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(extensionGrantRepository.findByDomain_migrated(domain))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndName_migrated(domain, name))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ExtensionGrant> findByDomainAndName(String domain, String name) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndName_migrated(domain, name));
}
@Override
    public Mono<ExtensionGrant> findByDomainAndName_migrated(String domain, String name) {
        LOGGER.debug("findByDomainAndName({}, {})", domain, name);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(extensionGrantRepository.findByDomainAndName_migrated(domain, name))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ExtensionGrant> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<ExtensionGrant> findById_migrated(String id) {
        LOGGER.debug("findByDomainAndName({}, {})", id);
        return RxJava2Adapter.maybeToMono(extensionGrantRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ExtensionGrant> create(ExtensionGrant item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<ExtensionGrant> create_migrated(ExtensionGrant item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create extension grants  with id {}", item.getId());

        Mono<Integer> action = dbClient.insert()
                .into(JdbcExtensionGrant.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        return action.flatMap(i->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.findById_migrated(item.getId()))).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<ExtensionGrant> update(ExtensionGrant item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<ExtensionGrant> update_migrated(ExtensionGrant item) {
        LOGGER.debug("update extension grants  with id {}", item.getId());
        return RxJava2Adapter.singleToMono(this.extensionGrantRepository.save(toJdbcEntity(item))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
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
        return RxJava2Adapter.completableToMono(extensionGrantRepository.deleteById(id));
    }
}

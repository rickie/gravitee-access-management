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

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.CriteriaDefinition.from;
import static reactor.adapter.rxjava.RxJava2Adapter.*;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.uma.Resource;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcResource;
import io.gravitee.am.repository.jdbc.management.api.spring.resources.SpringResourceRepository;
import io.gravitee.am.repository.jdbc.management.api.spring.resources.SpringResourceScopeRepository;
import io.gravitee.am.repository.management.api.ResourceRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcResourceRepository extends AbstractJdbcRepository implements ResourceRepository {

    public static final int MAX_CONCURRENCY = 1;
    @Autowired
    protected SpringResourceRepository resourceRepository;

    @Autowired
    protected SpringResourceScopeRepository resourceScopeRepository;

    protected Resource toEntity(JdbcResource entity) {
        return mapper.map(entity, Resource.class);
    }

    protected JdbcResource toJdbcEntity(Resource entity) {
        return mapper.map(entity, JdbcResource.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Resource>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<Resource>> findByDomain_migrated(String domain, int page, int size) {
        LOGGER.debug("findByDomain({}, {}, {})", domain, page, size);
        CriteriaDefinition whereClause = from(where("domain").is(domain));
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(findResourcePage_migrated(domain, page, size, whereClause)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findResourcePage_migrated(domain, page, size, whereClause))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Page<Resource>> findResourcePage(String domain, int page, int size, CriteriaDefinition whereClause) {
 return RxJava2Adapter.monoToSingle(findResourcePage_migrated(domain, page, size, whereClause));
}
private Mono<Page<Resource>> findResourcePage_migrated(String domain, int page, int size, CriteriaDefinition whereClause) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(dbClient.select()
                .from(JdbcResource.class)
                .matching(whereClause)
                .orderBy(Sort.Order.asc("id"))
                .page(PageRequest.of(page, size))
                .as(JdbcResource.class).all().map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)))
                .flatMap(res -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(completeWithScopes_migrated(RxJava2Adapter.monoToMaybe(Mono.just(res)), res.getId()))).flux()), MAX_CONCURRENCY)).collectList().flatMap(content->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(resourceRepository.countByDomain_migrated(domain))).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long count)->new Page<Resource>(content, page, count))))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.completeWithScopes_migrated(maybeResource, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<Resource> completeWithScopes(Maybe<Resource> maybeResource, String id) {
 return RxJava2Adapter.monoToMaybe(completeWithScopes_migrated(maybeResource, id));
}
private Mono<Resource> completeWithScopes_migrated(Maybe<Resource> maybeResource, String id) {
        Maybe<List<String>> scopes = RxJava2Adapter.monoToMaybe(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(resourceScopeRepository.findAllByResourceId_migrated(id))).map(RxJavaReactorMigrationUtil.toJdkFunction(JdbcResource.Scope::getScope)).collectList());

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(maybeResource).zipWith(RxJava2Adapter.maybeToMono(scopes), RxJavaReactorMigrationUtil.toJdkBiFunction((res, scope) -> {
                    LOGGER.debug("findById({}) fetch {} resource scopes", id, scope == null ? 0 : scope.size());
                    res.setResourceScopes(scope);
                    return res;
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomainAndClient_migrated(domain, client, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Resource>> findByDomainAndClient(String domain, String client, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomainAndClient_migrated(domain, client, page, size));
}
@Override
    public Mono<Page<Resource>> findByDomainAndClient_migrated(String domain, String client, int page, int size) {
        LOGGER.debug("findByDomainAndClient({}, {}, {}, {})", domain, client, page, size);
        CriteriaDefinition whereClause = from(where("domain").is(domain).and(where("client_id").is(client)));
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(findResourcePage_migrated(domain, page, size, whereClause)));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByResources_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Resource> findByResources(List<String> resources) {
 return RxJava2Adapter.fluxToFlowable(findByResources_migrated(resources));
}
@Override
    public Flux<Resource> findByResources_migrated(List<String> resources) {
        LOGGER.debug("findByResources({})", resources);
        if (resources == null || resources.isEmpty()) {
            return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty()));
        }
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(resourceRepository.findByIdIn_migrated(resources))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(resource -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(completeWithScopes_migrated(RxJava2Adapter.monoToMaybe(Mono.just(resource)), resource.getId()))).flux())))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndUser_migrated(domain, client, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Resource> findByDomainAndClientAndUser(String domain, String client, String userId) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndUser_migrated(domain, client, userId));
}
@Override
    public Flux<Resource> findByDomainAndClientAndUser_migrated(String domain, String client, String userId) {
        LOGGER.debug("findByDomainAndClientAndUser({},{},{})", domain, client, userId);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(resourceRepository.findByDomainAndClientAndUser_migrated(domain, client, userId))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(resource -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(completeWithScopes_migrated(RxJava2Adapter.monoToMaybe(Mono.just(resource)), resource.getId()))).flux())))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClientAndResources_migrated(domain, client, resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Resource> findByDomainAndClientAndResources(String domain, String client, List<String> resources) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndClientAndResources_migrated(domain, client, resources));
}
@Override
    public Flux<Resource> findByDomainAndClientAndResources_migrated(String domain, String client, List<String> resources) {
        LOGGER.debug("findByDomainAndClientAndUser({},{},{})", domain, client, resources);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(resourceRepository.findByDomainAndClientAndResources_migrated(domain, client, resources))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(resource -> RxJava2Adapter.fluxToFlowable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(completeWithScopes_migrated(RxJava2Adapter.monoToMaybe(Mono.just(resource)), resource.getId()))).flux())))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Resource> findByDomainAndClientAndUserAndResource(String domain, String client, String userId, String resource) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndUserAndResource_migrated(domain, client, userId, resource));
}
@Override
    public Mono<Resource> findByDomainAndClientAndUserAndResource_migrated(String domain, String client, String userId, String resource) {
        LOGGER.debug("findByDomainAndClientAndUserAndResource({},{},{},{})", domain, client, userId, resource);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(completeWithScopes_migrated(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(resourceRepository.findByDomainAndClientAndUserIdAndResource(domain, client, userId, resource)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))), resource)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Resource> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Resource> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(completeWithScopes_migrated(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(resourceRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))), id)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Resource> create(Resource item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Resource> create_migrated(Resource item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create Resource with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> insertResult = dbClient.insert()
                .into(JdbcResource.class)
                .using(toJdbcEntity(item))
                .fetch().rowsUpdated();

        final List<String> resourceScopes = item.getResourceScopes();
        if (resourceScopes != null && !resourceScopes.isEmpty()) {
            insertResult = insertResult.then(Flux.fromIterable(resourceScopes).concatMap(scope -> {
                JdbcResource.Scope rScope = new JdbcResource.Scope();
                rScope.setScope(scope);
                rScope.setResourceId(item.getId());
                return dbClient.insert().into(JdbcResource.Scope.class).using(rScope).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(insertResult.as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.findById_migrated(item.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Resource> update(Resource item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Resource> update_migrated(Resource item) {
        LOGGER.debug("update Resource with id {}", item.getId());

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> deleteScopes = dbClient.delete().from(JdbcResource.Scope.class)
                .matching(from(where("resource_id").is(item.getId()))).fetch().rowsUpdated();

        Mono<Integer> updateResource = dbClient.update()
                .table(JdbcResource.class)
                .using(toJdbcEntity(item))
                .matching(from(where("id").is(item.getId())))
                .fetch().rowsUpdated();

        final List<String> resourceScopes = item.getResourceScopes();
        if (resourceScopes != null && !resourceScopes.isEmpty()) {
            updateResource = updateResource.then(Flux.fromIterable(resourceScopes).concatMap(scope -> {
                JdbcResource.Scope rScope = new JdbcResource.Scope();
                rScope.setScope(scope);
                rScope.setResourceId(item.getId());
                return dbClient.insert().into(JdbcResource.Scope.class).using(rScope).fetch().rowsUpdated();
            }).reduce(Integer::sum));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(deleteScopes.then(updateResource).as(trx::transactional).flatMap(i->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.findById_migrated(item.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("Delete Resource with id {}", id);

        TransactionalOperator trx = TransactionalOperator.create(tm);
        Mono<Integer> deleteScopes = dbClient.delete().from(JdbcResource.Scope.class)
                .matching(from(where("resource_id").is(id))).fetch().rowsUpdated();

        Mono<Integer> delete = dbClient.delete().from(JdbcResource.class)
                .matching(from(where("id").is(id))).fetch().rowsUpdated();

        return RxJava2Adapter.completableToMono(monoToCompletable(delete.then(deleteScopes).as(trx::transactional)));
    }
}

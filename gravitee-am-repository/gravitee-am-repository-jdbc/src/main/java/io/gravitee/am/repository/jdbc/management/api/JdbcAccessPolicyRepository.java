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



import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.uma.policy.AccessPolicy;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcAccessPolicy;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringAccessPolicyRepository;
import io.gravitee.am.repository.management.api.AccessPolicyRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.relational.core.query.Update;
import org.springframework.data.relational.core.sql.SqlIdentifier;
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
public class JdbcAccessPolicyRepository extends AbstractJdbcRepository implements AccessPolicyRepository {

    @Autowired
    protected SpringAccessPolicyRepository accessPolicyRepository;

    protected AccessPolicy toAccessPolicy(JdbcAccessPolicy entity) {
        return mapper.map(entity, AccessPolicy.class);
    }

    protected JdbcAccessPolicy toJdbcAccessPolicy(AccessPolicy entity) {
        return mapper.map(entity, JdbcAccessPolicy.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<AccessPolicy>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<AccessPolicy>> findByDomain_migrated(String domain, int page, int size) {
        LOGGER.debug("findByDomain(domain:{}, page:{}, size:{})", domain, page, size);
        return dbClient.select()
                .from(JdbcAccessPolicy.class)
                .project("*") // required for mssql to work with to order column name
                .matching(from(where("domain").is(domain)))
                .orderBy(Sort.Order.desc("updated_at"))
                .page(PageRequest.of(page, size))
                .as(JdbcAccessPolicy.class).all().collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(content -> content.stream().map(this::toAccessPolicy).collect(Collectors.toList()))).flatMap(content->accessPolicyRepository.countByDomain_migrated(domain).map(RxJavaReactorMigrationUtil.toJdkFunction((Long count)->new Page<AccessPolicy>(content, page, count))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndResource_migrated(domain, resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<AccessPolicy> findByDomainAndResource(String domain, String resource) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndResource_migrated(domain, resource));
}
@Override
    public Flux<AccessPolicy> findByDomainAndResource_migrated(String domain, String resource) {
        LOGGER.debug("findByDomainAndResource(domain:{}, resources:{})", domain, resource);
        return accessPolicyRepository.findByDomainAndResource_migrated(domain, resource).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toAccessPolicy));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByResources_migrated(resources))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<AccessPolicy> findByResources(List<String> resources) {
 return RxJava2Adapter.fluxToFlowable(findByResources_migrated(resources));
}
@Override
    public Flux<AccessPolicy> findByResources_migrated(List<String> resources) {
        LOGGER.debug("findByResources({})", resources);
        return accessPolicyRepository.findByResourceIn_migrated(resources).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toAccessPolicy));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByResource_migrated(resource))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> countByResource(String resource) {
 return RxJava2Adapter.monoToSingle(countByResource_migrated(resource));
}
@Override
    public Mono<Long> countByResource_migrated(String resource) {
        LOGGER.debug("countByResource({})", resource);
        return accessPolicyRepository.countByResource_migrated(resource);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<AccessPolicy> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<AccessPolicy> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(accessPolicyRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toAccessPolicy));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AccessPolicy> create(AccessPolicy item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<AccessPolicy> create_migrated(AccessPolicy item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create AccessPolicy with id {}", item.getId());

        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("uma_access_policies");
        // doesn't use the class introspection to detect the fields due to keyword column name
        insertSpec = addQuotedField(insertSpec,"id", item.getId(), String.class);
        insertSpec = addQuotedField(insertSpec,"type", item.getType(), String.class);
        insertSpec = addQuotedField(insertSpec,"enabled", item.isEnabled(), Boolean.class);
        insertSpec = addQuotedField(insertSpec,"name", item.getName(), String.class);
        insertSpec = addQuotedField(insertSpec,"description", item.getDescription(), String.class);
        insertSpec = addQuotedField(insertSpec,"order", item.getOrder(), Integer.class); // keyword
        insertSpec = addQuotedField(insertSpec,"condition", item.getCondition(), String.class);
        insertSpec = addQuotedField(insertSpec,"domain", item.getDomain(), String.class);
        insertSpec = addQuotedField(insertSpec,"resource", item.getResource(), String.class);
        insertSpec = addQuotedField(insertSpec,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = insertSpec.fetch().rowsUpdated();

        return action.flatMap(i->this.findById_migrated(item.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AccessPolicy> update(AccessPolicy item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<AccessPolicy> update_migrated(AccessPolicy item) {
        LOGGER.debug("update AccessPolicy with id {}", item.getId());

        final DatabaseClient.GenericUpdateSpec updateSpec = dbClient.update().table("uma_access_policies");
        // doesn't use the class introspection to detect the fields due to keyword column name
        Map<SqlIdentifier, Object> updateFields = new HashMap<>();
        updateFields = addQuotedField(updateFields,"id", item.getId(), String.class);
        updateFields = addQuotedField(updateFields,"type", item.getType() == null ? null : item.getType().name(), String.class);
        updateFields = addQuotedField(updateFields,"enabled", item.isEnabled(), Boolean.class);
        updateFields = addQuotedField(updateFields,"name", item.getName(), String.class);
        updateFields = addQuotedField(updateFields,"description", item.getDescription(), String.class);
        updateFields = addQuotedField(updateFields,"order", item.getOrder(), Integer.class); // keyword
        updateFields = addQuotedField(updateFields,"condition", item.getCondition(), String.class);
        updateFields = addQuotedField(updateFields,"domain", item.getDomain(), String.class);
        updateFields = addQuotedField(updateFields,"resource", item.getResource(), String.class);
        updateFields = addQuotedField(updateFields,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);
        Mono<Integer> action = updateSpec.using(Update.from(updateFields)).matching(from(where("id").is(item.getId()))).fetch().rowsUpdated();

        return action.flatMap(i->this.findById_migrated(item.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        LOGGER.debug("delete AccessPolicy with id {}", id);
        return RxJava2Adapter.completableToMono(accessPolicyRepository.deleteById(id));
    }
}

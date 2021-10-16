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
import static reactor.adapter.rxjava.RxJava2Adapter.monoToSingle;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Email;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcEmail;
import io.gravitee.am.repository.jdbc.management.api.model.mapper.LocalDateConverter;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringEmailRepository;
import io.gravitee.am.repository.management.api.EmailRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
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
public class JdbcEmailRepository extends AbstractJdbcRepository implements EmailRepository {
    @Autowired
    private SpringEmailRepository emailRepository;

    protected final LocalDateConverter dateConverter = new LocalDateConverter();

    protected Email toEntity(JdbcEmail entity) {
        return mapper.map(entity, Email.class);
    }

    protected JdbcEmail toJdbcEntity(Email entity) {
        return mapper.map(entity, JdbcEmail.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Email> findAll_migrated() {
        LOGGER.debug("findAll()");
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(emailRepository.findAll()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<Email> findAll_migrated(ReferenceType referenceType, String referenceId) {
        LOGGER.debug("findAll({},{})", referenceType, referenceId);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(emailRepository.findAllByReference_migrated(referenceId, referenceType.name()))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClient_migrated(referenceType, referenceId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> findByClient(ReferenceType referenceType, String referenceId, String client) {
 return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
@Override
    public Flux<Email> findByClient_migrated(ReferenceType referenceType, String referenceId, String client) {
        LOGGER.debug("findByClient({}, {}, {})", referenceType, referenceId, client);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(emailRepository.findAllByReferenceAndClient_migrated(referenceId, referenceType.name(), client))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(referenceType, referenceId, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByTemplate(ReferenceType referenceType, String referenceId, String template) {
 return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
@Override
    public Mono<Email> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template) {
        LOGGER.debug("findByTemplate({}, {}, {})", referenceType, referenceId, template);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(emailRepository.findByTemplate_migrated(referenceId, referenceType.name(), template))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndTemplate_migrated(domain, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByDomainAndTemplate(String domain, String template) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndTemplate_migrated(domain, template));
}
@Override
    public Mono<Email> findByDomainAndTemplate_migrated(String domain, String template) {
        LOGGER.debug("findByDomainAndTemplate({}, {})", domain, template);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findByTemplate_migrated(ReferenceType.DOMAIN, domain, template)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByClientAndTemplate(ReferenceType referenceType, String referenceId, String client, String template) {
 return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
@Override
    public Mono<Email> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template) {
        LOGGER.debug("findByClientAndTemplate({}, {}, {}, {})", referenceType, referenceId, client, template);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(emailRepository.findByClientAndTemplate_migrated(referenceId, referenceType.name(), client, template))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndTemplate_migrated(domain, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByDomainAndClientAndTemplate(String domain, String client, String template) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndTemplate_migrated(domain, client, template));
}
@Override
    public Mono<Email> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template) {
        LOGGER.debug("findByClientAndTemplate({}, {}, {})", domain, client, template);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(ReferenceType.DOMAIN, domain, client, template)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<Email> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
        LOGGER.debug("findById({}, {}, {})", referenceType, referenceId, id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(emailRepository.findById_migrated(referenceId, referenceType.name(), id))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Email> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(emailRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> create(Email item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Email> create_migrated(Email item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create email with id {}", item.getId());

        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("emails");
        // doesn't use the class introspection to detect the fields due to keyword column name
        insertSpec = addQuotedField(insertSpec,"id", item.getId(), String.class);
        insertSpec = addQuotedField(insertSpec,"enabled", item.isEnabled(), Boolean.class);
        insertSpec = addQuotedField(insertSpec,"client", item.getClient(), String.class);
        insertSpec = addQuotedField(insertSpec,"content", item.getContent(), String.class);
        insertSpec = addQuotedField(insertSpec,"expires_after", item.getExpiresAfter(), int.class);
        insertSpec = addQuotedField(insertSpec,"from", item.getFrom(), String.class);
        insertSpec = addQuotedField(insertSpec,"from_name", item.getFromName(), String.class);
        insertSpec = addQuotedField(insertSpec,"reference_id", item.getReferenceId(), String.class);
        insertSpec = addQuotedField(insertSpec,"reference_type", item.getReferenceType() == null ? null : item.getReferenceType().name(), String.class);
        insertSpec = addQuotedField(insertSpec,"subject", item.getSubject(), String.class);
        insertSpec = addQuotedField(insertSpec,"template", item.getTemplate(), String.class);
        insertSpec = addQuotedField(insertSpec,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = insertSpec.fetch().rowsUpdated();
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(action.flatMap(i->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.findById_migrated(item.getId()))).single())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> update(Email item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Email> update_migrated(Email item) {
        LOGGER.debug("update email with id {}", item.getId());

        final DatabaseClient.GenericUpdateSpec updateSpec = dbClient.update().table("emails");

        // doesn't use the class introspection to detect the fields due to keyword column name
        Map<SqlIdentifier, Object> updateFields = new HashMap<>();
        updateFields = addQuotedField(updateFields,"id", item.getId(), String.class);
        updateFields = addQuotedField(updateFields,"enabled", item.isEnabled(), Boolean.class);
        updateFields = addQuotedField(updateFields,"client", item.getClient(), String.class);
        updateFields = addQuotedField(updateFields,"content", item.getContent(), String.class);
        updateFields = addQuotedField(updateFields,"expires_after", item.getExpiresAfter(), int.class);
        updateFields = addQuotedField(updateFields,"from", item.getFrom(), String.class);
        updateFields = addQuotedField(updateFields,"from_name", item.getFromName(), String.class);
        updateFields = addQuotedField(updateFields,"reference_id", item.getReferenceId(), String.class);
        updateFields = addQuotedField(updateFields,"reference_type", item.getReferenceType() == null ? null : item.getReferenceType().name(), String.class);
        updateFields = addQuotedField(updateFields,"subject", item.getSubject(), String.class);
        updateFields = addQuotedField(updateFields,"template", item.getTemplate(), String.class);
        updateFields = addQuotedField(updateFields,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = updateSpec.using(Update.from(updateFields)).matching(from(where("id").is(item.getId()))).fetch().rowsUpdated();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(action.flatMap(i->RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.findById_migrated(item.getId()))).single())));

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
        return RxJava2Adapter.completableToMono(emailRepository.deleteById(id));
    }
}

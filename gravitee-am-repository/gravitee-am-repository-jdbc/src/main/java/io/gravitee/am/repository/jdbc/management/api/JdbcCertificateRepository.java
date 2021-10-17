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
import io.gravitee.am.model.Certificate;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.jdbc.management.api.model.JdbcCertificate;
import io.gravitee.am.repository.jdbc.management.api.spring.SpringCertificateRepository;
import io.gravitee.am.repository.management.api.CertificateRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.time.LocalDateTime;
import java.util.Base64;
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
public class JdbcCertificateRepository extends AbstractJdbcRepository implements CertificateRepository {

    @Autowired
    private SpringCertificateRepository certificateRepository;

    protected Certificate toEntity(JdbcCertificate entity) {
        Certificate cert = mapper.map(entity, Certificate.class);
        if (cert != null && cert.getMetadata() != null && cert.getMetadata().containsKey("file")) {
            Object file = cert.getMetadata().get("file");
            if (file instanceof String) {
                // file value should be Byte[] but Jackson serialize it in B64
                byte[] data = Base64.getDecoder().decode((String) file);
                cert.getMetadata().put("file", data);
            }
        }
        return cert;
    }

    protected JdbcCertificate toJdbcEntity(Certificate entity) {
        return mapper.map(entity, JdbcCertificate.class);
    }

    
@Override
    public Flux<Certificate> findAll_migrated() {
        LOGGER.debug("findAll()");
        return RxJava2Adapter.flowableToFlux(this.certificateRepository.findAll()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Certificate> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Certificate> findByDomain_migrated(String domain) {
        LOGGER.debug("findByDomain({})", domain);
        return this.certificateRepository.findByDomain_migrated(domain).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Certificate> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Certificate> findById_migrated(String id) {
        LOGGER.debug("findById({})", id);
        return RxJava2Adapter.maybeToMono(this.certificateRepository.findById(id)).map(RxJavaReactorMigrationUtil.toJdkFunction(this::toEntity)).doOnError(error -> LOGGER.error("Unable to retrieve Certificate with id {}", id, error));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Certificate> create(Certificate item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Certificate> create_migrated(Certificate item) {
        item.setId(item.getId() == null ? RandomString.generate() : item.getId());
        LOGGER.debug("create certificate with id {}", item.getId());

        DatabaseClient.GenericInsertSpec<Map<String, Object>> insertSpec = dbClient.insert().into("certificates");

        // doesn't use the class introspection to allow the usage of Json type in PostgreSQL
        insertSpec = addQuotedField(insertSpec,"id", item.getId(), String.class);
        insertSpec = addQuotedField(insertSpec,"type", item.getType(), String.class);
        insertSpec = addQuotedField(insertSpec,"configuration", item.getConfiguration(), String.class);
        insertSpec = addQuotedField(insertSpec,"domain", item.getDomain(), String.class);
        insertSpec = addQuotedField(insertSpec,"name", item.getName(), String.class);
        insertSpec = databaseDialectHelper.addJsonField(insertSpec, "metadata", item.getMetadata());
        insertSpec = addQuotedField(insertSpec,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        insertSpec = addQuotedField(insertSpec,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = insertSpec.fetch().rowsUpdated();

        return action.flatMap(i->this.findById_migrated(item.getId()).single()).doOnError((error) -> LOGGER.error("unable to create certificate with id {}", item.getId(), error));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Certificate> update(Certificate item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Certificate> update_migrated(Certificate item) {
        LOGGER.debug("update Certificate with id {}", item.getId());
        DatabaseClient.GenericUpdateSpec updatedSpec = dbClient.update().table("certificates");

        // doesn't use the class introspection to allow the usage of Json type in PostgreSQL
        Map<SqlIdentifier, Object> updateFields = new HashMap<>();
        updateFields = addQuotedField(updateFields,"id", item.getId(), String.class);
        updateFields = addQuotedField(updateFields,"type", item.getType(), String.class);
        updateFields = addQuotedField(updateFields,"configuration", item.getConfiguration(), String.class);
        updateFields = addQuotedField(updateFields,"domain", item.getDomain(), String.class);
        updateFields = addQuotedField(updateFields,"name", item.getName(), String.class);
        updateFields = databaseDialectHelper.addJsonField(updateFields, "metadata", item.getMetadata());
        updateFields = addQuotedField(updateFields,"created_at", dateConverter.convertTo(item.getCreatedAt(), null), LocalDateTime.class);
        updateFields = addQuotedField(updateFields,"updated_at", dateConverter.convertTo(item.getUpdatedAt(), null), LocalDateTime.class);

        Mono<Integer> action = updatedSpec.using(Update.from(updateFields)).matching(from(where("id").is(item.getId()))).fetch().rowsUpdated();

        return action.flatMap(i->this.findById_migrated(item.getId()).single()).doOnError((error) -> LOGGER.error("unable to update certificate with id {}", item.getId(), error));
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
        return this.certificateRepository.deleteById(id).as(RxJava2Adapter::completableToMono).doOnError(error -> LOGGER.error("Unable to delete Certificate with id {}", id, error));
    }
}

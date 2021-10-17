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
package io.gravitee.am.repository.mongodb.management;

import static com.mongodb.client.model.Filters.eq;

import com.google.errorprone.annotations.InlineMe;
import com.mongodb.reactivestreams.client.MongoCollection;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.repository.management.api.CertificateRepository;
import io.gravitee.am.repository.mongodb.management.internal.model.CertificateMongo;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.bson.Document;
import org.bson.types.Binary;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MongoCertificateRepository extends AbstractManagementMongoRepository implements CertificateRepository {

    private MongoCollection<CertificateMongo> certificatesCollection;

    @PostConstruct
    public void init() {
        certificatesCollection = mongoOperations.getCollection("certificates", CertificateMongo.class);
        super.init(certificatesCollection);
        super.createIndex(certificatesCollection, new Document(FIELD_DOMAIN, 1));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Certificate> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Certificate> findByDomain_migrated(String domain) {
        return Flux.from(certificatesCollection.find(eq(FIELD_DOMAIN, domain))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Certificate> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Certificate> findAll_migrated() {
        return Flux.from(certificatesCollection.find()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(certificateId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Certificate> findById(String certificateId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(certificateId));
}
@Override
    public Mono<Certificate> findById_migrated(String certificateId) {
        return Flux.from(certificatesCollection.find(eq(FIELD_ID, certificateId)).first()).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Certificate> create(Certificate item) {
 return RxJava2Adapter.monoToSingle(create_migrated(item));
}
@Override
    public Mono<Certificate> create_migrated(Certificate item) {
        CertificateMongo certificate = convert(item);
        certificate.setId(certificate.getId() == null ? RandomString.generate() : certificate.getId());
        return Mono.from(certificatesCollection.insertOne(certificate)).flatMap(success->findById_migrated(certificate.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Certificate> update(Certificate item) {
 return RxJava2Adapter.monoToSingle(update_migrated(item));
}
@Override
    public Mono<Certificate> update_migrated(Certificate item) {
        CertificateMongo certificate = convert(item);
        return Mono.from(certificatesCollection.replaceOne(eq(FIELD_ID, certificate.getId()), certificate)).flatMap(updateResult->findById_migrated(certificate.getId()).single());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        return Mono.from(certificatesCollection.deleteOne(eq(FIELD_ID, id))).then();
    }
    private Certificate convert(CertificateMongo certificateMongo) {
        if (certificateMongo == null) {
            return null;
        }

        Certificate certificate = new Certificate();
        certificate.setId(certificateMongo.getId());
        certificate.setName(certificateMongo.getName());
        certificate.setType(certificateMongo.getType());
        certificate.setConfiguration(certificateMongo.getConfiguration());
        certificate.setDomain(certificateMongo.getDomain());
        if (certificateMongo.getMetadata() != null) {
            // convert bson binary type back to byte array
            Map<String, Object> metadata = certificateMongo.getMetadata().entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() instanceof Binary ? ((Binary) e.getValue()).getData() : e.getValue()));
            certificate.setMetadata(metadata);
        }
        certificate.setCreatedAt(certificateMongo.getCreatedAt());
        certificate.setUpdatedAt(certificateMongo.getUpdatedAt());
        return certificate;
    }

    private CertificateMongo convert(Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        CertificateMongo certificateMongo = new CertificateMongo();
        certificateMongo.setId(certificate.getId());
        certificateMongo.setName(certificate.getName());
        certificateMongo.setType(certificate.getType());
        certificateMongo.setConfiguration(certificate.getConfiguration());
        certificateMongo.setDomain(certificate.getDomain());
        certificateMongo.setMetadata(certificate.getMetadata() != null ? new Document(certificate.getMetadata()) : new Document());
        certificateMongo.setCreatedAt(certificate.getCreatedAt());
        certificateMongo.setUpdatedAt(certificate.getUpdatedAt());
        return certificateMongo;
    }
}

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

    @Override
    public Flowable<Certificate> findByDomain(String domain) {
        return RxJava2Adapter.fluxToFlowable(Flux.from(certificatesCollection.find(eq(FIELD_DOMAIN, domain))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Flowable<Certificate> findAll() {
        return RxJava2Adapter.fluxToFlowable(Flux.from(certificatesCollection.find()).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Maybe<Certificate> findById(String certificateId) {
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.observableToFlux(Observable.fromPublisher(certificatesCollection.find(eq(FIELD_ID, certificateId)).first()), BackpressureStrategy.BUFFER).next().map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert)));
    }

    @Override
    public Single<Certificate> create(Certificate item) {
        CertificateMongo certificate = convert(item);
        certificate.setId(certificate.getId() == null ? RandomString.generate() : certificate.getId());
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(certificatesCollection.insertOne(certificate))).flatMap(success->RxJava2Adapter.maybeToMono(findById(certificate.getId())).single()));
    }

    @Override
    public Single<Certificate> update(Certificate item) {
        CertificateMongo certificate = convert(item);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Single.fromPublisher(certificatesCollection.replaceOne(eq(FIELD_ID, certificate.getId()), certificate))).flatMap(updateResult->RxJava2Adapter.maybeToMono(findById(certificate.getId())).single()));
    }

    @Override
    public Completable delete(String id) {
        return RxJava2Adapter.monoToCompletable(Mono.from(certificatesCollection.deleteOne(eq(FIELD_ID, id))));
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

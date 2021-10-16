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
package io.gravitee.am.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.errorprone.annotations.InlineMe;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import io.gravitee.am.certificate.api.CertificateMetadata;
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.event.Action;
import io.gravitee.am.common.event.Type;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.plugins.certificate.core.CertificateSchema;
import io.gravitee.am.repository.management.api.CertificateRepository;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.CertificateNotFoundException;
import io.gravitee.am.service.exception.CertificatePluginSchemaNotFoundException;
import io.gravitee.am.service.exception.CertificateWithApplicationsException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewCertificate;
import io.gravitee.am.service.model.UpdateCertificate;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.CertificateAuditBuilder;
import io.reactivex.*;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
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
public class CertificateServiceImpl implements CertificateService {

    /**
     * Logger.
     */
    private final Logger LOGGER = LoggerFactory.getLogger(CertificateServiceImpl.class);

    @Lazy
    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CertificatePluginService certificatePluginService;

    @Autowired
    private Environment environment;

    public static final String DEFAULT_CERTIFICATE_PLUGIN = "pkcs12-am-certificate";

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Certificate> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Certificate> findById_migrated(String id) {
        LOGGER.debug("Find certificate by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(certificateRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a certificate using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a certificate using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Certificate> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Certificate> findByDomain_migrated(String domain) {
        LOGGER.debug("Find certificates by domain: {}", domain);
        return certificateRepository.findByDomain_migrated(domain).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find certificates by domain", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find certificates by domain", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Certificate> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Certificate> findAll_migrated() {
        LOGGER.debug("Find all certificates");
        return certificateRepository.findAll_migrated().onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find all certificates", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find all certificates by domain", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newCertificate, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Certificate> create(String domain, NewCertificate newCertificate, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newCertificate, principal));
}
@Override
    public Mono<Certificate> create_migrated(String domain, NewCertificate newCertificate, User principal) {
        LOGGER.debug("Create a new certificate {} for domain {}", newCertificate, domain);

        Single<Certificate> certificateSingle = RxJava2Adapter.monoToMaybe(certificatePluginService.getSchema_migrated(newCertificate.getType()).switchIfEmpty(Mono.error(new CertificatePluginSchemaNotFoundException(newCertificate.getType()))).map(RxJavaReactorMigrationUtil.toJdkFunction(schema -> objectMapper.readValue(schema, CertificateSchema.class))))
                .flatMapSingle(new Function<CertificateSchema, SingleSource<Certificate>>() {
                    @Override
                    public SingleSource<Certificate> apply(CertificateSchema certificateSchema) throws Exception {

                        return Single.create(emitter -> {
                            String certificateId = RandomString.generate();
                            Certificate certificate = new Certificate();
                            certificate.setId(certificateId);
                            certificate.setDomain(domain);
                            certificate.setName(newCertificate.getName());
                            certificate.setType(newCertificate.getType());
                            // handle file
                            try {
                                JsonNode certificateConfiguration = objectMapper.readTree(newCertificate.getConfiguration());

                                certificateSchema.getProperties()
                                        .entrySet()
                                        .stream()
                                        .filter(map -> map.getValue().getWidget() != null && "file".equals(map.getValue().getWidget()))
                                        .map(Entry::getKey)
                                        .forEach(key -> {
                                            try {
                                                JsonNode file = objectMapper.readTree(certificateConfiguration.get(key).asText());
                                                byte[] data = Base64.getDecoder().decode(file.get("content").asText());
                                                certificate.setMetadata(Collections.singletonMap(CertificateMetadata.FILE, data));

                                                // update configuration to set the file name
                                                ((ObjectNode) certificateConfiguration).put(key, file.get("name").asText());
                                                newCertificate.setConfiguration(objectMapper.writeValueAsString(certificateConfiguration));
                                            } catch (IOException ex) {
                                                LOGGER.error("An error occurs while trying to create certificate binaries", ex);
                                                emitter.onError(ex);
                                            }
                                        });

                                certificate.setConfiguration(newCertificate.getConfiguration());
                                certificate.setCreatedAt(new Date());
                                certificate.setUpdatedAt(certificate.getCreatedAt());
                            } catch (Exception ex) {
                                LOGGER.error("An error occurs while trying to create certificate configuration", ex);
                                emitter.onError(ex);
                            }
                            emitter.onSuccess(certificate);
                        });
                    }
                });

        return RxJava2Adapter.singleToMono(certificateSingle).flatMap(certificateRepository::create_migrated).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Certificate, SingleSource<Certificate>>toJdkFunction(certificate -> {
                    Event event = new Event(Type.CERTIFICATE, new Payload(certificate.getId(), ReferenceType.DOMAIN, certificate.getDomain(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(certificate)));
                }).apply(v)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(ex -> {
                    LOGGER.error("An error occurs while trying to create a certificate", ex);
                    throw new TechnicalManagementException("An error occurs while trying to create a certificate", ex);
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(certificate -> {
                    // send notification
                    auditService.report(AuditBuilder.builder(CertificateAuditBuilder.class).principal(principal).type(EventType.CERTIFICATE_CREATED).certificate(certificate));
                })).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(CertificateAuditBuilder.class).principal(principal).type(EventType.CERTIFICATE_CREATED).throwable(throwable))));
    }

    private static class CertificateWithSchema {
        private final Certificate certificate;
        private final CertificateSchema schema;

        public CertificateWithSchema(Certificate certificate, CertificateSchema schema) {
            this.certificate = certificate;
            this.schema = schema;
        }

        public Certificate getCertificate() {
            return certificate;
        }

        public CertificateSchema getSchema() {
            return schema;
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateCertificate, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Certificate> update(String domain, String id, UpdateCertificate updateCertificate, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateCertificate, principal));
}
@Override
    public Mono<Certificate> update_migrated(String domain, String id, UpdateCertificate updateCertificate, User principal) {
        LOGGER.debug("Update a certificate {} for domain {}", id, domain);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(certificateRepository.findById_migrated(id).switchIfEmpty(Mono.error(new CertificateNotFoundException(id))))
                .flatMapSingle(new Function<Certificate, SingleSource<CertificateWithSchema>>() {
                    @Override
                    public SingleSource<CertificateWithSchema> apply(Certificate certificate) throws Exception {
                        return RxJava2Adapter.monoToMaybe(certificatePluginService.getSchema_migrated(certificate.getType()).switchIfEmpty(Mono.error(new CertificatePluginSchemaNotFoundException(certificate.getType()))))
                                .flatMapSingle(new Function<String, SingleSource<? extends CertificateWithSchema>>() {
                                    @Override
                                    public SingleSource<? extends CertificateWithSchema> apply(String schema) throws Exception {
                                        return RxJava2Adapter.monoToSingle(Mono.just(new CertificateWithSchema(certificate, objectMapper.readValue(schema, CertificateSchema.class))));
                                    }
                                });
                    }
                })).flatMap(v->RxJava2Adapter.singleToMono((Single<Certificate>)RxJavaReactorMigrationUtil.toJdkFunction((Function<CertificateWithSchema, Single<Certificate>>)oldCertificate -> {
                    Single<Certificate> certificateSingle = Single.create(emitter -> {
                        Certificate certificateToUpdate = new Certificate(oldCertificate.getCertificate());
                        certificateToUpdate.setName(updateCertificate.getName());

                        try {

                            CertificateSchema certificateSchema = oldCertificate.getSchema();
                            JsonNode oldCertificateConfiguration = objectMapper.readTree(oldCertificate.getCertificate().getConfiguration());
                            JsonNode certificateConfiguration = objectMapper.readTree(updateCertificate.getConfiguration());

                            certificateSchema.getProperties()
                                    .entrySet()
                                    .stream()
                                    .filter(map -> map.getValue().getWidget() != null && "file".equals(map.getValue().getWidget()))
                                    .map(Entry::getKey)
                                    .forEach(key -> {
                                        try {
                                            String oldFileInformation = oldCertificateConfiguration.get(key).asText();
                                            String fileInformation = certificateConfiguration.get(key).asText();
                                            // file has changed, let's update it
                                            if (!oldFileInformation.equals(fileInformation)) {
                                                JsonNode file = objectMapper.readTree(certificateConfiguration.get(key).asText());
                                                byte[] data = Base64.getDecoder().decode(file.get("content").asText());
                                                certificateToUpdate.setMetadata(Collections.singletonMap(CertificateMetadata.FILE, data));

                                                // update configuration to set the file path
                                                ((ObjectNode) certificateConfiguration).put(key, file.get("name").asText());
                                                updateCertificate.setConfiguration(objectMapper.writeValueAsString(certificateConfiguration));
                                            }
                                        } catch (IOException ex) {
                                            LOGGER.error("An error occurs while trying to update certificate binaries", ex);
                                            emitter.onError(ex);
                                        }
                                    });


                            certificateToUpdate.setConfiguration(updateCertificate.getConfiguration());
                            certificateToUpdate.setUpdatedAt(new Date());

                        } catch (Exception ex) {
                            LOGGER.error("An error occurs while trying to update certificate configuration", ex);
                            emitter.onError(ex);
                        }
                        emitter.onSuccess(certificateToUpdate);
                    });

                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(certificateSingle).flatMap(t->certificateRepository.update_migrated(t)).flatMap(o->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Certificate, SingleSource<Certificate>>toJdkFunction(certificate1 -> {
                                Event event = new Event(Type.CERTIFICATE, new Payload(certificate1.getId(), ReferenceType.DOMAIN, certificate1.getDomain(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(certificate1)));
                            }).apply(o)))))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Certificate>>toJdkFunction(ex -> {
                                LOGGER.error("An error occurs while trying to update a certificate", ex);
                                throw new TechnicalManagementException("An error occurs while trying to update a certificate", ex);
                            }).apply(err))))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(certificate -> auditService.report(AuditBuilder.builder(CertificateAuditBuilder.class).principal(principal).type(EventType.CERTIFICATE_UPDATED).oldValue(oldCertificate).certificate(certificate)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(CertificateAuditBuilder.class).principal(principal).type(EventType.CERTIFICATE_UPDATED).throwable(throwable)))));
                }).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(certificate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Certificate> update(Certificate certificate) {
 return RxJava2Adapter.monoToSingle(update_migrated(certificate));
}
@Override
    public Mono<Certificate> update_migrated(Certificate certificate) {
        // update date
        certificate.setUpdatedAt(new Date());

        return certificateRepository.update_migrated(certificate).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Certificate, SingleSource<Certificate>>toJdkFunction(certificate1 -> {
                    // Reload domain to take care about certificate update
                    Event event = new Event(Type.CERTIFICATE, new Payload(certificate1.getId(), ReferenceType.DOMAIN, certificate1.getDomain(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(certificate1)));
                }).apply(v)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(ex -> {
                    LOGGER.error("An error occurs while trying to update a certificate", ex);
                    throw new TechnicalManagementException("An error occurs while trying to update a certificate", ex);
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(certificateId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String certificateId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(certificateId, principal));
}
@Override
    public Mono<Void> delete_migrated(String certificateId, User principal) {
        LOGGER.debug("Delete certificate {}", certificateId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(certificateRepository.findById_migrated(certificateId).switchIfEmpty(Mono.error(new CertificateNotFoundException(certificateId))).flatMap(y->RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(applicationService.findByCertificate_migrated(certificateId)).count()).flatMap((java.lang.Long v)->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Long applications)->{
if (applications > 0) {
throw new CertificateWithApplicationsException();
}
return RxJava2Adapter.monoToSingle(Mono.just(y));
}).apply(v)))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Certificate, CompletableSource>)certificate -> {
                    // create event for sync process
                    Event event = new Event(Type.CERTIFICATE, new Payload(certificate.getId(), ReferenceType.DOMAIN, certificate.getDomain(), Action.DELETE));
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(certificateRepository.delete_migrated(certificateId).then(eventService.create_migrated(event)))
                            .toCompletable()
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(CertificateAuditBuilder.class).principal(principal).type(EventType.CERTIFICATE_DELETED).certificate(certificate)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(CertificateAuditBuilder.class).principal(principal).type(EventType.CERTIFICATE_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to delete certificate: {}", certificateId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete certificate: %s", certificateId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Certificate> create(String domain) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain));
}
@Override
    public Mono<Certificate> create_migrated(String domain) {
        // Define the default certificate
        // Create a default PKCS12 certificate: io.gravitee.am.certificate.pkcs12.PKCS12Configuration
        NewCertificate certificate = new NewCertificate();
        certificate.setName("Default");

        // TODO: how-to handle default certificate type ?
        certificate.setType(DEFAULT_CERTIFICATE_PLUGIN);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(certificatePluginService.getSchema_migrated(certificate.getType()).map(RxJavaReactorMigrationUtil.toJdkFunction(new Function<String, CertificateSchema>() {
                    @Override
                    public CertificateSchema apply(String schema) throws Exception {
                        return objectMapper.readValue(schema, CertificateSchema.class);
                    }
                })).map(RxJavaReactorMigrationUtil.toJdkFunction(new Function<CertificateSchema, String>() {
                    @Override
                    public String apply(CertificateSchema certificateSchema) throws Exception {
                        final int keySize = environment.getProperty("domains.certificates.default.keysize", int.class, 2048);
                        final int validity = environment.getProperty("domains.certificates.default.validity", int.class, 365);
                        final String name = environment.getProperty("domains.certificates.default.name", String.class, "cn=Gravitee.io");
                        final String sigAlgName = environment.getProperty("domains.certificates.default.algorithm", String.class, "SHA256withRSA");
                        final String alias = environment.getProperty("domains.certificates.default.alias", String.class, "default");
                        final String keyPass = environment.getProperty("domains.certificates.default.keypass", String.class, "gravitee");
                        final String storePass = environment.getProperty("domains.certificates.default.storepass", String.class, "gravitee");

                        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                        keyPairGenerator.initialize(keySize);
                        KeyPair keyPair = keyPairGenerator.generateKeyPair();

                        java.security.cert.Certificate[] chain = {
                                generateCertificate(name, keyPair, validity, sigAlgName)
                        };

                        KeyStore ks = KeyStore.getInstance("pkcs12");
                        ks.load(null, null);
                        ks.setKeyEntry(alias, keyPair.getPrivate(), keyPass.toCharArray(), chain);

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        ks.store(outputStream, storePass.toCharArray());

                        ObjectNode certificateNode = objectMapper.createObjectNode();

                        ObjectNode contentNode = objectMapper.createObjectNode();
                        contentNode.put("content", new String(Base64.getEncoder().encode(outputStream.toByteArray())));
                        contentNode.put("name", domain + ".p12");
                        certificateNode.put("content", objectMapper.writeValueAsString(contentNode));
                        certificateNode.put("alias", alias);
                        certificateNode.put("storepass", storePass);
                        certificateNode.put("keypass", keyPass);

                        return objectMapper.writeValueAsString(certificateNode);
                    }
                }))).flatMapSingle(new Function<String, SingleSource<Certificate>>() {
                    @Override
                    public SingleSource<Certificate> apply(String configuration) throws Exception {
                        certificate.setConfiguration(configuration);
                        return RxJava2Adapter.monoToSingle(create_migrated(domain, certificate));
                    }
                }));
    }

    private X509Certificate generateCertificate(String dn, KeyPair keyPair, int validity, String sigAlgName) throws GeneralSecurityException, IOException, OperatorCreationException {
        // Use appropriate signature algorithm based on your keyPair algorithm.
        String signatureAlgorithm = sigAlgName;

        X500Name dnName = new X500Name(dn);
        Date from = new Date();
        Date to = new Date(from.getTime() + validity * 1000L * 24L * 60L * 60L);

        // Using the current timestamp as the certificate serial number
        BigInteger certSerialNumber = new BigInteger(Long.toString(from.getTime()));

        ContentSigner contentSigner = new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate());
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                dnName, certSerialNumber, from, to, dnName, keyPair.getPublic());

        // true for CA, false for EndEntity
        BasicConstraints basicConstraints = new BasicConstraints(true);

        // Basic Constraints is usually marked as critical.
        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints);

        return new JcaX509CertificateConverter().setProvider(BouncyCastleProviderSingleton.getInstance()).getCertificate(certBuilder.build(contentSigner));
    }
}

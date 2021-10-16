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
package io.gravitee.am.gateway.handler.common.certificate.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.certificate.api.CertificateMetadata;
import io.gravitee.am.certificate.api.DefaultKey;
import io.gravitee.am.certificate.api.Keys;
import io.gravitee.am.common.event.CertificateEvent;
import io.gravitee.am.common.event.EventManager;
import io.gravitee.am.common.jwt.SignatureAlgorithm;
import io.gravitee.am.gateway.certificate.CertificateProvider;
import io.gravitee.am.gateway.certificate.CertificateProviderManager;
import io.gravitee.am.gateway.handler.common.certificate.CertificateManager;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.model.jose.JWK;
import io.gravitee.am.repository.management.api.CertificateRepository;
import io.gravitee.common.event.Event;
import io.gravitee.common.event.EventListener;
import io.gravitee.common.service.AbstractService;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.security.InvalidKeyException;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CertificateManagerImpl extends AbstractService implements CertificateManager, EventListener<CertificateEvent, Payload>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(CertificateManagerImpl.class);

    @Value("${jwt.secret:s3cR3t4grAv1t3310AMS1g1ingDftK3y}")
    private String signingKeySecret;

    @Value("${jwt.kid:default-gravitee-AM-key}")
    private String signingKeyId;

    @Autowired
    private Domain domain;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private EventManager eventManager;

    @Autowired
    private CertificateProviderManager certificateProviderManager;

    private CertificateProvider defaultCertificateProvider;

    private CertificateProvider noneAlgorithmCertificateProvider;

    private final ConcurrentMap<String, Certificate> certificates = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("Initializing default certificate provider for domain {}", domain.getName());
        initDefaultCertificateProvider();
        logger.info("Default certificate loaded for domain {}", domain.getName());

        logger.info("Initializing none algorithm certificate provider for domain {}", domain.getName());
        initNoneAlgorithmCertificateProvider();
        logger.info("None algorithm certificate loaded for domain {}", domain.getName());

        logger.info("Initializing certificates for domain {}", domain.getName());
        RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(certificateRepository.findByDomain_migrated(domain.getId()))
                .subscribeOn(Schedulers.io())).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(certificate -> {
                            certificateProviderManager.create(certificate);
                            certificates.put(certificate.getId(), certificate);
                            logger.info("Certificate {} loaded for domain {}", certificate.getName(), domain.getName());
                        }), RxJavaReactorMigrationUtil.toJdkConsumer(error -> logger.error("An error has occurred when loading certificates for domain {}", domain.getName(), error)));
    }

    @Override
    public void onEvent(Event<CertificateEvent, Payload> event) {
        if (event.content().getReferenceType() == ReferenceType.DOMAIN &&
                domain.getId().equals(event.content().getReferenceId())) {
            switch (event.type()) {
                case DEPLOY:
                case UPDATE:
                    deployCertificate(event.content().getId());
                    break;
                case UNDEPLOY:
                    removeCertificate(event.content().getId());
                    break;
            }
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        logger.info("Register event listener for certificate events for domain {}", domain.getName());
        eventManager.subscribeForEvents(this, CertificateEvent.class, domain.getId());
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        logger.info("Dispose event listener for certificate events for domain {}", domain.getName());
        eventManager.unsubscribeForEvents(this, CertificateEvent.class, domain.getId());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.get_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<CertificateProvider> get(String id) {
 return RxJava2Adapter.monoToMaybe(get_migrated(id));
}
@Override
    public Mono<CertificateProvider> get_migrated(String id) {
        if (id == null) {
            return Mono.empty();
        }
        CertificateProvider certificateProvider = certificateProviderManager.get(id);
        return RxJava2Adapter.maybeToMono(certificateProvider != null ? RxJava2Adapter.monoToMaybe(Mono.just(certificateProvider)) : RxJava2Adapter.monoToMaybe(Mono.empty()));
    }

    @Override
    public io.gravitee.am.certificate.api.CertificateProvider getCertificate(String id) {
        CertificateProvider certificateProvider = certificateProviderManager.get(id);
        if (certificateProvider != null && domain.getId().equals(certificateProvider.getDomain())) {
            return certificateProvider.getProvider();
        }
        return null;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByAlgorithm_migrated(algorithm))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<CertificateProvider> findByAlgorithm(String algorithm) {
 return RxJava2Adapter.monoToMaybe(findByAlgorithm_migrated(algorithm));
}
@Override
    public Mono<CertificateProvider> findByAlgorithm_migrated(String algorithm) {

        if(algorithm==null || algorithm.trim().isEmpty()) {
            return Mono.empty();
        }

        Optional<CertificateProvider> certificate = this
                .providers()
                .stream()
                .filter(certificateProvider ->
                        certificateProvider!=null && certificateProvider.getProvider()!=null &&
                        algorithm.equals(certificateProvider.getProvider().signatureAlgorithm())
                )
                .findFirst();

        return RxJava2Adapter.maybeToMono(certificate.isPresent() ? RxJava2Adapter.monoToMaybe(Mono.just(certificate.get())) : RxJava2Adapter.monoToMaybe(Mono.empty()));
    }

    @Override
    public Collection<CertificateProvider> providers() {
        return certificateProviderManager
                .certificateProviders()
                .stream()
                .filter(c -> domain.getId().equals(c.getDomain()))
                .collect(Collectors.toList());
    }

    @Override
    public CertificateProvider defaultCertificateProvider() {
        return defaultCertificateProvider;
    }

    @Override
    public CertificateProvider noneAlgorithmCertificateProvider() {
        return noneAlgorithmCertificateProvider;
    }

    private void deployCertificate(String certificateId) {
        logger.info("Deploying certificate {} for domain {}", certificateId, domain.getName());
        RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(certificateRepository.findById_migrated(certificateId))
                .subscribeOn(Schedulers.io())).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(certificate -> {
                            try {
                                certificateProviderManager.create(certificate);
                                certificates.put(certificateId, certificate);
                                logger.info("Certificate {} loaded for domain {}", certificateId, domain.getName());
                            } catch (Exception ex) {
                                logger.error("Unable to load certificate {} for domain {}", certificate.getName(), certificate.getDomain(), ex);
                                certificates.remove(certificateId, certificate);
                            }
                        }), RxJavaReactorMigrationUtil.toJdkConsumer(error -> logger.error("An error has occurred when loading certificate {} for domain {}", certificateId, domain.getName(), error)), RxJavaReactorMigrationUtil.toRunnable(() -> logger.error("No certificate found with id {}", certificateId)));
    }

    private void removeCertificate(String certificateId) {
        logger.info("Removing certificate {} for domain {}", certificateId, domain.getName());
        Certificate deletedCertificate = certificates.remove(certificateId);
        certificateProviderManager.delete(certificateId);
        if (deletedCertificate != null) {
            logger.info("Certificate {} has been removed for domain {}", certificateId, domain.getName());
        } else {
            logger.info("Certificate {} was not loaded for domain {}", certificateId, domain.getName());
        }
    }

    private void initDefaultCertificateProvider() throws InvalidKeyException {
        // create default signing HMAC key
        byte[] keySecretBytes = signingKeySecret.getBytes();
        Key key = Keys.hmacShaKeyFor(keySecretBytes);
        SignatureAlgorithm signatureAlgorithm = Keys.hmacShaSignatureAlgorithmFor(keySecretBytes);
        io.gravitee.am.certificate.api.Key certificateKey = new DefaultKey(signingKeyId, key);

        // create default certificate provider
        CertificateMetadata certificateMetadata = new CertificateMetadata();
        certificateMetadata.setMetadata(Collections.singletonMap(CertificateMetadata.DIGEST_ALGORITHM_NAME, signatureAlgorithm.getDigestName()));

        io.gravitee.am.certificate.api.CertificateProvider defaultProvider = new io.gravitee.am.certificate.api.CertificateProvider() {

            @InlineMe(replacement = "RxJava2Adapter.monoToSingle(key_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
            public Single<io.gravitee.am.certificate.api.Key> key() {
 return RxJava2Adapter.monoToSingle(key_migrated());
}
@Override
            public Mono<io.gravitee.am.certificate.api.Key> key_migrated() {
                return Mono.just(certificateKey);
            }

            @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(privateKey_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
            public Flowable<JWK> privateKey() {
 return RxJava2Adapter.fluxToFlowable(privateKey_migrated());
}
@Override
            public Flux<JWK> privateKey_migrated() {
                return null;
            }

            @InlineMe(replacement = "RxJava2Adapter.monoToSingle(publicKey_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
            public Single<String> publicKey() {
 return RxJava2Adapter.monoToSingle(publicKey_migrated());
}
@Override
            public Mono<String> publicKey_migrated() {
                return null;
            }

            @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(keys_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
            public Flowable<JWK> keys() {
 return RxJava2Adapter.fluxToFlowable(keys_migrated());
}
@Override
            public Flux<JWK> keys_migrated() {
                return null;
            }

            @Override
            public String signatureAlgorithm() {
                return signatureAlgorithm.getValue();
            }

            @Override
            public CertificateMetadata certificateMetadata() {
                return certificateMetadata;
            }
        };
        this.defaultCertificateProvider = certificateProviderManager.create(defaultProvider);
    }

    private void initNoneAlgorithmCertificateProvider() {
        CertificateMetadata certificateMetadata = new CertificateMetadata();
        certificateMetadata.setMetadata(Collections.singletonMap(CertificateMetadata.DIGEST_ALGORITHM_NAME, SignatureAlgorithm.NONE.getValue()));

        io.gravitee.am.certificate.api.CertificateProvider noneProvider = new io.gravitee.am.certificate.api.CertificateProvider() {

            @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(privateKey_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
            public Flowable<JWK> privateKey() {
 return RxJava2Adapter.fluxToFlowable(privateKey_migrated());
}
@Override
            public Flux<JWK> privateKey_migrated() {
                throw new UnsupportedOperationException("No private key for \"none\" algorithm");
            }

            @InlineMe(replacement = "RxJava2Adapter.monoToSingle(key_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
            public Single<io.gravitee.am.certificate.api.Key> key() {
 return RxJava2Adapter.monoToSingle(key_migrated());
}
@Override
            public Mono<io.gravitee.am.certificate.api.Key> key_migrated() {
                throw new UnsupportedOperationException("No key for \"none\" algorithm");
            }

            @InlineMe(replacement = "RxJava2Adapter.monoToSingle(publicKey_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
            public Single<String> publicKey() {
 return RxJava2Adapter.monoToSingle(publicKey_migrated());
}
@Override
            public Mono<String> publicKey_migrated() {
                throw new UnsupportedOperationException("No public key for \"none\" algorithm");
            }

            @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(keys_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
            public Flowable<JWK> keys() {
 return RxJava2Adapter.fluxToFlowable(keys_migrated());
}
@Override
            public Flux<JWK> keys_migrated() {
                throw new UnsupportedOperationException("No keys for \"none\" algorithm");
            }

            @Override
            public String signatureAlgorithm() {
                return SignatureAlgorithm.NONE.getValue();
            }

            @Override
            public CertificateMetadata certificateMetadata() {
                return certificateMetadata;
            }
        };
        this.noneAlgorithmCertificateProvider = certificateProviderManager.create(noneProvider);
    }
}

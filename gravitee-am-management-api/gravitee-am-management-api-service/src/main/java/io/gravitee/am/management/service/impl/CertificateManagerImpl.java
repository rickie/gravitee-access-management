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
package io.gravitee.am.management.service.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.certificate.api.CertificateProvider;
import io.gravitee.am.common.event.CertificateEvent;
import io.gravitee.am.management.service.CertificateManager;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.plugins.certificate.core.CertificatePluginManager;
import io.gravitee.am.service.CertificateService;
import io.gravitee.common.event.Event;
import io.gravitee.common.event.EventListener;
import io.gravitee.common.event.EventManager;
import io.gravitee.common.service.AbstractService;
import io.reactivex.Maybe;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class CertificateManagerImpl extends AbstractService<CertificateManager> implements CertificateManager, EventListener<CertificateEvent, Payload> {

    private static final Logger logger = LoggerFactory.getLogger(CertificateManagerImpl.class);
    private static final long retryTimeout = 10000;
    private ConcurrentMap<String, CertificateProvider> certificateProviders = new ConcurrentHashMap<>();

    @Autowired
    private CertificatePluginManager certificatePluginManager;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private EventManager eventManager;

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        logger.info("Register event listener for certificate events for the management API");
        eventManager.subscribeForEvents(this, CertificateEvent.class);

        logger.info("Initializing certificate providers");
        certificateService.findAll().blockingIterable().forEach(certificate -> {
            logger.info("\tInitializing certificate: {} [{}]", certificate.getName(), certificate.getType());
            loadCertificate(certificate);
        });
    }

    @Override
    public void onEvent(Event<CertificateEvent, Payload> event) {
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

    @Deprecated
@Override
    public Maybe<CertificateProvider> getCertificateProvider(String certificateId) {
 return RxJava2Adapter.monoToMaybe(getCertificateProvider_migrated(certificateId));
}
@Override
    public Mono<CertificateProvider> getCertificateProvider_migrated(String certificateId) {
        return RxJava2Adapter.maybeToMono(doGetCertificateProvider(certificateId, System.currentTimeMillis()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.doGetCertificateProvider_migrated(certificateId, startTime))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Maybe<CertificateProvider> doGetCertificateProvider(String certificateId, long startTime) {
 return RxJava2Adapter.monoToMaybe(doGetCertificateProvider_migrated(certificateId, startTime));
}
private Mono<CertificateProvider> doGetCertificateProvider_migrated(String certificateId, long startTime) {
        CertificateProvider certificateProvider = certificateProviders.get(certificateId);

        if (certificateProvider != null) {
            return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(certificateProvider)));
        }

        // certificate can be missing as it can take sometime for the reporter events
        // to propagate across the cluster so if the next call comes
        // in quickly at a different node there is a possibility it isn't available yet.
        try {
            Certificate certificate = RxJava2Adapter.maybeToMono(certificateService.findById(certificateId)).block();
            if (certificate == null) {
                return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty()));
            }
            // retry
            while (certificateProvider == null && System.currentTimeMillis() - startTime < retryTimeout) {
                certificateProvider = certificateProviders.get(certificateId);
            }
            return RxJava2Adapter.maybeToMono(certificateProvider == null ? RxJava2Adapter.monoToMaybe(Mono.empty()) : RxJava2Adapter.monoToMaybe(Mono.just(certificateProvider)));
        } catch (Exception ex) {
            logger.error("An error has occurred while fetching certificate with id {}", certificateId, ex);
            throw new IllegalStateException(ex);
        }
    }

    private void deployCertificate(String certificateId) {
        logger.info("Management API has received a deploy certificate event for {}", certificateId);
        certificateService.findById(certificateId)
                .subscribe(
                        this::loadCertificate,
                        error -> logger.error("Unable to deploy certificate {}", certificateId, error),
                        () -> logger.error("No certificate found with id {}", certificateId));
    }

    private void removeCertificate(String certificateId) {
        logger.info("Management API has received a undeploy certificate event for {}", certificateId);
        certificateProviders.remove(certificateId);
    }

    private void loadCertificate(Certificate certificate) {
        try {
            CertificateProvider certificateProvider =
                    certificatePluginManager.create(certificate.getType(), certificate.getConfiguration(), certificate.getMetadata());
            if (certificateProvider != null) {
                certificateProviders.put(certificate.getId(), certificateProvider);
            } else {
                certificateProviders.remove(certificate.getId());
            }
        } catch (Exception ex) {
            logger.error("An error has occurred while loading certificate: {} [{}]", certificate.getName(), certificate.getType(), ex);
            certificateProviders.remove(certificate.getId());
        }
    }
}

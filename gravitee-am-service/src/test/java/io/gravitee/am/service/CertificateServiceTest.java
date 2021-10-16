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
package io.gravitee.am.service;

import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.plugins.certificate.core.CertificateSchema;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.CertificateRepository;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.impl.CertificateServiceImpl;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class CertificateServiceTest {

    @InjectMocks
    private CertificateService certificateService = new CertificateServiceImpl();

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private EventService eventService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuditService auditService;

    @Mock
    private CertificatePluginService certificatePluginService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(certificateRepository.findById_migrated("my-certificate")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(new Certificate()))));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(certificateService.findById_migrated("my-certificate")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingCertificate() {
        when(certificateRepository.findById_migrated("my-certificate")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(certificateService.findById_migrated("my-certificate")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(certificateRepository.findById_migrated("my-certificate")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)))));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(certificateService.findById_migrated("my-certificate")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomain() {
        when(certificateRepository.findByDomain_migrated(DOMAIN)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(new Certificate()))));
        TestSubscriber<Certificate> testSubscriber = RxJava2Adapter.fluxToFlowable(certificateService.findByDomain_migrated(DOMAIN)).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(certificateRepository.findByDomain_migrated(DOMAIN)).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)))));

        TestSubscriber testObserver = RxJava2Adapter.fluxToFlowable(certificateService.findByDomain_migrated(DOMAIN)).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete() {
        // prepare certificate
        Certificate certificate = Mockito.mock(Certificate.class);
        when(certificate.getDomain()).thenReturn(DOMAIN);

        when(certificateRepository.findById_migrated("my-certificate")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(certificate))));
        when(applicationService.findByCertificate_migrated("my-certificate")).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.empty())));
        when(certificateRepository.delete_migrated("my-certificate")).thenReturn(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty())));
        when(eventService.create_migrated(any())).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Event()))));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(certificateService.delete_migrated("my-certificate")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(certificateRepository, times(1)).findById_migrated("my-certificate");
        verify(applicationService, times(1)).findByCertificate_migrated("my-certificate");
        verify(certificateRepository, times(1)).delete_migrated("my-certificate");
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldDelete_technicalException() {
        when(certificateRepository.findById_migrated("my-certificate")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)))));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(certificateService.delete_migrated("my-certificate")).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(certificateRepository, times(1)).findById_migrated("my-certificate");
        verify(applicationService, never()).findByCertificate_migrated("my-certificate");
        verify(certificateRepository, never()).delete_migrated("my-certificate");
    }

    @Test
    public void shouldDelete_notFound() {
        when(certificateRepository.findById_migrated("my-certificate")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(certificateService.delete_migrated("my-certificate")).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(certificateRepository, times(1)).findById_migrated("my-certificate");
        verify(applicationService, never()).findByCertificate_migrated("my-certificate");
        verify(certificateRepository, never()).delete_migrated("my-certificate");
    }

    @Test
    public void shouldDelete_certificateWithClients() {
        when(certificateRepository.findById_migrated("my-certificate")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(new Certificate()))));
        when(applicationService.findByCertificate_migrated("my-certificate")).thenReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(new Application()))));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(certificateService.delete_migrated("my-certificate")).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(certificateRepository, times(1)).findById_migrated("my-certificate");
        verify(applicationService, times(1)).findByCertificate_migrated("my-certificate");
        verify(certificateRepository, never()).delete_migrated("my-certificate");
    }

    @Test
    @Ignore
    public void shouldCreate_defaultCertificate() throws Exception {
        when(objectMapper.readValue(anyString(), eq(CertificateSchema.class))).thenReturn(new CertificateSchema());
        when(objectMapper.createObjectNode()).thenReturn(mock(ObjectNode.class));
        when(certificatePluginService.getSchema_migrated(CertificateServiceImpl.DEFAULT_CERTIFICATE_PLUGIN))
                .thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just("{\n" +
                        "  \"type\" : \"object\",\n" +
                        "  \"id\" : \"urn:jsonschema:io:gravitee:am:certificate:pkcs12:PKCS12Configuration\",\n" +
                        "  \"properties\" : {\n" +
                        "    \"content\" : {\n" +
                        "      \"title\": \"PKCS#12 file\",\n" +
                        "      \"description\": \"PKCS file\",\n" +
                        "      \"type\" : \"string\",\n" +
                        "      \"widget\" : \"file\"\n" +
                        "    },\n" +
                        "    \"storepass\" : {\n" +
                        "      \"title\": \"Keystore password\",\n" +
                        "      \"description\": \"The password which is used to protect the integrity of the keystore.\",\n" +
                        "      \"type\" : \"string\"\n" +
                        "    },\n" +
                        "    \"alias\" : {\n" +
                        "      \"title\": \"Key alias\",\n" +
                        "      \"description\": \"Alias which identify the keystore entry.\",\n" +
                        "      \"type\" : \"string\"\n" +
                        "    },\n" +
                        "    \"keypass\" : {\n" +
                        "      \"title\": \"Key password\",\n" +
                        "      \"description\": \"The password used to protect the private key of the generated key pair.\",\n" +
                        "      \"type\" : \"string\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"required\": [\n" +
                        "    \"content\",\n" +
                        "    \"storepass\",\n" +
                        "    \"alias\",\n" +
                        "    \"keypass\"\n" +
                        "  ]\n" +
                        "}"))));
        TestObserver testObserver = RxJava2Adapter.monoToSingle(certificateService.create_migrated("my-domain")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
    }
}

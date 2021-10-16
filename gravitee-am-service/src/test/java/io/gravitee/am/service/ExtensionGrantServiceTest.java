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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.Application;
import io.gravitee.am.model.ExtensionGrant;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.ExtensionGrantRepository;
import io.gravitee.am.service.exception.ExtensionGrantAlreadyExistsException;
import io.gravitee.am.service.exception.ExtensionGrantNotFoundException;
import io.gravitee.am.service.exception.ExtensionGrantWithApplicationsException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.impl.ExtensionGrantServiceImpl;
import io.gravitee.am.service.model.NewExtensionGrant;
import io.gravitee.am.service.model.UpdateExtensionGrant;




import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.Collections;
import java.util.Date;
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
public class ExtensionGrantServiceTest {

    @InjectMocks
    private ExtensionGrantService extensionGrantService = new ExtensionGrantServiceImpl();

    @Mock
    private EventService eventService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ExtensionGrantRepository extensionGrantRepository;

    @Mock
    private AuditService auditService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(extensionGrantRepository.findById_migrated("my-extension-grant")).thenReturn(Mono.just(new ExtensionGrant()));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(extensionGrantService.findById_migrated("my-extension-grant")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingExtensionGrant() {
        when(extensionGrantRepository.findById_migrated("my-extension-grant")).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(extensionGrantService.findById_migrated("my-extension-grant")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(extensionGrantRepository.findById_migrated("my-extension-grant")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(extensionGrantService.findById_migrated("my-extension-grant")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomain() {
        when(extensionGrantRepository.findByDomain_migrated(DOMAIN)).thenReturn(Flux.just(new ExtensionGrant()));
        TestSubscriber<ExtensionGrant> testSubscriber = RxJava2Adapter.fluxToFlowable(extensionGrantService.findByDomain_migrated(DOMAIN)).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(extensionGrantRepository.findByDomain_migrated(DOMAIN)).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestSubscriber testSubscriber = RxJava2Adapter.fluxToFlowable(extensionGrantService.findByDomain_migrated(DOMAIN)).test();

        testSubscriber.assertError(TechnicalManagementException.class);
        testSubscriber.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        NewExtensionGrant newExtensionGrant = Mockito.mock(NewExtensionGrant.class);
        when(newExtensionGrant.getName()).thenReturn("my-extension-grant");
        when(extensionGrantRepository.findByDomainAndName_migrated(DOMAIN, "my-extension-grant")).thenReturn(Mono.empty());
        when(extensionGrantRepository.create_migrated(any(ExtensionGrant.class))).thenReturn(Mono.just(new ExtensionGrant()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(extensionGrantService.create_migrated(DOMAIN, newExtensionGrant)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(extensionGrantRepository, times(1)).findByDomainAndName_migrated(anyString(), anyString());
        verify(extensionGrantRepository, times(1)).create_migrated(any(ExtensionGrant.class));
    }

    @Test
    public void shouldCreate_technicalException() {
        NewExtensionGrant newExtensionGrant = Mockito.mock(NewExtensionGrant.class);
        when(newExtensionGrant.getName()).thenReturn("my-extension-grant");
        when(extensionGrantRepository.findByDomainAndName_migrated(DOMAIN, "my-extension-grant")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver<ExtensionGrant> testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(extensionGrantService.create_migrated(DOMAIN, newExtensionGrant)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(extensionGrantRepository, never()).create_migrated(any(ExtensionGrant.class));
    }

    @Test
    public void shouldCreate2_technicalException() {
        NewExtensionGrant newExtensionGrant = Mockito.mock(NewExtensionGrant.class);
        when(newExtensionGrant.getName()).thenReturn("my-extension-grant");
        when(extensionGrantRepository.findByDomainAndName_migrated(DOMAIN, "my-extension-grant")).thenReturn(Mono.empty());
        when(extensionGrantRepository.create_migrated(any(ExtensionGrant.class))).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver<ExtensionGrant> testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(extensionGrantService.create_migrated(DOMAIN, newExtensionGrant)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(extensionGrantRepository, times(1)).findByDomainAndName_migrated(anyString(), anyString());
    }

    @Test
    public void shouldCreate_existingExtensionGrant() {
        NewExtensionGrant newExtensionGrant = Mockito.mock(NewExtensionGrant.class);
        when(newExtensionGrant.getName()).thenReturn("my-extension-grant");
        when(extensionGrantRepository.findByDomainAndName_migrated(DOMAIN, "my-extension-grant")).thenReturn(Mono.just(new ExtensionGrant()));

        TestObserver<ExtensionGrant> testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(extensionGrantService.create_migrated(DOMAIN, newExtensionGrant)).subscribe(testObserver);

        testObserver.assertError(ExtensionGrantAlreadyExistsException.class);
        testObserver.assertNotComplete();

        verify(extensionGrantRepository, never()).create_migrated(any(ExtensionGrant.class));
    }

    @Test
    public void shouldUpdate() {
        UpdateExtensionGrant updateExtensionGrant = Mockito.mock(UpdateExtensionGrant.class);
        when(updateExtensionGrant.getName()).thenReturn("my-extension-grant");
        when(extensionGrantRepository.findById_migrated("my-extension-grant")).thenReturn(Mono.just(new ExtensionGrant()));
        when(extensionGrantRepository.findByDomainAndName_migrated(DOMAIN, "my-extension-grant")).thenReturn(Mono.empty());
        when(extensionGrantRepository.update_migrated(any(ExtensionGrant.class))).thenReturn(Mono.just(new ExtensionGrant()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(extensionGrantService.update_migrated(DOMAIN, "my-extension-grant", updateExtensionGrant)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(extensionGrantRepository, times(1)).findById_migrated(anyString());
        verify(extensionGrantRepository, times(1)).findByDomainAndName_migrated(anyString(), anyString());
        verify(extensionGrantRepository, times(1)).update_migrated(any(ExtensionGrant.class));
    }

    @Test
    public void shouldUpdate_technicalException() {
        UpdateExtensionGrant updateExtensionGrant = Mockito.mock(UpdateExtensionGrant.class);
        when(extensionGrantRepository.findById_migrated("my-extension-grant")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(extensionGrantService.update_migrated(DOMAIN, "my-extension-grant", updateExtensionGrant)).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(extensionGrantRepository, times(1)).findById_migrated(anyString());
        verify(extensionGrantRepository, never()).findByDomainAndName_migrated(anyString(), anyString());
        verify(extensionGrantRepository, never()).update_migrated(any(ExtensionGrant.class));
    }

    @Test
    public void shouldUpdate2_technicalException() {
        UpdateExtensionGrant updateExtensionGrant = Mockito.mock(UpdateExtensionGrant.class);
        when(updateExtensionGrant.getName()).thenReturn("my-extension-grant");
        when(extensionGrantRepository.findById_migrated("my-extension-grant")).thenReturn(Mono.just(new ExtensionGrant()));
        when(extensionGrantRepository.findByDomainAndName_migrated(DOMAIN, "my-extension-grant")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(extensionGrantService.update_migrated(DOMAIN, "my-extension-grant", updateExtensionGrant)).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(extensionGrantRepository, times(1)).findById_migrated(anyString());
        verify(extensionGrantRepository, times(1)).findByDomainAndName_migrated(anyString(), anyString());
        verify(extensionGrantRepository, never()).update_migrated(any(ExtensionGrant.class));
    }

    @Test
    public void shouldDelete_notExistingExtensionGrant() {
        when(extensionGrantRepository.findById_migrated("my-extension-grant")).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(extensionGrantService.delete_migrated(DOMAIN, "my-extension-grant")).test();

        testObserver.assertError(ExtensionGrantNotFoundException.class);
        testObserver.assertNotComplete();

        verify(applicationService, never()).findByDomainAndExtensionGrant_migrated(eq(DOMAIN), anyString());
        verify(extensionGrantRepository, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldNotDelete_extensionGrantWithClients() {
        ExtensionGrant extensionGrant = new ExtensionGrant();
        extensionGrant.setId("extension-grant-id");
        extensionGrant.setGrantType("extension-grant-type");
        when(extensionGrantRepository.findById_migrated(extensionGrant.getId())).thenReturn(Mono.just(extensionGrant));
        when(applicationService.findByDomainAndExtensionGrant_migrated(DOMAIN, extensionGrant.getGrantType() + "~" + extensionGrant.getId())).thenReturn(Mono.just(Collections.singleton(new Application())));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(extensionGrantService.delete_migrated(DOMAIN, extensionGrant.getId())).test();

        testObserver.assertError(ExtensionGrantWithApplicationsException.class);
        testObserver.assertNotComplete();

        verify(extensionGrantRepository, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldNotDelete_extensionGrantWithClients_backward_compatibility() {
        ExtensionGrant extensionGrant = new ExtensionGrant();
        extensionGrant.setId("extension-grant-id");
        extensionGrant.setGrantType("extension-grant-type");
        extensionGrant.setCreatedAt(new Date());

        ExtensionGrant extensionGrant2 = new ExtensionGrant();
        extensionGrant2.setId("extension-grant-id-2");
        extensionGrant2.setGrantType("extension-grant-type");
        extensionGrant2.setCreatedAt(new Date());

        when(extensionGrantRepository.findById_migrated(extensionGrant.getId())).thenReturn(Mono.just(extensionGrant));
        when(applicationService.findByDomainAndExtensionGrant_migrated(DOMAIN, extensionGrant.getGrantType() + "~" + extensionGrant.getId())).thenReturn(Mono.just(Collections.emptySet()));
        when(applicationService.findByDomainAndExtensionGrant_migrated(DOMAIN, extensionGrant.getGrantType())).thenReturn(Mono.just(Collections.singleton(new Application())));
        when(extensionGrantRepository.findByDomain_migrated(DOMAIN)).thenReturn(Flux.just(extensionGrant, extensionGrant2));
        TestObserver testObserver = RxJava2Adapter.monoToCompletable(extensionGrantService.delete_migrated(DOMAIN, extensionGrant.getId())).test();

        testObserver.assertError(ExtensionGrantWithApplicationsException.class);
        testObserver.assertNotComplete();

        verify(extensionGrantRepository, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldDelete_extensionGrantWithClients_backward_compatibility() {
        ExtensionGrant extensionGrant = new ExtensionGrant();
        extensionGrant.setId("extension-grant-id");
        extensionGrant.setGrantType("extension-grant-type");
        extensionGrant.setCreatedAt(new Date(System.currentTimeMillis() - 60 * 1000));

        ExtensionGrant extensionGrant2 = new ExtensionGrant();
        extensionGrant2.setId("extension-grant-id-2");
        extensionGrant2.setGrantType("extension-grant-type");
        extensionGrant2.setCreatedAt(new Date());

        when(extensionGrantRepository.findById_migrated(extensionGrant2.getId())).thenReturn(Mono.just(extensionGrant2));
        when(extensionGrantRepository.delete_migrated(extensionGrant2.getId())).thenReturn(Mono.empty());
        when(applicationService.findByDomainAndExtensionGrant_migrated(DOMAIN, extensionGrant2.getGrantType() + "~" + extensionGrant2.getId())).thenReturn(Mono.just(Collections.emptySet()));
        when(applicationService.findByDomainAndExtensionGrant_migrated(DOMAIN, extensionGrant2.getGrantType())).thenReturn(Mono.just(Collections.singleton(new Application())));
        when(extensionGrantRepository.findByDomain_migrated(DOMAIN)).thenReturn(Flux.just(extensionGrant, extensionGrant2));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        TestObserver testObserver = RxJava2Adapter.monoToCompletable(extensionGrantService.delete_migrated(DOMAIN, extensionGrant2.getId())).test();

        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(extensionGrantRepository, times(1)).delete_migrated(extensionGrant2.getId());
    }

    @Test
    public void shouldDelete_technicalException() {
        when(extensionGrantRepository.findById_migrated("my-extension-grant")).thenReturn(Mono.just(new ExtensionGrant()));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(extensionGrantService.delete_migrated(DOMAIN, "my-extension-grant")).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete() {
        ExtensionGrant existingExtensionGrant = Mockito.mock(ExtensionGrant.class);
        when(existingExtensionGrant.getId()).thenReturn("my-extension-grant");
        when(existingExtensionGrant.getGrantType()).thenReturn("my-extension-grant");
        when(extensionGrantRepository.findById_migrated("my-extension-grant")).thenReturn(Mono.just(existingExtensionGrant));
        when(extensionGrantRepository.delete_migrated("my-extension-grant")).thenReturn(Mono.empty());
        when(extensionGrantRepository.findByDomain_migrated(DOMAIN)).thenReturn(Flux.just(existingExtensionGrant));
        when(applicationService.findByDomainAndExtensionGrant_migrated(DOMAIN, "my-extension-grant~my-extension-grant")).thenReturn(Mono.just(Collections.emptySet()));
        when(applicationService.findByDomainAndExtensionGrant_migrated(DOMAIN, "my-extension-grant")).thenReturn(Mono.just(Collections.emptySet()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(extensionGrantService.delete_migrated(DOMAIN, "my-extension-grant")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(extensionGrantRepository, times(1)).delete_migrated("my-extension-grant");
    }
}

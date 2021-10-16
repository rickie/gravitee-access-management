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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.Application;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.IdentityProviderRepository;
import io.gravitee.am.service.exception.IdentityProviderNotFoundException;
import io.gravitee.am.service.exception.IdentityProviderWithApplicationsException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.impl.IdentityProviderServiceImpl;
import io.gravitee.am.service.model.NewIdentityProvider;
import io.gravitee.am.service.model.UpdateIdentityProvider;




import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
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
public class IdentityProviderServiceTest {

    @InjectMocks
    private IdentityProviderService identityProviderService = new IdentityProviderServiceImpl();

    @Mock
    private IdentityProviderRepository identityProviderRepository;

    @Mock
    private EventService eventService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private AuditService auditService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(identityProviderRepository.findById_migrated("my-identity-provider")).thenReturn(Mono.just(new IdentityProvider()));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(identityProviderService.findById_migrated("my-identity-provider")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingIdentityProvider() {
        when(identityProviderRepository.findById_migrated("my-identity-provider")).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(identityProviderService.findById_migrated("my-identity-provider")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(identityProviderRepository.findById_migrated("my-identity-provider")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(identityProviderService.findById_migrated("my-identity-provider")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomain() {
        when(identityProviderRepository.findAll_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN))).thenReturn(Flux.just(new IdentityProvider()));
        TestSubscriber<IdentityProvider> testObserver = RxJava2Adapter.fluxToFlowable(identityProviderService.findByDomain_migrated(DOMAIN)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(identityProviderRepository.findAll_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN))).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestSubscriber testSubscriber = RxJava2Adapter.fluxToFlowable(identityProviderService.findByDomain_migrated(DOMAIN)).test();

        testSubscriber.assertError(TechnicalManagementException.class);
        testSubscriber.assertNotComplete();
    }

    @Test
    public void shouldFindAllByType() {

        IdentityProvider identityProvider = new IdentityProvider();
        when(identityProviderRepository.findAll_migrated(ReferenceType.ORGANIZATION)).thenReturn(Flux.just(identityProvider));

        TestSubscriber<IdentityProvider> obs = RxJava2Adapter.fluxToFlowable(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION)).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(identityProvider);
    }

    @Test
    public void shouldFindAllByType_noIdentityProvider() {

        when(identityProviderRepository.findAll_migrated(ReferenceType.ORGANIZATION)).thenReturn(Flux.empty());

        TestSubscriber<IdentityProvider> obs = RxJava2Adapter.fluxToFlowable(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION)).test();

        obs.awaitTerminalEvent();
        obs.assertNoErrors();
        obs.assertComplete();
        obs.assertNoValues();
    }

    @Test
    public void shouldFindAllByType_TechnicalException() {

        when(identityProviderRepository.findAll_migrated(ReferenceType.ORGANIZATION)).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestSubscriber<IdentityProvider> obs = RxJava2Adapter.fluxToFlowable(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION)).test();

        obs.awaitTerminalEvent();
        obs.assertError(TechnicalException.class);
    }

    @Test
    public void shouldCreate() {
        NewIdentityProvider newIdentityProvider = Mockito.mock(NewIdentityProvider.class);
        IdentityProvider idp = new IdentityProvider();
        idp.setReferenceType(ReferenceType.DOMAIN);
        idp.setReferenceId("domain#1");
        when(identityProviderRepository.create_migrated(any(IdentityProvider.class))).thenReturn(Mono.just(idp));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(identityProviderService.create_migrated(DOMAIN, newIdentityProvider)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(identityProviderRepository, times(1)).create_migrated(any(IdentityProvider.class));
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldCreate_technicalException() {
        NewIdentityProvider newIdentityProvider = Mockito.mock(NewIdentityProvider.class);
        when(identityProviderRepository.create_migrated(any(IdentityProvider.class))).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver<IdentityProvider> testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(identityProviderService.create_migrated(DOMAIN, newIdentityProvider)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldUpdate() {
        UpdateIdentityProvider updateIdentityProvider = Mockito.mock(UpdateIdentityProvider.class);
        IdentityProvider idp = new IdentityProvider();
        idp.setReferenceType(ReferenceType.DOMAIN);
        idp.setReferenceId("domain#1");

        when(identityProviderRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-identity-provider"))).thenReturn(Mono.just(new IdentityProvider()));
        when(identityProviderRepository.update_migrated(any(IdentityProvider.class))).thenReturn(Mono.just(idp));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(identityProviderService.update_migrated(DOMAIN, "my-identity-provider", updateIdentityProvider)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(identityProviderRepository, times(1)).update_migrated(any(IdentityProvider.class));
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldUpdate_technicalException() {
        UpdateIdentityProvider updateIdentityProvider = Mockito.mock(UpdateIdentityProvider.class);
        when(identityProviderRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-identity-provider"))).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver<IdentityProvider> testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(identityProviderService.update_migrated(DOMAIN, "my-identity-provider", updateIdentityProvider)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_notExistingIdentityProvider() {
        when(identityProviderRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-identity-provider"))).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(identityProviderService.delete_migrated(DOMAIN, "my-identity-provider")).test();

        testObserver.assertError(IdentityProviderNotFoundException.class);
        testObserver.assertNotComplete();

        verify(applicationService, never()).findByIdentityProvider_migrated(anyString());
        verify(identityProviderRepository, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldDelete_identitiesWithClients() {
        when(identityProviderRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-identity-provider"))).thenReturn(Mono.just(new IdentityProvider()));
        when(applicationService.findByIdentityProvider_migrated("my-identity-provider")).thenReturn(Flux.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(identityProviderService.delete_migrated(DOMAIN, "my-identity-provider")).test();

        testObserver.assertError(IdentityProviderWithApplicationsException.class);
        testObserver.assertNotComplete();

        verify(identityProviderRepository, never()).delete_migrated(anyString());
    }


    @Test
    public void shouldDelete_technicalException() {
        when(identityProviderRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-identity-provider"))).thenReturn(Mono.just(new IdentityProvider()));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(identityProviderService.delete_migrated(DOMAIN, "my-identity-provider")).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete() {
        IdentityProvider existingIdentityProvider = Mockito.mock(IdentityProvider.class);
        when(identityProviderRepository.findById_migrated(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-identity-provider"))).thenReturn(Mono.just(existingIdentityProvider));
        when(identityProviderRepository.delete_migrated("my-identity-provider")).thenReturn(Mono.empty());
        when(applicationService.findByIdentityProvider_migrated("my-identity-provider")).thenReturn(Flux.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(identityProviderService.delete_migrated(DOMAIN, "my-identity-provider")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(identityProviderRepository, times(1)).delete_migrated("my-identity-provider");
        verify(eventService, times(1)).create_migrated(any());
    }
}

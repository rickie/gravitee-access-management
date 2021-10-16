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

import io.gravitee.am.model.Credential;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.CredentialRepository;
import io.gravitee.am.service.exception.CredentialNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.impl.CredentialServiceImpl;




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
public class CredentialServiceTest {

    @InjectMocks
    private CredentialService credentialService = new CredentialServiceImpl();

    @Mock
    private CredentialRepository credentialRepository;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(credentialRepository.findById_migrated("my-credential")).thenReturn(Mono.just(new Credential()));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(credentialService.findById_migrated("my-credential")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingCredential() {
        when(credentialRepository.findById_migrated("my-credential")).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(credentialService.findById_migrated("my-credential")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(credentialRepository.findById_migrated("my-credential")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(credentialService.findById_migrated("my-credential")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByUserId() {
        when(credentialRepository.findByUserId_migrated(ReferenceType.DOMAIN, DOMAIN, "user-id")).thenReturn(Flux.just(new Credential()));
        TestSubscriber<Credential> testSubscriber = RxJava2Adapter.fluxToFlowable(credentialService.findByUserId_migrated(ReferenceType.DOMAIN, DOMAIN, "user-id")).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldFindByUserId_technicalException() {
        when(credentialRepository.findByUserId_migrated(ReferenceType.DOMAIN, DOMAIN, "user-id")).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestSubscriber testSubscriber = RxJava2Adapter.fluxToFlowable(credentialService.findByUserId_migrated(ReferenceType.DOMAIN, DOMAIN, "user-id")).test();

        testSubscriber.assertError(TechnicalManagementException.class);
        testSubscriber.assertNotComplete();
    }

    @Test
    public void shouldFindByUsername() {
        when(credentialRepository.findByUsername_migrated(ReferenceType.DOMAIN, DOMAIN, "username")).thenReturn(Flux.just(new Credential()));
        TestSubscriber<Credential> testObserver = RxJava2Adapter.fluxToFlowable(credentialService.findByUsername_migrated(ReferenceType.DOMAIN, DOMAIN, "username")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindByUsername_technicalException() {
        when(credentialRepository.findByUsername_migrated(ReferenceType.DOMAIN, DOMAIN, "username")).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestSubscriber testSubscriber = RxJava2Adapter.fluxToFlowable(credentialService.findByUsername_migrated(ReferenceType.DOMAIN, DOMAIN, "username")).test();

        testSubscriber.assertError(TechnicalManagementException.class);
        testSubscriber.assertNotComplete();
    }

    @Test
    public void shouldFindByCredentialId() {
        when(credentialRepository.findByCredentialId_migrated(ReferenceType.DOMAIN, DOMAIN, "credentialId")).thenReturn(Flux.just(new Credential()));
        TestSubscriber<Credential> testSubscriber = RxJava2Adapter.fluxToFlowable(credentialService.findByCredentialId_migrated(ReferenceType.DOMAIN, DOMAIN, "credentialId")).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldFindByCredentialId_technicalException() {
        when(credentialRepository.findByCredentialId_migrated(ReferenceType.DOMAIN, DOMAIN, "credentialId")).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestSubscriber testSubscriber = RxJava2Adapter.fluxToFlowable(credentialService.findByCredentialId_migrated(ReferenceType.DOMAIN, DOMAIN, "credentialId")).test();

        testSubscriber.assertError(TechnicalManagementException.class);
        testSubscriber.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        Credential newCredential = Mockito.mock(Credential.class);
        when(credentialRepository.create_migrated(any(Credential.class))).thenReturn(Mono.just(new Credential()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(credentialService.create_migrated(newCredential)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(credentialRepository, times(1)).create_migrated(any(Credential.class));
    }

    @Test
    public void shouldCreate_technicalException() {
        Credential newCredential = Mockito.mock(Credential.class);
        when(credentialRepository.create_migrated(any(Credential.class))).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver<Credential> testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(credentialService.create_migrated(newCredential)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldUpdate() {
        Credential updateCredential = Mockito.mock(Credential.class);
        when(updateCredential.getId()).thenReturn("my-credential");
        when(credentialRepository.findById_migrated("my-credential")).thenReturn(Mono.just(new Credential()));
        when(credentialRepository.update_migrated(any(Credential.class))).thenReturn(Mono.just(new Credential()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(credentialService.update_migrated(updateCredential)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(credentialRepository, times(1)).findById_migrated(anyString());
        verify(credentialRepository, times(1)).update_migrated(any(Credential.class));
    }

    @Test
    public void shouldUpdate_technicalException() {
        Credential updateCredential = Mockito.mock(Credential.class);
        when(updateCredential.getId()).thenReturn("my-credential");
        when(credentialRepository.findById_migrated("my-credential")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(credentialService.update_migrated(updateCredential)).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(credentialRepository, times(1)).findById_migrated(anyString());
        verify(credentialRepository, never()).update_migrated(any(Credential.class));
    }

    @Test
    public void shouldUpdate2_technicalException() {
        Credential updateCredential = Mockito.mock(Credential.class);
        when(updateCredential.getId()).thenReturn("my-credential");
        when(credentialRepository.findById_migrated("my-credential")).thenReturn(Mono.just(new Credential()));
        when(credentialRepository.update_migrated(any(Credential.class))).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(credentialService.update_migrated(updateCredential)).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(credentialRepository, times(1)).findById_migrated(anyString());
        verify(credentialRepository, times(1)).update_migrated(any(Credential.class));
    }

    @Test
    public void shouldDelete_technicalException() {
        when(credentialRepository.findById_migrated("my-credential")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(credentialService.delete_migrated("my-credential")).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_unknownFactor() {
        when(credentialRepository.findById_migrated("my-credential")).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(credentialService.delete_migrated("my-credential")).test();
        testObserver.assertError(CredentialNotFoundException.class);
        testObserver.assertNotComplete();

        verify(credentialRepository, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldDelete() {
        when(credentialRepository.findById_migrated("my-credential")).thenReturn(Mono.just(new Credential()));
        when(credentialRepository.delete_migrated("my-credential")).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToCompletable(credentialService.delete_migrated("my-credential")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(credentialRepository, times(1)).delete_migrated("my-credential");
    }
}

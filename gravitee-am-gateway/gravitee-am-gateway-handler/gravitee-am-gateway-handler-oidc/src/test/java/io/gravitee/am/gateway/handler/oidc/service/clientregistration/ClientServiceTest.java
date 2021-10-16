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
package io.gravitee.am.gateway.handler.oidc.service.clientregistration;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.gateway.handler.oidc.service.clientregistration.impl.ClientServiceImpl;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Email;
import io.gravitee.am.model.Form;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.exception.ClientNotFoundException;
import io.gravitee.am.service.exception.InvalidClientMetadataException;
import io.gravitee.am.service.exception.InvalidRedirectUriException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientServiceTest {

    @InjectMocks
    private ClientService clientService = new ClientServiceImpl();

    @Mock
    private ApplicationService applicationService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(applicationService.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(clientService.findById_migrated("my-client")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingClient() {
        when(applicationService.findById_migrated("my-client")).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(clientService.findById_migrated("my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(applicationService.findById_migrated("my-client")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalManagementException::new)));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(clientService.findById_migrated("my-client")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void create_failWithNoDomain() {
        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.create_migrated(new Client())).test();
        testObserver.assertNotComplete();
        testObserver.assertError(InvalidClientMetadataException.class);
    }

    @Test
    public void create_implicit_invalidRedirectUri() {
        Client toCreate = new Client();
        toCreate.setDomain(DOMAIN);
        toCreate.setAuthorizedGrantTypes(Collections.singletonList("implicit"));
        toCreate.setResponseTypes(Collections.singletonList("token"));
        when(applicationService.create_migrated(any())).thenReturn(Mono.error(new InvalidRedirectUriException()));
        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.create_migrated(toCreate)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidRedirectUriException.class);
    }

    @Test
    public void create_generateUuidAsClientId() {
        when(applicationService.create_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));

        Client toCreate = new Client();
        toCreate.setDomain(DOMAIN);
        toCreate.setRedirectUris(Collections.singletonList("https://callback"));
        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.create_migrated(toCreate)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationService, times(1)).create_migrated(captor.capture());
        Assert.assertNotNull("client_id must be generated", captor.getValue().getSettings().getOauth().getClientId());
        Assert.assertNotNull("client_secret must be generated", captor.getValue().getSettings().getOauth().getClientSecret());
    }

    @Test
    public void update_failWithNoDomain() {
        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.update_migrated(new Client())).test();
        testObserver.assertNotComplete();
        testObserver.assertError(InvalidClientMetadataException.class);
    }

    @Test
    public void update_implicitGrant_invalidRedirectUri() {
        when(applicationService.update_migrated(any(Application.class))).thenReturn(Mono.error(new InvalidRedirectUriException()));

        Client toUpdate = new Client();
        toUpdate.setAuthorizedGrantTypes(Collections.singletonList("implicit"));
        toUpdate.setResponseTypes(Collections.singletonList("token"));
        toUpdate.setDomain(DOMAIN);
        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.update_migrated(toUpdate)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidRedirectUriException.class);
    }

    @Test
    public void update_defaultGrant_ok() {
        when(applicationService.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));

        Client toUpdate = new Client();
        toUpdate.setDomain(DOMAIN);
        toUpdate.setRedirectUris(Collections.singletonList("https://callback"));
        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.update_migrated(toUpdate)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationService, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void update_clientCredentials_ok() {
        when(applicationService.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));

        Client toUpdate = new Client();
        toUpdate.setDomain(DOMAIN);
        toUpdate.setAuthorizedGrantTypes(Collections.singletonList("client_credentials"));
        toUpdate.setResponseTypes(Collections.emptyList());
        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.update_migrated(toUpdate)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationService, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void shouldDelete() {
        when(applicationService.delete_migrated("my-client", null)).thenReturn(Mono.empty());
        Form form = new Form();
        form.setId("form-id");
        Email email = new Email();
        email.setId("email-id");

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(clientService.delete_migrated("my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationService, times(1)).delete_migrated("my-client", null);
    }

    @Test
    public void shouldDelete_withoutRelatedData() {
        when(applicationService.delete_migrated("my-client", null)).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(clientService.delete_migrated("my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationService, times(1)).delete_migrated("my-client", null);
    }

    @Test
    public void shouldDelete_technicalException() {
        when(applicationService.delete_migrated("my-client", null)).thenReturn(Mono.error(TechnicalManagementException::new));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(clientService.delete_migrated("my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_clientNotFound() {
        when(applicationService.delete_migrated("my-client", null)).thenReturn(Mono.error(new ClientNotFoundException("my-client")));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(clientService.delete_migrated("my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(ClientNotFoundException.class);
        testObserver.assertNotComplete();

        verify(applicationService, times(1)).delete_migrated("my-client", null);
    }

    @Test
    public void shouldRenewSecret() {
        Application client = new Application();
        client.setDomain(DOMAIN);

        when(applicationService.renewClientSecret_migrated(DOMAIN, "my-client", null)).thenReturn(Mono.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.renewClientSecret_migrated(DOMAIN, "my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationService, times(1)).renewClientSecret_migrated(DOMAIN, "my-client", null);
    }

    @Test
    public void shouldRenewSecret_clientNotFound() {
        when(applicationService.renewClientSecret_migrated(DOMAIN, "my-client", null)).thenReturn(Mono.error(new ClientNotFoundException("my-client")));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.renewClientSecret_migrated(DOMAIN, "my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(ClientNotFoundException.class);
        testObserver.assertNotComplete();

        verify(applicationService, never()).update_migrated(any());
    }

    @Test
    public void shouldRenewSecret_technicalException() {
        when(applicationService.renewClientSecret_migrated(DOMAIN, "my-client", null)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalManagementException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(clientService.renewClientSecret_migrated(DOMAIN, "my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(applicationService, never()).update_migrated(any());
    }
}

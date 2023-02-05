/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.gateway.handler.ciba.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.gravitee.am.authdevice.notifier.api.AuthenticationDeviceNotifierProvider;
import io.gravitee.am.authdevice.notifier.api.model.ADCallbackContext;
import io.gravitee.am.authdevice.notifier.api.model.ADUserResponse;
import io.gravitee.am.common.exception.oauth2.InvalidRequestException;
import io.gravitee.am.common.exception.oauth2.InvalidTokenException;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.gateway.handler.ciba.exception.AuthenticationRequestNotFoundException;
import io.gravitee.am.gateway.handler.ciba.exception.AuthorizationPendingException;
import io.gravitee.am.gateway.handler.ciba.exception.SlowDownException;
import io.gravitee.am.gateway.handler.ciba.service.request.AuthenticationRequestStatus;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.gateway.handler.manager.authdevice.notifier.AuthenticationDeviceNotifierManager;
import io.gravitee.am.gateway.handler.oauth2.exception.AccessDeniedException;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oidc.CIBASettings;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.oidc.OIDCSettings;
import io.gravitee.am.repository.oidc.api.CibaAuthRequestRepository;
import io.gravitee.am.repository.oidc.model.CibaAuthRequest;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.vertx.reactivex.core.MultiMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.*;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationRequestServiceTest {

    public static final int RETENTION_PERIOD = 960;
    private Domain domain = new Domain();

    private CIBASettings cibaSettings;

    @InjectMocks private AuthenticationRequestServiceImpl service;

    @Mock private CibaAuthRequestRepository requestRepository;

    @Mock private AuthenticationDeviceNotifierManager notifierManager;

    @Mock private JWTService jwtService;

    @Mock private ClientSyncService clientService;

    @Before
    public void init() {
        OIDCSettings oidc = new OIDCSettings();
        this.cibaSettings = new CIBASettings();
        oidc.setCibaSettings(this.cibaSettings);
        this.domain.setOidc(oidc);
    }

    @Test
    public void shouldNotRetrieve_UnknownId() {
        when(requestRepository.findById(anyString())).thenReturn(Maybe.empty());

        TestObserver<CibaAuthRequest> observer = service.retrieve(domain, "unknown").test();
        observer.awaitTerminalEvent();
        observer.assertError(AuthenticationRequestNotFoundException.class);
    }

    @Test
    public void shouldRetrieve_SlowDown() {
        CibaAuthRequest request = new CibaAuthRequest();
        request.setLastAccessAt(new Date(Instant.now().minusSeconds(1).toEpochMilli()));
        request.setStatus(AuthenticationRequestStatus.ONGOING.name());
        request.setExpireAt(new Date(Instant.now().plusSeconds(RETENTION_PERIOD).toEpochMilli()));

        when(requestRepository.findById(anyString())).thenReturn(Maybe.just(request));

        TestObserver<CibaAuthRequest> observer = service.retrieve(domain, "reqid").test();

        observer.awaitTerminalEvent();
        observer.assertError(SlowDownException.class);
    }

    @Test
    public void shouldRetrieve_Pending() {
        CibaAuthRequest request = new CibaAuthRequest();
        request.setLastAccessAt(new Date(Instant.now().minusSeconds(6).toEpochMilli()));
        request.setStatus(AuthenticationRequestStatus.ONGOING.name());
        request.setExpireAt(new Date(Instant.now().plusSeconds(RETENTION_PERIOD).toEpochMilli()));

        when(requestRepository.findById(anyString())).thenReturn(Maybe.just(request));
        when(requestRepository.update(any())).thenReturn(Single.just(request));

        TestObserver<CibaAuthRequest> observer = service.retrieve(domain, "reqid").test();

        observer.awaitTerminalEvent();
        observer.assertError(AuthorizationPendingException.class);

        verify(requestRepository).update(request);
    }

    @Test
    public void shouldRetrieve_AccessDenied() {
        CibaAuthRequest request = new CibaAuthRequest();
        request.setStatus(AuthenticationRequestStatus.REJECTED.name());
        request.setExpireAt(new Date(Instant.now().plusSeconds(RETENTION_PERIOD).toEpochMilli()));

        when(requestRepository.findById(anyString())).thenReturn(Maybe.just(request));
        when(requestRepository.delete(any())).thenReturn(Completable.complete());

        TestObserver<CibaAuthRequest> observer = service.retrieve(domain, "reqid").test();

        observer.awaitTerminalEvent();
        observer.assertError(AccessDeniedException.class);
    }

    @Test
    public void shouldRetrieve() {
        CibaAuthRequest request = new CibaAuthRequest();
        request.setStatus(AuthenticationRequestStatus.SUCCESS.name());
        request.setExpireAt(new Date(Instant.now().plusSeconds(RETENTION_PERIOD).toEpochMilli()));

        when(requestRepository.findById(anyString())).thenReturn(Maybe.just(request));
        when(requestRepository.delete(any())).thenReturn(Completable.complete());

        TestObserver<CibaAuthRequest> observer = service.retrieve(domain, "reqid").test();

        observer.awaitTerminalEvent();
        observer.assertValueCount(1);
    }

    @Test
    public void shouldNotUpdate() {
        CibaAuthRequest request = mock(CibaAuthRequest.class);
        when(requestRepository.findById(any())).thenReturn(Maybe.empty());
        TestObserver<CibaAuthRequest> observer =
                service.updateAuthDeviceInformation(request).test();

        observer.awaitTerminalEvent();
        observer.assertError(AuthenticationRequestNotFoundException.class);

        verify(requestRepository, never()).update(any());
    }

    @Test
    public void shouldUpdate() {
        CibaAuthRequest request = mock(CibaAuthRequest.class);
        when(requestRepository.findById(any())).thenReturn(Maybe.just(request));
        when(requestRepository.update(any())).thenReturn(Single.just(request));

        TestObserver<CibaAuthRequest> observer =
                service.updateAuthDeviceInformation(request).test();

        observer.awaitTerminalEvent();
        observer.assertValueCount(1);

        verify(request, never()).setLastAccessAt(any());
        verify(request).setExternalTrxId(any());
        verify(request).setExternalInformation(any());
    }

    @Test
    public void shouldUpdateAuthReqStatus() {
        String STATE = "state";
        String EXTERNAL_ID = "externalId";
        String AUTH_REQ_ID = "auth_red_id";
        boolean requestValidated = new Random().nextBoolean();

        AuthenticationDeviceNotifierProvider provider =
                mock(AuthenticationDeviceNotifierProvider.class);
        when(notifierManager.getAuthDeviceNotifierProviders()).thenReturn(List.of(provider));
        when(provider.extractUserResponse(any()))
                .thenReturn(
                        Single.just(
                                Optional.of(
                                        new ADUserResponse(EXTERNAL_ID, STATE, requestValidated))));

        JWT stateJwt = new JWT();
        stateJwt.setJti(EXTERNAL_ID);
        when(this.jwtService.decode(STATE)).thenReturn(Single.just(stateJwt));
        when(this.clientService.findByClientId(any())).thenReturn(Maybe.just(new Client()));
        when(this.jwtService.decodeAndVerify(anyString(), any(Client.class)))
                .thenReturn(Single.just(stateJwt));

        CibaAuthRequest cibaRequest = new CibaAuthRequest();
        cibaRequest.setId(AUTH_REQ_ID);
        when(this.requestRepository.findByExternalId(EXTERNAL_ID))
                .thenReturn(Maybe.just(cibaRequest));

        String status =
                requestValidated
                        ? AuthenticationRequestStatus.SUCCESS.name()
                        : AuthenticationRequestStatus.REJECTED.name();
        when(this.requestRepository.updateStatus(AUTH_REQ_ID, status))
                .thenReturn(Single.just(cibaRequest));

        ADCallbackContext context =
                new ADCallbackContext(
                        MultiMap.caseInsensitiveMultiMap(), MultiMap.caseInsensitiveMultiMap());
        TestObserver<Void> observer = this.service.validateUserResponse(context).test();
        observer.awaitTerminalEvent();
        observer.assertNoErrors();

        verify(requestRepository).updateStatus(AUTH_REQ_ID, status);
    }

    @Test
    public void shouldNotUpdateStatus_missingUserResponse() {
        AuthenticationDeviceNotifierProvider provider =
                mock(AuthenticationDeviceNotifierProvider.class);
        when(notifierManager.getAuthDeviceNotifierProviders()).thenReturn(List.of(provider));
        when(provider.extractUserResponse(any())).thenReturn(Single.just(Optional.empty()));

        ADCallbackContext context =
                new ADCallbackContext(
                        MultiMap.caseInsensitiveMultiMap(), MultiMap.caseInsensitiveMultiMap());
        TestObserver<Void> observer = this.service.validateUserResponse(context).test();

        observer.awaitTerminalEvent();
        observer.assertError(NoSuchElementException.class);

        verify(clientService, never()).findByClientId(any());
        verify(requestRepository, never()).updateStatus(any(), any());
    }

    @Test
    public void shouldNotUpdateStatus_UnknownClient() {
        String STATE = "state";
        String EXTERNAL_ID = "externalId";
        boolean requestValidated = new Random().nextBoolean();

        AuthenticationDeviceNotifierProvider provider =
                mock(AuthenticationDeviceNotifierProvider.class);
        when(notifierManager.getAuthDeviceNotifierProviders()).thenReturn(List.of(provider));
        when(provider.extractUserResponse(any()))
                .thenReturn(
                        Single.just(
                                Optional.of(
                                        new ADUserResponse(EXTERNAL_ID, STATE, requestValidated))));

        JWT stateJwt = new JWT();
        stateJwt.setJti(EXTERNAL_ID);
        when(this.jwtService.decode(STATE)).thenReturn(Single.just(stateJwt));
        when(this.clientService.findByClientId(any())).thenReturn(Maybe.empty());

        ADCallbackContext context =
                new ADCallbackContext(
                        MultiMap.caseInsensitiveMultiMap(), MultiMap.caseInsensitiveMultiMap());
        TestObserver<Void> observer = this.service.validateUserResponse(context).test();
        observer.awaitTerminalEvent();
        observer.assertError(InvalidRequestException.class);

        verify(requestRepository, never()).updateStatus(any(), any());
    }

    @Test
    public void shouldNotUpdateStatus_InvalidSignature() {
        String STATE = "state";
        String EXTERNAL_ID = "externalId";
        boolean requestValidated = new Random().nextBoolean();

        AuthenticationDeviceNotifierProvider provider =
                mock(AuthenticationDeviceNotifierProvider.class);
        when(notifierManager.getAuthDeviceNotifierProviders()).thenReturn(List.of(provider));
        when(provider.extractUserResponse(any()))
                .thenReturn(
                        Single.just(
                                Optional.of(
                                        new ADUserResponse(EXTERNAL_ID, STATE, requestValidated))));

        JWT stateJwt = new JWT();
        stateJwt.setJti(EXTERNAL_ID);
        when(this.jwtService.decode(STATE)).thenReturn(Single.just(stateJwt));
        when(this.clientService.findByClientId(any())).thenReturn(Maybe.just(new Client()));
        when(this.jwtService.decodeAndVerify(anyString(), any(Client.class)))
                .thenReturn(Single.error(new InvalidTokenException()));

        ADCallbackContext context =
                new ADCallbackContext(
                        MultiMap.caseInsensitiveMultiMap(), MultiMap.caseInsensitiveMultiMap());
        TestObserver<Void> observer = this.service.validateUserResponse(context).test();
        observer.awaitTerminalEvent();
        observer.assertError(InvalidRequestException.class);

        verify(clientService).findByClientId(any());
        verify(requestRepository, never()).updateStatus(any(), any());
    }

    @Test
    public void shouldNotUpdateStatus_StateMismatch() {
        String STATE = "state";
        String EXTERNAL_ID = "externalId";
        boolean requestValidated = new Random().nextBoolean();

        AuthenticationDeviceNotifierProvider provider =
                mock(AuthenticationDeviceNotifierProvider.class);
        when(notifierManager.getAuthDeviceNotifierProviders()).thenReturn(List.of(provider));
        when(provider.extractUserResponse(any()))
                .thenReturn(
                        Single.just(
                                Optional.of(
                                        new ADUserResponse("unknown", STATE, requestValidated))));

        JWT stateJwt = new JWT();
        stateJwt.setJti(EXTERNAL_ID);
        when(this.jwtService.decode(STATE)).thenReturn(Single.just(stateJwt));
        when(this.clientService.findByClientId(any())).thenReturn(Maybe.just(new Client()));
        when(this.jwtService.decodeAndVerify(anyString(), any(Client.class)))
                .thenReturn(Single.just(stateJwt));

        ADCallbackContext context =
                new ADCallbackContext(
                        MultiMap.caseInsensitiveMultiMap(), MultiMap.caseInsensitiveMultiMap());
        TestObserver<Void> observer = this.service.validateUserResponse(context).test();
        observer.awaitTerminalEvent();
        observer.assertError(InvalidRequestException.class);

        verify(clientService).findByClientId(any());
        verify(requestRepository, never()).updateStatus(any(), any());
    }

    @Test
    public void shouldNotUpdateStatus_UnknownRequestId() {
        String STATE = "state";
        String EXTERNAL_ID = "externalId";
        String AUTH_REQ_ID = "auth_red_id";
        boolean requestValidated = new Random().nextBoolean();

        AuthenticationDeviceNotifierProvider provider =
                mock(AuthenticationDeviceNotifierProvider.class);
        when(notifierManager.getAuthDeviceNotifierProviders()).thenReturn(List.of(provider));
        when(provider.extractUserResponse(any()))
                .thenReturn(
                        Single.just(
                                Optional.of(
                                        new ADUserResponse(EXTERNAL_ID, STATE, requestValidated))));

        JWT stateJwt = new JWT();
        stateJwt.setJti(EXTERNAL_ID);
        when(this.jwtService.decode(STATE)).thenReturn(Single.just(stateJwt));
        when(this.clientService.findByClientId(any())).thenReturn(Maybe.just(new Client()));
        when(this.jwtService.decodeAndVerify(anyString(), any(Client.class)))
                .thenReturn(Single.just(stateJwt));

        CibaAuthRequest cibaRequest = new CibaAuthRequest();
        cibaRequest.setId(AUTH_REQ_ID);
        when(this.requestRepository.findByExternalId(EXTERNAL_ID)).thenReturn(Maybe.empty());

        ADCallbackContext context =
                new ADCallbackContext(
                        MultiMap.caseInsensitiveMultiMap(), MultiMap.caseInsensitiveMultiMap());
        TestObserver<Void> observer = this.service.validateUserResponse(context).test();
        observer.awaitTerminalEvent();
        observer.assertError(InvalidRequestException.class);

        verify(clientService).findByClientId(any());
        verify(jwtService).decodeAndVerify(anyString(), any(Client.class));
        verify(requestRepository, never()).updateStatus(any(), any());
    }
}

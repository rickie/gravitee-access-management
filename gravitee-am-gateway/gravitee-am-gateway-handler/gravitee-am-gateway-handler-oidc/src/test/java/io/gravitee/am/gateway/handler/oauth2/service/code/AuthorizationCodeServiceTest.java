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
package io.gravitee.am.gateway.handler.oauth2.service.code;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.gateway.handler.oauth2.exception.InvalidGrantException;
import io.gravitee.am.gateway.handler.oauth2.service.code.impl.AuthorizationCodeServiceImpl;
import io.gravitee.am.gateway.handler.oauth2.service.request.AuthorizationRequest;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.repository.oauth2.api.AuthorizationCodeRepository;
import io.gravitee.am.repository.oauth2.api.RefreshTokenRepository;
import io.gravitee.am.repository.oauth2.model.AccessToken;
import io.gravitee.am.repository.oauth2.model.AuthorizationCode;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthorizationCodeServiceTest {

    @InjectMocks
    private AuthorizationCodeService authorizationCodeService = new AuthorizationCodeServiceImpl();

    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    public void shouldCreate_noExistingCode() {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        authorizationRequest.setClientId("my-client-id");

        User user = new User();
        user.setUsername("my-username-id");

        when(authorizationCodeRepository.create_migrated(any())).thenReturn(Mono.just(new AuthorizationCode()));

        TestObserver<AuthorizationCode> testObserver = RxJava2Adapter.monoToSingle(authorizationCodeService.create_migrated(authorizationRequest, user)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(authorizationCodeRepository, times(1)).create_migrated(any());
    }


    @Test
    public void shouldRemove_existingCode() {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        authorizationRequest.setClientId("my-client-id");

        Client client = new Client();
        client.setClientId("my-client-id");

        AuthorizationCode authorizationCode = new AuthorizationCode();
        authorizationCode.setId("code-id");
        authorizationCode.setCode("my-code");
        authorizationCode.setClientId("my-client-id");

        when(authorizationCodeRepository.findByCode_migrated(authorizationCode.getCode())).thenReturn(Mono.just(authorizationCode));
        when(authorizationCodeRepository.delete_migrated(authorizationCode.getId())).thenReturn(Mono.just(authorizationCode));
        when(accessTokenRepository.findByAuthorizationCode_migrated(authorizationCode.getCode())).thenReturn(Flux.empty());

        TestObserver<AuthorizationCode> testObserver = RxJava2Adapter.monoToMaybe(authorizationCodeService.remove_migrated(authorizationCode.getCode(), client)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(authorizationCodeRepository, times(1)).findByCode_migrated(any());
        verify(authorizationCodeRepository, times(1)).delete_migrated(any());
        verify(accessTokenRepository, times(1)).findByAuthorizationCode_migrated(anyString());
        verify(accessTokenRepository, never()).delete_migrated(anyString());
        verify(refreshTokenRepository, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldRemove_invalidCode_existingTokens_noRefreshToken() {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        authorizationRequest.setClientId("my-client-id");

        Client client = new Client();
        client.setClientId("my-client-id");

        AuthorizationCode authorizationCode = new AuthorizationCode();
        authorizationCode.setCode("my-code");
        authorizationCode.setClientId("my-client-id");

        AccessToken accessToken = new AccessToken();
        accessToken.setToken("my-access-token-1");
        accessToken.setAuthorizationCode("my-code");

        AccessToken accessToken2 = new AccessToken();
        accessToken2.setToken("my-access-token-2");
        accessToken2.setAuthorizationCode("my-code");

        List<AccessToken> tokens = Arrays.asList(accessToken, accessToken2);

        when(authorizationCodeRepository.findByCode_migrated(any())).thenReturn(Mono.empty());
        when(accessTokenRepository.findByAuthorizationCode_migrated(anyString())).thenReturn(RxJava2Adapter.observableToFlux(Observable.fromIterable(tokens), BackpressureStrategy.BUFFER));
        when(accessTokenRepository.delete_migrated(anyString())).thenReturn(Mono.empty());

        TestObserver<AuthorizationCode> testObserver = RxJava2Adapter.monoToMaybe(authorizationCodeService.remove_migrated(authorizationCode.getCode(), client)).test();
        testObserver.assertError(InvalidGrantException.class);

        verify(authorizationCodeRepository, times(1)).findByCode_migrated(any());
        verify(accessTokenRepository, times(1)).findByAuthorizationCode_migrated(anyString());
        verify(accessTokenRepository, times(2)).delete_migrated(anyString());
        verify(authorizationCodeRepository, never()).delete_migrated(any());
        verify(refreshTokenRepository, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldRemove_invalidCode_existingTokens_refreshTokens() {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        authorizationRequest.setClientId("my-client-id");

        Client client = new Client();
        client.setClientId("my-client-id");

        AuthorizationCode authorizationCode = new AuthorizationCode();
        authorizationCode.setCode("my-code");
        authorizationCode.setClientId("my-client-id");

        AccessToken accessToken = new AccessToken();
        accessToken.setToken("my-access-token-1");
        accessToken.setAuthorizationCode("my-code");
        accessToken.setRefreshToken("my-refresh-token-1");

        AccessToken accessToken2 = new AccessToken();
        accessToken2.setToken("my-access-token-2");
        accessToken2.setAuthorizationCode("my-code");
        accessToken2.setRefreshToken("my-refresh-token-2");

        List<AccessToken> tokens = Arrays.asList(accessToken, accessToken2);

        when(authorizationCodeRepository.findByCode_migrated(any())).thenReturn(Mono.empty());
        when(accessTokenRepository.findByAuthorizationCode_migrated(anyString())).thenReturn(RxJava2Adapter.observableToFlux(Observable.fromIterable(tokens), BackpressureStrategy.BUFFER));
        when(accessTokenRepository.delete_migrated(anyString())).thenReturn(Mono.empty());
        when(refreshTokenRepository.delete_migrated(anyString())).thenReturn(Mono.empty());

        TestObserver<AuthorizationCode> testObserver = RxJava2Adapter.monoToMaybe(authorizationCodeService.remove_migrated(authorizationCode.getCode(), client)).test();
        testObserver.assertError(InvalidGrantException.class);

        verify(authorizationCodeRepository, times(1)).findByCode_migrated(any());
        verify(accessTokenRepository, times(1)).findByAuthorizationCode_migrated(anyString());
        verify(accessTokenRepository, times(2)).delete_migrated(anyString());
        verify(refreshTokenRepository, times(2)).delete_migrated(anyString());
        verify(authorizationCodeRepository, never()).delete_migrated(any());
    }
}

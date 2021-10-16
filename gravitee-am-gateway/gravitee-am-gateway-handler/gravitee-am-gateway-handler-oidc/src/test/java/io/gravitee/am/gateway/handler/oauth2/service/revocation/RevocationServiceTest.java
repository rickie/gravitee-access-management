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
package io.gravitee.am.gateway.handler.oauth2.service.revocation;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import io.gravitee.am.common.oauth2.TokenTypeHint;
import io.gravitee.am.gateway.handler.oauth2.exception.InvalidGrantException;
import io.gravitee.am.gateway.handler.oauth2.service.revocation.impl.RevocationTokenServiceImpl;
import io.gravitee.am.gateway.handler.oauth2.service.token.Token;
import io.gravitee.am.gateway.handler.oauth2.service.token.TokenService;
import io.gravitee.am.gateway.handler.oauth2.service.token.impl.AccessToken;
import io.gravitee.am.gateway.handler.oauth2.service.token.impl.RefreshToken;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class RevocationServiceTest {

    @InjectMocks
    private RevocationTokenService revocationTokenService = new RevocationTokenServiceImpl();

    @Mock
    private TokenService tokenService;

    @Test
    public void shouldNotRevoke_WrongRequestedClientId() {
        final RevocationTokenRequest revocationTokenRequest = new RevocationTokenRequest("token");

        AccessToken accessToken = new AccessToken("token");
        accessToken.setClientId("client-id");

        Client client = new Client();
        client.setClientId("wrong-client-id");

        when(tokenService.getAccessToken_migrated("token", client)).thenReturn(Mono.just(accessToken));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(revocationTokenService.revoke_migrated(revocationTokenRequest, client)).test();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidGrantException.class);

        verify(tokenService, times(1)).getAccessToken_migrated("token", client);
        verify(tokenService, never()).deleteAccessToken_migrated(anyString());
        verify(tokenService, never()).getRefreshToken_migrated("token", client);
        verify(tokenService, never()).deleteRefreshToken_migrated(anyString());
    }

    @Test
    public void shouldRevoke_evenWithInvalidToken() {
        final RevocationTokenRequest revocationTokenRequest = new RevocationTokenRequest("token");

        Client client = new Client();
        client.setClientId("client-id");

        when(tokenService.getAccessToken_migrated("token", client)).thenReturn(Mono.empty());
        when(tokenService.getRefreshToken_migrated("token", client)).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(revocationTokenService.revoke_migrated(revocationTokenRequest, client)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(tokenService, times(1)).getAccessToken_migrated("token", client);
        verify(tokenService, never()).deleteAccessToken_migrated(anyString());
        verify(tokenService, times(1)).getRefreshToken_migrated("token", client);
        verify(tokenService, never()).deleteRefreshToken_migrated(anyString());

    }

    @Test
    public void shouldRevoke_accessToken() {
        final RevocationTokenRequest revocationTokenRequest = new RevocationTokenRequest("token");

        Client client = new Client();
        client.setClientId("client-id");

        AccessToken accessToken = new AccessToken("token");
        accessToken.setClientId("client-id");

        when(tokenService.getAccessToken_migrated("token", client)).thenReturn(Mono.just(accessToken));
        when(tokenService.deleteAccessToken_migrated("token")).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(revocationTokenService.revoke_migrated(revocationTokenRequest, client)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(tokenService, times(1)).getAccessToken_migrated("token", client);
        verify(tokenService, times(1)).deleteAccessToken_migrated("token");
        verify(tokenService, never()).getRefreshToken_migrated(anyString(), any());
        verify(tokenService, never()).deleteRefreshToken_migrated(anyString());

    }

    @Test
    public void shouldRevoke_refreshToken() {
        final RevocationTokenRequest revocationTokenRequest = new RevocationTokenRequest("token");
        revocationTokenRequest.setHint(TokenTypeHint.REFRESH_TOKEN);

        Client client = new Client();
        client.setClientId("client-id");

        Token refreshToken = new RefreshToken("token");
        refreshToken.setClientId("client-id");

        when(tokenService.getRefreshToken_migrated("token", client)).thenReturn(Mono.just(refreshToken));
        when(tokenService.deleteRefreshToken_migrated("token")).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(revocationTokenService.revoke_migrated(revocationTokenRequest, client)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(tokenService, times(1)).getRefreshToken_migrated("token", client);
        verify(tokenService, times(1)).deleteRefreshToken_migrated("token");
        verify(tokenService, never()).getAccessToken_migrated("token", client);
        verify(tokenService, never()).deleteAccessToken_migrated("token");

    }
}

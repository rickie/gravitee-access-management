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
package io.gravitee.am.gateway.handler.common.oauth2;

import static org.mockito.Mockito.*;

import io.gravitee.am.common.exception.jwt.JWTException;
import io.gravitee.am.common.exception.oauth2.InvalidTokenException;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.gateway.handler.common.oauth2.impl.IntrospectionTokenServiceImpl;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.repository.oauth2.model.AccessToken;


import io.reactivex.observers.TestObserver;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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
public class IntrospectionTokenServiceTest {

    @InjectMocks
    private IntrospectionTokenService introspectionTokenService = new IntrospectionTokenServiceImpl();

    @Mock
    private JWTService jwtService;

    @Mock
    private ClientSyncService clientService;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Test
    public void shouldIntrospect_validToken_offline_verification() {
        final String token = "token";
        final JWT jwt = new JWT();
        jwt.setJti("jti");
        jwt.setDomain("domain");
        jwt.setAud("client");
        final Client client = new Client();
        client.setClientId("client-id");

        when(jwtService.decode_migrated(token)).thenReturn(Mono.just(jwt));
        when(clientService.findByDomainAndClientId_migrated(jwt.getDomain(), jwt.getAud())).thenReturn(Mono.just(client));
        when(jwtService.decodeAndVerify_migrated(token, client)).thenReturn(Mono.just(jwt));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(introspectionTokenService.introspect_migrated(token, true)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(accessTokenRepository, never()).findByToken_migrated(jwt.getJti());
    }

    @Test
    public void shouldIntrospect_validToken_online_verification() {
        final String token = "token";

        final JWT jwt = new JWT();
        jwt.setJti("jti");
        jwt.setDomain("domain");
        jwt.setAud("client");
        jwt.setIat(Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond());

        final Client client = new Client();
        client.setClientId("client-id");

        final AccessToken accessToken = new AccessToken();
        accessToken.setExpireAt(new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli()));

        when(jwtService.decode_migrated(token)).thenReturn(Mono.just(jwt));
        when(clientService.findByDomainAndClientId_migrated(jwt.getDomain(), jwt.getAud())).thenReturn(Mono.just(client));
        when(jwtService.decodeAndVerify_migrated(token, client)).thenReturn(Mono.just(jwt));
        when(accessTokenRepository.findByToken_migrated(jwt.getJti())).thenReturn(Mono.just(accessToken));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(introspectionTokenService.introspect_migrated(token, false)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(accessTokenRepository, times(1)).findByToken_migrated(jwt.getJti());
    }

    @Test
    public void shouldIntrospect_validToken_offline_verification_timer() {
        final String token = "token";
        final JWT jwt = new JWT();
        jwt.setJti("jti");
        jwt.setDomain("domain");
        jwt.setAud("client");
        jwt.setIat(Instant.now().getEpochSecond());
        final Client client = new Client();
        client.setClientId("client-id");

        when(jwtService.decode_migrated(token)).thenReturn(Mono.just(jwt));
        when(clientService.findByDomainAndClientId_migrated(jwt.getDomain(), jwt.getAud())).thenReturn(Mono.just(client));
        when(jwtService.decodeAndVerify_migrated(token, client)).thenReturn(Mono.just(jwt));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(introspectionTokenService.introspect_migrated(token, false)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        // repository should not be call because the token is too recent
        verify(accessTokenRepository, never()).findByToken_migrated(jwt.getJti());
    }

    @Test
    public void shouldIntrospect_invalidValidToken_jwt_exception() {
        final String token = "token";
        final JWT jwt = new JWT();
        jwt.setJti("jti");
        jwt.setDomain("domain");
        jwt.setAud("client");
        jwt.setIat(Instant.now().getEpochSecond());
        final Client client = new Client();
        client.setClientId("client-id");

        when(jwtService.decode_migrated(token)).thenReturn(Mono.just(jwt));
        when(clientService.findByDomainAndClientId_migrated(jwt.getDomain(), jwt.getAud())).thenReturn(Mono.just(client));
        when(jwtService.decodeAndVerify_migrated(token, client)).thenReturn(Mono.error(new JWTException("invalid token")));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(introspectionTokenService.introspect_migrated(token, false)).test();
        testObserver.assertError(InvalidTokenException.class);
        verify(accessTokenRepository, never()).findByToken_migrated(jwt.getJti());
    }

    @Test
    public void shouldIntrospect_invalidValidToken_token_revoked() {
        final String token = "token";
        final JWT jwt = new JWT();
        jwt.setJti("jti");
        jwt.setDomain("domain");
        jwt.setAud("client");
        jwt.setIat(Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond());
        final Client client = new Client();
        client.setClientId("client-id");

        when(jwtService.decode_migrated(token)).thenReturn(Mono.just(jwt));
        when(clientService.findByDomainAndClientId_migrated(jwt.getDomain(), jwt.getAud())).thenReturn(Mono.just(client));
        when(jwtService.decodeAndVerify_migrated(token, client)).thenReturn(Mono.just(jwt));
        when(accessTokenRepository.findByToken_migrated(jwt.getJti())).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToSingle(introspectionTokenService.introspect_migrated(token, false)).test();
        testObserver.assertError(InvalidTokenException.class);
        verify(accessTokenRepository, times(1)).findByToken_migrated(jwt.getJti());
    }

    @Test
    public void shouldIntrospect_invalidValidToken_token_expired() {
        final String token = "token";
        final JWT jwt = new JWT();
        jwt.setJti("jti");
        jwt.setDomain("domain");
        jwt.setAud("client");
        jwt.setIat(Instant.now().minus(1, ChronoUnit.DAYS).getEpochSecond());
        final Client client = new Client();
        client.setClientId("client-id");

        final AccessToken accessToken = new AccessToken();
        accessToken.setExpireAt(new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli()));

        when(jwtService.decode_migrated(token)).thenReturn(Mono.just(jwt));
        when(clientService.findByDomainAndClientId_migrated(jwt.getDomain(), jwt.getAud())).thenReturn(Mono.just(client));
        when(jwtService.decodeAndVerify_migrated(token, client)).thenReturn(Mono.just(jwt));
        when(accessTokenRepository.findByToken_migrated(jwt.getJti())).thenReturn(Mono.just(accessToken));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(introspectionTokenService.introspect_migrated(token, false)).test();
        testObserver.assertError(InvalidTokenException.class);
        verify(accessTokenRepository, times(1)).findByToken_migrated(jwt.getJti());
    }
}

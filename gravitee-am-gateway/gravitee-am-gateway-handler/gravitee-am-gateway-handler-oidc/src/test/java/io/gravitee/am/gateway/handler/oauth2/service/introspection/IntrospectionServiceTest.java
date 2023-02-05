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
package io.gravitee.am.gateway.handler.oauth2.service.introspection;

import static org.mockito.Mockito.*;

import io.gravitee.am.common.jwt.Claims;
import io.gravitee.am.gateway.handler.oauth2.service.introspection.impl.IntrospectionServiceImpl;
import io.gravitee.am.gateway.handler.oauth2.service.token.TokenService;
import io.gravitee.am.gateway.handler.oauth2.service.token.impl.AccessToken;
import io.gravitee.am.model.User;
import io.gravitee.am.service.UserService;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class IntrospectionServiceTest {

    @InjectMocks private IntrospectionService introspectionService = new IntrospectionServiceImpl();

    @Mock private TokenService tokenService;

    @Mock private UserService userService;

    @Test
    public void shouldSearchForAUser() {
        String token = "token";
        AccessToken accessToken = new AccessToken(token);
        accessToken.setSubject("user");
        accessToken.setClientId("client-id");
        when(tokenService.introspect("token")).thenReturn(Single.just(accessToken));
        when(userService.findById("user")).thenReturn(Maybe.just(new User()));

        IntrospectionRequest introspectionRequest = new IntrospectionRequest(token);
        TestObserver<IntrospectionResponse> testObserver =
                introspectionService.introspect(introspectionRequest).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(userService, times(1)).findById("user");
    }

    @Test
    public void shouldNotSearchForAUser_clientCredentials() {
        String token = "token";
        AccessToken accessToken = new AccessToken(token);
        accessToken.setSubject("client-id");
        accessToken.setClientId("client-id");
        when(tokenService.introspect("token")).thenReturn(Single.just(accessToken));

        IntrospectionRequest introspectionRequest = new IntrospectionRequest(token);
        TestObserver<IntrospectionResponse> testObserver =
                introspectionService.introspect(introspectionRequest).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(userService, never()).findById(anyString());
    }

    @Test
    public void shouldReturnCustomClaims() {
        String token = "token";
        AccessToken accessToken = new AccessToken(token);
        accessToken.setSubject("client-id");
        accessToken.setClientId("client-id");
        accessToken.setCreatedAt(new Date());
        accessToken.setExpireAt(new Date());
        accessToken.setAdditionalInformation(Collections.singletonMap("custom-claim", "test"));
        when(tokenService.introspect(token)).thenReturn(Single.just(accessToken));

        IntrospectionRequest introspectionRequest = new IntrospectionRequest(token);
        TestObserver<IntrospectionResponse> testObserver =
                introspectionService.introspect(introspectionRequest).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(
                introspectionResponse -> introspectionResponse.get("custom-claim").equals("test"));
    }

    @Test
    public void shouldNotReturnAudClaim() {
        String token = "token";
        AccessToken accessToken = new AccessToken(token);
        accessToken.setSubject("client-id");
        accessToken.setClientId("client-id");
        accessToken.setCreatedAt(new Date());
        accessToken.setExpireAt(new Date());
        accessToken.setAdditionalInformation(Collections.singletonMap(Claims.aud, "test-aud"));
        when(tokenService.introspect(token)).thenReturn(Single.just(accessToken));

        IntrospectionRequest introspectionRequest = new IntrospectionRequest(token);
        TestObserver<IntrospectionResponse> testObserver =
                introspectionService.introspect(introspectionRequest).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(
                introspectionResponse -> !introspectionResponse.containsKey(Claims.aud));
    }
}

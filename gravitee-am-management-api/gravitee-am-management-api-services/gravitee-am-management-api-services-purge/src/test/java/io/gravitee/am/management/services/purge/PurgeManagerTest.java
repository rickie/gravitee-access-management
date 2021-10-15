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
package io.gravitee.am.management.services.purge;

import static org.mockito.Mockito.*;

import io.gravitee.am.repository.management.api.AuthenticationFlowContextRepository;
import io.gravitee.am.repository.management.api.LoginAttemptRepository;
import io.gravitee.am.repository.management.api.PermissionTicketRepository;
import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.repository.oauth2.api.AuthorizationCodeRepository;
import io.gravitee.am.repository.oauth2.api.RefreshTokenRepository;
import io.gravitee.am.repository.oauth2.api.ScopeApprovalRepository;
import io.gravitee.am.repository.oidc.api.RequestObjectRepository;
import io.reactivex.Completable;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class PurgeManagerTest {

    @InjectMocks
    private PurgeManager manager;
    
    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @Mock
    private PermissionTicketRepository permissionTicketRepository;

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RequestObjectRepository requestObjectRepository;

    @Mock
    private ScopeApprovalRepository scopeApprovalRepository;

    @Mock
    private AuthorizationCodeRepository authorizationCodeRepository;

    @Mock
    private AuthenticationFlowContextRepository authenticationFlowContextRepository;

    @Before
    public void prepare() {
        when(accessTokenRepository.purgeExpiredData()).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
        when(loginAttemptRepository.purgeExpiredData()).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
        when(permissionTicketRepository.purgeExpiredData()).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
        when(authorizationCodeRepository.purgeExpiredData()).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
        when(scopeApprovalRepository.purgeExpiredData()).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
        when(refreshTokenRepository.purgeExpiredData()).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
        when(requestObjectRepository.purgeExpiredData()).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
        when(authenticationFlowContextRepository.purgeExpiredData()).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
    }

    @Test
    public void testNullExclude() {
        manager.purge(null);

        verify(accessTokenRepository).purgeExpiredData();
        verify(loginAttemptRepository).purgeExpiredData();
        verify(permissionTicketRepository).purgeExpiredData();
        verify(authorizationCodeRepository).purgeExpiredData();
        verify(scopeApprovalRepository).purgeExpiredData();
        verify(refreshTokenRepository).purgeExpiredData();
        verify(requestObjectRepository).purgeExpiredData();
        verify(authenticationFlowContextRepository).purgeExpiredData();
    }

    @Test
    public void testEmptyExclude() {
        manager.purge(Collections.emptyList());

        verify(accessTokenRepository).purgeExpiredData();
        verify(loginAttemptRepository).purgeExpiredData();
        verify(permissionTicketRepository).purgeExpiredData();
        verify(authorizationCodeRepository).purgeExpiredData();
        verify(scopeApprovalRepository).purgeExpiredData();
        verify(refreshTokenRepository).purgeExpiredData();
        verify(requestObjectRepository).purgeExpiredData();
        verify(authenticationFlowContextRepository).purgeExpiredData();
    }

    @Test
    public void testExclude_AccessToken() {
        manager.purge(Arrays.asList(TableName.access_tokens));

        verify(accessTokenRepository, never()).purgeExpiredData();
        verify(loginAttemptRepository).purgeExpiredData();
        verify(permissionTicketRepository).purgeExpiredData();
        verify(authorizationCodeRepository).purgeExpiredData();
        verify(scopeApprovalRepository).purgeExpiredData();
        verify(refreshTokenRepository).purgeExpiredData();
        verify(requestObjectRepository).purgeExpiredData();
        verify(authenticationFlowContextRepository).purgeExpiredData();
    }
}

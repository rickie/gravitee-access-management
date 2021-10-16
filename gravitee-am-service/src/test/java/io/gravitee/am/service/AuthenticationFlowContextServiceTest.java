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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.AuthenticationFlowContext;
import io.gravitee.am.repository.management.api.AuthenticationFlowContextRepository;
import io.gravitee.am.service.exception.AuthenticationFlowConsistencyException;
import io.gravitee.am.service.impl.AuthenticationFlowContextServiceImpl;


import io.reactivex.observers.TestObserver;
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
public class AuthenticationFlowContextServiceTest {
    private static final String SESSION_ID = "someid";

    @InjectMocks
    private AuthenticationFlowContextServiceImpl service;

    @Mock
    private AuthenticationFlowContextRepository authFlowContextRepository;

    @Test
    public void testLoadContext_UnknownSessionId() {
        // if sessionId is unknown load default Context
        when(authFlowContextRepository.findLastByTransactionId_migrated(any())).thenReturn(Mono.empty());

        TestObserver<AuthenticationFlowContext> testObserver = RxJava2Adapter.monoToMaybe(service.loadContext_migrated(SESSION_ID, 1)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertValue(ctx -> ctx.getVersion() == 0);
        verify(authFlowContextRepository).findLastByTransactionId_migrated(any());
    }

    @Test
    public void testLoadContext() {
        AuthenticationFlowContext context = new AuthenticationFlowContext();
        context.setVersion(1);
        context.setTransactionId(SESSION_ID);

        // if sessionId is unknown load default Context
        when(authFlowContextRepository.findLastByTransactionId_migrated(any())).thenReturn(Mono.just(context));

        TestObserver<AuthenticationFlowContext> testObserver = RxJava2Adapter.monoToMaybe(service.loadContext_migrated(SESSION_ID, 1)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertValue(ctx -> ctx.getVersion() == 1);
        testObserver.assertValue(ctx -> ctx.getTransactionId().equals(SESSION_ID));
        verify(authFlowContextRepository).findLastByTransactionId_migrated(any());
    }

    @Test
    public void testLoadContext_ConsistencyError() {
        AuthenticationFlowContext context = new AuthenticationFlowContext();
        context.setVersion(1);
        context.setTransactionId(SESSION_ID);

        // if sessionId is unknown load default Context
        when(authFlowContextRepository.findLastByTransactionId_migrated(any())).thenReturn(Mono.just(context));

        TestObserver<AuthenticationFlowContext> testObserver = RxJava2Adapter.monoToMaybe(service.loadContext_migrated(SESSION_ID, 2)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(error -> error instanceof AuthenticationFlowConsistencyException);
        verify(authFlowContextRepository).findLastByTransactionId_migrated(any());
    }

    @Test
    public void testClearContext() {
        when(authFlowContextRepository.delete_migrated(SESSION_ID)).thenReturn(Mono.empty());
        TestObserver<Void> testObserver = RxJava2Adapter.monoToCompletable(service.clearContext_migrated(SESSION_ID)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        verify(authFlowContextRepository).delete_migrated(SESSION_ID);
    }

    @Test
    public void testClearContext_NullId() {
        TestObserver<Void> testObserver = RxJava2Adapter.monoToCompletable(service.clearContext_migrated(null)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        verify(authFlowContextRepository, never()).delete_migrated(any());
    }
}

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
package io.gravitee.am.gateway.handler.users.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.gravitee.am.gateway.handler.users.service.impl.UserServiceImpl;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.service.ScopeApprovalService;
import io.gravitee.am.service.exception.ScopeApprovalNotFoundException;



import io.reactivex.observers.TestObserver;
import java.util.Set;
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
public class UserServiceTest {

    @InjectMocks
    private UserService userService = new UserServiceImpl();

    @Mock
    private Domain domain;

    @Mock
    private ScopeApprovalService scopeApprovalService;

    @Test
    public void shouldFindUserConsents() {
        final String userId = "userId";
        final String domainId = "domainId";

        final ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId("consentId");
        scopeApproval.setUserId(userId);
        scopeApproval.setClientId("");
        scopeApproval.setScope("");

        when(domain.getId()).thenReturn(domainId);
        when(scopeApprovalService.findByDomainAndUser_migrated(domainId, userId)).thenReturn(Flux.just(scopeApproval));

        TestObserver<Set<ScopeApproval>> testObserver = RxJava2Adapter.monoToSingle(userService.consents_migrated(userId)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(scopeApprovals -> scopeApprovals.iterator().next().getId().equals("consentId"));
    }

    @Test
    public void shouldFindUserConsent() {
        final ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId("consentId");

        when(scopeApprovalService.findById_migrated("consentId")).thenReturn(Mono.just(scopeApproval));

        TestObserver<ScopeApproval> testObserver = RxJava2Adapter.monoToMaybe(userService.consent_migrated("consentId")).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(scopeApproval1 -> scopeApproval1.getId().equals("consentId"));
    }

    @Test
    public void shouldNotFindUserConsent_consentNotFound() {
        final ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId("consentId");

        when(scopeApprovalService.findById_migrated(anyString())).thenReturn(Mono.empty());

        TestObserver<ScopeApproval> testObserver = RxJava2Adapter.monoToMaybe(userService.consent_migrated("consentId")).test();

        testObserver.assertNotComplete();
        testObserver.assertError(ScopeApprovalNotFoundException.class);
    }

    @Test
    public void shouldRevokeConsents() {
        final String userId = "userId";
        final String domainId = "domainId";

        final ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId("consentId");

        when(domain.getId()).thenReturn(domainId);
        when(scopeApprovalService.revokeByUser_migrated(domainId, userId, null)).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(userService.revokeConsents_migrated(userId)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void shouldRevokeConsent() {
        final String domainId = "domainId";
        final String userId = "userId";
        final String consentId = "consentId";

        when(domain.getId()).thenReturn(domainId);
        when(scopeApprovalService.revokeByConsent_migrated(domainId, userId, consentId, null)).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(userService.revokeConsent_migrated(userId, consentId)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }
}

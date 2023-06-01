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
package io.gravitee.am.gateway.handler.users.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.gravitee.am.gateway.handler.users.service.impl.UserServiceImpl;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.service.ScopeApprovalService;
import io.gravitee.am.service.exception.ScopeApprovalNotFoundException;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks private UserService userService = new UserServiceImpl();

    @Mock private Domain domain;

    @Mock private ScopeApprovalService scopeApprovalService;

    @Test
    public void shouldFindUserConsents() {
        String userId = "userId";
        String domainId = "domainId";

        ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId("consentId");
        scopeApproval.setUserId(userId);
        scopeApproval.setClientId("");
        scopeApproval.setScope("");

        when(domain.getId()).thenReturn(domainId);
        when(scopeApprovalService.findByDomainAndUser(domainId, userId))
                .thenReturn(Flowable.just(scopeApproval));

        TestObserver<Set<ScopeApproval>> testObserver = userService.consents(userId).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(
                scopeApprovals -> scopeApprovals.iterator().next().getId().equals("consentId"));
    }

    @Test
    public void shouldFindUserConsent() {
        ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId("consentId");

        when(scopeApprovalService.findById("consentId")).thenReturn(Maybe.just(scopeApproval));

        TestObserver<ScopeApproval> testObserver = userService.consent("consentId").test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(scopeApproval1 -> scopeApproval1.getId().equals("consentId"));
    }

    @Test
    public void shouldNotFindUserConsent_consentNotFound() {
        ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId("consentId");

        when(scopeApprovalService.findById(anyString())).thenReturn(Maybe.empty());

        TestObserver<ScopeApproval> testObserver = userService.consent("consentId").test();

        testObserver.assertNotComplete();
        testObserver.assertError(ScopeApprovalNotFoundException.class);
    }

    @Test
    public void shouldRevokeConsents() {
        String userId = "userId";
        String domainId = "domainId";

        ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId("consentId");

        when(domain.getId()).thenReturn(domainId);
        when(scopeApprovalService.revokeByUser(domainId, userId, null))
                .thenReturn(Completable.complete());

        TestObserver testObserver = userService.revokeConsents(userId).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void shouldRevokeConsent() {
        String domainId = "domainId";
        String userId = "userId";
        String consentId = "consentId";

        when(domain.getId()).thenReturn(domainId);
        when(scopeApprovalService.revokeByConsent(domainId, userId, consentId, null))
                .thenReturn(Completable.complete());

        TestObserver testObserver = userService.revokeConsent(userId, consentId).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }
}

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
package io.gravitee.am.management.handlers.management.api.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.common.http.HttpStatusCode;


import javax.ws.rs.core.Response;
import org.junit.Test;

import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserConsentResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetUserConsent() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final User mockUser = new User();
        mockUser.setId("user-id-1");

        final Application mockClient = new Application();
        mockClient.setId("client-id-1");

        final Scope mockScope = new Scope();
        mockScope.setId("scope-id-1");
        mockScope.setKey("scope");

        final ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setId("consent-id");
        scopeApproval.setClientId("clientId");
        scopeApproval.setScope("scope");
        scopeApproval.setDomain(domainId);


        doReturn(Mono.just(mockDomain)).when(domainService).findById_migrated(domainId);
        doReturn(Mono.just(mockClient)).when(applicationService).findByDomainAndClientId_migrated(domainId, scopeApproval.getClientId());
        doReturn(Mono.just(mockScope)).when(scopeService).findByDomainAndKey_migrated(domainId, scopeApproval.getScope());
        doReturn(Mono.just(scopeApproval)).when(scopeApprovalService).findById_migrated(scopeApproval.getId());

        final Response response = target("domains")
                .path(domainId)
                .path("users")
                .path(mockUser.getId())
                .path("consents")
                .path(scopeApproval.getId())
                .request()
                .get();

        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }

    @Test
    public void shouldGetUserConsent_technicalManagementException() {
        final String domainId = "domain-1";
        doReturn(Mono.error(new TechnicalManagementException("error occurs"))).when(domainService).findById_migrated(domainId);

        final Response response = target("domains")
                .path(domainId)
                .path("users")
                .path("user1")
                .path("consents")
                .path("consent1")
                .request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void shouldRevokeUserConsent() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final User mockUser = new User();
        mockUser.setId("user-id-1");

        doReturn(Mono.just(mockDomain)).when(domainService).findById_migrated(domainId);
        doReturn(Mono.empty()).when(scopeApprovalService).revokeByConsent_migrated(eq(domainId), eq(mockUser.getId()), eq("consent1"), any());

        final Response response = target("domains")
                .path(domainId)
                .path("users")
                .path(mockUser.getId())
                .path("consents")
                .path("consent1")
                .request()
                .delete();

        assertEquals(HttpStatusCode.NO_CONTENT_204, response.getStatus());
    }
}

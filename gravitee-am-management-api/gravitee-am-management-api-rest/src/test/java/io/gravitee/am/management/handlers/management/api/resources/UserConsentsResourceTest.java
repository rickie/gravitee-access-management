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
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserConsentsResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetUserConsents() {
        String domainId = "domain-1";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        User mockUser = new User();
        mockUser.setId("user-id-1");

        Application mockClient = new Application();
        mockClient.setId("client-id-1");

        Scope mockScope = new Scope();
        mockScope.setId("scope-id-1");
        mockScope.setKey("scope");

        ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setUserId("user-id-1");
        scopeApproval.setClientId("clientId");
        scopeApproval.setScope("scope");
        scopeApproval.setDomain(domainId);

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Maybe.just(mockClient))
                .when(applicationService)
                .findByDomainAndClientId(domainId, scopeApproval.getClientId());
        doReturn(Maybe.just(mockScope))
                .when(scopeService)
                .findByDomainAndKey(domainId, scopeApproval.getScope());
        doReturn(Flowable.just(scopeApproval))
                .when(scopeApprovalService)
                .findByDomainAndUser(domainId, mockUser.getId());

        Response response =
                target("domains")
                        .path(domainId)
                        .path("users")
                        .path(mockUser.getId())
                        .path("consents")
                        .request()
                        .get();

        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }

    @Test
    public void shouldGetUserConsents_technicalManagementException() {
        String domainId = "domain-1";
        doReturn(Maybe.error(new TechnicalManagementException("error occurs")))
                .when(domainService)
                .findById(domainId);

        Response response =
                target("domains")
                        .path(domainId)
                        .path("users")
                        .path("user1")
                        .path("consents")
                        .request()
                        .get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void shouldRevokeUserConsents() {
        String domainId = "domain-1";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        User mockUser = new User();
        mockUser.setId("user-id-1");

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Completable.complete())
                .when(scopeApprovalService)
                .revokeByUser(eq(domainId), eq(mockUser.getId()), any());

        Response response =
                target("domains")
                        .path(domainId)
                        .path("users")
                        .path(mockUser.getId())
                        .path("consents")
                        .request()
                        .delete();

        assertEquals(HttpStatusCode.NO_CONTENT_204, response.getStatus());
    }
}

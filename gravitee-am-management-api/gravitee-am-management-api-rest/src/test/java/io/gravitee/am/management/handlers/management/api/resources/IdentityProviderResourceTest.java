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
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Maybe;

import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class IdentityProviderResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetIdentityProvider() {
        String domainId = "domain-id";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        String identityProviderId = "identityProvider-id";
        IdentityProvider mockIdentityProvider = new IdentityProvider();
        mockIdentityProvider.setId(identityProviderId);
        mockIdentityProvider.setName("identityProvider-name");
        mockIdentityProvider.setReferenceType(ReferenceType.DOMAIN);
        mockIdentityProvider.setReferenceId(domainId);

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Maybe.just(mockIdentityProvider))
                .when(identityProviderService)
                .findById(identityProviderId);

        Response response =
                target("domains")
                        .path(domainId)
                        .path("identities")
                        .path(identityProviderId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        IdentityProvider identityProvider = readEntity(response, IdentityProvider.class);
        assertEquals(domainId, identityProvider.getReferenceId());
        assertEquals(identityProviderId, identityProvider.getId());
    }

    @Test
    public void shouldGetIdentityProvider_notFound() {
        String domainId = "domain-id";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        String identityProviderId = "identityProvider-id";

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Maybe.empty()).when(identityProviderService).findById(identityProviderId);

        Response response =
                target("domains")
                        .path(domainId)
                        .path("identities")
                        .path(identityProviderId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void shouldGetIdentityProvider_domainNotFound() {
        String domainId = "domain-id";
        String identityProviderId = "identityProvider-id";

        doReturn(Maybe.empty()).when(domainService).findById(domainId);

        Response response =
                target("domains")
                        .path(domainId)
                        .path("identities")
                        .path(identityProviderId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void shouldGetIdentityProvider_wrongDomain() {
        String domainId = "domain-id";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        String identityProviderId = "identityProvider-id";
        IdentityProvider mockIdentityProvider = new IdentityProvider();
        mockIdentityProvider.setId(identityProviderId);
        mockIdentityProvider.setName("identityProvider-name");
        mockIdentityProvider.setReferenceType(ReferenceType.DOMAIN);
        mockIdentityProvider.setReferenceId("wrong-domain");

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Maybe.just(mockIdentityProvider))
                .when(identityProviderService)
                .findById(identityProviderId);

        Response response =
                target("domains")
                        .path(domainId)
                        .path("identities")
                        .path(identityProviderId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.BAD_REQUEST_400, response.getStatus());
    }
}

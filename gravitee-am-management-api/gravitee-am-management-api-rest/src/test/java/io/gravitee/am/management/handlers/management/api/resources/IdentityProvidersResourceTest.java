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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewIdentityProvider;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class IdentityProvidersResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetIdentityProviders() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final IdentityProvider mockIdentityProvider = new IdentityProvider();
        mockIdentityProvider.setId("identityProvider-1-id");
        mockIdentityProvider.setName("identityProvider-1-name");
        mockIdentityProvider.setReferenceType(ReferenceType.DOMAIN);
        mockIdentityProvider.setReferenceId(domainId);

        final IdentityProvider mockIdentityProvider2 = new IdentityProvider();
        mockIdentityProvider2.setId("identityProvider-2-id");
        mockIdentityProvider2.setName("identityProvider-2-name");
        mockIdentityProvider2.setReferenceType(ReferenceType.DOMAIN);
        mockIdentityProvider2.setReferenceId(domainId);

        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(mockIdentityProvider, mockIdentityProvider2)))).when(identityProviderService).findByDomain_migrated(domainId);

        final Response response = target("domains").path(domainId).path("identities").request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final List<IdentityProvider> responseEntity = readEntity(response, List.class);
        assertTrue(responseEntity.size() == 2);
    }

    @Test
    public void shouldGetIdentityProviders_technicalManagementException() {
        final String domainId = "domain-1";
        doReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("error occurs"))))).when(identityProviderService).findByDomain_migrated(domainId);

        final Response response = target("domains").path(domainId).path("identities").request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void shouldCreate() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        NewIdentityProvider newIdentityProvider = new NewIdentityProvider();
        newIdentityProvider.setName("extensionGrant-name");
        newIdentityProvider.setType("extensionGrant-type");
        newIdentityProvider.setConfiguration("extensionGrant-configuration");
        newIdentityProvider.setDomainWhitelist(List.of());

        IdentityProvider identityProvider = new IdentityProvider();
        identityProvider.setId("identityProvider-id");
        identityProvider.setName("identityProvider-name");
        identityProvider.setDomainWhitelist(List.of());

        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(identityProvider)))).when(identityProviderService).create_migrated(eq(domainId), any(), any());

        final Response response = target("domains")
                .path(domainId)
                .path("identities")
                .request().post(Entity.json(newIdentityProvider));
        assertEquals(HttpStatusCode.CREATED_201, response.getStatus());
    }
}

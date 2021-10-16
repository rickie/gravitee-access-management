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
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.application.ApplicationType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewApplication;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ApplicationsResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetApps() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final Application mockClient = new Application();
        mockClient.setId("client-1-id");
        mockClient.setName("client-1-name");
        mockClient.setDomain(domainId);
        mockClient.setUpdatedAt(new Date());

        final Application mockClient2 = new Application();
        mockClient2.setId("client-2-id");
        mockClient2.setName("client-2-name");
        mockClient2.setDomain(domainId);
        mockClient2.setUpdatedAt(new Date());

        final Page<Application> applicationPage = new Page(new HashSet<>(Arrays.asList(mockClient, mockClient2)), 0, 2);

        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(applicationPage)))).when(applicationService).findByDomain_migrated(domainId, 0, Integer.MAX_VALUE);

        final Response response = target("domains").path(domainId).path("applications").request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final Map responseEntity = readEntity(response, Map.class);
        assertTrue(((List)responseEntity.get("data")).size() == 2);
    }

    @Test
    public void shouldGetApplications_technicalManagementException() {
        final String domainId = "domain-1";
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("error occurs"))))).when(applicationService).findByDomain_migrated(domainId);

        final Response response = target("domains").path(domainId).path("applications").request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }


    @Test
    public void shouldCreate() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        NewApplication newApplication = new NewApplication();
        newApplication.setName("name");
        newApplication.setType(ApplicationType.SERVICE);

        Application application = new Application();
        application.setId("app-id");
        application.setName("name");
        application.setType(ApplicationType.SERVICE);

        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(application)))).when(applicationService).create_migrated(eq(domainId), any(NewApplication.class), any());

        final Response response = target("domains")
                .path(domainId)
                .path("applications")
                .request().post(Entity.json(newApplication));
        assertEquals(HttpStatusCode.CREATED_201, response.getStatus());
    }
}

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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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

import org.junit.Test;

import java.util.*;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ApplicationsResourceTest extends JerseySpringTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    @Test
    public void shouldGetApps() {
        String domainId = "domain-1";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        Application mockClient = new Application();
        mockClient.setId("client-1-id");
        mockClient.setName("client-1-name");
        mockClient.setDescription("a description client 1");
        mockClient.setDomain(domainId);
        mockClient.setUpdatedAt(new Date());

        Application mockClient2 = new Application();
        mockClient2.setId("client-2-id");
        mockClient2.setName("client-2-name");
        mockClient2.setDomain(domainId);
        mockClient2.setDescription("a description client 2");
        mockClient2.setUpdatedAt(new Date());

        Page<Application> applicationPage =
                new Page(new HashSet<>(Arrays.asList(mockClient, mockClient2)), 0, 2);

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Single.just(applicationPage))
                .when(applicationService)
                .findByDomain(domainId, 0, Integer.MAX_VALUE);

        Response response = target("domains").path(domainId).path("applications").request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        Map responseEntity = readEntity(response, Map.class);
        assertTrue(((List) responseEntity.get("data")).size() == 2);
    }

    @Test
    public void shouldGetApplications_technicalManagementException() {
        String domainId = "domain-1";
        doReturn(Single.error(new TechnicalManagementException("error occurs")))
                .when(applicationService)
                .findByDomain(domainId);

        Response response = target("domains").path(domainId).path("applications").request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void shouldCreate() throws JsonProcessingException {
        String domainId = "domain-1";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        NewApplication newApplication = new NewApplication();
        newApplication.setName("name");
        newApplication.setType(ApplicationType.SERVICE);

        Application application = new Application();
        application.setId("app-id");
        application.setName("name");
        application.setDescription("a description");
        application.setType(ApplicationType.SERVICE);

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Single.just(application))
                .when(applicationService)
                .create(eq(domainId), any(NewApplication.class), any());

        Response response =
                target("domains")
                        .path(domainId)
                        .path("applications")
                        .request()
                        .post(Entity.json(newApplication));

        Application result =
                objectMapper.readValue(response.readEntity(String.class), Application.class);
        assertEquals(result.getName(), application.getName());
        assertEquals(result.getType(), application.getType());
        assertEquals(result.getId(), application.getId());
        assertEquals(result.getDescription(), application.getDescription());

        assertEquals(HttpStatusCode.CREATED_201, response.getStatus());
    }
}

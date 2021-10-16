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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.model.Entrypoint;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewEntrypoint;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.junit.Test;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EntrypointsResourceTest extends JerseySpringTest {

    public static final String ORGANIZATION_ID = "orga-1";
    public static final String ENTRYPOINT_ID = "entrypoint-1";

    @Test
    public void shouldGetEntrypoints() {
        final Entrypoint entrypoint = new Entrypoint();
        entrypoint.setId(ENTRYPOINT_ID);
        entrypoint.setName("entrypoint-1-name");
        entrypoint.setOrganizationId(ORGANIZATION_ID);

        final Entrypoint entrypoint2 = new Entrypoint();
        entrypoint2.setId("entrypoint#2");
        entrypoint2.setName("entrypoint-2-name");
        entrypoint2.setOrganizationId(ORGANIZATION_ID);

        doReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.just(entrypoint, entrypoint2)))).when(entrypointService).findAll_migrated(ORGANIZATION_ID);

        final Response response = target("organizations")
                .path(ORGANIZATION_ID)
                .path("entrypoints").request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final List<Entrypoint> responseEntity = readEntity(response, List.class);
        assertEquals(2, responseEntity.size());
    }

    @Test
    public void shouldGetEntrypoints_technicalManagementException() {
        doReturn(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("error occurs"))))).when(entrypointService).findAll_migrated(anyString());

        final Response response = target("organizations")
                .path(ORGANIZATION_ID)
                .path("entrypoints").request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void shouldCreate() {
        NewEntrypoint newEntrypoint = new NewEntrypoint();
        newEntrypoint.setName("name");
        newEntrypoint.setUrl("https://auth.company.com");
        newEntrypoint.setTags(Collections.emptyList());

        Entrypoint entrypoint = new Entrypoint();
        entrypoint.setId("entrypoint-1");
        entrypoint.setName("name");

        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(entrypoint)))).when(entrypointService).create_migrated(eq(ORGANIZATION_ID), any(NewEntrypoint.class), any(User.class));

        WebTarget path = target("organizations")
                .path(ORGANIZATION_ID)
                .path("entrypoints");

        final Response response = post(path, newEntrypoint);
        assertEquals(HttpStatusCode.CREATED_201, response.getStatus());
        assertEquals(path.getUri().toString() + "/"+ entrypoint.getId(), response.getHeaderString(HttpHeaders.LOCATION));
    }
}

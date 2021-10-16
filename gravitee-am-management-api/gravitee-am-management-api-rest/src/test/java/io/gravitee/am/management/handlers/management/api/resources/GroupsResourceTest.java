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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Group;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewGroup;
import io.gravitee.common.http.HttpStatusCode;


import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.junit.Test;

import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class GroupsResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetGroups() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final Group mockGroup = new Group();
        mockGroup.setId("group-id-1");
        mockGroup.setReferenceId(domainId);

        final Group mockGroup2 = new Group();
        mockGroup2.setId("group-id-2");
        mockGroup2.setReferenceId(domainId);

        final List<Group> groups = Arrays.asList(mockGroup, mockGroup2);
        final Page<User> pagedUsers = new Page(groups, 0, 2);

        doReturn(Mono.just(mockDomain)).when(domainService).findById_migrated(domainId);
        doReturn(Mono.just(pagedUsers)).when(groupService).findByDomain_migrated(domainId, 0, 10);

        final Response response = target("domains")
                .path(domainId)
                .path("groups")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .request()
                .get();

        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final Map entity = readEntity(response, Map.class);
        assertTrue(((List)entity.get("data")).size() == 2);
    }

    @Test
    public void shouldGetGroups_technicalManagementException() {
        final String domainId = "domain-1";
        doReturn(Mono.error(new TechnicalManagementException("error occurs"))).when(domainService).findById_migrated(domainId);

        final Response response = target("domains").path(domainId).path("groups").request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void shouldCreate() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        NewGroup newGroup = new NewGroup();
        newGroup.setName("name");

        Group group = new Group();
        group.setId("group-id");

        doReturn(Mono.just(mockDomain)).when(domainService).findById_migrated(domainId);
        doReturn(Mono.just(group)).when(groupService).create_migrated(any(), any(), any());

        final Response response = target("domains")
                .path(domainId)
                .path("groups")
                .request().post(Entity.json(newGroup));
        assertEquals(HttpStatusCode.CREATED_201, response.getStatus());
    }
}

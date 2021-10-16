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
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.common.http.HttpStatusCode;

import javax.ws.rs.core.Response;
import org.junit.Test;

import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetUser() {
        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final String userId = "user-id";
        final User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("user-username");
        mockUser.setReferenceType(ReferenceType.DOMAIN);
        mockUser.setReferenceId(domainId);

        doReturn(Mono.just(mockDomain)).when(domainService).findById_migrated(domainId);
        doReturn(Mono.just(mockUser)).when(userService).findById_migrated(userId);

        final Response response = target("domains").path(domainId).path("users").path(userId).request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final User user = readEntity(response, User.class);
        assertEquals(domainId, user.getReferenceId());
        assertEquals("user-username", user.getUsername());
    }

    @Test
    public void shouldGetUser_notFound() {
        final String domainId = "domain-id";
        doReturn(Mono.empty()).when(domainService).findById_migrated(domainId);

        final Response response = target("domains/" + domainId).request().get();
        assertEquals(HttpStatusCode.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void shouldGetUser_technicalManagementException() {
        final String domainId = "domain-id";
        doReturn(Mono.error(new TechnicalManagementException("error occurs"))).when(domainService).findById_migrated(domainId);

        final Response response = target("domains").path(domainId).request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }
}

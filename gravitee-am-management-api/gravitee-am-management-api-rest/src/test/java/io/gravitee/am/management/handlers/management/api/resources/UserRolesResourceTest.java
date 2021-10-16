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
import io.gravitee.am.model.User;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collections;
import javax.ws.rs.core.Response;
import org.junit.Test;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserRolesResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetUserRoles() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final User mockUser = new User();
        mockUser.setId("user-id-1");
        mockUser.setRoles(Collections.singletonList("role-1"));

        final ScopeApproval scopeApproval = new ScopeApproval();
        scopeApproval.setClientId("clientId");
        scopeApproval.setScope("scope");
        scopeApproval.setDomain(domainId);


        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockUser)))).when(userService).findById_migrated(mockUser.getId());
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Collections.singleton("role-1"))))).when(roleService).findByIdIn_migrated(mockUser.getRoles());

        final Response response = target("domains")
                .path(domainId)
                .path("users")
                .path(mockUser.getId())
                .path("roles")
                .request()
                .get();

        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }

    @Test
    public void shouldGetUserRoles_technicalManagementException() {
        final String domainId = "domain-1";
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException("error occurs"))))).when(domainService).findById_migrated(domainId);

        final Response response = target("domains")
                .path(domainId)
                .path("users")
                .path("user1")
                .path("roles")
                .request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }
}

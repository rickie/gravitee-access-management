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
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
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
public class UserRoleResourceTest extends JerseySpringTest {

    @Test
    public void shouldRevokeUserRole() {
        final String domainId = "domain-1";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final User mockUser = new User();
        mockUser.setId("user-id-1");

        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(mockUser)))).when(userService).revokeRoles_migrated(eq(ReferenceType.DOMAIN), eq(domainId), eq(mockUser.getId()), eq(Collections.singletonList("role-1")), any());

        final Response response = target("domains")
                .path(domainId)
                .path("users")
                .path(mockUser.getId())
                .path("roles")
                .path("role-1")
                .request()
                .delete();

        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }
}

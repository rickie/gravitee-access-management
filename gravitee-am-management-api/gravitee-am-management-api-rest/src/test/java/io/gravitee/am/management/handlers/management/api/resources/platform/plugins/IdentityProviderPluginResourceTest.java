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
package io.gravitee.am.management.handlers.management.api.resources.platform.plugins;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.plugin.IdentityProviderPlugin;
import io.gravitee.common.http.HttpStatusCode;

import javax.ws.rs.core.Response;
import org.junit.Test;

import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class IdentityProviderPluginResourceTest extends JerseySpringTest {

    @Test
    public void shouldGet() {
        final String identityProviderPluginId = "identityProvider-plugin-id";
        final IdentityProviderPlugin identityProviderPlugin = new IdentityProviderPlugin();
        identityProviderPlugin.setId(identityProviderPluginId);
        identityProviderPlugin.setName("identityProvider-plugin-name");

        doReturn(Mono.just(identityProviderPlugin)).when(identityProviderPluginService).findById_migrated(identityProviderPluginId);

        final Response response = target("platform")
                .path("plugins")
                .path("identities")
                .path(identityProviderPluginId)
                .request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }

    @Test
    public void shouldGet_technicalManagementException() {
        final String identityProviderPluginId = "identityProvider-plugin-id";
        doReturn(Mono.error(new TechnicalManagementException("Error occurs"))).when(identityProviderPluginService).findById_migrated(identityProviderPluginId);

        final Response response = target("platform")
                .path("plugins")
                .path("identities")
                .path(identityProviderPluginId)
                .request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void shouldGetSchema() {
        final String identityProviderPluginId = "identityProvider-plugin-id";
        final IdentityProviderPlugin identityProviderPlugin = new IdentityProviderPlugin();
        identityProviderPlugin.setId(identityProviderPluginId);
        identityProviderPlugin.setName("identityProvider-plugin-name");

        final String schema = "identityProvider-plugin-schema";

        doReturn(Mono.just(identityProviderPlugin)).when(identityProviderPluginService).findById_migrated(identityProviderPluginId);
        doReturn(Mono.just(schema)).when(identityProviderPluginService).getSchema_migrated(identityProviderPluginId);

        final Response response = target("platform")
                .path("plugins")
                .path("identities")
                .path(identityProviderPluginId)
                .path("schema")
                .request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final String responseEntity = readEntity(response, String.class);
        assertEquals(schema, responseEntity);
    }

    @Test
    public void shouldGetSchema_technicalManagementException() {
        final String identityProviderPluginId = "identityProvider-plugin-id";
        doReturn(Mono.error(new TechnicalManagementException("Error occurs"))).when(identityProviderPluginService).findById_migrated(identityProviderPluginId);

        final Response response = target("platform")
                .path("plugins")
                .path("identities")
                .path(identityProviderPluginId)
                .path("schema")
                .request().get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }
}

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
package io.gravitee.am.management.handlers.management.api.resources.platform.plugins;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.plugin.ExtensionGrantPlugin;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Maybe;

import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ExtensionGrantPluginResourceTest extends JerseySpringTest {

    @Test
    public void shouldGet() {
        String extensionGrantPluginId = "extensionGrant-plugin-id";
        ExtensionGrantPlugin extensionGrantPlugin = new ExtensionGrantPlugin();
        extensionGrantPlugin.setId(extensionGrantPluginId);
        extensionGrantPlugin.setName("extensionGrant-plugin-name");

        doReturn(Maybe.just(extensionGrantPlugin))
                .when(extensionGrantPluginService)
                .findById(extensionGrantPluginId);

        Response response =
                target("platform")
                        .path("plugins")
                        .path("extensionGrants")
                        .path(extensionGrantPluginId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }

    @Test
    public void shouldGet_technicalManagementException() {
        String extensionGrantPluginId = "extensionGrant-plugin-id";
        doReturn(Maybe.error(new TechnicalManagementException("Error occurs")))
                .when(extensionGrantPluginService)
                .findById(extensionGrantPluginId);

        Response response =
                target("platform")
                        .path("plugins")
                        .path("extensionGrants")
                        .path(extensionGrantPluginId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void shouldGetSchema() {
        String extensionGrantPluginId = "extensionGrant-plugin-id";
        ExtensionGrantPlugin extensionGrantPlugin = new ExtensionGrantPlugin();
        extensionGrantPlugin.setId(extensionGrantPluginId);
        extensionGrantPlugin.setName("extensionGrant-plugin-name");

        String schema = "extensionGrant-plugin-schema";

        doReturn(Maybe.just(extensionGrantPlugin))
                .when(extensionGrantPluginService)
                .findById(extensionGrantPluginId);
        doReturn(Maybe.just(schema))
                .when(extensionGrantPluginService)
                .getSchema(extensionGrantPluginId);

        Response response =
                target("platform")
                        .path("plugins")
                        .path("extensionGrants")
                        .path(extensionGrantPluginId)
                        .path("schema")
                        .request()
                        .get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        String responseEntity = readEntity(response, String.class);
        assertEquals(schema, responseEntity);
    }

    @Test
    public void shouldGetSchema_technicalManagementException() {
        String extensionGrantPluginId = "extensionGrant-plugin-id";
        doReturn(Maybe.error(new TechnicalManagementException("Error occurs")))
                .when(extensionGrantPluginService)
                .findById(extensionGrantPluginId);

        Response response =
                target("platform")
                        .path("plugins")
                        .path("extensionGrants")
                        .path(extensionGrantPluginId)
                        .path("schema")
                        .request()
                        .get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }
}

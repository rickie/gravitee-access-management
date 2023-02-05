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
import io.gravitee.am.service.model.plugin.CertificatePlugin;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Maybe;

import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CertificatePluginResourceTest extends JerseySpringTest {

    @Test
    public void shouldGet() {
        String certificatePluginId = "certificate-plugin-id";
        CertificatePlugin certificatePlugin = new CertificatePlugin();
        certificatePlugin.setId(certificatePluginId);
        certificatePlugin.setName("certificate-plugin-name");

        doReturn(Maybe.just(certificatePlugin))
                .when(certificatePluginService)
                .findById(certificatePluginId);

        Response response =
                target("platform")
                        .path("plugins")
                        .path("certificates")
                        .path(certificatePluginId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }

    @Test
    public void shouldGet_technicalManagementException() {
        String certificatePluginId = "certificate-plugin-id";
        doReturn(Maybe.error(new TechnicalManagementException("Error occurs")))
                .when(certificatePluginService)
                .findById(certificatePluginId);

        Response response =
                target("platform")
                        .path("plugins")
                        .path("certificates")
                        .path(certificatePluginId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    public void shouldGetSchema() {
        String certificatePluginId = "certificate-plugin-id";
        CertificatePlugin certificatePlugin = new CertificatePlugin();
        certificatePlugin.setId(certificatePluginId);
        certificatePlugin.setName("certificate-plugin-name");

        String schema = "certificate-plugin-schema";

        doReturn(Maybe.just(certificatePlugin))
                .when(certificatePluginService)
                .findById(certificatePluginId);
        doReturn(Maybe.just(schema)).when(certificatePluginService).getSchema(certificatePluginId);

        Response response =
                target("platform")
                        .path("plugins")
                        .path("certificates")
                        .path(certificatePluginId)
                        .path("schema")
                        .request()
                        .get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        String responseEntity = readEntity(response, String.class);
        assertEquals(schema, responseEntity);
    }

    @Test
    public void shouldGetSchema_technicalManagementException() {
        String certificatePluginId = "certificate-plugin-id";
        doReturn(Maybe.error(new TechnicalManagementException("Error occurs")))
                .when(certificatePluginService)
                .findById(certificatePluginId);

        Response response =
                target("platform")
                        .path("plugins")
                        .path("certificates")
                        .path(certificatePluginId)
                        .path("schema")
                        .request()
                        .get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }
}

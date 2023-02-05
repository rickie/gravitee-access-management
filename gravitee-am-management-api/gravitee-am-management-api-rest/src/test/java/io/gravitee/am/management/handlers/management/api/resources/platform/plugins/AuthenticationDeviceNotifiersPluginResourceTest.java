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
import io.gravitee.am.service.model.plugin.AuthenticationDeviceNotifierPlugin;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Single;

import org.junit.Test;

import java.util.Collections;

import javax.ws.rs.core.Response;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AuthenticationDeviceNotifiersPluginResourceTest extends JerseySpringTest {

    @Test
    public void shouldList() {
        final AuthenticationDeviceNotifierPlugin plugin = new AuthenticationDeviceNotifierPlugin();
        plugin.setId("plugin-id");
        plugin.setName("plugin-name");
        plugin.setDescription("desc");
        plugin.setVersion("1");

        doReturn(Single.just(Collections.singletonList(plugin)))
                .when(authDeviceNotifierPluginService)
                .findAll(Collections.emptyList());

        final Response response =
                target("platform").path("plugins").path("auth-device-notifiers").request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }
}

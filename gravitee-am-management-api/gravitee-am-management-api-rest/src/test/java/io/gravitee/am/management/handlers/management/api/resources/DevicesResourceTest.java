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
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.model.Device;
import io.gravitee.am.model.Domain;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

import org.junit.Test;

import java.util.List;

import javax.ws.rs.core.Response;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DevicesResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetDevices() {
        String userId = "domain-1";
        String domainId = "user-1";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);
        var device = new Device().setDeviceId("deviceID");
        var device2 = new Device().setDeviceId("deviceID2");
        var device3 = new Device().setDeviceId("deviceID3");

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);

        doReturn(Flowable.just(List.of(device, device2, device3)))
                .when(deviceService)
                .findByDomainAndUser(domainId, userId);
        doReturn(Flowable.empty()).when(deviceService).findByDomainAndUser(domainId, "wrong user");

        Response response =
                target("domains")
                        .path(domainId)
                        .path("users")
                        .path(userId)
                        .path("devices")
                        .request()
                        .get();

        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }
}

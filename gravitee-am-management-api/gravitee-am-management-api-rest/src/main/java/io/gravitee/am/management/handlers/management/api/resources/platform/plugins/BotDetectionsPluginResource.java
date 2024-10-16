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

import io.gravitee.am.management.service.BotDetectionPluginService;
import io.gravitee.am.service.model.plugin.BotDetectionPlugin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Comparator;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Api(tags = {"Plugin", "Bot Detection"})
public class BotDetectionsPluginResource {

    @Context private ResourceContext resourceContext;

    @Inject private BotDetectionPluginService pluginService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "List bot detection plugins",
            notes = "There is no particular permission needed. User must be authenticated.")
    public void list(@Suspended final AsyncResponse response) {

        pluginService
                .findAll()
                .map(
                        plugins ->
                                plugins.stream()
                                        .sorted(Comparator.comparing(BotDetectionPlugin::getName))
                                        .collect(Collectors.toList()))
                .subscribe(response::resume, response::resume);
    }

    @Path("{botDetection}")
    public BotDetectionPluginResource getBotDetectionPluginResource() {
        return resourceContext.getResource(BotDetectionPluginResource.class);
    }
}

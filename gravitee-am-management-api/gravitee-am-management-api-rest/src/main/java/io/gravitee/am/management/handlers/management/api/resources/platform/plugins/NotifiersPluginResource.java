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

import io.gravitee.am.management.handlers.management.api.model.ErrorEntity;
import io.gravitee.am.management.service.impl.plugins.NotifierPluginService;
import io.gravitee.am.service.model.plugin.AbstractPlugin;
import io.gravitee.am.service.model.plugin.NotifierPlugin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Api(tags = {"Plugin", "Notifier"})
public class NotifiersPluginResource {

    @Context
    private ResourceContext resourceContext;

    @Inject
    private NotifierPluginService notifierPluginService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all available notifier plugins",
            notes = "There is no particular permission needed. User must be authenticated.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Notifier plugin list", response = NotifierPlugin.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(@QueryParam("expand") List<String> expand, @Suspended final AsyncResponse response) {

        RxJava2Adapter.monoToSingle(notifierPluginService.findAll_migrated(expand.toArray(new String[0])).sort(Comparator.comparing(AbstractPlugin::getName)).collectList())
                .subscribe(response::resume, response::resume);
    }

    @Path("{notifierId}")
    public NotifierPluginResource getNotifierPluginResource() {
        return resourceContext.getResource(NotifierPluginResource.class);
    }
}
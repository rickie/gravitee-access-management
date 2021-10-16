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

import io.gravitee.am.management.service.ReporterPluginService;
import io.gravitee.am.management.service.exception.ReporterPluginNotFoundException;
import io.gravitee.am.management.service.exception.ReporterPluginSchemaNotFoundException;
import io.gravitee.common.http.MediaType;
import io.reactivex.Maybe;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Api(tags = {"Plugin", "Reporter"})
public class ReporterPluginResource {

    @Context
    private ResourceContext resourceContext;

    @Inject
    private ReporterPluginService reporterPluginService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a reporter plugin",
            notes = "There is no particular permission needed. User must be authenticated.")
    public void get(
            @PathParam("reporter") String reporterId,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(reporterPluginService.findById_migrated(reporterId))).switchIfEmpty(Mono.error(new ReporterPluginNotFoundException(reporterId))).map(RxJavaReactorMigrationUtil.toJdkFunction(reporterPlugin -> Response.ok(reporterPlugin).build())))
                .subscribe(response::resume, response::resume);
    }

    @GET
    @Path("schema")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a reporter plugin's schema")
    public void getSchema(
            @PathParam("reporter") String reporterId,
            @Suspended final AsyncResponse response) {

        // Check that the identity provider exists
        RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(reporterPluginService.findById_migrated(reporterId))).switchIfEmpty(Mono.error(new ReporterPluginNotFoundException(reporterId))).flatMap(z->RxJava2Adapter.monoToMaybe(reporterPluginService.getSchema_migrated(reporterId)).as(RxJava2Adapter::maybeToMono)).switchIfEmpty(Mono.error(new ReporterPluginSchemaNotFoundException(reporterId))).map(RxJavaReactorMigrationUtil.toJdkFunction(reporterPluginSchema -> Response.ok(reporterPluginSchema).build())))
                .subscribe(response::resume, response::resume);
    }
}
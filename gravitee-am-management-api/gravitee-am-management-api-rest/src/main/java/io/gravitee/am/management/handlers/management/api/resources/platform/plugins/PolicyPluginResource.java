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

import io.gravitee.am.management.service.PolicyPluginService;
import io.gravitee.am.management.service.exception.PolicyPluginNotFoundException;
import io.gravitee.am.management.service.exception.PolicyPluginSchemaNotFoundException;
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
@Api(tags = {"Plugin", "Policy"})
public class PolicyPluginResource {

    @Context
    private ResourceContext resourceContext;

    @Inject
    private PolicyPluginService policyPluginService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a policy plugin",
            notes = "There is no particular permission needed. User must be authenticated.")
    public void get(
            @PathParam("policy") String policyId,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(policyPluginService.findById_migrated(policyId))).switchIfEmpty(Mono.error(new PolicyPluginNotFoundException(policyId))).map(RxJavaReactorMigrationUtil.toJdkFunction(policyPlugin -> Response.ok(policyPlugin).build())))
                .subscribe(response::resume, response::resume);
    }

    @GET
    @Path("schema")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a policy plugin's schema")
    public void getSchema(
            @PathParam("policy") String policyId,
            @Suspended final AsyncResponse response) {

        // Check that the policy exists
        RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(policyPluginService.findById_migrated(policyId))).switchIfEmpty(Mono.error(new PolicyPluginNotFoundException(policyId))).flatMap(z->RxJava2Adapter.monoToMaybe(policyPluginService.getSchema_migrated(policyId)).as(RxJava2Adapter::maybeToMono)).switchIfEmpty(Mono.error(new PolicyPluginSchemaNotFoundException(policyId))).map(RxJavaReactorMigrationUtil.toJdkFunction(policyPluginSchema -> Response.ok(policyPluginSchema).build())))
                .subscribe(response::resume, response::resume);
    }

    @GET
    @Path("documentation")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get a policy plugin's documentation")
    public void getDocumentation(
        @PathParam("policy") String policyId,
        @Suspended final AsyncResponse response) {

        // Check that the policy exists
        RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(policyPluginService.findById_migrated(policyId))).switchIfEmpty(Mono.error(new PolicyPluginNotFoundException(policyId))).flatMap(z->RxJava2Adapter.monoToMaybe(policyPluginService.getDocumentation_migrated(policyId)).as(RxJava2Adapter::maybeToMono)).map(RxJavaReactorMigrationUtil.toJdkFunction(policyPluginDocumentation -> Response.ok(policyPluginDocumentation).build())))
            .subscribe(response::resume, response::resume);
    }

}

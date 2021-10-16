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
package io.gravitee.am.management.handlers.management.api.resources.organizations.environments.domains;




import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Form;

import io.gravitee.am.model.Template;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.FormService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.model.NewForm;

import io.gravitee.common.http.MediaType;


import io.swagger.annotations.*;
import java.net.URI;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Api(tags = {"form"})
public class FormsResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private FormService formService;

    @Autowired
    private DomainService domainService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Find a form",
            notes = "User must have the DOMAIN_FORM[READ] permission on the specified domain " +
                    "or DOMAIN_FORM[READ] permission on the specified environment " +
                    "or DOMAIN_FORM[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Form successfully fetched"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void get(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @NotNull @QueryParam("template") Template formTemplate,
            @Suspended final AsyncResponse response) {

        checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_FORM, Acl.READ).then(formService.findByDomainAndTemplate_migrated(domain, formTemplate.template())).map(RxJavaReactorMigrationUtil.toJdkFunction(form -> Response.ok(form).build())).defaultIfEmpty(Response.ok(new Form(false, formTemplate.template())).build()).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a form",
            notes = "User must have the DOMAIN_FORM[CREATE] permission on the specified domain " +
                    "or DOMAIN_FORM[CREATE] permission on the specified environment " +
                    "or DOMAIN_FORM[CREATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Form successfully created"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void create(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @ApiParam(name = "form", required = true)
            @Valid @NotNull final NewForm newForm,
            @Suspended final AsyncResponse response) {

        final User authenticatedUser = getAuthenticatedUser();

        checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_FORM, Acl.CREATE).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domain).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(irrelevant -> RxJava2Adapter.monoToSingle(formService.create_migrated(domain, newForm, authenticatedUser).map(RxJavaReactorMigrationUtil.toJdkFunction(form -> Response
                                        .created(URI.create("/organizations/" + organizationId + "/environments/" + environmentId + "/domains/" + domain + "/forms/" + form.getId()))
                                        .entity(form)
                                        .build())))))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @Path("{form}")
    public FormResource getFormResource() {
        return resourceContext.getResource(FormResource.class);
    }
}

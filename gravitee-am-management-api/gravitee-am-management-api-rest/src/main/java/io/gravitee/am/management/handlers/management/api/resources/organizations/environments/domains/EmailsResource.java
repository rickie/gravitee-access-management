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
import io.gravitee.am.model.Email;
import io.gravitee.am.model.Template;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.EmailTemplateService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.model.NewEmail;
import io.gravitee.common.http.MediaType;
import io.reactivex.Maybe;
import io.reactivex.Single;
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
@Api(tags = {"email"})
public class EmailsResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private DomainService domainService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Find a email",
            notes = "User must have the DOMAIN_EMAIL_TEMPLATE[READ] permission on the specified domain " +
                    "or DOMAIN_EMAIL_TEMPLATE[READ] permission on the specified environment " +
                    "or DOMAIN_EMAIL_TEMPLATE[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Email successfully fetched"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void get(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @NotNull @QueryParam("template") Template emailTemplate,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_EMAIL_TEMPLATE, Acl.READ).then(emailTemplateService.findByDomainAndTemplate_migrated(domain, emailTemplate.template())).map(RxJavaReactorMigrationUtil.toJdkFunction(email -> Response.ok(email).build())).defaultIfEmpty(Response.ok(new Email(false, emailTemplate.template())).build()))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a email",
            notes = "User must have the DOMAIN_EMAIL_TEMPLATE[CREATE] permission on the specified domain " +
                    "or DOMAIN_EMAIL_TEMPLATE[CREATE] permission on the specified environment " +
                    "or DOMAIN_EMAIL_TEMPLATE[CREATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Email successfully created"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void create(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @ApiParam(name = "email", required = true)
            @Valid @NotNull final NewEmail newEmail,
            @Suspended final AsyncResponse response) {

        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_EMAIL_TEMPLATE, Acl.CREATE).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domain).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(__ -> RxJava2Adapter.monoToSingle(emailTemplateService.create_migrated(domain, newEmail, authenticatedUser)))).map(RxJavaReactorMigrationUtil.toJdkFunction(email -> Response
                                .created(URI.create("/organizations/" + organizationId + "/environments/" + environmentId + "/domains/" + domain + "/emails/" + email.getId()))
                                .entity(email)
                                .build()))))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @Path("{email}")
    public EmailResource getEmailResource() {
        return resourceContext.getResource(EmailResource.class);
    }
}

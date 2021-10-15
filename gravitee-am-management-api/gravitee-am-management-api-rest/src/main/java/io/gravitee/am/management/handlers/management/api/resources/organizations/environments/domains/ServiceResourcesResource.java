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
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.model.resource.ServiceResource;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.ServiceResourceService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.model.NewServiceResource;
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
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Api(tags = {"resource"})
public class ServiceResourcesResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private ServiceResourceService resourceService;

    @Autowired
    private DomainService domainService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List registered resources for a security domain",
            notes = "User must have the DOMAIN_RESOURCE[LIST] permission on the specified domain " +
                    "or DOMAIN_RESOURCE[LIST] permission on the specified environment " +
                    "or DOMAIN_RESOURCE[LIST] permission on the specified organization " +
                    "Each returned resource is filtered and contains only basic information such as id, name and resource type.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List registered resources for a security domain", response = ServiceResource.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_FACTOR, Acl.LIST)).then(RxJava2Adapter.flowableToFlux(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.error(new DomainNotFoundException(domain))))))
                        .flatMapPublisher(___ -> resourceService.findByDomain(domain))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::filterFactorInfos)).collectList()))
                .subscribe(response::resume, response::resume);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a resource",
            notes = "User must have the DOMAIN_RESOURCE[CREATE] permission on the specified domain " +
                    "or DOMAIN_RESOURCE[CREATE] permission on the specified environment " +
                    "or DOMAIN_RESOURCE[CREATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Resource successfully created"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void create(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @ApiParam(name = "resource", required = true) @Valid @NotNull final NewServiceResource newResource,
            @Suspended final AsyncResponse response) {

        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_RESOURCE, Acl.CREATE)).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(__ -> resourceService.create(domain, newResource, authenticatedUser))).map(RxJavaReactorMigrationUtil.toJdkFunction(resource -> Response
                                .created(URI.create("/organizations/" + organizationId + "/environments/" + environmentId + "/domains/" + domain + "/resources/" + resource.getId()))
                                .entity(resource)
                                .build()))))
                .subscribe(response::resume, response::resume);
    }

    @Path("{resource}")
    public ServiceResourceResource getServiceResourceResource() {
        return resourceContext.getResource(ServiceResourceResource.class);
    }

    private ServiceResource filterFactorInfos(ServiceResource resource) {
        ServiceResource filteredResource = new ServiceResource();
        filteredResource.setId(resource.getId());
        filteredResource.setName(resource.getName());
        filteredResource.setType(resource.getType());
        return filteredResource;
    }
}

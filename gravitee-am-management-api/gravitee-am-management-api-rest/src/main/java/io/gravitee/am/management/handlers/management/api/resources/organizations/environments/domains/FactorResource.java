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

import static io.gravitee.am.management.service.permissions.Permissions.of;
import static io.gravitee.am.management.service.permissions.Permissions.or;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Factor;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.FactorService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.exception.FactorNotFoundException;
import io.gravitee.am.service.model.UpdateFactor;
import io.gravitee.common.http.MediaType;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
public class FactorResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private FactorService factorService;

    @Autowired
    private DomainService domainService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a factor",
            notes = "User must have the DOMAIN_FACTOR[READ] permission on the specified domain " +
                    "or DOMAIN_FACTOR[READ] permission on the specified environment " +
                    "or DOMAIN_FACTOR[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Factor successfully fetched", response = Factor.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void get(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("factor") String factor,
            @Suspended final AsyncResponse response) {

        checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_FACTOR, Acl.READ).as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(RxJava2Adapter.maybeToMono(Maybe.wrap(Maybe.error(new DomainNotFoundException(domain))))))).flatMap(z->factorService.findById(factor).as(RxJava2Adapter::maybeToMono)).switchIfEmpty(Mono.error(new FactorNotFoundException(factor))).map(RxJavaReactorMigrationUtil.toJdkFunction(factor1 -> {
                            if (!factor1.getDomain().equalsIgnoreCase(domain)) {
                                throw new BadRequestException("Factor does not belong to domain");
                            }
                            return Response.ok(factor1).build();
                        }))).as(RxJava2Adapter::monoToMaybe)
                .subscribe(response::resume, response::resume);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update a factor",
            notes = "User must have the DOMAIN_FACTOR[UPDATE] permission on the specified domain " +
                    "or DOMAIN_FACTOR[UPDATE] permission on the specified environment " +
                    "or DOMAIN_FACTOR[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Factor successfully updated", response = Factor.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void update(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("factor") String factor,
            @ApiParam(name = "identity", required = true) @Valid @NotNull UpdateFactor updateFactor,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_FACTOR, Acl.UPDATE)).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(__ -> factorService.update(domain, factor, updateFactor, authenticatedUser)))))
                .subscribe(response::resume, response::resume);
    }

    @DELETE
    @ApiOperation(value = "Delete a factor",
            notes = "User must have the DOMAIN_FACTOR[DELETE] permission on the specified domain " +
                    "or DOMAIN_FACTOR[DELETE] permission on the specified environment " +
                    "or DOMAIN_FACTOR[DELETE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Factor successfully deleted"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void delete(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("factor") String factor,
            @Suspended final AsyncResponse response) {

        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_FACTOR, Acl.DELETE)).then(RxJava2Adapter.completableToMono(factorService.delete(domain, factor, authenticatedUser))))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);
    }
}
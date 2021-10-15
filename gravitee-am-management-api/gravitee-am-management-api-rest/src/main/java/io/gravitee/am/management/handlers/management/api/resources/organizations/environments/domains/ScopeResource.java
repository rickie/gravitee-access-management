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
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.ScopeService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.exception.ScopeNotFoundException;
import io.gravitee.am.service.model.PatchScope;
import io.gravitee.am.service.model.UpdateScope;
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
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class ScopeResource extends AbstractResource {

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private DomainService domainService;

    @Context
    private ResourceContext resourceContext;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a scope",
            notes = "User must have the DOMAIN_SCOPE[READ] permission on the specified domain " +
                    "or DOMAIN_SCOPE[READ] permission on the specified environment " +
                    "or DOMAIN_SCOPE[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Scope", response = Scope.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void get(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("scope") String scopeId,
            @Suspended final AsyncResponse response) {

        checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_SCOPE, Acl.READ).as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(RxJava2Adapter.maybeToMono(Maybe.wrap(Maybe.error(new DomainNotFoundException(domain))))).flatMap(z->scopeService.findById(scopeId).as(RxJava2Adapter::maybeToMono)).switchIfEmpty(Mono.error(new ScopeNotFoundException(scopeId))).map(RxJavaReactorMigrationUtil.toJdkFunction(scope -> {
                            if (!scope.getDomain().equalsIgnoreCase(domain)) {
                                throw new BadRequestException("Scope does not belong to domain");
                            }
                            return Response.ok(scope).build();
                        }))).as(RxJava2Adapter::monoToMaybe)
                .subscribe(response::resume, response::resume);
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Patch a scope",
            notes = "User must have the DOMAIN_SCOPE[UPDATE] permission on the specified domain " +
                    "or DOMAIN_SCOPE[UPDATE] permission on the specified environment " +
                    "or DOMAIN_SCOPE[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Scope successfully patched", response = Scope.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void patch(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("scope") String scope,
            @ApiParam(name = "scope", required = true) @Valid @NotNull PatchScope patchScope,
            @Suspended final AsyncResponse response) {

        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_SCOPE, Acl.UPDATE)).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(irrelevant -> scopeService.patch(domain, scope, patchScope, authenticatedUser)))))
                .subscribe(response::resume, response::resume);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update a scope",
            notes = "User must have the DOMAIN_SCOPE[UPDATE] permission on the specified domain " +
                    "or DOMAIN_SCOPE[UPDATE] permission on the specified environment " +
                    "or DOMAIN_SCOPE[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Scope successfully updated", response = Scope.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void update(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("scope") String scope,
            @ApiParam(name = "scope", required = true) @Valid @NotNull UpdateScope updateScope,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_SCOPE, Acl.UPDATE)).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(irrelevant -> scopeService.update(domain, scope, updateScope, authenticatedUser)))))
                .subscribe(response::resume, response::resume);
    }

    @DELETE
    @ApiOperation(value = "Delete a scope",
            notes = "User must have the DOMAIN_SCOPE[DELETE] permission on the specified domain " +
                    "or DOMAIN_SCOPE[DELETE] permission on the specified environment " +
                    "or DOMAIN_SCOPE[DELETE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Scope successfully deleted"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void delete(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("scope") String scope,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_SCOPE, Acl.DELETE)).then(RxJava2Adapter.completableToMono(scopeService.delete(scope, false, authenticatedUser))))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);
    }
}

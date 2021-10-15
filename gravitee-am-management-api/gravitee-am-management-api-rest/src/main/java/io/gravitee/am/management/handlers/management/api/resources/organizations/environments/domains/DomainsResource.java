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
import io.gravitee.am.management.service.IdentityProviderManager;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.ReporterService;
import io.gravitee.am.service.model.NewDomain;
import io.gravitee.common.http.MediaType;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.swagger.annotations.*;
import java.net.URI;
import java.util.stream.Collectors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Api(tags = {"domain"})
public class DomainsResource extends AbstractDomainResource {

    private static final int MAX_DOMAINS_SIZE_PER_PAGE = 50;
    private static final String MAX_DOMAINS_SIZE_PER_PAGE_STRING = "50";

    @Autowired
    private IdentityProviderManager identityProviderManager;

    @Autowired
    private ReporterService reporterService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "List security domains for an environment",
            notes = "List all the security domains accessible to the current user. " +
                    "User must have DOMAIN[LIST] permission on the specified environment or organization " +
                    "AND either DOMAIN[READ] permission on each security domain " +
                    "or DOMAIN[READ] permission on the specified environment " +
                    "or DOMAIN[READ] permission on the specified organization." +
                    "Each returned domain is filtered and contains only basic information such as id, name and description and isEnabled.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List accessible security domains for current user", response = Domain.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue(MAX_DOMAINS_SIZE_PER_PAGE_STRING) int size,
            @QueryParam("q") String query,
            @Suspended final AsyncResponse response) {

        User authenticatedUser = getAuthenticatedUser();
        RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, Permission.DOMAIN, Acl.LIST)).thenMany(query != null ? domainService.search(organizationId, environmentId, query) : domainService.findAllByEnvironment(organizationId, environmentId)))).flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<Domain, MaybeSource<Domain>>toJdkFunction(domain -> RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(hasPermission(authenticatedUser,
                        or(of(ReferenceType.DOMAIN, domain.getId(), Permission.DOMAIN, Acl.READ),
                                of(ReferenceType.ENVIRONMENT, environmentId, Permission.DOMAIN, Acl.READ),
                                of(ReferenceType.ORGANIZATION, organizationId, Permission.DOMAIN, Acl.READ)))
                        .filter(Boolean::booleanValue)).map(RxJavaReactorMigrationUtil.toJdkFunction(permit -> domain)))).apply(e)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::filterDomainInfos)).sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName())).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(domains -> new Page<Domain>(domains.stream().skip((long) page * size).limit(size).collect(Collectors.toList()), page, domains.size()))))
                .subscribe(response::resume, response::resume);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a security domain.",
            notes = "Create a security domain. " +
                    "User must have DOMAIN[CREATE] permission on the specified environment " +
                    "or DOMAIN[CREATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Domain successfully created"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void create(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @ApiParam(name = "domain", required = true)
            @Valid @NotNull final NewDomain newDomain,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, Permission.DOMAIN, Acl.CREATE)).then(RxJava2Adapter.singleToMono(domainService.create(organizationId, environmentId, newDomain, authenticatedUser)).flatMap(domain->RxJava2Adapter.singleToMono(identityProviderManager.create(domain.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.IdentityProvider __)->domain))).flatMap(domain->RxJava2Adapter.singleToMono(reporterService.createDefault(domain.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.Reporter __)->domain)))))
                .subscribe(domain -> response.resume(Response.created(URI.create("/organizations/" + organizationId + "/environments/" + environmentId + "/domains/" + domain.getId()))
                        .entity(domain).build()), response::resume);
    }

    @GET
    @Path("_hrid/{hrid}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a security domain by hrid",
            notes = "User must have the DOMAIN[READ] permission on the specified domain, environment or organization. " +
                    "Domain will be filtered according to permissions (READ on DOMAIN_USER_ACCOUNT, DOMAIN_IDENTITY_PROVIDER, DOMAIN_FORM, DOMAIN_LOGIN_SETTINGS, " +
                    "DOMAIN_DCR, DOMAIN_SCIM, DOMAIN_SETTINGS)")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Domain", response = Domain.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void get(@PathParam("organizationId") String organizationId,
                    @PathParam("environmentId") String environmentId,
                    @PathParam("hrid") String hrid,
                    @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(domainService.findByHrid(environmentId, hrid)).flatMap(domain->RxJava2Adapter.completableToMono(checkAnyPermission(authenticatedUser, organizationId, environmentId, domain.getId(), Permission.DOMAIN, Acl.READ)).then(Mono.defer(()->RxJava2Adapter.singleToMono(findAllPermissions(authenticatedUser, organizationId, environmentId, domain.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.Map<io.gravitee.am.model.ReferenceType, java.util.Map<io.gravitee.am.model.permissions.Permission, java.util.Set<io.gravitee.am.model.Acl>>> userPermissions)->filterDomainInfos(domain, userPermissions))))))).subscribe(response::resume, response::resume);
    }


    @Path("{domain}")
    public DomainResource getDomainResource() {
        return resourceContext.getResource(DomainResource.class);
    }
}

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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.model.NewRole;
import io.gravitee.common.http.MediaType;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.swagger.annotations.*;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
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
@Api(tags = {"role"})
public class RolesResource extends AbstractResource {
    private static final String MAX_ROLES_SIZE_PER_PAGE_STRING = "50";
    private static final int MAX_ROLES_SIZE_PER_PAGE = 50;

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private RoleService roleService;

    @Autowired
    private DomainService domainService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List registered roles for a security domain",
            notes = "User must have the DOMAIN_ROLE[LIST] permission on the specified domain " +
                    "or DOMAIN_ROLE[LIST] permission on the specified environment " +
                    "or DOMAIN_ROLE[LIST] permission on the specified organization. " +
                    "Each returned role is filtered and contains only basic information such as id and name.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List registered roles for a security domain", response = Role.class, responseContainer = "Set"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue(MAX_ROLES_SIZE_PER_PAGE_STRING) int size,
            @QueryParam("q") String query,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_ROLE, Acl.LIST).then(domainService.findById_migrated(domain).switchIfEmpty(Mono.error(new DomainNotFoundException(domain)))))
                        .flatMapSingle(__ -> RxJava2Adapter.monoToSingle(searchRoles_migrated(domain, query, page, size)))).map(RxJavaReactorMigrationUtil.toJdkFunction(pagedRoles -> {
                            List<Role> roles = pagedRoles.getData().stream()
                                    .map(this::filterRoleInfos)
                                    .sorted(Comparator.comparing(Role::getName))
                                    .collect(Collectors.toList());
                            return new Page<>(roles, pagedRoles.getCurrentPage(), pagedRoles.getTotalCount());
                        })))
                .subscribe(response::resume, response::resume);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a role",
            notes = "User must have the DOMAIN_ROLE[CREATE] permission on the specified domain " +
                    "or DOMAIN_ROLE[CREATE] permission on the specified environment " +
                    "or DOMAIN_ROLE[CREATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Role successfully created"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void create(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @ApiParam(name = "role", required = true)
            @Valid @NotNull final NewRole newRole,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_ROLE, Acl.CREATE).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domain).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(irrelevant -> RxJava2Adapter.monoToSingle(roleService.create_migrated(domain, newRole, authenticatedUser).map(RxJavaReactorMigrationUtil.toJdkFunction(role -> Response
                                        .created(URI.create("/organizations/" + organizationId + "/environments/" + environmentId + "/domains/" + domain + "/roles/" + role.getId()))
                                        .entity(role)
                                        .build())))))))
                .subscribe(response::resume, response::resume);
    }

    @Path("{role}")
    public RoleResource getRoleResource() {
        return resourceContext.getResource(RoleResource.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.searchRoles_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Page<Role>> searchRoles(String domain, String query, int page, int size) {
 return RxJava2Adapter.monoToSingle(searchRoles_migrated(domain, query, page, size));
}
private Mono<Page<Role>> searchRoles_migrated(String domain, String query, int page, int size) {
        if (query == null) {
            return roleService.findByDomain_migrated(domain, page, Math.min(MAX_ROLES_SIZE_PER_PAGE, size));
        }
        return roleService.searchByDomain_migrated(domain, query, page, Math.min(MAX_ROLES_SIZE_PER_PAGE, size));
    }

    private Role filterRoleInfos(Role role) {
        Role filteredRole = new Role();
        filteredRole.setId(role.getId());
        filteredRole.setName(role.getName());
        filteredRole.setDescription(role.getDescription());

        return filteredRole;
    }
}

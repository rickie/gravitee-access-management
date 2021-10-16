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
import io.gravitee.am.model.Group;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.GroupService;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.exception.GroupNotFoundException;
import io.gravitee.common.http.MediaType;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collections;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class GroupRolesResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private DomainService domainService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private RoleService roleService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a group roles",
            notes = "User must have the DOMAIN_GROUP[READ] permission on the specified domain " +
                    "or DOMAIN_GROUP[READ] permission on the specified environment " +
                    "or DOMAIN_GROUP[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Group roles successfully fetched", response = Role.class, responseContainer = "Set"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("group") String group,
            @Suspended final AsyncResponse response) {

        checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_GROUP, Acl.READ).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domain).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))).flatMap(z->groupService.findById_migrated(group)).switchIfEmpty(Mono.error(new GroupNotFoundException(group))))
                        .flatMapSingle(group1 -> {
                            if (group1.getRoles() == null || group1.getRoles().isEmpty()) {
                                return RxJava2Adapter.monoToSingle(Mono.just(Collections.emptyList()));
                            }
                            return RxJava2Adapter.monoToSingle(roleService.findByIdIn_migrated(group1.getRoles()));
                        }))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Assign roles to a group",
            notes = "User must have the DOMAIN_GROUP[UPDATE] permission on the specified domain " +
                    "or DOMAIN_GROUP[UPDATE] permission on the specified environment " +
                    "or DOMAIN_GROUP[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Roles successfully assigned", response = Group.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void assign(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("group") String group,
            @Valid @NotNull final List<String> roles,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_GROUP, Acl.UPDATE).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domain).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(domain1 -> RxJava2Adapter.monoToSingle(groupService.assignRoles_migrated(ReferenceType.DOMAIN, domain, group, roles, authenticatedUser))))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @Path("{role}")
    public GroupRoleResource getGroupRoleResource() {
        return resourceContext.getResource(GroupRoleResource.class);
    }
}
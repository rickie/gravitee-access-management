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
package io.gravitee.am.management.handlers.management.api.resources.organizations.roles;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.model.RoleEntity;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.model.UpdateRole;
import io.gravitee.common.http.MediaType;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RoleResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private RoleService roleService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a platform role",
            notes = "User must have the ORGANIZATION_ROLE[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Role successfully fetched", response = RoleEntity.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void get(
            @PathParam("organizationId") String organizationId,
            @PathParam("role") String role,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_ROLE, Acl.READ).then(roleService.findById_migrated(ReferenceType.ORGANIZATION, organizationId, role).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update a platform role",
            notes = "User must have the ORGANIZATION_ROLE[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Role successfully updated", response = RoleEntity.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void update(
            @PathParam("organizationId") String organizationId,
            @PathParam("role") String role,
            @ApiParam(name = "role", required = true) @Valid @NotNull UpdateRole updateRole,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_ROLE, Acl.UPDATE).then(roleService.update_migrated(ReferenceType.ORGANIZATION, organizationId, role, updateRole, authenticatedUser).map(RxJavaReactorMigrationUtil.toJdkFunction(this::convert))))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @DELETE
    @ApiOperation(value = "Delete a plaform role",
            notes = "User must have the ORGANIZATION_ROLE[DELETE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Role successfully deleted"),
            @ApiResponse(code = 400, message = "Role is bind to existing users"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void delete(
            @PathParam("organizationId") String organizationId,
            @PathParam("role") String role,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_ROLE, Acl.DELETE).then(roleService.delete_migrated(ReferenceType.ORGANIZATION, organizationId, role, authenticatedUser)))
                .subscribe(() -> response.resume(Response.noContent().build()),
                        response::resume);
    }

    private RoleEntity convert(Role role) {

        RoleEntity roleEntity = new RoleEntity(role);

        roleEntity.setAvailablePermissions(Permission.allPermissions(role.getAssignableType()).stream().map(permission -> permission.name().toLowerCase()).collect(Collectors.toList()));
        roleEntity.setPermissions(Permission.flatten(role.getPermissionAcls()));

        return roleEntity;
    }
}
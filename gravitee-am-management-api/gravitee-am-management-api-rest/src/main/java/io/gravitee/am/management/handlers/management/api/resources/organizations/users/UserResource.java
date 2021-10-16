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
package io.gravitee.am.management.handlers.management.api.resources.organizations.users;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.management.handlers.management.api.model.PasswordValue;
import io.gravitee.am.management.handlers.management.api.model.StatusEntity;
import io.gravitee.am.management.handlers.management.api.model.UserEntity;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.management.service.OrganizationUserService;
import io.gravitee.am.management.service.impl.IdentityProviderManagerImpl;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.IdentityProviderService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.exception.UserInvalidException;
import io.gravitee.am.service.model.UpdateUser;
import io.gravitee.common.http.MediaType;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.inject.Named;
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
public class UserResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    @Named("managementOrganizationUserService")
    private OrganizationUserService organizationUserService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a user",
            notes = "User must have the ORGANIZATION_USER[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User successfully fetched", response = UserEntity.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void get(
            @PathParam("organizationId") String organizationId,
            @PathParam("user") String user,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.monoToSingle(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_USER, Acl.READ).then(organizationUserService.findById_migrated(ReferenceType.ORGANIZATION, organizationId, user).map(RxJavaReactorMigrationUtil.toJdkFunction(UserEntity::new)).flatMap(v->enhanceIdentityProvider_migrated(v))))
                .subscribe(response::resume, response::resume);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update a user",
            notes = "User must have the ORGANIZATION_USER[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "User successfully updated", response = User.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void updateUser(
            @PathParam("organizationId") String organizationId,
            @PathParam("user") String user,
            @ApiParam(name = "user", required = true) @Valid @NotNull UpdateUser updateUser,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_USER, Acl.UPDATE).then(organizationUserService.update_migrated(ReferenceType.ORGANIZATION, organizationId, user, updateUser, authenticatedUser)))
                .subscribe(response::resume, response::resume);
    }

    @PUT
    @Path("/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update a user status",
            notes = "User must have the ORGANIZATION_USER[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "User status successfully updated", response = User.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void updateUserStatus(
            @PathParam("organizationId") String organizationId,
            @PathParam("user") String user,
            @ApiParam(name = "status", required = true) @Valid @NotNull StatusEntity status,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_USER, Acl.UPDATE).then(organizationUserService.updateStatus_migrated(ReferenceType.ORGANIZATION, organizationId, user, status.isEnabled(), authenticatedUser)))
                .subscribe(response::resume, response::resume);
    }

    @DELETE
    @ApiOperation(value = "Delete a user",
            notes = "User must have the ORGANIZATION_USER[DELETE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 204, message = "User successfully deleted"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void delete(
            @PathParam("organizationId") String organizationId,
            @PathParam("user") String user,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_USER, Acl.DELETE).then(organizationUserService.delete_migrated(ReferenceType.ORGANIZATION, organizationId, user, authenticatedUser)))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);
    }

    @POST
    @Path("resetPassword")
    @ApiOperation(value = "Reset password",
            notes = "User must have the ORGANIZATION_USER[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password reset"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void resetPassword(
            @PathParam("organizationId") String organizationId,
            @PathParam("user") String user,
            @ApiParam(name = "password", required = true) @Valid @NotNull PasswordValue password,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_USER, Acl.UPDATE).then(organizationUserService.findById_migrated(ReferenceType.ORGANIZATION, organizationId, user).filter(RxJavaReactorMigrationUtil.toJdkPredicate(existingUser -> IdentityProviderManagerImpl.IDP_GRAVITEE.equals(existingUser.getSource()))).switchIfEmpty(Mono.error(new UserInvalidException("Unable to reset password"))).flatMap(existingUser->organizationUserService.resetPassword_migrated(organizationId, existingUser, password.getPassword(), authenticatedUser)).then()))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);

    }
    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enhanceIdentityProvider_migrated(userEntity))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<UserEntity> enhanceIdentityProvider(UserEntity userEntity) {
 return RxJava2Adapter.monoToSingle(enhanceIdentityProvider_migrated(userEntity));
}
private Mono<UserEntity> enhanceIdentityProvider_migrated(UserEntity userEntity) {
        if (userEntity.getSource() != null) {
            return identityProviderService.findById_migrated(userEntity.getSource()).map(RxJavaReactorMigrationUtil.toJdkFunction(idP -> {
                        userEntity.setSource(idP.getName());
                        return userEntity;
                    })).defaultIfEmpty(userEntity).single();
        }
        return Mono.just(userEntity);
    }
}

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
import io.gravitee.am.management.handlers.management.api.resources.AbstractUsersResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.IdentityProviderService;
import io.gravitee.am.service.OrganizationService;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.common.http.MediaType;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.swagger.annotations.*;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
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
@Api(tags = {"user"})
public class UsersResource extends AbstractUsersResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Autowired
    private OrganizationService organizationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List users of the organization",
            notes = "User must have the ORGANIZATION_USER[LIST] permission on the specified organization. " +
                    "Each returned user is filtered and contains only basic information such as id and username and displayname. " +
                    "Last login and identity provider name will be also returned if current user has ORGANIZATION_USER[READ] permission on the organization.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List users of the organization", response = User.class, responseContainer = "Set"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @QueryParam("q") String query,
            @QueryParam("filter") String filter,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue(MAX_USERS_SIZE_PER_PAGE_STRING) int size,
            @Suspended final AsyncResponse response) {

        io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(permissionService.findAllPermissions_migrated(authenticatedUser, ReferenceType.ORGANIZATION, organizationId).flatMap(organizationPermissions->checkPermission_migrated(organizationPermissions, Permission.ORGANIZATION_USER, Acl.LIST).then(searchUsers_migrated(ReferenceType.ORGANIZATION, organizationId, query, filter, page, size).flatMap(pagedUsers->RxJava2Adapter.singleToMono(Observable.fromIterable(pagedUsers.getData()).flatMapSingle((io.gravitee.am.model.User user)->RxJava2Adapter.monoToSingle(filterUserInfos_migrated(organizationPermissions, user))).toSortedList(Comparator.comparing(User::getUsername))).map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<io.gravitee.am.model.User> users)->new Page<>(users, pagedUsers.getCurrentPage(), pagedUsers.getTotalCount())))))))
                .subscribe(response::resume, response::resume);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a platform user",
            notes = "User must have the ORGANIZATION_USER[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "User successfully created"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void create(
            @PathParam("organizationId") String organizationId,
            @ApiParam(name = "user", required = true) @Valid @NotNull final NewUser newUser,
            @Suspended final AsyncResponse response) {

        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_USER, Acl.CREATE).then(organizationService.findById_migrated(organizationId).flatMap(organization->organizationUserService.createGraviteeUser_migrated(organization, newUser, authenticatedUser)).map(RxJavaReactorMigrationUtil.toJdkFunction(user -> Response
                                .created(URI.create("/organizations/" + organizationId + "/users/" + user.getId()))
                                .entity(user)
                                .build()))))
                .subscribe(response::resume, response::resume);
    }

    @Path("{user}")
    public UserResource getUserResource() {
        return resourceContext.getResource(UserResource.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.filterUserInfos_migrated(organizationPermissions, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<User> filterUserInfos(Map<Permission, Set<Acl>> organizationPermissions, User user) {
 return RxJava2Adapter.monoToSingle(filterUserInfos_migrated(organizationPermissions, user));
}
private Mono<User> filterUserInfos_migrated(Map<Permission, Set<Acl>> organizationPermissions, User user) {

        User filteredUser;

        if (hasPermission(organizationPermissions, Permission.ORGANIZATION_USER, Acl.READ)) {
            // Current user has read permission, copy all information.
            filteredUser = new User(user);
            if (user.getSource() != null) {
                return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(identityProviderService.findById_migrated(user.getSource()))).map(RxJavaReactorMigrationUtil.toJdkFunction(idP -> {
                            filteredUser.setSource(idP.getName());
                            return filteredUser;
                        })).defaultIfEmpty(filteredUser).single();
            }
        } else {
            // Current user doesn't have read permission, select only few information and remove default values that could be inexact.
            filteredUser = new User(false);
            filteredUser.setId(user.getId());
            filteredUser.setUsername(user.getUsername());
            filteredUser.setDisplayName(user.getDisplayName());
            filteredUser.setPicture(user.getPicture());
        }

        return Mono.just(filteredUser);
    }
}

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
import io.gravitee.am.management.handlers.management.api.resources.AbstractUsersResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.IdentityProviderService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.common.http.MediaType;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.util.Comparator;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
    private DomainService domainService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List users for a security domain",
            notes = "User must have the DOMAIN_USER[LIST] permission on the specified domain " +
                    "or DOMAIN_USER[LIST] permission on the specified environment " +
                    "or DOMAIN_USER[LIST] permission on the specified organization. " +
                    "Each returned user is filtered and contains only basic information such as id and username and displayname. " +
                    "Last login and identity provider name will be also returned if current user has DOMAIN_USER[READ] permission on the domain, environment or organization.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List users for a security domain", response = User.class, responseContainer = "Set"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @QueryParam("q") String query,
            @QueryParam("filter") String filter,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue(MAX_USERS_SIZE_PER_PAGE_STRING) int size,
            @Suspended final AsyncResponse response) {

        io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.LIST).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domain).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(__ -> RxJava2Adapter.monoToSingle(searchUsers_migrated(ReferenceType.DOMAIN, domain, query, filter, page, size)))).flatMap(pagedUsers->hasAnyPermission_migrated(authenticatedUser, organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.READ).flatMap(hasPermission->RxJava2Adapter.singleToMono(Observable.fromIterable(pagedUsers.getData()).flatMapSingle((io.gravitee.am.model.User user)->RxJava2Adapter.monoToSingle(filterUserInfos_migrated(hasPermission, user))).toSortedList(Comparator.comparing(User::getUsername))).map(RxJavaReactorMigrationUtil.toJdkFunction((java.util.List<io.gravitee.am.model.User> users)->new Page<>(users, pagedUsers.getCurrentPage(), pagedUsers.getTotalCount())))))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a user on the specified security domain",
            notes = "User must have the DOMAIN_USER[CREATE] permission on the specified domain " +
                    "or DOMAIN_USER[CREATE] permission on the specified environment " +
                    "or DOMAIN_USER[CREATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "User successfully created"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void create(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domainId,
            @ApiParam(name = "user", required = true)
            @Valid @NotNull final NewUser newUser,
            @Suspended final AsyncResponse response) {

        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        checkAnyPermission_migrated(organizationId, environmentId, domainId, Permission.DOMAIN_USER, Acl.CREATE).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domainId).switchIfEmpty(Mono.error(new DomainNotFoundException(domainId))))
                        .flatMapSingle(domain -> RxJava2Adapter.monoToSingle(userService.create_migrated(domain, newUser, authenticatedUser)))).map(RxJavaReactorMigrationUtil.toJdkFunction(user -> Response
                                .created(URI.create("/organizations/" + organizationId + "/environments/" + environmentId + "/domains/" + domainId + "/users/" + user.getId()))
                                .entity(user)
                                .build()))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @Path("{user}")
    public UserResource getUserResource() {
        return resourceContext.getResource(UserResource.class);
    }

    
private Mono<User> filterUserInfos_migrated(Boolean hasPermission, User user) {
        User filteredUser;
        if (hasPermission) {
            // Current user has read permission, copy all information.
            filteredUser = new User(user);
            if (user.getSource() != null) {
                return identityProviderService.findById_migrated(user.getSource()).map(RxJavaReactorMigrationUtil.toJdkFunction(idP -> {
                            filteredUser.setSource(idP.getName());
                            return filteredUser;
                        })).defaultIfEmpty(filteredUser).single();
            }
        } else {
            // Current user doesn't have read permission, select only few information and remove default values that could be inexact.
            filteredUser = new User(false);
            filteredUser.setId(user.getId());
            filteredUser.setUsername(user.getUsername());
            filteredUser.setEnabled(user.isEnabled());
            filteredUser.setDisplayName(user.getDisplayName());
            filteredUser.setPicture(user.getPicture());
        }

        return Mono.just(filteredUser);
    }
}

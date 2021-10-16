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

import io.gravitee.am.management.handlers.management.api.model.ApplicationEntity;
import io.gravitee.am.management.handlers.management.api.model.PasswordValue;
import io.gravitee.am.management.handlers.management.api.model.StatusEntity;
import io.gravitee.am.management.handlers.management.api.model.UserEntity;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.management.service.UserService;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.IdentityProviderService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.gravitee.am.service.model.UpdateUser;
import io.gravitee.common.http.MediaType;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
    private UserService userService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Autowired
    private ApplicationService applicationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a user",
            notes = "User must have the DOMAIN_USER[READ] permission on the specified domain " +
                    "or DOMAIN_USER[READ] permission on the specified environment " +
                    "or DOMAIN_USER[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User successfully fetched", response = UserEntity.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void get(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("user") String user,
            @Suspended final AsyncResponse response) {

        checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.READ).as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))).flatMap(z->userService.findById(user).as(RxJava2Adapter::maybeToMono)).switchIfEmpty(Mono.error(new UserNotFoundException(user))).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.model.User, MaybeSource<io.gravitee.am.management.handlers.management.api.model.UserEntity>>toJdkFunction(user1 -> {
                            if (user1.getReferenceType() == ReferenceType.DOMAIN
                                    && !user1.getReferenceId().equalsIgnoreCase(domain)) {
                                throw new BadRequestException("User does not belong to domain");
                            }
                            return RxJava2Adapter.monoToMaybe(Mono.just(new UserEntity(user1)));
                        }).apply(v)))).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.management.handlers.management.api.model.UserEntity, MaybeSource<io.gravitee.am.management.handlers.management.api.model.UserEntity>>toJdkFunction(this::enhanceIdentityProvider).apply(v)))).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.management.handlers.management.api.model.UserEntity, MaybeSource<io.gravitee.am.management.handlers.management.api.model.UserEntity>>toJdkFunction(this::enhanceClient).apply(v))))).as(RxJava2Adapter::monoToMaybe)
                .subscribe(response::resume, response::resume);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update a user",
            notes = "User must have the DOMAIN_USER[UPDATE] permission on the specified domain " +
                    "or DOMAIN_USER[UPDATE] permission on the specified environment " +
                    "or DOMAIN_USER[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "User successfully updated", response = User.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void updateUser(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("user") String user,
            @ApiParam(name = "user", required = true) @Valid @NotNull UpdateUser updateUser,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.UPDATE)).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(irrelevant -> userService.update(ReferenceType.DOMAIN, domain, user, updateUser, authenticatedUser)))))
                .subscribe(response::resume, response::resume);
    }

    @PUT
    @Path("/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update a user status",
            notes = "User must have the DOMAIN_USER[UPDATE] permission on the specified domain " +
                    "or DOMAIN_USER[UPDATE] permission on the specified environment " +
                    "or DOMAIN_USER[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "User status successfully updated", response = User.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void updateUserStatus(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("user") String user,
            @ApiParam(name = "status", required = true) @Valid @NotNull StatusEntity status,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.UPDATE)).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapSingle(irrelevant -> userService.updateStatus(ReferenceType.DOMAIN, domain, user, status.isEnabled(), authenticatedUser)))))
                .subscribe(response::resume, response::resume);
    }

    @DELETE
    @ApiOperation(value = "Delete a user",
            notes = "User must have the DOMAIN_USER[DELETE] permission on the specified domain " +
                    "or DOMAIN_USER[DELETE] permission on the specified environment " +
                    "or DOMAIN_USER[DELETE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 204, message = "User successfully deleted"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void delete(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("user") String user,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.DELETE)).then(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))).flatMap(irrelevant->RxJava2Adapter.completableToMono(userService.delete(ReferenceType.DOMAIN, domain, user, authenticatedUser))).then()))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);
    }

    @POST
    @Path("resetPassword")
    @ApiOperation(value = "Reset password",
            notes = "User must have the DOMAIN_USER[UPDATE] permission on the specified domain " +
                    "or DOMAIN_USER[UPDATE] permission on the specified environment " +
                    "or DOMAIN_USER[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password reset"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void resetPassword(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domainId,
            @PathParam("user") String user,
            @ApiParam(name = "password", required = true) @Valid @NotNull PasswordValue password,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domainId, Permission.DOMAIN_USER, Acl.UPDATE)).then(RxJava2Adapter.maybeToMono(domainService.findById(domainId)).switchIfEmpty(Mono.error(new DomainNotFoundException(domainId))).flatMap(domain->RxJava2Adapter.completableToMono(userService.resetPassword(domain, user, password.getPassword(), authenticatedUser))).then()))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);

    }

    @POST
    @Path("sendRegistrationConfirmation")
    @ApiOperation(value = "Send registration confirmation email",
            notes = "User must have the DOMAIN_USER[UPDATE] permission on the specified domain " +
                    "or DOMAIN_USER[UPDATE] permission on the specified environment " +
                    "or DOMAIN_USER[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Email sent"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void sendRegistrationConfirmation(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("user") String user,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.UPDATE)).then(RxJava2Adapter.completableToMono(userService.sendRegistrationConfirmation(domain, user, authenticatedUser))))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);
    }

    @POST
    @Path("unlock")
    @ApiOperation(value = "Unlock a user",
            notes = "User must have the DOMAIN_USER[UPDATE] permission on the specified domain " +
                    "or DOMAIN_USER[UPDATE] permission on the specified environment " +
                    "or DOMAIN_USER[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User unlocked"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void unlockUser(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("user") String user,
            @Suspended final AsyncResponse response) {
        final io.gravitee.am.identityprovider.api.User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(checkAnyPermission(organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.UPDATE)).then(RxJava2Adapter.maybeToMono(domainService.findById(domain)).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))).flatMap(irrelevant->RxJava2Adapter.completableToMono(userService.unlock(ReferenceType.DOMAIN, domain, user, authenticatedUser))).then()))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);

    }

    @Path("consents")
    public UserConsentsResource getUserConsentsResource() {
        return resourceContext.getResource(UserConsentsResource.class);
    }

    @Path("roles")
    public UserRolesResource getUserRolesResource() {
        return resourceContext.getResource(UserRolesResource.class);
    }

    @Path("factors")
    public UserFactorsResource getUserFactorsResource() {
        return resourceContext.getResource(UserFactorsResource.class);
    }

    @Path("credentials")
    public UserCredentialsResource getUserCredentialsResource() {
        return resourceContext.getResource(UserCredentialsResource.class);
    }

    @Deprecated
private Maybe<UserEntity> enhanceIdentityProvider(UserEntity userEntity) {
 return RxJava2Adapter.monoToMaybe(enhanceIdentityProvider_migrated(userEntity));
}
private Mono<UserEntity> enhanceIdentityProvider_migrated(UserEntity userEntity) {
        if (userEntity.getSource() != null) {
            return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(identityProviderService.findById(userEntity.getSource())).map(RxJavaReactorMigrationUtil.toJdkFunction(idP -> {
                        userEntity.setSource(idP.getName());
                        return userEntity;
                    })).defaultIfEmpty(userEntity)));
        }
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(userEntity)));
    }

    @Deprecated
private Maybe<UserEntity> enhanceClient(UserEntity userEntity) {
 return RxJava2Adapter.monoToMaybe(enhanceClient_migrated(userEntity));
}
private Mono<UserEntity> enhanceClient_migrated(UserEntity userEntity) {
        if (userEntity.getClient() != null) {
            return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(applicationService.findById(userEntity.getClient())).switchIfEmpty(Mono.defer(()->RxJava2Adapter.maybeToMono(applicationService.findByDomainAndClientId(userEntity.getReferenceId(), userEntity.getClient())))).map(RxJavaReactorMigrationUtil.toJdkFunction(application -> {
                        userEntity.setApplicationEntity(new ApplicationEntity(application));
                        return userEntity;
                    })).defaultIfEmpty(userEntity)));
        }
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(userEntity)));
    }
}

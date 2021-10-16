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
package io.gravitee.am.management.handlers.management.api.resources.organizations.tags;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Tag;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.TagService;
import io.gravitee.am.service.exception.TagNotFoundException;
import io.gravitee.am.service.model.UpdateTag;
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
 * @author GraviteeSource Team
 */
public class TagResource extends AbstractResource {

    @Autowired
    private TagService tagService;

    @Context
    private ResourceContext resourceContext;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a sharding tag",
            notes = "User must have the ORGANIZATION_TAG[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Sharding tag", response = Tag.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void get(
            @PathParam("organizationId") String organizationId,
            @PathParam("tag") String tagId, @Suspended final AsyncResponse response) {

        RxJava2Adapter.maybeToMono(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_TAG, Acl.READ).then(tagService.findById_migrated(tagId, organizationId).switchIfEmpty(Mono.error(new TagNotFoundException(tagId)))).as(RxJava2Adapter::monoToMaybe)).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Update the sharding tag",
            notes = "User must have the ORGANIZATION_TAG[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Sharding tag successfully updated", response = Tag.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void update(
            @PathParam("organizationId") String organizationId,
            @ApiParam(name = "tag", required = true) @Valid @NotNull final UpdateTag tagToUpdate,
            @PathParam("tag") String tagId,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_TAG, Acl.UPDATE).then(tagService.update_migrated(tagId, organizationId, tagToUpdate, authenticatedUser)))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(response::resume), RxJavaReactorMigrationUtil.toJdkConsumer(response::resume));
    }

    @DELETE
    @ApiOperation(value = "Delete the sharding tag",
            notes = "User must have the ORGANIZATION_TAG[DELETE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Sharding tag successfully deleted"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void delete(
            @PathParam("organizationId") String organizationId,
            @PathParam("tag") String tag,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_TAG, Acl.DELETE).then(tagService.delete_migrated(tag, organizationId, authenticatedUser)))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);
    }
}

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
package io.gravitee.am.management.handlers.management.api.resources.organizations.idps;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.management.service.IdentityProviderManager;
import io.gravitee.am.management.service.impl.IdentityProviderManagerImpl;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.IdentityProviderService;
import io.gravitee.am.service.model.NewIdentityProvider;
import io.gravitee.common.http.MediaType;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.swagger.annotations.*;
import java.net.URI;
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
@Api(tags = {"identity provider"})
public class IdentityProvidersResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Autowired
    private IdentityProviderManager identityProviderManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List registered identity providers of the organization",
            notes = "User must have the ORGANIZATION_IDENTITY_PROVIDER[LIST] permission on the specified organization. " +
                    "Each returned identity provider is filtered and contains only basic information such as id, name, type and isExternal.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List registered identity providers of the organization", response = IdentityProvider.class, responseContainer = "Set"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @QueryParam("userProvider") boolean userProvider,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_IDENTITY_PROVIDER, Acl.LIST))).then(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(identityProviderService.findAll_migrated(ReferenceType.ORGANIZATION, organizationId))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(identityProvider -> {
                            if (userProvider) {
                                return identityProviderManager.userProviderExists(identityProvider.getId());
                            }
                            return true;
                        })).map(RxJavaReactorMigrationUtil.toJdkFunction(this::filterIdentityProviderInfos)).sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName())).collectList()))
                .subscribe(response::resume, response::resume);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create an identity provider for the organization",
            notes = "User must have the ORGANIZATION_IDENTITY_PROVIDER[CREATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 201, message = "Identity provider successfully created"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void create(
            @PathParam("organizationId") String organizationId,
            @ApiParam(name = "identity", required = true) @Valid @NotNull final NewIdentityProvider newIdentityProvider,
            @Suspended final AsyncResponse response) {

        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_IDENTITY_PROVIDER, Acl.CREATE))).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(identityProviderService.create_migrated(ReferenceType.ORGANIZATION, organizationId, newIdentityProvider, authenticatedUser))).map(RxJavaReactorMigrationUtil.toJdkFunction(identityProvider -> Response
                                .created(URI.create("/organizations/" + organizationId + "/identities/" + identityProvider.getId()))
                                .entity(identityProvider)
                                .build()))))
                .subscribe(response::resume, response::resume);
    }

    @Path("{identity}")
    public IdentityProviderResource getIdentityProviderResource() {
        return resourceContext.getResource(IdentityProviderResource.class);
    }

    private IdentityProvider filterIdentityProviderInfos(IdentityProvider identityProvider) {
        IdentityProvider filteredIdentityProvider = new IdentityProvider();
        filteredIdentityProvider.setId(identityProvider.getId());
        filteredIdentityProvider.setName(identityProvider.getName());
        filteredIdentityProvider.setType(identityProvider.getType());
        filteredIdentityProvider.setExternal(identityProvider.isExternal());

        return filteredIdentityProvider;
    }
}

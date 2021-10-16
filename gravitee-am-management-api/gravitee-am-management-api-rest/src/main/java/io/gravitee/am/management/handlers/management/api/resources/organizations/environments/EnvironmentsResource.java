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
package io.gravitee.am.management.handlers.management.api.resources.organizations.environments;

import static io.gravitee.am.management.service.permissions.Permissions.of;
import static io.gravitee.am.management.service.permissions.Permissions.or;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Environment;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.EnvironmentService;
import io.gravitee.common.http.MediaType;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EnvironmentsResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private EnvironmentService environmentService;

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all the environments",
            notes = "User must have the ENVIRONMENT[LIST] permission on the specified organization " +
                    "AND either ENVIRONMENT[READ] permission on each environment " +
                    "or ENVIRONMENT[READ] permission on the specified organization." +
                    "Each returned environment is filtered and contains only basic information such as id and name.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List all the environments of the organization", response = Environment.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @Suspended final AsyncResponse response) {

        User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ENVIRONMENT, Acl.LIST).thenMany(RxJava2Adapter.fluxToFlowable(environmentService.findAll_migrated(organizationId))).flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<Environment, MaybeSource<Environment>>toJdkFunction(environment -> RxJava2Adapter.monoToMaybe(hasPermission_migrated(authenticatedUser, or(of(ReferenceType.ENVIRONMENT, environment.getId(), Permission.ENVIRONMENT, Acl.READ),
                                of(ReferenceType.ORGANIZATION, organizationId, Permission.ENVIRONMENT, Acl.READ))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(Boolean::booleanValue)).map(RxJavaReactorMigrationUtil.toJdkFunction(permit -> environment)))).apply(e)))).map(RxJavaReactorMigrationUtil.toJdkFunction(this::filterEnvironmentInfos)).sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName())).collectList())
                .subscribe(response::resume, response::resume);
    }

    private Environment filterEnvironmentInfos(Environment environment) {

        Environment filteredEnvironment = new Environment();
        filteredEnvironment.setId(environment.getId());
        filteredEnvironment.setName(environment.getName());
        filteredEnvironment.setDescription(environment.getDescription());
        filteredEnvironment.setDomainRestrictions(environment.getDomainRestrictions());
        filteredEnvironment.setHrids(environment.getHrids());

        return filteredEnvironment;
    }

    @Path("/{environmentId}")
    public EnvironmentResource getEnvironmentResource() {
        return resourceContext.getResource(EnvironmentResource.class);
    }
}
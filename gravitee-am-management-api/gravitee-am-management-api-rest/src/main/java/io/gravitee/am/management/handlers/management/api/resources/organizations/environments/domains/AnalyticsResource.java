/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.management.handlers.management.api.resources.organizations.environments.domains;

import io.gravitee.am.management.handlers.management.api.model.AnalyticsParam;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.management.service.AnalyticsService;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.analytics.AnalyticsQuery;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.common.http.MediaType;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AnalyticsResource extends AbstractResource {

    @Autowired private AnalyticsService analyticsService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Find domain analytics",
            notes =
                    "User must have DOMAIN_ANALYTICS[READ] permission on the specified domain "
                            + "or DOMAIN_ANALYTICS[READ] permission on the specified environment "
                            + "or DOMAIN_ANALYTICS[READ] permission on the specified organization")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Analytics successfully fetched"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public void get(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @BeanParam AnalyticsParam param,
            @Suspended final AsyncResponse response) {

        // validate param
        param.validate();

        AnalyticsQuery query = new AnalyticsQuery();
        query.setType(param.getType());
        query.setField(param.getField());
        query.setFrom(param.getFrom());
        query.setTo(param.getTo());
        query.setDomain(domain);
        query.setInterval(param.getInterval());
        query.setSize(param.getSize());

        checkAnyPermission(
                        organizationId,
                        environmentId,
                        domain,
                        Permission.DOMAIN_ANALYTICS,
                        Acl.READ)
                .andThen(analyticsService.execute(query))
                .subscribe(response::resume, response::resume);
    }
}

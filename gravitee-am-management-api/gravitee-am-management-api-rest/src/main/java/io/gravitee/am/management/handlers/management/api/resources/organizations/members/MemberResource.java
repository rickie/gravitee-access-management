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
package io.gravitee.am.management.handlers.management.api.resources.organizations.members;




import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.model.Acl;

import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.MembershipService;
import io.gravitee.am.service.OrganizationService;




import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.DELETE;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;


/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class MemberResource extends AbstractResource {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private MembershipService membershipService;

    @DELETE
    @ApiOperation(value = "Remove a membership of the organization",
            notes = "User must have ORGANIZATION_MEMBER[DELETE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 204, message = "Membership successfully deleted"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void removeMember(
            @PathParam("organizationId") String organizationId,
            @PathParam("member") String membershipId,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(checkPermission_migrated(ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_MEMBER, Acl.DELETE).then(organizationService.findById_migrated(organizationId).flatMap(irrelevant->membershipService.delete_migrated(membershipId, authenticatedUser)).then()))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);
    }
}
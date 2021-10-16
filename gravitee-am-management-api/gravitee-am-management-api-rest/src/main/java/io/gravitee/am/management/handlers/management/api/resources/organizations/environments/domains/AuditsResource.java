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

import static io.gravitee.am.management.service.permissions.Permissions.of;
import static io.gravitee.am.management.service.permissions.Permissions.or;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.model.AuditParam;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.management.handlers.management.api.resources.utils.FilterUtils;
import io.gravitee.am.management.service.AuditService;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.reporter.api.audit.AuditReportableCriteria;
import io.gravitee.am.reporter.api.audit.model.Audit;
import io.gravitee.common.http.MediaType;
import io.reactivex.Single;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.Collections;
import java.util.stream.Collectors;
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
@Api(tags = {"audit"})
public class AuditsResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private AuditService auditService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List audit logs for a security domain",
            notes = "User must have the DOMAIN_AUDIT[LIST] permission on the specified domain, environment or organization. " +
                    "Except if user has ORGANIZATION_AUDIT[READ] permission on the domain, environment or organization, each returned audit is filtered and contains only basic information such as id, date, event, actor, target and status.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List audit logs for a security domain", response = Audit.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @BeanParam AuditParam param,
            @Suspended final AsyncResponse response) {

        AuditReportableCriteria.Builder queryBuilder = new AuditReportableCriteria.Builder()
                .from(param.getFrom())
                .to(param.getTo())
                .status(param.getStatus())
                .user(param.getUser());

        if (param.getType() != null) {
            queryBuilder.types(Collections.singletonList(param.getType()));
        }

        User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToSingle(checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_AUDIT, Acl.LIST).then(auditService.search_migrated(domain, queryBuilder.build(), param.getPage(), param.getSize()).flatMap(auditPage->hasPermission_migrated(authenticatedUser, ReferenceType.ORGANIZATION, organizationId, Permission.ORGANIZATION_AUDIT, Acl.READ).map(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.Boolean hasPermission)->{
if (hasPermission) {
return auditPage;
} else {
return new Page<>(auditPage.getData().stream().map(FilterUtils::filterAuditInfos).collect(Collectors.toList()), auditPage.getCurrentPage(), auditPage.getTotalCount());
}
})))))
                .subscribe(response::resume, response::resume);
    }

    @Path("{audit}")
    public AuditResource getAuditResource() {
        return resourceContext.getResource(AuditResource.class);
    }
}
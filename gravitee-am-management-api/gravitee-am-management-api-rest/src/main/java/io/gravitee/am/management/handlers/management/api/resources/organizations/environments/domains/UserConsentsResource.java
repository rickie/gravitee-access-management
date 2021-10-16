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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.model.ApplicationEntity;
import io.gravitee.am.management.handlers.management.api.model.ScopeApprovalEntity;
import io.gravitee.am.management.handlers.management.api.model.ScopeEntity;
import io.gravitee.am.management.handlers.management.api.resources.AbstractResource;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.ScopeApprovalService;
import io.gravitee.am.service.ScopeService;
import io.gravitee.am.service.exception.DomainNotFoundException;
import io.gravitee.common.http.MediaType;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
public class UserConsentsResource extends AbstractResource {

    @Context
    private ResourceContext resourceContext;

    @Autowired
    private DomainService domainService;

    @Autowired
    private ScopeApprovalService scopeApprovalService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ScopeService scopeService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a user consents",
            notes = "User must have the DOMAIN_USER[READ] permission on the specified domain " +
                    "or DOMAIN_USER[READ] permission on the specified environment " +
                    "or DOMAIN_USER[READ] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 200, message = "User consents successfully fetched", response = ScopeApprovalEntity.class),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void list(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("user") String user,
            @QueryParam("clientId") String clientId,
            @Suspended final AsyncResponse response) {

        RxJava2Adapter.monoToSingle(checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.READ).then(RxJava2Adapter.flowableToFlux(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domain).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))))
                        .flatMapPublisher(__ -> {
                            if (clientId == null || clientId.isEmpty()) {
                                return RxJava2Adapter.fluxToFlowable(scopeApprovalService.findByDomainAndUser_migrated(domain, user));
                            }
                            return RxJava2Adapter.fluxToFlowable(scopeApprovalService.findByDomainAndUserAndClient_migrated(domain, user, clientId));
                        })
                        .flatMapSingle(scopeApproval ->
                                RxJava2Adapter.monoToSingle(getClient_migrated(scopeApproval.getDomain(), scopeApproval.getClientId()).zipWith(getScope_migrated(scopeApproval.getDomain(), scopeApproval.getScope()), RxJavaReactorMigrationUtil.toJdkBiFunction(((clientEntity, scopeEntity) -> {
                                            ScopeApprovalEntity scopeApprovalEntity = new ScopeApprovalEntity(scopeApproval);
                                            scopeApprovalEntity.setClientEntity(clientEntity);
                                            scopeApprovalEntity.setScopeEntity(scopeEntity);
                                            return scopeApprovalEntity;
                                        })))))).collectList()))
                .subscribe(response::resume, response::resume);
    }

    @DELETE
    @ApiOperation(value = "Revoke user consents",
            notes = "User must have the DOMAIN_USER[UPDATE] permission on the specified domain " +
                    "or DOMAIN_USER[UPDATE] permission on the specified environment " +
                    "or DOMAIN_USER[UPDATE] permission on the specified organization")
    @ApiResponses({
            @ApiResponse(code = 204, message = "User consents successfully revoked"),
            @ApiResponse(code = 500, message = "Internal server error")})
    public void delete(
            @PathParam("organizationId") String organizationId,
            @PathParam("environmentId") String environmentId,
            @PathParam("domain") String domain,
            @PathParam("user") String user,
            @QueryParam("clientId") String clientId,
            @Suspended final AsyncResponse response) {
        final User authenticatedUser = getAuthenticatedUser();

        RxJava2Adapter.monoToCompletable(checkAnyPermission_migrated(organizationId, environmentId, domain, Permission.DOMAIN_USER, Acl.UPDATE).then(domainService.findById_migrated(domain).switchIfEmpty(Mono.error(new DomainNotFoundException(domain))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Domain, CompletableSource>)__ -> {
                            if (clientId == null || clientId.isEmpty()) {
                                return RxJava2Adapter.monoToCompletable(scopeApprovalService.revokeByUser_migrated(domain, user, authenticatedUser));
                            }
                            return RxJava2Adapter.monoToCompletable(scopeApprovalService.revokeByUserAndClient_migrated(domain, user, clientId, authenticatedUser));
                        }).apply(y)))).then()))
                .subscribe(() -> response.resume(Response.noContent().build()), response::resume);
    }

    @Path("{consent}")
    public UserConsentResource getUserConsentResource() {
        return resourceContext.getResource(UserConsentResource.class);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getClient_migrated(domain, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<ApplicationEntity> getClient(String domain, String clientId) {
 return RxJava2Adapter.monoToSingle(getClient_migrated(domain, clientId));
}
private Mono<ApplicationEntity> getClient_migrated(String domain, String clientId) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationService.findByDomainAndClientId_migrated(domain, clientId).map(RxJavaReactorMigrationUtil.toJdkFunction(ApplicationEntity::new)).defaultIfEmpty(new ApplicationEntity("unknown-id", clientId, "unknown-client-name")).single())
                .cache());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getScope_migrated(domain, scopeKey))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<ScopeEntity> getScope(String domain, String scopeKey) {
 return RxJava2Adapter.monoToSingle(getScope_migrated(domain, scopeKey));
}
private Mono<ScopeEntity> getScope_migrated(String domain, String scopeKey) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(scopeService.findByDomainAndKey_migrated(domain, scopeKey).switchIfEmpty(scopeService.findByDomainAndKey_migrated(domain, getScopeBase(scopeKey)).map(RxJavaReactorMigrationUtil.toJdkFunction(entity -> {
                    // set the right scopeKey since the one returned by the service contains the scope definition without parameter
                    entity.setId("unknown-id");
                    entity.setKey(scopeKey);
                    return entity;
                }))).map(RxJavaReactorMigrationUtil.toJdkFunction(ScopeEntity::new)).defaultIfEmpty(new ScopeEntity("unknown-id", scopeKey, "unknown-scope-name", "unknown-scope-description")).single())
                .cache());
    }

    private String getScopeBase(String scope) {
        return scope.indexOf(':') > 0 ? scope.substring(0, scope.indexOf(':')) : scope;
    }
}

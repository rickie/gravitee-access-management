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
package io.gravitee.am.gateway.handler.oidc.resources.endpoint;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.common.vertx.utils.UriBuilderRequest;
import io.gravitee.am.gateway.handler.oauth2.exception.ResourceNotFoundException;
import io.gravitee.am.gateway.handler.oidc.service.clientregistration.DynamicClientRegistrationResponse;
import io.gravitee.am.gateway.handler.oidc.service.clientregistration.DynamicClientRegistrationService;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.common.http.MediaType;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.json.Json;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * This endpoint aim to access to client-id generated through the dynamic client registration protocol.
 * See <a href="https://openid.net/specs/openid-connect-registration-1_0.html">Openid Connect Dynamic Client Registration</a>
 * See <a href="https://tools.ietf.org/html/rfc7591"> OAuth 2.0 Dynamic Client Registration Protocol</a>
 *
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class DynamicClientAccessEndpoint extends DynamicClientRegistrationEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicClientAccessEndpoint.class);

    public DynamicClientAccessEndpoint(DynamicClientRegistrationService dcrService, ClientSyncService clientSyncService) {
        super(dcrService, clientSyncService);
    }

    /**
     * Read client_metadata.
     * See <a href="https://openid.net/specs/openid-connect-registration-1_0.html#ReadRequest">Read Request</a>
     * See <a href="https://openid.net/specs/openid-connect-registration-1_0.html#ReadResponse">Read Response</a>
     *
     * @param context
     */
    public void read(RoutingContext context) {
        LOGGER.debug("Dynamic client registration GET endpoint");

        this.getClient_migrated(context).map(RxJavaReactorMigrationUtil.toJdkFunction(DynamicClientRegistrationResponse::fromClient)).map(RxJavaReactorMigrationUtil.toJdkFunction(response -> {
                    //The Authorization Server need not include the registration access_token or client_uri unless they have been updated.
                    response.setRegistrationAccessToken(null);
                    response.setRegistrationClientUri(null);
                    return response;
                })).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(result -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .setStatusCode(HttpStatusCode.OK_200)
                                .end(Json.encodePrettily(result))), RxJavaReactorMigrationUtil.toJdkConsumer(context::fail));
    }

    /**
     * Patch client_metadata.
     * @param context
     */
    public void patch(RoutingContext context) {
        LOGGER.debug("Dynamic client registration PATCH endpoint");

        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.getClient_migrated(context))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Client, SingleSource<Client>>toJdkFunction(Single::just).apply(y)))))).flatMap(client->this.extractRequest_migrated(context).flatMap(request->dcrService.patch_migrated(client, request, UriBuilderRequest.resolveProxyRequest(context))).map(RxJavaReactorMigrationUtil.toJdkFunction(clientSyncService::addDynamicClientRegistred))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(client -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .setStatusCode(HttpStatusCode.OK_200)
                                .end(Json.encodePrettily(DynamicClientRegistrationResponse.fromClient(client)))), RxJavaReactorMigrationUtil.toJdkConsumer(context::fail));
    }

    /**
     * Update/Override client_metadata.
     * @param context
     */
    public void update(RoutingContext context) {
        LOGGER.debug("Dynamic client registration UPDATE endpoint");

        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.getClient_migrated(context))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Client, SingleSource<Client>>toJdkFunction(Single::just).apply(y)))))).flatMap(client->this.extractRequest_migrated(context).flatMap(request->dcrService.update_migrated(client, request, UriBuilderRequest.resolveProxyRequest(context))).map(RxJavaReactorMigrationUtil.toJdkFunction(clientSyncService::addDynamicClientRegistred))).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(client -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .setStatusCode(HttpStatusCode.OK_200)
                                .end(Json.encodePrettily(DynamicClientRegistrationResponse.fromClient(client)))), RxJavaReactorMigrationUtil.toJdkConsumer(context::fail));
    }

    /**
     * Delete client
     * @param context
     */
    public void delete(RoutingContext context) {
        LOGGER.debug("Dynamic client registration DELETE endpoint");

        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.getClient_migrated(context))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Client, SingleSource<Client>>toJdkFunction((io.gravitee.am.model.oidc.Client ident) -> RxJava2Adapter.monoToSingle(dcrService.delete_migrated(ident))).apply(y)))))).map(RxJavaReactorMigrationUtil.toJdkFunction(this.clientSyncService::removeDynamicClientRegistred)).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(client -> context.response().setStatusCode(HttpStatusCode.NO_CONTENT_204).end()), RxJavaReactorMigrationUtil.toJdkConsumer(context::fail));
    }

    /**
     * Renew client_secret
     * @param context
     */
    public void renewClientSecret(RoutingContext context) {
        LOGGER.debug("Dynamic client registration RENEW SECRET endpoint");

        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(this.getClient_migrated(context))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Client, SingleSource<Client>>toJdkFunction(Single::just).apply(y)))))).flatMap(toRenew->dcrService.renewSecret_migrated(toRenew, UriBuilderRequest.resolveProxyRequest(context))).map(RxJavaReactorMigrationUtil.toJdkFunction(clientSyncService::addDynamicClientRegistred)).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(client -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .setStatusCode(HttpStatusCode.OK_200)
                                .end(Json.encodePrettily(DynamicClientRegistrationResponse.fromClient(client)))), RxJavaReactorMigrationUtil.toJdkConsumer(context::fail));
    }

    
private Mono<Client> getClient_migrated(RoutingContext context) {
        String clientId = context.request().getParam("client_id");

        return this.clientSyncService.findByClientId_migrated(clientId).switchIfEmpty(Mono.error(new ResourceNotFoundException("client not found"))).map(RxJavaReactorMigrationUtil.toJdkFunction(Client::clone));
    }
}

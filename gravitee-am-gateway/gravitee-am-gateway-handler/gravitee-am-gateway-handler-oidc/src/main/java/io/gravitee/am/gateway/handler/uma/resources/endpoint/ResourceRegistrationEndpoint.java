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
package io.gravitee.am.gateway.handler.uma.resources.endpoint;

import static io.gravitee.am.gateway.handler.uma.constants.UMAConstants.*;

import io.gravitee.am.common.exception.oauth2.InvalidRequestException;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.gateway.handler.common.utils.ConstantKeys;
import io.gravitee.am.gateway.handler.common.vertx.utils.UriBuilderRequest;
import io.gravitee.am.gateway.handler.uma.resources.response.ResourceResponse;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.uma.Resource;
import io.gravitee.am.service.ResourceService;
import io.gravitee.am.service.exception.ResourceNotFoundException;
import io.gravitee.am.service.model.NewResource;
import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.common.http.MediaType;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * <pre>
 * This endpoint is part of the UMA 2.0 Protection API.
 * It enables the resource server (API) to put resources under the protection of the Authorization Server
 * on behalf of the resource owner, and manage them over time.
 * See <a href="https://docs.kantarainitiative.org/uma/wg/rec-oauth-uma-federated-authz-2.0.html#resource-registration-endpoint">here</a>
 * </pre>
 *
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class ResourceRegistrationEndpoint implements Handler<RoutingContext> {

    private ResourceService resourceService;
    private Domain domain;

    public ResourceRegistrationEndpoint(Domain domain, ResourceService resourceService) {
        this.domain = domain;
        this.resourceService = resourceService;
    }

    @Override
    public void handle(RoutingContext context) {
        JWT accessToken = context.get(ConstantKeys.TOKEN_CONTEXT_KEY);
        Client client = context.get(ConstantKeys.CLIENT_CONTEXT_KEY);

        RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(this.resourceService.listByDomainAndClientAndUser(domain.getId(), client.getId(), accessToken.getSub())).map(RxJavaReactorMigrationUtil.toJdkFunction(Resource::getId)))
                .collect(JsonArray::new, JsonArray::add)
                .subscribe(
                        buffer -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .setStatusCode(buffer.isEmpty()?HttpStatusCode.NO_CONTENT_204:HttpStatusCode.OK_200)
                                .end(Json.encodePrettily(buffer))
                        , context::fail
                );
    }

    public void create(RoutingContext context) {
        JWT accessToken = context.get(ConstantKeys.TOKEN_CONTEXT_KEY);
        Client client = context.get(ConstantKeys.CLIENT_CONTEXT_KEY);
        String basePath = UriBuilderRequest.resolveProxyRequest(context);

        RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.extractRequest(context)).flatMap(request->RxJava2Adapter.singleToMono(this.resourceService.create(request, domain.getId(), client.getId(), accessToken.getSub()))))
                .subscribe(
                        resource -> {
                            final String resourceLocation = resourceLocation(basePath, resource);
                            context.response()
                                    .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                    .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                    .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                    .putHeader(HttpHeaders.LOCATION, resourceLocation)
                                    .setStatusCode(HttpStatusCode.CREATED_201)
                                    .end(Json.encodePrettily(ResourceResponse.from(resource, resourceLocation)));
                        }
                        , context::fail
                );
    }

    public void get(RoutingContext context) {
        JWT accessToken = context.get(ConstantKeys.TOKEN_CONTEXT_KEY);
        Client client = context.get(ConstantKeys.CLIENT_CONTEXT_KEY);
        String resource_id = context.request().getParam(RESOURCE_ID);

        RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(this.resourceService.findByDomainAndClientAndUserAndResource(domain.getId(), client.getId(), accessToken.getSub(), resource_id)).switchIfEmpty(Mono.error(new ResourceNotFoundException(resource_id))))
                .subscribe(
                        resource -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .setStatusCode(HttpStatusCode.OK_200)
                                .end(Json.encodePrettily(ResourceResponse.from(resource)))
                        , context::fail
                );
    }

    /**
     * https://docs.kantarainitiative.org/uma/wg/rec-oauth-uma-federated-authz-2.0.html#reg-api
     * The spec state that if the resource can not be found, it must result in a 404.
     * By the way this may be better than a 403 to avoid confirming ids to a potential attacks.
     * @param context
     */
    public void update(RoutingContext context) {
        JWT accessToken = context.get(ConstantKeys.TOKEN_CONTEXT_KEY);
        Client client = context.get(ConstantKeys.CLIENT_CONTEXT_KEY);
        String resource_id = context.request().getParam(RESOURCE_ID);

        RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.extractRequest(context)).flatMap(request->RxJava2Adapter.singleToMono(this.resourceService.update(request, domain.getId(), client.getId(), accessToken.getSub(), resource_id))))
                .subscribe(
                        resource -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .setStatusCode(HttpStatusCode.OK_200)
                                .end(Json.encodePrettily(ResourceResponse.from(resource)))
                        , context::fail
                );
    }

    public void delete(RoutingContext context) {
        JWT accessToken = context.get(ConstantKeys.TOKEN_CONTEXT_KEY);
        Client client = context.get(ConstantKeys.CLIENT_CONTEXT_KEY);
        String resource_id = context.request().getParam(RESOURCE_ID);

        this.resourceService.delete(domain.getId(), client.getId(), accessToken.getSub(), resource_id)
                .subscribe(
                        () -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .setStatusCode(HttpStatusCode.NO_CONTENT_204)
                                .end()
                        , context::fail
                );
    }

    private Single<NewResource> extractRequest(RoutingContext context) {
        return RxJava2Adapter.monoToSingle(Mono.just(context.getBodyAsJson()).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<JsonObject, SingleSource<JsonObject>>toJdkFunction(this::bodyValidation).apply(v)))).map(RxJavaReactorMigrationUtil.toJdkFunction(body -> body.mapTo(NewResource.class))));
    }

    private Single<JsonObject> bodyValidation(JsonObject body) {
        //Only one field is required from the spec, others are tag as optional
        if (body == null || !body.containsKey("resource_scopes")) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRequestException("missing resource_scopes")));
        }
        return RxJava2Adapter.monoToSingle(Mono.just(body));
    }

    private String resourceLocation(String basePath, Resource resource) {
        return new StringBuilder()
                .append(basePath)
                .append(UMA_PATH)
                .append(RESOURCE_REGISTRATION_PATH)
                .append("/")
                .append(resource.getId())
                .toString();
    }
}

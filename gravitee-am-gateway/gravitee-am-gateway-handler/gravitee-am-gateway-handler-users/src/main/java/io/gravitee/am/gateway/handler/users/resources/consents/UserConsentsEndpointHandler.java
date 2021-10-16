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
package io.gravitee.am.gateway.handler.users.resources.consents;

import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.users.service.UserService;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.MediaType;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.json.Json;
import io.vertx.reactivex.ext.web.RoutingContext;
import java.util.Optional;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserConsentsEndpointHandler extends AbstractUserConsentEndpointHandler {

    public UserConsentsEndpointHandler(UserService userService, ClientSyncService clientSyncService, Domain domain) {
        super(userService, clientSyncService, domain);
    }

    /**
     * Retrieve consents for a user per application basis or for all applications
     */
    public void list(RoutingContext context) {
        final String userId = context.request().getParam("userId");
        final String clientId = context.request().getParam("clientId");

        RxJava2Adapter.monoToSingle(Mono.just(Optional.ofNullable(clientId)).flatMap(v->RxJava2Adapter.singleToMono((Single<Set<ScopeApproval>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Optional<String>, Single<Set<ScopeApproval>>>)optClient -> {
                    if (optClient.isPresent()) {
                        return RxJava2Adapter.monoToSingle(userService.consents_migrated(userId, optClient.get()));
                    }
                    return RxJava2Adapter.monoToSingle(userService.consents_migrated(userId));
                }).apply(v))))
                .subscribe(
                        scopeApprovals -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .end(Json.encodePrettily(scopeApprovals)),
                        context::fail);

    }

    /**
     * Revoke consents for a user per application basis or for all applications
     */
    public void revoke(RoutingContext context) {
        final String userId = context.request().getParam("userId");
        final String clientId = context.request().getParam("clientId");

        RxJava2Adapter.monoToCompletable(Mono.just(Optional.ofNullable(clientId)).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Optional<String>, CompletableSource>)optClient -> {
                    if (optClient.isPresent()) {
                        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getPrincipal_migrated(context))).flatMap(t->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<User, CompletableSource>toJdkFunction(principal -> RxJava2Adapter.monoToCompletable(userService.revokeConsents_migrated(userId, optClient.get(), principal))).apply(t)))).then());
                    }
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getPrincipal_migrated(context))).flatMap(t->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<User, CompletableSource>toJdkFunction(principal -> RxJava2Adapter.monoToCompletable(userService.revokeConsents_migrated(userId, principal))).apply(t)))).then());
                }).apply(y)))).then())
                .subscribe(
                        () -> context.response().setStatusCode(204).end(),
                        context::fail);

    }
}

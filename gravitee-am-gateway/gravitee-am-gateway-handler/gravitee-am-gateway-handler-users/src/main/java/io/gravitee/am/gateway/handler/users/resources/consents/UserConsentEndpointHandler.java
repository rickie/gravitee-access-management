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
package io.gravitee.am.gateway.handler.users.resources.consents;

import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.users.service.UserService;
import io.gravitee.am.model.Domain;
import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.MediaType;
import io.vertx.core.json.Json;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserConsentEndpointHandler extends AbstractUserConsentEndpointHandler {

    public UserConsentEndpointHandler(
            UserService userService, ClientSyncService clientSyncService, Domain domain) {
        super(userService, clientSyncService, domain);
    }

    /** Retrieve specific consent for a user */
    public void get(RoutingContext context) {
        final String consentId = context.request().getParam("consentId");
        userService
                .consent(consentId)
                .subscribe(
                        scopeApproval ->
                                context.response()
                                        .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                        .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                        .putHeader(
                                                HttpHeaders.CONTENT_TYPE,
                                                MediaType.APPLICATION_JSON)
                                        .end(Json.encodePrettily(scopeApproval)),
                        error -> context.fail(error));
    }

    /** Revoke specific consent for a user */
    public void revoke(RoutingContext context) {
        final String userId = context.request().getParam("userId");
        final String consentId = context.request().getParam("consentId");

        getPrincipal(context)
                .flatMapCompletable(
                        principal -> userService.revokeConsent(userId, consentId, principal))
                .subscribe(
                        () -> context.response().setStatusCode(204).end(),
                        error -> context.fail(error));
    }
}

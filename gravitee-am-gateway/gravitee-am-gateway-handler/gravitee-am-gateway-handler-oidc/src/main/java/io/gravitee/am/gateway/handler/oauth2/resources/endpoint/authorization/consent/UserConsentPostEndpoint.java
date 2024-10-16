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
package io.gravitee.am.gateway.handler.oauth2.resources.endpoint.authorization.consent;

import static io.gravitee.am.gateway.handler.common.vertx.utils.UriBuilderRequest.CONTEXT_PATH;

import io.gravitee.am.gateway.handler.common.vertx.utils.RequestUtils;
import io.gravitee.am.gateway.handler.common.vertx.utils.UriBuilderRequest;
import io.gravitee.common.http.HttpHeaders;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserConsentPostEndpoint implements Handler<RoutingContext> {

    private static final Logger logger = LoggerFactory.getLogger(UserConsentPostEndpoint.class);

    @Override
    public void handle(RoutingContext routingContext) {
        // consent has been processed, replay authorization request
        try {
            final String authorizationRequestUrl =
                    UriBuilderRequest.resolveProxyRequest(
                            routingContext.request(),
                            routingContext.get(CONTEXT_PATH) + "/oauth/authorize",
                            RequestUtils.getCleanedQueryParams(routingContext.request()),
                            true);
            doRedirect(routingContext.response(), authorizationRequestUrl);
        } catch (Exception e) {
            logger.error("An error occurs while handling authorization approval request", e);
            routingContext.fail(503);
        }
    }

    private void doRedirect(HttpServerResponse response, String url) {
        response.putHeader(HttpHeaders.LOCATION, url).setStatusCode(302).end();
    }
}

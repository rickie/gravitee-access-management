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
package io.gravitee.am.gateway.handler.ciba;

import io.gravitee.am.gateway.handler.api.ProtocolProvider;
import io.gravitee.am.gateway.handler.ciba.resources.handler.AuthenticationRequestFailureHandler;
import io.gravitee.am.gateway.handler.ciba.resources.handler.AuthenticationRequestParseRequestObjectHandler;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.oauth2.resources.auth.handler.ClientAuthHandler;
import io.gravitee.am.gateway.handler.oauth2.resources.handler.ExceptionHandler;
import io.gravitee.am.gateway.handler.oauth2.resources.handler.authorization.AuthorizationRequestParseClientHandler;
import io.gravitee.am.gateway.handler.oauth2.resources.handler.authorization.AuthorizationRequestParseProviderConfigurationHandler;
import io.gravitee.am.gateway.handler.oauth2.service.assertion.ClientAssertionService;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDDiscoveryService;
import io.gravitee.am.gateway.handler.oidc.service.jwk.JWKService;
import io.gravitee.am.gateway.handler.oidc.service.request.RequestObjectService;
import io.gravitee.am.model.Domain;
import io.gravitee.common.service.AbstractService;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CIBAProvider extends AbstractService<ProtocolProvider> implements ProtocolProvider {

    public static final String CIBA_PATH = "/ciba";

    private static final Logger LOGGER = LoggerFactory.getLogger(CIBAProvider.class);
    public static final String AUTHENTICATION_ENDPOINT = "/authenticate";

    @Autowired
    private Domain domain;

    @Autowired
    private Router router;

    @Autowired
    private Vertx vertx;

    @Autowired
    private ClientSyncService clientSyncService;

    @Autowired
    private ClientAssertionService clientAssertionService;

    @Autowired
    private JWKService jwkService;

    @Autowired
    private OpenIDDiscoveryService openIDDiscoveryService;

    @Autowired
    private RequestObjectService requestObjectService;

    @Override
    public String path() {
        return CIBA_PATH;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (isCibaEnabled()) {
            initRouter();
        }
    }

    private void initRouter() {
        // Create the CIBA router
        final Router cibaRouter = Router.router(vertx);

        // client auth handler
        final Handler<RoutingContext> clientAuthHandler = ClientAuthHandler.create(clientSyncService, clientAssertionService, jwkService, domain);

        // Authorization endpoint
        cibaRouter.route(HttpMethod.POST, AUTHENTICATION_ENDPOINT)
                .handler(clientAuthHandler)
                .handler(new AuthorizationRequestParseProviderConfigurationHandler(this.openIDDiscoveryService))
                .handler(new AuthenticationRequestParseRequestObjectHandler(this.requestObjectService, this.domain))
                .handler(ctx -> ctx.response().end())
                .failureHandler(new AuthenticationRequestFailureHandler()); // fin

        // error handler
        errorHandler(cibaRouter);

        // mount OAuth 2.0 router
        router.mountSubRouter(path(), cibaRouter);
    }

    private void errorHandler(Router router) {
        router.route().failureHandler(new ExceptionHandler());
    }


    private boolean isCibaEnabled() {
        return domain.useCiba();
    }
}

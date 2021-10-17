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
package io.gravitee.am.gateway.handler.root.resources.handler.login;

import com.google.common.net.HttpHeaders;
import io.gravitee.am.common.exception.authentication.AuthenticationException;
import io.gravitee.am.common.exception.oauth2.OAuth2Exception;
import io.gravitee.am.common.oauth2.Parameters;
import io.gravitee.am.common.web.UriBuilder;
import io.gravitee.am.gateway.handler.common.utils.ConstantKeys;
import io.gravitee.am.gateway.handler.common.vertx.utils.UriBuilderRequest;
import io.gravitee.am.gateway.policy.PolicyChainException;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.service.AuthenticationFlowContextService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.common.http.HttpStatusCode;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.adapter.rxjava.RxJava2Adapter;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * In case of login callback failures, the user will be redirected to the login page with error message
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class LoginCallbackFailureHandler implements Handler<RoutingContext> {

    private static final Logger logger = LoggerFactory.getLogger(LoginCallbackFailureHandler.class);

    private AuthenticationFlowContextService authenticationFlowContextService;

    public LoginCallbackFailureHandler(AuthenticationFlowContextService authenticationFlowContextService) {
        this.authenticationFlowContextService = authenticationFlowContextService;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        if (routingContext.failed()) {
            Throwable throwable = routingContext.failure();
            if (throwable instanceof OAuth2Exception
                    || throwable instanceof AbstractManagementException
                    || throwable instanceof AuthenticationException
                    || throwable instanceof PolicyChainException) {
                redirectToLoginPage(routingContext, throwable);
            } else {
                logger.error(throwable.getMessage(), throwable);
                if (routingContext.statusCode() != -1) {
                    routingContext
                            .response()
                            .setStatusCode(routingContext.statusCode())
                            .end();
                } else {
                    routingContext
                            .response()
                            .setStatusCode(HttpStatusCode.INTERNAL_SERVER_ERROR_500)
                            .end();
                }
            }
        }
    }

    private void redirectToLoginPage(RoutingContext context, Throwable throwable) {
        try {
            // logout user if exists
            if (context.user() != null) {
                // clear AuthenticationFlowContext. data of this context have a TTL so we can fire and forget in case on error.
                authenticationFlowContextService.clearContext_migrated(context.session().get(ConstantKeys.TRANSACTION_ID_KEY)).doOnError((error) -> logger.info("Deletion of some authentication flow data fails '{}'", error.getMessage())).as(RxJava2Adapter::monoToCompletable)
                        .subscribe();

                context.clearUser();
                context.session().destroy();
            }
            Client client = context.get(ConstantKeys.CLIENT_CONTEXT_KEY);
            Map<String, String> params = new HashMap<>();
            params.put(Parameters.CLIENT_ID, client.getClientId());
            params.put(ConstantKeys.ERROR_PARAM_KEY, "social_authentication_failed");
            params.put(ConstantKeys.ERROR_DESCRIPTION_PARAM_KEY, UriBuilder.encodeURIComponent(throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage()));
            String uri = UriBuilderRequest.resolveProxyRequest(context.request(), context.request().path().replaceFirst("/callback", ""), params);
            doRedirect(context.response(), uri);
        } catch (Exception ex) {
            logger.error("An error has occurred while redirecting to the login page", ex);
            // Note: we can't invoke context.fail cause it'll lead to infinite failure handling.
            context
                    .response()
                    .setStatusCode(HttpStatusCode.SERVICE_UNAVAILABLE_503)
                    .end();
        }
    }

    private void doRedirect(HttpServerResponse response, String url) {
        response.putHeader(HttpHeaders.LOCATION, url).setStatusCode(302).end();
    }
}

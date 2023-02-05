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
package io.gravitee.am.gateway.handler.root.resources.handler.user.register;

import static io.gravitee.am.common.utils.ConstantKeys.CLIENT_CONTEXT_KEY;

import io.gravitee.am.model.Domain;
import io.gravitee.am.model.login.LoginSettings;
import io.gravitee.am.model.oidc.Client;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RegisterAccessHandler implements Handler<RoutingContext> {

    private final Domain domain;

    public RegisterAccessHandler(Domain domain) {
        this.domain = domain;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        Client client = routingContext.get(CLIENT_CONTEXT_KEY);
        LoginSettings loginSettings = LoginSettings.getInstance(domain, client);
        if (loginSettings == null || !loginSettings.isRegisterEnabled()) {
            routingContext.fail(404);
            return;
        }
        routingContext.next();
    }
}

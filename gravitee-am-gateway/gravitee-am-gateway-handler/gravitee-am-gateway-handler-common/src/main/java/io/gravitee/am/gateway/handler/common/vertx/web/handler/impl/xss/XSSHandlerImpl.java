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
package io.gravitee.am.gateway.handler.common.vertx.web.handler.impl.xss;

import io.gravitee.am.gateway.handler.common.vertx.web.handler.XSSHandler;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class XSSHandlerImpl implements XSSHandler {

    private final String action;

    public XSSHandlerImpl(String action) {
        this.action = action;
    }

    @Override
    public void handle(RoutingContext context) {
        context.response().putHeader("X-XSS-Protection", action);
        context.next();
    }
}

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
package io.gravitee.am.gateway.handler.common.utils;

import static io.gravitee.am.common.utils.ConstantKeys.RISK_ASSESSMENT_KEY;

import io.gravitee.am.common.utils.ConstantKeys;
import io.gravitee.am.gateway.handler.common.vertx.web.auth.user.User;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RoutingContextHelper {
    private static final List<String> BLACKLIST_CONTEXT_ATTRIBUTES =
            Arrays.asList("X-XSRF-TOKEN", "_csrf", "__body-handled");

    /**
     * Return the {@link RoutingContext#data()} entries without technical attributes defined in
     * {@link #BLACKLIST_CONTEXT_ATTRIBUTES} If {@link RoutingContext#data()} doesn't contain {@link
     * ConstantKeys#USER_CONTEXT_KEY}, then the {@link RoutingContext#user()} is added if present
     *
     * @param routingContext
     * @return
     */
    public static Map<String, Object> getEvaluableAttributes(RoutingContext routingContext) {
        Map<String, Object> contextData = new HashMap<>(routingContext.data());

        Object user = routingContext.get(ConstantKeys.USER_CONTEXT_KEY);
        if (user != null) {
            contextData.put(ConstantKeys.USER_CONTEXT_KEY, user);
        } else if (routingContext.user() != null) {
            contextData.put(
                    ConstantKeys.USER_CONTEXT_KEY,
                    ((User) routingContext.user().getDelegate()).getUser());
        }

        if (routingContext.session() != null
                && routingContext.session().get(RISK_ASSESSMENT_KEY) != null) {
            contextData.put(RISK_ASSESSMENT_KEY, routingContext.session().get(RISK_ASSESSMENT_KEY));
        }

        // remove technical attributes
        BLACKLIST_CONTEXT_ATTRIBUTES.forEach(attribute -> contextData.remove(attribute));
        return contextData;
    }
}

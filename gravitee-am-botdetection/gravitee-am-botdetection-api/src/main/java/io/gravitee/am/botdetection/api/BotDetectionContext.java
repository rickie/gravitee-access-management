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
package io.gravitee.am.botdetection.api;

import io.vertx.reactivex.core.MultiMap;

import java.util.Optional;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class BotDetectionContext {

    private final MultiMap headers;
    private final MultiMap params;
    private final String pluginId;

    public BotDetectionContext(String pluginId, MultiMap headers, MultiMap params) {
        this.headers = headers;
        this.params = params;
        this.pluginId = pluginId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public Optional<String> getHeader(String name) {
        if (this.headers == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.headers.get(name));
    }

    public Optional<String> getParameter(String name) {
        if (this.params == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.params.get(name));
    }
}

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
package io.gravitee.am.repository.mongodb.management.internal.model.oidc;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SecurityProfileSettingsMongo {

    private boolean enablePlainFapi;
    private boolean enableFapiBrazil;

    public boolean isEnablePlainFapi() {
        return enablePlainFapi;
    }

    public void setEnablePlainFapi(boolean enablePlainFapi) {
        this.enablePlainFapi = enablePlainFapi;
    }

    public boolean isEnableFapiBrazil() {
        return enableFapiBrazil;
    }

    public void setEnableFapiBrazil(boolean enableFapiBrazil) {
        this.enableFapiBrazil = enableFapiBrazil;
    }
}

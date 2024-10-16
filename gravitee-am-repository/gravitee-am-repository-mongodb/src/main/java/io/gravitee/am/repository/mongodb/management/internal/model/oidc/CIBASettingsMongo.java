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

import java.util.List;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CIBASettingsMongo {
    private boolean enabled;
    private int authReqExpiry;
    private int tokenReqInterval;
    private int bindingMessageLength;
    private List<CIBASettingNotifierMongo> deviceNotifiers;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getAuthReqExpiry() {
        return authReqExpiry;
    }

    public void setAuthReqExpiry(int authReqExpiry) {
        this.authReqExpiry = authReqExpiry;
    }

    public int getTokenReqInterval() {
        return tokenReqInterval;
    }

    public void setTokenReqInterval(int tokenReqInterval) {
        this.tokenReqInterval = tokenReqInterval;
    }

    public int getBindingMessageLength() {
        return bindingMessageLength;
    }

    public void setBindingMessageLength(int bindingMessageLength) {
        this.bindingMessageLength = bindingMessageLength;
    }

    public List<CIBASettingNotifierMongo> getDeviceNotifiers() {
        return deviceNotifiers;
    }

    public void setDeviceNotifiers(List<CIBASettingNotifierMongo> deviceNotifiers) {
        this.deviceNotifiers = deviceNotifiers;
    }
}

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
package io.gravitee.am.gateway.handler.oidc.model.jwk;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * See <a href="https://tools.ietf.org/html/rfc7638#section-3.2">3.2. JWK Members Used in the
 * Thumbprint Computation</a>
 *
 * <p>The required members for a Symmetric public key, in lexicographic order, are: - "kty" - "k"
 *
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class OCTKey extends JWK {

    @JsonProperty("k")
    private String k;

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public static OCTKey from(io.gravitee.am.model.jose.OCTKey source) {
        OCTKey octKey = new OCTKey();
        octKey.setK(source.getK());
        octKey.copy(source);
        return octKey;
    }
}

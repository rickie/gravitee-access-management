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
 * See <a href="https://tools.ietf.org/html/rfc8037#appendix-A.3">JWK Thumbprint
 * Canonicalization</a>
 *
 * <p>The required members for an Octet Key Pair public key, in lexicographic order, are: - "crv" -
 * "kty" - "x"
 *
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class OKPKey extends JWK {

    @JsonProperty("crv")
    private String crv;

    @JsonProperty("x")
    private String x;

    public String getCrv() {
        return crv;
    }

    public void setCrv(String crv) {
        this.crv = crv;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public static OKPKey from(io.gravitee.am.model.jose.OKPKey source) {
        OKPKey okpKey = new OKPKey();
        okpKey.setCrv(source.getCrv());
        okpKey.setX(source.getX());
        okpKey.copy(source);
        return okpKey;
    }
}

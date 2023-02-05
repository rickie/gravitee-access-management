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
package io.gravitee.am.gateway.handler.oauth2.service.response;

import io.gravitee.am.common.oauth2.Parameters;
import io.gravitee.am.common.web.UriBuilder;

import java.net.URISyntaxException;

/**
 * See <a href="https://tools.ietf.org/html/rfc6749#section-4.1.2">4.1.2. Authorization Response</a>
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AuthorizationCodeResponse extends AuthorizationResponse {

    /**
     * REQUIRED. The authorization code generated by the authorization server. The authorization
     * code MUST expire shortly after it is issued to mitigate the risk of leaks. A maximum
     * authorization code lifetime of 10 minutes is RECOMMENDED.
     *
     * <p>The client MUST NOT use the authorization code more than once. If an authorization code is
     * used more than once, the authorization server MUST deny the request and SHOULD revoke (when
     * possible) all tokens previously issued based on that authorization code. The authorization
     * code is bound to the client identifier and redirection URI.
     */
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String buildRedirectUri() throws URISyntaxException {
        UriBuilder uriBuilder = UriBuilder.fromURIString(getRedirectUri());
        uriBuilder.addParameter(Parameters.CODE, getCode());
        if (getState() != null) {
            uriBuilder.addParameter(Parameters.STATE, getState());
        }
        return uriBuilder.buildString();
    }
}

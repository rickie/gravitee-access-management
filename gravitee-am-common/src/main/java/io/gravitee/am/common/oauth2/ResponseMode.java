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
package io.gravitee.am.common.oauth2;

/**
 * OAuth2 Response Modes
 *
 * <p>See <a
 * href="https://openid.net/specs/oauth-v2-multiple-response-types-1_0.html#ResponseModes">OAuth 2.0
 * Response Modes</a>
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ResponseMode {

    /**
     * In this mode, Authorization Response parameters are encoded in the query string added to the
     * redirect_uri when redirecting back to the Client.
     */
    String QUERY = "query";

    /**
     * In this mode, Authorization Response parameters are encoded in the fragment added to the
     * redirect_uri when redirecting back to the Client.
     */
    String FRAGMENT = "fragment";

    /**
     * In this mode, Authorization Response parameters are encoded as HTML form values that are
     * auto-submitted in the User Agent, and thus are transmitted via the HTTP POST method to the
     * Client, with the result parameters being encoded in the body using the
     * application/x-www-form-urlencoded format.
     */
    String FORM_POST = "form_post";
}

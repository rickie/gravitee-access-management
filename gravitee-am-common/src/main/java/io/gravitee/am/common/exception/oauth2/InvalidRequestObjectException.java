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
package io.gravitee.am.common.exception.oauth2;

/**
 * invalid_request_object The request parameter contains an invalid Request Object.
 *
 * <p>See <a href="https://openid.net/specs/openid-connect-core-1_0.html#AuthError">3.1.2.6.
 * Authentication Error Response</a>
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class InvalidRequestObjectException extends OAuth2Exception {

    public InvalidRequestObjectException() {
        super();
    }

    public InvalidRequestObjectException(String message) {
        super(message);
    }

    @Override
    public String getOAuth2ErrorCode() {
        return "invalid_request_object";
    }
}

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
package io.gravitee.am.service.exception;

import io.gravitee.am.common.exception.oauth2.OAuth2Exception;

/**
 * Return invalid_grant when permission ticket is not found or not valid.
 * https://docs.kantarainitiative.org/uma/wg/rec-oauth-uma-grant-2.0.html#authorization-failure
 *
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 */
public class InvalidPermissionTicketException extends OAuth2Exception {

    public InvalidPermissionTicketException() {
        super("Missing or invalid permission ticket.");
    }

    @Override
    public String getOAuth2ErrorCode() {
        return "invalid_grant";
    }
}

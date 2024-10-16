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
package io.gravitee.am.common.scim;

import java.util.Arrays;
import java.util.List;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface Schema {

    String SCHEMA_URI_CORE = "urn:ietf:params:scim:schemas:core:2.0";
    String SCHEMA_URI_USER = "urn:ietf:params:scim:schemas:core:2.0:User";
    String SCHEMA_URI_ENTERPRISE_USER =
            "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User";
    String SCHEMA_URI_CUSTOM_USER = "urn:ietf:params:scim:schemas:extension:custom:2.0:User";
    String SCHEMA_URI_GROUP = "urn:ietf:params:scim:schemas:core:2.0:Group";

    static List<String> supportedSchemas() {
        return Arrays.asList(SCHEMA_URI_USER, SCHEMA_URI_ENTERPRISE_USER, SCHEMA_URI_CUSTOM_USER);
    }
}

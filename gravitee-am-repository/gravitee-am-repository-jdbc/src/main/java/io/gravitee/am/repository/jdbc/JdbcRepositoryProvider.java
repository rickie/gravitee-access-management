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
package io.gravitee.am.repository.jdbc;

import io.gravitee.am.repository.jdbc.management.ManagementRepositoryConfiguration;
import io.gravitee.am.repository.jdbc.oauth2.OAuth2RepositoryConfiguration;
import io.gravitee.platform.repository.api.RepositoryProvider;
import io.gravitee.platform.repository.api.Scope;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class JdbcRepositoryProvider implements RepositoryProvider {
    @Override
    public String type() {
        return "jdbc";
    }

    @Override
    public Scope[] scopes() {
        return new Scope[] {Scope.MANAGEMENT, Scope.OAUTH2};
    }

    @Override
    public Class<?> configuration(Scope scope) {
        switch (scope) {
            case MANAGEMENT:
                return ManagementRepositoryConfiguration.class;
            case OAUTH2:
                return OAuth2RepositoryConfiguration.class;
        }
        return null;
    }
}

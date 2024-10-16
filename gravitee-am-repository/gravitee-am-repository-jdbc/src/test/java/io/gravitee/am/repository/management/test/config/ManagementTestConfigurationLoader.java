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
package io.gravitee.am.repository.management.test.config;

import io.gravitee.am.repository.jdbc.common.AbstractTestRepositoryConfiguration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "io.gravitee.am.repository.jdbc.management.api.spring")
@ComponentScan({
    "io.gravitee.am.repository.jdbc.management.api",
    "io.gravitee.am.repository.jdbc.common"
})
public class ManagementTestConfigurationLoader extends AbstractTestRepositoryConfiguration {}

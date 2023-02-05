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
package io.gravitee.am.plugins.policy.spring;

import io.gravitee.am.plugins.handlers.api.core.ConfigurationFactory;
import io.gravitee.am.plugins.handlers.api.core.impl.ConfigurationFactoryImpl;
import io.gravitee.am.plugins.policy.core.PolicyPluginManager;
import io.gravitee.am.plugins.policy.core.impl.PolicyPluginManagerImpl;
import io.gravitee.plugin.policy.spring.PolicyPluginConfiguration;
import io.gravitee.policy.api.PolicyConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Import(PolicyPluginConfiguration.class)
@Configuration
public class PolicySpringConfiguration {

    @Bean
    public PolicyPluginManager policyPluginManager() {
        return new PolicyPluginManagerImpl();
    }

    @Bean
    public ConfigurationFactory<PolicyConfiguration> policyConfigurationFactory() {
        return new ConfigurationFactoryImpl<>();
    }
}

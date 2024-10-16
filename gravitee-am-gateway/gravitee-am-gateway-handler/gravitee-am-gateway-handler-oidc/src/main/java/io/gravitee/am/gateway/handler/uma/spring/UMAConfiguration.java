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
package io.gravitee.am.gateway.handler.uma.spring;

import io.gravitee.am.gateway.handler.api.ProtocolConfiguration;
import io.gravitee.am.gateway.handler.api.ProtocolProvider;
import io.gravitee.am.gateway.handler.uma.UMAProvider;
import io.gravitee.am.gateway.handler.uma.policy.DefaultRulesEngine;
import io.gravitee.am.gateway.handler.uma.policy.RulesEngine;
import io.gravitee.am.gateway.handler.uma.service.discovery.UMADiscoveryService;
import io.gravitee.am.gateway.handler.uma.service.discovery.impl.UMADiscoveryServiceImpl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@Configuration
public class UMAConfiguration implements ProtocolConfiguration {

    @Bean
    public UMADiscoveryService umaDiscoveryService() {
        return new UMADiscoveryServiceImpl();
    }

    @Bean
    public ProtocolProvider umaProvider() {
        return new UMAProvider();
    }

    @Bean
    public RulesEngine rulesEngine() {
        return new DefaultRulesEngine();
    }
}

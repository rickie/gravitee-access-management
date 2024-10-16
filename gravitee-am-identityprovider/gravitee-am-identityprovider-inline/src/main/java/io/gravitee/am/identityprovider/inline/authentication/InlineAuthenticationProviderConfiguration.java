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
package io.gravitee.am.identityprovider.inline.authentication;

import io.gravitee.am.identityprovider.inline.InlineIdentityProviderConfiguration;
import io.gravitee.am.identityprovider.inline.authentication.provisioning.InlineInMemoryUserDetailsManager;
import io.gravitee.am.service.authentication.crypto.password.NoOpPasswordEncoder;
import io.gravitee.am.service.authentication.crypto.password.PasswordEncoder;
import io.gravitee.am.service.authentication.crypto.password.bcrypt.BCryptPasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
public class InlineAuthenticationProviderConfiguration {

    @Autowired private InlineIdentityProviderConfiguration configuration;

    @Bean
    public PasswordEncoder passwordEncoder() {
        if (configuration.getPasswordEncoder() != null
                && "BCrypt".equals(configuration.getPasswordEncoder())) {
            return new BCryptPasswordEncoder();
        }
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public InlineInMemoryUserDetailsManager userDetailsService() {
        return new InlineInMemoryUserDetailsManager();
    }
}

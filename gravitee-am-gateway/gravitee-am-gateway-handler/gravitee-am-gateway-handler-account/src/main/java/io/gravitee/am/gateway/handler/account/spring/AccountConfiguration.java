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
package io.gravitee.am.gateway.handler.account.spring;

import io.gravitee.am.gateway.handler.account.services.AccountService;
import io.gravitee.am.gateway.handler.account.services.impl.AccountServiceImpl;
import io.gravitee.am.gateway.handler.api.ProtocolConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Donald Courtney (donald.courtney at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
public class AccountConfiguration implements ProtocolConfiguration {

    @Bean
    public AccountService accountService() {
        return new AccountServiceImpl();
    }
}

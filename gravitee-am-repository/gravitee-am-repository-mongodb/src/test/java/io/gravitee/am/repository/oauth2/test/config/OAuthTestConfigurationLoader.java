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
package io.gravitee.am.repository.oauth2.test.config;

import com.mongodb.reactivestreams.client.MongoDatabase;

import io.gravitee.am.repository.mongodb.MongodbProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Configuration
@ComponentScan({
    "io.gravitee.am.repository.mongodb.oauth2",
    "io.gravitee.am.repository.mongodb.common",
    "io.gravitee.am.repository.mongodb.provider"
})
public class OAuthTestConfigurationLoader {

    @Bean
    public MongodbProvider embeddedClient() {
        return new MongodbProvider("test-am-oauth2");
    }

    @Bean(name = "oauth2MongoTemplate")
    public MongoDatabase mongoOperations() {
        return embeddedClient().mongoDatabase();
    }
}

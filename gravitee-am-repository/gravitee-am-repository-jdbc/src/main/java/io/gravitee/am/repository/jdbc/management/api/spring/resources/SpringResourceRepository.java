/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.repository.jdbc.management.api.spring.resources;


import io.gravitee.am.repository.jdbc.management.api.model.JdbcResource;



import java.util.List;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringResourceRepository extends RxJava2CrudRepository<JdbcResource, String> {

      
Mono<Long> countByDomain_migrated(@Param(value = "domain")
String domain);

      
Flux<JdbcResource> findByIdIn_migrated(@Param(value = "ids")
List<String> resources);

      
Flux<JdbcResource> findByDomainAndClientAndUser_migrated(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "uid")
String user);

      
Flux<JdbcResource> findByDomainAndClientAndResources_migrated(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "ids")
List<String> resources);

      
Mono<JdbcResource> findByDomainAndClientAndUserIdAndResource_migrated(@Param(value = "domain")
String domain, @Param(value = "client")
String client, @Param(value = "uid")
String user, @Param(value = "rid")
String resource);
}

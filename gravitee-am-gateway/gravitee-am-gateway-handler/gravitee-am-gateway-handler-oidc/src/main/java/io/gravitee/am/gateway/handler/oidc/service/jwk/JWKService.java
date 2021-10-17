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
package io.gravitee.am.gateway.handler.oidc.service.jwk;


import io.gravitee.am.model.jose.JWK;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.oidc.JWKSet;


import java.util.function.Predicate;

import reactor.core.publisher.Mono;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface JWKService {

      
Mono<JWKSet> getKeys_migrated();
      
Mono<JWKSet> getKeys_migrated(Client client);
      
Mono<JWKSet> getDomainPrivateKeys_migrated();
      
Mono<JWKSet> getKeys_migrated(String jwksUri);
      
Mono<JWK> getKey_migrated(JWKSet jwkSet, String kid);
      
Mono<JWK> filter_migrated(JWKSet jwkSet, Predicate<JWK> filter);
}

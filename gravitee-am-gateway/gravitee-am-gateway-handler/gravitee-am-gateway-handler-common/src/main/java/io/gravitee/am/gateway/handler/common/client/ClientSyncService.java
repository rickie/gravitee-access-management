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
package io.gravitee.am.gateway.handler.common.client;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ClientSyncService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Client> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Client> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientId_migrated(clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Client> findByClientId(String clientId) {
    return RxJava2Adapter.monoToMaybe(findByClientId_migrated(clientId));
}
default Mono<Client> findByClientId_migrated(String clientId) {
    return RxJava2Adapter.maybeToMono(findByClientId(clientId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientId_migrated(domain, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Client> findByDomainAndClientId(String domain, String clientId) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientId_migrated(domain, clientId));
}
default Mono<Client> findByDomainAndClientId_migrated(String domain, String clientId) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientId(domain, clientId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTemplates_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<Client>> findTemplates() {
    return RxJava2Adapter.monoToSingle(findTemplates_migrated());
}
default Mono<List<Client>> findTemplates_migrated() {
    return RxJava2Adapter.singleToMono(findTemplates());
}

    Client addDynamicClientRegistred(Client client);

    Client removeDynamicClientRegistred(Client client);
}

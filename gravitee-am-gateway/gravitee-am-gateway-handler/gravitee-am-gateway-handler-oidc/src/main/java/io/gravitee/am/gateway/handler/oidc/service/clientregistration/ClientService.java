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
package io.gravitee.am.gateway.handler.oidc.service.clientregistration;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.service.ApplicationService;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * NOTE : this service must only be used in an OpenID Connect context
 * Use the {@link ApplicationService} for management purpose
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface ClientService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Client> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Client> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Client> create(Client client) {
    return RxJava2Adapter.monoToSingle(create_migrated(client));
}
default Mono<Client> create_migrated(Client client) {
    return RxJava2Adapter.singleToMono(create(client));
}

      
Mono<Client> renewClientSecret_migrated(String domain, String id, User principal);

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(clientId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String clientId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(clientId, principal));
}
default Mono<Void> delete_migrated(String clientId, User principal) {
    return RxJava2Adapter.completableToMono(delete(clientId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Client> update(Client client) {
    return RxJava2Adapter.monoToSingle(update_migrated(client));
}
default Mono<Client> update_migrated(Client client) {
    return RxJava2Adapter.singleToMono(update(client));
}

      default Mono<Client> renewClientSecret_migrated(String domain, String id) {
          return renewClientSecret_migrated(domain, id, null);
      }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String clientId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(clientId));
}default Mono<Void> delete_migrated(String clientId) {
        return RxJava2Adapter.completableToMono(delete(clientId, null));
    }

}

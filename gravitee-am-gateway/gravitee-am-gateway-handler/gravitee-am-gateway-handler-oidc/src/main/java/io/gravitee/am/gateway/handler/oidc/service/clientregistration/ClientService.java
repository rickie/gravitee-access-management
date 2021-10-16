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
default io.reactivex.Maybe<io.gravitee.am.model.oidc.Client> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oidc.Client> create(io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToSingle(create_migrated(client));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> create_migrated(Client client) {
    return RxJava2Adapter.singleToMono(create(client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.renewClientSecret_migrated(domain, id, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oidc.Client> renewClientSecret(java.lang.String domain, java.lang.String id, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(renewClientSecret_migrated(domain, id, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> renewClientSecret_migrated(String domain, String id, User principal) {
    return RxJava2Adapter.singleToMono(renewClientSecret(domain, id, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(clientId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String clientId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(clientId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String clientId, User principal) {
    return RxJava2Adapter.completableToMono(delete(clientId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oidc.Client> update(io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToSingle(update_migrated(client));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> update_migrated(Client client) {
    return RxJava2Adapter.singleToMono(update(client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.renewClientSecret_migrated(domain, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oidc.Client> renewClientSecret(java.lang.String domain, java.lang.String id) {
    return RxJava2Adapter.monoToSingle(renewClientSecret_migrated(domain, id));
}default Mono<Client> renewClientSecret_migrated(String domain, String id) {
        return RxJava2Adapter.singleToMono(renewClientSecret(domain, id, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String clientId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(clientId));
}default Mono<Void> delete_migrated(String clientId) {
        return RxJava2Adapter.completableToMono(delete(clientId, null));
    }

}

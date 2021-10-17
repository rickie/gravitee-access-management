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
import io.gravitee.am.gateway.handler.oidc.service.clientregistration.DynamicClientRegistrationRequest;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface DynamicClientRegistrationService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(request, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Client> create(DynamicClientRegistrationRequest request, String basePath) {
    return RxJava2Adapter.monoToSingle(create_migrated(request, basePath));
}
default Mono<Client> create_migrated(DynamicClientRegistrationRequest request, String basePath) {
    return RxJava2Adapter.singleToMono(create(request, basePath));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(toPatch, request, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Client> patch(Client toPatch, DynamicClientRegistrationRequest request, String basePath) {
    return RxJava2Adapter.monoToSingle(patch_migrated(toPatch, request, basePath));
}
default Mono<Client> patch_migrated(Client toPatch, DynamicClientRegistrationRequest request, String basePath) {
    return RxJava2Adapter.singleToMono(patch(toPatch, request, basePath));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(toUpdate, request, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Client> update(Client toUpdate, DynamicClientRegistrationRequest request, String basePath) {
    return RxJava2Adapter.monoToSingle(update_migrated(toUpdate, request, basePath));
}
default Mono<Client> update_migrated(Client toUpdate, DynamicClientRegistrationRequest request, String basePath) {
    return RxJava2Adapter.singleToMono(update(toUpdate, request, basePath));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.delete_migrated(toDelete))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Client> delete(Client toDelete) {
    return RxJava2Adapter.monoToSingle(delete_migrated(toDelete));
}
default Mono<Client> delete_migrated(Client toDelete) {
    return RxJava2Adapter.singleToMono(delete(toDelete));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.renewSecret_migrated(toRenew, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Client> renewSecret(Client toRenew, String basePath) {
    return RxJava2Adapter.monoToSingle(renewSecret_migrated(toRenew, basePath));
}
default Mono<Client> renewSecret_migrated(Client toRenew, String basePath) {
    return RxJava2Adapter.singleToMono(renewSecret(toRenew, basePath));
}
}

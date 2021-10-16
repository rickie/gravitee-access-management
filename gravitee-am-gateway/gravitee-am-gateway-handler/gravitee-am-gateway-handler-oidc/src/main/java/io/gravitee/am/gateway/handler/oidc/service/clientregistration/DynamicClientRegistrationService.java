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
import io.gravitee.am.model.oidc.Client;

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface DynamicClientRegistrationService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(request, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oidc.Client> create(io.gravitee.am.gateway.handler.oidc.service.clientregistration.DynamicClientRegistrationRequest request, java.lang.String basePath) {
    return RxJava2Adapter.monoToSingle(create_migrated(request, basePath));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> create_migrated(DynamicClientRegistrationRequest request, String basePath) {
    return RxJava2Adapter.singleToMono(create(request, basePath));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(toPatch, request, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oidc.Client> patch(io.gravitee.am.model.oidc.Client toPatch, io.gravitee.am.gateway.handler.oidc.service.clientregistration.DynamicClientRegistrationRequest request, java.lang.String basePath) {
    return RxJava2Adapter.monoToSingle(patch_migrated(toPatch, request, basePath));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> patch_migrated(Client toPatch, DynamicClientRegistrationRequest request, String basePath) {
    return RxJava2Adapter.singleToMono(patch(toPatch, request, basePath));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(toUpdate, request, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oidc.Client> update(io.gravitee.am.model.oidc.Client toUpdate, io.gravitee.am.gateway.handler.oidc.service.clientregistration.DynamicClientRegistrationRequest request, java.lang.String basePath) {
    return RxJava2Adapter.monoToSingle(update_migrated(toUpdate, request, basePath));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> update_migrated(Client toUpdate, DynamicClientRegistrationRequest request, String basePath) {
    return RxJava2Adapter.singleToMono(update(toUpdate, request, basePath));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.delete_migrated(toDelete))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oidc.Client> delete(io.gravitee.am.model.oidc.Client toDelete) {
    return RxJava2Adapter.monoToSingle(delete_migrated(toDelete));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> delete_migrated(Client toDelete) {
    return RxJava2Adapter.singleToMono(delete(toDelete));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.renewSecret_migrated(toRenew, basePath))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oidc.Client> renewSecret(io.gravitee.am.model.oidc.Client toRenew, java.lang.String basePath) {
    return RxJava2Adapter.monoToSingle(renewSecret_migrated(toRenew, basePath));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oidc.Client> renewSecret_migrated(Client toRenew, String basePath) {
    return RxJava2Adapter.singleToMono(renewSecret(toRenew, basePath));
}
}

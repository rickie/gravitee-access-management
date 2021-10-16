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
package io.gravitee.am.management.service;

import io.gravitee.am.identityprovider.api.UserProvider;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.common.service.Service;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface IdentityProviderManager extends Service<IdentityProviderManager> {

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.identityprovider.api.UserProvider> getUserProvider(java.lang.String userProvider) {
    return RxJava2Adapter.monoToMaybe(getUserProvider_migrated(userProvider));
}
default reactor.core.publisher.Mono<io.gravitee.am.identityprovider.api.UserProvider> getUserProvider_migrated(String userProvider) {
    return RxJava2Adapter.maybeToMono(getUserProvider(userProvider));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.IdentityProvider> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.IdentityProvider> create_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId));
}

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.IdentityProvider> create(java.lang.String domain) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.IdentityProvider> create_migrated(String domain) {
    return RxJava2Adapter.singleToMono(create(domain));
}

    boolean userProviderExists(String identityProviderId);

    void setListener(InMemoryIdentityProviderListener listener);

    void loadIdentityProviders();

}

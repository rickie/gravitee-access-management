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
package io.gravitee.am.gateway.handler.oauth2.service.consent;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Single;
import java.util.List;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserConsentService {

      @Deprecated  
default io.reactivex.Single<java.util.Set<java.lang.String>> checkConsent(io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(checkConsent_migrated(client, user));
}
default reactor.core.publisher.Mono<java.util.Set<java.lang.String>> checkConsent_migrated(Client client, io.gravitee.am.model.User user) {
    return RxJava2Adapter.singleToMono(checkConsent(client, user));
}

      @Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.oauth2.ScopeApproval>> saveConsent(io.gravitee.am.model.oidc.Client client, java.util.List<io.gravitee.am.model.oauth2.ScopeApproval> approvals, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(saveConsent_migrated(client, approvals, principal));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.oauth2.ScopeApproval>> saveConsent_migrated(Client client, List<ScopeApproval> approvals, User principal) {
    return RxJava2Adapter.singleToMono(saveConsent(client, approvals, principal));
}

      @Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.oauth2.Scope>> getConsentInformation(java.util.Set<java.lang.String> consent) {
    return RxJava2Adapter.monoToSingle(getConsentInformation_migrated(consent));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.oauth2.Scope>> getConsentInformation_migrated(Set<String> consent) {
    return RxJava2Adapter.singleToMono(getConsentInformation(consent));
}
}

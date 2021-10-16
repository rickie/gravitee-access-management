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
package io.gravitee.am.repository.management.api;

import com.google.errorprone.annotations.InlineMe;

import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.repository.common.CrudRepository;



import java.util.List;

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ScopeRepository extends CrudRepository<Scope, String> {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.oauth2.Scope>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.oauth2.Scope>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.oauth2.Scope>> search(java.lang.String domain, java.lang.String query, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(domain, query, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.oauth2.Scope>> search_migrated(String domain, String query, int page, int size) {
    return RxJava2Adapter.singleToMono(search(domain, query, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndKey_migrated(domain, key))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.oauth2.Scope> findByDomainAndKey(java.lang.String domain, java.lang.String key) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndKey_migrated(domain, key));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.Scope> findByDomainAndKey_migrated(String domain, String key) {
    return RxJava2Adapter.maybeToMono(findByDomainAndKey(domain, key));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndKeys_migrated(domain, keys))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.oauth2.Scope> findByDomainAndKeys(java.lang.String domain, java.util.List<java.lang.String> keys) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndKeys_migrated(domain, keys));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.oauth2.Scope> findByDomainAndKeys_migrated(String domain, List<String> keys) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndKeys(domain, keys));
}
}

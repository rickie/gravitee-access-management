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
package io.gravitee.am.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.service.model.*;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface ScopeService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.oauth2.Scope> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.Scope> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, scope, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oauth2.Scope> create(java.lang.String domain, io.gravitee.am.service.model.NewScope scope, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, scope, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.Scope> create_migrated(String domain, NewScope scope, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, scope, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, scope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oauth2.Scope> create(java.lang.String domain, io.gravitee.am.service.model.NewSystemScope scope) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, scope));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.Scope> create_migrated(String domain, NewSystemScope scope) {
    return RxJava2Adapter.singleToMono(create(domain, scope));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.oauth2.Scope>> findByDomain(java.lang.String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.oauth2.Scope>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndKey_migrated(domain, scopeKey))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.oauth2.Scope> findByDomainAndKey(java.lang.String domain, java.lang.String scopeKey) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndKey_migrated(domain, scopeKey));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.Scope> findByDomainAndKey_migrated(String domain, String scopeKey) {
    return RxJava2Adapter.maybeToMono(findByDomainAndKey(domain, scopeKey));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomainAndKeys_migrated(domain, scopeKeys))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.oauth2.Scope>> findByDomainAndKeys(java.lang.String domain, java.util.List<java.lang.String> scopeKeys) {
    return RxJava2Adapter.monoToSingle(findByDomainAndKeys_migrated(domain, scopeKeys));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.oauth2.Scope>> findByDomainAndKeys_migrated(String domain, List<String> scopeKeys) {
    return RxJava2Adapter.singleToMono(findByDomainAndKeys(domain, scopeKeys));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(domain, id, patchScope, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oauth2.Scope> patch(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.PatchScope patchScope, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domain, id, patchScope, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.Scope> patch_migrated(String domain, String id, PatchScope patchScope, User principal) {
    return RxJava2Adapter.singleToMono(patch(domain, id, patchScope, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateScope, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oauth2.Scope> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateScope updateScope, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateScope, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.Scope> update_migrated(String domain, String id, UpdateScope updateScope, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateScope, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateScope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oauth2.Scope> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateSystemScope updateScope) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateScope));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.oauth2.Scope> update_migrated(String domain, String id, UpdateSystemScope updateScope) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateScope));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(scopeId, force, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String scopeId, boolean force, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(scopeId, force, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String scopeId, boolean force, User principal) {
    return RxJava2Adapter.completableToMono(delete(scopeId, force, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<io.gravitee.am.model.oauth2.Scope>> search(java.lang.String domain, java.lang.String query, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(domain, query, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<io.gravitee.am.model.oauth2.Scope>> search_migrated(String domain, String query, int page, int size) {
    return RxJava2Adapter.singleToMono(search(domain, query, page, size));
}

    /**
     * Throw InvalidClientMetadataException if null or empty, or contains unknown scope.
     * @param scopes Array of scope to validate.
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.validateScope_migrated(domain, scopes))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Boolean> validateScope(java.lang.String domain, java.util.List<java.lang.String> scopes) {
    return RxJava2Adapter.monoToSingle(validateScope_migrated(domain, scopes));
}
default reactor.core.publisher.Mono<java.lang.Boolean> validateScope_migrated(String domain, List<String> scopes) {
    return RxJava2Adapter.singleToMono(validateScope(domain, scopes));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, scope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oauth2.Scope> create(java.lang.String domain, io.gravitee.am.service.model.NewScope scope) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, scope));
}default Mono<Scope> create_migrated(String domain, NewScope scope) {
        return RxJava2Adapter.singleToMono(create(domain, scope, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(domain, id, patchScope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oauth2.Scope> patch(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.PatchScope patchScope) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domain, id, patchScope));
}default Mono<Scope> patch_migrated(String domain, String id, PatchScope patchScope) {
        return RxJava2Adapter.singleToMono(patch(domain, id, patchScope, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateScope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.oauth2.Scope> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateScope updateScope) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateScope));
}default Mono<Scope> update_migrated(String domain, String id, UpdateScope updateScope) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateScope, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(scopeId, force))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String scopeId, boolean force) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(scopeId, force));
}default Mono<Void> delete_migrated(String scopeId, boolean force) {
        return RxJava2Adapter.completableToMono(delete(scopeId, force, null));
    }
}

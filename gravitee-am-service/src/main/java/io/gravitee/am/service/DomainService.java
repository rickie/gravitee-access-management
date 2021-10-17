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
import io.gravitee.am.model.Domain;
import io.gravitee.am.repository.management.api.search.DomainCriteria;
import io.gravitee.am.service.model.NewDomain;
import io.gravitee.am.service.model.PatchDomain;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collection;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface DomainService {

      
Flux<Domain> findAllByEnvironment_migrated(String organizationId, String environment);

      
Flux<Domain> search_migrated(String organizationId, String environmentId, String query);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Domain> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Domain> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Mono<Domain> findByHrid_migrated(String environmentId, String hrid);

      
Mono<List<Domain>> findAll_migrated();

      
Flux<Domain> findAllByCriteria_migrated(DomainCriteria criteria);

      
Flux<Domain> findByIdIn_migrated(Collection<String> ids);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(organizationId, environmentId, domain, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Domain> create(String organizationId, String environmentId, NewDomain domain, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(organizationId, environmentId, domain, principal));
}
default Mono<Domain> create_migrated(String organizationId, String environmentId, NewDomain domain, User principal) {
    return RxJava2Adapter.singleToMono(create(organizationId, environmentId, domain, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domainId, domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Domain> update(String domainId, Domain domain) {
    return RxJava2Adapter.monoToSingle(update_migrated(domainId, domain));
}
default Mono<Domain> update_migrated(String domainId, Domain domain) {
    return RxJava2Adapter.singleToMono(update(domainId, domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(domainId, domain, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Domain> patch(String domainId, PatchDomain domain, User principal) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domainId, domain, principal));
}
default Mono<Domain> patch_migrated(String domainId, PatchDomain domain, User principal) {
    return RxJava2Adapter.singleToMono(patch(domainId, domain, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String domain, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, principal));
}
default Mono<Void> delete_migrated(String domain, User principal) {
    return RxJava2Adapter.completableToMono(delete(domain, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(organizationId, environmentId, domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Domain> create(String organizationId, String environmentId, NewDomain domain) {
    return RxJava2Adapter.monoToSingle(create_migrated(organizationId, environmentId, domain));
}default Mono<Domain> create_migrated(String organizationId, String environmentId, NewDomain domain) {
        return RxJava2Adapter.singleToMono(create(organizationId, environmentId, domain, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(domainId, domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Domain> patch(String domainId, PatchDomain domain) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domainId, domain));
}default Mono<Domain> patch_migrated(String domainId, PatchDomain domain) {
        return RxJava2Adapter.singleToMono(patch(domainId, domain, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String domain) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain));
}default Mono<Void> delete_migrated(String domain) {
        return RxJava2Adapter.completableToMono(delete(domain, null));
    }

    String buildUrl(Domain domain, String path);
}

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
import io.gravitee.am.model.Form;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.service.model.NewForm;
import io.gravitee.am.service.model.UpdateForm;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface FormService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Form> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Form> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Flux<Form> findAll_migrated(ReferenceType referenceType, String referenceId);

      
Flux<Form> findAll_migrated(ReferenceType referenceType);

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Form> findByDomain(String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default Flux<Form> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      
Flux<Form> findByClient_migrated(ReferenceType referenceType, String referenceId, String client);

      
Flux<Form> findByDomainAndClient_migrated(String domain, String client);

      
Mono<Form> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template);

      
Mono<Form> findByDomainAndTemplate_migrated(String domain, String template);

      
Mono<Form> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template);

      
Mono<Form> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template);

      
Mono<List<Form>> copyFromClient_migrated(String domain, String clientSource, String clientTarget);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newForm, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> create(ReferenceType referenceType, String referenceId, NewForm newForm, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newForm, principal));
}
default Mono<Form> create_migrated(ReferenceType referenceType, String referenceId, NewForm newForm, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newForm, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, form, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> create(String domain, NewForm form, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, form, principal));
}
default Mono<Form> create_migrated(String domain, NewForm form, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, form, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, client, form, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> create(String domain, String client, NewForm form, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, client, form, principal));
}
default Mono<Form> create_migrated(String domain, String client, NewForm form, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, client, form, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateForm, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> update(ReferenceType referenceType, String referenceId, String id, UpdateForm updateForm, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateForm, principal));
}
default Mono<Form> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateForm updateForm, User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateForm, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, form, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> update(String domain, String id, UpdateForm form, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, form, principal));
}
default Mono<Form> update_migrated(String domain, String id, UpdateForm form, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, form, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, client, id, form, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> update(String domain, String client, String id, UpdateForm form, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, form, principal));
}
default Mono<Form> update_migrated(String domain, String client, String id, UpdateForm form, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, client, id, form, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, formId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(ReferenceType referenceType, String referenceId, String formId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, formId, principal));
}
default Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String formId, User principal) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, formId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, pageId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String domain, String pageId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, pageId, principal));
}
default Mono<Void> delete_migrated(String domain, String pageId, User principal) {
    return RxJava2Adapter.completableToMono(delete(domain, pageId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, form))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> create(String domain, NewForm form) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, form));
}default Mono<Form> create_migrated(String domain, NewForm form) {
        return RxJava2Adapter.singleToMono(create(domain, form, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, client, form))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> create(String domain, String client, NewForm form) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, client, form));
}default Mono<Form> create_migrated(String domain, String client, NewForm form) {
        return RxJava2Adapter.singleToMono(create(domain, client, form, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, form))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> update(String domain, String id, UpdateForm form) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, form));
}default Mono<Form> update_migrated(String domain, String id, UpdateForm form) {
        return RxJava2Adapter.singleToMono(update(domain, id, form, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, client, id, form))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Form> update(String domain, String client, String id, UpdateForm form) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, form));
}default Mono<Form> update_migrated(String domain, String client, String id, UpdateForm form) {
        return RxJava2Adapter.singleToMono(update(domain, client, id, form, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, pageId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String domain, String pageId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, pageId));
}default Mono<Void> delete_migrated(String domain, String pageId) {
        return RxJava2Adapter.completableToMono(delete(domain, pageId, null));
    }

}

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

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Form> findAll(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default Flux<Form> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Form> findAll(ReferenceType referenceType) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
default Flux<Form> findAll_migrated(ReferenceType referenceType) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Form> findByDomain(String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default Flux<Form> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClient_migrated(referenceType, referenceId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Form> findByClient(ReferenceType referenceType, String referenceId, String client) {
    return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
default Flux<Form> findByClient_migrated(ReferenceType referenceType, String referenceId, String client) {
    return RxJava2Adapter.flowableToFlux(findByClient(referenceType, referenceId, client));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClient_migrated(domain, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<Form> findByDomainAndClient(String domain, String client) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClient_migrated(domain, client));
}
default Flux<Form> findByDomainAndClient_migrated(String domain, String client) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClient(domain, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(referenceType, referenceId, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Form> findByTemplate(ReferenceType referenceType, String referenceId, String template) {
    return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
default Mono<Form> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template) {
    return RxJava2Adapter.maybeToMono(findByTemplate(referenceType, referenceId, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndTemplate_migrated(domain, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Form> findByDomainAndTemplate(String domain, String template) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndTemplate_migrated(domain, template));
}
default Mono<Form> findByDomainAndTemplate_migrated(String domain, String template) {
    return RxJava2Adapter.maybeToMono(findByDomainAndTemplate(domain, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Form> findByClientAndTemplate(ReferenceType referenceType, String referenceId, String client, String template) {
    return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
default Mono<Form> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template) {
    return RxJava2Adapter.maybeToMono(findByClientAndTemplate(referenceType, referenceId, client, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndTemplate_migrated(domain, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Form> findByDomainAndClientAndTemplate(String domain, String client, String template) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndTemplate_migrated(domain, client, template));
}
default Mono<Form> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientAndTemplate(domain, client, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.copyFromClient_migrated(domain, clientSource, clientTarget))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<Form>> copyFromClient(String domain, String clientSource, String clientTarget) {
    return RxJava2Adapter.monoToSingle(copyFromClient_migrated(domain, clientSource, clientTarget));
}
default Mono<List<Form>> copyFromClient_migrated(String domain, String clientSource, String clientTarget) {
    return RxJava2Adapter.singleToMono(copyFromClient(domain, clientSource, clientTarget));
}

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

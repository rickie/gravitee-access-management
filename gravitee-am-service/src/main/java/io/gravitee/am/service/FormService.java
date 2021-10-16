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
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface FormService {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Form> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Form> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Form> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Form> findAll(io.gravitee.am.model.ReferenceType referenceType) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Form> findAll_migrated(ReferenceType referenceType) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Form> findByDomain(java.lang.String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Form> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClient_migrated(referenceType, referenceId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Form> findByClient(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String client) {
    return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Form> findByClient_migrated(ReferenceType referenceType, String referenceId, String client) {
    return RxJava2Adapter.flowableToFlux(findByClient(referenceType, referenceId, client));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndClient_migrated(domain, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Form> findByDomainAndClient(java.lang.String domain, java.lang.String client) {
    return RxJava2Adapter.fluxToFlowable(findByDomainAndClient_migrated(domain, client));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Form> findByDomainAndClient_migrated(String domain, String client) {
    return RxJava2Adapter.flowableToFlux(findByDomainAndClient(domain, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(referenceType, referenceId, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Form> findByTemplate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template) {
    return RxJava2Adapter.maybeToMono(findByTemplate(referenceType, referenceId, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndTemplate_migrated(domain, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Form> findByDomainAndTemplate(java.lang.String domain, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndTemplate_migrated(domain, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> findByDomainAndTemplate_migrated(String domain, String template) {
    return RxJava2Adapter.maybeToMono(findByDomainAndTemplate(domain, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Form> findByClientAndTemplate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String client, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template) {
    return RxJava2Adapter.maybeToMono(findByClientAndTemplate(referenceType, referenceId, client, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndTemplate_migrated(domain, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Form> findByDomainAndClientAndTemplate(java.lang.String domain, java.lang.String client, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndTemplate_migrated(domain, client, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientAndTemplate(domain, client, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.copyFromClient_migrated(domain, clientSource, clientTarget))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.model.Form>> copyFromClient(java.lang.String domain, java.lang.String clientSource, java.lang.String clientTarget) {
    return RxJava2Adapter.monoToSingle(copyFromClient_migrated(domain, clientSource, clientTarget));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.model.Form>> copyFromClient_migrated(String domain, String clientSource, String clientTarget) {
    return RxJava2Adapter.singleToMono(copyFromClient(domain, clientSource, clientTarget));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newForm, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.service.model.NewForm newForm, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newForm, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> create_migrated(ReferenceType referenceType, String referenceId, NewForm newForm, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newForm, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, form, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> create(java.lang.String domain, io.gravitee.am.service.model.NewForm form, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, form, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> create_migrated(String domain, NewForm form, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, form, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, client, form, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> create(java.lang.String domain, java.lang.String client, io.gravitee.am.service.model.NewForm form, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, client, form, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> create_migrated(String domain, String client, NewForm form, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, client, form, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(referenceType, referenceId, id, updateForm, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> update(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id, io.gravitee.am.service.model.UpdateForm updateForm, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateForm, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateForm updateForm, User principal) {
    return RxJava2Adapter.singleToMono(update(referenceType, referenceId, id, updateForm, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, form, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateForm form, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, form, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> update_migrated(String domain, String id, UpdateForm form, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, form, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, client, id, form, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> update(java.lang.String domain, java.lang.String client, java.lang.String id, io.gravitee.am.service.model.UpdateForm form, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, form, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Form> update_migrated(String domain, String client, String id, UpdateForm form, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, client, id, form, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, formId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String formId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, formId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(ReferenceType referenceType, String referenceId, String formId, User principal) {
    return RxJava2Adapter.completableToMono(delete(referenceType, referenceId, formId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, pageId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String pageId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, pageId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String domain, String pageId, User principal) {
    return RxJava2Adapter.completableToMono(delete(domain, pageId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, form))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> create(java.lang.String domain, io.gravitee.am.service.model.NewForm form) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, form));
}default Mono<Form> create_migrated(String domain, NewForm form) {
        return RxJava2Adapter.singleToMono(create(domain, form, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, client, form))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> create(java.lang.String domain, java.lang.String client, io.gravitee.am.service.model.NewForm form) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, client, form));
}default Mono<Form> create_migrated(String domain, String client, NewForm form) {
        return RxJava2Adapter.singleToMono(create(domain, client, form, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, form))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateForm form) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, form));
}default Mono<Form> update_migrated(String domain, String id, UpdateForm form) {
        return RxJava2Adapter.singleToMono(update(domain, id, form, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, client, id, form))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Form> update(java.lang.String domain, java.lang.String client, java.lang.String id, io.gravitee.am.service.model.UpdateForm form) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, form));
}default Mono<Form> update_migrated(String domain, String client, String id, UpdateForm form) {
        return RxJava2Adapter.singleToMono(update(domain, client, id, form, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, pageId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String domain, java.lang.String pageId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(domain, pageId));
}default Mono<Void> delete_migrated(String domain, String pageId) {
        return RxJava2Adapter.completableToMono(delete(domain, pageId, null));
    }

}

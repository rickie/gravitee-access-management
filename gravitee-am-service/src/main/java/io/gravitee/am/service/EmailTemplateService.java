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
import io.gravitee.am.model.Email;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.service.model.NewEmail;
import io.gravitee.am.service.model.UpdateEmail;
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
public interface EmailTemplateService {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Email> findAll() {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Email> findAll_migrated() {
    return RxJava2Adapter.flowableToFlux(findAll());
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Email> findAll(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId) {
    return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Email> findAll_migrated(ReferenceType referenceType, String referenceId) {
    return RxJava2Adapter.flowableToFlux(findAll(referenceType, referenceId));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClient_migrated(referenceType, referenceId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Email> findByClient(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String client) {
    return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Email> findByClient_migrated(ReferenceType referenceType, String referenceId, String client) {
    return RxJava2Adapter.flowableToFlux(findByClient(referenceType, referenceId, client));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(referenceType, referenceId, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Email> findByTemplate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template) {
    return RxJava2Adapter.maybeToMono(findByTemplate(referenceType, referenceId, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndTemplate_migrated(domain, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Email> findByDomainAndTemplate(java.lang.String domain, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndTemplate_migrated(domain, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> findByDomainAndTemplate_migrated(String domain, String template) {
    return RxJava2Adapter.maybeToMono(findByDomainAndTemplate(domain, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Email> findByClientAndTemplate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String client, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template) {
    return RxJava2Adapter.maybeToMono(findByClientAndTemplate(referenceType, referenceId, client, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndTemplate_migrated(domain, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Email> findByDomainAndClientAndTemplate(java.lang.String domain, java.lang.String client, java.lang.String template) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndTemplate_migrated(domain, client, template));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template) {
    return RxJava2Adapter.maybeToMono(findByDomainAndClientAndTemplate(domain, client, template));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.Email> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.copyFromClient_migrated(domain, clientSource, clientTarget))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Flowable<io.gravitee.am.model.Email> copyFromClient(java.lang.String domain, java.lang.String clientSource, java.lang.String clientTarget) {
    return RxJava2Adapter.fluxToFlowable(copyFromClient_migrated(domain, clientSource, clientTarget));
}
default reactor.core.publisher.Flux<io.gravitee.am.model.Email> copyFromClient_migrated(String domain, String clientSource, String clientTarget) {
    return RxJava2Adapter.flowableToFlux(copyFromClient(domain, clientSource, clientTarget));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.service.model.NewEmail newEmail, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newEmail, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> create_migrated(ReferenceType referenceType, String referenceId, NewEmail newEmail, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> create(java.lang.String domain, io.gravitee.am.service.model.NewEmail newEmail, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newEmail, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> create_migrated(String domain, NewEmail newEmail, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, client, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> create(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String client, io.gravitee.am.service.model.NewEmail newEmail, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, client, newEmail, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> create_migrated(ReferenceType referenceType, String referenceId, String client, NewEmail newEmail, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, client, newEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, client, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> create(java.lang.String domain, java.lang.String client, io.gravitee.am.service.model.NewEmail newEmail, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, client, newEmail, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> create_migrated(String domain, String client, NewEmail newEmail, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, client, newEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateEmail updateEmail, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateEmail, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> update_migrated(String domain, String id, UpdateEmail updateEmail, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, client, id, updateEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> update(java.lang.String domain, java.lang.String client, java.lang.String id, io.gravitee.am.service.model.UpdateEmail updateEmail, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, updateEmail, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Email> update_migrated(String domain, String client, String id, UpdateEmail updateEmail, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, client, id, updateEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(emailId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String emailId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(emailId, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String emailId, User principal) {
    return RxJava2Adapter.completableToMono(delete(emailId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newEmail))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> create(java.lang.String domain, io.gravitee.am.service.model.NewEmail newEmail) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newEmail));
}default Mono<Email> create_migrated(String domain, NewEmail newEmail) {
        return RxJava2Adapter.singleToMono(create(domain, newEmail,  null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, client, newEmail))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> create(java.lang.String domain, java.lang.String client, io.gravitee.am.service.model.NewEmail newEmail) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, client, newEmail));
}default Mono<Email> create_migrated(String domain, String client, NewEmail newEmail) {
        return RxJava2Adapter.singleToMono(create(domain, client, newEmail, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateEmail))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> update(java.lang.String domain, java.lang.String id, io.gravitee.am.service.model.UpdateEmail updateEmail) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateEmail));
}default Mono<Email> update_migrated(String domain, String id, UpdateEmail updateEmail) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateEmail,  null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, client, id, updateEmail))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Email> update(java.lang.String domain, java.lang.String client, java.lang.String id, io.gravitee.am.service.model.UpdateEmail updateEmail) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, updateEmail));
}default Mono<Email> update_migrated(String domain, String client, String id, UpdateEmail updateEmail) {
        return RxJava2Adapter.singleToMono(update(domain, client, id, updateEmail, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(emailId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String emailId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(emailId));
}default Mono<Void> delete_migrated(String emailId) {
        return RxJava2Adapter.completableToMono(delete(emailId, null));
    }

}

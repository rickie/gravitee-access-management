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

import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface EmailTemplateService {

      
Flux<Email> findAll_migrated();

      
Flux<Email> findAll_migrated(ReferenceType referenceType, String referenceId);

      
Flux<Email> findByClient_migrated(ReferenceType referenceType, String referenceId, String client);

      
Mono<Email> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template);

      
Mono<Email> findByDomainAndTemplate_migrated(String domain, String template);

      
Mono<Email> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template);

      
Mono<Email> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Email> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<Email> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      
Flux<Email> copyFromClient_migrated(String domain, String clientSource, String clientTarget);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> create(ReferenceType referenceType, String referenceId, NewEmail newEmail, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newEmail, principal));
}
default Mono<Email> create_migrated(ReferenceType referenceType, String referenceId, NewEmail newEmail, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, newEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> create(String domain, NewEmail newEmail, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newEmail, principal));
}
default Mono<Email> create_migrated(String domain, NewEmail newEmail, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, client, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> create(ReferenceType referenceType, String referenceId, String client, NewEmail newEmail, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, client, newEmail, principal));
}
default Mono<Email> create_migrated(ReferenceType referenceType, String referenceId, String client, NewEmail newEmail, User principal) {
    return RxJava2Adapter.singleToMono(create(referenceType, referenceId, client, newEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, client, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> create(String domain, String client, NewEmail newEmail, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, client, newEmail, principal));
}
default Mono<Email> create_migrated(String domain, String client, NewEmail newEmail, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, client, newEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> update(String domain, String id, UpdateEmail updateEmail, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateEmail, principal));
}
default Mono<Email> update_migrated(String domain, String id, UpdateEmail updateEmail, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, client, id, updateEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> update(String domain, String client, String id, UpdateEmail updateEmail, User principal) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, updateEmail, principal));
}
default Mono<Email> update_migrated(String domain, String client, String id, UpdateEmail updateEmail, User principal) {
    return RxJava2Adapter.singleToMono(update(domain, client, id, updateEmail, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(emailId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String emailId, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(emailId, principal));
}
default Mono<Void> delete_migrated(String emailId, User principal) {
    return RxJava2Adapter.completableToMono(delete(emailId, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newEmail))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> create(String domain, NewEmail newEmail) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newEmail));
}default Mono<Email> create_migrated(String domain, NewEmail newEmail) {
        return RxJava2Adapter.singleToMono(create(domain, newEmail,  null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, client, newEmail))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> create(String domain, String client, NewEmail newEmail) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, client, newEmail));
}default Mono<Email> create_migrated(String domain, String client, NewEmail newEmail) {
        return RxJava2Adapter.singleToMono(create(domain, client, newEmail, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateEmail))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> update(String domain, String id, UpdateEmail updateEmail) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateEmail));
}default Mono<Email> update_migrated(String domain, String id, UpdateEmail updateEmail) {
        return RxJava2Adapter.singleToMono(update(domain, id, updateEmail,  null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, client, id, updateEmail))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Email> update(String domain, String client, String id, UpdateEmail updateEmail) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, updateEmail));
}default Mono<Email> update_migrated(String domain, String client, String id, UpdateEmail updateEmail) {
        return RxJava2Adapter.singleToMono(update(domain, client, id, updateEmail, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(emailId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String emailId) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(emailId));
}default Mono<Void> delete_migrated(String emailId) {
        return RxJava2Adapter.completableToMono(delete(emailId, null));
    }

}

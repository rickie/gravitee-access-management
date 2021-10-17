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
import io.gravitee.am.model.analytics.AnalyticsQuery;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserService extends CommonUserService {

      @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Flowable<io.gravitee.am.model.User> findByDomain(String domain) {
    return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
default Flux<io.gravitee.am.model.User> findByDomain_migrated(String domain) {
    return RxJava2Adapter.flowableToFlux(findByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<io.gravitee.am.model.User>> findByDomain(String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
default Mono<Page<io.gravitee.am.model.User>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndUsername_migrated(domain, username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<io.gravitee.am.model.User> findByDomainAndUsername(String domain, String username) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndUsername_migrated(domain, username));
}
default Mono<io.gravitee.am.model.User> findByDomainAndUsername_migrated(String domain, String username) {
    return RxJava2Adapter.maybeToMono(findByDomainAndUsername(domain, username));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndUsernameAndSource_migrated(domain, username, source))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<io.gravitee.am.model.User> findByDomainAndUsernameAndSource(String domain, String username, String source) {
    return RxJava2Adapter.monoToMaybe(findByDomainAndUsernameAndSource_migrated(domain, username, source));
}
default Mono<io.gravitee.am.model.User> findByDomainAndUsernameAndSource_migrated(String domain, String username, String source) {
    return RxJava2Adapter.maybeToMono(findByDomainAndUsernameAndSource(domain, username, source));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<io.gravitee.am.model.User> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<io.gravitee.am.model.User> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> create(String domain, NewUser newUser) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newUser));
}
default Mono<io.gravitee.am.model.User> create_migrated(String domain, NewUser newUser) {
    return RxJava2Adapter.singleToMono(create(domain, newUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> update(String domain, String id, UpdateUser updateUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateUser));
}
default Mono<io.gravitee.am.model.User> update_migrated(String domain, String id, UpdateUser updateUser) {
    return RxJava2Adapter.singleToMono(update(domain, id, updateUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countByDomain(String domain) {
    return RxJava2Adapter.monoToSingle(countByDomain_migrated(domain));
}
default Mono<Long> countByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(countByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByApplication_migrated(domain, application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Long> countByApplication(String domain, String application) {
    return RxJava2Adapter.monoToSingle(countByApplication_migrated(domain, application));
}
default Mono<Long> countByApplication_migrated(String domain, String application) {
    return RxJava2Adapter.singleToMono(countByApplication(domain, application));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.statistics_migrated(query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Map<Object, Object>> statistics(AnalyticsQuery query) {
    return RxJava2Adapter.monoToSingle(statistics_migrated(query));
}
default Mono<Map<Object, Object>> statistics_migrated(AnalyticsQuery query) {
    return RxJava2Adapter.singleToMono(statistics(query));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.upsertFactor_migrated(userId, enrolledFactor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<io.gravitee.am.model.User> upsertFactor(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(upsertFactor_migrated(userId, enrolledFactor, principal));
}
default Mono<io.gravitee.am.model.User> upsertFactor_migrated(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(upsertFactor(userId, enrolledFactor, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.removeFactor_migrated(userId, factorId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable removeFactor(String userId, String factorId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(removeFactor_migrated(userId, factorId, principal));
}
default Mono<Void> removeFactor_migrated(String userId, String factorId, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(removeFactor(userId, factorId, principal));
}
}

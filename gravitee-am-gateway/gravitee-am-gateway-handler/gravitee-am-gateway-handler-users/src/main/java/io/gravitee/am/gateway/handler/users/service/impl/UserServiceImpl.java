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
package io.gravitee.am.gateway.handler.users.service.impl;

import io.gravitee.am.gateway.handler.users.service.UserService;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.service.ScopeApprovalService;
import io.gravitee.am.service.exception.ScopeApprovalNotFoundException;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserServiceImpl implements UserService {

    @Autowired
    private io.gravitee.am.service.UserService userService;

    @Autowired
    private Domain domain;

    @Autowired
    private ScopeApprovalService scopeApprovalService;

    @Deprecated
@Override
    public Maybe<User> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<User> findById_migrated(String id) {
        return RxJava2Adapter.maybeToMono(userService.findById(id));
    }

    @Deprecated
@Override
    public Single<Set<ScopeApproval>> consents(String userId) {
 return RxJava2Adapter.monoToSingle(consents_migrated(userId));
}
@Override
    public Mono<Set<ScopeApproval>> consents_migrated(String userId) {
        return RxJava2Adapter.singleToMono(scopeApprovalService.findByDomainAndUser(domain.getId(), userId).collect(HashSet::new, Set::add));
    }

    @Deprecated
@Override
    public Single<Set<ScopeApproval>> consents(String userId, String clientId) {
 return RxJava2Adapter.monoToSingle(consents_migrated(userId, clientId));
}
@Override
    public Mono<Set<ScopeApproval>> consents_migrated(String userId, String clientId) {
        return RxJava2Adapter.singleToMono(scopeApprovalService.findByDomainAndUserAndClient(domain.getId(), userId, clientId).collect(HashSet::new, Set::add));
    }

    @Deprecated
@Override
    public Maybe<ScopeApproval> consent(String consentId) {
 return RxJava2Adapter.monoToMaybe(consent_migrated(consentId));
}
@Override
    public Mono<ScopeApproval> consent_migrated(String consentId) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(scopeApprovalService.findById(consentId)).switchIfEmpty(Mono.error(new ScopeApprovalNotFoundException(consentId)))));
    }

    @Deprecated
@Override
    public Completable revokeConsent(String userId, String consentId, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToCompletable(revokeConsent_migrated(userId, consentId, principal));
}
@Override
    public Mono<Void> revokeConsent_migrated(String userId, String consentId, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.completableToMono(scopeApprovalService.revokeByConsent(domain.getId(), userId, consentId, principal));
    }

    @Deprecated
@Override
    public Completable revokeConsents(String userId, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToCompletable(revokeConsents_migrated(userId, principal));
}
@Override
    public Mono<Void> revokeConsents_migrated(String userId, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.completableToMono(scopeApprovalService.revokeByUser(domain.getId(), userId, principal));
    }

    @Deprecated
@Override
    public Completable revokeConsents(String userId, String clientId, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToCompletable(revokeConsents_migrated(userId, clientId, principal));
}
@Override
    public Mono<Void> revokeConsents_migrated(String userId, String clientId, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.completableToMono(scopeApprovalService.revokeByUserAndClient(domain.getId(), userId, clientId, principal));
    }

}

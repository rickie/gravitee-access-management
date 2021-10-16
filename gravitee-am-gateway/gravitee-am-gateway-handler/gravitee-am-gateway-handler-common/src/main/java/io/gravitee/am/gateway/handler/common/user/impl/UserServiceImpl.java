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
package io.gravitee.am.gateway.handler.common.user.impl;

import io.gravitee.am.gateway.handler.common.user.UserService;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.repository.management.api.search.FilterCriteria;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserServiceImpl implements UserService {

    @Autowired
    private io.gravitee.am.service.UserService userService;

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
    public Maybe<User> findByDomainAndExternalIdAndSource(String domain, String externalId, String source) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndExternalIdAndSource_migrated(domain, externalId, source));
}
@Override
    public Mono<User> findByDomainAndExternalIdAndSource_migrated(String domain, String externalId, String source) {
        return RxJava2Adapter.maybeToMono(userService.findByExternalIdAndSource(ReferenceType.DOMAIN, domain, externalId, source));
    }

    @Deprecated
@Override
    public Maybe<User> findByDomainAndUsernameAndSource(String domain, String username, String source) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndUsernameAndSource_migrated(domain, username, source));
}
@Override
    public Mono<User> findByDomainAndUsernameAndSource_migrated(String domain, String username, String source) {
        return RxJava2Adapter.maybeToMono(userService.findByDomainAndUsernameAndSource(domain, username, source));
    }

    @Deprecated
@Override
    public Single<List<User>> findByDomainAndCriteria(String domain, FilterCriteria criteria) {
 return RxJava2Adapter.monoToSingle(findByDomainAndCriteria_migrated(domain, criteria));
}
@Override
    public Mono<List<User>> findByDomainAndCriteria_migrated(String domain, FilterCriteria criteria) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(userService.search(ReferenceType.DOMAIN, domain, criteria, 0, 2)).map(RxJavaReactorMigrationUtil.toJdkFunction(p -> new ArrayList<>(p.getData())))));
    }

    @Deprecated
@Override
    public Single<User> create(User user) {
 return RxJava2Adapter.monoToSingle(create_migrated(user));
}
@Override
    public Mono<User> create_migrated(User user) {
        return RxJava2Adapter.singleToMono(userService.create(user));
    }

    @Deprecated
@Override
    public Single<User> update(User user) {
 return RxJava2Adapter.monoToSingle(update_migrated(user));
}
@Override
    public Mono<User> update_migrated(User user) {
        return RxJava2Adapter.singleToMono(userService.update(user));
    }

    @Deprecated
@Override
    public Single<User> enhance(User user) {
 return RxJava2Adapter.monoToSingle(enhance_migrated(user));
}
@Override
    public Mono<User> enhance_migrated(User user) {
        return RxJava2Adapter.singleToMono(userService.enhance(user));
    }

    @Deprecated
@Override
    public Single<User> addFactor(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(addFactor_migrated(userId, enrolledFactor, principal));
}
@Override
    public Mono<User> addFactor_migrated(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
        return RxJava2Adapter.singleToMono(userService.upsertFactor(userId, enrolledFactor, principal));
    }
}

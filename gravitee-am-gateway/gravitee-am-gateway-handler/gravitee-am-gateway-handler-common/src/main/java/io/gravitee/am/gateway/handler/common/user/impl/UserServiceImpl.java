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

import com.google.errorprone.annotations.InlineMe;
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

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<User> findById_migrated(String id) {
        return userService.findById_migrated(id);
    }

    
@Override
    public Mono<User> findByDomainAndExternalIdAndSource_migrated(String domain, String externalId, String source) {
        return userService.findByExternalIdAndSource_migrated(ReferenceType.DOMAIN, domain, externalId, source);
    }

    
@Override
    public Mono<User> findByDomainAndUsernameAndSource_migrated(String domain, String username, String source) {
        return userService.findByDomainAndUsernameAndSource_migrated(domain, username, source);
    }

    
@Override
    public Mono<List<User>> findByDomainAndCriteria_migrated(String domain, FilterCriteria criteria) {
        return userService.search_migrated(ReferenceType.DOMAIN, domain, criteria, 0, 2).map(RxJavaReactorMigrationUtil.toJdkFunction(p -> new ArrayList<>(p.getData())));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> create(User user) {
 return RxJava2Adapter.monoToSingle(create_migrated(user));
}
@Override
    public Mono<User> create_migrated(User user) {
        return userService.create_migrated(user);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> update(User user) {
 return RxJava2Adapter.monoToSingle(update_migrated(user));
}
@Override
    public Mono<User> update_migrated(User user) {
        return userService.update_migrated(user);
    }

    
@Override
    public Mono<User> enhance_migrated(User user) {
        return userService.enhance_migrated(user);
    }

    
@Override
    public Mono<User> addFactor_migrated(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
        return userService.upsertFactor_migrated(userId, enrolledFactor, principal);
    }
}

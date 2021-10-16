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
package io.gravitee.am.identityprovider.gravitee.user;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.identityprovider.api.UserProvider;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class GraviteeUserProvider implements UserProvider {

    @Deprecated
@Override
    public Maybe<User> findByUsername(String username) {
 return RxJava2Adapter.monoToMaybe(findByUsername_migrated(username));
}
@Override
    public Mono<User> findByUsername_migrated(String username) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty()));
    }

    @Deprecated
@Override
    public Single<User> create(User user) {
 return RxJava2Adapter.monoToSingle(create_migrated(user));
}
@Override
    public Mono<User> create_migrated(User user) {
        // create will be performed by the repository layer called by the OrganizationUserService
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(user)));
    }

    @Deprecated
@Override
    public Single<User> update(String id, User updateUser) {
 return RxJava2Adapter.monoToSingle(update_migrated(id, updateUser));
}
@Override
    public Mono<User> update_migrated(String id, User updateUser) {
        // update will be performed by the repository layer called by the OrganizationUserService
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(updateUser)));
    }

    @Deprecated
@Override
    public Completable delete(String id) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
@Override
    public Mono<Void> delete_migrated(String id) {
        // delete will be performed by the repository layer called by the OrganizationUserService
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty()));
    }
}

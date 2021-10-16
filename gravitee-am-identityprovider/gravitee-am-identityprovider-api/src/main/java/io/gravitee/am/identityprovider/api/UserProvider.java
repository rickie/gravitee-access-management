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
package io.gravitee.am.identityprovider.api;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.common.component.Lifecycle;
import io.gravitee.common.service.Service;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserProvider extends Service<UserProvider> {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByEmail_migrated(email))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.identityprovider.api.User> findByEmail(java.lang.String email) {
    return RxJava2Adapter.monoToMaybe(findByEmail_migrated(email));
}default Mono<User> findByEmail_migrated(String email) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty()));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByUsername_migrated(username))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.identityprovider.api.User> findByUsername(java.lang.String username) {
    return RxJava2Adapter.monoToMaybe(findByUsername_migrated(username));
}
default reactor.core.publisher.Mono<io.gravitee.am.identityprovider.api.User> findByUsername_migrated(String username) {
    return RxJava2Adapter.maybeToMono(findByUsername(username));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.identityprovider.api.User> create(io.gravitee.am.identityprovider.api.User user) {
    return RxJava2Adapter.monoToSingle(create_migrated(user));
}
default reactor.core.publisher.Mono<io.gravitee.am.identityprovider.api.User> create_migrated(User user) {
    return RxJava2Adapter.singleToMono(create(user));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(id, updateUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.identityprovider.api.User> update(java.lang.String id, io.gravitee.am.identityprovider.api.User updateUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(id, updateUser));
}
default reactor.core.publisher.Mono<io.gravitee.am.identityprovider.api.User> update_migrated(String id, User updateUser) {
    return RxJava2Adapter.singleToMono(update(id, updateUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String id) {
    return RxJava2Adapter.completableToMono(delete(id));
}

    default Lifecycle.State lifecycleState() {
        return Lifecycle.State.INITIALIZED;
    }

    default UserProvider start() throws Exception {
        return this;
    }

    default UserProvider stop() throws Exception {
        return this;
    }
}

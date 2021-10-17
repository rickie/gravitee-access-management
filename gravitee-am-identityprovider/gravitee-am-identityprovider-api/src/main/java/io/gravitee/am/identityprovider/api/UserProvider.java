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
import io.gravitee.am.identityprovider.api.User;
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

      default Mono<User> findByEmail_migrated(String email) {
        return Mono.empty();
    }

      
Mono<User> findByUsername_migrated(String username);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> create(User user) {
    return RxJava2Adapter.monoToSingle(create_migrated(user));
}
default Mono<User> create_migrated(User user) {
    return RxJava2Adapter.singleToMono(create(user));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(id, updateUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<User> update(String id, User updateUser) {
    return RxJava2Adapter.monoToSingle(update_migrated(id, updateUser));
}
default Mono<User> update_migrated(String id, User updateUser) {
    return RxJava2Adapter.singleToMono(update(id, updateUser));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
default Mono<Void> delete_migrated(String id) {
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

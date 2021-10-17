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
package io.gravitee.am.repository.oauth2.api;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.repository.oauth2.model.AuthorizationCode;

import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AuthorizationCodeRepository {

    /**
     * Store an authorization code.
     *
     * @param authorizationCode The authorization code.
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(authorizationCode))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AuthorizationCode> create(AuthorizationCode authorizationCode) {
    return RxJava2Adapter.monoToSingle(create_migrated(authorizationCode));
}
default Mono<AuthorizationCode> create_migrated(AuthorizationCode authorizationCode) {
    return RxJava2Adapter.singleToMono(create(authorizationCode));
}

    /**
     * Look for an {@link AuthorizationCode} by id and delete it.
     *
     * @param id The id to consume.
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<AuthorizationCode> delete(String id) {
    return RxJava2Adapter.monoToMaybe(delete_migrated(id));
}
default Mono<AuthorizationCode> delete_migrated(String id) {
    return RxJava2Adapter.maybeToMono(delete(id));
}

    /**
     * Find an {@link AuthorizationCode} by its code.
     *
     * @param code The authorization code.
     * @return
     */
      
Mono<AuthorizationCode> findByCode_migrated(String code);

      default Mono<Void> purgeExpiredData_migrated() {
        return Mono.empty();
    }
}

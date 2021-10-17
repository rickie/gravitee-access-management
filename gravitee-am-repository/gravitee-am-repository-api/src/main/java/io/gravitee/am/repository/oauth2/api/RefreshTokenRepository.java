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
import io.gravitee.am.repository.oauth2.model.RefreshToken;
import io.reactivex.Completable;

import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface RefreshTokenRepository {

      
Mono<RefreshToken> findByToken_migrated(String token);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(refreshToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<RefreshToken> create(RefreshToken refreshToken) {
    return RxJava2Adapter.monoToSingle(create_migrated(refreshToken));
}
default Mono<RefreshToken> create_migrated(RefreshToken refreshToken) {
    return RxJava2Adapter.singleToMono(create(refreshToken));
}

      
Mono<Void> bulkWrite_migrated(List<RefreshToken> refreshTokens);

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String token) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(token));
}
default Mono<Void> delete_migrated(String token) {
    return RxJava2Adapter.completableToMono(delete(token));
}

      
Mono<Void> deleteByUserId_migrated(String userId);

      
Mono<Void> deleteByDomainIdClientIdAndUserId_migrated(String domainId, String clientId, String userId);

      
Mono<Void> deleteByDomainIdAndUserId_migrated(String domainId, String userId);

      default Mono<Void> purgeExpiredData_migrated() {
        return Mono.empty();
    }
}

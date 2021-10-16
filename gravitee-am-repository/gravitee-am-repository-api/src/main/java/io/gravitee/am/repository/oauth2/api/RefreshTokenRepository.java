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



import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface RefreshTokenRepository {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByToken_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.oauth2.model.RefreshToken> findByToken(java.lang.String token) {
    return RxJava2Adapter.monoToMaybe(findByToken_migrated(token));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.oauth2.model.RefreshToken> findByToken_migrated(String token) {
    return RxJava2Adapter.maybeToMono(findByToken(token));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(refreshToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.repository.oauth2.model.RefreshToken> create(io.gravitee.am.repository.oauth2.model.RefreshToken refreshToken) {
    return RxJava2Adapter.monoToSingle(create_migrated(refreshToken));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.oauth2.model.RefreshToken> create_migrated(RefreshToken refreshToken) {
    return RxJava2Adapter.singleToMono(create(refreshToken));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.bulkWrite_migrated(refreshTokens))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable bulkWrite(java.util.List<io.gravitee.am.repository.oauth2.model.RefreshToken> refreshTokens) {
    return RxJava2Adapter.monoToCompletable(bulkWrite_migrated(refreshTokens));
}
default reactor.core.publisher.Mono<java.lang.Void> bulkWrite_migrated(List<RefreshToken> refreshTokens) {
    return RxJava2Adapter.completableToMono(bulkWrite(refreshTokens));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String token) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(token));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String token) {
    return RxJava2Adapter.completableToMono(delete(token));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByUserId_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable deleteByUserId(java.lang.String userId) {
    return RxJava2Adapter.monoToCompletable(deleteByUserId_migrated(userId));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteByUserId_migrated(String userId) {
    return RxJava2Adapter.completableToMono(deleteByUserId(userId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainIdClientIdAndUserId_migrated(domainId, clientId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable deleteByDomainIdClientIdAndUserId(java.lang.String domainId, java.lang.String clientId, java.lang.String userId) {
    return RxJava2Adapter.monoToCompletable(deleteByDomainIdClientIdAndUserId_migrated(domainId, clientId, userId));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteByDomainIdClientIdAndUserId_migrated(String domainId, String clientId, String userId) {
    return RxJava2Adapter.completableToMono(deleteByDomainIdClientIdAndUserId(domainId, clientId, userId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByDomainIdAndUserId_migrated(domainId, userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable deleteByDomainIdAndUserId(java.lang.String domainId, java.lang.String userId) {
    return RxJava2Adapter.monoToCompletable(deleteByDomainIdAndUserId_migrated(domainId, userId));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteByDomainIdAndUserId_migrated(String domainId, String userId) {
    return RxJava2Adapter.completableToMono(deleteByDomainIdAndUserId(domainId, userId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.purgeExpiredData_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable purgeExpiredData() {
    return RxJava2Adapter.monoToCompletable(purgeExpiredData_migrated());
}default Mono<Void> purgeExpiredData_migrated() {
        return Mono.empty();
    }
}

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
import io.gravitee.am.repository.oauth2.model.AccessToken;
import io.reactivex.*;
import io.reactivex.BackpressureStrategy;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AccessTokenRepository {
    /**
     * Find access token by id
     * @param token access token's id
     * @return Access token if any
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByToken_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.repository.oauth2.model.AccessToken> findByToken(java.lang.String token) {
    return RxJava2Adapter.monoToMaybe(findByToken_migrated(token));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.oauth2.model.AccessToken> findByToken_migrated(String token) {
    return RxJava2Adapter.maybeToMono(findByToken(token));
}

    /**
     * Create an access token
     * @param accessToken access token to store
     * @return th created access token
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(accessToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.repository.oauth2.model.AccessToken> create(io.gravitee.am.repository.oauth2.model.AccessToken accessToken) {
    return RxJava2Adapter.monoToSingle(create_migrated(accessToken));
}
default reactor.core.publisher.Mono<io.gravitee.am.repository.oauth2.model.AccessToken> create_migrated(AccessToken accessToken) {
    return RxJava2Adapter.singleToMono(create(accessToken));
}

    /**
     * Delete token by its id
     * @param token token's id
     * @return acknowledge of the operation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete(java.lang.String token) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(token));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(String token) {
    return RxJava2Adapter.completableToMono(delete(token));
}

    /**
     * Bulk insert of access tokens
     * @param accessTokens access token to store
     * @return acknowledge of the operation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.bulkWrite_migrated(accessTokens))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable bulkWrite(java.util.List<io.gravitee.am.repository.oauth2.model.AccessToken> accessTokens) {
    return RxJava2Adapter.monoToCompletable(bulkWrite_migrated(accessTokens));
}
default reactor.core.publisher.Mono<java.lang.Void> bulkWrite_migrated(List<AccessToken> accessTokens) {
    return RxJava2Adapter.completableToMono(bulkWrite(accessTokens));
}

    /**
     * Retrieve access tokens stored against the provided client id.
     *
     * @param clientId the client id to search
     * @param subject the end-user technical identifier
     * @return a collection of access tokens
     */
      @InlineMe(replacement = "RxJava2Adapter.fluxToObservable(this.findByClientIdAndSubject_migrated(clientId, subject))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Observable<io.gravitee.am.repository.oauth2.model.AccessToken> findByClientIdAndSubject(java.lang.String clientId, java.lang.String subject) {
    return RxJava2Adapter.fluxToObservable(findByClientIdAndSubject_migrated(clientId, subject));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.oauth2.model.AccessToken> findByClientIdAndSubject_migrated(String clientId, String subject) {
    return RxJava2Adapter.observableToFlux(findByClientIdAndSubject(clientId, subject), BackpressureStrategy.BUFFER);
}

    /**
     * Retrieve access tokens stored against the provided client id.
     *
     * @param clientId the client id to search
     * @return a collection of access tokens
     */
      @InlineMe(replacement = "RxJava2Adapter.fluxToObservable(this.findByClientId_migrated(clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Observable<io.gravitee.am.repository.oauth2.model.AccessToken> findByClientId(java.lang.String clientId) {
    return RxJava2Adapter.fluxToObservable(findByClientId_migrated(clientId));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.oauth2.model.AccessToken> findByClientId_migrated(String clientId) {
    return RxJava2Adapter.observableToFlux(findByClientId(clientId), BackpressureStrategy.BUFFER);
}

    /**
     * Retrieve access tokens stored against the provided authorization code.
     *
     * @param authorizationCode the authorization code to search
     * @return a collection of access tokens
     */
      @InlineMe(replacement = "RxJava2Adapter.fluxToObservable(this.findByAuthorizationCode_migrated(authorizationCode))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Observable<io.gravitee.am.repository.oauth2.model.AccessToken> findByAuthorizationCode(java.lang.String authorizationCode) {
    return RxJava2Adapter.fluxToObservable(findByAuthorizationCode_migrated(authorizationCode));
}
default reactor.core.publisher.Flux<io.gravitee.am.repository.oauth2.model.AccessToken> findByAuthorizationCode_migrated(String authorizationCode) {
    return RxJava2Adapter.observableToFlux(findByAuthorizationCode(authorizationCode), BackpressureStrategy.BUFFER);
}

    /**
     * Count access tokens stored against the provided client id.
     *
     * @param clientId the client id to search
     * @return the number of access tokens
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByClientId_migrated(clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Long> countByClientId(java.lang.String clientId) {
    return RxJava2Adapter.monoToSingle(countByClientId_migrated(clientId));
}
default reactor.core.publisher.Mono<java.lang.Long> countByClientId_migrated(String clientId) {
    return RxJava2Adapter.singleToMono(countByClientId(clientId));
}

    /**
     * Delete access tokens by user id
     * @param userId end-user
     * @return acknowledge of the operation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByUserId_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable deleteByUserId(java.lang.String userId) {
    return RxJava2Adapter.monoToCompletable(deleteByUserId_migrated(userId));
}
default reactor.core.publisher.Mono<java.lang.Void> deleteByUserId_migrated(String userId) {
    return RxJava2Adapter.completableToMono(deleteByUserId(userId));
}

    /**
     * Delete access token by domainId, clientId and userId.
     */
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
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty()));
    }
}

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
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
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
      
Mono<AccessToken> findByToken_migrated(String token);

    /**
     * Create an access token
     * @param accessToken access token to store
     * @return th created access token
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(accessToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AccessToken> create(AccessToken accessToken) {
    return RxJava2Adapter.monoToSingle(create_migrated(accessToken));
}
default Mono<AccessToken> create_migrated(AccessToken accessToken) {
    return RxJava2Adapter.singleToMono(create(accessToken));
}

    /**
     * Delete token by its id
     * @param token token's id
     * @return acknowledge of the operation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(String token) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(token));
}
default Mono<Void> delete_migrated(String token) {
    return RxJava2Adapter.completableToMono(delete(token));
}

    /**
     * Bulk insert of access tokens
     * @param accessTokens access token to store
     * @return acknowledge of the operation
     */
      
Mono<Void> bulkWrite_migrated(List<AccessToken> accessTokens);

    /**
     * Retrieve access tokens stored against the provided client id.
     *
     * @param clientId the client id to search
     * @param subject the end-user technical identifier
     * @return a collection of access tokens
     */
      
Flux<AccessToken> findByClientIdAndSubject_migrated(String clientId, String subject);

    /**
     * Retrieve access tokens stored against the provided client id.
     *
     * @param clientId the client id to search
     * @return a collection of access tokens
     */
      
Flux<AccessToken> findByClientId_migrated(String clientId);

    /**
     * Retrieve access tokens stored against the provided authorization code.
     *
     * @param authorizationCode the authorization code to search
     * @return a collection of access tokens
     */
      
Flux<AccessToken> findByAuthorizationCode_migrated(String authorizationCode);

    /**
     * Count access tokens stored against the provided client id.
     *
     * @param clientId the client id to search
     * @return the number of access tokens
     */
      
Mono<Long> countByClientId_migrated(String clientId);

    /**
     * Delete access tokens by user id
     * @param userId end-user
     * @return acknowledge of the operation
     */
      
Mono<Void> deleteByUserId_migrated(String userId);

    /**
     * Delete access token by domainId, clientId and userId.
     */
      
Mono<Void> deleteByDomainIdClientIdAndUserId_migrated(String domainId, String clientId, String userId);

      
Mono<Void> deleteByDomainIdAndUserId_migrated(String domainId, String userId);

      default Mono<Void> purgeExpiredData_migrated() {
        return Mono.empty();
    }
}

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
package io.gravitee.am.gateway.handler.oauth2.service.granter;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.oauth2.GrantType;
import io.gravitee.am.gateway.handler.oauth2.exception.UnauthorizedClientException;
import io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request;
import io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequest;
import io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequestResolver;
import io.gravitee.am.gateway.handler.oauth2.service.token.Token;
import io.gravitee.am.gateway.handler.oauth2.service.token.TokenService;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Maybe;

import io.reactivex.Single;
import java.util.Objects;
import java.util.Optional;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AbstractTokenGranter implements TokenGranter {

    private final String grantType;

    private TokenRequestResolver tokenRequestResolver;

    private TokenService tokenService;

    private boolean supportRefreshToken = true;

    public AbstractTokenGranter(final String grantType) {
        Objects.requireNonNull(grantType);
        this.grantType = grantType;
    }

    @Override
    public boolean handle(String grantType, Client client) {
        return this.grantType.equals(grantType);
    }

    
@Override
    public Mono<Token> grant_migrated(TokenRequest tokenRequest, Client client) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(parseRequest_migrated(tokenRequest, client).flatMap(e->resolveResourceOwner_migrated(e, client)).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()))
                .flatMapSingle(user -> RxJava2Adapter.monoToSingle(handleRequest_migrated(tokenRequest, client, user.orElse(null)))));
    }

    /**
     * The authorization server validates the request to ensure that all required parameters are present and valid.
     * @param tokenRequest Access Token Request
     * @param client OAuth2 client
     * @return Access Token Request or invalid request exception
     */
    
protected Mono<TokenRequest> parseRequest_migrated(TokenRequest tokenRequest, Client client) {
        // Is client allowed to use such grant type ?
        if (client.getAuthorizedGrantTypes() != null && !client.getAuthorizedGrantTypes().isEmpty()
                && !client.getAuthorizedGrantTypes().contains(grantType)) {
            throw new UnauthorizedClientException("Unauthorized grant type: " + grantType);
        }
        return Mono.just(tokenRequest);
    }

    /**
     * If the request is valid, the authorization server authenticates the resource owner and obtains an authorization decision
     * @param tokenRequest Access Token Request
     * @param client OAuth2 client
     * @return Resource Owner or empty for protocol flow like client_credentials
     */
    
protected Mono<User> resolveResourceOwner_migrated(TokenRequest tokenRequest, Client client) {
        return Mono.empty();
    }

    /**
     * Validates the request to ensure that all required parameters meet the Client and Resource Owner requirements
     * @param tokenRequest Access Token Request
     * @param client OAuth2 client
     * @param endUser Resource Owner (if exists)
     * @return Access Token Request or OAuth 2.0 exception
     */
    
protected Mono<TokenRequest> resolveRequest_migrated(TokenRequest tokenRequest, Client client, User endUser) {
        return tokenRequestResolver.resolve_migrated(tokenRequest, client, endUser);
    }

    /**
     * Determines if a refresh token should be included in the token response
     * @param supportRefreshToken
     */
    protected void setSupportRefreshToken(boolean supportRefreshToken) {
        this.supportRefreshToken = supportRefreshToken;
    }

    
private Mono<Token> handleRequest_migrated(TokenRequest tokenRequest, Client client, User endUser) {
        return resolveRequest_migrated(tokenRequest, client, endUser).flatMap(tokenRequest1->createOAuth2Request_migrated(tokenRequest1, client, endUser)).flatMap(oAuth2Request->createAccessToken_migrated(oAuth2Request, client, endUser));
    }

    
private Mono<OAuth2Request> createOAuth2Request_migrated(TokenRequest tokenRequest, Client client, User endUser) {
        return Mono.just(tokenRequest.createOAuth2Request()).map(RxJavaReactorMigrationUtil.toJdkFunction(oAuth2Request -> {
                    if (endUser != null) {
                        oAuth2Request.setSubject(endUser.getId());
                    }
                    oAuth2Request.setSupportRefreshToken(isSupportRefreshToken(client));
                    return oAuth2Request;
                }));
    }

    
private Mono<Token> createAccessToken_migrated(OAuth2Request oAuth2Request, Client client, User endUser) {
        return tokenService.create_migrated(oAuth2Request, client, endUser);
    }

    protected boolean isSupportRefreshToken(Client client) {
        return supportRefreshToken && client.getAuthorizedGrantTypes().contains(GrantType.REFRESH_TOKEN);
    }

    public TokenRequestResolver getTokenRequestResolver() {
        return tokenRequestResolver;
    }

    public void setTokenRequestResolver(TokenRequestResolver tokenRequestResolver) {
        this.tokenRequestResolver = tokenRequestResolver;
    }

    public TokenService getTokenService() {
        return tokenService;
    }

    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

}

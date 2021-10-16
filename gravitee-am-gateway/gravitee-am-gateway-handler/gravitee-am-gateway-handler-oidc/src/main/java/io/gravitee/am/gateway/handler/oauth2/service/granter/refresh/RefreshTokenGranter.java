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
package io.gravitee.am.gateway.handler.oauth2.service.granter.refresh;

import io.gravitee.am.common.exception.oauth2.InvalidRequestException;
import io.gravitee.am.common.oauth2.GrantType;
import io.gravitee.am.common.oauth2.Parameters;
import io.gravitee.am.gateway.handler.common.auth.user.UserAuthenticationManager;
import io.gravitee.am.gateway.handler.oauth2.exception.InvalidGrantException;
import io.gravitee.am.gateway.handler.oauth2.service.granter.AbstractTokenGranter;
import io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequest;
import io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequestResolver;
import io.gravitee.am.gateway.handler.oauth2.service.token.TokenService;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * Implementation of the Refresh Token Grant Flow
 * See <a href="https://tools.ietf.org/html/rfc6749#section-6">6. Refreshing an Access Token</a>
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RefreshTokenGranter extends AbstractTokenGranter {

    private UserAuthenticationManager userAuthenticationManager;

    public RefreshTokenGranter() {
        super(GrantType.REFRESH_TOKEN);
    }

    public RefreshTokenGranter(TokenRequestResolver tokenRequestResolver, TokenService tokenService, UserAuthenticationManager userAuthenticationManager) {
        this();
        setTokenRequestResolver(tokenRequestResolver);
        setTokenService(tokenService);
        this.userAuthenticationManager = userAuthenticationManager;
    }

    @Deprecated
@Override
    protected Single<TokenRequest> parseRequest(TokenRequest tokenRequest, Client client) {
 return RxJava2Adapter.monoToSingle(parseRequest_migrated(tokenRequest, client));
}
@Override
    protected Mono<TokenRequest> parseRequest_migrated(TokenRequest tokenRequest, Client client) {
        String refreshToken = tokenRequest.parameters().getFirst(Parameters.REFRESH_TOKEN);

        if (refreshToken == null || refreshToken.isEmpty()) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new InvalidRequestException("A refresh token must be supplied."))));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(super.parseRequest(tokenRequest, client)).flatMap(tokenRequest1->RxJava2Adapter.singleToMono(getTokenService().refresh(refreshToken, tokenRequest, client)).map(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.gateway.handler.oauth2.service.token.Token refreshToken1)->{
if (refreshToken1.getSubject() != null) {
tokenRequest1.setSubject(refreshToken1.getSubject());
}
final Set<String> originalScopes = (refreshToken1.getScope() != null ? new HashSet(Arrays.asList(refreshToken1.getScope().split("\\s+"))) : null);
final Set<String> requestedScopes = tokenRequest1.getScopes();
if (requestedScopes == null || requestedScopes.isEmpty()) {
tokenRequest1.setScopes(originalScopes);
} else if (originalScopes != null && !originalScopes.isEmpty()) {
Set<String> filteredScopes = requestedScopes.stream().filter(originalScopes::contains).collect(Collectors.toSet());
tokenRequest1.setScopes(filteredScopes);
}
tokenRequest1.setRefreshToken(refreshToken1.getAdditionalInformation());
return tokenRequest1;
})))));
    }

    @Deprecated
@Override
    protected Maybe<User> resolveResourceOwner(TokenRequest tokenRequest, Client client) {
 return RxJava2Adapter.monoToMaybe(resolveResourceOwner_migrated(tokenRequest, client));
}
@Override
    protected Mono<User> resolveResourceOwner_migrated(TokenRequest tokenRequest, Client client) {
        final String subject = tokenRequest.getSubject();

        if (subject == null) {
            return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty()));
        }

        return RxJava2Adapter.maybeToMono(userAuthenticationManager.loadPreAuthenticatedUser(subject, tokenRequest)
                .onErrorResumeNext(ex -> { return RxJava2Adapter.monoToMaybe(Mono.error(new InvalidGrantException())); }));
    }

    @Deprecated
@Override
    protected Single<TokenRequest> resolveRequest(TokenRequest tokenRequest, Client client, User endUser) {
 return RxJava2Adapter.monoToSingle(resolveRequest_migrated(tokenRequest, client, endUser));
}
@Override
    protected Mono<TokenRequest> resolveRequest_migrated(TokenRequest tokenRequest, Client client, User endUser) {
        // request has already been resolved during parse request step
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(tokenRequest)));
    }
}

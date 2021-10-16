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
package io.gravitee.am.gateway.handler.oauth2.service.token.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.oidc.ResponseType;
import io.gravitee.am.common.oidc.idtoken.Claims;
import io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request;
import io.gravitee.am.gateway.handler.oauth2.service.token.Token;
import io.gravitee.am.gateway.handler.oauth2.service.token.TokenEnhancer;
import io.gravitee.am.gateway.handler.oidc.service.idtoken.IDTokenService;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.gateway.api.ExecutionContext;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class TokenEnhancerImpl implements TokenEnhancer {

    @Autowired
    private IDTokenService idTokenService;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enhance_migrated(accessToken, oAuth2Request, client, endUser, executionContext))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Token> enhance(Token accessToken, OAuth2Request oAuth2Request, Client client, User endUser, ExecutionContext executionContext) {
 return RxJava2Adapter.monoToSingle(enhance_migrated(accessToken, oAuth2Request, client, endUser, executionContext));
}
@Override
    public Mono<Token> enhance_migrated(Token accessToken, OAuth2Request oAuth2Request, Client client, User endUser, ExecutionContext executionContext) {
        // enhance token with ID token
        if (oAuth2Request.shouldGenerateIDToken()) {
            return enhanceIDToken_migrated(accessToken, client, endUser, oAuth2Request, executionContext);
        } else {
            return Mono.just(accessToken);
        }
    }

    
private Mono<Token> enhanceIDToken_migrated(Token accessToken, Client client, User user, OAuth2Request oAuth2Request, ExecutionContext executionContext) {
        if (oAuth2Request.isSupportAtHashValue()) {
            oAuth2Request.getContext().put(Claims.at_hash, accessToken.getValue());
        }
        return idTokenService.create_migrated(oAuth2Request, client, user, executionContext).flatMap(v->RxJava2Adapter.singleToMono((Single<Token>)RxJavaReactorMigrationUtil.toJdkFunction((Function<String, Single<Token>>)idToken -> {
                    Map<String, Object> additionalInformation = new HashMap<>(accessToken.getAdditionalInformation());
                    additionalInformation.put(ResponseType.ID_TOKEN, idToken);
                    accessToken.setAdditionalInformation(additionalInformation);
                    return RxJava2Adapter.monoToSingle(Mono.just(accessToken));
                }).apply(v)));
    }
}

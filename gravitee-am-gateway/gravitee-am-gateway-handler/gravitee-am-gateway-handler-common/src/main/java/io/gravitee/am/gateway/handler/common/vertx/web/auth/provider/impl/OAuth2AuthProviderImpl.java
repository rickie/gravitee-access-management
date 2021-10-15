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
package io.gravitee.am.gateway.handler.common.vertx.web.auth.provider.impl;

import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.common.oauth2.IntrospectionTokenService;
import io.gravitee.am.gateway.handler.common.vertx.web.auth.handler.OAuth2AuthResponse;
import io.gravitee.am.gateway.handler.common.vertx.web.auth.provider.OAuth2AuthProvider;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.functions.Function;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class OAuth2AuthProviderImpl implements OAuth2AuthProvider {

    @Autowired
    private IntrospectionTokenService introspectionTokenService;

    @Autowired
    private ClientSyncService clientSyncService;

    @Override
    public void decodeToken(String token, boolean offlineVerification, Handler<AsyncResult<OAuth2AuthResponse>> handler) {
        RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(introspectionTokenService.introspect(token, offlineVerification)).flatMap(e->RxJava2Adapter.maybeToMono(clientSyncService.findByDomainAndClientId(e.getDomain(), e.getAud())).map(RxJavaReactorMigrationUtil.toJdkFunction((io.gravitee.am.model.oidc.Client client)->new OAuth2AuthResponse(e, client)))))
                .subscribe(
                        accessToken -> handler.handle(Future.succeededFuture(accessToken)),
                        error -> handler.handle(Future.failedFuture(error)));
    }
}

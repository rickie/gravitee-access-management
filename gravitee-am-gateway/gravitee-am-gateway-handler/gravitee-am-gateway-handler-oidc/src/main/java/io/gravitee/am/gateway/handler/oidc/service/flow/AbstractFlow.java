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
package io.gravitee.am.gateway.handler.oidc.service.flow;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.gateway.handler.oauth2.service.request.AuthorizationRequest;
import io.gravitee.am.gateway.handler.oauth2.service.response.AuthorizationResponse;
import io.gravitee.am.gateway.handler.oauth2.service.response.jwt.JWTAuthorizationResponse;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDDiscoveryService;
import io.gravitee.am.gateway.handler.oidc.service.jwe.JWEService;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Single;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public abstract class AbstractFlow implements Flow {

    private final List<String> responseTypes;
    private ApplicationContext applicationContext;
    private OpenIDDiscoveryService openIDDiscoveryService;
    private JWTService jwtService;
    private JWEService jweService;
    private int codeValidityInSec;

    public AbstractFlow(final List<String> responseTypes) {
        Objects.requireNonNull(responseTypes);
        this.responseTypes = responseTypes;
    }

    @Override
    public boolean handle(String responseType) {
        return responseTypes.contains(responseType);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.run_migrated(authorizationRequest, client, endUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AuthorizationResponse> run(AuthorizationRequest authorizationRequest, Client client, User endUser) {
 return RxJava2Adapter.monoToSingle(run_migrated(authorizationRequest, client, endUser));
}
@Override
    public Mono<AuthorizationResponse> run_migrated(AuthorizationRequest authorizationRequest, Client client, User endUser) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(prepareResponse(authorizationRequest, client, endUser)).flatMap(response->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(processResponse_migrated(response, authorizationRequest, client, endUser))))));
    }

    protected abstract Single<AuthorizationResponse> prepareResponse(AuthorizationRequest authorizationRequest, Client client, User endUser);
    
    protected abstract Mono<AuthorizationResponse> prepareResponse_migrated(AuthorizationRequest authorizationRequest, Client client, User endUser);

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.processResponse_migrated(authorizationResponse, authorizationRequest, client, endUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<AuthorizationResponse> processResponse(AuthorizationResponse authorizationResponse, AuthorizationRequest authorizationRequest, Client client, User endUser) {
 return RxJava2Adapter.monoToSingle(processResponse_migrated(authorizationResponse, authorizationRequest, client, endUser));
}
private Mono<AuthorizationResponse> processResponse_migrated(AuthorizationResponse authorizationResponse, AuthorizationRequest authorizationRequest, Client client, User endUser) {
        // Response Mode is not supplied by the client, process the response as usual
        if (authorizationRequest.getResponseMode() == null || !authorizationRequest.getResponseMode().endsWith("jwt")) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(authorizationResponse)));
        }

        // Create JWT Response
        JWTAuthorizationResponse jwtAuthorizationResponse = JWTAuthorizationResponse.from(authorizationResponse);
        jwtAuthorizationResponse.setIss(openIDDiscoveryService.getIssuer(authorizationRequest.getOrigin()));
        jwtAuthorizationResponse.setAud(client.getClientId());
        // JWT contains Authorization code, this JWT duration should be short
        // Because the code is persisted, we align the JWT duration with it
        jwtAuthorizationResponse.setExp(Instant.now().plusSeconds(codeValidityInSec).getEpochSecond());

        // Sign if needed, else return unsigned JWT
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(jwtService.encodeAuthorization_migrated(jwtAuthorizationResponse.build(), client))).flatMap(authorization->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(jweService.encryptAuthorization_migrated(authorization, client)))).map(RxJavaReactorMigrationUtil.toJdkFunction(token -> {
                    jwtAuthorizationResponse.setResponseType(authorizationRequest.getResponseType());
                    jwtAuthorizationResponse.setResponseMode(authorizationRequest.getResponseMode());
                    jwtAuthorizationResponse.setToken(token);
                    return jwtAuthorizationResponse;
                }))));
    }

    void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    void afterPropertiesSet() {
        this.openIDDiscoveryService = applicationContext.getBean(OpenIDDiscoveryService.class);
        this.jwtService = applicationContext.getBean(JWTService.class);
        this.jweService = applicationContext.getBean(JWEService.class);
        final Environment environment = applicationContext.getEnvironment();
        this.codeValidityInSec = environment.getProperty("authorization.code.validity", Integer.class, 60000) / 1000;
    }
}

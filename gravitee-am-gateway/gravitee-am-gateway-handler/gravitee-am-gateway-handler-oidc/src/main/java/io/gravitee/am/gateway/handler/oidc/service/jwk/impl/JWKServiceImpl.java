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
package io.gravitee.am.gateway.handler.oidc.service.jwk.impl;

import io.gravitee.am.common.web.UriBuilder;
import io.gravitee.am.gateway.handler.common.certificate.CertificateManager;
import io.gravitee.am.gateway.handler.oidc.service.jwk.JWKService;
import io.gravitee.am.gateway.handler.oidc.service.jwk.converter.JWKSetDeserializer;
import io.gravitee.am.model.jose.JWK;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.oidc.JWKSet;
import io.gravitee.am.service.exception.InvalidClientMetadataException;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class JWKServiceImpl implements JWKService {

    @Autowired
    private CertificateManager certificateManager;

    @Autowired
    @Qualifier("oidcWebClient")
    public WebClient client;

    @Override
    public Single<JWKSet> getKeys() {
        return RxJava2Adapter.monoToSingle(Flux.fromIterable(certificateManager.providers()).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(certificateProvider -> certificateProvider.getProvider().keys())).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(keys -> {
                    JWKSet jwkSet = new JWKSet();
                    jwkSet.setKeys(keys);
                    return jwkSet;
                })));
    }

    @Override
    public Maybe<JWKSet> getKeys(Client client) {
        if(client.getJwks()!=null) {
            return RxJava2Adapter.monoToMaybe(Mono.just(client.getJwks()));
        }
        else if(client.getJwksUri()!=null) {
            return getKeys(client.getJwksUri());
        }
        return RxJava2Adapter.monoToMaybe(Mono.empty());
    }

    @Override
    public Maybe<JWKSet> getDomainPrivateKeys() {
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(Flux.fromIterable(certificateManager.providers()))).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(provider -> provider.getProvider().privateKey())).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(keys -> {
                    JWKSet jwkSet = new JWKSet();
                    jwkSet.setKeys(keys);
                    return jwkSet;
                })));
    }

    @Override
    public Maybe<JWKSet> getKeys(String jwksUri) {
        try{
            return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(client.getAbs(UriBuilder.fromHttpUrl(jwksUri).build().toString())
                    .rxSend()).map(RxJavaReactorMigrationUtil.toJdkFunction(HttpResponse::bodyAsString)).map(RxJavaReactorMigrationUtil.toJdkFunction(new JWKSetDeserializer()::convert)).flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Optional<JWKSet>, MaybeSource<JWKSet>>)jwkSet -> {
                        if(jwkSet!=null && jwkSet.isPresent()) {
                            return RxJava2Adapter.monoToMaybe(Mono.just(jwkSet.get()));
                        }
                        return RxJava2Adapter.monoToMaybe(Mono.empty());
                    }).apply(e)))))
                    .onErrorResumeNext(RxJava2Adapter.monoToMaybe(Mono.error(new InvalidClientMetadataException("Unable to parse jwks from : " + jwksUri))));
        }
        catch(IllegalArgumentException | URISyntaxException ex) {
            return RxJava2Adapter.monoToMaybe(Mono.error(new InvalidClientMetadataException(jwksUri+" is not valid.")));
        }
        catch(InvalidClientMetadataException ex) {
            return RxJava2Adapter.monoToMaybe(Mono.error(ex));
        }
    }

    @Override
    public Maybe<JWK> getKey(JWKSet jwkSet, String kid) {

        if(jwkSet==null || jwkSet.getKeys().isEmpty() || kid==null || kid.trim().isEmpty()) {
            return RxJava2Adapter.monoToMaybe(Mono.empty());
        }

        //Else return matching key
        Optional<JWK> jwk = jwkSet.getKeys().stream().filter(key -> kid.equals(key.getKid())).findFirst();
        if(jwk.isPresent()) {
            return RxJava2Adapter.monoToMaybe(Mono.just(jwk.get()));
        }

        //No matching key found in JWKs...
        return RxJava2Adapter.monoToMaybe(Mono.empty());
    }

    @Override
    public Maybe<JWK> filter(JWKSet jwkSet, Predicate<JWK> filter) {
        if(jwkSet==null || jwkSet.getKeys()==null || jwkSet.getKeys().isEmpty()) {
            return RxJava2Adapter.monoToMaybe(Mono.empty());
        }

        Optional<JWK> jwk = jwkSet.getKeys()
                .stream()
                .filter(filter)
                .findFirst();

        if(jwk.isPresent()) {
            return RxJava2Adapter.monoToMaybe(Mono.just(jwk.get()));
        }
        return RxJava2Adapter.monoToMaybe(Mono.empty());
    }
}

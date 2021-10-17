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

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.web.UriBuilder;
import io.gravitee.am.gateway.handler.common.certificate.CertificateManager;
import io.gravitee.am.gateway.handler.oidc.service.jwk.JWKService;
import io.gravitee.am.gateway.handler.oidc.service.jwk.converter.JWKSetDeserializer;
import io.gravitee.am.model.jose.JWK;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.oidc.JWKSet;
import io.gravitee.am.service.exception.InvalidClientMetadataException;

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
    public Mono<JWKSet> getKeys_migrated() {
        return Flux.fromIterable(certificateManager.providers()).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(certificateProvider -> RxJava2Adapter.fluxToFlowable(certificateProvider.getProvider().keys_migrated()))).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(keys -> {
                    JWKSet jwkSet = new JWKSet();
                    jwkSet.setKeys(keys);
                    return jwkSet;
                }));
    }

    
@Override
    public Mono<JWKSet> getKeys_migrated(Client client) {
        if(client.getJwks()!=null) {
            return Mono.just(client.getJwks());
        }
        else if(client.getJwksUri()!=null) {
            return getKeys_migrated(client.getJwksUri());
        }
        return Mono.empty();
    }

    
@Override
    public Mono<JWKSet> getDomainPrivateKeys_migrated() {
        return Flux.fromIterable(certificateManager.providers()).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(provider -> RxJava2Adapter.fluxToFlowable(provider.getProvider().privateKey_migrated()))).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(keys -> {
                    JWKSet jwkSet = new JWKSet();
                    jwkSet.setKeys(keys);
                    return jwkSet;
                }));
    }

    
@Override
    public Mono<JWKSet> getKeys_migrated(String jwksUri) {
        try{
            return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(client.getAbs(UriBuilder.fromHttpUrl(jwksUri).build().toString())
                    .rxSend()).map(RxJavaReactorMigrationUtil.toJdkFunction(HttpResponse::bodyAsString)).map(RxJavaReactorMigrationUtil.toJdkFunction(new JWKSetDeserializer()::convert)).flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Optional<JWKSet>, MaybeSource<JWKSet>>)jwkSet -> {
                        if(jwkSet!=null && jwkSet.isPresent()) {
                            return RxJava2Adapter.monoToMaybe(Mono.just(jwkSet.get()));
                        }
                        return RxJava2Adapter.monoToMaybe(Mono.empty());
                    }).apply(e)))))
                    .onErrorResumeNext(RxJava2Adapter.monoToMaybe(Mono.error(new InvalidClientMetadataException("Unable to parse jwks from : " + jwksUri)))));
        }
        catch(IllegalArgumentException | URISyntaxException ex) {
            return Mono.error(new InvalidClientMetadataException(jwksUri+" is not valid."));
        }
        catch(InvalidClientMetadataException ex) {
            return Mono.error(ex);
        }
    }

    
@Override
    public Mono<JWK> getKey_migrated(JWKSet jwkSet, String kid) {

        if(jwkSet==null || jwkSet.getKeys().isEmpty() || kid==null || kid.trim().isEmpty()) {
            return Mono.empty();
        }

        //Else return matching key
        Optional<JWK> jwk = jwkSet.getKeys().stream().filter(key -> kid.equals(key.getKid())).findFirst();
        if(jwk.isPresent()) {
            return Mono.just(jwk.get());
        }

        //No matching key found in JWKs...
        return Mono.empty();
    }

    
@Override
    public Mono<JWK> filter_migrated(JWKSet jwkSet, Predicate<JWK> filter) {
        if(jwkSet==null || jwkSet.getKeys()==null || jwkSet.getKeys().isEmpty()) {
            return Mono.empty();
        }

        Optional<JWK> jwk = jwkSet.getKeys()
                .stream()
                .filter(filter)
                .findFirst();

        if(jwk.isPresent()) {
            return Mono.just(jwk.get());
        }
        return Mono.empty();
    }
}

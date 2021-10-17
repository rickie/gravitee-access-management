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
package io.gravitee.am.gateway.handler.oidc.service.jwk;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.jose.JWK;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.oidc.JWKSet;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.function.Predicate;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface JWKService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getKeys_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<JWKSet> getKeys() {
    return RxJava2Adapter.monoToSingle(getKeys_migrated());
}
default Mono<JWKSet> getKeys_migrated() {
    return RxJava2Adapter.singleToMono(getKeys());
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getKeys_migrated(client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JWKSet> getKeys(Client client) {
    return RxJava2Adapter.monoToMaybe(getKeys_migrated(client));
}
default Mono<JWKSet> getKeys_migrated(Client client) {
    return RxJava2Adapter.maybeToMono(getKeys(client));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getDomainPrivateKeys_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JWKSet> getDomainPrivateKeys() {
    return RxJava2Adapter.monoToMaybe(getDomainPrivateKeys_migrated());
}
default Mono<JWKSet> getDomainPrivateKeys_migrated() {
    return RxJava2Adapter.maybeToMono(getDomainPrivateKeys());
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getKeys_migrated(jwksUri))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JWKSet> getKeys(String jwksUri) {
    return RxJava2Adapter.monoToMaybe(getKeys_migrated(jwksUri));
}
default Mono<JWKSet> getKeys_migrated(String jwksUri) {
    return RxJava2Adapter.maybeToMono(getKeys(jwksUri));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getKey_migrated(jwkSet, kid))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JWK> getKey(JWKSet jwkSet, String kid) {
    return RxJava2Adapter.monoToMaybe(getKey_migrated(jwkSet, kid));
}
default Mono<JWK> getKey_migrated(JWKSet jwkSet, String kid) {
    return RxJava2Adapter.maybeToMono(getKey(jwkSet, kid));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.filter_migrated(jwkSet, filter))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<JWK> filter(JWKSet jwkSet, Predicate<JWK> filter) {
    return RxJava2Adapter.monoToMaybe(filter_migrated(jwkSet, filter));
}
default Mono<JWK> filter_migrated(JWKSet jwkSet, Predicate<JWK> filter) {
    return RxJava2Adapter.maybeToMono(filter(jwkSet, filter));
}
}

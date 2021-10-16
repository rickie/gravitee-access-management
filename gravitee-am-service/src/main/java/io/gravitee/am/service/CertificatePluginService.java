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
package io.gravitee.am.service;

import io.gravitee.am.service.model.plugin.CertificatePlugin;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CertificatePluginService {

      @Deprecated  
default io.reactivex.Single<java.util.Set<io.gravitee.am.service.model.plugin.CertificatePlugin>> findAll() {
    return RxJava2Adapter.monoToSingle(findAll_migrated());
}
default reactor.core.publisher.Mono<java.util.Set<io.gravitee.am.service.model.plugin.CertificatePlugin>> findAll_migrated() {
    return RxJava2Adapter.singleToMono(findAll());
}

      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.service.model.plugin.CertificatePlugin> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.service.model.plugin.CertificatePlugin> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @Deprecated  
default io.reactivex.Maybe<java.lang.String> getSchema(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(getSchema_migrated(id));
}
default reactor.core.publisher.Mono<java.lang.String> getSchema_migrated(String id) {
    return RxJava2Adapter.maybeToMono(getSchema(id));
}
}

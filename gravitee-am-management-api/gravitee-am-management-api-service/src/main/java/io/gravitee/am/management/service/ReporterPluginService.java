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
package io.gravitee.am.management.service;

import com.google.errorprone.annotations.InlineMe;




import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ReporterPluginService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.service.model.plugin.ReporterPlugin>> findAll() {
    return RxJava2Adapter.monoToSingle(findAll_migrated());
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.service.model.plugin.ReporterPlugin>> findAll_migrated() {
    return RxJava2Adapter.singleToMono(findAll());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(reporterId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.service.model.plugin.ReporterPlugin> findById(java.lang.String reporterId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(reporterId));
}
default reactor.core.publisher.Mono<io.gravitee.am.service.model.plugin.ReporterPlugin> findById_migrated(String reporterId) {
    return RxJava2Adapter.maybeToMono(findById(reporterId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getSchema_migrated(reporterId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<java.lang.String> getSchema(java.lang.String reporterId) {
    return RxJava2Adapter.monoToMaybe(getSchema_migrated(reporterId));
}
default reactor.core.publisher.Mono<java.lang.String> getSchema_migrated(String reporterId) {
    return RxJava2Adapter.maybeToMono(getSchema(reporterId));
}
}

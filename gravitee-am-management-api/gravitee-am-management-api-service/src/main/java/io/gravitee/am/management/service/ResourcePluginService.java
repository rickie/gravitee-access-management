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
import io.gravitee.am.service.model.plugin.ResourcePlugin;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ResourcePluginService {
    String MANIFEST_KEY_CATEGORIES = "categories";
    String EXPAND_ICON = "icon";

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(expand))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<List<ResourcePlugin>> findAll(List<String> expand) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(expand));
}
default Mono<List<ResourcePlugin>> findAll_migrated(List<String> expand) {
    return RxJava2Adapter.singleToMono(findAll(expand));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<ResourcePlugin> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<ResourcePlugin> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getSchema_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<String> getSchema(String id) {
    return RxJava2Adapter.monoToMaybe(getSchema_migrated(id));
}
default Mono<String> getSchema_migrated(String id) {
    return RxJava2Adapter.maybeToMono(getSchema(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getIcon_migrated(resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<String> getIcon(String resourceId) {
    return RxJava2Adapter.monoToMaybe(getIcon_migrated(resourceId));
}
default Mono<String> getIcon_migrated(String resourceId) {
    return RxJava2Adapter.maybeToMono(getIcon(resourceId));
}
}

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
import io.gravitee.am.service.model.plugin.IdentityProviderPlugin;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface IdentityProviderPluginService {

    String EXPAND_DISPLAY_NAME = "displayName";
    String EXPAND_ICON = "icon";
    String EXPAND_LABELS = "labels";

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(expand))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.service.model.plugin.IdentityProviderPlugin>> findAll(java.util.List<java.lang.String> expand) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(expand));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.service.model.plugin.IdentityProviderPlugin>> findAll_migrated(List<String> expand) {
    return RxJava2Adapter.singleToMono(findAll(expand));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(external))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.service.model.plugin.IdentityProviderPlugin>> findAll(java.lang.Boolean external) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(external));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.service.model.plugin.IdentityProviderPlugin>> findAll_migrated(Boolean external) {
    return RxJava2Adapter.singleToMono(findAll(external));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(external, expand))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<io.gravitee.am.service.model.plugin.IdentityProviderPlugin>> findAll(java.lang.Boolean external, java.util.List<java.lang.String> expand) {
    return RxJava2Adapter.monoToSingle(findAll_migrated(external, expand));
}
default reactor.core.publisher.Mono<java.util.List<io.gravitee.am.service.model.plugin.IdentityProviderPlugin>> findAll_migrated(Boolean external, List<String> expand) {
    return RxJava2Adapter.singleToMono(findAll(external, expand));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(identityProviderPlugin))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.service.model.plugin.IdentityProviderPlugin> findById(java.lang.String identityProviderPlugin) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(identityProviderPlugin));
}
default reactor.core.publisher.Mono<io.gravitee.am.service.model.plugin.IdentityProviderPlugin> findById_migrated(String identityProviderPlugin) {
    return RxJava2Adapter.maybeToMono(findById(identityProviderPlugin));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getSchema_migrated(identityProviderPlugin))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<java.lang.String> getSchema(java.lang.String identityProviderPlugin) {
    return RxJava2Adapter.monoToMaybe(getSchema_migrated(identityProviderPlugin));
}
default reactor.core.publisher.Mono<java.lang.String> getSchema_migrated(String identityProviderPlugin) {
    return RxJava2Adapter.maybeToMono(getSchema(identityProviderPlugin));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getIcon_migrated(identityProviderPlugin))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<java.lang.String> getIcon(java.lang.String identityProviderPlugin) {
    return RxJava2Adapter.monoToMaybe(getIcon_migrated(identityProviderPlugin));
}
default reactor.core.publisher.Mono<java.lang.String> getIcon_migrated(String identityProviderPlugin) {
    return RxJava2Adapter.maybeToMono(getIcon(identityProviderPlugin));
}
}

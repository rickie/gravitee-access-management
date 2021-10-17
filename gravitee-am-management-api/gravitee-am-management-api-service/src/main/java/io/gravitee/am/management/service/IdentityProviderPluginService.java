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

import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface IdentityProviderPluginService {

    String EXPAND_DISPLAY_NAME = "displayName";
    String EXPAND_ICON = "icon";
    String EXPAND_LABELS = "labels";

      
Mono<List<IdentityProviderPlugin>> findAll_migrated(List<String> expand);

      
Mono<List<IdentityProviderPlugin>> findAll_migrated(Boolean external);

      
Mono<List<IdentityProviderPlugin>> findAll_migrated(Boolean external, List<String> expand);

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(identityProviderPlugin))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<IdentityProviderPlugin> findById(String identityProviderPlugin) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(identityProviderPlugin));
}
default Mono<IdentityProviderPlugin> findById_migrated(String identityProviderPlugin) {
    return RxJava2Adapter.maybeToMono(findById(identityProviderPlugin));
}

      
Mono<String> getSchema_migrated(String identityProviderPlugin);

      
Mono<String> getIcon_migrated(String identityProviderPlugin);
}

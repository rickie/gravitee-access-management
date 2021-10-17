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
package io.gravitee.am.management.service.impl.plugins;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.management.service.FactorPluginService;
import io.gravitee.am.plugins.factor.core.FactorPluginManager;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.plugin.FactorPlugin;
import io.gravitee.plugin.core.api.Plugin;
import io.reactivex.Maybe;


import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class FactorPluginServiceImpl implements FactorPluginService {

    private final Logger LOGGER = LoggerFactory.getLogger(FactorPluginServiceImpl.class);

    @Autowired
    private FactorPluginManager factorPluginManager;

    
@Override
    public Mono<List<FactorPlugin>> findAll_migrated() {
        LOGGER.debug("List all factor plugins");
        return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToObservable(Flux.fromIterable(factorPluginManager.getAll()))
                .map(this::convert)
                .toList());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(factorId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<FactorPlugin> findById(String factorId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(factorId));
}
@Override
    public Mono<FactorPlugin> findById_migrated(String factorId) {
        LOGGER.debug("Find factor plugin by ID: {}", factorId);
        return RxJava2Adapter.maybeToMono(Maybe.create(emitter -> {
            try {
                Plugin authenticator = factorPluginManager.findById(factorId);
                if (authenticator != null) {
                    emitter.onSuccess(convert(authenticator));
                } else {
                    emitter.onComplete();
                }
            } catch (Exception ex) {
                LOGGER.error("An error occurs while trying to get factor plugin : {}", factorId, ex);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get factor plugin : " + factorId, ex));
            }
        }));
    }

    
@Override
    public Mono<String> getSchema_migrated(String factorId) {
        LOGGER.debug("Find authenticator plugin schema by ID: {}", factorId);
        return RxJava2Adapter.maybeToMono(Maybe.create(emitter -> {
            try {
                String schema = factorPluginManager.getSchema(factorId);
                if (schema != null) {
                    emitter.onSuccess(schema);
                } else {
                    emitter.onComplete();
                }
            } catch (Exception e) {
                LOGGER.error("An error occurs while trying to get schema for factor plugin {}", factorId, e);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get schema for factor plugin " + factorId, e));
            }
        }));
    }

    private FactorPlugin convert(Plugin factorPlugin) {
        FactorPlugin plugin = new FactorPlugin();
        plugin.setId(factorPlugin.manifest().id());
        plugin.setName(factorPlugin.manifest().name());
        plugin.setDescription(factorPlugin.manifest().description());
        plugin.setVersion(factorPlugin.manifest().version());
        plugin.setCategory(factorPlugin.manifest().category());
        return plugin;
    }
}

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
import io.gravitee.am.management.service.BotDetectionPluginService;
import io.gravitee.am.plugins.botdetection.core.BotDetectionPluginManager;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.plugin.BotDetectionPlugin;
import io.gravitee.plugin.core.api.Plugin;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class BotDetectionPluginServiceImpl implements BotDetectionPluginService {

    private final Logger LOGGER = LoggerFactory.getLogger(BotDetectionPluginServiceImpl.class);

    @Autowired
    private BotDetectionPluginManager pluginManager;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<List<BotDetectionPlugin>> findAll() {
 return RxJava2Adapter.monoToSingle(findAll_migrated());
}
@Override
    public Mono<List<BotDetectionPlugin>> findAll_migrated() {
        LOGGER.debug("List all bot detection plugins");
        return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToObservable(Flux.fromIterable(pluginManager.getAll()))
                .map(this::convert)
                .toList());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(pluginId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<BotDetectionPlugin> findById(String pluginId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(pluginId));
}
@Override
    public Mono<BotDetectionPlugin> findById_migrated(String pluginId) {
        LOGGER.debug("Find bot detection plugin by ID: {}", pluginId);
        return RxJava2Adapter.maybeToMono(Maybe.create(emitter -> {
            try {
                Plugin resource = pluginManager.findById(pluginId);
                if (resource != null) {
                    emitter.onSuccess(convert(resource));
                } else {
                    emitter.onComplete();
                }
            } catch (Exception ex) {
                LOGGER.error("An error occurs while trying to get bot detection plugin : {}", pluginId, ex);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get bot detection plugin : " + pluginId, ex));
            }
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getSchema_migrated(pluginId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<String> getSchema(String pluginId) {
 return RxJava2Adapter.monoToMaybe(getSchema_migrated(pluginId));
}
@Override
    public Mono<String> getSchema_migrated(String pluginId) {
        LOGGER.debug("Find bot detection plugin schema by ID: {}", pluginId);
        return RxJava2Adapter.maybeToMono(Maybe.create(emitter -> {
            try {
                String schema = pluginManager.getSchema(pluginId);
                if (schema != null) {
                    emitter.onSuccess(schema);
                } else {
                    emitter.onComplete();
                }
            } catch (Exception e) {
                LOGGER.error("An error occurs while trying to get schema for bot detection plugin {}", pluginId, e);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get schema for bot detection plugin " + pluginId, e));
            }
        }));
    }

    private BotDetectionPlugin convert(Plugin botDetectionPlugin) {
        BotDetectionPlugin plugin = new BotDetectionPlugin();
        plugin.setId(botDetectionPlugin.manifest().id());
        plugin.setName(botDetectionPlugin.manifest().name());
        plugin.setDescription(botDetectionPlugin.manifest().description());
        plugin.setVersion(botDetectionPlugin.manifest().version());
        plugin.setCategory(botDetectionPlugin.manifest().category());
        return plugin;
    }
}
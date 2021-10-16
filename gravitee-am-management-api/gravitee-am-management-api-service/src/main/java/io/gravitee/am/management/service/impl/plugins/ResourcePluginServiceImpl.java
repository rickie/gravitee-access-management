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
import io.gravitee.am.management.service.IdentityProviderPluginService;
import io.gravitee.am.management.service.ResourcePluginService;
import io.gravitee.am.plugins.resource.core.ResourcePluginManager;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.plugin.ResourcePlugin;
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
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ResourcePluginServiceImpl implements ResourcePluginService {

    private final Logger LOGGER = LoggerFactory.getLogger(ResourcePluginServiceImpl.class);

    @Autowired
    private ResourcePluginManager resourcePluginManager;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(expand))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<List<ResourcePlugin>> findAll(List<String> expand) {
 return RxJava2Adapter.monoToSingle(findAll_migrated(expand));
}
@Override
    public Mono<List<ResourcePlugin>> findAll_migrated(List<String> expand) {
        LOGGER.debug("List all resource plugins");
        return RxJava2Adapter.singleToMono(Observable.fromIterable(resourcePluginManager.getAll())
                .map(plugin -> convert(plugin, expand))
                .toList());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ResourcePlugin> findById(String resourceId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(resourceId));
}
@Override
    public Mono<ResourcePlugin> findById_migrated(String resourceId) {
        LOGGER.debug("Find resource plugin by ID: {}", resourceId);
        return RxJava2Adapter.maybeToMono(Maybe.create(emitter -> {
            try {
                Plugin resource = resourcePluginManager.findById(resourceId);
                if (resource != null) {
                    emitter.onSuccess(convert(resource));
                } else {
                    emitter.onComplete();
                }
            } catch (Exception ex) {
                LOGGER.error("An error occurs while trying to get resource plugin : {}", resourceId, ex);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get resource plugin : " + resourceId, ex));
            }
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getSchema_migrated(resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<String> getSchema(String resourceId) {
 return RxJava2Adapter.monoToMaybe(getSchema_migrated(resourceId));
}
@Override
    public Mono<String> getSchema_migrated(String resourceId) {
        LOGGER.debug("Find resource plugin schema by ID: {}", resourceId);
        return RxJava2Adapter.maybeToMono(Maybe.create(emitter -> {
            try {
                String schema = resourcePluginManager.getSchema(resourceId);
                if (schema != null) {
                    emitter.onSuccess(schema);
                } else {
                    emitter.onComplete();
                }
            } catch (Exception e) {
                LOGGER.error("An error occurs while trying to get schema for resource plugin {}", resourceId, e);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get schema for resource plugin " + resourceId, e));
            }
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getIcon_migrated(resourceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<String> getIcon(String resourceId) {
 return RxJava2Adapter.monoToMaybe(getIcon_migrated(resourceId));
}
@Override
    public Mono<String> getIcon_migrated(String resourceId) {
        LOGGER.debug("Find resource plugin icon by ID: {}", resourceId);
        return RxJava2Adapter.maybeToMono(Maybe.create(emitter -> {
            try {
                String icon = resourcePluginManager.getIcon(resourceId);
                if (icon != null) {
                    emitter.onSuccess(icon);
                } else {
                    emitter.onComplete();
                }
            } catch (Exception e) {
                LOGGER.error("An error has occurred when trying to get icon for resource plugin {}", resourceId, e);
                emitter.onError(new TechnicalManagementException("An error has occurred when trying to get icon for resource plugin " + resourceId, e));
            }
        }));
    }

    private ResourcePlugin convert(Plugin resourcePlugin) {
        return convert(resourcePlugin, null);
    }

    private ResourcePlugin convert(Plugin resourcePlugin, List<String> expand) {
        ResourcePlugin plugin = new ResourcePlugin();
        plugin.setId(resourcePlugin.manifest().id());
        plugin.setName(resourcePlugin.manifest().name());
        plugin.setDescription(resourcePlugin.manifest().description());
        plugin.setVersion(resourcePlugin.manifest().version());
        String tags = resourcePlugin.manifest().properties().get(MANIFEST_KEY_CATEGORIES);
        if (tags != null) {
            plugin.setCategories(tags.split(","));
        } else {
            plugin.setCategories(new String[0]);
        }
        if (expand != null) {
            if (expand.contains(ResourcePluginService.EXPAND_ICON)) {
                this.getIcon_migrated(plugin.getId()).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(plugin::setIcon));
            }
        }
        return plugin;
    }
}

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
import io.gravitee.am.management.service.ReporterPluginService;
import io.gravitee.am.plugins.reporter.core.ReporterPluginManager;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.plugin.ReporterPlugin;
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

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ReporterPluginServiceImpl implements ReporterPluginService {

    private final Logger LOGGER = LoggerFactory.getLogger(ReporterPluginServiceImpl.class);

    @Autowired
    private ReporterPluginManager reporterPluginManager;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<List<ReporterPlugin>> findAll() {
 return RxJava2Adapter.monoToSingle(findAll_migrated());
}
@Override
    public Mono<List<ReporterPlugin>> findAll_migrated() {
        LOGGER.debug("List all reporter plugins");
        return RxJava2Adapter.singleToMono(Observable.fromIterable(reporterPluginManager.getAll())
                .map(this::convert)
                .toList());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(reporterId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ReporterPlugin> findById(String reporterId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(reporterId));
}
@Override
    public Mono<ReporterPlugin> findById_migrated(String reporterId) {
        LOGGER.debug("Find reporter plugin by ID: {}", reporterId);
        return RxJava2Adapter.maybeToMono(Maybe.create(emitter -> {
            try {
                Plugin reporter = reporterPluginManager.findById(reporterId);
                if (reporter != null) {
                    emitter.onSuccess(convert(reporter));
                } else {
                    emitter.onComplete();
                }
            } catch (Exception ex) {
                LOGGER.error("An error occurs while trying to get reporter plugin : {}", reporterId, ex);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get reporter plugin : " + reporterId, ex));
            }
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getSchema_migrated(reporterId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<String> getSchema(String reporterId) {
 return RxJava2Adapter.monoToMaybe(getSchema_migrated(reporterId));
}
@Override
    public Mono<String> getSchema_migrated(String reporterId) {
        LOGGER.debug("Find reporter plugin schema by ID: {}", reporterId);
        return RxJava2Adapter.maybeToMono(Maybe.create(emitter -> {
            try {
                String schema = reporterPluginManager.getSchema(reporterId);
                if (schema != null) {
                    emitter.onSuccess(schema);
                } else {
                    emitter.onComplete();
                }
            } catch (Exception e) {
                LOGGER.error("An error occurs while trying to get schema for reporter plugin {}", reporterId, e);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get schema for reporter plugin " + reporterId, e));
            }
        }));
    }

    private ReporterPlugin convert(Plugin reporterPlugin) {
        ReporterPlugin plugin = new ReporterPlugin();
        plugin.setId(reporterPlugin.manifest().id());
        plugin.setName(reporterPlugin.manifest().name());
        plugin.setDescription(reporterPlugin.manifest().description());
        plugin.setVersion(reporterPlugin.manifest().version());
        return plugin;
    }
}

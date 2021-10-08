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

package io.gravitee.am.management.service.impl;

import io.gravitee.am.management.service.RememberDevicePluginService;
import io.gravitee.am.plugins.rememberdevice.core.RememberDevicePluginManager;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.plugin.RememberDevicePlugin;
import io.gravitee.plugin.core.api.Plugin;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class RememberDevicePluginServiceImpl implements RememberDevicePluginService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RememberDevicePluginServiceImpl.class);

    @Autowired
    private RememberDevicePluginManager rememberDevicePluginManager;

    @Override
    public Single<List<RememberDevicePlugin>> findAll() {
        LOGGER.debug("List all Remember Device plugins");
        return Observable.fromIterable(rememberDevicePluginManager.getAll())
                .map(this::convert)
                .toList();
    }

    @Override
    public Maybe<RememberDevicePlugin> findById(String rememberDeviceId) {
        LOGGER.debug("Find remember device plugin by ID: {}", rememberDeviceId);
        return Maybe.create(emitter -> {
            try {
                var rememberDevicePlugin = rememberDevicePluginManager.findById(rememberDeviceId);
                Optional.ofNullable(rememberDevicePlugin).ifPresentOrElse(
                        plugin -> emitter.onSuccess(convert(plugin)),
                        emitter::onComplete
                );
            } catch (Exception ex) {
                LOGGER.error("An error occurs while trying to get remember device plugin : {}", rememberDeviceId, ex);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get factor plugin : " + rememberDeviceId, ex));
            }
        });
    }

    @Override
    public Maybe<String> getSchema(String factorId) {
        LOGGER.debug("Find authenticator plugin schema by ID: {}", factorId);
        return Maybe.create(emitter -> {
            try {
                var schema = rememberDevicePluginManager.getSchema(factorId);
                Optional.ofNullable(schema).ifPresentOrElse(
                        emitter::onSuccess,
                        emitter::onComplete
                );
            } catch (Exception e) {
                LOGGER.error("An error occurs while trying to get schema for remember device plugin {}", factorId, e);
                emitter.onError(new TechnicalManagementException("An error occurs while trying to get schema for remember device plugin " + factorId, e));
            }
        });
    }


    private RememberDevicePlugin convert(Plugin rememberDevice) {
        var plugin = new RememberDevicePlugin();
        plugin.setId(rememberDevice.manifest().id());
        plugin.setName(rememberDevice.manifest().name());
        plugin.setDescription(rememberDevice.manifest().description());
        plugin.setVersion(rememberDevice.manifest().version());
        plugin.setCategory(rememberDevice.manifest().category());
        return plugin;
    }
}

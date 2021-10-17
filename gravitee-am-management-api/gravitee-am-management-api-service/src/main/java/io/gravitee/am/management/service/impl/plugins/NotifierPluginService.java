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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.management.service.exception.NotifierPluginNotFoundException;
import io.gravitee.am.management.service.exception.NotifierPluginSchemaNotFoundException;
import io.gravitee.am.plugins.notifier.core.NotifierPluginManager;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.plugin.NotifierPlugin;
import io.gravitee.plugin.core.api.Plugin;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class NotifierPluginService {

    public static String EXPAND_ICON = "icon";
    public static final String DEFAULT_NOTIFIER_MESSAGE = "An alert '${alert.name}' has been fired.\\n"
            + "\\n"
            + "Date: ${notification.timestamp?number?number_to_datetime}\\n"
            + "Domain: ${domain.name} (${domain.id})\\n"
            + "Application: ${application.name} (${application.id})\\n"
            + "User: ${notification.properties['user']}\\n"
            + "Alert: ${alert.description}\\n"
            + "Technical message: ${notification.message}";

    private final NotifierPluginManager notifierPluginManager;
    private final ObjectMapper objectMapper;

    public NotifierPluginService(@Lazy NotifierPluginManager notifierPluginManager, ObjectMapper objectMapper) {
        this.notifierPluginManager = notifierPluginManager;
        this.objectMapper = objectMapper;
    }

    
public Flux<NotifierPlugin> findAll_migrated(String... expand) {
        return Flux.fromIterable(notifierPluginManager.findAll()).flatMap(e->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<io.gravitee.plugin.notifier.NotifierPlugin, Single<NotifierPlugin>>toJdkFunction(plugin -> RxJava2Adapter.monoToSingle(convert_migrated(plugin, expand))).apply(e))).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(throwable -> {
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to get notifier plugins", throwable)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(notifierId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
public Single<NotifierPlugin> findById(String notifierId) {
 return RxJava2Adapter.monoToSingle(findById_migrated(notifierId));
}
public Mono<NotifierPlugin> findById_migrated(String notifierId) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.fromSupplier(() -> notifierPluginManager.findById(notifierId)).flatMap(this::convert_migrated))
                .onErrorResumeNext(throwable -> {
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException("An error occurs while trying to get notifier plugin " + notifierId, throwable)));
                })).switchIfEmpty(Mono.defer(()->Mono.error(new NotifierPluginNotFoundException(notifierId))));
    }

    
public Mono<String> getSchema_migrated(String notifierId) {

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(() -> notifierPluginManager.getSchema(notifierId))).map(RxJavaReactorMigrationUtil.toJdkFunction(objectMapper::readTree)).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(jsonSchema -> {
                    final JsonNode propertiesNode = jsonSchema.get("properties");
                    JsonNode messageNode = null;
                    if (propertiesNode instanceof ObjectNode) {
                        if (propertiesNode.has("message")) {
                            messageNode = propertiesNode.get("message");
                        } else if (propertiesNode.has("body")) {
                            messageNode = propertiesNode.get("body");
                        }
                    }

                    if (messageNode instanceof ObjectNode) {
                        ((ObjectNode) messageNode).put("default", DEFAULT_NOTIFIER_MESSAGE);
                    }
                })).map(RxJavaReactorMigrationUtil.toJdkFunction(JsonNode::toString)))
                .onErrorResumeNext(throwable -> {
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException("An error occurs while trying to get schema for notifier plugin " + notifierId, throwable)));
                })).switchIfEmpty(Mono.defer(()->Mono.error(new NotifierPluginSchemaNotFoundException(notifierId))));
    }

    
public Mono<String> getIcon_migrated(String notifierId) {

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(() -> notifierPluginManager.getIcon(notifierId))))
                .onErrorResumeNext(throwable -> {
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException("An error occurs while trying to get incon for notifier plugin " + notifierId, throwable)));
                }));
    }

    
public Mono<String> getDocumentation_migrated(String notifierId) {

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(() -> notifierPluginManager.getDocumentation(notifierId))))
                .onErrorResumeNext(throwable -> {
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException("An error occurs while trying to get documentation for notifier plugin " + notifierId, throwable)));
                }));
    }

    
private Mono<NotifierPlugin> convert_migrated(Plugin plugin, String... expand) {
        NotifierPlugin notifierPlugin = new NotifierPlugin();
        notifierPlugin.setId(plugin.manifest().id());
        notifierPlugin.setName(plugin.manifest().name());
        notifierPlugin.setDescription(plugin.manifest().description());
        notifierPlugin.setVersion(plugin.manifest().version());


        if (expand != null) {
            final List<String> expandList = Arrays.asList(expand);
            if (expandList.contains(EXPAND_ICON)) {
                return this.getIcon_migrated(notifierPlugin.getId()).doOnSuccess(notifierPlugin::setIcon).then().then(Mono.just(notifierPlugin));
            }
        }

        return Mono.just(notifierPlugin);
    }
}

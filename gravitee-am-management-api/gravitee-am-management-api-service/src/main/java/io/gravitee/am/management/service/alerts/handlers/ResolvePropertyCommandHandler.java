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
package io.gravitee.am.management.service.alerts.handlers;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.alert.api.trigger.TriggerProvider;
import io.gravitee.alert.api.trigger.command.Command;
import io.gravitee.alert.api.trigger.command.Handler;
import io.gravitee.alert.api.trigger.command.ResolvePropertyCommand;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.DomainService;
import io.reactivex.Single;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ResolvePropertyCommandHandler implements TriggerProvider.OnCommandResultListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResolvePropertyCommandHandler.class);
    private static final String RESOLVE_DOMAIN_PROPERTIES_KEY = "domain";
    private static final String RESOLVE_APPLICATION_PROPERTIES_KEY = "application";

    private final DomainService domainService;
    private final ApplicationService applicationService;

    public ResolvePropertyCommandHandler(DomainService domainService, ApplicationService applicationService) {
        this.domainService = domainService;
        this.applicationService = applicationService;
    }

    @Override
    public <T> void doOnCommand(Command command, Handler<T> resultHandler) {
        LOGGER.debug("Received a command from alert engine {}.", command);
        if (command instanceof ResolvePropertyCommand) {
            resolveProperties_migrated((ResolvePropertyCommand) command).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(result -> resultHandler.handle((T) result)), RxJavaReactorMigrationUtil.toJdkConsumer(error -> resultHandler.handle(null)));
        } else {
            LOGGER.warn("Unknown alert command: {}", command);
            resultHandler.handle(null);
        }
    }


    
private Mono<Map<String,Map<String,Object>>> resolveProperties_migrated(ResolvePropertyCommand command) {

        Map<String, String> commandProperties = command.getProperties();
        Map<String, Map<String, Object>> values = new HashMap<>();

        List<Single<Map<String, Object>>> obs = new ArrayList<>();

        if (commandProperties != null) {
            commandProperties.forEach((key, value) -> {
                if (RESOLVE_DOMAIN_PROPERTIES_KEY.equals(key)) {
                    obs.add(RxJava2Adapter.monoToSingle(resolveDomainProperties_migrated(value).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(domainProperties -> values.put(key, domainProperties)))));
                } else if (RESOLVE_APPLICATION_PROPERTIES_KEY.equals(key)) {
                    obs.add(RxJava2Adapter.monoToSingle(resolveApplicationProperties_migrated(value).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(appProperties -> values.put(key, appProperties)))));
                }
            });
        }

        return RxJava2Adapter.flowableToFlux(Single.merge(obs)).ignoreElements().then().then(Mono.just(values));
    }

    
private Mono<Map<String,Object>> resolveDomainProperties_migrated(String domainId) {

        final Map<String, Object> properties = new HashMap<>();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domainId))
                .flatMapSingle(domain -> {
                    properties.put("id", domain.getId());
                    properties.put("name", domain.getName());
                    properties.put("description", domain.getDescription());
                    properties.put("tags", domain.getTags());

                    return RxJava2Adapter.monoToSingle(Mono.just(properties));
                })
                .onErrorResumeNext(RxJava2Adapter.monoToSingle(Mono.just(properties))));
    }

    
private Mono<Map<String,Object>> resolveApplicationProperties_migrated(String applicationId) {

        final Map<String, Object> properties = new HashMap<>();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(applicationService.findById_migrated(applicationId))
                .flatMapSingle(application -> {
                    properties.put("id", application.getId());
                    properties.put("name", application.getName());
                    properties.put("description", application.getDescription());
                    properties.put("type", application.getType());
                    properties.put("metadata", application.getMetadata());

                    return RxJava2Adapter.monoToSingle(Mono.just(properties));
                })
                .onErrorResumeNext(RxJava2Adapter.monoToSingle(Mono.just(properties))));
    }
}

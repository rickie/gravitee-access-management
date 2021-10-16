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
package io.gravitee.am.management.service.impl.upgrades;

import static io.gravitee.am.management.service.impl.upgrades.UpgraderOrder.APPLICATION_SCOPE_SETTINGS_UPGRADER;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.SystemTask;
import io.gravitee.am.model.SystemTaskStatus;
import io.gravitee.am.model.SystemTaskTypes;
import io.gravitee.am.model.application.ApplicationOAuthSettings;
import io.gravitee.am.model.application.ApplicationScopeSettings;
import io.gravitee.am.repository.management.api.ApplicationRepository;
import io.gravitee.am.repository.management.api.SystemTaskRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ApplicationScopeSettingsUpgrader implements Upgrader, Ordered {
    private static final String TASK_ID = "scope_settings_migration";
    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ApplicationScopeSettingsUpgrader.class);

    @Autowired
    @Lazy
    private SystemTaskRepository systemTaskRepository;

    @Autowired
    @Lazy
    private ApplicationRepository applicationRepository;

    @Override
    public boolean upgrade() {
        final String instanceOperationId = UUID.randomUUID().toString();
        boolean upgraded = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(systemTaskRepository.findById_migrated(TASK_ID).switchIfEmpty(Mono.defer(()->createSystemTask_migrated(instanceOperationId))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<SystemTask, SingleSource<Boolean>>toJdkFunction(task -> {
                    switch (SystemTaskStatus.valueOf(task.getStatus())) {
                        case INITIALIZED:
                            return RxJava2Adapter.monoToSingle(processUpgrade_migrated(instanceOperationId, task, instanceOperationId));
                        case FAILURE:
                            // In Failure case, we use the operationId of the read task otherwise update will always fail
                            // force the task.operationId to assign the task to the instance
                            String previousOperationId = task.getOperationId();
                            task.setOperationId(instanceOperationId);
                            return RxJava2Adapter.monoToSingle(processUpgrade_migrated(instanceOperationId, task, previousOperationId));
                        case ONGOING:
                            // wait until status change
                            return RxJava2Adapter.monoToSingle(Mono.error(new IllegalStateException("ONGOING task " + TASK_ID + " : trigger a retry")));
                        default:
                            // SUCCESS case
                            return RxJava2Adapter.monoToSingle(Mono.just(true));
                    }
                }).apply(v))))).retryWhen(new RetryWithDelay(3, 5000))).block();

        if (!upgraded) {
            throw new IllegalStateException("Settings for Application Scopes can't be upgraded, other instance may process them or an upgrader has failed previously");
        }

        return upgraded;
    }

    
private Mono<Boolean> processUpgrade_migrated(String instanceOperationId, SystemTask task, String conditionalOperationId) {
        return updateSystemTask_migrated(task, (SystemTaskStatus.ONGOING), conditionalOperationId).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<SystemTask, SingleSource<Boolean>>toJdkFunction(updatedTask -> {
                    if (updatedTask.getOperationId().equals(instanceOperationId)) {
                        return RxJava2Adapter.monoToSingle(migrateScopeSettings_migrated(updatedTask));
                    } else {
                        return RxJava2Adapter.monoToSingle(Mono.error(new IllegalStateException("Task " + TASK_ID + " already processed by another instance : trigger a retry")));
                    }
                }).apply(v)))).map(RxJavaReactorMigrationUtil.toJdkFunction(__ -> true));
    }

    
private Mono<SystemTask> createSystemTask_migrated(String operationId) {
        SystemTask  systemTask = new SystemTask();
        systemTask.setId(TASK_ID);
        systemTask.setType(SystemTaskTypes.UPGRADER.name());
        systemTask.setStatus(SystemTaskStatus.INITIALIZED.name());
        systemTask.setCreatedAt(new Date());
        systemTask.setUpdatedAt(systemTask.getCreatedAt());
        systemTask.setOperationId(operationId);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(systemTaskRepository.create_migrated(systemTask)).onErrorResumeNext(err -> {
            logger.warn("SystemTask {} can't be created due to '{}'", TASK_ID, err.getMessage());
            // if the creation fails, try to find the task, this will allow to manage the retry properly
            return RxJava2Adapter.monoToSingle(systemTaskRepository.findById_migrated(systemTask.getId()).single());
        }));
    }

    
private Mono<SystemTask>  updateSystemTask_migrated(SystemTask task, SystemTaskStatus status, String operationId) {
        task.setUpdatedAt(new Date());
        task.setStatus(status.name());
        return systemTaskRepository.updateIf_migrated(task, operationId);
    }

    
private Mono<Boolean> migrateScopeSettings_migrated(SystemTask task) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(applicationRepository.findAll_migrated()).flatMapSingle(app -> {
                    logger.debug("Process application '{}'", app.getId());
                    if (app.getSettings() != null && app.getSettings().getOauth() != null) {
                        final ApplicationOAuthSettings oauthSettings = app.getSettings().getOauth();
                        List<ApplicationScopeSettings> scopeSettings = new ArrayList<>();
                        if (oauthSettings.getScopes() != null && !oauthSettings.getScopes().isEmpty()) {
                            logger.debug("Process scope options for application '{}'", app.getId());
                            for (String scope: oauthSettings.getScopes()) {
                                ApplicationScopeSettings setting = new ApplicationScopeSettings();
                                setting.setScope(scope);
                                setting.setDefaultScope(oauthSettings.getDefaultScopes() != null && oauthSettings.getDefaultScopes().contains(scope));
                                if (oauthSettings.getScopeApprovals() != null && oauthSettings.getScopeApprovals().containsKey(scope)) {
                                    setting.setScopeApproval(oauthSettings.getScopeApprovals().get(scope));
                                }
                                scopeSettings.add(setting);
                            }

                            oauthSettings.setScopeSettings(scopeSettings);

                            // remove old values
                            oauthSettings.setScopes(null);
                            oauthSettings.setDefaultScopes(null);
                            oauthSettings.setScopeApprovals(null);

                            logger.debug("Update settings for application '{}'", app.getId());
                            return RxJava2Adapter.monoToSingle(applicationRepository.update_migrated(app));
                        } else {
                            logger.debug("No scope to process for application '{}'", app.getId());
                        }
                    } else {
                        logger.debug("No scope to process for application '{}'", app.getId());
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(app));
                })).ignoreElements().then().doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(err -> RxJava2Adapter.monoToSingle(updateSystemTask_migrated(task, (SystemTaskStatus.FAILURE), task.getOperationId())).subscribe())).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(updateSystemTask_migrated(task, SystemTaskStatus.SUCCESS, task.getOperationId()).map(RxJavaReactorMigrationUtil.toJdkFunction(__ -> true)))
                        .onErrorResumeNext((err) -> {
                            logger.error("Unable to update status for migrate scope options task: {}", err.getMessage());
                            return RxJava2Adapter.monoToSingle(Mono.just(false));
                        }))))
                .onErrorResumeNext((err) -> {
                    logger.error("Unable to migrate scope options for applications: {}", err.getMessage());
                    return RxJava2Adapter.monoToSingle(Mono.just(false));
                }));
    }

    @Override
    public int getOrder() {
        return APPLICATION_SCOPE_SETTINGS_UPGRADER;
    }

    private class RetryWithDelay implements Function<Flowable<Throwable>, Publisher<?>> {
        private final int maxRetries;
        private final int retryDelayMillis;
        private int retryCount;

        public RetryWithDelay(int retries, int delay) {
            this.maxRetries = retries;
            this.retryDelayMillis = delay;
            this.retryCount = 0;
        }

        @Override
        public Publisher<?> apply(@NonNull Flowable<Throwable> attempts) throws Exception {
            return RxJava2Adapter.flowableToFlux(attempts).flatMap(RxJavaReactorMigrationUtil.toJdkFunction((throwable) -> {
                        if (++retryCount < maxRetries) {
                            // When this Observable calls onNext, the original
                            // Observable will be retried (i.e. re-subscribed).
                            return Flowable.timer(retryDelayMillis * (retryCount + 1),
                                    TimeUnit.MILLISECONDS);
                        }
                        // Max retries hit. Just pass the error along.
                        return RxJava2Adapter.fluxToFlowable(Flux.error(throwable));
                    }));
        }
    }
}

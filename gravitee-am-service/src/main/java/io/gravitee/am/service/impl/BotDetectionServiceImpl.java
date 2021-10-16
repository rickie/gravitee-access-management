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
package io.gravitee.am.service.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.event.Action;
import io.gravitee.am.common.event.Type;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.BotDetection;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.BotDetectionRepository;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.BotDetectionNotFoundException;
import io.gravitee.am.service.exception.BotDetectionUsedException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewBotDetection;
import io.gravitee.am.service.model.UpdateBotDetection;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.BotDetectionAuditBuilder;
import io.reactivex.*;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.Date;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
public class BotDetectionServiceImpl implements BotDetectionService {
    /**
     * Logger.
     */
    private final Logger LOGGER = LoggerFactory.getLogger(BotDetectionServiceImpl.class);

    @Lazy
    @Autowired
    private BotDetectionRepository botDetectionRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<BotDetection> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<BotDetection> findById_migrated(String id) {
        LOGGER.debug("Find bot detection by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(botDetectionRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a bot detection using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a bot detection using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<BotDetection> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<BotDetection> findByDomain_migrated(String domain) {
        LOGGER.debug("Find bot detections by domain: {}", domain);
        return botDetectionRepository.findByReference_migrated(ReferenceType.DOMAIN, domain).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find bot detections by domain", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find bot detections by domain", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newBotDetection, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<BotDetection> create(String domain, NewBotDetection newBotDetection, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newBotDetection, principal));
}
@Override
    public Mono<BotDetection> create_migrated(String domain, NewBotDetection newBotDetection, User principal) {
        LOGGER.debug("Create a new bot detection {} for domain {}", newBotDetection, domain);

        BotDetection botDetection = new BotDetection();
        botDetection.setId(newBotDetection.getId() == null ? RandomString.generate() : newBotDetection.getId());
        botDetection.setReferenceId(domain);
        botDetection.setReferenceType(ReferenceType.DOMAIN);
        botDetection.setName(newBotDetection.getName());
        botDetection.setType(newBotDetection.getType());
        botDetection.setDetectionType(newBotDetection.getDetectionType());
        botDetection.setConfiguration(newBotDetection.getConfiguration());
        botDetection.setCreatedAt(new Date());
        botDetection.setUpdatedAt(botDetection.getCreatedAt());

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(botDetectionRepository.create_migrated(botDetection).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<BotDetection, SingleSource<BotDetection>>toJdkFunction(detection -> {
                    // create event for sync process
                    Event event = new Event(Type.BOT_DETECTION, new Payload(detection.getId(), detection.getReferenceType(), detection.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(detection)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a detection", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a detection", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(detection -> auditService.report(AuditBuilder.builder(BotDetectionAuditBuilder.class).principal(principal).type(EventType.BOT_DETECTION_CREATED).botDetection(detection)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(BotDetectionAuditBuilder.class).principal(principal).type(EventType.BOT_DETECTION_CREATED).throwable(throwable))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateBotDetection, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<BotDetection> update(String domain, String id, UpdateBotDetection updateBotDetection, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateBotDetection, principal));
}
@Override
    public Mono<BotDetection> update_migrated(String domain, String id, UpdateBotDetection updateBotDetection, User principal) {
        LOGGER.debug("Update bot detection {} for domain {}", id, domain);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(botDetectionRepository.findById_migrated(id).switchIfEmpty(Mono.error(new BotDetectionNotFoundException(id))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<BotDetection, SingleSource<BotDetection>>toJdkFunction(oldBotDetection -> {
                    BotDetection botDetectionToUpdate = new BotDetection(oldBotDetection);
                    botDetectionToUpdate.setName(updateBotDetection.getName());
                    botDetectionToUpdate.setConfiguration(updateBotDetection.getConfiguration());
                    botDetectionToUpdate.setUpdatedAt(new Date());

                    return  RxJava2Adapter.monoToSingle(botDetectionRepository.update_migrated(botDetectionToUpdate).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<BotDetection, SingleSource<BotDetection>>toJdkFunction(detection -> {
                                // create event for sync process
                                Event event = new Event(Type.BOT_DETECTION, new Payload(detection.getId(), detection.getReferenceType(), detection.getReferenceId(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(detection)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(detection -> auditService.report(AuditBuilder.builder(BotDetectionAuditBuilder.class).principal(principal).type(EventType.BOT_DETECTION_UPDATED).oldValue(oldBotDetection).botDetection(detection)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(BotDetectionAuditBuilder.class).principal(principal).type(EventType.BOT_DETECTION_UPDATED).throwable(throwable)))));
                }).apply(y)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update bot detection", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update bot detection", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domainId, botDetectionId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String domainId, String botDetectionId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(domainId, botDetectionId, principal));
}
@Override
    public Mono<Void> delete_migrated(String domainId, String botDetectionId, User principal) {
        LOGGER.debug("Delete bot detection {}", botDetectionId);

        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(botDetectionRepository.findById_migrated(botDetectionId).switchIfEmpty(Mono.error(new BotDetectionNotFoundException(botDetectionId))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<BotDetection, SingleSource<? extends BotDetection>>toJdkFunction(checkBotDetectionReleasedByDomain(domainId, botDetectionId)).apply(y)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<BotDetection, SingleSource<? extends BotDetection>>toJdkFunction(checkBotDetectionReleasedByApp(domainId, botDetectionId)).apply(v)))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<BotDetection, CompletableSource>)botDetection -> {
                    // create event for sync process
                    Event event = new Event(Type.BOT_DETECTION, new Payload(botDetectionId, ReferenceType.DOMAIN, domainId, Action.DELETE));
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(botDetectionRepository.delete_migrated(botDetectionId).then(eventService.create_migrated(event)))
                            .toCompletable()
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(BotDetectionAuditBuilder.class).principal(principal).type(EventType.BOT_DETECTION_DELETED).botDetection(botDetection)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(BotDetectionAuditBuilder.class).principal(principal).type(EventType.BOT_DETECTION_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete bot detection: {}", botDetectionId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete bot detection: %s", botDetectionId), ex)));
                }));
    }

    private Function<BotDetection, SingleSource<? extends BotDetection>> checkBotDetectionReleasedByApp(String domainId, String botDetectionId) {
        return botDetection -> RxJava2Adapter.monoToSingle(applicationService.findByDomain_migrated(domainId).flatMap(v->RxJava2Adapter.singleToMono((Single<BotDetection>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Set<Application>, Single<BotDetection>>)applications -> {
                    if (applications.stream().filter(app -> app.getSettings() != null &&
                            app.getSettings().getAccount() != null &&
                            botDetectionId.equals(app.getSettings().getAccount().getBotDetectionPlugin())).count() > 0) {
                        throw new BotDetectionUsedException();
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(botDetection));
                }).apply(v))));
    }

    private Function<BotDetection, SingleSource<? extends BotDetection>> checkBotDetectionReleasedByDomain(String domainId, String botDetectionId) {
        return botDetection -> RxJava2Adapter.monoToMaybe(domainService.findById_migrated(domainId))
                .flatMapSingle(domain -> {
                    if (domain.getAccountSettings() != null &&
                            botDetectionId.equals(domain.getAccountSettings().getBotDetectionPlugin())) {
                        throw new BotDetectionUsedException();
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(botDetection));
                });
    }
}

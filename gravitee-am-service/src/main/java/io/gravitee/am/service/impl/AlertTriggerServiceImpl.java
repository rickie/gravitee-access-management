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
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.alert.AlertTrigger;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.AlertTriggerRepository;
import io.gravitee.am.repository.management.api.search.AlertTriggerCriteria;
import io.gravitee.am.service.AlertTriggerService;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.exception.AlertTriggerNotFoundException;
import io.gravitee.am.service.model.PatchAlertTrigger;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.AlertTriggerAuditBuilder;
import io.reactivex.Completable;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class AlertTriggerServiceImpl implements AlertTriggerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertTriggerServiceImpl.class);

    private final AlertTriggerRepository alertTriggerRepository;
    private final AuditService auditService;
    private final EventService eventService;

    public AlertTriggerServiceImpl(@Lazy AlertTriggerRepository alertTriggerRepository, AuditService auditService, EventService eventService) {
        this.alertTriggerRepository = alertTriggerRepository;
        this.auditService = auditService;
        this.eventService = eventService;
    }

    /**
     * Find the alert trigger corresponding to the specified id.
     *
     * @param id the alert trigger identifier.
     * @return the alert trigger found or an {@link AlertTriggerNotFoundException} if no alert trigger has been found.
     */
    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AlertTrigger> getById(String id) {
 return RxJava2Adapter.monoToSingle(getById_migrated(id));
}
@Override
    public Mono<AlertTrigger> getById_migrated(String id) {
        LOGGER.debug("Find alert trigger by id: {}", id);

        return alertTriggerRepository.findById_migrated(id).switchIfEmpty(Mono.error(new AlertTriggerNotFoundException(id)));
    }


    /**
     * Find the alert trigger corresponding to the specified id and its reference.
     *
     * @param referenceType the reference type.
     * @param referenceId the reference id.
     * @param id the alert trigger identifier.
     * @return the alert trigger found or an {@link AlertTriggerNotFoundException} if no alert trigger has been found.
     */
    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AlertTrigger> getById(ReferenceType referenceType, String referenceId, String id) {
 return RxJava2Adapter.monoToSingle(getById_migrated(referenceType, referenceId, id));
}
@Override
    public Mono<AlertTrigger> getById_migrated(ReferenceType referenceType, String referenceId, String id) {
        LOGGER.debug("Find alert trigger by id: {}", id);

        return alertTriggerRepository.findById_migrated(id).filter(RxJavaReactorMigrationUtil.toJdkPredicate(alertTrigger -> alertTrigger.getReferenceType() == referenceType && alertTrigger.getReferenceId().equals(referenceId))).switchIfEmpty(Mono.error(new AlertTriggerNotFoundException(id)));
    }

    /**
     * Find all alert triggers of a domain and matching the specified criteria.
     *
     * @param domainId the id of the domain the alert trigger is attached to.
     * @param criteria the criteria to match.
     * @return the alert triggers found or empty if none has been found.
     */
    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndCriteria_migrated(domainId, criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<AlertTrigger> findByDomainAndCriteria(String domainId, AlertTriggerCriteria criteria) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndCriteria_migrated(domainId, criteria));
}
@Override
    public Flux<AlertTrigger> findByDomainAndCriteria_migrated(String domainId, AlertTriggerCriteria criteria) {
        LOGGER.debug("Find alert trigger by domain {} and criteria: {}", domainId, criteria);

        return alertTriggerRepository.findByCriteria_migrated(ReferenceType.DOMAIN, domainId, criteria);
    }

    /**
     * Create or update an alert trigger.
     * Note: alert trigger are predefined. There is at most one trigger of each type for a given reference.
     *
     * @param referenceType the reference type.
     * @param referenceId the reference id.
     * @param patchAlertTrigger the information on the alert trigger to create or update.
     * @param byUser the user at the origin of the update.
     * @return the created or updated alert trigger.
     */
    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.createOrUpdate_migrated(referenceType, referenceId, patchAlertTrigger, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AlertTrigger> createOrUpdate(ReferenceType referenceType, String referenceId, PatchAlertTrigger patchAlertTrigger, User byUser) {
 return RxJava2Adapter.monoToSingle(createOrUpdate_migrated(referenceType, referenceId, patchAlertTrigger, byUser));
}
@Override
    public Mono<AlertTrigger> createOrUpdate_migrated(ReferenceType referenceType, String referenceId, PatchAlertTrigger patchAlertTrigger, User byUser) {
        LOGGER.debug("Create or update alert trigger for {} {}: {}", referenceType, referenceId, patchAlertTrigger);

        final AlertTriggerCriteria criteria = new AlertTriggerCriteria();
        criteria.setType(patchAlertTrigger.getType());

        return alertTriggerRepository.findByCriteria_migrated(referenceType, referenceId, criteria).next().flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<AlertTrigger, MaybeSource<AlertTrigger>>toJdkFunction(alertTrigger -> {
                    AlertTrigger toUpdate = patchAlertTrigger.patch(alertTrigger);

                    if (toUpdate.equals(alertTrigger)) {
                        // Do not update alert trigger if nothing has changed.
                        return RxJava2Adapter.monoToMaybe(Mono.just(alertTrigger));
                    }
                    return RxJava2Adapter.monoToMaybe(updateInternal_migrated(toUpdate, byUser, alertTrigger));
                }).apply(v)))).switchIfEmpty(RxJava2Adapter.singleToMono(Single.defer(() -> {
                    AlertTrigger alertTrigger = new AlertTrigger();
                    alertTrigger.setId(RandomString.generate());
                    alertTrigger.setReferenceType(referenceType);
                    alertTrigger.setReferenceId(referenceId);
                    alertTrigger.setType(patchAlertTrigger.getType());
                    alertTrigger = patchAlertTrigger.patch(alertTrigger);

                    return RxJava2Adapter.monoToSingle(createInternal_migrated(alertTrigger, byUser));
                })));
    }

    /**
     * Delete the alert trigger by its id and reference it belongs to.
     *
     * @param referenceType the reference type.
     * @param referenceId the reference id.
     * @param alertTriggerId the alert trigger identifier.
     * @return nothing if the alert trigger has been successfully delete or an {@link AlertTriggerNotFoundException} exception if it has not been found.
     */
    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(referenceType, referenceId, alertTriggerId, byUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(ReferenceType referenceType, String referenceId, String alertTriggerId, User byUser) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, alertTriggerId, byUser));
}
@Override
    public Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String alertTriggerId, User byUser) {
        return this.getById_migrated(referenceType, referenceId, alertTriggerId).flatMap(alertTrigger->deleteInternal_migrated(alertTrigger, byUser)).then();
    }

    
private Mono<AlertTrigger> createInternal_migrated(AlertTrigger toCreate, User byUser) {

        Date now = new Date();

        toCreate.setCreatedAt(now);
        toCreate.setUpdatedAt(now);

        return alertTriggerRepository.create_migrated(toCreate).flatMap(created->eventService.create_migrated(new Event(Type.ALERT_TRIGGER, new Payload(created.getId(), created.getReferenceType(), created.getReferenceId(), Action.CREATE))).then().then(Mono.just(created))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(alertTrigger -> auditService.report(AuditBuilder.builder(AlertTriggerAuditBuilder.class).type(EventType.ALERT_TRIGGER_CREATED).alertTrigger(alertTrigger).principal(byUser)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(AlertTriggerAuditBuilder.class).type(EventType.ALERT_TRIGGER_CREATED).alertTrigger(toCreate).principal(byUser).throwable(throwable))));
    }

    
private Mono<AlertTrigger> updateInternal_migrated(AlertTrigger alertTrigger, User updatedBy, AlertTrigger previous) {

        alertTrigger.setUpdatedAt(new Date());

        return alertTriggerRepository.update_migrated(alertTrigger).flatMap(updated->eventService.create_migrated(new Event(Type.ALERT_TRIGGER, new Payload(updated.getId(), updated.getReferenceType(), updated.getReferenceId(), Action.UPDATE))).then().then(Mono.just(updated))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(updated -> auditService.report(AuditBuilder.builder(AlertTriggerAuditBuilder.class).type(EventType.ALERT_TRIGGER_UPDATED).alertTrigger(updated).principal(updatedBy).oldValue(previous)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(AlertTriggerAuditBuilder.class).type(EventType.ALERT_TRIGGER_UPDATED).alertTrigger(previous).principal(updatedBy).throwable(throwable))));
    }


    
private Mono<Void> deleteInternal_migrated(AlertTrigger alertTrigger, User deletedBy) {
        return RxJava2Adapter.monoToCompletable(alertTriggerRepository.delete_migrated(alertTrigger.getId()).then(eventService.create_migrated(new Event(Type.ALERT_TRIGGER, new Payload(alertTrigger.getId(), alertTrigger.getReferenceType(), alertTrigger.getReferenceId(), Action.DELETE))).then()))
                .doOnComplete(() -> auditService.report(AuditBuilder.builder(AlertTriggerAuditBuilder.class).type(EventType.ALERT_TRIGGER_DELETED).alertTrigger(alertTrigger).principal(deletedBy))).as(RxJava2Adapter::completableToMono).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(AlertTriggerAuditBuilder.class).type(EventType.ALERT_TRIGGER_DELETED).alertTrigger(alertTrigger).principal(deletedBy).throwable(throwable))));
    }
}
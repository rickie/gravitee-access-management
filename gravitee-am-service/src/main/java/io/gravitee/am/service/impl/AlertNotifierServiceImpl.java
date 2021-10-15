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

import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.event.Action;
import io.gravitee.am.common.event.Type;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.alert.AlertNotifier;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.AlertNotifierRepository;
import io.gravitee.am.repository.management.api.search.AlertNotifierCriteria;
import io.gravitee.am.service.AlertNotifierService;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.exception.AlertNotifierNotFoundException;
import io.gravitee.am.service.model.NewAlertNotifier;
import io.gravitee.am.service.model.PatchAlertNotifier;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.AlertNotifierAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class AlertNotifierServiceImpl implements AlertNotifierService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertNotifierServiceImpl.class);

    private final AlertNotifierRepository alertNotifierRepository;
    private final AuditService auditService;
    private final EventService eventService;

    public AlertNotifierServiceImpl(@Lazy AlertNotifierRepository alertNotifierRepository, AuditService auditService, EventService eventService) {
        this.alertNotifierRepository = alertNotifierRepository;
        this.auditService = auditService;
        this.eventService = eventService;
    }

    /**
     * Get the alert notifier by its id and reference it belongs to.
     *
     * @param referenceType the reference type.
     * @param referenceId the reference id.
     * @param notifierId the notifier identifier.
     * @return the alert notifier found or an {@link AlertNotifierNotFoundException} exception if it has not been found.
     */
    @Override
    public Single<AlertNotifier> getById(ReferenceType referenceType, String referenceId, String notifierId) {
        LOGGER.debug("Find alert notifier by id {}", notifierId);

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(this.alertNotifierRepository.findById(notifierId)).filter(RxJavaReactorMigrationUtil.toJdkPredicate(alertNotifier -> alertNotifier.getReferenceType() == referenceType && alertNotifier.getReferenceId().equals(referenceId))).switchIfEmpty(Mono.error(new AlertNotifierNotFoundException(notifierId))));
    }

    /**
     * Find all the alert notifiers of a domain corresponding to the specified criteria.
     *
     * @param domainId the domain the alert notifiers are attached to.
     * @param criteria the criteria to match.
     * @return the list of alert notifiers found.
     */
    @Override
    public Flowable<AlertNotifier> findByDomainAndCriteria(String domainId, AlertNotifierCriteria criteria) {
        return findByReferenceAndCriteria(ReferenceType.DOMAIN, domainId, criteria);
    }

    /**
     * Find all the alert notifiers by reference (ex: domain) corresponding to the specified criteria.
     *
     * @param referenceType the reference type.
     * @param referenceId the reference id.
     * @param criteria the criteria to match.
     * @return the list of alert notifiers found.
     */
    @Override
    public Flowable<AlertNotifier> findByReferenceAndCriteria(ReferenceType referenceType, String referenceId, AlertNotifierCriteria criteria) {
        LOGGER.debug("Find alert notifier by {} {} and criteria {}", referenceType, referenceId, criteria);

        return alertNotifierRepository.findByCriteria(referenceType, referenceId, criteria);
    }

    /**
     * Create a new alert notifier.
     *
     * @param referenceType the reference type the newly created alert notifier will be attached to.
     * @param referenceId the reference id the newly created alert notifier will be attached to.
     * @param newAlertNotifier the information about the alert notifier to create.
     * @param byUser the user at the origin of the creation.
     * @return the newly created alert notifier.
     */
    @Override
    public Single<AlertNotifier> create(ReferenceType referenceType, String referenceId, NewAlertNotifier newAlertNotifier, User byUser) {
        LOGGER.debug("Create alert notifier for {} {}: {}", referenceType, referenceId, newAlertNotifier);

        final AlertNotifier alertNotifier = newAlertNotifier.toAlertNotifier();
        alertNotifier.setReferenceType(referenceType);
        alertNotifier.setReferenceId(referenceId);

        return this.createInternal(alertNotifier, byUser);
    }

    /**
     * Update an existing alert notifier.
     *
     * @param referenceType the reference type the newly created alert notifier will be attached to.
     * @param referenceId the reference id the newly created alert notifier will be attached to.
     * @param patchAlertNotifier the information about the alert notifier to update.
     * @param byUser the user at the origin of the update.
     * @return the updated alert notifier or a {@link AlertNotifierNotFoundException} if the notifier has not been found.
     */
    @Override
    public Single<AlertNotifier> update(ReferenceType referenceType, String referenceId, String alertNotifierId, PatchAlertNotifier patchAlertNotifier, User byUser) {
        LOGGER.debug("Update alert notifier for {}: {}", alertNotifierId, patchAlertNotifier);

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.getById(referenceType, referenceId, alertNotifierId)).flatMap(v->RxJava2Adapter.singleToMono((Single<AlertNotifier>)RxJavaReactorMigrationUtil.toJdkFunction((Function<AlertNotifier, Single<AlertNotifier>>)alertNotifier -> {
                    AlertNotifier toUpdate = patchAlertNotifier.patch(alertNotifier);

                    if (toUpdate.equals(alertNotifier)) {
                        // Do not update alert notifier if nothing has changed.
                        return RxJava2Adapter.monoToSingle(Mono.just(alertNotifier));
                    }
                    return updateInternal(toUpdate, byUser, alertNotifier);
                }).apply(v))));
    }

    /**
     * Delete the alert notifier by its id and reference it belongs to.
     *
     * @param referenceType the reference type.
     * @param referenceId the reference id.
     * @param notifierId the notifier identifier.
     * @return nothing if the alert notifier has been successfully delete or an {@link AlertNotifierNotFoundException} exception if it has not been found.
     */
    @Override
    public Completable delete(ReferenceType referenceType, String referenceId, String notifierId, User byUser) {
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(this.getById(referenceType, referenceId, notifierId)).flatMap(alertNotifier->RxJava2Adapter.completableToMono(deleteInternal(alertNotifier, byUser))).then());
    }

    private Single<AlertNotifier> createInternal(AlertNotifier toCreate, User byUser) {

        Date now = new Date();

        toCreate.setId(RandomString.generate());
        toCreate.setCreatedAt(now);
        toCreate.setUpdatedAt(now);

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(alertNotifierRepository.create(toCreate)).flatMap(updated->RxJava2Adapter.singleToMono(eventService.create(new Event(Type.ALERT_NOTIFIER, new Payload(updated.getId(), updated.getReferenceType(), updated.getReferenceId(), Action.CREATE))).ignoreElement().andThen(Single.just(updated)))))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(alertTrigger -> auditService.report(AuditBuilder.builder(AlertNotifierAuditBuilder.class).type(EventType.ALERT_NOTIFIER_CREATED).alertNotifier(alertTrigger).principal(byUser)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(AlertNotifierAuditBuilder.class).type(EventType.ALERT_NOTIFIER_CREATED).alertNotifier(toCreate).principal(byUser).throwable(throwable)))));
    }

    private Single<AlertNotifier> updateInternal(AlertNotifier alertNotifier, User updatedBy, AlertNotifier previous) {

        alertNotifier.setUpdatedAt(new Date());

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(alertNotifierRepository.update(alertNotifier)).flatMap(updated->RxJava2Adapter.singleToMono(eventService.create(new Event(Type.ALERT_NOTIFIER, new Payload(updated.getId(), updated.getReferenceType(), updated.getReferenceId(), Action.UPDATE))).ignoreElement().andThen(Single.just(updated)))))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(updated -> auditService.report(AuditBuilder.builder(AlertNotifierAuditBuilder.class).type(EventType.ALERT_NOTIFIER_UPDATED).alertNotifier(updated).principal(updatedBy).oldValue(previous)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(AlertNotifierAuditBuilder.class).type(EventType.ALERT_NOTIFIER_UPDATED).alertNotifier(previous).principal(updatedBy).throwable(throwable)))));
    }

    private Completable deleteInternal(AlertNotifier alertNotifier, User deletedBy) {
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(alertNotifierRepository.delete(alertNotifier.getId())).then(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(eventService.create(new Event(Type.ALERT_NOTIFIER, new Payload(alertNotifier.getId(), alertNotifier.getReferenceType(), alertNotifier.getReferenceId(), Action.DELETE)))).then()))))
                .doOnComplete(() -> auditService.report(AuditBuilder.builder(AlertNotifierAuditBuilder.class).type(EventType.ALERT_NOTIFIER_DELETED).alertNotifier(alertNotifier).principal(deletedBy))).as(RxJava2Adapter::completableToMono).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(AlertNotifierAuditBuilder.class).type(EventType.ALERT_NOTIFIER_DELETED).alertNotifier(alertNotifier).principal(deletedBy).throwable(throwable)))).as(RxJava2Adapter::monoToCompletable);
    }
}

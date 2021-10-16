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
package io.gravitee.am.management.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.alert.api.trigger.Trigger;
import io.gravitee.alert.api.trigger.TriggerProvider;
import io.gravitee.am.common.event.AlertNotifierEvent;
import io.gravitee.am.common.event.AlertTriggerEvent;
import io.gravitee.am.common.event.DomainEvent;
import io.gravitee.am.management.service.alerts.AlertTriggerFactory;
import io.gravitee.am.management.service.alerts.handlers.AlertNotificationCommandHandler;
import io.gravitee.am.management.service.alerts.handlers.ResolvePropertyCommandHandler;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.alert.AlertTrigger;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.search.AlertNotifierCriteria;
import io.gravitee.am.repository.management.api.search.AlertTriggerCriteria;
import io.gravitee.am.repository.management.api.search.DomainCriteria;
import io.gravitee.am.service.AlertNotifierService;
import io.gravitee.am.service.AlertTriggerService;
import io.gravitee.am.service.DomainService;
import io.gravitee.common.event.Event;
import io.gravitee.common.event.EventManager;
import io.gravitee.common.service.AbstractService;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
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
public class AlertTriggerManager extends AbstractService<CertificateManager> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertTriggerManager.class);

    private final TriggerProvider triggerProvider;
    private final AlertTriggerService alertTriggerService;
    private final AlertNotifierService alertNotifierService;
    private final DomainService domainService;
    private final EventManager eventManager;
    private final Environment environment;
    private final ResolvePropertyCommandHandler resolvePropertyCommandHandler;
    private final AlertNotificationCommandHandler alertNotificationCommandHandler;

    public AlertTriggerManager(TriggerProvider triggerProvider, AlertTriggerService alertTriggerService, AlertNotifierService alertNotifierService, DomainService domainService, EventManager eventManager, Environment environment, ResolvePropertyCommandHandler resolvePropertyCommandHandler, AlertNotificationCommandHandler alertNotificationCommandHandler) {
        this.triggerProvider = triggerProvider;
        this.alertTriggerService = alertTriggerService;
        this.alertNotifierService = alertNotifierService;
        this.domainService = domainService;
        this.eventManager = eventManager;
        this.environment = environment;
        this.resolvePropertyCommandHandler = resolvePropertyCommandHandler;
        this.alertNotificationCommandHandler = alertNotificationCommandHandler;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        triggerProvider.addListener((TriggerProvider.OnConnectionListener) this::doOnConnect);
        triggerProvider.addListener((TriggerProvider.OnDisconnectionListener) this::doOnDisconnect);
        triggerProvider.addListener(alertNotificationCommandHandler);
        triggerProvider.addListener(resolvePropertyCommandHandler);

        // Subscribe to some internal events in order to propagate changes on triggers to the alert system.
        eventManager.subscribeForEvents(this::onDomainEvent, DomainEvent.class);
        eventManager.subscribeForEvents(this::onAlertTriggerEvent, AlertTriggerEvent.class);
        eventManager.subscribeForEvents(this::onAlertNotifierEvent, AlertNotifierEvent.class);
    }

    void doOnConnect() {
        LOGGER.info("Connected to alerting system. Sync alert triggers...");
        RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(domainService.findAllByCriteria(new DomainCriteria())
                .doOnNext(domain -> LOGGER.info("Sending alert triggers for domain {}", domain.getName()))).flatMap(RxJavaReactorMigrationUtil.toJdkFunction(this::prepareAETriggers)))
                .flatMapSingle(this::registerAETrigger)
                .count()
                .subscribe(count -> LOGGER.info("{} alert triggers synchronized with the alerting system.", count),
                        throwable -> LOGGER.error("An error occurred when trying to synchronize alert triggers with alerting system", throwable));
    }

    void doOnDisconnect() {
        LOGGER.warn("Connection with the alerting system has been lost.");
    }

    void onDomainEvent(Event<DomainEvent, ?> event) {

        final Payload payload = (Payload) event.content();
        domainService.findById(payload.getReferenceId())
                .flatMapPublisher(this::prepareAETriggers)
                .flatMapSingle(this::registerAETrigger)
                .count()
                .subscribe(count -> LOGGER.info("{} alert triggers synchronized with the alerting system for domain [{}].", count, payload.getReferenceId()),
                        throwable -> LOGGER.error("An error occurred when trying to synchronize alert triggers with alerting system for domain [{}]", payload.getReferenceId(), throwable));
    }

    void onAlertTriggerEvent(Event<AlertTriggerEvent, ?> event) {

        LOGGER.debug("Received alert trigger event {}", event);

        final Payload payload = (Payload) event.content();
        domainService.findById(payload.getReferenceId())
                .flatMapSingle(domain -> RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(alertTriggerService.getById(payload.getId())).flatMap(alertTrigger->RxJava2Adapter.singleToMono(this.prepareAETrigger(domain, alertTrigger))).flatMap(v->RxJava2Adapter.singleToMono((Single<Trigger>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Trigger, Single<Trigger>>)this::registerAETrigger).apply(v)))))
                .subscribe(aeTrigger -> LOGGER.info("Alert trigger [{}] synchronized with the alerting system.", aeTrigger.getId()),
                        throwable -> LOGGER.error("An error occurred when trying to synchronize alert trigger [{}] with alerting system", payload.getId(), throwable));
    }

    void onAlertNotifierEvent(Event<AlertNotifierEvent, ?> event) {

        LOGGER.debug("Received alert notifier event {}", event);

        final Payload payload = (Payload) event.content();
        final AlertTriggerCriteria alertTriggerCriteria = new AlertTriggerCriteria();
        alertTriggerCriteria.setEnabled(true);
        alertTriggerCriteria.setAlertNotifierIds(Collections.singletonList(payload.getId()));

        RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(payload.getReferenceId())).filter(RxJavaReactorMigrationUtil.toJdkPredicate(domain -> domain.isEnabled() && domain.isAlertEnabled())))
                .flatMapPublisher(domain -> this.alertTriggerService.findByDomainAndCriteria(domain.getId(), alertTriggerCriteria)
                        .flatMapSingle(alertTrigger -> prepareAETrigger(domain, alertTrigger))
                        .flatMapSingle(this::registerAETrigger))
                .count()
                .subscribe(count -> LOGGER.info("{} alert triggers synchronized with the alerting system for domain [{}] after the update of alert notifier [{}].", count, payload.getReferenceId(), payload.getId()),
                        throwable -> LOGGER.error("An error occurred when trying to synchronize alert triggers with alerting system for domain [{}] after the alert notifier {} event [{}].", payload.getReferenceId(), event.type().name().toLowerCase(), payload.getId(), throwable));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.registerAETrigger_migrated(trigger))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Trigger> registerAETrigger(Trigger trigger) {
 return RxJava2Adapter.monoToSingle(registerAETrigger_migrated(trigger));
}
private Mono<Trigger> registerAETrigger_migrated(Trigger trigger) {
        return RxJava2Adapter.singleToMono(Single.defer(() -> {
            triggerProvider.register(trigger);
            LOGGER.debug("Alert trigger [{}] has been pushed to alert system.", trigger.getId());
            return RxJava2Adapter.monoToSingle(Mono.just(trigger));
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.prepareAETriggers_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Flowable<Trigger> prepareAETriggers(Domain domain) {
 return RxJava2Adapter.fluxToFlowable(prepareAETriggers_migrated(domain));
}
private Flux<Trigger> prepareAETriggers_migrated(Domain domain) {
        return RxJava2Adapter.flowableToFlux(alertTriggerService.findByDomainAndCriteria(domain.getId(), new AlertTriggerCriteria())
                .flatMapSingle(alertTrigger -> this.prepareAETrigger(domain, alertTrigger)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.prepareAETrigger_migrated(domain, alertTrigger))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Trigger> prepareAETrigger(Domain domain, AlertTrigger alertTrigger) {
 return RxJava2Adapter.monoToSingle(prepareAETrigger_migrated(domain, alertTrigger));
}
private Mono<Trigger> prepareAETrigger_migrated(Domain domain, AlertTrigger alertTrigger) {
        final AlertNotifierCriteria alertNotifierCriteria = new AlertNotifierCriteria();
        alertNotifierCriteria.setEnabled(true);
        alertNotifierCriteria.setIds(alertTrigger.getAlertNotifiers());

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(alertNotifierService.findByReferenceAndCriteria(alertTrigger.getReferenceType(), alertTrigger.getReferenceId(), alertNotifierCriteria)).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(alertNotifiers -> AlertTriggerFactory.create(alertTrigger, alertNotifiers, environment))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(trigger -> trigger.setEnabled(domain.isEnabled() && domain.isAlertEnabled() && trigger.isEnabled())))));
    }
}

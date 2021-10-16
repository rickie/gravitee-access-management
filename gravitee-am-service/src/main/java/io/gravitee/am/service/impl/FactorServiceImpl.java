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
import io.gravitee.am.model.Factor;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.FactorRepository;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.FactorService;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewFactor;
import io.gravitee.am.service.model.UpdateFactor;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.FactorAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import java.util.*;
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
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class FactorServiceImpl implements FactorService {

    public static final String SMS_AM_FACTOR = "sms-am-factor";
    public static final String CONFIG_KEY_COUNTRY_CODES = "countryCodes";
    private static final List<String> COUNTRY_CODES = Arrays.asList(Locale.getISOCountries());
    /**
     * Logger.
     */
    private final Logger LOGGER = LoggerFactory.getLogger(FactorServiceImpl.class);

    @Lazy
    @Autowired
    private FactorRepository factorRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Factor> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Factor> findById_migrated(String id) {
        LOGGER.debug("Find factor by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(factorRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find an factor using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find an factor using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Factor> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Factor> findByDomain_migrated(String domain) {
        LOGGER.debug("Find factors by domain: {}", domain);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(factorRepository.findByDomain_migrated(domain))).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find factors by domain", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find factors by domain", ex)));
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newFactor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Factor> create(String domain, NewFactor newFactor, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newFactor, principal));
}
@Override
    public Mono<Factor> create_migrated(String domain, NewFactor newFactor, User principal) {
        LOGGER.debug("Create a new factor {} for domain {}", newFactor, domain);

        Factor factor = new Factor();
        factor.setId(newFactor.getId() == null ? RandomString.generate() : newFactor.getId());
        factor.setDomain(domain);
        factor.setName(newFactor.getName());
        factor.setType(newFactor.getType());
        factor.setFactorType(newFactor.getFactorType());
        factor.setConfiguration(newFactor.getConfiguration());
        factor.setCreatedAt(new Date());
        factor.setUpdatedAt(factor.getCreatedAt());

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(checkFactorConfiguration_migrated(factor))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Factor, SingleSource<Factor>>toJdkFunction((T ident) -> RxJava2Adapter.monoToSingle(factorRepository.create_migrated(ident))).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Factor, SingleSource<Factor>>toJdkFunction(factor1 -> {
                    // create event for sync process
                    Event event = new Event(Type.FACTOR, new Payload(factor1.getId(), ReferenceType.DOMAIN, factor1.getDomain(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event))).flatMap(__->Mono.just(factor1)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a factor", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a factor", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(factor1 -> auditService.report(AuditBuilder.builder(FactorAuditBuilder.class).principal(principal).type(EventType.FACTOR_CREATED).factor(factor1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(FactorAuditBuilder.class).principal(principal).type(EventType.FACTOR_CREATED).throwable(throwable))))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.checkFactorConfiguration_migrated(factor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Factor> checkFactorConfiguration(Factor factor) {
 return RxJava2Adapter.monoToSingle(checkFactorConfiguration_migrated(factor));
}
private Mono<Factor> checkFactorConfiguration_migrated(Factor factor) {
        if (SMS_AM_FACTOR.equalsIgnoreCase(factor.getType())) {
            // for SMS Factor, check that countries code provided into the configuration are valid
            final JsonObject configuration = (JsonObject) Json.decodeValue(factor.getConfiguration());
            String countryCodes = configuration.getString(CONFIG_KEY_COUNTRY_CODES);
            for(String code : countryCodes.split(",")) {
                if (!COUNTRY_CODES.contains(code.trim().toUpperCase(Locale.ROOT))) {
                    return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new FactorConfigurationException(CONFIG_KEY_COUNTRY_CODES, code))));
                }
            }
        }
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(factor)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateFactor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Factor> update(String domain, String id, UpdateFactor updateFactor, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateFactor, principal));
}
@Override
    public Mono<Factor> update_migrated(String domain, String id, UpdateFactor updateFactor, User principal) {
        LOGGER.debug("Update an factor {} for domain {}", id, domain);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(factorRepository.findById_migrated(id))).switchIfEmpty(Mono.error(new FactorNotFoundException(id))))
                .flatMapSingle(oldFactor -> {
                    Factor factorToUpdate = new Factor(oldFactor);
                    factorToUpdate.setName(updateFactor.getName());
                    factorToUpdate.setConfiguration(updateFactor.getConfiguration());
                    factorToUpdate.setUpdatedAt(new Date());

                    return  RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(checkFactorConfiguration_migrated(factorToUpdate))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Factor, SingleSource<Factor>>toJdkFunction((T ident) -> RxJava2Adapter.monoToSingle(factorRepository.update_migrated(ident))).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Factor, SingleSource<Factor>>toJdkFunction(factor1 -> {
                                // create event for sync process
                                Event event = new Event(Type.FACTOR, new Payload(factor1.getId(), ReferenceType.DOMAIN, factor1.getDomain(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event))).flatMap(__->Mono.just(factor1)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(factor1 -> auditService.report(AuditBuilder.builder(FactorAuditBuilder.class).principal(principal).type(EventType.FACTOR_UPDATED).oldValue(oldFactor).factor(factor1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(FactorAuditBuilder.class).principal(principal).type(EventType.FACTOR_UPDATED).throwable(throwable)))));
                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a factor", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a factor", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domain, factorId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String domain, String factorId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(domain, factorId, principal));
}
@Override
    public Mono<Void> delete_migrated(String domain, String factorId, User principal) {
        LOGGER.debug("Delete factor {}", factorId);

        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(factorRepository.findById_migrated(factorId))).switchIfEmpty(Mono.error(new FactorNotFoundException(factorId))))
                .flatMapSingle(factor -> RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(applicationService.findByFactor_migrated(factorId)).count()).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Long, SingleSource<Factor>>toJdkFunction(applications -> {
                            if (applications > 0) {
                                throw new FactorWithApplicationsException();
                            }
                            return RxJava2Adapter.monoToSingle(Mono.just(factor));
                        }).apply(v))))))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Factor, CompletableSource>)factor -> {
                    // create event for sync process
                    Event event = new Event(Type.FACTOR, new Payload(factorId, ReferenceType.DOMAIN, domain, Action.DELETE));
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(factorRepository.delete_migrated(factorId))).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event)))))
                            .toCompletable()
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(FactorAuditBuilder.class).principal(principal).type(EventType.FACTOR_DELETED).factor(factor)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(FactorAuditBuilder.class).principal(principal).type(EventType.FACTOR_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete factor: {}", factorId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete factor: %s", factorId), ex)));
                }));
    }
}

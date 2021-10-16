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
import io.gravitee.am.model.Form;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Template;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.FormRepository;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.FormService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.FormAlreadyExistsException;
import io.gravitee.am.service.exception.FormNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewForm;
import io.gravitee.am.service.model.UpdateForm;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.FormTemplateAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.Date;
import java.util.List;
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
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@Component
public class FormServiceImpl implements FormService {

    private final Logger LOGGER = LoggerFactory.getLogger(FormServiceImpl.class);

    @Lazy
    @Autowired
    private FormRepository formRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @Deprecated
@Override
    public Maybe<Form> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Form> findById_migrated(String id) {
        LOGGER.debug("Find form by id {}", id);
        return RxJava2Adapter.maybeToMono(formRepository.findById(id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a form using its id {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a form using its id %s", id), ex)));
                }));
    }

    @Deprecated
@Override
    public Flowable<Form> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<Form> findAll_migrated(ReferenceType referenceType, String referenceId) {
        LOGGER.debug("Find form by {} {}", referenceType, referenceId);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(formRepository.findAll(referenceType, referenceId)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a form using its {} {}", referenceType, referenceId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a form using its %s %s", referenceType, referenceId), ex)));
                }))));
    }

    @Deprecated
@Override
    public Flowable<Form> findAll(ReferenceType referenceType) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType));
}
@Override
    public Flux<Form> findAll_migrated(ReferenceType referenceType) {
        LOGGER.debug("Find form by type {}", referenceType);
        return RxJava2Adapter.flowableToFlux(formRepository.findAll(referenceType));
    }

    @Deprecated
@Override
    public Flowable<Form> findByDomain(String domain) {
 return RxJava2Adapter.fluxToFlowable(findByDomain_migrated(domain));
}
@Override
    public Flux<Form> findByDomain_migrated(String domain) {
        return RxJava2Adapter.flowableToFlux(findAll(ReferenceType.DOMAIN, domain));
    }

    @Deprecated
@Override
    public Flowable<Form> findByClient(ReferenceType referenceType, String referenceId, String client) {
 return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
@Override
    public Flux<Form> findByClient_migrated(ReferenceType referenceType, String referenceId, String client) {
        LOGGER.debug("Find form by {} {} and client {}", referenceType, referenceId, client);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(formRepository.findByClient(referenceType, referenceId, client)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a form using its {} {} and its client {}", referenceType, referenceId, client, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a form using its %s %s and client %s", referenceType, referenceId, client), ex)));
                }))));
    }

    @Deprecated
@Override
    public Flowable<Form> findByDomainAndClient(String domain, String client) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndClient_migrated(domain, client));
}
@Override
    public Flux<Form> findByDomainAndClient_migrated(String domain, String client) {
        LOGGER.debug("Find form by domain {} and client", domain, client);
        return RxJava2Adapter.flowableToFlux(findByClient(ReferenceType.DOMAIN, domain, client));
    }

    @Deprecated
@Override
    public Maybe<Form> findByTemplate(ReferenceType referenceType, String referenceId, String template) {
 return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
@Override
    public Mono<Form> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template) {
        LOGGER.debug("Find form by {} {} and template {}", referenceType, referenceId, template);
        return RxJava2Adapter.maybeToMono(formRepository.findByTemplate(referenceType, referenceId, template)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a form using its {} {} and template {}", referenceType, referenceId, template, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a form using its domain %s %s and template %s", referenceType, referenceId, template), ex)));
                }));
    }

    @Deprecated
@Override
    public Maybe<Form> findByDomainAndTemplate(String domain, String template) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndTemplate_migrated(domain, template));
}
@Override
    public Mono<Form> findByDomainAndTemplate_migrated(String domain, String template) {
        LOGGER.debug("Find form by domain {} and template {}", domain, template);
        return RxJava2Adapter.maybeToMono(findByTemplate(ReferenceType.DOMAIN, domain, template));
    }

    @Deprecated
@Override
    public Maybe<Form> findByClientAndTemplate(ReferenceType referenceType, String referenceId, String client, String template) {
 return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
@Override
    public Mono<Form> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template) {
        LOGGER.debug("Find form by {} {}, client {} and template {}", referenceType, referenceId, client, template);
        return RxJava2Adapter.maybeToMono(formRepository.findByClientAndTemplate(referenceType, referenceId, client, template)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a form using its {} {} its client {} and template {}", referenceType, referenceId, client, template, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a form using its %s %s its client %s and template %s", referenceType, referenceId, client, template), ex)));
                }));
    }

    @Deprecated
@Override
    public Maybe<Form> findByDomainAndClientAndTemplate(String domain, String client, String template) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndTemplate_migrated(domain, client, template));
}
@Override
    public Mono<Form> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template) {
        LOGGER.debug("Find form by domain {}, client {} and template {}", domain, client, template);
        return RxJava2Adapter.maybeToMono(findByClientAndTemplate(ReferenceType.DOMAIN, domain, client, template));
    }

    @Deprecated
@Override
    public Single<List<Form>> copyFromClient(String domain, String clientSource, String clientTarget) {
 return RxJava2Adapter.monoToSingle(copyFromClient_migrated(domain, clientSource, clientTarget));
}
@Override
    public Mono<List<Form>> copyFromClient_migrated(String domain, String clientSource, String clientTarget) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(findByDomainAndClient(domain, clientSource)
                .flatMapSingle(source -> {
                    NewForm form = new NewForm();
                    form.setEnabled(source.isEnabled());
                    form.setTemplate(Template.parse(source.getTemplate()));
                    form.setContent(source.getContent());
                    form.setAssets(source.getAssets());
                    return this.create(domain, clientTarget, form);
                })).collectList()));
    }

    @Deprecated
@Override
    public Single<Form> create(ReferenceType referenceType, String referenceId, NewForm newForm, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newForm, principal));
}
@Override
    public Mono<Form> create_migrated(ReferenceType referenceType, String referenceId, NewForm newForm, User principal) {
        LOGGER.debug("Create a new form {} for {} {}", newForm, referenceType, referenceId);
        return RxJava2Adapter.singleToMono(create0(referenceType, referenceId, null, newForm, principal));
    }

    @Deprecated
@Override
    public Single<Form> create(String domain, NewForm newForm, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newForm, principal));
}
@Override
    public Mono<Form> create_migrated(String domain, NewForm newForm, User principal) {
        LOGGER.debug("Create a new form {} for domain {}", newForm, domain);
        return RxJava2Adapter.singleToMono(create0(ReferenceType.DOMAIN, domain, null, newForm, principal));
    }

    @Deprecated
@Override
    public Single<Form> create(String domain, String client, NewForm newForm, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, client, newForm, principal));
}
@Override
    public Mono<Form> create_migrated(String domain, String client, NewForm newForm, User principal) {
        LOGGER.debug("Create a new form {} for domain {} and client {}", newForm, domain, client);
        return RxJava2Adapter.singleToMono(create0(ReferenceType.DOMAIN, domain, client, newForm, principal));
    }

    @Deprecated
@Override
    public Single<Form> update(ReferenceType referenceType, String referenceId, String id, UpdateForm updateForm, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(referenceType, referenceId, id, updateForm, principal));
}
@Override
    public Mono<Form> update_migrated(ReferenceType referenceType, String referenceId, String id, UpdateForm updateForm, User principal) {
        LOGGER.debug("Update a form {} for {}} {}", id, referenceType, referenceId);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(formRepository.findById(referenceType, referenceId, id)).switchIfEmpty(Mono.error(new FormNotFoundException(id))))
                .flatMapSingle(oldForm -> {
                    Form formToUpdate = new Form(oldForm);
                    formToUpdate.setEnabled(updateForm.isEnabled());
                    formToUpdate.setContent(updateForm.getContent());
                    formToUpdate.setAssets(updateForm.getAssets());
                    formToUpdate.setUpdatedAt(new Date());

                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(formRepository.update(formToUpdate)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Form, SingleSource<Form>>toJdkFunction(page -> {
                                // create event for sync process
                                Event event = new Event(Type.FORM, new Payload(page.getId(), page.getReferenceType(), page.getReferenceId(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(page)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(form -> auditService.report(AuditBuilder.builder(FormTemplateAuditBuilder.class).principal(principal).type(EventType.FORM_TEMPLATE_UPDATED).oldValue(oldForm).form(form)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(FormTemplateAuditBuilder.class).principal(principal).type(EventType.FORM_TEMPLATE_UPDATED).throwable(throwable)))));
                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a form", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a form", ex)));
                }));
    }

    @Deprecated
@Override
    public Single<Form> update(String domain, String id, UpdateForm updateForm, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateForm, principal));
}
@Override
    public Mono<Form> update_migrated(String domain, String id, UpdateForm updateForm, User principal) {
        LOGGER.debug("Update a form {} for domain {}", id, domain);
        return RxJava2Adapter.singleToMono(update(ReferenceType.DOMAIN, domain, id, updateForm, principal));
    }

    @Deprecated
@Override
    public Single<Form> update(String domain, String client, String id, UpdateForm updateForm, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, updateForm, principal));
}
@Override
    public Mono<Form> update_migrated(String domain, String client, String id, UpdateForm updateForm, User principal) {
        LOGGER.debug("Update a form {} for domain {} and client {}", id, domain, client);
        return RxJava2Adapter.singleToMono(update(ReferenceType.DOMAIN, domain, id, updateForm, principal));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create0_migrated(referenceType, referenceId, client, newForm, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Form> create0(ReferenceType referenceType, String referenceId, String client, NewForm newForm, User principal) {
 return RxJava2Adapter.monoToSingle(create0_migrated(referenceType, referenceId, client, newForm, principal));
}
private Mono<Form> create0_migrated(ReferenceType referenceType, String referenceId, String client, NewForm newForm, User principal) {
        String formId = RandomString.generate();

        // check if form is unique
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(checkFormUniqueness(referenceType, referenceId, client, newForm.getTemplate().template())).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Boolean, SingleSource<Form>>toJdkFunction(irrelevant -> {
                    Form form = new Form();
                    form.setId(formId);
                    form.setReferenceType(referenceType);
                    form.setReferenceId(referenceId);
                    form.setClient(client);
                    form.setEnabled(newForm.isEnabled());
                    form.setTemplate(newForm.getTemplate().template());
                    form.setContent(newForm.getContent());
                    form.setAssets(newForm.getAssets());
                    form.setCreatedAt(new Date());
                    form.setUpdatedAt(form.getCreatedAt());
                    return formRepository.create(form);
                }).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Form, SingleSource<Form>>toJdkFunction(page -> {
                    // create event for sync process
                    Event event = new Event(Type.FORM, new Payload(page.getId(), page.getReferenceType(), page.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(page)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a form", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a form", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(form -> auditService.report(AuditBuilder.builder(FormTemplateAuditBuilder.class).principal(principal).type(EventType.FORM_TEMPLATE_CREATED).form(form)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(FormTemplateAuditBuilder.class).principal(principal).type(EventType.FORM_TEMPLATE_CREATED).throwable(throwable))))));
    }

    @Deprecated
@Override
    public Completable delete(ReferenceType referenceType, String referenceId, String formId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(referenceType, referenceId, formId, principal));
}
@Override
    public Mono<Void> delete_migrated(ReferenceType referenceType, String referenceId, String formId, User principal) {
        LOGGER.debug("Delete form {}", formId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(formRepository.findById(referenceType, referenceId, formId)).switchIfEmpty(Mono.error(new FormNotFoundException(formId))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Form, CompletableSource>)page -> {
                    // create event for sync process
                    Event event = new Event(Type.FORM, new Payload(page.getId(), page.getReferenceType(), page.getReferenceId(), Action.DELETE));

                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(formRepository.delete(formId)).then(RxJava2Adapter.singleToMono(eventService.create(event)))).toCompletable()
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(FormTemplateAuditBuilder.class).principal(principal).type(EventType.FORM_TEMPLATE_DELETED).form(page)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(FormTemplateAuditBuilder.class).principal(principal).type(EventType.FORM_TEMPLATE_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete form: {}", formId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete form: %s", formId), ex)));
                }));
    }

    @Deprecated
@Override
    public Completable delete(String domain, String formId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(domain, formId, principal));
}
@Override
    public Mono<Void> delete_migrated(String domain, String formId, User principal) {

        return RxJava2Adapter.completableToMono(delete(ReferenceType.DOMAIN, domain, formId, principal));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.checkFormUniqueness_migrated(referenceType, referenceId, client, formTemplate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Boolean> checkFormUniqueness(ReferenceType referenceType, String referenceId, String client, String formTemplate) {
 return RxJava2Adapter.monoToSingle(checkFormUniqueness_migrated(referenceType, referenceId, client, formTemplate));
}
private Mono<Boolean> checkFormUniqueness_migrated(ReferenceType referenceType, String referenceId, String client, String formTemplate) {
        Maybe<Form> maybeSource = client == null ?
                findByTemplate(referenceType, referenceId, formTemplate) :
                findByClientAndTemplate(referenceType, referenceId, client, formTemplate);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(maybeSource).hasElement().map(RxJavaReactorMigrationUtil.toJdkFunction(isEmpty -> {
                    if (!isEmpty) {
                        throw new FormAlreadyExistsException(formTemplate);
                    }
                    return true;
                }))));
    }
}

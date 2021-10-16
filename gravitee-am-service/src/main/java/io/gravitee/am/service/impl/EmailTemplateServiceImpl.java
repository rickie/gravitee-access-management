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
import io.gravitee.am.model.Email;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Template;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.repository.management.api.EmailRepository;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.EmailTemplateService;
import io.gravitee.am.service.EventService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.EmailAlreadyExistsException;
import io.gravitee.am.service.exception.EmailNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewEmail;
import io.gravitee.am.service.model.UpdateEmail;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.EmailTemplateAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.Date;
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
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final Logger LOGGER = LoggerFactory.getLogger(EmailTemplateServiceImpl.class);

    @Lazy
    @Autowired
    private EmailRepository emailRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated(referenceType, referenceId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> findAll(ReferenceType referenceType, String referenceId) {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated(referenceType, referenceId));
}
@Override
    public Flux<Email> findAll_migrated(ReferenceType referenceType, String referenceId) {
        LOGGER.debug("Find all emails for {} {}", referenceType, referenceId);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(emailRepository.findAll_migrated(referenceType, referenceId))).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find all emails for {} {}", referenceType, referenceId, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(String.format("An error occurs while trying to find a all emails for %s %s", referenceType, referenceId), ex)));
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Email> findAll_migrated() {
        LOGGER.debug("Find all emails");
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(emailRepository.findAll_migrated())).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find all emails", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find a all emails", ex)));
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByClient_migrated(referenceType, referenceId, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> findByClient(ReferenceType referenceType, String referenceId, String client) {
 return RxJava2Adapter.fluxToFlowable(findByClient_migrated(referenceType, referenceId, client));
}
@Override
    public Flux<Email> findByClient_migrated(ReferenceType referenceType, String referenceId, String client) {
        LOGGER.debug("Find email by {} {} and client {}", referenceType, referenceId, client);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(emailRepository.findByClient_migrated(referenceType, referenceId, client))).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a email using its {} {} and its client {}", referenceType, referenceId, client, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a email using its %s %s and its client %s", referenceType, referenceId, client), ex)));
                }))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByTemplate_migrated(referenceType, referenceId, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByTemplate(ReferenceType referenceType, String referenceId, String template) {
 return RxJava2Adapter.monoToMaybe(findByTemplate_migrated(referenceType, referenceId, template));
}
@Override
    public Mono<Email> findByTemplate_migrated(ReferenceType referenceType, String referenceId, String template) {
        LOGGER.debug("Find email by {} {} and template {}", referenceType, referenceId, template);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(emailRepository.findByTemplate_migrated(referenceType, referenceId, template))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a email using its {} {} and template {}", referenceType, referenceId, template, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a email using its %s %s and template %s", referenceType, referenceId, template), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndTemplate_migrated(domain, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByDomainAndTemplate(String domain, String template) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndTemplate_migrated(domain, template));
}
@Override
    public Mono<Email> findByDomainAndTemplate_migrated(String domain, String template) {

        return RxJava2Adapter.maybeToMono(findByTemplate(ReferenceType.DOMAIN, domain, template));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByClientAndTemplate(ReferenceType referenceType, String referenceId, String client, String template) {
 return RxJava2Adapter.monoToMaybe(findByClientAndTemplate_migrated(referenceType, referenceId, client, template));
}
@Override
    public Mono<Email> findByClientAndTemplate_migrated(ReferenceType referenceType, String referenceId, String client, String template) {
        LOGGER.debug("Find email by {} {}, client {} and template {}", referenceType, referenceId, client, template);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(emailRepository.findByClientAndTemplate_migrated(referenceType, referenceId, client, template))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a email using its {} {} its client {} and template {}", referenceType, referenceId, client, template, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a email using its %s %s its client %s and template %s", referenceType, referenceId, client, template), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientAndTemplate_migrated(domain, client, template))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findByDomainAndClientAndTemplate(String domain, String client, String template) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndClientAndTemplate_migrated(domain, client, template));
}
@Override
    public Mono<Email> findByDomainAndClientAndTemplate_migrated(String domain, String client, String template) {
        return RxJava2Adapter.maybeToMono(findByClientAndTemplate(ReferenceType.DOMAIN, domain, client, template));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Email> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Email> findById_migrated(String id) {
        LOGGER.debug("Find email by id {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(emailRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a email using its id {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a email using its id %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.copyFromClient_migrated(domain, clientSource, clientTarget))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Email> copyFromClient(String domain, String clientSource, String clientTarget) {
 return RxJava2Adapter.fluxToFlowable(copyFromClient_migrated(domain, clientSource, clientTarget));
}
@Override
    public Flux<Email> copyFromClient_migrated(String domain, String clientSource, String clientTarget) {
        return RxJava2Adapter.flowableToFlux(findByClient(ReferenceType.DOMAIN, domain, clientSource)
                .flatMapSingle(source -> {
                    NewEmail email = new NewEmail();
                    email.setEnabled(source.isEnabled());
                    email.setTemplate(Template.parse(source.getTemplate()));
                    email.setFrom(source.getFrom());
                    email.setFromName(source.getFromName());
                    email.setSubject(source.getSubject());
                    email.setContent(source.getContent());
                    email.setExpiresAfter(source.getExpiresAfter());
                    return RxJava2Adapter.monoToSingle(this.create_migrated(domain, clientTarget, email));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> create(ReferenceType referenceType, String referenceId, NewEmail newEmail, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, newEmail, principal));
}
@Override
    public Mono<Email> create_migrated(ReferenceType referenceType, String referenceId, NewEmail newEmail, User principal) {
        LOGGER.debug("Create a new email {} for {} {}", newEmail, referenceType, referenceId);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(create0_migrated(referenceType, referenceId, null, newEmail, principal)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> create(String domain, NewEmail newEmail, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newEmail, principal));
}
@Override
    public Mono<Email> create_migrated(String domain, NewEmail newEmail, User principal) {
        return RxJava2Adapter.singleToMono(create(ReferenceType.DOMAIN, domain, newEmail, principal));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(referenceType, referenceId, client, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> create(ReferenceType referenceType, String referenceId, String client, NewEmail newEmail, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(referenceType, referenceId, client, newEmail, principal));
}
@Override
    public Mono<Email> create_migrated(ReferenceType referenceType, String referenceId, String client, NewEmail newEmail, User principal) {
        LOGGER.debug("Create a new email {} for {} {} and client {}", newEmail, referenceType, referenceId, client);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(create0_migrated(referenceType, referenceId, client, newEmail, principal)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, client, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> create(String domain, String client, NewEmail newEmail, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, client, newEmail, principal));
}
@Override
    public Mono<Email> create_migrated(String domain, String client, NewEmail newEmail, User principal) {
        return RxJava2Adapter.singleToMono(create(ReferenceType.DOMAIN, domain, client, newEmail, principal));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> update(String domain, String id, UpdateEmail updateEmail, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateEmail, principal));
}
@Override
    public Mono<Email> update_migrated(String domain, String id, UpdateEmail updateEmail, User principal) {
        LOGGER.debug("Update an email {} for domain {}", id, domain);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(update0_migrated(ReferenceType.DOMAIN, domain, id, updateEmail, principal)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, client, id, updateEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Email> update(String domain, String client, String id, UpdateEmail updateEmail, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, client, id, updateEmail, principal));
}
@Override
    public Mono<Email> update_migrated(String domain, String client, String id, UpdateEmail updateEmail, User principal) {
        LOGGER.debug("Update an email {} for domain {} and client {}", id, domain, client);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(update0_migrated(ReferenceType.DOMAIN, domain, id, updateEmail, principal)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(emailId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String emailId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(emailId, principal));
}
@Override
    public Mono<Void> delete_migrated(String emailId, User principal) {
        LOGGER.debug("Delete email {}", emailId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(emailRepository.findById_migrated(emailId))).switchIfEmpty(Mono.error(new EmailNotFoundException(emailId))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Email, CompletableSource>)email -> {
                    // create event for sync process
                    Event event = new Event(Type.EMAIL, new Payload(email.getId(), email.getReferenceType(), email.getReferenceId(), Action.DELETE));
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(emailRepository.delete_migrated(emailId))).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event)))))
                            .toCompletable()
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(EmailTemplateAuditBuilder.class).principal(principal).type(EventType.EMAIL_TEMPLATE_DELETED).email(email)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(EmailTemplateAuditBuilder.class).principal(principal).type(EventType.EMAIL_TEMPLATE_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete email: {}", emailId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete email: %s", emailId), ex)));
                }));
    }


    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create0_migrated(referenceType, referenceId, client, newEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Email> create0(ReferenceType referenceType, String referenceId, String client, NewEmail newEmail, User principal) {
 return RxJava2Adapter.monoToSingle(create0_migrated(referenceType, referenceId, client, newEmail, principal));
}
private Mono<Email> create0_migrated(ReferenceType referenceType, String referenceId, String client, NewEmail newEmail, User principal) {
        String emailId = RandomString.generate();

        // check if email is unique
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(checkEmailUniqueness_migrated(referenceType, referenceId, client, newEmail.getTemplate().template()))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Boolean, SingleSource<Email>>toJdkFunction(irrelevant -> {
                    Email email = new Email();
                    email.setId(emailId);
                    email.setReferenceType(referenceType);
                    email.setReferenceId(referenceId);
                    email.setClient(client);
                    email.setEnabled(newEmail.isEnabled());
                    email.setTemplate(newEmail.getTemplate().template());
                    email.setFrom(newEmail.getFrom());
                    email.setFromName(newEmail.getFromName());
                    email.setSubject(newEmail.getSubject());
                    email.setContent(newEmail.getContent());
                    email.setExpiresAfter(newEmail.getExpiresAfter());
                    email.setCreatedAt(new Date());
                    email.setUpdatedAt(email.getCreatedAt());
                    return RxJava2Adapter.monoToSingle(emailRepository.create_migrated(email));
                }).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Email, SingleSource<Email>>toJdkFunction(email -> {
                    // create event for sync process
                    Event event = new Event(Type.EMAIL, new Payload(email.getId(), email.getReferenceType(), email.getReferenceId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event))).flatMap(__->Mono.just(email)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a email", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a email", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(email -> auditService.report(AuditBuilder.builder(EmailTemplateAuditBuilder.class).principal(principal).type(EventType.EMAIL_TEMPLATE_CREATED).email(email)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(EmailTemplateAuditBuilder.class).principal(principal).type(EventType.EMAIL_TEMPLATE_CREATED).throwable(throwable))))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update0_migrated(referenceType, referenceId, id, updateEmail, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Email> update0(ReferenceType referenceType, String referenceId, String id, UpdateEmail updateEmail, User principal) {
 return RxJava2Adapter.monoToSingle(update0_migrated(referenceType, referenceId, id, updateEmail, principal));
}
private Mono<Email> update0_migrated(ReferenceType referenceType, String referenceId, String id, UpdateEmail updateEmail, User principal) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(emailRepository.findById_migrated(referenceType, referenceId, id))).switchIfEmpty(Mono.error(new EmailNotFoundException(id))))
                .flatMapSingle(oldEmail -> {
                    Email emailToUpdate = new Email(oldEmail);
                    emailToUpdate.setEnabled(updateEmail.isEnabled());
                    emailToUpdate.setFrom(updateEmail.getFrom());
                    emailToUpdate.setFromName(updateEmail.getFromName());
                    emailToUpdate.setSubject(updateEmail.getSubject());
                    emailToUpdate.setContent(updateEmail.getContent());
                    emailToUpdate.setExpiresAfter(updateEmail.getExpiresAfter());
                    emailToUpdate.setUpdatedAt(new Date());

                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(emailRepository.update_migrated(emailToUpdate))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Email, SingleSource<Email>>toJdkFunction(email -> {
                                // create event for sync process
                                Event event = new Event(Type.EMAIL, new Payload(email.getId(), email.getReferenceType(), email.getReferenceId(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event))).flatMap(__->Mono.just(email)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(email -> auditService.report(AuditBuilder.builder(EmailTemplateAuditBuilder.class).principal(principal).type(EventType.EMAIL_TEMPLATE_UPDATED).oldValue(oldEmail).email(email)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(EmailTemplateAuditBuilder.class).principal(principal).type(EventType.EMAIL_TEMPLATE_UPDATED).throwable(throwable)))));
                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to update a email", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a email", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.checkEmailUniqueness_migrated(referenceType, referenceId, client, emailTemplate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Boolean> checkEmailUniqueness(ReferenceType referenceType, String referenceId, String client, String emailTemplate) {
 return RxJava2Adapter.monoToSingle(checkEmailUniqueness_migrated(referenceType, referenceId, client, emailTemplate));
}
private Mono<Boolean> checkEmailUniqueness_migrated(ReferenceType referenceType, String referenceId, String client, String emailTemplate) {
        Maybe<Email> maybeSource = client == null ?
                findByTemplate(referenceType, referenceId, emailTemplate) :
                findByClientAndTemplate(referenceType, referenceId, client, emailTemplate);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(maybeSource).hasElement().map(RxJavaReactorMigrationUtil.toJdkFunction(isEmpty -> {
                    if (!isEmpty) {
                        throw new EmailAlreadyExistsException(emailTemplate);
                    }
                    return true;
                }))));
    }
}

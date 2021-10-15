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
import io.gravitee.am.common.exception.oauth2.OAuth2Exception;
import io.gravitee.am.common.jwt.Claims;
import io.gravitee.am.common.oauth2.GrantType;
import io.gravitee.am.common.oidc.ClientAuthenticationMethod;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.common.utils.SecureRandomString;
import io.gravitee.am.common.web.UriBuilder;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.*;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.application.ApplicationOAuthSettings;
import io.gravitee.am.model.application.ApplicationScopeSettings;
import io.gravitee.am.model.application.ApplicationSettings;
import io.gravitee.am.model.application.ApplicationType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.model.permissions.SystemRole;
import io.gravitee.am.repository.management.api.ApplicationRepository;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewApplication;
import io.gravitee.am.service.model.PatchApplication;
import io.gravitee.am.service.model.TopApplication;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.ApplicationAuditBuilder;
import io.gravitee.am.service.utils.GrantTypeUtils;
import io.gravitee.am.service.validators.AccountSettingsValidator;
import io.reactivex.*;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private static final String AM_V2_VERSION = "AM_V2_VERSION";

    @Lazy
    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationTemplateManager applicationTemplateManager;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DomainService domainService;

    @Autowired
    private EventService eventService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private FormService formService;

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private CertificateService certificateService;

    @Override
    public Single<Page<Application>> findAll(int page, int size) {
        LOGGER.debug("Find applications");
        return applicationRepository.findAll(page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find applications", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find applications", ex)));
                });
    }

    @Override
    public Single<Page<Application>> findByDomain(String domain, int page, int size) {
        LOGGER.debug("Find applications by domain {}", domain);
        return applicationRepository.findByDomain(domain, page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by domain {}", domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find applications by domain %s", domain), ex)));
                });
    }

    @Override
    public Single<Page<Application>> search(String domain, String query, int page, int size) {
        LOGGER.debug("Search applications with query {} for domain {}", query, domain);
        return applicationRepository.search(domain, query, page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to search applications with query {} for domain {}", query, domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to search applications with query %s by domain %s", query, domain), ex)));
                });
    }

    @Override
    public Flowable<Application> findByCertificate(String certificate) {
        LOGGER.debug("Find applications by certificate : {}", certificate);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findByCertificate(certificate)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by certificate", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find applications by certificate", ex)));
                })));
    }

    @Override
    public Flowable<Application> findByIdentityProvider(String identityProvider) {
        LOGGER.debug("Find applications by identity provider : {}", identityProvider);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findByIdentityProvider(identityProvider)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by identity provider", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find applications by identity provider", ex)));
                })));
    }

    @Override
    public Flowable<Application> findByFactor(String factor) {
        LOGGER.debug("Find applications by factor : {}", factor);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findByFactor(factor)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by factor", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find applications by factor", ex)));
                })));
    }

    @Override
    public Single<Set<Application>> findByDomainAndExtensionGrant(String domain, String extensionGrant) {
        LOGGER.debug("Find applications by domain {} and extension grant : {}", domain, extensionGrant);
        return applicationRepository.findByDomainAndExtensionGrant(domain, extensionGrant)
                .collect(() -> (Set<Application>)new HashSet(), Set::add) // TODO CHECK IF FLOWABLE is useful...
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by extension grant", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find applications by extension grant", ex)));
                });
    }

    @Override
    public Flowable<Application> findByIdIn(List<String> ids) {
        LOGGER.debug("Find applications by ids : {}", ids);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(applicationRepository.findByIdIn(ids)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by ids {}", ids, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find applications by ids", ex)));
                })));
    }

    @Override
    public Maybe<Application> findById(String id) {
        LOGGER.debug("Find application by ID: {}", id);
        return applicationRepository.findById(id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find an application using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find an application using its ID: %s", id), ex)));
                });
    }

    @Override
    public Maybe<Application> findByDomainAndClientId(String domain, String clientId) {
        LOGGER.debug("Find application by domain: {} and client_id {}", domain, clientId);
        return applicationRepository.findByDomainAndClientId(domain, clientId)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find an application using its domain: {} and client_id : {}", domain, clientId, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find an application using its domain: %s, and client_id", domain, clientId), ex)));
                });
    }

    @Override
    public Single<Application> create(String domain, NewApplication newApplication, User principal) {
        LOGGER.debug("Create a new application {} for domain {}", newApplication, domain);
        Application application = new Application();
        application.setId(RandomString.generate());
        application.setName(newApplication.getName());
        application.setType(newApplication.getType());
        application.setDomain(domain);

        // apply default oauth 2.0 settings
        ApplicationSettings applicationSettings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setClientId(newApplication.getClientId());
        oAuthSettings.setClientSecret(newApplication.getClientSecret());
        oAuthSettings.setTokenEndpointAuthMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
        oAuthSettings.setRedirectUris(newApplication.getRedirectUris());
        applicationSettings.setOauth(oAuthSettings);
        application.setSettings(applicationSettings);

        // apply templating
        applicationTemplateManager.apply(application);

        return create0(domain, application, principal)
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create an application", ex)));
                });
    }

    @Override
    public Single<Application> create(Application application) {
        LOGGER.debug("Create a new application {} ", application);

        if (application.getDomain() == null || application.getDomain().trim().isEmpty()) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("No domain set on application")));
        }

        return create0(application.getDomain(), application, null)
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create an application", ex)));
                });
    }

    @Override
    public Single<Application> update(Application application) {
        LOGGER.debug("Update an application {} ", application);

        if (application.getDomain() == null || application.getDomain().trim().isEmpty()) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("No domain set on application")));
        }

        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(applicationRepository.findById(application.getId())).switchIfEmpty(Mono.error(new ApplicationNotFoundException(application.getId()))))
                .flatMapSingle(application1 -> update0(application1.getDomain(), application1, application, null))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update an application", ex)));
                });
    }

    @Override
    public Single<Application> updateType(String domain, String id, ApplicationType type, User principal) {
        LOGGER.debug("Update application {} type to {} for domain {}", id, type, domain);

        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(applicationRepository.findById(id)).switchIfEmpty(Mono.error(new ApplicationNotFoundException(id))))
                .flatMapSingle(existingApplication -> {
                    Application toPatch = new Application(existingApplication);
                    toPatch.setType(type);
                    applicationTemplateManager.changeType(toPatch);
                    return update0(domain, existingApplication, toPatch, principal);
                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to patch an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to patch an application", ex)));
                });
    }

    @Override
    public Single<Application> patch(String domain, String id, PatchApplication patchApplication, User principal) {
        LOGGER.debug("Patch an application {} for domain {}", id, domain);

        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(applicationRepository.findById(id)).switchIfEmpty(Mono.error(new ApplicationNotFoundException(id))))
                .flatMapSingle(existingApplication -> {
                    Application toPatch = patchApplication.patch(existingApplication);
                    applicationTemplateManager.apply(toPatch);
                    final AccountSettings accountSettings = toPatch.getSettings().getAccount();
                    if (AccountSettingsValidator.hasInvalidResetPasswordFields(accountSettings)) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidParameterException("Unexpected forgot password field")));
                    }
                    return update0(domain, existingApplication, toPatch, principal);
                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to patch an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to patch an application", ex)));
                });
    }

    @Override
    public Single<Application> renewClientSecret(String domain, String id, User principal) {
        LOGGER.debug("Renew client secret for application {} and domain {}", id, domain);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(applicationRepository.findById(id)).switchIfEmpty(RxJava2Adapter.maybeToMono(Maybe.wrap(Maybe.error(new ApplicationNotFoundException(id))))))
                .flatMapSingle(application -> {
                    // check application
                    if (application.getSettings() == null) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new IllegalStateException("Application settings is undefined")));
                    }
                    if (application.getSettings().getOauth() == null) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new IllegalStateException("Application OAuth 2.0 settings is undefined")));
                    }
                    // update secret
                    application.getSettings().getOauth().setClientSecret(SecureRandomString.generate());
                    application.setUpdatedAt(new Date());
                    return applicationRepository.update(application);
                })).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(application1 -> {
                    Event event = new Event(Type.APPLICATION, new Payload(application1.getId(), ReferenceType.DOMAIN, application1.getDomain(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(domain1->RxJava2Adapter.singleToMono(Single.just(application1))));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(updatedApplication -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_CLIENT_SECRET_RENEWED).application(updatedApplication)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_CLIENT_SECRET_RENEWED).throwable(throwable)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to renew client secret for application {} and domain {}", id, domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to renew client secret for application %s and domain %s", id, domain), ex)));
                });
    }

    @Override
    public Completable delete(String id, User principal) {
        LOGGER.debug("Delete application {}", id);
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(applicationRepository.findById(id)).switchIfEmpty(Mono.error(new ApplicationNotFoundException(id))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Application, CompletableSource>)application -> {
                    // create event for sync process
                    Event event = new Event(Type.APPLICATION, new Payload(application.getId(), ReferenceType.DOMAIN, application.getDomain(), Action.DELETE));
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(applicationRepository.delete(id)
                            .andThen(eventService.create(event).toCompletable())).then(RxJava2Adapter.completableToMono(Completable.wrap(emailTemplateService.findByClient(ReferenceType.DOMAIN, application.getDomain(), application.getId())
                                    .flatMapCompletable(email -> emailTemplateService.delete(email.getId()))))))).then(RxJava2Adapter.completableToMono(formService.findByDomainAndClient(application.getDomain(), application.getId())
                                    .flatMapCompletable(form -> formService.delete(application.getDomain(), form.getId())))).then(RxJava2Adapter.flowableToFlux(membershipService.findByReference(application.getId(), ReferenceType.APPLICATION)).flatMap(z->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<Membership, CompletableSource>toJdkFunction(membership -> membershipService.delete(membership.getId())).apply(z)))).then()))
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_DELETED).application(application)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete application: {}", id, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete application: %s", id), ex)));
                });
    }

    @Override
    public Single<Long> count() {
        LOGGER.debug("Count applications");
        return applicationRepository.count()
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to count applications", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to count applications"), ex)));
                });
    }

    @Override
    public Single<Long> countByDomain(String domainId) {
        LOGGER.debug("Count applications for domain {}", domainId);
        return applicationRepository.countByDomain(domainId)
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to count applications for domain {}", domainId, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to count applications for domain %s", domainId), ex)));
                });
    }

    @Override
    public Single<Set<TopApplication>> findTopApplications() {
        LOGGER.debug("Find top applications");
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(applicationRepository.findAll(0, Integer.MAX_VALUE)
                .flatMapObservable(pagedApplications -> Observable.fromIterable(pagedApplications.getData()))
                .flatMapSingle(application -> RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(tokenService.findTotalTokensByApplication(application)).map(RxJavaReactorMigrationUtil.toJdkFunction(totalToken -> {
                            TopApplication topApplication = new TopApplication();
                            topApplication.setApplication(application);
                            topApplication.setAccessTokens(totalToken.getTotalAccessTokens());
                            return topApplication;
                        })))
                )
                .toList()).map(RxJavaReactorMigrationUtil.toJdkFunction(topApplications -> topApplications.stream().filter(topClient -> topClient.getAccessTokens() > 0).collect(Collectors.toSet()))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find top applications", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find top applications", ex)));
                });
    }

    @Override
    public Single<Set<TopApplication>> findTopApplicationsByDomain(String domain) {
        LOGGER.debug("Find top applications for domain: {}", domain);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(applicationRepository.findByDomain(domain, 0, Integer.MAX_VALUE)
                .flatMapObservable(pagedApplications -> Observable.fromIterable(pagedApplications.getData()))
                .flatMapSingle(application -> RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(tokenService.findTotalTokensByApplication(application)).map(RxJavaReactorMigrationUtil.toJdkFunction(totalToken -> {
                            TopApplication topApplication = new TopApplication();
                            topApplication.setApplication(application);
                            topApplication.setAccessTokens(totalToken.getTotalAccessTokens());
                            return topApplication;
                        })))
                )
                .toList()).map(RxJavaReactorMigrationUtil.toJdkFunction(topApplications -> topApplications.stream().filter(topClient -> topClient.getAccessTokens() > 0).collect(Collectors.toSet()))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find top applications for domain {}", domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(String.format("An error occurs while trying to find top applications for domain %s", domain), ex)));
                });
    }

    private Single<Application> create0(String domain, Application application, User principal) {
        // created and updated date
        application.setCreatedAt(new Date());
        application.setUpdatedAt(application.getCreatedAt());

        // check uniqueness
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(checkApplicationUniqueness(domain, application)
                // validate application metadata
                .andThen(validateApplicationMetadata(application))
                // set default certificate
                .flatMap(this::setDefaultCertificate)
                // create the application
                .flatMap(applicationRepository::create)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Object>>toJdkFunction(application1 -> {
                    if (principal == null || principal.getAdditionalInformation() == null || StringUtils.isEmpty(principal.getAdditionalInformation().get(Claims.organization))) {
                        // There is no principal or we can not find the organization the user is attached to. Can't assign role.
                        return Single.just(application1);
                    }

                    return roleService.findSystemRole(SystemRole.APPLICATION_PRIMARY_OWNER, ReferenceType.APPLICATION)
                            .switchIfEmpty(Single.error(new InvalidRoleException("Cannot assign owner to the application, owner role does not exist")))
                            .flatMap(role -> {
                                Membership membership = new Membership();
                                membership.setDomain(application1.getDomain());
                                membership.setMemberId(principal.getId());
                                membership.setMemberType(MemberType.USER);
                                membership.setReferenceId(application1.getId());
                                membership.setReferenceType(ReferenceType.APPLICATION);
                                membership.setRoleId(role.getId());
                                return membershipService.addOrUpdate((String) principal.getAdditionalInformation().get(Claims.organization), membership)
                                        .map(__ -> domain);
                            });
                }).apply(v)))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Object, SingleSource<Application>>toJdkFunction(application1 -> {
                    Event event = new Event(Type.APPLICATION, new Payload(application.getId(), ReferenceType.DOMAIN, application.getDomain(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(domain1->RxJava2Adapter.singleToMono(Single.just(application))));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(application1 -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_CREATED).application(application1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_CREATED).throwable(throwable)))));
    }

    //TODO Boualem : domain never used
    private Single<Application> update0(String domain, Application currentApplication, Application applicationToUpdate, User principal) {
        // updated date
        applicationToUpdate.setUpdatedAt(new Date());

        // validate application metadata
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(validateApplicationMetadata(applicationToUpdate)
                // validate identity providers
                .flatMap(this::validateApplicationIdentityProviders)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(applicationRepository::update).apply(v)))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(application1 -> {
                    Event event = new Event(Type.APPLICATION, new Payload(application1.getId(), ReferenceType.DOMAIN, application1.getDomain(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(domain1->RxJava2Adapter.singleToMono(Single.just(application1))));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(application -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_UPDATED).oldValue(currentApplication).application(application)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_UPDATED).throwable(throwable)))));
    }

    /**
     * Set default domain certificate for the application
     * @param application the application to create
     * @return the application with the certificate
     */
    private Single<Application> setDefaultCertificate(Application application) {
        // certificate might have been set via DCR, continue
        if (application.getCertificate() != null) {
            return RxJava2Adapter.monoToSingle(Mono.just(application));
        }

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(certificateService
                .findByDomain(application.getDomain())).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(certificates -> {
                    if (certificates == null || certificates.isEmpty()) {
                        return application;
                    }
                    Certificate defaultCertificate = certificates
                            .stream()
                            .filter(certificate -> "Default".equals(certificate.getName()))
                            .findFirst()
                            .orElse(certificates.get(0));
                    application.setCertificate(defaultCertificate.getId());
                    return application;
                })));
    }

    private Completable checkApplicationUniqueness(String domain, Application application) {
        final String clientId = application.getSettings() != null && application.getSettings().getOauth() != null ? application.getSettings().getOauth().getClientId() : null;
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(findByDomainAndClientId(domain, clientId)).hasElement().flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, CompletableSource>)isEmpty -> {
                    if (!isEmpty) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(new ApplicationAlreadyExistsException(clientId, domain)));
                    }
                    return RxJava2Adapter.monoToCompletable(Mono.empty());
                }).apply(y)))).then());
    }

    private Single<Application> validateApplicationIdentityProviders(Application application) {
        if (application.getIdentities() == null || application.getIdentities().isEmpty()) {
            return RxJava2Adapter.monoToSingle(Mono.just(application));
        }
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Observable.fromIterable(application.getIdentities())
                .flatMapSingle(identity -> RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(identityProviderService.findById(identity)).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()).single()))
                .toList()).map(RxJavaReactorMigrationUtil.toJdkFunction(optionalIdentities -> {
                    if (optionalIdentities == null || optionalIdentities.isEmpty()) {
                        application.setIdentities(Collections.emptySet());
                    } else {
                        Set<String> identities = optionalIdentities
                                .stream()
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(IdentityProvider::getId)
                                .collect(Collectors.toSet());
                        application.setIdentities(identities);
                    }
                    return application;
                })));
    }

    /**
     * <pre>
     * This function will return an error if :
     * We try to enable Dynamic Client Registration on client side while it is not enabled on domain.
     * The redirect_uris do not respect domain conditions (localhost, scheme and wildcard)
     * </pre>
     *
     * @param application application to check
     * @return a client only if every conditions are respected.
     */
    private Single<Application> validateApplicationMetadata(Application application) {
        // do nothing if application has no settings
        if (application.getSettings() == null) {
            return RxJava2Adapter.monoToSingle(Mono.just(application));
        }
        if (application.getSettings().getOauth() == null) {
            return RxJava2Adapter.monoToSingle(Mono.just(application));
        }
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(GrantTypeUtils.validateGrantTypes(application)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(this::validateRedirectUris).apply(v)))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(this::validateScopes).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(this::validateTokenEndpointAuthMethod).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono((Single<Application>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Application, Single<Application>>)this::validateTlsClientAuth).apply(v))));
    }

    private Single<Application> validateRedirectUris(Application application) {
        ApplicationOAuthSettings oAuthSettings = application.getSettings().getOauth();

        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainService.findById(application.getDomain())).switchIfEmpty(Mono.error(new DomainNotFoundException(application.getDomain()))))
                .flatMapSingle(domain -> {
                    //check redirect_uri
                    if (GrantTypeUtils.isRedirectUriRequired(oAuthSettings.getGrantTypes()) && CollectionUtils.isEmpty(oAuthSettings.getRedirectUris())) {
                        // if client type is from V2, it means that the application has been created from an old client without redirect_uri control (via the upgrader)
                        // skip for now since it will be set in the next update operation
                        if (AM_V2_VERSION.equals(oAuthSettings.getSoftwareVersion())) {
                            oAuthSettings.setSoftwareVersion(null);
                        } else {
                            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException()));
                        }
                    }

                    //check redirect_uri content
                    if (oAuthSettings.getRedirectUris() != null) {
                        for (String redirectUri : oAuthSettings.getRedirectUris()) {

                            try {
                                URI uri = UriBuilder.fromURIString(redirectUri).build();

                                if (uri.getScheme() == null) {
                                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException("redirect_uri : " + redirectUri + " is malformed")));
                                }

                                if (!domain.isRedirectUriLocalhostAllowed() && UriBuilder.isHttp(uri.getScheme()) && UriBuilder.isLocalhost(uri.getHost())) {
                                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException("localhost is forbidden")));
                                }
                                //check http scheme
                                if (!domain.isRedirectUriUnsecuredHttpSchemeAllowed() && uri.getScheme().equalsIgnoreCase("http")) {
                                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException("Unsecured http scheme is forbidden")));
                                }
                                //check wildcard
                                if (!domain.isRedirectUriWildcardAllowed() && uri.getPath().contains("*")) {
                                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException("Wildcard are forbidden")));
                                }
                                // check fragment
                                if (uri.getFragment() != null) {
                                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException("redirect_uri with fragment is forbidden")));
                                }
                            } catch (IllegalArgumentException | URISyntaxException ex) {
                                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRedirectUriException("redirect_uri : " + redirectUri + " is malformed")));
                            }
                        }
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(application));
                });
    }

    private Single<Application> validateScopes(Application application) {
        ApplicationOAuthSettings oAuthSettings = application.getSettings().getOauth();
        // check scope approvals and default scopes coherency
        List<String> scopes = oAuthSettings.getScopeSettings() != null ? oAuthSettings.getScopeSettings().stream().map(ApplicationScopeSettings::getScope).collect(Collectors.toList()) : new ArrayList<>();
        List<String> defaultScopes = oAuthSettings.getScopeSettings() != null ? oAuthSettings.getScopeSettings().stream().filter(ApplicationScopeSettings::isDefaultScope).map(ApplicationScopeSettings::getScope).collect(Collectors.toList()) : new ArrayList<>();
        Set<String> scopeApprovals = oAuthSettings.getScopeSettings() != null ? oAuthSettings.getScopeSettings().stream().filter(s -> s.getScopeApproval() != null).map(ApplicationScopeSettings::getScope).collect(Collectors.toSet()) : new HashSet<>();
        if (!scopes.containsAll(defaultScopes)) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("non valid default scopes")));
        }
        if (!scopes.containsAll(scopeApprovals)) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("non valid scope approvals")));
        }
        // check scopes against domain scopes
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(scopeService.validateScope(application.getDomain(), scopes)).flatMap(v->RxJava2Adapter.singleToMono((Single<Application>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, Single<Application>>)isValid -> {
                    if (!isValid) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("non valid scopes")));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(application));
                }).apply(v))));
    }

    private Single<Application> validateTokenEndpointAuthMethod(Application application) {
        ApplicationOAuthSettings oauthSettings = application.getSettings().getOauth();
        String tokenEndpointAuthMethod = oauthSettings.getTokenEndpointAuthMethod();
        if ((ApplicationType.SERVICE == application.getType() || (oauthSettings.getGrantTypes() != null && oauthSettings.getGrantTypes().contains(GrantType.CLIENT_CREDENTIALS)))
                && (tokenEndpointAuthMethod != null && ClientAuthenticationMethod.NONE.equals(tokenEndpointAuthMethod))) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Invalid token_endpoint_auth_method for service application (client_credentials grant type)")));
        }
        return RxJava2Adapter.monoToSingle(Mono.just(application));
    }

    /**
     * A client using the "tls_client_auth" authentication method MUST use exactly one of the
     * below metadata parameters to indicate the certificate subject value that the authorization server is
     * to expect when authenticating the respective client.
     */
    private Single<Application> validateTlsClientAuth(Application application) {
        ApplicationOAuthSettings settings = application.getSettings().getOauth();
        if (settings.getTokenEndpointAuthMethod() != null &&
                ClientAuthenticationMethod.TLS_CLIENT_AUTH.equalsIgnoreCase(settings.getTokenEndpointAuthMethod())) {

            if ((settings.getTlsClientAuthSubjectDn() == null || settings.getTlsClientAuthSubjectDn().isEmpty()) &&
                    (settings.getTlsClientAuthSanDns() == null || settings.getTlsClientAuthSanDns().isEmpty()) &&
                    (settings.getTlsClientAuthSanIp() == null || settings.getTlsClientAuthSanIp().isEmpty()) &&
                    (settings.getTlsClientAuthSanEmail() == null || settings.getTlsClientAuthSanEmail().isEmpty()) &&
                    (settings.getTlsClientAuthSanUri() == null || settings.getTlsClientAuthSanUri().isEmpty())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("Missing TLS parameter for tls_client_auth.")));
            }

            if (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty() && (
                    (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty()) ||
                            (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty()) ||
                            (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty()) ||
                            (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            } else if (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty() && (
                    (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty()) ||
                            (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty()) ||
                            (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty()) ||
                            (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            } else if (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty() && (
                    (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty()) ||
                            (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty()) ||
                            (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty()) ||
                            (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            } else if (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty() && (
                    (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty()) ||
                            (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty()) ||
                            (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty()) ||
                            (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            } else if (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty() && (
                    (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty()) ||
                            (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty()) ||
                            (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty()) ||
                            (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty()))) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters.")));
            }
        }

        return RxJava2Adapter.monoToSingle(Mono.just(application));
    }
}

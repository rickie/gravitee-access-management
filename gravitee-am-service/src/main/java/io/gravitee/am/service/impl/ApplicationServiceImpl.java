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
import io.gravitee.am.model.Email;
import io.gravitee.am.model.Form;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.Role;
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

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated(page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Application>> findAll(int page, int size) {
 return RxJava2Adapter.monoToSingle(findAll_migrated(page, size));
}
@Override
    public Mono<Page<Application>> findAll_migrated(int page, int size) {
        LOGGER.debug("Find applications");
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.findAll_migrated(page, size))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find applications", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find applications", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Application>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<Application>> findByDomain_migrated(String domain, int page, int size) {
        LOGGER.debug("Find applications by domain {}", domain);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.findByDomain_migrated(domain, page, size))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by domain {}", domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find applications by domain %s", domain), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(domain, query, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Application>> search(String domain, String query, int page, int size) {
 return RxJava2Adapter.monoToSingle(search_migrated(domain, query, page, size));
}
@Override
    public Mono<Page<Application>> search_migrated(String domain, String query, int page, int size) {
        LOGGER.debug("Search applications with query {} for domain {}", query, domain);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.search_migrated(domain, query, page, size))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to search applications with query {} for domain {}", query, domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to search applications with query %s by domain %s", query, domain), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByCertificate_migrated(certificate))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByCertificate(String certificate) {
 return RxJava2Adapter.fluxToFlowable(findByCertificate_migrated(certificate));
}
@Override
    public Flux<Application> findByCertificate_migrated(String certificate) {
        LOGGER.debug("Find applications by certificate : {}", certificate);
        return applicationRepository.findByCertificate_migrated(certificate).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by certificate", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find applications by certificate", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdentityProvider_migrated(identityProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByIdentityProvider(String identityProvider) {
 return RxJava2Adapter.fluxToFlowable(findByIdentityProvider_migrated(identityProvider));
}
@Override
    public Flux<Application> findByIdentityProvider_migrated(String identityProvider) {
        LOGGER.debug("Find applications by identity provider : {}", identityProvider);
        return applicationRepository.findByIdentityProvider_migrated(identityProvider).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by identity provider", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find applications by identity provider", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByFactor_migrated(factor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByFactor(String factor) {
 return RxJava2Adapter.fluxToFlowable(findByFactor_migrated(factor));
}
@Override
    public Flux<Application> findByFactor_migrated(String factor) {
        LOGGER.debug("Find applications by factor : {}", factor);
        return applicationRepository.findByFactor_migrated(factor).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by factor", ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find applications by factor", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomainAndExtensionGrant_migrated(domain, extensionGrant))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Set<Application>> findByDomainAndExtensionGrant(String domain, String extensionGrant) {
 return RxJava2Adapter.monoToSingle(findByDomainAndExtensionGrant_migrated(domain, extensionGrant));
}
@Override
    public Mono<Set<Application>> findByDomainAndExtensionGrant_migrated(String domain, String extensionGrant) {
        LOGGER.debug("Find applications by domain {} and extension grant : {}", domain, extensionGrant);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(applicationRepository.findByDomainAndExtensionGrant_migrated(domain, extensionGrant))
                .collect(() -> (Set<Application>)new HashSet(), Set::add) // TODO CHECK IF FLOWABLE is useful...
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by extension grant", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find applications by extension grant", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Application> findByIdIn(List<String> ids) {
 return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
@Override
    public Flux<Application> findByIdIn_migrated(List<String> ids) {
        LOGGER.debug("Find applications by ids : {}", ids);
        return applicationRepository.findByIdIn_migrated(ids).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find applications by ids {}", ids, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find applications by ids", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Application> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Application> findById_migrated(String id) {
        LOGGER.debug("Find application by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(applicationRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find an application using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find an application using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findByDomainAndClientId_migrated(domain, clientId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Application> findByDomainAndClientId(String domain, String clientId) {
 return RxJava2Adapter.monoToMaybe(findByDomainAndClientId_migrated(domain, clientId));
}
@Override
    public Mono<Application> findByDomainAndClientId_migrated(String domain, String clientId) {
        LOGGER.debug("Find application by domain: {} and client_id {}", domain, clientId);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(applicationRepository.findByDomainAndClientId_migrated(domain, clientId))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find an application using its domain: {} and client_id : {}", domain, clientId, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find an application using its domain: %s, and client_id", domain, clientId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newApplication, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Application> create(String domain, NewApplication newApplication, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newApplication, principal));
}
@Override
    public Mono<Application> create_migrated(String domain, NewApplication newApplication, User principal) {
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

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(create0_migrated(domain, application, principal))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create an application", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Application> create(Application application) {
 return RxJava2Adapter.monoToSingle(create_migrated(application));
}
@Override
    public Mono<Application> create_migrated(Application application) {
        LOGGER.debug("Create a new application {} ", application);

        if (application.getDomain() == null || application.getDomain().trim().isEmpty()) {
            return Mono.error(new InvalidClientMetadataException("No domain set on application"));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(create0_migrated(application.getDomain(), application, null))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create an application", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Application> update(Application application) {
 return RxJava2Adapter.monoToSingle(update_migrated(application));
}
@Override
    public Mono<Application> update_migrated(Application application) {
        LOGGER.debug("Update an application {} ", application);

        if (application.getDomain() == null || application.getDomain().trim().isEmpty()) {
            return Mono.error(new InvalidClientMetadataException("No domain set on application"));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.findById_migrated(application.getId()).switchIfEmpty(Mono.error(new ApplicationNotFoundException(application.getId()))).flatMap(y->update0_migrated(y.getDomain(), y, application, null)))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update an application", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.updateType_migrated(domain, id, type, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Application> updateType(String domain, String id, ApplicationType type, User principal) {
 return RxJava2Adapter.monoToSingle(updateType_migrated(domain, id, type, principal));
}
@Override
    public Mono<Application> updateType_migrated(String domain, String id, ApplicationType type, User principal) {
        LOGGER.debug("Update application {} type to {} for domain {}", id, type, domain);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.findById_migrated(id).switchIfEmpty(Mono.error(new ApplicationNotFoundException(id))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(existingApplication -> {
                    Application toPatch = new Application(existingApplication);
                    toPatch.setType(type);
                    applicationTemplateManager.changeType(toPatch);
                    return RxJava2Adapter.monoToSingle(update0_migrated(domain, existingApplication, toPatch, principal));
                }).apply(y)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to patch an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to patch an application", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(domain, id, patchApplication, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Application> patch(String domain, String id, PatchApplication patchApplication, User principal) {
 return RxJava2Adapter.monoToSingle(patch_migrated(domain, id, patchApplication, principal));
}
@Override
    public Mono<Application> patch_migrated(String domain, String id, PatchApplication patchApplication, User principal) {
        LOGGER.debug("Patch an application {} for domain {}", id, domain);

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.findById_migrated(id).switchIfEmpty(Mono.error(new ApplicationNotFoundException(id))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(existingApplication -> {
                    Application toPatch = patchApplication.patch(existingApplication);
                    applicationTemplateManager.apply(toPatch);
                    final AccountSettings accountSettings = toPatch.getSettings().getAccount();
                    if (AccountSettingsValidator.hasInvalidResetPasswordFields(accountSettings)) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidParameterException("Unexpected forgot password field")));
                    }
                    return RxJava2Adapter.monoToSingle(update0_migrated(domain, existingApplication, toPatch, principal));
                }).apply(y)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException || ex instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to patch an application", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to patch an application", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.renewClientSecret_migrated(domain, id, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Application> renewClientSecret(String domain, String id, User principal) {
 return RxJava2Adapter.monoToSingle(renewClientSecret_migrated(domain, id, principal));
}
@Override
    public Mono<Application> renewClientSecret_migrated(String domain, String id, User principal) {
        LOGGER.debug("Renew client secret for application {} and domain {}", id, domain);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(applicationRepository.findById_migrated(id).switchIfEmpty(Mono.error(new ApplicationNotFoundException(id))))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(application -> {
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
                    return RxJava2Adapter.monoToSingle(applicationRepository.update_migrated(application));
                }).apply(y)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(application1 -> {
                    Event event = new Event(Type.APPLICATION, new Payload(application1.getId(), ReferenceType.DOMAIN, application1.getDomain(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(domain1->Mono.just(application1)));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(updatedApplication -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_CLIENT_SECRET_RENEWED).application(updatedApplication)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_CLIENT_SECRET_RENEWED).throwable(throwable)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to renew client secret for application {} and domain {}", id, domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to renew client secret for application %s and domain %s", id, domain), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String id, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(id, principal));
}
@Override
    public Mono<Void> delete_migrated(String id, User principal) {
        LOGGER.debug("Delete application {}", id);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(applicationRepository.findById_migrated(id).switchIfEmpty(Mono.error(new ApplicationNotFoundException(id))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Application, CompletableSource>)application -> {
                    // create event for sync process
                    Event event = new Event(Type.APPLICATION, new Payload(application.getId(), ReferenceType.DOMAIN, application.getDomain(), Action.DELETE));
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(applicationRepository.delete_migrated(id).then(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(eventService.create_migrated(event)).toCompletable())).then(emailTemplateService.findByClient_migrated(ReferenceType.DOMAIN, application.getDomain(), application.getId()).flatMap(v->emailTemplateService.delete_migrated(v.getId())).then()).then(formService.findByDomainAndClient_migrated(application.getDomain(), application.getId()).flatMap(v->formService.delete_migrated(application.getDomain(), v.getId())).then()).then(membershipService.findByReference_migrated(application.getId(), ReferenceType.APPLICATION).flatMap(v->membershipService.delete_migrated(v.getId())).then()))
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_DELETED).application(application)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete application: {}", id, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete application: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.count_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> count() {
 return RxJava2Adapter.monoToSingle(count_migrated());
}
@Override
    public Mono<Long> count_migrated() {
        LOGGER.debug("Count applications");
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.count_migrated())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to count applications", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to count applications"), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByDomain_migrated(domainId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Long> countByDomain(String domainId) {
 return RxJava2Adapter.monoToSingle(countByDomain_migrated(domainId));
}
@Override
    public Mono<Long> countByDomain_migrated(String domainId) {
        LOGGER.debug("Count applications for domain {}", domainId);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.countByDomain_migrated(domainId))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to count applications for domain {}", domainId, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to count applications for domain %s", domainId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTopApplications_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Set<TopApplication>> findTopApplications() {
 return RxJava2Adapter.monoToSingle(findTopApplications_migrated());
}
@Override
    public Mono<Set<TopApplication>> findTopApplications_migrated() {
        LOGGER.debug("Find top applications");
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.findAll_migrated(0, Integer.MAX_VALUE))
                .flatMapObservable(pagedApplications -> Observable.fromIterable(pagedApplications.getData()))
                .flatMapSingle(application -> RxJava2Adapter.monoToSingle(tokenService.findTotalTokensByApplication_migrated(application).map(RxJavaReactorMigrationUtil.toJdkFunction(totalToken -> {
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
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTopApplicationsByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Set<TopApplication>> findTopApplicationsByDomain(String domain) {
 return RxJava2Adapter.monoToSingle(findTopApplicationsByDomain_migrated(domain));
}
@Override
    public Mono<Set<TopApplication>> findTopApplicationsByDomain_migrated(String domain) {
        LOGGER.debug("Find top applications for domain: {}", domain);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationRepository.findByDomain_migrated(domain, 0, Integer.MAX_VALUE))
                .flatMapObservable(pagedApplications -> Observable.fromIterable(pagedApplications.getData()))
                .flatMapSingle(application -> RxJava2Adapter.monoToSingle(tokenService.findTotalTokensByApplication_migrated(application).map(RxJavaReactorMigrationUtil.toJdkFunction(totalToken -> {
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
                }));
    }

    
private Mono<Application> create0_migrated(String domain, Application application, User principal) {
        // created and updated date
        application.setCreatedAt(new Date());
        application.setUpdatedAt(application.getCreatedAt());

        // check uniqueness
        return checkApplicationUniqueness_migrated(domain, application).then(validateApplicationMetadata_migrated(application)).flatMap(v->setDefaultCertificate_migrated(v)).flatMap(v->applicationRepository.create_migrated(v)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Object>>toJdkFunction(application1 -> {
                    if (principal == null || principal.getAdditionalInformation() == null || StringUtils.isEmpty(principal.getAdditionalInformation().get(Claims.organization))) {
                        // There is no principal or we can not find the organization the user is attached to. Can't assign role.
                        return RxJava2Adapter.monoToSingle(Mono.just(application1));
                    }

                    return RxJava2Adapter.monoToSingle(roleService.findSystemRole_migrated(SystemRole.APPLICATION_PRIMARY_OWNER, ReferenceType.APPLICATION).switchIfEmpty(Mono.error(new InvalidRoleException("Cannot assign owner to the application, owner role does not exist"))).flatMap(a->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Role, SingleSource<Object>>toJdkFunction(role -> {
                                Membership membership = new Membership();
                                membership.setDomain(application1.getDomain());
                                membership.setMemberId(principal.getId());
                                membership.setMemberType(MemberType.USER);
                                membership.setReferenceId(application1.getId());
                                membership.setReferenceType(ReferenceType.APPLICATION);
                                membership.setRoleId(role.getId());
                                return RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated((String) principal.getAdditionalInformation().get(Claims.organization), membership).map(RxJavaReactorMigrationUtil.toJdkFunction(__ -> domain)));
                            }).apply(a)))));
                }).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Object, SingleSource<Application>>toJdkFunction(application1 -> {
                    Event event = new Event(Type.APPLICATION, new Payload(application.getId(), ReferenceType.DOMAIN, application.getDomain(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(domain1->Mono.just(application)));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(application1 -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_CREATED).application(application1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_CREATED).throwable(throwable))));
    }

    
private Mono<Application> update0_migrated(String domain, Application currentApplication, Application applicationToUpdate, User principal) {
        // updated date
        applicationToUpdate.setUpdatedAt(new Date());

        // validate application metadata
        return validateApplicationMetadata_migrated(applicationToUpdate).flatMap(v->validateApplicationIdentityProviders_migrated(v)).flatMap(v->applicationRepository.update_migrated(v)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Application, SingleSource<Application>>toJdkFunction(application1 -> {
                    Event event = new Event(Type.APPLICATION, new Payload(application1.getId(), ReferenceType.DOMAIN, application1.getDomain(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(domain1->Mono.just(application1)));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(application -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_UPDATED).oldValue(currentApplication).application(application)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ApplicationAuditBuilder.class).principal(principal).type(EventType.APPLICATION_UPDATED).throwable(throwable))));
    }

    
private Mono<Application> setDefaultCertificate_migrated(Application application) {
        // certificate might have been set via DCR, continue
        if (application.getCertificate() != null) {
            return Mono.just(application);
        }

        return certificateService.findByDomain_migrated(application.getDomain()).collectList().map(RxJavaReactorMigrationUtil.toJdkFunction(certificates -> {
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
                }));
    }

    
private Mono<Void> checkApplicationUniqueness_migrated(String domain, Application application) {
        final String clientId = application.getSettings() != null && application.getSettings().getOauth() != null ? application.getSettings().getOauth().getClientId() : null;
        return findByDomainAndClientId_migrated(domain, clientId).hasElement().flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, CompletableSource>)isEmpty -> {
                    if (!isEmpty) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(new ApplicationAlreadyExistsException(clientId, domain)));
                    }
                    return RxJava2Adapter.monoToCompletable(Mono.empty());
                }).apply(y)))).then();
    }

    
private Mono<Application> validateApplicationIdentityProviders_migrated(Application application) {
        if (application.getIdentities() == null || application.getIdentities().isEmpty()) {
            return Mono.just(application);
        }
        return RxJava2Adapter.singleToMono(Observable.fromIterable(application.getIdentities())
                .flatMapSingle(identity -> RxJava2Adapter.monoToSingle(identityProviderService.findById_migrated(identity).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()).single()))
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
                }));
    }

    
private Mono<Application> validateApplicationMetadata_migrated(Application application) {
        // do nothing if application has no settings
        if (application.getSettings() == null) {
            return Mono.just(application);
        }
        if (application.getSettings().getOauth() == null) {
            return Mono.just(application);
        }
        return GrantTypeUtils.validateGrantTypes_migrated(application).flatMap(v->validateRedirectUris_migrated(v)).flatMap(v->validateScopes_migrated(v)).flatMap(v->validateTokenEndpointAuthMethod_migrated(v)).flatMap(v->RxJava2Adapter.singleToMono((Single<Application>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Application, Single<Application>>)(io.gravitee.am.model.Application ident) -> RxJava2Adapter.monoToSingle(validateTlsClientAuth_migrated(ident))).apply(v)));
    }

    
private Mono<Application> validateRedirectUris_migrated(Application application) {
        ApplicationOAuthSettings oAuthSettings = application.getSettings().getOauth();

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainService.findById_migrated(application.getDomain()).switchIfEmpty(Mono.error(new DomainNotFoundException(application.getDomain()))))
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
                }));
    }

    
private Mono<Application> validateScopes_migrated(Application application) {
        ApplicationOAuthSettings oAuthSettings = application.getSettings().getOauth();
        // check scope approvals and default scopes coherency
        List<String> scopes = oAuthSettings.getScopeSettings() != null ? oAuthSettings.getScopeSettings().stream().map(ApplicationScopeSettings::getScope).collect(Collectors.toList()) : new ArrayList<>();
        List<String> defaultScopes = oAuthSettings.getScopeSettings() != null ? oAuthSettings.getScopeSettings().stream().filter(ApplicationScopeSettings::isDefaultScope).map(ApplicationScopeSettings::getScope).collect(Collectors.toList()) : new ArrayList<>();
        Set<String> scopeApprovals = oAuthSettings.getScopeSettings() != null ? oAuthSettings.getScopeSettings().stream().filter(s -> s.getScopeApproval() != null).map(ApplicationScopeSettings::getScope).collect(Collectors.toSet()) : new HashSet<>();
        if (!scopes.containsAll(defaultScopes)) {
            return Mono.error(new InvalidClientMetadataException("non valid default scopes"));
        }
        if (!scopes.containsAll(scopeApprovals)) {
            return Mono.error(new InvalidClientMetadataException("non valid scope approvals"));
        }
        // check scopes against domain scopes
        return scopeService.validateScope_migrated(application.getDomain(), scopes).flatMap(v->RxJava2Adapter.singleToMono((Single<Application>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, Single<Application>>)isValid -> {
                    if (!isValid) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("non valid scopes")));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(application));
                }).apply(v)));
    }

    
private Mono<Application> validateTokenEndpointAuthMethod_migrated(Application application) {
        ApplicationOAuthSettings oauthSettings = application.getSettings().getOauth();
        String tokenEndpointAuthMethod = oauthSettings.getTokenEndpointAuthMethod();
        if ((ApplicationType.SERVICE == application.getType() || (oauthSettings.getGrantTypes() != null && oauthSettings.getGrantTypes().contains(GrantType.CLIENT_CREDENTIALS)))
                && (tokenEndpointAuthMethod != null && ClientAuthenticationMethod.NONE.equals(tokenEndpointAuthMethod))) {
            return Mono.error(new InvalidClientMetadataException("Invalid token_endpoint_auth_method for service application (client_credentials grant type)"));
        }
        return Mono.just(application);
    }

    
private Mono<Application> validateTlsClientAuth_migrated(Application application) {
        ApplicationOAuthSettings settings = application.getSettings().getOauth();
        if (settings.getTokenEndpointAuthMethod() != null &&
                ClientAuthenticationMethod.TLS_CLIENT_AUTH.equalsIgnoreCase(settings.getTokenEndpointAuthMethod())) {

            if ((settings.getTlsClientAuthSubjectDn() == null || settings.getTlsClientAuthSubjectDn().isEmpty()) &&
                    (settings.getTlsClientAuthSanDns() == null || settings.getTlsClientAuthSanDns().isEmpty()) &&
                    (settings.getTlsClientAuthSanIp() == null || settings.getTlsClientAuthSanIp().isEmpty()) &&
                    (settings.getTlsClientAuthSanEmail() == null || settings.getTlsClientAuthSanEmail().isEmpty()) &&
                    (settings.getTlsClientAuthSanUri() == null || settings.getTlsClientAuthSanUri().isEmpty())) {
                return Mono.error(new InvalidClientMetadataException("Missing TLS parameter for tls_client_auth."));
            }

            if (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty() && (
                    (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty()) ||
                            (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty()) ||
                            (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty()) ||
                            (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
            } else if (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty() && (
                    (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty()) ||
                            (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty()) ||
                            (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty()) ||
                            (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
            } else if (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty() && (
                    (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty()) ||
                            (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty()) ||
                            (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty()) ||
                            (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
            } else if (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty() && (
                    (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty()) ||
                            (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty()) ||
                            (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty()) ||
                            (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
            } else if (settings.getTlsClientAuthSanUri() != null && !settings.getTlsClientAuthSanUri().isEmpty() && (
                    (settings.getTlsClientAuthSubjectDn() != null && !settings.getTlsClientAuthSubjectDn().isEmpty()) ||
                            (settings.getTlsClientAuthSanDns() != null && !settings.getTlsClientAuthSanDns().isEmpty()) ||
                            (settings.getTlsClientAuthSanIp() != null && !settings.getTlsClientAuthSanIp().isEmpty()) ||
                            (settings.getTlsClientAuthSanEmail() != null && !settings.getTlsClientAuthSanEmail().isEmpty()))) {
                return Mono.error(new InvalidClientMetadataException("The tls_client_auth must use exactly one of the TLS parameters."));
            }
        }

        return Mono.just(application);
    }
}

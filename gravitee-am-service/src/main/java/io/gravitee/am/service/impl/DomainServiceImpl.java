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
import io.gravitee.am.common.oidc.Scope;
import io.gravitee.am.common.utils.PathUtils;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.*;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Email;
import io.gravitee.am.model.Environment;
import io.gravitee.am.model.ExtensionGrant;
import io.gravitee.am.model.Factor;
import io.gravitee.am.model.Form;
import io.gravitee.am.model.Group;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.Reporter;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.alert.AlertNotifier;
import io.gravitee.am.model.alert.AlertTrigger;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.model.flow.Flow;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.model.oidc.OIDCSettings;
import io.gravitee.am.model.permissions.SystemRole;
import io.gravitee.am.model.uma.Resource;
import io.gravitee.am.repository.management.api.DomainRepository;
import io.gravitee.am.repository.management.api.search.AlertNotifierCriteria;
import io.gravitee.am.repository.management.api.search.AlertTriggerCriteria;
import io.gravitee.am.repository.management.api.search.DomainCriteria;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.NewDomain;
import io.gravitee.am.service.model.NewSystemScope;
import io.gravitee.am.service.model.PatchDomain;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.DomainAuditBuilder;
import io.gravitee.am.service.validators.AccountSettingsValidator;
import io.gravitee.am.service.validators.DomainValidator;
import io.gravitee.am.service.validators.VirtualHostValidator;
import io.gravitee.common.utils.IdGenerator;
import io.reactivex.*;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class DomainServiceImpl implements DomainService {

    private final Logger LOGGER = LoggerFactory.getLogger(DomainServiceImpl.class);

    private static final Pattern SCHEME_PATTERN = Pattern.compile("^(https?://).*$");

    @Value("${gateway.url:http://localhost:8092}")
    private String gatewayUrl;

    @Lazy
    @Autowired
    private DomainRepository domainRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Autowired
    private ExtensionGrantService extensionGrantService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private FormService formService;

    @Autowired
    private ReporterService reporterService;

    @Autowired
    private FlowService flowService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private EventService eventService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private FactorService factorService;

    @Autowired
    private EnvironmentService environmentService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private AlertTriggerService alertTriggerService;

    @Autowired
    private AlertNotifierService alertNotifierService;

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Domain> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Domain> findById_migrated(String id) {
        LOGGER.debug("Find domain by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(domainRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a domain using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a domain using its ID: %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByHrid_migrated(environmentId, hrid))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Domain> findByHrid(String environmentId, String hrid) {
 return RxJava2Adapter.monoToSingle(findByHrid_migrated(environmentId, hrid));
}
@Override
    public Mono<Domain> findByHrid_migrated(String environmentId, String hrid) {
        LOGGER.debug("Find domain by hrid: {}", hrid);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(domainRepository.findByHrid_migrated(ReferenceType.ENVIRONMENT, environmentId, hrid).switchIfEmpty(Mono.error(new DomainNotFoundException(hrid))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error has occurred when trying to find a domain using its hrid: {}", hrid, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error has occurred when trying to find a domain using its hrid: %s", hrid), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.search_migrated(organizationId, environmentId, query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Domain> search(String organizationId, String environmentId, String query) {
 return RxJava2Adapter.fluxToFlowable(search_migrated(organizationId, environmentId, query));
}
@Override
    public Flux<Domain> search_migrated(String organizationId, String environmentId, String query) {
        LOGGER.debug("Search domains with query {} for environmentId {}", query, environmentId);
        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(environmentService.findById_migrated(environmentId, organizationId).map(RxJavaReactorMigrationUtil.toJdkFunction(Environment::getId)).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(envId -> RxJava2Adapter.fluxToFlowable(domainRepository.search_migrated(environmentId, query)))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error has occurred when trying to search domains with query {} for environmentId {}", query, environmentId, ex);
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByEnvironment_migrated(organizationId, environmentId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Domain> findAllByEnvironment(String organizationId, String environmentId) {
 return RxJava2Adapter.fluxToFlowable(findAllByEnvironment_migrated(organizationId, environmentId));
}
@Override
    public Flux<Domain> findAllByEnvironment_migrated(String organizationId, String environmentId) {
        LOGGER.debug("Find all domains of environment {} (organization {})", environmentId, organizationId);

        return RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(environmentService.findById_migrated(environmentId, organizationId).map(RxJavaReactorMigrationUtil.toJdkFunction(Environment::getId)).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction((java.lang.String ident) -> RxJava2Adapter.fluxToFlowable(domainRepository.findAllByReferenceId_migrated(ident)))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error has occurred when trying to find domains by environment", ex);
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<List<Domain>> findAll() {
 return RxJava2Adapter.monoToSingle(findAll_migrated());
}
@Override
    public Mono<List<Domain>> findAll_migrated() {
        LOGGER.debug("Find all domains");
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(domainRepository.findAll_migrated().collectList())
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find all domains", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find all domains", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAllByCriteria_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Domain> findAllByCriteria(DomainCriteria criteria) {
 return RxJava2Adapter.fluxToFlowable(findAllByCriteria_migrated(criteria));
}
@Override
    public Flux<Domain> findAllByCriteria_migrated(DomainCriteria criteria) {
        LOGGER.debug("Find all domains by criteria");
        return domainRepository.findAllByCriteria_migrated(criteria);
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByIdIn_migrated(ids))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Domain> findByIdIn(Collection<String> ids) {
 return RxJava2Adapter.fluxToFlowable(findByIdIn_migrated(ids));
}
@Override
    public Flux<Domain> findByIdIn_migrated(Collection<String> ids) {
        LOGGER.debug("Find domains by id in {}", ids);
        return domainRepository.findByIdIn_migrated(ids).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find domains by id in {}", ids, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find domains by id in", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(organizationId, environmentId, newDomain, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Domain> create(String organizationId, String environmentId, NewDomain newDomain, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(organizationId, environmentId, newDomain, principal));
}
@Override
    public Mono<Domain> create_migrated(String organizationId, String environmentId, NewDomain newDomain, User principal) {
        LOGGER.debug("Create a new domain: {}", newDomain);
        // generate hrid
        String hrid = IdGenerator.generate(newDomain.getName());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(domainRepository.findByHrid_migrated(ReferenceType.ENVIRONMENT, environmentId, hrid).hasElement().flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Boolean, SingleSource<Domain>>toJdkFunction(empty -> {
                    if (!empty) {
                        throw new DomainAlreadyExistsException(newDomain.getName());
                    } else {
                        Domain domain = new Domain();
                        domain.setId(RandomString.generate());
                        domain.setHrid(hrid);
                        domain.setPath(generateContextPath(newDomain.getName()));
                        domain.setName(newDomain.getName());
                        domain.setDescription(newDomain.getDescription());
                        domain.setEnabled(false);
                        domain.setAlertEnabled(false);
                        domain.setOidc(OIDCSettings.defaultSettings());
                        domain.setReferenceType(ReferenceType.ENVIRONMENT);
                        domain.setReferenceId(environmentId);
                        domain.setCreatedAt(new Date());
                        domain.setUpdatedAt(domain.getCreatedAt());

                        return RxJava2Adapter.monoToSingle(environmentService.findById_migrated(domain.getReferenceId()).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(environment -> setDeployMode(domain, environment))).flatMap(e->validateDomain_migrated(domain, e)).then().then(Mono.defer(()->domainRepository.create_migrated(domain))));
                    }
                }).apply(v)))).flatMap(v->createSystemScopes_migrated(v)).flatMap(v->createDefaultCertificate_migrated(v)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Domain, SingleSource<Domain>>toJdkFunction(domain -> {
                    if (principal == null) {
                        return RxJava2Adapter.monoToSingle(Mono.just(domain));
                    }
                    return RxJava2Adapter.monoToSingle(roleService.findSystemRole_migrated(SystemRole.DOMAIN_PRIMARY_OWNER, ReferenceType.DOMAIN).switchIfEmpty(Mono.error(new InvalidRoleException("Cannot assign owner to the domain, owner role does not exist"))).flatMap(t->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Role, SingleSource<Domain>>toJdkFunction(role -> {
                                Membership membership = new Membership();
                                membership.setDomain(domain.getId());
                                membership.setMemberId(principal.getId());
                                membership.setMemberType(MemberType.USER);
                                membership.setReferenceId(domain.getId());
                                membership.setReferenceType(ReferenceType.DOMAIN);
                                membership.setRoleId(role.getId());
                                return RxJava2Adapter.monoToSingle(membershipService.addOrUpdate_migrated(organizationId, membership).map(RxJavaReactorMigrationUtil.toJdkFunction(__ -> domain)));
                            }).apply(t)))));
                }).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Domain, SingleSource<Domain>>toJdkFunction(domain -> {
                    Event event = new Event(Type.DOMAIN, new Payload(domain.getId(), ReferenceType.DOMAIN, domain.getId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(domain)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a domain", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a domain", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(domain -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_CREATED).domain(domain).referenceType(ReferenceType.ENVIRONMENT).referenceId(environmentId)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_CREATED).referenceType(ReferenceType.ENVIRONMENT).referenceId(environmentId).throwable(throwable))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domainId, domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Domain> update(String domainId, Domain domain) {
 return RxJava2Adapter.monoToSingle(update_migrated(domainId, domain));
}
@Override
    public Mono<Domain> update_migrated(String domainId, Domain domain) {
        LOGGER.debug("Update an existing domain: {}", domain);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainRepository.findById_migrated(domainId).switchIfEmpty(Mono.error(new DomainNotFoundException(domainId))))
                .flatMapSingle(__ -> {
                    domain.setHrid(IdGenerator.generate(domain.getName()));
                    domain.setUpdatedAt(new Date());
                    return RxJava2Adapter.monoToSingle(validateDomain_migrated(domain).then(Mono.defer(()->domainRepository.update_migrated(domain))));
                })).flatMap(v->RxJava2Adapter.singleToMono((Single<Domain>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Domain, Single<Domain>>)domain1 -> {
                    Event event = new Event(Type.DOMAIN, new Payload(domain1.getId(), ReferenceType.DOMAIN, domain1.getId(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(domain1)));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a domain", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a domain", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(domainId, patchDomain, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Domain> patch(String domainId, PatchDomain patchDomain, User principal) {
 return RxJava2Adapter.monoToSingle(patch_migrated(domainId, patchDomain, principal));
}
@Override
    public Mono<Domain> patch_migrated(String domainId, PatchDomain patchDomain, User principal) {
        LOGGER.debug("Patching an existing domain ({}) with : {}", domainId, patchDomain);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(domainRepository.findById_migrated(domainId).switchIfEmpty(Mono.error(new DomainNotFoundException(domainId))))
                .flatMapSingle(oldDomain -> {
                    Domain toPatch = patchDomain.patch(oldDomain);
                    final AccountSettings accountSettings = toPatch.getAccountSettings();
                    if (AccountSettingsValidator.hasInvalidResetPasswordFields(accountSettings)) {
                       return RxJava2Adapter.monoToSingle(Mono.error(new InvalidParameterException("Unexpected forgot password field")));
                    }
                    toPatch.setHrid(IdGenerator.generate(toPatch.getName()));
                    toPatch.setUpdatedAt(new Date());
                    return RxJava2Adapter.monoToSingle(validateDomain_migrated(toPatch).then(Mono.defer(()->domainRepository.update_migrated(toPatch))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Domain, SingleSource<Domain>>toJdkFunction(domain1 -> {
                                Event event = new Event(Type.DOMAIN, new Payload(domain1.getId(), ReferenceType.DOMAIN, domain1.getId(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(domain1)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(domain1 -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_UPDATED).oldValue(oldDomain).domain(domain1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_UPDATED).throwable(throwable)))));

                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to patch a domain", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to patch a domain", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(domainId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String domainId, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(domainId, principal));
}
@Override
    public Mono<Void> delete_migrated(String domainId, User principal) {
        LOGGER.debug("Delete security domain {}", domainId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(domainRepository.findById_migrated(domainId).switchIfEmpty(Mono.error(new DomainNotFoundException(domainId))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Domain, CompletableSource>)domain -> {
                    // delete applications
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(applicationService.findByDomain_migrated(domainId).flatMap(u->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<Set<Application>, CompletableSource>toJdkFunction(applications -> {
                                List<Completable> deleteApplicationsCompletable = applications.stream().map(a -> RxJava2Adapter.monoToCompletable(applicationService.delete_migrated(a.getId()))).collect(Collectors.toList());
                                return Completable.concat(deleteApplicationsCompletable);
                            }).apply(u)))).then(certificateService.findByDomain_migrated(domainId).flatMap(v->certificateService.delete_migrated(v.getId())).then()).then(identityProviderService.findByDomain_migrated(domainId).flatMap(v->identityProviderService.delete_migrated(domainId, v.getId())).then()).then(extensionGrantService.findByDomain_migrated(domainId).flatMap(v->extensionGrantService.delete_migrated(domainId, v.getId())).then()).then(roleService.findByDomain_migrated(domainId).flatMap(a->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<Set<Role>, CompletableSource>toJdkFunction(roles -> {
                                        List<Completable> deleteRolesCompletable = roles.stream().map(r -> RxJava2Adapter.monoToCompletable(roleService.delete_migrated(ReferenceType.DOMAIN, domainId, r.getId()))).collect(Collectors.toList());
                                        return Completable.concat(deleteRolesCompletable);
                                    }).apply(a)))).then()).then(userService.findByDomain_migrated(domainId).flatMap(v->userService.delete_migrated(v.getId())).then()).then(groupService.findByDomain_migrated(domainId).flatMap(v->groupService.delete_migrated(ReferenceType.DOMAIN, domainId, v.getId())).then()).then(scopeService.findByDomain_migrated(domainId, 0, Integer.MAX_VALUE).flatMap(g->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<Page<io.gravitee.am.model.oauth2.Scope>, CompletableSource>toJdkFunction(scopes -> {
                                        List<Completable> deleteScopesCompletable = scopes.getData().stream().map(s -> RxJava2Adapter.monoToCompletable(scopeService.delete_migrated(s.getId(), true))).collect(Collectors.toList());
                                        return Completable.concat(deleteScopesCompletable);
                                    }).apply(g)))).then()).then(emailTemplateService.findAll_migrated(ReferenceType.DOMAIN, domainId).flatMap(v->emailTemplateService.delete_migrated(v.getId())).then()).then(formService.findByDomain_migrated(domainId).flatMap(v->formService.delete_migrated(domainId, v.getId())).then()).then(reporterService.findByDomain_migrated(domainId).flatMap(v->reporterService.delete_migrated(v.getId())).then()).then(flowService.findAll_migrated(ReferenceType.DOMAIN, domainId).filter(RxJavaReactorMigrationUtil.toJdkPredicate(f -> f.getId() != null)).flatMap(v->flowService.delete_migrated(v.getId())).then()).then(membershipService.findByReference_migrated(domainId, ReferenceType.DOMAIN).flatMap(v->membershipService.delete_migrated(v.getId())).then()).then(factorService.findByDomain_migrated(domainId).flatMap(v->factorService.delete_migrated(domainId, v.getId())).then()).then(resourceService.findByDomain_migrated(domainId).flatMap(a->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<Set<Resource>, CompletableSource>toJdkFunction(resources -> {
                                        List<Completable> deletedResourceCompletable = resources.stream().map((io.gravitee.am.model.uma.Resource ident) -> RxJava2Adapter.monoToCompletable(resourceService.delete_migrated(ident))).collect(Collectors.toList());
                                        return Completable.concat(deletedResourceCompletable);
                                    }).apply(a)))).then()).then(alertTriggerService.findByDomainAndCriteria_migrated(domainId, new AlertTriggerCriteria()).flatMap(v->alertTriggerService.delete_migrated(v.getReferenceType(), v.getReferenceId(), v.getId(), principal)).then()).then(alertNotifierService.findByDomainAndCriteria_migrated(domainId, new AlertNotifierCriteria()).flatMap(v->alertNotifierService.delete_migrated(v.getReferenceType(), v.getReferenceId(), v.getId(), principal)).then()).then(domainRepository.delete_migrated(domainId)).then(RxJava2Adapter.completableToMono(Completable.fromSingle(RxJava2Adapter.monoToSingle(eventService.create_migrated(new Event(Type.DOMAIN, new Payload(domainId, ReferenceType.DOMAIN, domainId, Action.DELETE))))))))
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_DELETED).domain(domain)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete security domain {}", domainId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException("An error occurs while trying to delete security domain " + domainId, ex)));
                }));
    }

    
private Mono<Domain> createSystemScopes_migrated(Domain domain) {
        return RxJava2Adapter.singleToMono(Observable.fromArray(Scope.values())
                .flatMapSingle(systemScope -> {
                    final String scopeKey = systemScope.getKey();
                    NewSystemScope scope = new NewSystemScope();
                    scope.setKey(scopeKey);
                    scope.setClaims(systemScope.getClaims());
                    scope.setName(systemScope.getLabel());
                    scope.setDescription(systemScope.getDescription());
                    scope.setDiscovery(systemScope.isDiscovery());
                    return RxJava2Adapter.monoToSingle(scopeService.create_migrated(domain.getId(), scope));
                })
                .lastOrError()).map(RxJavaReactorMigrationUtil.toJdkFunction(scope -> domain));
    }

    @Override
    public String buildUrl(Domain domain, String path) {
        String entryPoint = gatewayUrl;

        if (entryPoint != null && entryPoint.endsWith("/")) {
            entryPoint = entryPoint.substring(0, entryPoint.length() - 1);
        }

        String uri = null;

        if (domain.isVhostMode()) {
            // Try generate uri using defined virtual hosts.
            Matcher matcher = SCHEME_PATTERN.matcher(entryPoint);
            String scheme = "http";
            if (matcher.matches()) {
                scheme = matcher.group(1);
            }

            for (VirtualHost vhost : domain.getVhosts()) {
                if (vhost.isOverrideEntrypoint()) {
                    uri = scheme + vhost.getHost() + vhost.getPath() + path;
                    break;
                }
            }
        }

        if (uri == null) {
            uri = entryPoint + PathUtils.sanitize(domain.getPath() + path);
        }

        return uri;
    }

    
private Mono<Domain> createDefaultCertificate_migrated(Domain domain) {
        return certificateService.create_migrated(domain.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(certificate -> domain));
    }

    private String generateContextPath(String domainName) {

        return "/" + IdGenerator.generate(domainName);
    }

    
private Mono<Void> validateDomain_migrated(Domain domain) {
        if (domain.getReferenceType() != ReferenceType.ENVIRONMENT) {
            return Mono.error(new InvalidDomainException("Domain must be attached to an environment"));
        }

        // check the uniqueness of the domain
        return domainRepository.findByHrid_migrated(domain.getReferenceType(), domain.getReferenceId(), domain.getHrid()).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Optional<Domain>, CompletableSource>)optDomain -> {
                    if (optDomain.isPresent() && !optDomain.get().getId().equals(domain.getId())) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(new DomainAlreadyExistsException(domain.getName())));
                    } else {
                        // Get environment domain restrictions and validate all data are correctly defined.
                        return RxJava2Adapter.monoToCompletable(environmentService.findById_migrated(domain.getReferenceId()).flatMap(v->validateDomain_migrated(domain, v)).then());
                    }
                }).apply(y)))).then();
    }

    
private Mono<Void> validateDomain_migrated(Domain domain, Environment environment) {

        // Get environment domain restrictions and validate all data are correctly defined.
        return DomainValidator.validate_migrated(domain, environment.getDomainRestrictions()).then(findAll_migrated().flatMap(v->VirtualHostValidator.validateDomainVhosts_migrated(domain, v)).then());
    }

    private void setDeployMode(Domain domain, Environment environment) {

        if (CollectionUtils.isEmpty(environment.getDomainRestrictions())) {
            domain.setVhostMode(false);
        } else {
            // There are some domain restrictions defined at environment level. Switching to domain vhost mode.

            // Creating one vhost per constraint.
            List<VirtualHost> vhosts = environment.getDomainRestrictions().stream().map(domainConstraint -> {
                VirtualHost virtualHost = new VirtualHost();
                virtualHost.setHost(domainConstraint);
                virtualHost.setPath(domain.getPath());

                return virtualHost;
            }).collect(Collectors.toList());

            // The first one will be used as primary displayed entrypoint.
            vhosts.get(0).setOverrideEntrypoint(true);

            domain.setVhostMode(true);
            domain.setVhosts(vhosts);
        }
    }
}

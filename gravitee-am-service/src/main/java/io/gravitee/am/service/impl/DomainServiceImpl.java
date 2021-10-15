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
import io.gravitee.am.common.oidc.Scope;
import io.gravitee.am.common.utils.PathUtils;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.*;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Environment;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.alert.AlertNotifier;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.model.oidc.OIDCSettings;
import io.gravitee.am.model.permissions.SystemRole;
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

    @Override
    public Maybe<Domain> findById(String id) {
        LOGGER.debug("Find domain by ID: {}", id);
        return domainRepository.findById(id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a domain using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a domain using its ID: %s", id), ex)));
                });
    }

    @Override
    public Single<Domain> findByHrid(String environmentId, String hrid) {
        LOGGER.debug("Find domain by hrid: {}", hrid);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(domainRepository.findByHrid(ReferenceType.ENVIRONMENT, environmentId, hrid)).switchIfEmpty(Mono.error(new DomainNotFoundException(hrid))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error has occurred when trying to find a domain using its hrid: {}", hrid, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error has occurred when trying to find a domain using its hrid: %s", hrid), ex)));
                });
    }

    @Override
    public Flowable<Domain> search(String organizationId, String environmentId, String query) {
        LOGGER.debug("Search domains with query {} for environmentId {}", query, environmentId);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(environmentService.findById(environmentId, organizationId)).map(RxJavaReactorMigrationUtil.toJdkFunction(Environment::getId)).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(envId -> domainRepository.search(environmentId, query))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error has occurred when trying to search domains with query {} for environmentId {}", query, environmentId, ex);
                });
    }

    @Override
    public Flowable<Domain> findAllByEnvironment(String organizationId, String environmentId) {
        LOGGER.debug("Find all domains of environment {} (organization {})", environmentId, organizationId);

        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.singleToMono(environmentService.findById(environmentId, organizationId)).map(RxJavaReactorMigrationUtil.toJdkFunction(Environment::getId)).flatMapMany(RxJavaReactorMigrationUtil.toJdkFunction(domainRepository::findAllByReferenceId)))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error has occurred when trying to find domains by environment", ex);
                });
    }

    @Override
    public Single<List<Domain>> findAll() {
        LOGGER.debug("Find all domains");
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(domainRepository.findAll()).collectList())
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find all domains", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find all domains", ex)));
                });
    }

    @Override
    public Flowable<Domain> findAllByCriteria(DomainCriteria criteria) {
        LOGGER.debug("Find all domains by criteria");
        return domainRepository.findAllByCriteria(criteria);
    }

    @Override
    public Flowable<Domain> findByIdIn(Collection<String> ids) {
        LOGGER.debug("Find domains by id in {}", ids);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(domainRepository.findByIdIn(ids)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find domains by id in {}", ids, ex);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException("An error occurs while trying to find domains by id in", ex)));
                })));
    }

    @Override
    public Single<Domain> create(String organizationId, String environmentId, NewDomain newDomain, User principal) {
        LOGGER.debug("Create a new domain: {}", newDomain);
        // generate hrid
        String hrid = IdGenerator.generate(newDomain.getName());
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(domainRepository.findByHrid(ReferenceType.ENVIRONMENT, environmentId, hrid)
                .isEmpty()
                .flatMap(empty -> {
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

                        return environmentService.findById(domain.getReferenceId())
                                .doOnSuccess(environment -> setDeployMode(domain, environment))
                                .flatMapCompletable(environment -> validateDomain(domain, environment))
                                .andThen(Single.defer(() -> domainRepository.create(domain)));
                    }
                })).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Domain, SingleSource<Domain>>toJdkFunction(this::createSystemScopes).apply(v)))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Domain, SingleSource<Domain>>toJdkFunction(this::createDefaultCertificate).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Domain, SingleSource<Domain>>toJdkFunction(domain -> {
                    if (principal == null) {
                        return RxJava2Adapter.monoToSingle(Mono.just(domain));
                    }
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(roleService.findSystemRole(SystemRole.DOMAIN_PRIMARY_OWNER, ReferenceType.DOMAIN)).switchIfEmpty(RxJava2Adapter.singleToMono(Single.wrap(Single.error(new InvalidRoleException("Cannot assign owner to the domain, owner role does not exist"))))).flatMap(t->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Role, SingleSource<Domain>>toJdkFunction(role -> {
                                Membership membership = new Membership();
                                membership.setDomain(domain.getId());
                                membership.setMemberId(principal.getId());
                                membership.setMemberType(MemberType.USER);
                                membership.setReferenceId(domain.getId());
                                membership.setReferenceType(ReferenceType.DOMAIN);
                                membership.setRoleId(role.getId());
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(membershipService.addOrUpdate(organizationId, membership)).map(RxJavaReactorMigrationUtil.toJdkFunction(__ -> domain)));
                            }).apply(t)))));
                }).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Domain, SingleSource<Domain>>toJdkFunction(domain -> {
                    Event event = new Event(Type.DOMAIN, new Payload(domain.getId(), ReferenceType.DOMAIN, domain.getId(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(domain)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a domain", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a domain", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(domain -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_CREATED).domain(domain).referenceType(ReferenceType.ENVIRONMENT).referenceId(environmentId)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_CREATED).referenceType(ReferenceType.ENVIRONMENT).referenceId(environmentId).throwable(throwable)))));
    }

    @Override
    public Single<Domain> update(String domainId, Domain domain) {
        LOGGER.debug("Update an existing domain: {}", domain);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainRepository.findById(domainId)).switchIfEmpty(Mono.error(new DomainNotFoundException(domainId))))
                .flatMapSingle(__ -> {
                    domain.setHrid(IdGenerator.generate(domain.getName()));
                    domain.setUpdatedAt(new Date());
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(validateDomain(domain)).then(Mono.defer(()->RxJava2Adapter.singleToMono(domainRepository.update(domain)))));
                })).flatMap(v->RxJava2Adapter.singleToMono((Single<Domain>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Domain, Single<Domain>>)domain1 -> {
                    Event event = new Event(Type.DOMAIN, new Payload(domain1.getId(), ReferenceType.DOMAIN, domain1.getId(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(domain1)));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a domain", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a domain", ex)));
                });
    }

    @Override
    public Single<Domain> patch(String domainId, PatchDomain patchDomain, User principal) {
        LOGGER.debug("Patching an existing domain ({}) with : {}", domainId, patchDomain);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(domainRepository.findById(domainId)).switchIfEmpty(Mono.error(new DomainNotFoundException(domainId))))
                .flatMapSingle(oldDomain -> {
                    Domain toPatch = patchDomain.patch(oldDomain);
                    final AccountSettings accountSettings = toPatch.getAccountSettings();
                    if (AccountSettingsValidator.hasInvalidResetPasswordFields(accountSettings)) {
                       return RxJava2Adapter.monoToSingle(Mono.error(new InvalidParameterException("Unexpected forgot password field")));
                    }
                    toPatch.setHrid(IdGenerator.generate(toPatch.getName()));
                    toPatch.setUpdatedAt(new Date());
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(validateDomain(toPatch)).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.defer(()->RxJava2Adapter.singleToMono(domainRepository.update(toPatch)))))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Domain, SingleSource<Domain>>toJdkFunction(domain1 -> {
                                Event event = new Event(Type.DOMAIN, new Payload(domain1.getId(), ReferenceType.DOMAIN, domain1.getId(), Action.UPDATE));
                                return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(domain1)));
                            }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(domain1 -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_UPDATED).oldValue(oldDomain).domain(domain1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_UPDATED).throwable(throwable)))));

                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to patch a domain", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to patch a domain", ex)));
                });
    }

    @Override
    public Completable delete(String domainId, User principal) {
        LOGGER.debug("Delete security domain {}", domainId);
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(domainRepository.findById(domainId)).switchIfEmpty(Mono.error(new DomainNotFoundException(domainId))).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Domain, CompletableSource>)domain -> {
                    // delete applications
                    return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(applicationService.findByDomain(domainId)
                            .flatMapCompletable(applications -> {
                                List<Completable> deleteApplicationsCompletable = applications.stream().map(a -> applicationService.delete(a.getId())).collect(Collectors.toList());
                                return Completable.concat(deleteApplicationsCompletable);
                            })
                            // delete certificates
                            .andThen(certificateService.findByDomain(domainId)
                                    .flatMapCompletable(certificate -> certificateService.delete(certificate.getId()))
                            )
                            // delete identity providers
                            .andThen(identityProviderService.findByDomain(domainId)
                                    .flatMapCompletable(identityProvider ->
                                        identityProviderService.delete(domainId, identityProvider.getId())
                                    )
                            )
                            // delete extension grants
                            .andThen(extensionGrantService.findByDomain(domainId)
                                    .flatMapCompletable(extensionGrant -> extensionGrantService.delete(domainId, extensionGrant.getId()))
                            )
                            // delete roles
                            .andThen(roleService.findByDomain(domainId)
                                    .flatMapCompletable(roles -> {
                                        List<Completable> deleteRolesCompletable = roles.stream().map(r -> roleService.delete(ReferenceType.DOMAIN, domainId, r.getId())).collect(Collectors.toList());
                                        return Completable.concat(deleteRolesCompletable);
                                    })
                            )
                            // delete users
                            .andThen(userService.findByDomain(domainId)
                                    .flatMapCompletable(user ->
                                        userService.delete(user.getId()))
                            )
                            // delete groups
                            .andThen(groupService.findByDomain(domainId)
                                    .flatMapCompletable(group ->
                                        groupService.delete(ReferenceType.DOMAIN, domainId, group.getId()))
                            )
                            // delete scopes
                            .andThen(scopeService.findByDomain(domainId, 0, Integer.MAX_VALUE)
                                    .flatMapCompletable(scopes -> {
                                        List<Completable> deleteScopesCompletable = scopes.getData().stream().map(s -> scopeService.delete(s.getId(), true)).collect(Collectors.toList());
                                        return Completable.concat(deleteScopesCompletable);
                                    })
                            )
                            // delete email templates
                            .andThen(emailTemplateService.findAll(ReferenceType.DOMAIN, domainId)
                                    .flatMapCompletable(emailTemplate -> emailTemplateService.delete(emailTemplate.getId()))
                            )
                            // delete form templates
                            .andThen(formService.findByDomain(domainId)
                                    .flatMapCompletable(formTemplate -> formService.delete(domainId, formTemplate.getId()))
                            )
                            // delete reporters
                            .andThen(reporterService.findByDomain(domainId)
                                    .flatMapCompletable(reporter ->
                                        reporterService.delete(reporter.getId()))
                            )
                            // delete flows
                            .andThen(flowService.findAll(ReferenceType.DOMAIN, domainId)
                                    .filter(f -> f.getId() != null)
                                    .flatMapCompletable(flows -> flowService.delete(flows.getId()))
                            )
                            // delete memberships
                            .andThen(membershipService.findByReference(domainId, ReferenceType.DOMAIN)
                                    .flatMapCompletable(membership ->  membershipService.delete(membership.getId()))
                            )
                            // delete factors
                            .andThen(factorService.findByDomain(domainId)
                                    .flatMapCompletable(factor -> factorService.delete(domainId, factor.getId()))
                            )
                            // delete uma resources
                            .andThen(resourceService.findByDomain(domainId)
                                    .flatMapCompletable(resources -> {
                                        List<Completable> deletedResourceCompletable = resources.stream().map(resourceService::delete).collect(Collectors.toList());
                                        return Completable.concat(deletedResourceCompletable);
                                    })
                            )).then(RxJava2Adapter.completableToMono(Completable.wrap(alertTriggerService.findByDomainAndCriteria(domainId, new AlertTriggerCriteria())
                                    .flatMapCompletable(alertTrigger -> alertTriggerService.delete(alertTrigger.getReferenceType(), alertTrigger.getReferenceId(), alertTrigger.getId(), principal)
                                    )))).then(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.flowableToFlux(alertNotifierService.findByDomainAndCriteria(domainId, new AlertNotifierCriteria())).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<AlertNotifier, CompletableSource>toJdkFunction(alertNotifier -> alertNotifierService.delete(alertNotifier.getReferenceType(), alertNotifier.getReferenceId(), alertNotifier.getId(), principal)).apply(y)))).then()))).then(RxJava2Adapter.completableToMono(domainRepository.delete(domainId))).then(RxJava2Adapter.completableToMono(Completable.fromSingle(eventService.create(new Event(Type.DOMAIN, new Payload(domainId, ReferenceType.DOMAIN, domainId, Action.DELETE)))))))
                            .doOnComplete(() -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_DELETED).domain(domain)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(DomainAuditBuilder.class).principal(principal).type(EventType.DOMAIN_DELETED).throwable(throwable)))));
                }).apply(y)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete security domain {}", domainId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException("An error occurs while trying to delete security domain " + domainId, ex)));
                });
    }

    private Single<Domain> createSystemScopes(Domain domain) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Observable.fromArray(Scope.values())
                .flatMapSingle(systemScope -> {
                    final String scopeKey = systemScope.getKey();
                    NewSystemScope scope = new NewSystemScope();
                    scope.setKey(scopeKey);
                    scope.setClaims(systemScope.getClaims());
                    scope.setName(systemScope.getLabel());
                    scope.setDescription(systemScope.getDescription());
                    scope.setDiscovery(systemScope.isDiscovery());
                    return scopeService.create(domain.getId(), scope);
                })
                .lastOrError()).map(RxJavaReactorMigrationUtil.toJdkFunction(scope -> domain)));
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

    private Single<Domain> createDefaultCertificate(Domain domain) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(certificateService
                .create(domain.getId())).map(RxJavaReactorMigrationUtil.toJdkFunction(certificate -> domain)));
    }

    private String generateContextPath(String domainName) {

        return "/" + IdGenerator.generate(domainName);
    }

    private Completable validateDomain(Domain domain) {
        if (domain.getReferenceType() != ReferenceType.ENVIRONMENT) {
            return RxJava2Adapter.monoToCompletable(Mono.error(new InvalidDomainException("Domain must be attached to an environment")));
        }

        // check the uniqueness of the domain
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(domainRepository.findByHrid(domain.getReferenceType(), domain.getReferenceId(), domain.getHrid())).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<Optional<Domain>, CompletableSource>)optDomain -> {
                    if (optDomain.isPresent() && !optDomain.get().getId().equals(domain.getId())) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(new DomainAlreadyExistsException(domain.getName())));
                    } else {
                        // Get environment domain restrictions and validate all data are correctly defined.
                        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(environmentService.findById(domain.getReferenceId())).flatMap(z->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<Environment, CompletableSource>toJdkFunction(environment -> validateDomain(domain, environment)).apply(z)))).then());
                    }
                }).apply(y)))).then());
    }

    private Completable validateDomain(Domain domain, Environment environment) {

        // Get environment domain restrictions and validate all data are correctly defined.
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(DomainValidator.validate(domain, environment.getDomainRestrictions())).then(RxJava2Adapter.singleToMono(findAll()).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<List<Domain>, CompletableSource>toJdkFunction(domains -> VirtualHostValidator.validateDomainVhosts(domain, domains)).apply(y)))).then()));
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

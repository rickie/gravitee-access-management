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
import io.gravitee.am.model.application.ApplicationOAuthSettings;
import io.gravitee.am.model.application.ApplicationScopeSettings;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.common.event.Payload;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.repository.management.api.ScopeRepository;
import io.gravitee.am.repository.oauth2.api.ScopeApprovalRepository;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.model.*;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.management.ScopeAuditBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@Component
public class ScopeServiceImpl implements ScopeService {

    /**
     * Logger.
     */
    private final Logger LOGGER = LoggerFactory.getLogger(ScopeServiceImpl.class);

    @Lazy
    @Autowired
    private ScopeRepository scopeRepository;

    @Lazy
    @Autowired
    private ScopeApprovalRepository scopeApprovalRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private AuditService auditService;

    @Override
    public Maybe<Scope> findById(String id) {
        LOGGER.debug("Find scope by ID: {}", id);
        return scopeRepository.findById(id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a scope using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a scope using its ID: %s", id), ex)));
                });
    }

    @Override
    public Single<Page<Scope>> search(String domain, String query, int page, int size) {
        LOGGER.debug("Search scopes by domain and query: {} {}", domain, query);
        return scopeRepository.search(domain, query, page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find scopes by domain and query : {} {}", domain, query, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find scopes by domain and query: %s %s", domain, query), ex)));
                });
    }

    @Override
    public Single<Scope> create(String domain, NewScope newScope, User principal) {
        LOGGER.debug("Create a new scope {} for domain {}", newScope, domain);
        // replace all whitespace by an underscore (whitespace is a reserved keyword to separate tokens)
        String scopeKey = newScope.getKey().replaceAll("\\s+", "_");
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(scopeRepository.findByDomainAndKey(domain, scopeKey)
                .isEmpty()).map(RxJavaReactorMigrationUtil.toJdkFunction(empty -> {
                    if (!empty) {
                        throw new ScopeAlreadyExistsException(scopeKey, domain);
                    }
                    Scope scope = new Scope();
                    scope.setId(RandomString.generate());
                    scope.setDomain(domain);
                    scope.setKey(scopeKey);
                    scope.setName(newScope.getName());
                    scope.setDescription(newScope.getDescription());
                    scope.setIconUri(newScope.getIconUri());
                    scope.setExpiresIn(newScope.getExpiresIn());
                    scope.setDiscovery(newScope.isDiscovery());
                    scope.setParameterized(newScope.isParameterized());
                    scope.setCreatedAt(new Date());
                    scope.setUpdatedAt(new Date());

                    return scope;
                })))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(this::validateIconUri).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(scopeRepository::create).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(scope -> {
                    // create event for sync process
                    Event event = new Event(Type.SCOPE, new Payload(scope.getId(), ReferenceType.DOMAIN, scope.getDomain(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(scope)));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a scope", ex)));
                })).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(scope -> auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_CREATED).scope(scope)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_CREATED).throwable(throwable)))));
    }

    @Override
    public Single<Scope> create(String domain, NewSystemScope newScope) {
        LOGGER.debug("Create a new system scope {} for domain {}", newScope, domain);
        String scopeKey = newScope.getKey().toLowerCase();
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(scopeRepository.findByDomainAndKey(domain, scopeKey)).hasElement().flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Boolean, SingleSource<Scope>>toJdkFunction(empty -> {
                    if (!empty) {
                        throw new ScopeAlreadyExistsException(scopeKey, domain);
                    }
                    Scope scope = new Scope();
                    scope.setId(RandomString.generate());
                    scope.setDomain(domain);
                    scope.setKey(scopeKey);
                    scope.setSystem(true);
                    scope.setClaims(newScope.getClaims());
                    scope.setName(newScope.getName());
                    scope.setDescription(newScope.getDescription());
                    scope.setExpiresIn(newScope.getExpiresIn());
                    scope.setDiscovery(newScope.isDiscovery());
                    scope.setParameterized(false);
                    scope.setCreatedAt(new Date());
                    scope.setUpdatedAt(new Date());
                    return scopeRepository.create(scope);
                }).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono((Single<Scope>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Scope, Single<Scope>>)scope -> {
                    // create event for sync process
                    Event event = new Event(Type.SCOPE, new Payload(scope.getId(), ReferenceType.DOMAIN, scope.getDomain(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(scope)));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create a system scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a system scope", ex)));
                });
    }

    @Override
    public Single<Scope> patch(String domain, String id, PatchScope patchScope, User principal) {
        LOGGER.debug("Patching a scope {} for domain {}", id, domain);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(scopeRepository.findById(id)).switchIfEmpty(Mono.error(new ScopeNotFoundException(id))))
                .flatMapSingle(oldScope -> {
                    Scope scopeToUpdate = patchScope.patch(oldScope);
                    return update(domain, scopeToUpdate, oldScope, principal);
                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to patch a scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to patch a scope", ex)));
                });
    }

    @Override
    public Single<Scope> update(String domain, String id, UpdateScope updateScope, User principal) {
        LOGGER.debug("Update a scope {} for domain {}", id, domain);
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(scopeRepository.findById(id)).switchIfEmpty(Mono.error(new ScopeNotFoundException(id))))
                .flatMapSingle(oldScope -> {
                    Scope scopeToUpdate = new Scope(oldScope);
                    scopeToUpdate.setName(updateScope.getName());
                    scopeToUpdate.setDescription(updateScope.getDescription());
                    scopeToUpdate.setExpiresIn(updateScope.getExpiresIn());
                    if (!oldScope.isSystem() && updateScope.getDiscovery() != null) {
                        scopeToUpdate.setDiscovery(updateScope.isDiscovery());
                    }
                    if (!oldScope.isSystem() && updateScope.getParameterized() != null) {
                        scopeToUpdate.setParameterized(updateScope.isParameterized());
                    }
                    scopeToUpdate.setIconUri(updateScope.getIconUri());

                    return update(domain, scopeToUpdate, oldScope, principal);
                })
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a scope", ex)));
                });
    }

    private Single<Scope> update(String domain, Scope toUpdate, Scope oldValue, User principal) {

        toUpdate.setUpdatedAt(new Date());
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.validateIconUri(toUpdate)).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(scopeRepository::update).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(scope1 -> {
                    // create event for sync process
                    Event event = new Event(Type.SCOPE, new Payload(scope1.getId(), ReferenceType.DOMAIN, scope1.getDomain(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(scope1)));
                }).apply(v)))).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(scope1 -> auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_UPDATED).oldValue(oldValue).scope(scope1)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_UPDATED).throwable(throwable)))));
    }

    @Override
    public Single<Scope> update(String domain, String id, UpdateSystemScope updateScope) {
        LOGGER.debug("Update a system scope {} for domain {}", id, domain);
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(scopeRepository.findById(id)).switchIfEmpty(Mono.error(new ScopeNotFoundException(id))))
                .flatMapSingle(scope -> {
                    scope.setName(updateScope.getName());
                    scope.setDescription(updateScope.getDescription());
                    scope.setUpdatedAt(new Date());
                    scope.setSystem(true);
                    scope.setClaims(updateScope.getClaims());
                    scope.setExpiresIn(updateScope.getExpiresIn());
                    scope.setDiscovery(updateScope.isDiscovery());
                    return scopeRepository.update(scope);
                })).flatMap(v->RxJava2Adapter.singleToMono((Single<Scope>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Scope, Single<Scope>>)scope -> {
                    // create event for sync process
                    Event event = new Event(Type.SCOPE, new Payload(scope.getId(), ReferenceType.DOMAIN, scope.getDomain(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(eventService.create(event)).flatMap(__->Mono.just(scope)));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a system scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a system scope", ex)));
                });
    }

    @Override
    public Completable delete(String scopeId, boolean force, User principal) {
        LOGGER.debug("Delete scope {}", scopeId);
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(scopeRepository.findById(scopeId)).switchIfEmpty(Mono.error(new ScopeNotFoundException(scopeId))))
                .flatMapSingle(scope -> {
                    if (scope.isSystem() && !force) {
                        throw new SystemScopeDeleteException(scopeId);
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(scope));
                })).flatMap(scope->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(Completable.fromSingle(roleService.findByDomain(scope.getDomain()).flatMapObservable((java.util.Set<io.gravitee.am.model.Role> roles)->Observable.fromIterable(roles.stream().filter((io.gravitee.am.model.Role role)->role.getOauthScopes() != null && role.getOauthScopes().contains(scope.getKey())).collect(Collectors.toList()))).flatMapSingle((io.gravitee.am.model.Role role)->{
role.getOauthScopes().remove(scope.getKey());
UpdateRole updatedRole = new UpdateRole();
updatedRole.setName(role.getName());
updatedRole.setDescription(role.getDescription());
updatedRole.setPermissions(role.getOauthScopes());
return roleService.update(scope.getDomain(), role.getId(), updatedRole);
}).toList()).andThen(applicationService.findByDomain(scope.getDomain()).flatMapObservable((java.util.Set<io.gravitee.am.model.Application> applications)->Observable.fromIterable(applications.stream().filter((io.gravitee.am.model.Application application)->{
if (application.getSettings() == null) {
return false;
}
if (application.getSettings().getOauth() == null) {
return false;
}
ApplicationOAuthSettings oAuthSettings = application.getSettings().getOauth();
return oAuthSettings.getScopeSettings() != null && !oAuthSettings.getScopeSettings().stream().filter((io.gravitee.am.model.application.ApplicationScopeSettings s)->s.getScope().equals(scope.getKey())).findFirst().isEmpty();
}).collect(Collectors.toList()))).flatMapSingle((io.gravitee.am.model.Application application)->{
final List<ApplicationScopeSettings> cleanScopes = application.getSettings().getOauth().getScopeSettings().stream().filter((io.gravitee.am.model.application.ApplicationScopeSettings s)->!s.getScope().equals(scope.getKey())).collect(Collectors.toList());
application.getSettings().getOauth().setScopeSettings(cleanScopes);
return applicationService.update(application);
}).toList()).toCompletable()).then(RxJava2Adapter.completableToMono(Completable.wrap(scopeApprovalRepository.deleteByDomainAndScopeKey(scope.getDomain(), scope.getKey())))).then(RxJava2Adapter.completableToMono(scopeRepository.delete(scopeId))).then(RxJava2Adapter.completableToMono(Completable.fromSingle(eventService.create(new Event(Type.SCOPE, new Payload(scope.getId(), ReferenceType.DOMAIN, scope.getDomain(), Action.DELETE))))))).doOnComplete(()->auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_DELETED).scope(scope)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_DELETED).throwable(throwable))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete scope: {}", scopeId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete scope: %s", scopeId), ex)));
                });
    }

    @Override
    public Single<Page<Scope>> findByDomain(String domain, int page, int size) {
        LOGGER.debug("Find scopes by domain: {}", domain);
        return scopeRepository.findByDomain(domain, page, size)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find scopes by domain: {}", domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find scopes by domain: %s", domain), ex)));
                });
    }

    @Override
    public Maybe<Scope> findByDomainAndKey(String domain, String scopeKey) {
        LOGGER.debug("Find scopes by domain: {} and scope key: {}", domain, scopeKey);
        return scopeRepository.findByDomainAndKey(domain, scopeKey)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find scopes by domain: {} and scope key: {}", domain, scopeKey, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find scopes by domain: %s and scope key: %s", domain, scopeKey), ex)));
                });
    }

    @Override
    public Single<List<Scope>> findByDomainAndKeys(String domain, List<String> scopeKeys) {
        LOGGER.debug("Find scopes by domain: {} and scope keys: {}", domain, scopeKeys);
        if(scopeKeys==null || scopeKeys.isEmpty()) {
            return RxJava2Adapter.monoToSingle(Mono.just(Collections.emptyList()));
        }
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(scopeRepository.findByDomainAndKeys(domain, scopeKeys)).collectList())
                .onErrorResumeNext(ex -> {
                    String keys = scopeKeys!=null?String.join(",",scopeKeys):null;
                    LOGGER.error("An error occurs while trying to find scopes by domain: {} and scope keys: {}", domain, keys, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find scopes by domain: %s and scope keys: %s", domain, keys), ex)));
                });
    }

    /**
     * Throw InvalidClientMetadataException if null or empty, or contains unknown scope.
     * @param scopes Array of scope to validate.
     */
    @Override
    public Single<Boolean> validateScope(String domain, List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return RxJava2Adapter.monoToSingle(Mono.just(true));//nothing to do...
        }

        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(findByDomain(domain, 0, Integer.MAX_VALUE)).map(RxJavaReactorMigrationUtil.toJdkFunction(domainSet -> domainSet.getData().stream().map(Scope::getKey).collect(Collectors.toSet()))).flatMap(domainScopes->RxJava2Adapter.singleToMono(this.validateScope(domainScopes, scopes))));
    }

    private Single<Boolean> validateScope(Set<String> domainScopes, List<String> scopes) {

        for (String scope : scopes) {
            if (!domainScopes.contains(scope)) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidClientMetadataException("scope " + scope + " is not valid.")));
            }
        }

        return RxJava2Adapter.monoToSingle(Mono.just(true));
    }

    private Single<Scope> validateIconUri(Scope scope) {
        if(scope.getIconUri()!=null) {
            try {
                URI.create(scope.getIconUri()).toURL();
            } catch (MalformedURLException | IllegalArgumentException e) {
                return RxJava2Adapter.monoToSingle(Mono.error(new MalformedIconUriException(scope.getIconUri())));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(scope));
    }
}

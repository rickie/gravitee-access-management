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
import io.gravitee.am.model.Application;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
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
import io.reactivex.Maybe;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Scope> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<Scope> findById_migrated(String id) {
        LOGGER.debug("Find scope by ID: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(scopeRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a scope using its ID: {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a scope using its ID: %s", id), ex)));
                }));
    }

    
@Override
    public Mono<Page<Scope>> search_migrated(String domain, String query, int page, int size) {
        LOGGER.debug("Search scopes by domain and query: {} {}", domain, query);
        return scopeRepository.search_migrated(domain, query, page, size).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Page<Scope>>>toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find scopes by domain and query : {} {}", domain, query, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find scopes by domain and query: %s %s", domain, query), ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newScope, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Scope> create(String domain, NewScope newScope, User principal) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newScope, principal));
}
@Override
    public Mono<Scope> create_migrated(String domain, NewScope newScope, User principal) {
        LOGGER.debug("Create a new scope {} for domain {}", newScope, domain);
        // replace all whitespace by an underscore (whitespace is a reserved keyword to separate tokens)
        String scopeKey = newScope.getKey().replaceAll("\\s+", "_");
        return scopeRepository.findByDomainAndKey_migrated(domain, scopeKey).hasElement().map(RxJavaReactorMigrationUtil.toJdkFunction(empty -> {
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
                })).flatMap(this::validateIconUri_migrated).flatMap(scopeRepository::create_migrated).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(scope -> {
                    // create event for sync process
                    Event event = new Event(Type.SCOPE, new Payload(scope.getId(), ReferenceType.DOMAIN, scope.getDomain(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(scope)));
                }).apply(v)))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Scope>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to create a scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a scope", ex)));
                }).apply(err))).doOnSuccess(scope -> auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_CREATED).scope(scope))).doOnError(throwable -> auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_CREATED).throwable(throwable)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newScope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Scope> create(String domain, NewSystemScope newScope) {
 return RxJava2Adapter.monoToSingle(create_migrated(domain, newScope));
}
@Override
    public Mono<Scope> create_migrated(String domain, NewSystemScope newScope) {
        LOGGER.debug("Create a new system scope {} for domain {}", newScope, domain);
        String scopeKey = newScope.getKey().toLowerCase();
        return scopeRepository.findByDomainAndKey_migrated(domain, scopeKey).hasElement().flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Boolean, SingleSource<Scope>>toJdkFunction(empty -> {
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
                    return RxJava2Adapter.monoToSingle(scopeRepository.create_migrated(scope));
                }).apply(v)))).flatMap(v->RxJava2Adapter.singleToMono((Single<Scope>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Scope, Single<Scope>>)scope -> {
                    // create event for sync process
                    Event event = new Event(Type.SCOPE, new Payload(scope.getId(), ReferenceType.DOMAIN, scope.getDomain(), Action.CREATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(scope)));
                }).apply(v))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Scope>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to create a system scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to create a system scope", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.patch_migrated(domain, id, patchScope, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Scope> patch(String domain, String id, PatchScope patchScope, User principal) {
 return RxJava2Adapter.monoToSingle(patch_migrated(domain, id, patchScope, principal));
}
@Override
    public Mono<Scope> patch_migrated(String domain, String id, PatchScope patchScope, User principal) {
        LOGGER.debug("Patching a scope {} for domain {}", id, domain);
        return scopeRepository.findById_migrated(id).switchIfEmpty(Mono.error(new ScopeNotFoundException(id))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(oldScope -> {
                    Scope scopeToUpdate = patchScope.patch(oldScope);
                    return RxJava2Adapter.monoToSingle(update_migrated(domain, scopeToUpdate, oldScope, principal));
                }).apply(y)))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Scope>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to patch a scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to patch a scope", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateScope, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Scope> update(String domain, String id, UpdateScope updateScope, User principal) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateScope, principal));
}
@Override
    public Mono<Scope> update_migrated(String domain, String id, UpdateScope updateScope, User principal) {
        LOGGER.debug("Update a scope {} for domain {}", id, domain);
        return scopeRepository.findById_migrated(id).switchIfEmpty(Mono.error(new ScopeNotFoundException(id))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(oldScope -> {
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

                    return RxJava2Adapter.monoToSingle(update_migrated(domain, scopeToUpdate, oldScope, principal));
                }).apply(y)))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Scope>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a scope", ex)));
                }).apply(err)));
    }

    
private Mono<Scope> update_migrated(String domain, Scope toUpdate, Scope oldValue, User principal) {

        toUpdate.setUpdatedAt(new Date());
        return this.validateIconUri_migrated(toUpdate).flatMap(scopeRepository::update_migrated).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(scope1 -> {
                    // create event for sync process
                    Event event = new Event(Type.SCOPE, new Payload(scope1.getId(), ReferenceType.DOMAIN, scope1.getDomain(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(scope1)));
                }).apply(v)))).doOnSuccess(scope1 -> auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_UPDATED).oldValue(oldValue).scope(scope1))).doOnError(throwable -> auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_UPDATED).throwable(throwable)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(domain, id, updateScope))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Scope> update(String domain, String id, UpdateSystemScope updateScope) {
 return RxJava2Adapter.monoToSingle(update_migrated(domain, id, updateScope));
}
@Override
    public Mono<Scope> update_migrated(String domain, String id, UpdateSystemScope updateScope) {
        LOGGER.debug("Update a system scope {} for domain {}", id, domain);
        return scopeRepository.findById_migrated(id).switchIfEmpty(Mono.error(new ScopeNotFoundException(id))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(scope -> {
                    scope.setName(updateScope.getName());
                    scope.setDescription(updateScope.getDescription());
                    scope.setUpdatedAt(new Date());
                    scope.setSystem(true);
                    scope.setClaims(updateScope.getClaims());
                    scope.setExpiresIn(updateScope.getExpiresIn());
                    scope.setDiscovery(updateScope.isDiscovery());
                    return RxJava2Adapter.monoToSingle(scopeRepository.update_migrated(scope));
                }).apply(y)))).flatMap(v->RxJava2Adapter.singleToMono((Single<Scope>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Scope, Single<Scope>>)scope -> {
                    // create event for sync process
                    Event event = new Event(Type.SCOPE, new Payload(scope.getId(), ReferenceType.DOMAIN, scope.getDomain(), Action.UPDATE));
                    return RxJava2Adapter.monoToSingle(eventService.create_migrated(event).flatMap(__->Mono.just(scope)));
                }).apply(v))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Scope>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to update a system scope", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to update a system scope", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(scopeId, force, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete(String scopeId, boolean force, User principal) {
 return RxJava2Adapter.monoToCompletable(delete_migrated(scopeId, force, principal));
}
@Override
    public Mono<Void> delete_migrated(String scopeId, boolean force, User principal) {
        LOGGER.debug("Delete scope {}", scopeId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(scopeRepository.findById_migrated(scopeId).switchIfEmpty(Mono.error(new ScopeNotFoundException(scopeId))).flatMap(y->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<Scope, SingleSource<Scope>>toJdkFunction(scope -> {
                    if (scope.isSystem() && !force) {
                        throw new SystemScopeDeleteException(scopeId);
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(scope));
                }).apply(y)))).flatMap(scope->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(Completable.fromSingle(RxJava2Adapter.monoToSingle(roleService.findByDomain_migrated(scope.getDomain())).flatMapObservable((Set<Role> roles)->RxJava2Adapter.fluxToObservable(Flux.fromIterable(roles.stream().filter((Role role)->role.getOauthScopes() != null && role.getOauthScopes().contains(scope.getKey())).collect(Collectors.toList())))).flatMapSingle((Role role)->{
role.getOauthScopes().remove(scope.getKey());
UpdateRole updatedRole = new UpdateRole();
updatedRole.setName(role.getName());
updatedRole.setDescription(role.getDescription());
updatedRole.setPermissions(role.getOauthScopes());
return roleService.update(scope.getDomain(), role.getId(), updatedRole);
}).toList())).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationService.findByDomain_migrated(scope.getDomain())).flatMapObservable((Set<Application> applications)->RxJava2Adapter.fluxToObservable(Flux.fromIterable(applications.stream().filter((Application application)->{
if (application.getSettings() == null) {
return false;
}
if (application.getSettings().getOauth() == null) {
return false;
}
ApplicationOAuthSettings oAuthSettings = application.getSettings().getOauth();
return oAuthSettings.getScopeSettings() != null && !oAuthSettings.getScopeSettings().stream().filter((ApplicationScopeSettings s)->s.getScope().equals(scope.getKey())).findFirst().isEmpty();
}).collect(Collectors.toList())))).flatMapSingle((Application application)->{
final List<ApplicationScopeSettings> cleanScopes = application.getSettings().getOauth().getScopeSettings().stream().filter((ApplicationScopeSettings s)->!s.getScope().equals(scope.getKey())).collect(Collectors.toList());
application.getSettings().getOauth().setScopeSettings(cleanScopes);
return RxJava2Adapter.monoToSingle(applicationService.update_migrated(application));
}).toList()))).toCompletable()).then(RxJava2Adapter.completableToMono(scopeApprovalRepository.deleteByDomainAndScopeKey(scope.getDomain(), scope.getKey()))).then(scopeRepository.delete_migrated(scopeId)).then(RxJava2Adapter.completableToMono(Completable.fromSingle(RxJava2Adapter.monoToSingle(eventService.create_migrated(new Event(Type.SCOPE, new Payload(scope.getId(), ReferenceType.DOMAIN, scope.getDomain(), Action.DELETE)))))))).doOnComplete(()->auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_DELETED).scope(scope)))).doOnError((Throwable throwable)->auditService.report(AuditBuilder.builder(ScopeAuditBuilder.class).principal(principal).type(EventType.SCOPE_DELETED).throwable(throwable)))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to delete scope: {}", scopeId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete scope: %s", scopeId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Scope>> findByDomain(String domain, int page, int size) {
 return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
}
@Override
    public Mono<Page<Scope>> findByDomain_migrated(String domain, int page, int size) {
        LOGGER.debug("Find scopes by domain: {}", domain);
        return scopeRepository.findByDomain_migrated(domain, page, size).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<Page<Scope>>>toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find scopes by domain: {}", domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find scopes by domain: %s", domain), ex)));
                }).apply(err)));
    }

    
@Override
    public Mono<Scope> findByDomainAndKey_migrated(String domain, String scopeKey) {
        LOGGER.debug("Find scopes by domain: {} and scope key: {}", domain, scopeKey);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(scopeRepository.findByDomainAndKey_migrated(domain, scopeKey))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find scopes by domain: {} and scope key: {}", domain, scopeKey, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find scopes by domain: %s and scope key: %s", domain, scopeKey), ex)));
                }));
    }

    
@Override
    public Mono<List<Scope>> findByDomainAndKeys_migrated(String domain, List<String> scopeKeys) {
        LOGGER.debug("Find scopes by domain: {} and scope keys: {}", domain, scopeKeys);
        if(scopeKeys==null || scopeKeys.isEmpty()) {
            return Mono.just(Collections.emptyList());
        }
        return scopeRepository.findByDomainAndKeys_migrated(domain, scopeKeys).collectList().onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<List<Scope>>>toJdkFunction(ex -> {
                    String keys = scopeKeys!=null?String.join(",",scopeKeys):null;
                    LOGGER.error("An error occurs while trying to find scopes by domain: {} and scope keys: {}", domain, keys, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find scopes by domain: %s and scope keys: %s", domain, keys), ex)));
                }).apply(err)));
    }

    /**
     * Throw InvalidClientMetadataException if null or empty, or contains unknown scope.
     * @param scopes Array of scope to validate.
     */
    
@Override
    public Mono<Boolean> validateScope_migrated(String domain, List<String> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return Mono.just(true);//nothing to do...
        }

        return findByDomain_migrated(domain, 0, Integer.MAX_VALUE).map(RxJavaReactorMigrationUtil.toJdkFunction(domainSet -> domainSet.getData().stream().map(Scope::getKey).collect(Collectors.toSet()))).flatMap(domainScopes->this.validateScope_migrated(domainScopes, scopes));
    }

    
private Mono<Boolean> validateScope_migrated(Set<String> domainScopes, List<String> scopes) {

        for (String scope : scopes) {
            if (!domainScopes.contains(scope)) {
                return Mono.error(new InvalidClientMetadataException("scope " + scope + " is not valid."));
            }
        }

        return Mono.just(true);
    }

    
private Mono<Scope> validateIconUri_migrated(Scope scope) {
        if(scope.getIconUri()!=null) {
            try {
                URI.create(scope.getIconUri()).toURL();
            } catch (MalformedURLException | IllegalArgumentException e) {
                return Mono.error(new MalformedIconUriException(scope.getIconUri()));
            }
        }
        return Mono.just(scope);
    }
}

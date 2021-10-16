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
package io.gravitee.am.management.service.impl.upgrades;

import static io.gravitee.am.management.service.impl.upgrades.UpgraderOrder.SCOPE_UPGRADER;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.service.*;
import io.gravitee.am.service.model.NewScope;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ScopeUpgrader implements Upgrader, Ordered {

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ScopeUpgrader.class);

    @Autowired
    private DomainService domainService;

    @Autowired
    private ScopeService scopeService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private RoleService roleService;

    @Override
    public boolean upgrade() {
        logger.info("Applying scope upgrade");
        RxJava2Adapter.monoToSingle(domainService.findAll_migrated())
                .flatMapObservable(Observable::fromIterable)
                .flatMapSingle((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.monoToSingle(upgradeDomain_migrated(ident)))
                .subscribe();
        return true;
    }

    
private Mono<List<Scope>> upgradeDomain_migrated(Domain domain) {
        logger.info("Looking for scopes for domain id[{}] name[{}]", domain.getId(), domain.getName());
        return scopeService.findByDomain_migrated(domain.getId(), 0, Integer.MAX_VALUE).flatMap(v->RxJava2Adapter.singleToMono((Single<List<Scope>>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Page<Scope>, Single<List<Scope>>>)scopes -> {
                    if (scopes.getData().isEmpty()) {
                        logger.info("No scope found for domain id[{}] name[{}]. Upgrading...", domain.getId(), domain.getName());
                        return RxJava2Adapter.monoToSingle(createAppScopes_migrated(domain).flatMap(irrelevant->createRoleScopes_migrated(domain)));
                    }
                    logger.info("No scope to update, skip upgrade");
                    return RxJava2Adapter.monoToSingle(Mono.just(new ArrayList<>(scopes.getData())));
                }).apply(v)));
    }

    
private Mono<List<Scope>> createAppScopes_migrated(Domain domain) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToObservable(RxJava2Adapter.observableToFlux(RxJava2Adapter.monoToMaybe(applicationService.findByDomain_migrated(domain.getId()).filter(RxJavaReactorMigrationUtil.toJdkPredicate(applications -> applications != null)))
                .flatMapObservable(Observable::fromIterable), BackpressureStrategy.BUFFER).filter(RxJavaReactorMigrationUtil.toJdkPredicate(app -> app.getSettings() != null && app.getSettings().getOauth() != null)).flatMap(z->RxJava2Adapter.observableToFlux(Observable.wrap(RxJavaReactorMigrationUtil.<Application, ObservableSource<String>>toJdkFunction(app -> Observable.fromIterable(app.getSettings().getOauth().getScopes())).apply(z)), BackpressureStrategy.BUFFER)))
                .flatMapSingle(scope -> RxJava2Adapter.monoToSingle(createScope_migrated(domain.getId(), scope)))
                .toList());
    }

    
private Mono<List<Scope>> createRoleScopes_migrated(Domain domain) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToObservable(RxJava2Adapter.observableToFlux(RxJava2Adapter.monoToMaybe(roleService.findByDomain_migrated(domain.getId()).filter(RxJavaReactorMigrationUtil.toJdkPredicate(roles -> roles != null)))
                .flatMapObservable(Observable::fromIterable), BackpressureStrategy.BUFFER).filter(RxJavaReactorMigrationUtil.toJdkPredicate(role -> role.getOauthScopes() != null)).flatMap(z->RxJava2Adapter.observableToFlux(Observable.wrap(RxJavaReactorMigrationUtil.<Role, ObservableSource<String>>toJdkFunction(role -> Observable.fromIterable(role.getOauthScopes())).apply(z)), BackpressureStrategy.BUFFER)))
                .flatMapSingle(scope -> RxJava2Adapter.monoToSingle(createScope_migrated(domain.getId(), scope)))
                .toList());
    }

    
private Mono<Scope> createScope_migrated(String domain, String scopeKey) {
        return scopeService.findByDomain_migrated(domain, 0, Integer.MAX_VALUE).flatMap(v->RxJava2Adapter.singleToMono((Single<Scope>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Page<Scope>, Single<Scope>>)scopes -> {
                    Optional<Scope> optScope = scopes.getData().stream().filter(scope -> scope.getKey().equalsIgnoreCase(scopeKey)).findFirst();
                    if (!optScope.isPresent()) {
                        logger.info("Create a new scope key[{}] for domain[{}]", scopeKey, domain);
                        NewScope scope = new NewScope();
                        scope.setKey(scopeKey);
                        scope.setName(Character.toUpperCase(scopeKey.charAt(0)) + scopeKey.substring(1));
                        scope.setDescription("Default description for scope " + scopeKey);
                        return RxJava2Adapter.monoToSingle(scopeService.create_migrated(domain, scope));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(optScope.get()));
                }).apply(v)));
    }

    @Override
    public int getOrder() {
        return SCOPE_UPGRADER;
    }
}

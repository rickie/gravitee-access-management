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

import static io.gravitee.am.management.service.impl.upgrades.UpgraderOrder.DOMAIN_IDP_UPGRADER;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.management.service.IdentityProviderManager;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.IdentityProvider;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.IdentityProviderService;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * Create default mongo IDP for each domain for user management
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class DomainIdpUpgrader implements Upgrader, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(DomainIdpUpgrader.class);
    private static final String DEFAULT_IDP_PREFIX = "default-idp-";

    @Autowired
    private DomainService domainService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Autowired
    private IdentityProviderManager identityProviderManager;

    @Override
    public boolean upgrade() {
        logger.info("Applying domain idp upgrade");
        RxJava2Adapter.monoToSingle(domainService.findAll_migrated())
                .flatMapObservable(Observable::fromIterable)
                .flatMapSingle((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.monoToSingle(updateDefaultIdp_migrated(ident)))
                .subscribe();
        return true;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.updateDefaultIdp_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<IdentityProvider> updateDefaultIdp(Domain domain) {
 return RxJava2Adapter.monoToSingle(updateDefaultIdp_migrated(domain));
}
private Mono<IdentityProvider> updateDefaultIdp_migrated(Domain domain) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(identityProviderService.findById_migrated(DEFAULT_IDP_PREFIX + domain.getId()))).hasElement().flatMap(v->RxJava2Adapter.singleToMono((Single<IdentityProvider>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Boolean, Single<IdentityProvider>>)isEmpty -> {
                    if (isEmpty) {
                        logger.info("No default idp found for domain {}, update domain", domain.getName());
                        return RxJava2Adapter.monoToSingle(identityProviderManager.create_migrated(domain.getId()));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.just(new IdentityProvider()));
                }).apply(v)));
    }

    @Override
    public int getOrder() {
        return DOMAIN_IDP_UPGRADER;
    }

}

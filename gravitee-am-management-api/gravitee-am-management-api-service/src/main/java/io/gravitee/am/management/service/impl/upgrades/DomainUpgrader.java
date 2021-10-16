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

import static io.gravitee.am.management.service.impl.upgrades.UpgraderOrder.DOMAIN_UPGRADER;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Domain;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.model.PatchDomain;
import io.gravitee.am.service.model.openid.PatchClientRegistrationSettings;
import io.gravitee.am.service.model.openid.PatchOIDCSettings;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@Component
public class DomainUpgrader implements Upgrader, Ordered {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DomainUpgrader.class);

    @Autowired
    private DomainService domainService;

    @Override
    public boolean upgrade() {
        LOGGER.info("Applying domain upgrade");
        RxJava2Adapter.monoToSingle(domainService.findAll_migrated())
                .flatMapObservable(Observable::fromIterable)
                .flatMapSingle((io.gravitee.am.model.Domain ident) -> RxJava2Adapter.monoToSingle(upgradeDomain_migrated(ident)))
                .toList()
                .subscribe();
        return true;

    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.upgradeDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Domain> upgradeDomain(Domain domain) {
 return RxJava2Adapter.monoToSingle(upgradeDomain_migrated(domain));
}
private Mono<Domain> upgradeDomain_migrated(Domain domain) {
        if(domain.getOidc()!=null) {
            return Mono.just(domain);
        }

        PatchClientRegistrationSettings clientRegistrationPatch = new PatchClientRegistrationSettings();
        clientRegistrationPatch.setDynamicClientRegistrationEnabled(Optional.of(false));
        clientRegistrationPatch.setOpenDynamicClientRegistrationEnabled(Optional.of(false));
        clientRegistrationPatch.setAllowHttpSchemeRedirectUri(Optional.of(true));
        clientRegistrationPatch.setAllowLocalhostRedirectUri(Optional.of(true));
        clientRegistrationPatch.setAllowWildCardRedirectUri(Optional.of(true));

        PatchOIDCSettings oidcPatch = new PatchOIDCSettings();
        oidcPatch.setClientRegistrationSettings(Optional.of(clientRegistrationPatch));

        PatchDomain patchDomain = new PatchDomain();
        patchDomain.setOidc(Optional.of(oidcPatch));

        return RxJava2Adapter.singleToMono(domainService.patch(domain.getId(),patchDomain));
    }

    @Override
    public int getOrder() {
        return DOMAIN_UPGRADER;
    }
}

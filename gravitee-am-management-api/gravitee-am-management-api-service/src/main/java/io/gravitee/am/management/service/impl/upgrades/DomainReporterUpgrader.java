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

import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Reporter;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.ReporterService;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * Create default mongo Reporter for each domain for audit logs
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class DomainReporterUpgrader implements Upgrader, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(DomainReporterUpgrader.class);

    @Autowired
    private DomainService domainService;

    @Autowired
    private ReporterService reporterService;

    @Override
    public boolean upgrade() {
        logger.info("Applying domain reporter upgrade");
        domainService.findAll()
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(this::updateDefaultReporter)
                .subscribe();
        return true;
    }

    private Completable updateDefaultReporter(Domain domain) {
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.singleToMono(reporterService.findByDomain(domain.getId())
                .toList()).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Reporter>, CompletableSource>)reporters -> {
                    if (reporters == null || reporters.isEmpty()) {
                        logger.info("No default reporter found for domain {}, update domain", domain.getName());
                        return reporterService.createDefault(domain.getId())
                                .ignoreElement();
                    }
                    return Completable.complete();
                }).apply(y)))).then());
    }
    @Override
    public int getOrder() {
        return UpgraderOrder.DOMAIN_REPORTER_UPGRADER;
    }

}

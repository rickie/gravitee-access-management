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
package io.gravitee.am.management.service.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.analytics.Type;
import io.gravitee.am.management.service.AuditReporterManager;
import io.gravitee.am.management.service.AuditService;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.reporter.api.audit.AuditReportableCriteria;
import io.gravitee.am.reporter.api.audit.model.Audit;
import io.gravitee.am.reporter.api.provider.Reporter;
import io.gravitee.am.service.exception.AuditNotFoundException;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component("managementAuditService")
public class AuditServiceImpl implements AuditService {

    public static final Logger logger = LoggerFactory.getLogger(AuditServiceImpl.class);

    @Autowired
    private AuditReporterManager auditReporterManager;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, criteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Audit>> search(ReferenceType referenceType, String referenceId, AuditReportableCriteria criteria, int page, int size) {
 return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, criteria, page, size));
}
@Override
    public Mono<Page<Audit>> search_migrated(ReferenceType referenceType, String referenceId, AuditReportableCriteria criteria, int page, int size) {
        try {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getReporter(referenceType, referenceId).search_migrated(referenceType, referenceId, criteria, page, size)));
        } catch (Exception ex) {
            logger.error("An error occurs during audits search for {}}: {}", referenceType, referenceId, ex);
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(ex)));
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(domain, criteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Audit>> search(String domain, AuditReportableCriteria criteria, int page, int size) {
 return RxJava2Adapter.monoToSingle(search_migrated(domain, criteria, page, size));
}
@Override
    public Mono<Page<Audit>> search_migrated(String domain, AuditReportableCriteria criteria, int page, int size) {

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(search_migrated(ReferenceType.DOMAIN, domain, criteria, page, size)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.aggregate_migrated(domain, criteria, analyticsType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Map<Object, Object>> aggregate(String domain, AuditReportableCriteria criteria, Type analyticsType) {
 return RxJava2Adapter.monoToSingle(aggregate_migrated(domain, criteria, analyticsType));
}
@Override
    public Mono<Map<Object,Object>> aggregate_migrated(String domain, AuditReportableCriteria criteria, Type analyticsType) {
        try {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(getReporter(domain).aggregate_migrated(ReferenceType.DOMAIN, domain, criteria, analyticsType)));
        } catch (Exception ex) {
            logger.error("An error occurs during audits aggregation for domain: {}", domain, ex);
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(ex)));
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, auditId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Audit> findById(ReferenceType referenceType, String referenceId, String auditId) {
 return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, auditId));
}
@Override
    public Mono<Audit> findById_migrated(ReferenceType referenceType, String referenceId, String auditId) {
        try {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(getReporter(referenceType, referenceId).findById_migrated(referenceType, referenceId, auditId))).switchIfEmpty(Mono.error(new AuditNotFoundException(auditId)))));
        } catch (Exception ex) {
            logger.error("An error occurs while trying to find audit by id: {} and for the {}}: {}", auditId, referenceType, referenceId, ex);
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(ex)));
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(domain, auditId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Audit> findById(String domain, String auditId) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(domain, auditId));
}
@Override
    public Mono<Audit> findById_migrated(String domain, String auditId) {
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(findById_migrated(ReferenceType.DOMAIN, domain, auditId)))));
    }

    private Reporter getReporter(String domain) {
        return auditReporterManager.getReporter(domain);
    }


    private Reporter getReporter(ReferenceType referenceType, String referenceId) {
        return auditReporterManager.getReporter(referenceType, referenceId);
    }
}

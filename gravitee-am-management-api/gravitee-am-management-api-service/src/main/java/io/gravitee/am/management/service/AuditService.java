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
package io.gravitee.am.management.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.analytics.Type;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.reporter.api.audit.AuditReportableCriteria;
import io.gravitee.am.reporter.api.audit.model.Audit;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AuditService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, criteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<Audit>> search(ReferenceType referenceType, String referenceId, AuditReportableCriteria criteria, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, criteria, page, size));
}
default Mono<Page<Audit>> search_migrated(ReferenceType referenceType, String referenceId, AuditReportableCriteria criteria, int page, int size) {
    return RxJava2Adapter.singleToMono(search(referenceType, referenceId, criteria, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(domain, criteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Page<Audit>> search(String domain, AuditReportableCriteria criteria, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(domain, criteria, page, size));
}
default Mono<Page<Audit>> search_migrated(String domain, AuditReportableCriteria criteria, int page, int size) {
    return RxJava2Adapter.singleToMono(search(domain, criteria, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.aggregate_migrated(domain, criteria, analyticsType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Map<Object, Object>> aggregate(String domain, AuditReportableCriteria criteria, io.gravitee.am.common.analytics.Type analyticsType) {
    return RxJava2Adapter.monoToSingle(aggregate_migrated(domain, criteria, analyticsType));
}
default Mono<Map<Object, Object>> aggregate_migrated(String domain, AuditReportableCriteria criteria, Type analyticsType) {
    return RxJava2Adapter.singleToMono(aggregate(domain, criteria, analyticsType));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findById_migrated(referenceType, referenceId, auditId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Audit> findById(ReferenceType referenceType, String referenceId, String auditId) {
    return RxJava2Adapter.monoToSingle(findById_migrated(referenceType, referenceId, auditId));
}
default Mono<Audit> findById_migrated(ReferenceType referenceType, String referenceId, String auditId) {
    return RxJava2Adapter.singleToMono(findById(referenceType, referenceId, auditId));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(domain, auditId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<Audit> findById(String domain, String auditId) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(domain, auditId));
}
default Mono<Audit> findById_migrated(String domain, String auditId) {
    return RxJava2Adapter.maybeToMono(findById(domain, auditId));
}
}

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
package io.gravitee.am.reporter.jdbc.dialect;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.reporter.api.audit.AuditReportableCriteria;
import io.reactivex.Single;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.data.r2dbc.core.DatabaseClient;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface DialectHelper {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.buildAndProcessHistogram_migrated(dbClient, referenceType, referenceId, criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<java.util.Map<java.lang.String, java.lang.Object>>> buildAndProcessHistogram(org.springframework.data.r2dbc.core.DatabaseClient dbClient, io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, io.gravitee.am.reporter.api.audit.AuditReportableCriteria criteria) {
    return RxJava2Adapter.monoToSingle(buildAndProcessHistogram_migrated(dbClient, referenceType, referenceId, criteria));
}
default reactor.core.publisher.Mono<java.util.List<java.util.Map<java.lang.String, java.lang.Object>>> buildAndProcessHistogram_migrated(DatabaseClient dbClient, ReferenceType referenceType, String referenceId, AuditReportableCriteria criteria) {
    return RxJava2Adapter.singleToMono(buildAndProcessHistogram(dbClient, referenceType, referenceId, criteria));
}

    SearchQuery buildHistogramQuery(ReferenceType referenceType, String referenceId, AuditReportableCriteria criteria);

    SearchQuery buildGroupByQuery(ReferenceType referenceType, String referenceId, AuditReportableCriteria criteria);

    SearchQuery buildSearchQuery(ReferenceType referenceType, String referenceId, AuditReportableCriteria criteria);

    String tableExists(String table);

    String buildPagingClause(int page, int size);

    void setAuditsTable(String auditsTable);
    void setAuditAccessPointsTable(String auditAccessPointsTable);
    void setAuditOutcomesTable(String auditOutcomesTable);
    void setAuditEntitiesTable(String auditEntitiesTable);
}

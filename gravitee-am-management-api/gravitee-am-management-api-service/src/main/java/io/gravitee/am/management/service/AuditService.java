/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.management.service;

import io.gravitee.am.common.analytics.Type;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.reporter.api.audit.AuditReportableCriteria;
import io.gravitee.am.reporter.api.audit.model.Audit;
import io.reactivex.Maybe;
import io.reactivex.Single;

import java.util.Map;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AuditService {

    Single<Page<Audit>> search(
            ReferenceType referenceType,
            String referenceId,
            AuditReportableCriteria criteria,
            int page,
            int size);

    Single<Page<Audit>> search(String domain, AuditReportableCriteria criteria, int page, int size);

    Single<Map<Object, Object>> aggregate(
            String domain, AuditReportableCriteria criteria, Type analyticsType);

    Single<Audit> findById(ReferenceType referenceType, String referenceId, String auditId);

    Maybe<Audit> findById(String domain, String auditId);
}

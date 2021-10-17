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


import io.gravitee.am.common.analytics.Field;
import io.gravitee.am.common.audit.Status;
import io.gravitee.am.management.service.ApplicationAnalyticsService;
import io.gravitee.am.management.service.AuditService;
import io.gravitee.am.model.analytics.*;
import io.gravitee.am.reporter.api.audit.AuditReportableCriteria;
import io.gravitee.am.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

@Component
public class ApplicationAnalyticsServiceImpl implements ApplicationAnalyticsService {

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserService userService;

    
@Override
    public Mono<AnalyticsResponse> execute_migrated(AnalyticsQuery query) {
        switch (query.getType()) {
            case DATE_HISTO:
                return executeDateHistogram_migrated(query);
            case GROUP_BY:
                return executeGroupBy_migrated(query);
            case COUNT:
                return executeCount_migrated(query);
        }
        return Mono.just(new AnalyticsResponse() {});
    }

    
private Mono<AnalyticsResponse> executeGroupBy_migrated(AnalyticsQuery query) {
        AuditReportableCriteria.Builder queryBuilder = new AuditReportableCriteria.Builder()
                .types(Collections.singletonList(query.getField().toUpperCase()));
        queryBuilder.from(query.getFrom());
        queryBuilder.to(query.getTo());
        queryBuilder.size(query.getSize());
        queryBuilder.accessPointId(query.getApplication());

        switch (query.getField()) {
            case Field.USER_STATUS:
                return userService.statistics_migrated(query).map(RxJavaReactorMigrationUtil.toJdkFunction(AnalyticsGroupByResponse::new));
            default :
                return Mono.just(new AnalyticsResponse() {});
        }
    }


    
private Mono<AnalyticsResponse> executeCount_migrated(AnalyticsQuery query) {
        AuditReportableCriteria.Builder queryBuilder = new AuditReportableCriteria.Builder()
                .types(Collections.singletonList(query.getField().toUpperCase()));
        queryBuilder.from(query.getFrom());
        queryBuilder.to(query.getTo());
        queryBuilder.status(Status.SUCCESS);
        queryBuilder.accessPointId(query.getApplication());

        switch (query.getField()) {
            case Field.USER:
                return userService.countByApplication_migrated(query.getDomain(), query.getApplication()).map(RxJavaReactorMigrationUtil.toJdkFunction(AnalyticsCountResponse::new));
            default:
                return auditService.aggregate_migrated(query.getDomain(), queryBuilder.build(), query.getType()).map(RxJavaReactorMigrationUtil.toJdkFunction(values -> new AnalyticsCountResponse((Long) values.values().iterator().next())));
        }
    }

    
private Mono<AnalyticsResponse> executeDateHistogram_migrated(AnalyticsQuery query) {
        AuditReportableCriteria.Builder queryBuilder = new AuditReportableCriteria.Builder()
                .types(Collections.singletonList(query.getField().toUpperCase()));
        queryBuilder.from(query.getFrom());
        queryBuilder.to(query.getTo());
        queryBuilder.interval(query.getInterval());
        queryBuilder.accessPointId(query.getApplication());

        return auditService.aggregate_migrated(query.getDomain(), queryBuilder.build(), query.getType()).map(RxJavaReactorMigrationUtil.toJdkFunction(values -> {
                    Timestamp timestamp = new Timestamp(query.getFrom(), query.getTo(), query.getInterval());
                    List<Bucket> buckets = values
                            .entrySet()
                            .stream()
                            .map(entry -> {
                                Bucket bucket = new Bucket();
                                bucket.setName((String) entry.getKey());
                                bucket.setField(query.getField());
                                bucket.setData((List<Long>) entry.getValue());
                                return bucket;
                            })
                            .collect(Collectors.toList());
                    AnalyticsHistogramResponse analyticsHistogramResponse = new AnalyticsHistogramResponse();
                    analyticsHistogramResponse.setTimestamp(timestamp);
                    analyticsHistogramResponse.setValues(buckets);
                    return analyticsHistogramResponse;
                }));
    }

}

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
import io.gravitee.am.common.analytics.Field;
import io.gravitee.am.common.analytics.Type;
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.common.audit.Status;
import io.gravitee.am.management.service.AnalyticsService;
import io.gravitee.am.management.service.AuditService;
import io.gravitee.am.model.analytics.*;
import io.gravitee.am.reporter.api.audit.AuditReportableCriteria;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.UserService;
import io.reactivex.Observable;
import io.reactivex.Single;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private AuditService auditService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserService userService;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.execute_migrated(query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<AnalyticsResponse> execute(AnalyticsQuery query) {
 return RxJava2Adapter.monoToSingle(execute_migrated(query));
}
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

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.executeDateHistogram_migrated(query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<AnalyticsResponse> executeDateHistogram(AnalyticsQuery query) {
 return RxJava2Adapter.monoToSingle(executeDateHistogram_migrated(query));
}
private Mono<AnalyticsResponse> executeDateHistogram_migrated(AnalyticsQuery query) {
        AuditReportableCriteria.Builder queryBuilder = new AuditReportableCriteria.Builder()
                .types(Collections.singletonList(query.getField().toUpperCase()));
        queryBuilder.from(query.getFrom());
        queryBuilder.to(query.getTo());
        queryBuilder.interval(query.getInterval());
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(auditService.aggregate_migrated(query.getDomain(), queryBuilder.build(), query.getType()))).map(RxJavaReactorMigrationUtil.toJdkFunction(values -> {
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

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.executeGroupBy_migrated(query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<AnalyticsResponse> executeGroupBy(AnalyticsQuery query) {
 return RxJava2Adapter.monoToSingle(executeGroupBy_migrated(query));
}
private Mono<AnalyticsResponse> executeGroupBy_migrated(AnalyticsQuery query) {
        AuditReportableCriteria.Builder queryBuilder = new AuditReportableCriteria.Builder()
                .types(Collections.singletonList(query.getField().toUpperCase()));
        queryBuilder.from(query.getFrom());
        queryBuilder.to(query.getTo());
        queryBuilder.size(query.getSize());

        switch (query.getField()) {
            case Field.APPLICATION:
                // applications are group by login attempts
                queryBuilder.types(Collections.singletonList(EventType.USER_LOGIN));
                queryBuilder.status(Status.SUCCESS);
                queryBuilder.field("accessPoint.id");
                return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(executeGroupBy_migrated(query.getDomain(), queryBuilder.build(), query.getType()))).flatMap(analyticsResponse->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(fetchMetadata_migrated((AnalyticsGroupByResponse)analyticsResponse))));
            case Field.USER_STATUS:
            case Field.USER_REGISTRATION:
                return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.statistics_migrated(query))).map(RxJavaReactorMigrationUtil.toJdkFunction(value -> new AnalyticsGroupByResponse(value)));
            default :
                return executeGroupBy_migrated(query.getDomain(), queryBuilder.build(), query.getType());
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.fetchMetadata_migrated(analyticsGroupByResponse))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<AnalyticsResponse> fetchMetadata(AnalyticsGroupByResponse analyticsGroupByResponse) {
 return RxJava2Adapter.monoToSingle(fetchMetadata_migrated(analyticsGroupByResponse));
}
private Mono<AnalyticsResponse> fetchMetadata_migrated(AnalyticsGroupByResponse analyticsGroupByResponse) {
        Map<Object, Object> values = analyticsGroupByResponse.getValues();
        if (values == null && values.isEmpty()) {
            return Mono.just(analyticsGroupByResponse);
        }
        return RxJava2Adapter.singleToMono(Observable.fromIterable(values.keySet())
                .flatMapMaybe(appId -> RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(applicationService.findById_migrated((String) appId))).map(RxJavaReactorMigrationUtil.toJdkFunction(application -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("name", application.getName());
                            data.put("domain", application.getDomain());
                            return Collections.singletonMap((String) appId, data);
                        })).defaultIfEmpty(Collections.singletonMap((String) appId, getGenericMetadata("Deleted application", true)))))
                .toList()).map(RxJavaReactorMigrationUtil.toJdkFunction(result -> {
                    Map<String, Map<String, Object>> metadata = result.stream()
                            .flatMap(m -> m.entrySet().stream())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                    analyticsGroupByResponse.setMetadata(metadata);
                    return analyticsGroupByResponse;
                }));

    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.executeCount_migrated(query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<AnalyticsResponse> executeCount(AnalyticsQuery query) {
 return RxJava2Adapter.monoToSingle(executeCount_migrated(query));
}
private Mono<AnalyticsResponse> executeCount_migrated(AnalyticsQuery query) {
        AuditReportableCriteria.Builder queryBuilder = new AuditReportableCriteria.Builder()
                .types(Collections.singletonList(query.getField().toUpperCase()));
        queryBuilder.from(query.getFrom());
        queryBuilder.to(query.getTo());
        queryBuilder.status(Status.SUCCESS);

        switch (query.getField()) {
            case Field.APPLICATION:
                return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationService.countByDomain_migrated(query.getDomain()))).map(RxJavaReactorMigrationUtil.toJdkFunction(value -> new AnalyticsCountResponse(value)));
            case Field.USER:
                return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.countByDomain_migrated(query.getDomain()))).map(RxJavaReactorMigrationUtil.toJdkFunction(value -> new AnalyticsCountResponse(value)));
            default :
                return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(auditService.aggregate_migrated(query.getDomain(), queryBuilder.build(), query.getType()))).map(RxJavaReactorMigrationUtil.toJdkFunction(values -> values.values().isEmpty() ? new AnalyticsCountResponse(0l) : new AnalyticsCountResponse((Long) values.values().iterator().next())));
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.executeGroupBy_migrated(domain, criteria, type))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<AnalyticsResponse> executeGroupBy(String domain, AuditReportableCriteria criteria, Type type) {
 return RxJava2Adapter.monoToSingle(executeGroupBy_migrated(domain, criteria, type));
}
private Mono<AnalyticsResponse> executeGroupBy_migrated(String domain, AuditReportableCriteria criteria, Type type) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(auditService.aggregate_migrated(domain, criteria, type))).map(RxJavaReactorMigrationUtil.toJdkFunction(values -> new AnalyticsGroupByResponse(values)));
    }

    private Map<String, Object> getGenericMetadata(String value, boolean deleted) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", value);
        if (deleted) {
            metadata.put("deleted", true);
        }
        return metadata;
    }
}

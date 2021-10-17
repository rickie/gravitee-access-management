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
import io.gravitee.am.model.analytics.AnalyticsQuery;
import io.gravitee.am.model.analytics.AnalyticsResponse;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

public interface ApplicationAnalyticsService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.execute_migrated(query))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<AnalyticsResponse> execute(AnalyticsQuery query) {
    return RxJava2Adapter.monoToSingle(execute_migrated(query));
}
default Mono<AnalyticsResponse> execute_migrated(AnalyticsQuery query) {
    return RxJava2Adapter.singleToMono(execute(query));
}
}

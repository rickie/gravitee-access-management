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

import io.gravitee.am.model.analytics.AnalyticsQuery;
import io.gravitee.am.model.analytics.AnalyticsResponse;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface AnalyticsService {

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.analytics.AnalyticsResponse> execute(io.gravitee.am.model.analytics.AnalyticsQuery query) {
    return RxJava2Adapter.monoToSingle(execute_migrated(query));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.analytics.AnalyticsResponse> execute_migrated(AnalyticsQuery query) {
    return RxJava2Adapter.singleToMono(execute(query));
}
}

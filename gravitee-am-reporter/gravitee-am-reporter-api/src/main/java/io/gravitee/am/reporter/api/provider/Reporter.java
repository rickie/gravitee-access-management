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
package io.gravitee.am.reporter.api.provider;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.analytics.Type;
import io.gravitee.am.model.ReferenceType;

import io.gravitee.am.reporter.api.Reportable;



import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface Reporter<R extends Reportable, C extends ReportableCriteria> extends io.gravitee.reporter.api.Reporter {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.search_migrated(referenceType, referenceId, criteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.common.Page<R>> search(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, C criteria, int page, int size) {
    return RxJava2Adapter.monoToSingle(search_migrated(referenceType, referenceId, criteria, page, size));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.common.Page<R>> search_migrated(ReferenceType referenceType, String referenceId, C criteria, int page, int size) {
    return RxJava2Adapter.singleToMono(search(referenceType, referenceId, criteria, page, size));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.aggregate_migrated(referenceType, referenceId, criteria, analyticsType))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.Map<java.lang.Object, java.lang.Object>> aggregate(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, C criteria, io.gravitee.am.common.analytics.Type analyticsType) {
    return RxJava2Adapter.monoToSingle(aggregate_migrated(referenceType, referenceId, criteria, analyticsType));
}
default reactor.core.publisher.Mono<java.util.Map<java.lang.Object, java.lang.Object>> aggregate_migrated(ReferenceType referenceType, String referenceId, C criteria, Type analyticsType) {
    return RxJava2Adapter.singleToMono(aggregate(referenceType, referenceId, criteria, analyticsType));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(referenceType, referenceId, id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<R> findById(io.gravitee.am.model.ReferenceType referenceType, java.lang.String referenceId, java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(referenceType, referenceId, id));
}
default reactor.core.publisher.Mono<R> findById_migrated(ReferenceType referenceType, String referenceId, String id) {
    return RxJava2Adapter.maybeToMono(findById(referenceType, referenceId, id));
}

    boolean canSearch();
}

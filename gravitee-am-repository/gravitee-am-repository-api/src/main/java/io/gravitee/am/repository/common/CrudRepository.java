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
package io.gravitee.am.repository.common;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CrudRepository<T, ID> {

      @Deprecated  
default io.reactivex.Maybe<T> findById(ID id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<T> findById_migrated(ID id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @Deprecated  
default io.reactivex.Single<T> create(T item) {
    return RxJava2Adapter.monoToSingle(create_migrated(item));
}
default reactor.core.publisher.Mono<T> create_migrated(T item) {
    return RxJava2Adapter.singleToMono(create(item));
}

      @Deprecated  
default io.reactivex.Single<T> update(T item) {
    return RxJava2Adapter.monoToSingle(update_migrated(item));
}
default reactor.core.publisher.Mono<T> update_migrated(T item) {
    return RxJava2Adapter.singleToMono(update(item));
}

      @Deprecated  
default io.reactivex.Completable delete(ID id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated(ID id) {
    return RxJava2Adapter.completableToMono(delete(id));
}
}
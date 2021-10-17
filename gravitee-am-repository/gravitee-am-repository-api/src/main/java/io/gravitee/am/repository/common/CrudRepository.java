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

import com.google.errorprone.annotations.InlineMe;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface CrudRepository<T, ID> {

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<T> findById(ID id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<T> findById_migrated(ID id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<T> create(T item) {
    return RxJava2Adapter.monoToSingle(create_migrated(item));
}
default Mono<T> create_migrated(T item) {
    return RxJava2Adapter.singleToMono(create(item));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(item))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<T> update(T item) {
    return RxJava2Adapter.monoToSingle(update_migrated(item));
}
default Mono<T> update_migrated(T item) {
    return RxJava2Adapter.singleToMono(update(item));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete(ID id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
}
default Mono<Void> delete_migrated(ID id) {
    return RxJava2Adapter.completableToMono(delete(id));
}
}
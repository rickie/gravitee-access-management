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
package io.gravitee.am.repository.jdbc.management.api;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Policy;
import io.gravitee.am.repository.jdbc.management.AbstractJdbcRepository;
import io.gravitee.am.repository.management.api.PolicyRepository;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public class JdbcPolicyRepository extends AbstractJdbcRepository implements PolicyRepository {

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<Policy> findAll() {
 return RxJava2Adapter.fluxToFlowable(findAll_migrated());
}
@Override
    public Flux<Policy> findAll_migrated() {
        return Flux.empty();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.collectionExists_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Boolean> collectionExists() {
 return RxJava2Adapter.monoToSingle(collectionExists_migrated());
}
@Override
    public Mono<Boolean> collectionExists_migrated() {
        return Mono.just(false);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteCollection_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteCollection() {
 return RxJava2Adapter.monoToCompletable(deleteCollection_migrated());
}
@Override
    public Mono<Void> deleteCollection_migrated() {
        return Mono.empty();
    }
}

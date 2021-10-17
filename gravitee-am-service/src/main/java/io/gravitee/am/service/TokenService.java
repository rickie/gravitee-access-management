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
package io.gravitee.am.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Application;
import io.gravitee.am.service.model.TotalToken;
import io.reactivex.Completable;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface TokenService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTotalTokensByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<TotalToken> findTotalTokensByDomain(String domain) {
    return RxJava2Adapter.monoToSingle(findTotalTokensByDomain_migrated(domain));
}
default Mono<TotalToken> findTotalTokensByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(findTotalTokensByDomain(domain));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTotalTokensByApplication_migrated(application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<TotalToken> findTotalTokensByApplication(Application application) {
    return RxJava2Adapter.monoToSingle(findTotalTokensByApplication_migrated(application));
}
default Mono<TotalToken> findTotalTokensByApplication_migrated(Application application) {
    return RxJava2Adapter.singleToMono(findTotalTokensByApplication(application));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTotalTokens_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<TotalToken> findTotalTokens() {
    return RxJava2Adapter.monoToSingle(findTotalTokens_migrated());
}
default Mono<TotalToken> findTotalTokens_migrated() {
    return RxJava2Adapter.singleToMono(findTotalTokens());
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByUserId_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable deleteByUserId(String userId) {
    return RxJava2Adapter.monoToCompletable(deleteByUserId_migrated(userId));
}
default Mono<Void> deleteByUserId_migrated(String userId) {
    return RxJava2Adapter.completableToMono(deleteByUserId(userId));
}
}

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

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ReCaptchaService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.isValid_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.lang.Boolean> isValid(java.lang.String token) {
    return RxJava2Adapter.monoToSingle(isValid_migrated(token));
}
default reactor.core.publisher.Mono<java.lang.Boolean> isValid_migrated(String token) {
    return RxJava2Adapter.singleToMono(isValid(token));
}

    boolean isEnabled();

    String getSiteKey();
}
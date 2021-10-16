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


import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface NewsletterService {

    /**
     * Subscribe to newsletters.
     * @param user a user with email, firstname and lastname.
     */
    void subscribe(Object user);

    /**
     * Get tag lines
     * @return tag lines
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getTaglines_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<java.util.List<java.lang.String>> getTaglines() {
    return RxJava2Adapter.monoToSingle(getTaglines_migrated());
}
default reactor.core.publisher.Mono<java.util.List<java.lang.String>> getTaglines_migrated() {
    return RxJava2Adapter.singleToMono(getTaglines());
}
}

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
import io.reactivex.Single;
import net.minidev.json.JSONObject;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Guillaume CUSNIEUX (guillaume.cusnieux at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface SpelService {
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getGrammar_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<net.minidev.json.JSONObject> getGrammar() {
    return RxJava2Adapter.monoToSingle(getGrammar_migrated());
}
default reactor.core.publisher.Mono<net.minidev.json.JSONObject> getGrammar_migrated() {
    return RxJava2Adapter.singleToMono(getGrammar());
}
}

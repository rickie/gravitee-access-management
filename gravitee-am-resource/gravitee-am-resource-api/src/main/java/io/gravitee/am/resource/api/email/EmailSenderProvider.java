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
package io.gravitee.am.resource.api.email;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.email.Email;
import io.gravitee.am.resource.api.ResourceProvider;

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface EmailSenderProvider extends ResourceProvider {
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.sendMessage_migrated(message))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable sendMessage(io.gravitee.am.common.email.Email message) {
    return RxJava2Adapter.monoToCompletable(sendMessage_migrated(message));
}
default reactor.core.publisher.Mono<java.lang.Void> sendMessage_migrated(Email message) {
    return RxJava2Adapter.completableToMono(sendMessage(message));
}
}

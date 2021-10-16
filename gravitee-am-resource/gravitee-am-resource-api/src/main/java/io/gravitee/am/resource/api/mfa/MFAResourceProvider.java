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
package io.gravitee.am.resource.api.mfa;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.resource.api.ResourceProvider;

import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface MFAResourceProvider extends ResourceProvider {
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.send_migrated(target))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable send(io.gravitee.am.resource.api.mfa.MFALink target) {
    return RxJava2Adapter.monoToCompletable(send_migrated(target));
}
default reactor.core.publisher.Mono<java.lang.Void> send_migrated(MFALink target) {
    return RxJava2Adapter.completableToMono(send(target));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.verify_migrated(challenge))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable verify(io.gravitee.am.resource.api.mfa.MFAChallenge challenge) {
    return RxJava2Adapter.monoToCompletable(verify_migrated(challenge));
}
default reactor.core.publisher.Mono<java.lang.Void> verify_migrated(MFAChallenge challenge) {
    return RxJava2Adapter.completableToMono(verify(challenge));
}
}

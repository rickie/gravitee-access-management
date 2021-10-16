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
package io.gravitee.am.resource.mock.provider;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.exception.mfa.InvalidCodeException;
import io.gravitee.am.resource.api.mfa.MFAChallenge;
import io.gravitee.am.resource.api.mfa.MFALink;
import io.gravitee.am.resource.api.mfa.MFAResourceProvider;
import io.gravitee.am.resource.mock.MFAResourceConfiguration;
import io.reactivex.Completable;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class MFAMockProvider implements MFAResourceProvider {

    @Autowired
    private MFAResourceConfiguration configuration;

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.send_migrated(target))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable send(MFALink target) {
 return RxJava2Adapter.monoToCompletable(send_migrated(target));
}
@Override
    public Mono<Void> send_migrated(MFALink target) {
        System.out.println("MFAMockProvider: SEND CODE " + configuration.getCode());
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.verify_migrated(challenge))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable verify(MFAChallenge challenge) {
 return RxJava2Adapter.monoToCompletable(verify_migrated(challenge));
}
@Override
    public Mono<Void> verify_migrated(MFAChallenge challenge) {
        System.out.println("MFAMockProvider: VERIFY CODE " + configuration.getCode());
        if (!configuration.getCode().equals(challenge.getCode())) {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new InvalidCodeException("Invalid 2FA code"))));
        }
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.empty()));
    }
}

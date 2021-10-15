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
package io.gravitee.am.repository.oauth2.api;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import io.gravitee.am.repository.jdbc.oauth2.api.JdbcPushedAuthorizationRequestRepository;
import io.gravitee.am.repository.oauth2.AbstractOAuthTest;
import io.gravitee.am.repository.oauth2.model.PushedAuthorizationRequest;
import io.gravitee.am.repository.oidc.model.RequestObject;
import io.gravitee.common.util.LinkedMultiValueMap;
import io.reactivex.observers.TestObserver;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PushedAuthorizationRequestRepositoryPurgeTest extends AbstractOAuthTest {

    @Autowired
    private JdbcPushedAuthorizationRequestRepository parRepository;

    @Test
    public void shouldPurge() {
        Instant now = Instant.now();
        PushedAuthorizationRequest object1 = new PushedAuthorizationRequest();
        object1.setDomain("domain");
        object1.setClient("client");
        object1.setParameters(new LinkedMultiValueMap<>());
        object1.setExpireAt(new Date(now.plus(1, ChronoUnit.MINUTES).toEpochMilli()));

        PushedAuthorizationRequest object2 = new PushedAuthorizationRequest();
        object2.setDomain("domain");
        object2.setClient("client");
        object2.setParameters(new LinkedMultiValueMap<>());
        object2.setExpireAt(new Date(now.minus(1, ChronoUnit.MINUTES).toEpochMilli()));

        parRepository.create(object1).test().awaitTerminalEvent();
        parRepository.create(object2).test().awaitTerminalEvent();

        assertNotNull(RxJava2Adapter.maybeToMono(parRepository.findById(object1.getId())).block());
        assertNull(RxJava2Adapter.maybeToMono(parRepository.findById(object2.getId())).block());

        TestObserver<Void> testPurge = parRepository.purgeExpiredData().test();
        testPurge.awaitTerminalEvent();
        testPurge.assertNoErrors();

        assertNotNull(RxJava2Adapter.maybeToMono(parRepository.findById(object1.getId())).block());
        assertNull(RxJava2Adapter.maybeToMono(parRepository.findById(object2.getId())).block());

    }

}

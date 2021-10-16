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

import io.gravitee.am.repository.jdbc.oauth2.api.JdbcAuthorizationCodeRepository;
import io.gravitee.am.repository.oauth2.AbstractOAuthTest;
import io.gravitee.am.repository.oauth2.model.AuthorizationCode;
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
public class AuthorizationCodeRepositoryPurgeTest extends AbstractOAuthTest {

    @Autowired
    private JdbcAuthorizationCodeRepository authorizationCodeRepository;

    @Test
    public void shouldRemoveCode() {
        Instant now = Instant.now();
        String code = "testCode";
        AuthorizationCode authorizationCode = new AuthorizationCode();
        authorizationCode.setId(code);
        authorizationCode.setCode(code);
        authorizationCode.setExpireAt(new Date(now.plus(1, ChronoUnit.MINUTES).toEpochMilli()));
        String codeExpired = "testCodeExpired";
        AuthorizationCode authorizationCodeExpired = new AuthorizationCode();
        authorizationCodeExpired.setId(codeExpired);
        authorizationCodeExpired.setCode(codeExpired);
        authorizationCodeExpired.setExpireAt(new Date(now.minus(1, ChronoUnit.MINUTES).toEpochMilli()));

        TestObserver<AuthorizationCode> testObserver = RxJava2Adapter.monoToSingle(authorizationCodeRepository.create_migrated(authorizationCode)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver = RxJava2Adapter.monoToSingle(authorizationCodeRepository.create_migrated(authorizationCodeExpired)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();

        assertNotNull(authorizationCodeRepository.findByCode_migrated(code).block());
        assertNull(authorizationCodeRepository.findByCode_migrated(codeExpired).block());

        TestObserver<Void> testPurge = RxJava2Adapter.monoToCompletable(authorizationCodeRepository.purgeExpiredData_migrated()).test();
        testPurge.awaitTerminalEvent();
        testPurge.assertNoErrors();

        assertNotNull(authorizationCodeRepository.findByCode_migrated(code).block());
        assertNull(authorizationCodeRepository.findByCode_migrated(codeExpired).block());

    }

}

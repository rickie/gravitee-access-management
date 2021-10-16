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
package io.gravitee.am.repository.management.api;

import io.gravitee.am.model.LoginAttempt;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.reactivex.observers.TestObserver;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class LoginAttemptRepositoryTest extends AbstractManagementTest {
    @Autowired
    protected LoginAttemptRepository repository;

    @Test
    public void shouldCreate() {
        LoginAttempt attempt = buildLoginAttempt();

        TestObserver<LoginAttempt> testObserver = RxJava2Adapter.monoToSingle(repository.create_migrated(attempt)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertValue(l -> l.getId() != null);
        assertEqualsTo(attempt, testObserver);
    }

    @Test
    public void shouldFindById() {
        LoginAttempt attempt = buildLoginAttempt();
        LoginAttempt createdAttempt = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(attempt))).block();

        TestObserver<LoginAttempt> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdAttempt.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertValue(l -> l.getId().equals(createdAttempt.getId()));
        assertEqualsTo(attempt, testObserver);
    }

    @Test
    public void shouldFindByCriteria() {
        LoginAttempt attempt = buildLoginAttempt();
        LoginAttempt createdAttempt = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(attempt))).block();

        LoginAttempt unexpectedAttempt = buildLoginAttempt();
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(unexpectedAttempt))).block();

        TestObserver<LoginAttempt> testObserver = RxJava2Adapter.monoToMaybe(repository.findByCriteria_migrated(new LoginAttemptCriteria.Builder()
                .client(attempt.getClient())
                .domain(attempt.getDomain())
                .username(attempt.getUsername())
                .identityProvider(attempt.getIdentityProvider())
                .build())).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertValue(l -> l.getId().equals(createdAttempt.getId()));
        assertEqualsTo(attempt, testObserver);
    }

    @Test
    public void shouldNotFindByCriteria_invalidDomain() {
        LoginAttempt attempt = buildLoginAttempt();
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(attempt))).block();

        TestObserver<LoginAttempt> testObserver = RxJava2Adapter.monoToMaybe(repository.findByCriteria_migrated(new LoginAttemptCriteria.Builder()
                .client(attempt.getClient())
                .domain("unknown")
                .username(attempt.getUsername())
                .identityProvider(attempt.getIdentityProvider())
                .build())).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldDeleteByCriteria() {
        // should be deleted
        LoginAttempt attempt = buildLoginAttempt();
        LoginAttempt createdAttempt = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(attempt))).block();

        TestObserver<LoginAttempt> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdAttempt.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        assertEqualsTo(createdAttempt, testObserver);

        // shouldn't be deleted
        LoginAttempt unexpectedAttempt = buildLoginAttempt();
        LoginAttempt createdUnexpectedAttempt = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(unexpectedAttempt))).block();

        testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdUnexpectedAttempt.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        assertEqualsTo(createdUnexpectedAttempt, testObserver);

        // delete one of LoginAttempt
        TestObserver<Void> deleteObserver = RxJava2Adapter.monoToCompletable(repository.delete_migrated(new LoginAttemptCriteria.Builder()
                .client(attempt.getClient())
                .domain(attempt.getDomain())
                .username(attempt.getUsername())
                .identityProvider(attempt.getIdentityProvider())
                .build())).test();

        deleteObserver.awaitTerminalEvent();
        deleteObserver.assertNoErrors();

        // check delete successful
        testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdAttempt.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertNoValues();

        // shouldn't be deleted
        testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdUnexpectedAttempt.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        assertEqualsTo(createdUnexpectedAttempt, testObserver);
    }

    @Test
    public void shouldDeleteById() {
        LoginAttempt attempt = buildLoginAttempt();
        LoginAttempt createdAttempt = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(attempt))).block();

        TestObserver<LoginAttempt> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdAttempt.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertValue(l -> l != null);

        TestObserver<Void> deleteObserver = RxJava2Adapter.monoToCompletable(repository.delete_migrated(createdAttempt.getId())).test();
        deleteObserver.awaitTerminalEvent();
        deleteObserver.assertNoErrors();

        testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdAttempt.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldUpdate() {
        LoginAttempt attempt = buildLoginAttempt();
        LoginAttempt createdAttempt = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(attempt))).block();

        TestObserver<LoginAttempt> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdAttempt.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertValue(l -> l != null);

        LoginAttempt updatableAttempt = buildLoginAttempt();
        updatableAttempt.setId(createdAttempt.getId());
        updatableAttempt.setAttempts(654);

        TestObserver<LoginAttempt> updateObserver = RxJava2Adapter.monoToSingle(repository.update_migrated(updatableAttempt)).test();
        updateObserver.awaitTerminalEvent();
        updateObserver.assertNoErrors();
        updateObserver.assertValue( l -> l.getId().equals(createdAttempt.getId()));
        assertEqualsTo(updatableAttempt, updateObserver);
    }


    private void assertEqualsTo(LoginAttempt attempt, TestObserver<LoginAttempt> testObserver) {
        testObserver.assertValue(l -> l.getAttempts() == attempt.getAttempts());
        testObserver.assertValue(l -> l.getClient().equals(attempt.getClient()));
        testObserver.assertValue(l -> l.getDomain().equals(attempt.getDomain()));
        testObserver.assertValue(l -> l.getUsername().equals(attempt.getUsername()));
        testObserver.assertValue(l -> l.getIdentityProvider().equals(attempt.getIdentityProvider()));
    }

    private LoginAttempt buildLoginAttempt() {
        LoginAttempt attempt = new LoginAttempt();
        String random = UUID.randomUUID().toString();
        attempt.setAttempts(1);
        attempt.setClient("client"+random);
        attempt.setDomain("domain"+random);
        attempt.setIdentityProvider("idp"+random);
        attempt.setUsername("user"+random);
        Date createdAt = new Date();
        attempt.setCreatedAt(createdAt);
        attempt.setUpdatedAt(createdAt);
        attempt.setExpireAt(new Date(Instant.now().plusSeconds(60).toEpochMilli()));
        return attempt;
    }
}

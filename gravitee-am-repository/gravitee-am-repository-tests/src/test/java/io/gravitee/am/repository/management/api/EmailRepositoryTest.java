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

import io.gravitee.am.model.Email;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EmailRepositoryTest extends AbstractManagementTest {

    public static final String FIXED_REF_ID = "fixedRefId";
    public static final String FIXED_CLI_ID = "fixedClientId";

    @Autowired
    protected EmailRepository repository;

    protected Email buildEmail() {
        Email email = new Email();
        String randomString = UUID.randomUUID().toString();
        email.setClient("client"+randomString);
        email.setContent("content"+randomString);
        email.setFrom("from"+randomString);
        email.setFromName("fromName"+randomString);
        email.setReferenceId("ref"+randomString);
        email.setReferenceType(ReferenceType.DOMAIN);
        email.setSubject("subject"+randomString);
        email.setTemplate("tpl"+randomString);
        email.setCreatedAt(new Date());
        email.setUpdatedAt(new Date());
        email.setExpiresAfter(120000);
        return email;
    }

    @Test
    public void shouldNotFindById() {
        final TestObserver<Email> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated("unknownId")).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById() {
        Email email = buildEmail();
        Email createdEmail = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).block();

        TestObserver<Email> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdEmail.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(email, createdEmail.getId(), testObserver);
    }

    @Test
    public void shouldFindById_withRef() {
        Email email = buildEmail();
        Email createdEmail = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).block();

        TestObserver<Email> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdEmail.getReferenceType(), createdEmail.getReferenceId(), createdEmail.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(email, createdEmail.getId(), testObserver);
    }

    @Test
    public void shouldUpdateEmail() {
        Email email = buildEmail();
        Email createdEmail = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).block();

        TestObserver<Email> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdEmail.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(email, createdEmail.getId(), testObserver);

        Email updatableEmail = buildEmail();
        updatableEmail.setId(createdEmail.getId());
        TestObserver<Email> updatedEmail = RxJava2Adapter.monoToSingle(repository.update_migrated(updatableEmail)).test();
        updatedEmail.awaitTerminalEvent();
        assertEqualsTo(updatableEmail, createdEmail.getId(), updatedEmail);
    }

    private void assertEqualsTo(Email expectedEmail, String expectedId, TestObserver<Email> testObserver) {
        testObserver.assertValue(e -> e.getId().equals(expectedId));
        testObserver.assertValue(e ->  (e.getClient() == null && expectedEmail.getClient() == null) || e.getClient().equals(expectedEmail.getClient()));
        testObserver.assertValue(e -> e.getContent().equals(expectedEmail.getContent()));
        testObserver.assertValue(e -> e.getExpiresAfter() == expectedEmail.getExpiresAfter());
        testObserver.assertValue(e -> e.getFrom().equals(expectedEmail.getFrom()));
        testObserver.assertValue(e -> e.getFromName().equals(expectedEmail.getFromName()));
        testObserver.assertValue(e -> e.getReferenceId().equals(expectedEmail.getReferenceId()));
        testObserver.assertValue(e -> e.getReferenceType() == expectedEmail.getReferenceType());
        testObserver.assertValue(e -> e.getSubject().equals(expectedEmail.getSubject()));
        testObserver.assertValue(e -> e.getTemplate().equals(expectedEmail.getTemplate()));
    }

    @Test
    public void shouldDeleteById() {
        Email email = buildEmail();
        Email createdEmail = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).block();

        TestObserver<Email> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdEmail.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(email, createdEmail.getId(), testObserver);

        RxJava2Adapter.monoToCompletable(repository.delete_migrated(createdEmail.getId())).blockingGet();

        testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(createdEmail.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindAllEmails() {
        TestSubscriber<Email> testSubscriber = RxJava2Adapter.fluxToFlowable(repository.findAll_migrated()).test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        List<Email> emails = IntStream.range(0, loop).mapToObj(__ -> buildEmail()).collect(Collectors.toList());
        emails.forEach(email -> RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).map(RxJavaReactorMigrationUtil.toJdkFunction(e -> {
            email.setId(e.getId());
            return e;
        })).block());

        TestSubscriber<String> testIdSubscriber = RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(repository.findAll_migrated())).map(RxJavaReactorMigrationUtil.toJdkFunction(Email::getId))).test();
        testIdSubscriber.awaitTerminalEvent();
        testIdSubscriber.assertNoErrors();
        testIdSubscriber.assertValueCount(loop);
        testIdSubscriber.assertValueSet(emails.stream().map(Email::getId).collect(Collectors.toList()));
    }

    @Test
    public void shouldFindAllByReference() {
        TestSubscriber<Email> testSubscriber = RxJava2Adapter.fluxToFlowable(repository.findAll_migrated()).test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        List<Email> emails = IntStream.range(0, loop).mapToObj(__ -> {
            final Email email = buildEmail();
            email.setReferenceId(FIXED_REF_ID);
            return email;
        }).collect(Collectors.toList());
        emails.forEach(email -> RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).map(RxJavaReactorMigrationUtil.toJdkFunction(e -> {
            email.setId(e.getId());
            return e;
        })).block());

        // random refId
        IntStream.range(0, loop).forEach(email -> RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(buildEmail()))).block());

        TestSubscriber<String> testIdSubscriber = RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(repository.findAll_migrated(ReferenceType.DOMAIN, FIXED_REF_ID))).map(RxJavaReactorMigrationUtil.toJdkFunction(Email::getId))).test();
        testIdSubscriber.awaitTerminalEvent();
        testIdSubscriber.assertNoErrors();
        testIdSubscriber.assertValueCount(loop);
        testIdSubscriber.assertValueSet(emails.stream().map(Email::getId).collect(Collectors.toList()));
    }

    @Test
    public void shouldFindByClient() {
        TestSubscriber<Email> testSubscriber = RxJava2Adapter.fluxToFlowable(repository.findAll_migrated()).test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        List<Email> emails = IntStream.range(0, loop).mapToObj(__ -> {
            final Email email = buildEmail();
            email.setReferenceId(FIXED_REF_ID);
            email.setClient(FIXED_CLI_ID);
            return email;
        }).collect(Collectors.toList());
        emails.forEach(email -> RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).map(RxJavaReactorMigrationUtil.toJdkFunction(e -> {
            email.setId(e.getId());
            return e;
        })).block());

        for (int i = 0; i < loop; i++) {
            final Email email = buildEmail();
            email.setReferenceId(FIXED_REF_ID);
            RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).block();
        }

        TestSubscriber<String> testIdSubscriber = RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(repository.findByClient_migrated(ReferenceType.DOMAIN, FIXED_REF_ID, FIXED_CLI_ID))).map(RxJavaReactorMigrationUtil.toJdkFunction(Email::getId))).test();
        testIdSubscriber.awaitTerminalEvent();
        testIdSubscriber.assertNoErrors();
        testIdSubscriber.assertValueCount(loop);
        testIdSubscriber.assertValueSet(emails.stream().map(Email::getId).collect(Collectors.toList()));
    }

    @Test
    public void shouldFindByTemplate() {
        TestSubscriber<Email> testSubscriber = RxJava2Adapter.fluxToFlowable(repository.findAll_migrated()).test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        for (int i = 0; i < loop; i++) {
            final Email email = buildEmail();
            email.setReferenceId(FIXED_REF_ID);
            RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).block();
        }

        final Email email = buildEmail();
        email.setReferenceId(FIXED_REF_ID);
        email.setTemplate("MyTemplateId");
        email.setClient(null);
        Email templateEmail = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).block();

        TestObserver<Email> testMaybe = RxJava2Adapter.monoToMaybe(repository.findByTemplate_migrated(ReferenceType.DOMAIN, FIXED_REF_ID, "MyTemplateId")).test();
        testMaybe.awaitTerminalEvent();
        testMaybe.assertNoErrors();
        assertEqualsTo(templateEmail, templateEmail.getId(), testMaybe);
    }

    @Test
    public void shouldFindByClientAndTemplate() {
        TestSubscriber<Email> testSubscriber = RxJava2Adapter.fluxToFlowable(repository.findAll_migrated()).test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        for (int i = 0; i < loop; i++) {
            final Email email = buildEmail();
            email.setReferenceId(FIXED_REF_ID);
            email.setClient(FIXED_CLI_ID);
            RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).block();
        }

        final Email email = buildEmail();
        email.setReferenceId(FIXED_REF_ID);
        email.setClient(FIXED_CLI_ID);
        email.setTemplate("MyTemplateId");
        Email templateEmail = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(email))).block();

        TestObserver<Email> testMaybe = RxJava2Adapter.monoToMaybe(repository.findByClientAndTemplate_migrated(ReferenceType.DOMAIN, FIXED_REF_ID, FIXED_CLI_ID, "MyTemplateId")).test();
        testMaybe.awaitTerminalEvent();
        testMaybe.assertNoErrors();
        assertEqualsTo(templateEmail, templateEmail.getId(), testMaybe);
    }

}

/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.repository.management.api;

import io.gravitee.am.model.Email;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EmailRepositoryTest extends AbstractManagementTest {

    public static final String FIXED_REF_ID = "fixedRefId";
    public static final String FIXED_CLI_ID = "fixedClientId";

    @Autowired protected EmailRepository repository;

    protected Email buildEmail() {
        Email email = new Email();
        String randomString = UUID.randomUUID().toString();
        email.setClient("client" + randomString);
        email.setContent("content" + randomString);
        email.setFrom("from" + randomString);
        email.setFromName("fromName" + randomString);
        email.setReferenceId("ref" + randomString);
        email.setReferenceType(ReferenceType.DOMAIN);
        email.setSubject("subject" + randomString);
        email.setTemplate("tpl" + randomString);
        email.setCreatedAt(new Date());
        email.setUpdatedAt(new Date());
        email.setExpiresAfter(120000);
        return email;
    }

    @Test
    public void shouldNotFindById() {
        final TestObserver<Email> testObserver = repository.findById("unknownId").test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById() {
        Email email = buildEmail();
        Email createdEmail = repository.create(email).blockingGet();

        TestObserver<Email> testObserver = repository.findById(createdEmail.getId()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(email, createdEmail.getId(), testObserver);
    }

    @Test
    public void shouldFindById_withRef() {
        Email email = buildEmail();
        Email createdEmail = repository.create(email).blockingGet();

        TestObserver<Email> testObserver =
                repository
                        .findById(
                                createdEmail.getReferenceType(),
                                createdEmail.getReferenceId(),
                                createdEmail.getId())
                        .test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(email, createdEmail.getId(), testObserver);
    }

    @Test
    public void shouldUpdateEmail() {
        Email email = buildEmail();
        Email createdEmail = repository.create(email).blockingGet();

        TestObserver<Email> testObserver = repository.findById(createdEmail.getId()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(email, createdEmail.getId(), testObserver);

        Email updatableEmail = buildEmail();
        updatableEmail.setId(createdEmail.getId());
        TestObserver<Email> updatedEmail = repository.update(updatableEmail).test();
        updatedEmail.awaitTerminalEvent();
        assertEqualsTo(updatableEmail, createdEmail.getId(), updatedEmail);
    }

    private void assertEqualsTo(
            Email expectedEmail, String expectedId, TestObserver<Email> testObserver) {
        testObserver.assertValue(e -> e.getId().equals(expectedId));
        testObserver.assertValue(
                e ->
                        (e.getClient() == null && expectedEmail.getClient() == null)
                                || e.getClient().equals(expectedEmail.getClient()));
        testObserver.assertValue(e -> e.getContent().equals(expectedEmail.getContent()));
        testObserver.assertValue(e -> e.getExpiresAfter() == expectedEmail.getExpiresAfter());
        testObserver.assertValue(e -> e.getFrom().equals(expectedEmail.getFrom()));
        testObserver.assertValue(e -> e.getFromName().equals(expectedEmail.getFromName()));
        testObserver.assertValue(e -> e.getReferenceId().equals(expectedEmail.getReferenceId()));
        testObserver.assertValue(
                e -> e.getReferenceType().equals(expectedEmail.getReferenceType()));
        testObserver.assertValue(e -> e.getSubject().equals(expectedEmail.getSubject()));
        testObserver.assertValue(e -> e.getTemplate().equals(expectedEmail.getTemplate()));
    }

    @Test
    public void shouldDeleteById() {
        Email email = buildEmail();
        Email createdEmail = repository.create(email).blockingGet();

        TestObserver<Email> testObserver = repository.findById(createdEmail.getId()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(email, createdEmail.getId(), testObserver);

        repository.delete(createdEmail.getId()).blockingGet();

        testObserver = repository.findById(createdEmail.getId()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindAllEmails() {
        TestSubscriber<Email> testSubscriber = repository.findAll().test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        List<Email> emails =
                IntStream.range(0, loop).mapToObj(__ -> buildEmail()).collect(Collectors.toList());
        emails.forEach(
                email ->
                        repository
                                .create(email)
                                .map(
                                        e -> {
                                            email.setId(e.getId());
                                            return e;
                                        })
                                .blockingGet());

        TestSubscriber<String> testIdSubscriber = repository.findAll().map(Email::getId).test();
        testIdSubscriber.awaitTerminalEvent();
        testIdSubscriber.assertNoErrors();
        testIdSubscriber.assertValueCount(loop);
        testIdSubscriber.assertValueSet(
                emails.stream().map(Email::getId).collect(Collectors.toList()));
    }

    @Test
    public void shouldFindAllByReference() {
        TestSubscriber<Email> testSubscriber = repository.findAll().test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        List<Email> emails =
                IntStream.range(0, loop)
                        .mapToObj(
                                __ -> {
                                    final Email email = buildEmail();
                                    email.setReferenceId(FIXED_REF_ID);
                                    return email;
                                })
                        .collect(Collectors.toList());
        emails.forEach(
                email ->
                        repository
                                .create(email)
                                .map(
                                        e -> {
                                            email.setId(e.getId());
                                            return e;
                                        })
                                .blockingGet());

        // random refId
        IntStream.range(0, loop).forEach(email -> repository.create(buildEmail()).blockingGet());

        TestSubscriber<String> testIdSubscriber =
                repository.findAll(ReferenceType.DOMAIN, FIXED_REF_ID).map(Email::getId).test();
        testIdSubscriber.awaitTerminalEvent();
        testIdSubscriber.assertNoErrors();
        testIdSubscriber.assertValueCount(loop);
        testIdSubscriber.assertValueSet(
                emails.stream().map(Email::getId).collect(Collectors.toList()));
    }

    @Test
    public void shouldFindByClient() {
        TestSubscriber<Email> testSubscriber = repository.findAll().test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        List<Email> emails =
                IntStream.range(0, loop)
                        .mapToObj(
                                __ -> {
                                    final Email email = buildEmail();
                                    email.setReferenceId(FIXED_REF_ID);
                                    email.setClient(FIXED_CLI_ID);
                                    return email;
                                })
                        .collect(Collectors.toList());
        emails.forEach(
                email ->
                        repository
                                .create(email)
                                .map(
                                        e -> {
                                            email.setId(e.getId());
                                            return e;
                                        })
                                .blockingGet());

        for (int i = 0; i < loop; i++) {
            final Email email = buildEmail();
            email.setReferenceId(FIXED_REF_ID);
            repository.create(email).blockingGet();
        }

        TestSubscriber<String> testIdSubscriber =
                repository
                        .findByClient(ReferenceType.DOMAIN, FIXED_REF_ID, FIXED_CLI_ID)
                        .map(Email::getId)
                        .test();
        testIdSubscriber.awaitTerminalEvent();
        testIdSubscriber.assertNoErrors();
        testIdSubscriber.assertValueCount(loop);
        testIdSubscriber.assertValueSet(
                emails.stream().map(Email::getId).collect(Collectors.toList()));
    }

    @Test
    public void shouldFindByTemplate() {
        TestSubscriber<Email> testSubscriber = repository.findAll().test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        for (int i = 0; i < loop; i++) {
            final Email email = buildEmail();
            email.setReferenceId(FIXED_REF_ID);
            repository.create(email).blockingGet();
        }

        final Email email = buildEmail();
        email.setReferenceId(FIXED_REF_ID);
        email.setTemplate("MyTemplateId");
        email.setClient(null);
        Email templateEmail = repository.create(email).blockingGet();

        TestObserver<Email> testMaybe =
                repository
                        .findByTemplate(ReferenceType.DOMAIN, FIXED_REF_ID, "MyTemplateId")
                        .test();
        testMaybe.awaitTerminalEvent();
        testMaybe.assertNoErrors();
        assertEqualsTo(templateEmail, templateEmail.getId(), testMaybe);
    }

    @Test
    public void shouldFindByClientAndTemplate() {
        TestSubscriber<Email> testSubscriber = repository.findAll().test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();

        final int loop = 10;
        for (int i = 0; i < loop; i++) {
            final Email email = buildEmail();
            email.setReferenceId(FIXED_REF_ID);
            email.setClient(FIXED_CLI_ID);
            repository.create(email).blockingGet();
        }

        final Email email = buildEmail();
        email.setReferenceId(FIXED_REF_ID);
        email.setClient(FIXED_CLI_ID);
        email.setTemplate("MyTemplateId");
        Email templateEmail = repository.create(email).blockingGet();

        TestObserver<Email> testMaybe =
                repository
                        .findByClientAndTemplate(
                                ReferenceType.DOMAIN, FIXED_REF_ID, FIXED_CLI_ID, "MyTemplateId")
                        .test();
        testMaybe.awaitTerminalEvent();
        testMaybe.assertNoErrors();
        assertEqualsTo(templateEmail, templateEmail.getId(), testMaybe);
    }
}

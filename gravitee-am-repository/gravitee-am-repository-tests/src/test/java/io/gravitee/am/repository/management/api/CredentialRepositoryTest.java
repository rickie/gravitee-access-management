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

import io.gravitee.am.model.Credential;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CredentialRepositoryTest extends AbstractManagementTest {

    @Autowired
    private CredentialRepository credentialRepository;

    @Test
    public void findByUserId() throws TechnicalException {
        // create credential
        Credential credential = buildCredential();
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential))).block();

        // fetch credentials
        TestSubscriber<Credential> testObserver = RxJava2Adapter.fluxToFlowable(credentialRepository.findByUserId_migrated(credential.getReferenceType(), credential.getReferenceId(), credential.getUserId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void findByUsername() throws TechnicalException {
        // create credential
        Credential credential = buildCredential();
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential))).block();

        // fetch credentials
        TestSubscriber<Credential> testSubscriber = RxJava2Adapter.fluxToFlowable(credentialRepository.findByUsername_migrated(credential.getReferenceType(), credential.getReferenceId(), credential.getUsername())).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void findByCredentialId() throws TechnicalException {
        // create credential
        Credential credential = buildCredential();
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential))).block();

        // fetch credentials
        TestSubscriber<Credential> testSubscriber = RxJava2Adapter.fluxToFlowable(credentialRepository.findByCredentialId_migrated(credential.getReferenceType(), credential.getReferenceId(), credential.getCredentialId())).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    private Credential buildCredential() {
        Credential credential = new Credential();
        credential.setReferenceType(ReferenceType.DOMAIN);
        credential.setReferenceId("domainId");
        String randomStr = UUID.randomUUID().toString();
        credential.setCredentialId("cred"+ randomStr);
        credential.setUserId("uid"+ randomStr);
        credential.setUsername("uname"+ randomStr);
        credential.setPublicKey("pub"+ randomStr);
        credential.setAccessedAt(new Date());
        credential.setUserAgent("uagent"+ randomStr);
        credential.setIpAddress("ip"+ randomStr);
        return credential;
    }

    @Test
    public void testFindById() throws TechnicalException {
        // create credential
        Credential credential = buildCredential();
        Credential credentialCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential))).block();

        // fetch credential
        TestObserver<Credential> testObserver = RxJava2Adapter.monoToMaybe(credentialRepository.findById_migrated(credentialCreated.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> c.getId().equals(credentialCreated.getId()));
        testObserver.assertValue(c -> c.getCredentialId().equals(credential.getCredentialId()));
        testObserver.assertValue(c -> c.getPublicKey().equals(credential.getPublicKey()));
        testObserver.assertValue(c -> c.getReferenceId().equals(credential.getReferenceId()));
        testObserver.assertValue(c -> c.getReferenceType() == credential.getReferenceType());
        testObserver.assertValue(c -> c.getUserId().equals(credential.getUserId()));
        testObserver.assertValue(c -> c.getUsername().equals(credential.getUsername()));
        testObserver.assertValue(c -> c.getIpAddress().equals(credential.getIpAddress()));
        testObserver.assertValue(c -> c.getUserAgent().equals(credential.getUserAgent()));
    }

    @Test
    public void testNotFoundById() throws TechnicalException {
        RxJava2Adapter.monoToMaybe(credentialRepository.findById_migrated("test")).test().assertEmpty();
    }

    @Test
    public void testCreate() throws TechnicalException {
        Credential credential = buildCredential();

        TestObserver<Credential> testObserver = RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> c.getCredentialId().equals(credential.getCredentialId()));
    }

    @Test
    public void testUpdate() throws TechnicalException {
        // create credential
        Credential credential = buildCredential();
        Credential credentialCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential))).block();

        // update credential
        Credential updateCredential = buildCredential();
        updateCredential.setId(credentialCreated.getId());
        updateCredential.setCredentialId("updateCredentialId");

        TestObserver<Credential> testObserver = RxJava2Adapter.monoToSingle(credentialRepository.update_migrated(updateCredential)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> c.getCredentialId().equals(updateCredential.getCredentialId()));
    }

    @Test
    public void testDelete() throws TechnicalException {
        // create credential
        Credential credential = buildCredential();
        Credential credentialCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential))).block();

        // fetch credential
        TestObserver<Credential> testObserver = RxJava2Adapter.monoToMaybe(credentialRepository.findById_migrated(credentialCreated.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(c -> c.getCredentialId().equals(credentialCreated.getCredentialId()));

        // delete credential
        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(credentialRepository.delete_migrated(credentialCreated.getId())).test();
        testObserver1.awaitTerminalEvent();

        // fetch credential
        RxJava2Adapter.monoToMaybe(credentialRepository.findById_migrated(credentialCreated.getId())).test().assertEmpty();
    }

    @Test
    public void testDeleteByUser() throws TechnicalException {
        // create credential
        Credential credential = new Credential();
        credential.setCredentialId("credentialId");
        credential.setReferenceType(ReferenceType.DOMAIN);
        credential.setReferenceId("domain-id");
        credential.setUserId("user-id");
        Credential credentialCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential))).block();

        // fetch credential
        TestSubscriber<Credential> testSubscriber = RxJava2Adapter.fluxToFlowable(credentialRepository.findByUserId_migrated(ReferenceType.DOMAIN, "domain-id", "user-id")).test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue(c -> c.getCredentialId().equals(credentialCreated.getCredentialId()));

        // delete credential
        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(credentialRepository.deleteByUserId_migrated(ReferenceType.DOMAIN, "domain-id", "user-id")).test();
        testObserver1.awaitTerminalEvent();

        // fetch credential
        TestSubscriber<Credential> testSubscriber2 = RxJava2Adapter.fluxToFlowable(credentialRepository.findByUserId_migrated(ReferenceType.DOMAIN, "domain-id", "user-id")).test();
        testSubscriber2.awaitTerminalEvent();
        testSubscriber2.assertComplete();
        testSubscriber2.assertNoErrors();
        testSubscriber2.assertNoValues();
    }

    @Test
    public void testDeleteByUser_invalid_user() throws TechnicalException {
        // create credential
        Credential credential = new Credential();
        credential.setCredentialId("credentialId");
        credential.setReferenceType(ReferenceType.DOMAIN);
        credential.setReferenceId("domain-id");
        credential.setUserId("user-id");
        Credential credentialCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential))).block();

        // fetch credential
        TestSubscriber<Credential> testSubscriber = RxJava2Adapter.fluxToFlowable(credentialRepository.findByUserId_migrated(ReferenceType.DOMAIN, "domain-id", "user-id")).test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue(c -> c.getCredentialId().equals(credentialCreated.getCredentialId()));

        // delete credential
        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(credentialRepository.deleteByUserId_migrated(ReferenceType.DOMAIN, "domain-id", "wrong-user-id")).test();
        testObserver1.awaitTerminalEvent();

        // fetch credential
        TestSubscriber<Credential> testSubscriber2 = RxJava2Adapter.fluxToFlowable(credentialRepository.findByUserId_migrated(ReferenceType.DOMAIN, "domain-id", "user-id")).test();
        testSubscriber2.awaitTerminalEvent();
        testSubscriber2.assertComplete();
        testSubscriber2.assertNoErrors();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue(c -> c.getCredentialId().equals(credentialCreated.getCredentialId()));
    }

    @Test
    public void testDeleteByAaguid() throws TechnicalException {
        // create credential
        Credential credential = new Credential();
        credential.setCredentialId("credentialId");
        credential.setReferenceType(ReferenceType.DOMAIN);
        credential.setReferenceId("domain-id");
        credential.setUserId("user-id");
        credential.setAaguid("aaguid");
        Credential credentialCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(credentialRepository.create_migrated(credential))).block();

        // fetch credential
        TestSubscriber<Credential> testSubscriber = RxJava2Adapter.fluxToFlowable(credentialRepository.findByUserId_migrated(ReferenceType.DOMAIN, "domain-id", "user-id")).test();
        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue(c -> c.getCredentialId().equals(credentialCreated.getCredentialId()));

        // delete credential
        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(credentialRepository.deleteByAaguid_migrated(ReferenceType.DOMAIN, "domain-id", "aaguid")).test();
        testObserver1.awaitTerminalEvent();

        // fetch credential
        TestSubscriber<Credential> testSubscriber2 = RxJava2Adapter.fluxToFlowable(credentialRepository.findByUserId_migrated(ReferenceType.DOMAIN, "domain-id", "user-id")).test();
        testSubscriber2.awaitTerminalEvent();
        testSubscriber2.assertComplete();
        testSubscriber2.assertNoErrors();
        testSubscriber2.assertNoValues();
    }
}

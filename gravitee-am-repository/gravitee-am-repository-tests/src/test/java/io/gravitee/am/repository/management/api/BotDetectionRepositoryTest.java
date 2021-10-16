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

import io.gravitee.am.model.BotDetection;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.gravitee.common.utils.UUID;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.Date;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class BotDetectionRepositoryTest extends AbstractManagementTest {

    @Autowired
    private BotDetectionRepository repository;

    @Test
    public void testFindByDomain() throws TechnicalException {
        BotDetection botDetection = buildBotDetection();
        botDetection.setReferenceId("testDomain");
        botDetection.setReferenceType(ReferenceType.DOMAIN);
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(botDetection))).block();

        TestSubscriber<BotDetection> testSubscriber = RxJava2Adapter.fluxToFlowable(repository.findByReference_migrated(ReferenceType.DOMAIN, "testDomain")).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    private BotDetection buildBotDetection() {
        BotDetection bdetect = new BotDetection();
        String random = UUID.random().toString();
        bdetect.setName("name"+random);
        bdetect.setReferenceId("domain"+random);
        bdetect.setReferenceType(ReferenceType.DOMAIN);
        bdetect.setConfiguration("{\"config\": \"" + random +"\"}");
        bdetect.setType("type"+random);
        bdetect.setDetectionType("CAPTCHA");
        bdetect.setCreatedAt(new Date());
        bdetect.setUpdatedAt(new Date());
        return bdetect;
    }

    @Test
    public void testFindById() throws TechnicalException {
        BotDetection bdectection = buildBotDetection();
        BotDetection bdetectionCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(bdectection))).block();

        TestObserver<BotDetection> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(bdetectionCreated.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(bd -> bd.getId().equals(bdetectionCreated.getId()));
        testObserver.assertValue(bd -> bd.getName().equals(bdetectionCreated.getName()));
        testObserver.assertValue(bd -> bd.getConfiguration().equals(bdetectionCreated.getConfiguration()));
        testObserver.assertValue(bd -> bd.getReferenceId().equals(bdetectionCreated.getReferenceId()));
        testObserver.assertValue(bd -> bd.getType().equals(bdetectionCreated.getType()));
    }

    @Test
    public void testNotFoundById() throws TechnicalException {
        RxJava2Adapter.monoToMaybe(repository.findById_migrated("test")).test().assertEmpty();
    }

    @Test
    public void testCreate() throws TechnicalException {
        BotDetection bDetection = buildBotDetection();

        TestObserver<BotDetection> testObserver = RxJava2Adapter.monoToSingle(repository.create_migrated(bDetection)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(bd -> bd.getId() != null);
        testObserver.assertValue(bd -> bd.getName().equals(bDetection.getName()));
        testObserver.assertValue(bd -> bd.getConfiguration().equals(bDetection.getConfiguration()));
        testObserver.assertValue(bd -> bd.getReferenceId().equals(bDetection.getReferenceId()));
        testObserver.assertValue(bd -> bd.getType().equals(bDetection.getType()));
    }

    @Test
    public void testUpdate() throws TechnicalException {
        BotDetection botDetection = buildBotDetection();
        BotDetection botDetectionCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(botDetection))).block();

        BotDetection bDetection = buildBotDetection();
        bDetection.setId(botDetectionCreated.getId());
        bDetection.setName("testUpdatedName");

        TestObserver<BotDetection> testObserver = RxJava2Adapter.monoToSingle(repository.update_migrated(bDetection)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(bd -> bd.getId().equals(botDetectionCreated.getId()));
        testObserver.assertValue(bd -> bd.getName().equals(bDetection.getName()));
        testObserver.assertValue(bd -> bd.getConfiguration().equals(bDetection.getConfiguration()));
        testObserver.assertValue(bd -> bd.getReferenceId().equals(bDetection.getReferenceId()));
        testObserver.assertValue(bd -> bd.getType().equals(bDetection.getType()));
    }

    @Test
    public void testDelete() throws TechnicalException {
        BotDetection botDetection = buildBotDetection();
        BotDetection botDetectionCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(repository.create_migrated(botDetection))).block();

        TestObserver<BotDetection> testObserver = RxJava2Adapter.monoToMaybe(repository.findById_migrated(botDetectionCreated.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(bd -> bd.getName().equals(botDetectionCreated.getName()));

        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(repository.delete_migrated(botDetectionCreated.getId())).test();
        testObserver1.awaitTerminalEvent();

        RxJava2Adapter.monoToMaybe(repository.findById_migrated(botDetectionCreated.getId())).test().assertEmpty();
    }

}

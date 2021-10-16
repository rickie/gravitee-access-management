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

import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.alert.AlertNotifier;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.gravitee.am.repository.management.api.search.AlertNotifierCriteria;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.Collections;
import java.util.Date;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AlertNotifierRepositoryTest extends AbstractManagementTest {

    private static final String DOMAIN_ID = "alertNotifier#1";

    @Autowired
    private AlertNotifierRepository alertNotifierRepository;

    @Test
    public void testFindById() {
        // create idp
        AlertNotifier alertNotifier = buildAlertNotifier();
        AlertNotifier alertNotifierCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(alertNotifierRepository.create_migrated(alertNotifier))).block();

        // fetch idp
        TestObserver<AlertNotifier> testObserver = RxJava2Adapter.monoToMaybe(alertNotifierRepository.findById_migrated(alertNotifierCreated.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(found -> found.getId().equals(alertNotifier.getId()));
    }

    @Test
    public void testNotFoundById() {
        RxJava2Adapter.monoToMaybe(alertNotifierRepository.findById_migrated("UNKNOWN")).test().assertEmpty();
    }

    @Test
    public void testCreate() {
        AlertNotifier alertNotifier = buildAlertNotifier();
        TestObserver<AlertNotifier> testObserver = RxJava2Adapter.monoToSingle(alertNotifierRepository.create_migrated(alertNotifier)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(idp -> idp.getId().equals(alertNotifier.getId()));
    }

    @Test
    public void testUpdate() {
        // create idp
        AlertNotifier alertNotifier = buildAlertNotifier();
        AlertNotifier alertNotifierCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(alertNotifierRepository.create_migrated(alertNotifier))).block();

        // update idp
        AlertNotifier updatedAlertNotifier = buildAlertNotifier();
        updatedAlertNotifier.setId(alertNotifierCreated.getId());
        updatedAlertNotifier.setEnabled(false);

        TestObserver<AlertNotifier> testObserver = RxJava2Adapter.monoToSingle(alertNotifierRepository.update_migrated(updatedAlertNotifier)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(updated -> updated.getId().equals(updatedAlertNotifier.getId())
                && !updated.isEnabled());
    }

    @Test
    public void testDelete() {
        // create idp
        AlertNotifier alertNotifier = buildAlertNotifier();
        AlertNotifier alertNotifierCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(alertNotifierRepository.create_migrated(alertNotifier))).block();

        // delete idp
        TestObserver<Void> testObserver1 = RxJava2Adapter.monoToCompletable(alertNotifierRepository.delete_migrated(alertNotifierCreated.getId())).test();
        testObserver1.awaitTerminalEvent();

        // fetch idp
        RxJava2Adapter.monoToMaybe(alertNotifierRepository.findById_migrated(alertNotifierCreated.getId())).test().assertEmpty();
    }

    @Test
    public void findByCriteria() {
        AlertNotifier alertNotifierToCreate = buildAlertNotifier();
        AlertNotifier alertNotifierCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(alertNotifierRepository.create_migrated(alertNotifierToCreate))).block();

        AlertNotifierCriteria criteria = new AlertNotifierCriteria();
        criteria.setEnabled(false);
        TestSubscriber<AlertNotifier> testObserver1 = RxJava2Adapter.fluxToFlowable(alertNotifierRepository.findByCriteria_migrated(ReferenceType.DOMAIN, DOMAIN_ID, criteria)).test();

        testObserver1.awaitTerminalEvent();
        testObserver1.assertComplete();
        testObserver1.assertNoErrors();
        testObserver1.assertNoValues();

        alertNotifierCreated.setEnabled(false);
        final AlertNotifier alertNotifierUpdated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(alertNotifierRepository.update_migrated(alertNotifierCreated))).block();
        testObserver1 = RxJava2Adapter.fluxToFlowable(alertNotifierRepository.findByCriteria_migrated(ReferenceType.DOMAIN, DOMAIN_ID, criteria)).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertComplete();
        testObserver1.assertNoErrors();
        testObserver1.assertValue(alertNotifier -> alertNotifier.getId().equals(alertNotifierUpdated.getId()));
    }

    @Test
    public void findAll() {
        TestSubscriber<AlertNotifier> testObserver1 = RxJava2Adapter.fluxToFlowable(alertNotifierRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN_ID)).test();

        testObserver1.awaitTerminalEvent();
        testObserver1.assertComplete();
        testObserver1.assertNoErrors();
        testObserver1.assertNoValues();
        AlertNotifier alertNotifierToCreate1 = buildAlertNotifier();
        AlertNotifier alertNotifierToCreate2 = buildAlertNotifier();
        alertNotifierToCreate2.setReferenceId("domain#2");
        AlertNotifier alertNotifierCreated1 = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(alertNotifierRepository.create_migrated(alertNotifierToCreate1))).block();
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(alertNotifierRepository.create_migrated(alertNotifierToCreate2))).block();

        testObserver1 = RxJava2Adapter.fluxToFlowable(alertNotifierRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN_ID)).test();

        testObserver1.awaitTerminalEvent();
        testObserver1.assertComplete();
        testObserver1.assertValue(alertNotifier -> alertNotifier.getId().equals(alertNotifierCreated1.getId()));
    }

    @Test
    public void findByCriteriaWithEmptyNotifierIdList() {
        TestSubscriber<AlertNotifier> testObserver1 = RxJava2Adapter.fluxToFlowable(alertNotifierRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN_ID)).test();

        testObserver1.awaitTerminalEvent();
        testObserver1.assertComplete();
        testObserver1.assertNoErrors();
        testObserver1.assertNoValues();
        AlertNotifier alertNotifierToCreate1 = buildAlertNotifier();
        AlertNotifier alertNotifierToCreate2 = buildAlertNotifier();
        alertNotifierToCreate2.setReferenceId("domain#2");
        AlertNotifier alertNotifierCreated1 = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(alertNotifierRepository.create_migrated(alertNotifierToCreate1))).block();
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(alertNotifierRepository.create_migrated(alertNotifierToCreate2))).block();

        final AlertNotifierCriteria criteria = new AlertNotifierCriteria();
        criteria.setIds(Collections.emptyList());
        testObserver1 = RxJava2Adapter.fluxToFlowable(alertNotifierRepository.findByCriteria_migrated(ReferenceType.DOMAIN, DOMAIN_ID, criteria)).test();

        testObserver1.awaitTerminalEvent();
        testObserver1.assertComplete();
        testObserver1.assertValue(alertNotifier -> alertNotifier.getId().equals(alertNotifierCreated1.getId()));
    }

    private AlertNotifier buildAlertNotifier() {
        AlertNotifier alertNotifier = new AlertNotifier();
        alertNotifier.setId(RandomString.generate());
        alertNotifier.setEnabled(true);
        alertNotifier.setName("alert-notifier-name");
        alertNotifier.setReferenceType(ReferenceType.DOMAIN);
        alertNotifier.setReferenceId(DOMAIN_ID);
        alertNotifier.setConfiguration("{}");
        alertNotifier.setType("webhook");
        alertNotifier.setCreatedAt(new Date());
        alertNotifier.setUpdatedAt(new Date());
        return alertNotifier;
    }
}

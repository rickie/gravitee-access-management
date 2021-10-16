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

import io.gravitee.am.model.Factor;
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
public class FactorRepositoryTest extends AbstractManagementTest {
    @Autowired
    private FactorRepository factorRepository;

    @Test
    public void testFindByDomain() throws TechnicalException {
        // create factor
        Factor factor = buildFactor();
        factor.setDomain("testDomain");
        factorRepository.create_migrated(factor).block();

        // fetch factors
        TestSubscriber<Factor> testSubscriber = RxJava2Adapter.fluxToFlowable(factorRepository.findByDomain_migrated("testDomain")).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    private Factor buildFactor() {
        Factor factor = new Factor();
        String random = UUID.random().toString();
        factor.setName("name"+random);
        factor.setDomain("domain"+random);
        factor.setConfiguration("{\"config\": \"" + random +"\"}");
        factor.setFactorType("EMAIL");
        factor.setType("type"+random);
        factor.setCreatedAt(new Date());
        factor.setUpdatedAt(new Date());
        return factor;
    }

    @Test
    public void testFindById() throws TechnicalException {
        // create factor
        Factor factor = buildFactor();
        Factor factorCreated = factorRepository.create_migrated(factor).block();

        // fetch factor
        TestObserver<Factor> testObserver = RxJava2Adapter.monoToMaybe(factorRepository.findById_migrated(factorCreated.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(f -> f.getId().equals(factorCreated.getId()));
        testObserver.assertValue(f -> f.getName().equals(factorCreated.getName()));
        testObserver.assertValue(f -> f.getConfiguration().equals(factorCreated.getConfiguration()));
        testObserver.assertValue(f -> f.getDomain().equals(factorCreated.getDomain()));
        testObserver.assertValue(f -> f.getFactorType() == factorCreated.getFactorType());
        testObserver.assertValue(f -> f.getType().equals(factorCreated.getType()));
    }

    @Test
    public void testNotFoundById() throws TechnicalException {
        RxJava2Adapter.monoToMaybe(factorRepository.findById_migrated("test")).test().assertEmpty();
    }

    @Test
    public void testCreate() throws TechnicalException {
        Factor factor = buildFactor();

        TestObserver<Factor> testObserver = RxJava2Adapter.monoToSingle(factorRepository.create_migrated(factor)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(f -> f.getId() != null);
        testObserver.assertValue(f -> f.getName().equals(factor.getName()));
        testObserver.assertValue(f -> f.getConfiguration().equals(factor.getConfiguration()));
        testObserver.assertValue(f -> f.getDomain().equals(factor.getDomain()));
        testObserver.assertValue(f -> f.getFactorType() == factor.getFactorType());
        testObserver.assertValue(f -> f.getType().equals(factor.getType()));
    }

    @Test
    public void testUpdate() throws TechnicalException {
        // create factor
        Factor factor = buildFactor();
        Factor factorCreated = factorRepository.create_migrated(factor).block();

        // update factor
        Factor updateFactor = buildFactor();
        updateFactor.setId(factorCreated.getId());
        updateFactor.setName("testUpdatedName");

        TestObserver<Factor> testObserver = RxJava2Adapter.monoToSingle(factorRepository.update_migrated(updateFactor)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(f -> f.getId().equals(factorCreated.getId()));
        testObserver.assertValue(f -> f.getName().equals(updateFactor.getName()));
        testObserver.assertValue(f -> f.getConfiguration().equals(updateFactor.getConfiguration()));
        testObserver.assertValue(f -> f.getDomain().equals(updateFactor.getDomain()));
        testObserver.assertValue(f -> f.getFactorType() == updateFactor.getFactorType());
        testObserver.assertValue(f -> f.getType().equals(updateFactor.getType()));
    }

    @Test
    public void testDelete() throws TechnicalException {
        // create factor
        Factor factor = buildFactor();
        Factor factorCreated = factorRepository.create_migrated(factor).block();

        // fetch factor
        TestObserver<Factor> testObserver = RxJava2Adapter.monoToMaybe(factorRepository.findById_migrated(factorCreated.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(f -> f.getName().equals(factorCreated.getName()));

        // delete factor
        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(factorRepository.delete_migrated(factorCreated.getId())).test();
        testObserver1.awaitTerminalEvent();

        // fetch factor
        RxJava2Adapter.monoToMaybe(factorRepository.findById_migrated(factorCreated.getId())).test().assertEmpty();
    }

}

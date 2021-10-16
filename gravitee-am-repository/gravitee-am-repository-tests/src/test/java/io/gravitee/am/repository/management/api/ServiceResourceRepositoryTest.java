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

import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.resource.ServiceResource;
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
public class ServiceResourceRepositoryTest extends AbstractManagementTest {

    @Autowired
    private ServiceResourceRepository serviceResourceRepository;

    @Test
    public void testFindByDomain() throws TechnicalException {
        // create res
        ServiceResource resource = buildResource();
        resource.setReferenceId("testDomain");
        ServiceResource resourceCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(serviceResourceRepository.create_migrated(resource))).block();

        // fetch factors
        TestSubscriber<ServiceResource> testDomain = RxJava2Adapter.fluxToFlowable(serviceResourceRepository.findByReference_migrated(ReferenceType.DOMAIN, "testDomain")).test();
        testDomain.awaitTerminalEvent();

        testDomain.assertComplete();
        testDomain.assertNoErrors();
        testDomain.assertValue(f -> f.getId().equals(resourceCreated.getId()));
        testDomain.assertValue(f -> f.getName().equals(resourceCreated.getName()));
        testDomain.assertValue(f -> f.getConfiguration().equals(resourceCreated.getConfiguration()));
        testDomain.assertValue(f -> f.getReferenceId().equals("testDomain"));
        testDomain.assertValue(f -> f.getReferenceType() == resourceCreated.getReferenceType());
        testDomain.assertValue(f -> f.getType().equals(resourceCreated.getType()));
    }

    private ServiceResource buildResource() {
        ServiceResource resource = new ServiceResource();
        String random = UUID.random().toString();
        resource.setName("name"+random);
        resource.setConfiguration("{\"config\": \"" + random +"\"}");
        resource.setType("type"+random);
        resource.setReferenceId("ref"+random);
        resource.setReferenceType(ReferenceType.DOMAIN);
        resource.setCreatedAt(new Date());
        resource.setUpdatedAt(new Date());
        return resource;
    }

    @Test
    public void testFindById() throws TechnicalException {
        // create resource
        ServiceResource resource = buildResource();
        ServiceResource resourceCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(serviceResourceRepository.create_migrated(resource))).block();

        // fetch resource
        TestObserver<ServiceResource> testObserver = RxJava2Adapter.monoToMaybe(serviceResourceRepository.findById_migrated(resourceCreated.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(f -> f.getId().equals(resourceCreated.getId()));
        testObserver.assertValue(f -> f.getName().equals(resourceCreated.getName()));
        testObserver.assertValue(f -> f.getConfiguration().equals(resourceCreated.getConfiguration()));
        testObserver.assertValue(f -> f.getReferenceId().equals(resourceCreated.getReferenceId()));
        testObserver.assertValue(f -> f.getReferenceType() == resourceCreated.getReferenceType());
        testObserver.assertValue(f -> f.getType().equals(resourceCreated.getType()));
    }

    @Test
    public void testNotFoundById() throws TechnicalException {
        RxJava2Adapter.monoToMaybe(serviceResourceRepository.findById_migrated("test")).test().assertEmpty();
    }

    @Test
    public void testCreate() throws TechnicalException {
        ServiceResource resource = buildResource();

        TestObserver<ServiceResource> testObserver = RxJava2Adapter.monoToSingle(serviceResourceRepository.create_migrated(resource)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(f -> f.getId() != null);
        testObserver.assertValue(f -> f.getName().equals(resource.getName()));
        testObserver.assertValue(f -> f.getConfiguration().equals(resource.getConfiguration()));
        testObserver.assertValue(f -> f.getReferenceId().equals(resource.getReferenceId()));
        testObserver.assertValue(f -> f.getReferenceType() == resource.getReferenceType());
        testObserver.assertValue(f -> f.getType().equals(resource.getType()));
    }

    @Test
    public void testUpdate() throws TechnicalException {
        ServiceResource resource = buildResource();
        ServiceResource resourceCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(serviceResourceRepository.create_migrated(resource))).block();

        ServiceResource updateResource = buildResource();
        updateResource.setId(resourceCreated.getId());
        updateResource.setName("testUpdatedName");

        TestObserver<ServiceResource> testObserver = RxJava2Adapter.monoToSingle(serviceResourceRepository.update_migrated(updateResource)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(f -> f.getId().equals(resourceCreated.getId()));
        testObserver.assertValue(f -> f.getName().equals(updateResource.getName()));
        testObserver.assertValue(f -> f.getConfiguration().equals(updateResource.getConfiguration()));
        testObserver.assertValue(f -> f.getReferenceType() == updateResource.getReferenceType());
        testObserver.assertValue(f -> f.getReferenceId().equals(updateResource.getReferenceId()));
        testObserver.assertValue(f -> f.getType().equals(updateResource.getType()));
    }

    @Test
    public void testDelete() throws TechnicalException {
        ServiceResource resource = buildResource();
        ServiceResource resourceCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(serviceResourceRepository.create_migrated(resource))).block();

        TestObserver<ServiceResource> testObserver = RxJava2Adapter.monoToMaybe(serviceResourceRepository.findById_migrated(resourceCreated.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(f -> f.getName().equals(resourceCreated.getName()));

        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(serviceResourceRepository.delete_migrated(resourceCreated.getId())).test();
        testObserver1.awaitTerminalEvent();

        RxJava2Adapter.monoToMaybe(serviceResourceRepository.findById_migrated(resourceCreated.getId())).test().assertEmpty();
    }

}

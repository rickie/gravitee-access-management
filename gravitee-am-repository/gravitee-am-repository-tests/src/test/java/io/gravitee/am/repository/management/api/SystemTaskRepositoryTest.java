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

import io.gravitee.am.model.SystemTask;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.reactivex.observers.TestObserver;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SystemTaskRepositoryTest extends AbstractManagementTest {
    @Autowired
    private SystemTaskRepository taskRepository;

    @Test
    public void testFindById() {
        // create task
        SystemTask task = buildSystemTask();
        SystemTask systemTaskCreated = taskRepository.create_migrated(task).block();

        // fetch task
        TestObserver<SystemTask> testObserver = RxJava2Adapter.monoToMaybe(taskRepository.findById_migrated(systemTaskCreated.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(task, task.getOperationId(), testObserver);
    }

    private void assertEqualsTo(SystemTask scope, String expectedOpId, TestObserver<SystemTask> testObserver) {
        testObserver.assertValue(s -> s.getId().equals(scope.getId()));
        testObserver.assertValue(s -> s.getStatus().equals(scope.getStatus()));
        testObserver.assertValue(s -> s.getType().equals(scope.getType()));
        testObserver.assertValue(s -> s.getOperationId().equals(expectedOpId));
    }

    private SystemTask buildSystemTask() {
        SystemTask task = new SystemTask();
        String rand = UUID.randomUUID().toString();
        task.setId(rand);
        task.setType(rand);
        task.setStatus(rand);
        task.setOperationId(rand);
        task.setCreatedAt(new Date());
        task.setUpdatedAt(new Date());
        return task;
    }

    @Test
    public void testNotFoundById() throws TechnicalException {
        RxJava2Adapter.monoToMaybe(taskRepository.findById_migrated("test")).test().assertEmpty();
    }

    @Test
    public void testUpdateNotImpl() {
        TestObserver<SystemTask> testObserver = RxJava2Adapter.monoToSingle(taskRepository.update_migrated(buildSystemTask())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(IllegalStateException.class);
    }

    @Test
    public void testUpdateIf() {
        SystemTask task = buildSystemTask();
        SystemTask systemTaskCreated = taskRepository.create_migrated(task).block();

        SystemTask updatedSystemTask = buildSystemTask();
        updatedSystemTask.setId(systemTaskCreated.getId());

        TestObserver<SystemTask> testObserver = RxJava2Adapter.monoToSingle(taskRepository.updateIf_migrated(updatedSystemTask, systemTaskCreated.getOperationId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(updatedSystemTask, updatedSystemTask.getOperationId(), testObserver);
    }

    @Test
    public void testUpdateIf_mismatch() {
        SystemTask task = buildSystemTask();
        SystemTask systemTaskCreated = taskRepository.create_migrated(task).block();

        SystemTask updatedSystemTask = buildSystemTask();
        updatedSystemTask.setId(systemTaskCreated.getId());

        TestObserver<SystemTask> testObserver = RxJava2Adapter.monoToSingle(taskRepository.updateIf_migrated(updatedSystemTask, "unknownId")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        // task shouldn't change because operation id wasn't good
        assertEqualsTo(systemTaskCreated, systemTaskCreated.getOperationId(), testObserver);
    }

    @Test
    public void testDelete() {
        SystemTask task = buildSystemTask();
        SystemTask systemTaskCreated = taskRepository.create_migrated(task).block();

        // fetch SystemTask
        TestObserver<SystemTask> testObserver = RxJava2Adapter.monoToMaybe(taskRepository.findById_migrated(systemTaskCreated.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(s -> s.getId().equals(systemTaskCreated.getId()));

        // delete SystemTask
        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(taskRepository.delete_migrated(systemTaskCreated.getId())).test();
        testObserver1.awaitTerminalEvent();

        // fetch SystemTask
        RxJava2Adapter.monoToMaybe(taskRepository.findById_migrated(systemTaskCreated.getId())).test().assertEmpty();
    }

}

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
package io.gravitee.am.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.model.Entrypoint;
import io.gravitee.am.model.Organization;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.reporter.api.audit.model.Audit;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.EntrypointRepository;
import io.gravitee.am.service.exception.EntrypointNotFoundException;
import io.gravitee.am.service.exception.InvalidEntrypointException;
import io.gravitee.am.service.impl.EntrypointServiceImpl;
import io.gravitee.am.service.model.NewEntrypoint;
import io.gravitee.am.service.model.UpdateEntrypoint;



import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class EntrypointServiceTest {

    public static final String ENTRYPOINT_ID = "entrypoint#1";
    public static final String ORGANIZATION_ID = "orga#1";
    public static final String USER_ID = "user#1";

    @Mock
    private EntrypointRepository entrypointRepository;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private AuditService auditService;

    private EntrypointService cut;

    @Before
    public void before() {

        cut = new EntrypointServiceImpl(entrypointRepository, organizationService, auditService);
    }

    @Test
    public void shouldFindById() {

        Entrypoint entrypoint = new Entrypoint();
        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.just(entrypoint));

        TestObserver<Entrypoint> obs = RxJava2Adapter.monoToSingle(cut.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();
        obs.assertValue(entrypoint);
    }

    @Test
    public void shouldFindById_notExistingEntrypoint() {

        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.empty());

        TestObserver<Entrypoint> obs = RxJava2Adapter.monoToSingle(cut.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).test();

        obs.awaitTerminalEvent();
        obs.assertError(EntrypointNotFoundException.class);
    }

    @Test
    public void shouldFindById_technicalException() {

        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver<Entrypoint> obs = RxJava2Adapter.monoToSingle(cut.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).test();

        obs.awaitTerminalEvent();
        obs.assertError(TechnicalException.class);
    }

    @Test
    public void shouldCreateDefaults() {

        Organization organization = new Organization();
        organization.setId(ORGANIZATION_ID);

        when(organizationService.findById_migrated(ORGANIZATION_ID)).thenReturn(Mono.just(organization));
        when(entrypointRepository.create_migrated(any(Entrypoint.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestSubscriber<Entrypoint> obs = RxJava2Adapter.fluxToFlowable(cut.createDefaults_migrated(organization)).test();

        obs.awaitTerminalEvent();
        obs.assertValue(entrypoint -> entrypoint.getId() != null
                && entrypoint.isDefaultEntrypoint() && entrypoint.getOrganizationId().equals(ORGANIZATION_ID));

        verify(auditService, times(1)).report(argThat(builder -> {
            Audit audit = builder.build(new ObjectMapper());
            assertEquals(EventType.ENTRYPOINT_CREATED, audit.getType());
            assertEquals(ReferenceType.ORGANIZATION, audit.getReferenceType());
            assertEquals(ORGANIZATION_ID, audit.getReferenceId());
            assertEquals("system", audit.getActor().getId());

            return true;
        }));
    }

    @Test
    public void shouldCreateDefaultsWithDomainRestrictions() {

        Organization organization = new Organization();
        organization.setId(ORGANIZATION_ID);
        organization.setDomainRestrictions(Arrays.asList("domain1.gravitee.io", "domain2.gravitee.io"));

        when(organizationService.findById_migrated(ORGANIZATION_ID)).thenReturn(Mono.just(organization));
        when(entrypointRepository.create_migrated(argThat(e -> e != null && e.getUrl().equals("https://domain1.gravitee.io") && e.isDefaultEntrypoint()))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(entrypointRepository.create_migrated(argThat(e -> e != null && e.getUrl().equals("https://domain2.gravitee.io") && !e.isDefaultEntrypoint()))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestSubscriber<Entrypoint> obs = RxJava2Adapter.fluxToFlowable(cut.createDefaults_migrated(organization)).test();

        obs.awaitTerminalEvent();
        obs.assertValueAt(0, entrypoint -> entrypoint.getId() != null
                && entrypoint.isDefaultEntrypoint() && entrypoint.getOrganizationId().equals(ORGANIZATION_ID));
        obs.assertValueAt(1, entrypoint -> entrypoint.getId() != null
                && !entrypoint.isDefaultEntrypoint() && entrypoint.getOrganizationId().equals(ORGANIZATION_ID));

        verify(auditService, times(2)).report(argThat(builder -> {
            Audit audit = builder.build(new ObjectMapper());
            assertEquals(EventType.ENTRYPOINT_CREATED, audit.getType());
            assertEquals(ReferenceType.ORGANIZATION, audit.getReferenceType());
            assertEquals(ORGANIZATION_ID, audit.getReferenceId());
            assertEquals("system", audit.getActor().getId());

            return true;
        }));
    }

    @Test
    public void shouldCreate() {

        Organization organization = new Organization();
        organization.setId(ORGANIZATION_ID);

        DefaultUser user = new DefaultUser("test");
        user.setId(USER_ID);

        NewEntrypoint newEntrypoint = new NewEntrypoint();
        newEntrypoint.setName("name");
        newEntrypoint.setDescription("description");
        newEntrypoint.setTags(Arrays.asList("tag#1", "tags#2"));
        newEntrypoint.setUrl("https://auth.company.com");

        when(organizationService.findById_migrated(ORGANIZATION_ID)).thenReturn(Mono.just(organization));
        when(entrypointRepository.create_migrated(any(Entrypoint.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestObserver<Entrypoint> obs = RxJava2Adapter.monoToSingle(cut.create_migrated(ORGANIZATION_ID, newEntrypoint, user)).test();

        obs.awaitTerminalEvent();
        obs.assertValue(entrypoint -> entrypoint.getId() != null
                && !entrypoint.isDefaultEntrypoint()
                && entrypoint.getOrganizationId().equals(ORGANIZATION_ID)
                && entrypoint.getName().equals(newEntrypoint.getName())
                && entrypoint.getDescription().equals(newEntrypoint.getDescription())
                && entrypoint.getTags().equals(newEntrypoint.getTags())
                && entrypoint.getUrl().equals(newEntrypoint.getUrl()));

        verify(auditService, times(1)).report(argThat(builder -> {
            Audit audit = builder.build(new ObjectMapper());
            assertEquals(EventType.ENTRYPOINT_CREATED, audit.getType());
            assertEquals(ReferenceType.ORGANIZATION, audit.getReferenceType());
            assertEquals(ORGANIZATION_ID, audit.getReferenceId());
            assertEquals(user.getId(), audit.getActor().getId());

            return true;
        }));
    }

    @Test
    public void shouldNotCreate_badUrl() {

        DefaultUser user = new DefaultUser("test");
        user.setId(USER_ID);

        NewEntrypoint newEntrypoint = new NewEntrypoint();
        newEntrypoint.setName("name");
        newEntrypoint.setDescription("description");
        newEntrypoint.setTags(Arrays.asList("tag#1", "tags#2"));
        newEntrypoint.setUrl("invalid");

        when(entrypointRepository.create_migrated(any(Entrypoint.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestObserver<Entrypoint> obs = RxJava2Adapter.monoToSingle(cut.create_migrated(ORGANIZATION_ID, newEntrypoint, user)).test();

        obs.awaitTerminalEvent();
        obs.assertError(InvalidEntrypointException.class);

        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldUpdate() {

        DefaultUser user = new DefaultUser("test");
        user.setId(USER_ID);

        Entrypoint existingEntrypoint = new Entrypoint();
        existingEntrypoint.setId(ENTRYPOINT_ID);
        existingEntrypoint.setOrganizationId(ORGANIZATION_ID);

        UpdateEntrypoint updateEntrypoint = new UpdateEntrypoint();
        updateEntrypoint.setName("name");
        updateEntrypoint.setDescription("description");
        updateEntrypoint.setTags(Arrays.asList("tag#1", "tags#2"));
        updateEntrypoint.setUrl("https://auth.company.com");

        when(organizationService.findById_migrated(ORGANIZATION_ID)).thenReturn(Mono.just(new Organization()));
        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.just(existingEntrypoint));
        when(entrypointRepository.update_migrated(any(Entrypoint.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestObserver<Entrypoint> obs = RxJava2Adapter.monoToSingle(cut.update_migrated(ENTRYPOINT_ID, ORGANIZATION_ID, updateEntrypoint, user)).test();

        obs.awaitTerminalEvent();
        obs.assertValue(entrypoint -> entrypoint.getId() != null
                && !entrypoint.isDefaultEntrypoint()
                && entrypoint.getOrganizationId().equals(ORGANIZATION_ID)
                && entrypoint.getName().equals(updateEntrypoint.getName())
                && entrypoint.getDescription().equals(updateEntrypoint.getDescription())
                && entrypoint.getTags().equals(updateEntrypoint.getTags())
                && entrypoint.getUrl().equals(updateEntrypoint.getUrl())
                && entrypoint.getUpdatedAt() != null);

        verify(auditService, times(1)).report(argThat(builder -> {
            Audit audit = builder.build(new ObjectMapper());
            assertEquals(EventType.ENTRYPOINT_UPDATED, audit.getType());
            assertEquals(ReferenceType.ORGANIZATION, audit.getReferenceType());
            assertEquals(ORGANIZATION_ID, audit.getReferenceId());
            assertEquals(user.getId(), audit.getActor().getId());

            return true;
        }));
    }

    @Test
    public void shouldNotUpdate_badUrl() {

        DefaultUser user = new DefaultUser("test");
        user.setId(USER_ID);

        Entrypoint existingEntrypoint = new Entrypoint();
        existingEntrypoint.setId(ENTRYPOINT_ID);
        existingEntrypoint.setOrganizationId(ORGANIZATION_ID);

        UpdateEntrypoint updateEntrypoint = new UpdateEntrypoint();
        updateEntrypoint.setName("name");
        updateEntrypoint.setDescription("description");
        updateEntrypoint.setTags(Arrays.asList("tag#1", "tags#2"));
        updateEntrypoint.setUrl("invalid");

        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.just(existingEntrypoint));
        when(entrypointRepository.update_migrated(any(Entrypoint.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestObserver<Entrypoint> obs = RxJava2Adapter.monoToSingle(cut.update_migrated(ENTRYPOINT_ID, ORGANIZATION_ID, updateEntrypoint, user)).test();

        obs.awaitTerminalEvent();
        obs.assertError(InvalidEntrypointException.class);

        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldNotUpdate_notExistingEntrypoint() {

        DefaultUser user = new DefaultUser("test");
        user.setId(USER_ID);

        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.empty());

        TestObserver<Entrypoint> obs = RxJava2Adapter.monoToSingle(cut.update_migrated(ENTRYPOINT_ID, ORGANIZATION_ID, new UpdateEntrypoint(), user)).test();

        obs.awaitTerminalEvent();
        obs.assertError(EntrypointNotFoundException.class);

        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldUpdateDefault_onlyUrl() {

        DefaultUser user = new DefaultUser("test");
        user.setId(USER_ID);

        Entrypoint existingEntrypoint = new Entrypoint();
        existingEntrypoint.setId(ENTRYPOINT_ID);
        existingEntrypoint.setOrganizationId(ORGANIZATION_ID);
        existingEntrypoint.setName("name");
        existingEntrypoint.setDescription("description");
        existingEntrypoint.setTags(Arrays.asList("tag#1", "tags#2"));
        existingEntrypoint.setUrl("https://current.com");

        UpdateEntrypoint updateEntrypoint = new UpdateEntrypoint();
        updateEntrypoint.setName("name");
        updateEntrypoint.setDescription("description");
        updateEntrypoint.setTags(Arrays.asList("tag#1", "tags#2"));
        updateEntrypoint.setUrl("https://changed.com");

        when(organizationService.findById_migrated(ORGANIZATION_ID)).thenReturn(Mono.just(new Organization()));
        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.just(existingEntrypoint));
        when(entrypointRepository.update_migrated(any(Entrypoint.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestObserver<Entrypoint> obs = RxJava2Adapter.monoToSingle(cut.update_migrated(ENTRYPOINT_ID, ORGANIZATION_ID, updateEntrypoint, user)).test();

        obs.awaitTerminalEvent();
        obs.assertValue(entrypoint -> entrypoint.getId() != null
                && !entrypoint.isDefaultEntrypoint()
                && entrypoint.getOrganizationId().equals(ORGANIZATION_ID)
                && entrypoint.getName().equals(updateEntrypoint.getName())
                && entrypoint.getDescription().equals(updateEntrypoint.getDescription())
                && entrypoint.getTags().equals(updateEntrypoint.getTags())
                && entrypoint.getUrl().equals(updateEntrypoint.getUrl())
                && entrypoint.getUpdatedAt() != null);

        verify(auditService, times(1)).report(argThat(builder -> {
            Audit audit = builder.build(new ObjectMapper());
            assertEquals(EventType.ENTRYPOINT_UPDATED, audit.getType());
            assertEquals(ReferenceType.ORGANIZATION, audit.getReferenceType());
            assertEquals(ORGANIZATION_ID, audit.getReferenceId());
            assertEquals(user.getId(), audit.getActor().getId());

            return true;
        }));
    }

    @Test
    public void shouldNotUpdateDefault_onlyUrl() {

        DefaultUser user = new DefaultUser("test");
        user.setId(USER_ID);

        Entrypoint existingEntrypoint = new Entrypoint();
        existingEntrypoint.setId(ENTRYPOINT_ID);
        existingEntrypoint.setOrganizationId(ORGANIZATION_ID);
        existingEntrypoint.setName("name");
        existingEntrypoint.setDescription("description");
        existingEntrypoint.setTags(Collections.emptyList());
        existingEntrypoint.setUrl("https://current.com");
        existingEntrypoint.setDefaultEntrypoint(true);

        UpdateEntrypoint updateEntrypoint = new UpdateEntrypoint();
        updateEntrypoint.setName("name");
        updateEntrypoint.setDescription("description");
        updateEntrypoint.setTags(Collections.emptyList());
        updateEntrypoint.setUrl("https://changed.com");

        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.just(existingEntrypoint));
        when(entrypointRepository.update_migrated(any(Entrypoint.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        TestObserver<Entrypoint> obs;

        updateEntrypoint.setName("updated");
        obs = RxJava2Adapter.monoToSingle(cut.update_migrated(ENTRYPOINT_ID, ORGANIZATION_ID, updateEntrypoint, user)).test();
        obs.awaitTerminalEvent();
        obs.assertError(InvalidEntrypointException.class);

        updateEntrypoint.setName(existingEntrypoint.getName());
        updateEntrypoint.setDescription("updated");
        obs = RxJava2Adapter.monoToSingle(cut.update_migrated(ENTRYPOINT_ID, ORGANIZATION_ID, updateEntrypoint, user)).test();
        obs.awaitTerminalEvent();
        obs.assertError(InvalidEntrypointException.class);

        updateEntrypoint.setDescription(existingEntrypoint.getDescription());
        updateEntrypoint.setTags(Arrays.asList("updated"));
        obs = RxJava2Adapter.monoToSingle(cut.update_migrated(ENTRYPOINT_ID, ORGANIZATION_ID, updateEntrypoint, user)).test();
        obs.awaitTerminalEvent();
        obs.assertError(InvalidEntrypointException.class);

        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldDelete() {

        DefaultUser user = new DefaultUser("test");
        user.setId(USER_ID);

        Entrypoint existingEntrypoint = new Entrypoint();
        existingEntrypoint.setId(ENTRYPOINT_ID);
        existingEntrypoint.setOrganizationId(ORGANIZATION_ID);

        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.just(existingEntrypoint));
        when(entrypointRepository.delete_migrated(ENTRYPOINT_ID)).thenReturn(Mono.empty());

        TestObserver<Void> obs = RxJava2Adapter.monoToCompletable(cut.delete_migrated(ENTRYPOINT_ID, ORGANIZATION_ID, user)).test();

        obs.awaitTerminalEvent();
        obs.assertComplete();

        verify(auditService, times(1)).report(argThat(builder -> {
            Audit audit = builder.build(new ObjectMapper());
            assertEquals(EventType.ENTRYPOINT_DELETED, audit.getType());
            assertEquals(ReferenceType.ORGANIZATION, audit.getReferenceType());
            assertEquals(ORGANIZATION_ID, audit.getReferenceId());
            assertEquals(user.getId(), audit.getActor().getId());

            return true;
        }));
    }


    @Test
    public void shouldNotDelete_notExistingEntrypoint() {

        DefaultUser user = new DefaultUser("test");
        user.setId(USER_ID);

        when(entrypointRepository.findById_migrated(ENTRYPOINT_ID, ORGANIZATION_ID)).thenReturn(Mono.empty());

        TestObserver<Void> obs = RxJava2Adapter.monoToCompletable(cut.delete_migrated(ENTRYPOINT_ID, ORGANIZATION_ID, user)).test();

        obs.awaitTerminalEvent();
        obs.assertError(EntrypointNotFoundException.class);

        verifyZeroInteractions(auditService);
    }
}

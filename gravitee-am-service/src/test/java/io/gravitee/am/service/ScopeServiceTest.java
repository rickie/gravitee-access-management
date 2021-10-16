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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.Application;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.application.ApplicationOAuthSettings;
import io.gravitee.am.model.application.ApplicationScopeSettings;
import io.gravitee.am.model.application.ApplicationSettings;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.ScopeRepository;
import io.gravitee.am.repository.oauth2.api.ScopeApprovalRepository;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.impl.ScopeServiceImpl;
import io.gravitee.am.service.model.*;




import io.reactivex.observers.TestObserver;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ScopeServiceTest {

    @InjectMocks
    private ScopeService scopeService = new ScopeServiceImpl();

    @Mock
    private RoleService roleService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ScopeRepository scopeRepository;

    @Mock
    private ScopeApprovalRepository scopeApprovalRepository;

    @Mock
    private EventService eventService;

    @Mock
    private AuditService auditService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.just(new Scope()));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(scopeService.findById_migrated("my-scope")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingScope() {
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(scopeService.findById_migrated("my-scope")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(scopeService.findById_migrated("my-scope")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomain() {
        when(scopeRepository.findByDomain_migrated(DOMAIN, 0, Integer.MAX_VALUE)).thenReturn(Mono.just(new Page<>(Collections.singleton(new Scope()),0,1)));
        TestObserver<Page<Scope>> testObserver = RxJava2Adapter.monoToSingle(scopeService.findByDomain_migrated(DOMAIN, 0, Integer.MAX_VALUE)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(scopes -> scopes.getData().size() == 1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(scopeRepository.findByDomain_migrated(DOMAIN, 0, 1)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(scopeService.findByDomain_migrated(DOMAIN, 0, 1)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomainAndKey_technicalException() {
        when(scopeRepository.findByDomainAndKey_migrated(DOMAIN, "my-scope")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver<Scope> testObserver = RxJava2Adapter.monoToMaybe(scopeService.findByDomainAndKey_migrated(DOMAIN, "my-scope")).test();
        testObserver.assertNotComplete().assertError(TechnicalManagementException.class);
    }

    @Test
    public void shouldFindByDomainAndKey() {
        when(scopeRepository.findByDomainAndKey_migrated(DOMAIN, "my-scope")).thenReturn(Mono.just(new Scope()));
        TestObserver<Scope> testObserver = RxJava2Adapter.monoToMaybe(scopeService.findByDomainAndKey_migrated(DOMAIN, "my-scope")).test();
        testObserver.assertComplete().assertNoErrors().assertValue(Objects::nonNull);
    }

    @Test
    public void shouldFindByDomainAndKeys_nullInput() {
        TestObserver<List<Scope>> testObserver = RxJava2Adapter.monoToSingle(scopeService.findByDomainAndKeys_migrated(DOMAIN, null)).test();
        testObserver.assertComplete().assertNoErrors().assertValue(List::isEmpty);
    }

    @Test
    public void shouldFindByDomainAndKeys_emptyInput() {
        TestObserver<List<Scope>> testObserver = RxJava2Adapter.monoToSingle(scopeService.findByDomainAndKeys_migrated(DOMAIN, Collections.emptyList())).test();
        testObserver.assertComplete().assertNoErrors().assertValue(List::isEmpty);
    }

    @Test
    public void shouldFindByDomainAndKeys_technicalException() {
        List<String> searchingScopes = Arrays.asList("a","b");
        when(scopeRepository.findByDomainAndKeys_migrated(DOMAIN, searchingScopes)).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver<List<Scope>> testObserver = RxJava2Adapter.monoToSingle(scopeService.findByDomainAndKeys_migrated(DOMAIN, searchingScopes)).test();
        testObserver.assertNotComplete().assertError(TechnicalManagementException.class);
    }

    @Test
    public void shouldFindByDomainAndKeys() {
        List<String> searchingScopes = Arrays.asList("a","b");
        when(scopeRepository.findByDomainAndKeys_migrated(DOMAIN, searchingScopes)).thenReturn(Flux.just(new Scope()));
        TestObserver<List<Scope>> testObserver = RxJava2Adapter.monoToSingle(scopeService.findByDomainAndKeys_migrated(DOMAIN, searchingScopes)).test();
        testObserver.assertComplete().assertNoErrors().assertValue(scopes -> scopes.size()==1);
    }

    @Test
    public void shouldCreate() {
        NewScope newScope = Mockito.mock(NewScope.class);
        when(newScope.getKey()).thenReturn("my-scope");
        when(newScope.getIconUri()).thenReturn("https://gravitee.io/icon");
        when(scopeRepository.findByDomainAndKey_migrated(DOMAIN, "my-scope")).thenReturn(Mono.empty());
        when(scopeRepository.create_migrated(any(Scope.class))).thenReturn(Mono.just(new Scope()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.create_migrated(DOMAIN, newScope)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(scopeRepository, times(1)).findByDomainAndKey_migrated(anyString(), anyString());
        verify(scopeRepository, times(1)).create_migrated(any(Scope.class));
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldCreate_keyUpperCase() {
        NewScope newScope = Mockito.mock(NewScope.class);
        when(newScope.getKey()).thenReturn("MY-SCOPE");
        when(scopeRepository.findByDomainAndKey_migrated(DOMAIN, "MY-SCOPE")).thenReturn(Mono.empty());
        when(scopeRepository.create_migrated(any(Scope.class))).thenReturn(Mono.just(new Scope()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.create_migrated(DOMAIN, newScope)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(scopeRepository, times(1)).create_migrated(any(Scope.class));
        verify(scopeRepository, times(1)).create_migrated(argThat(new ArgumentMatcher<Scope>() {
            @Override
            public boolean matches(Scope scope) {
                return scope.getKey().equals("MY-SCOPE");
            }
        }));
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldCreate_whiteSpaces() {
        NewScope newScope = Mockito.mock(NewScope.class);
        when(newScope.getKey()).thenReturn("MY scope");
        when(scopeRepository.findByDomainAndKey_migrated(DOMAIN, "MY_scope")).thenReturn(Mono.empty());
        when(scopeRepository.create_migrated(any(Scope.class))).thenReturn(Mono.just(new Scope()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.create_migrated(DOMAIN, newScope)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(scopeRepository, times(1)).create_migrated(any(Scope.class));
        verify(scopeRepository, times(1)).create_migrated(argThat(new ArgumentMatcher<Scope>() {
            @Override
            public boolean matches(Scope scope) {
                return scope.getKey().equals("MY_scope");
            }
        }));
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldNotCreate_malformedIconUri() {
        NewScope newScope = Mockito.mock(NewScope.class);
        when(newScope.getKey()).thenReturn("my-scope");
        when(newScope.getIconUri()).thenReturn("malformedIconUri");
        when(scopeRepository.findByDomainAndKey_migrated(DOMAIN, "my-scope")).thenReturn(Mono.empty());

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(scopeService.create_migrated(DOMAIN, newScope)).subscribe(testObserver);

        testObserver.assertError(MalformedIconUriException.class);
        testObserver.assertNotComplete();

        verify(scopeRepository, times(1)).findByDomainAndKey_migrated(DOMAIN,"my-scope");
        verify(scopeRepository, never()).create_migrated(any(Scope.class));
    }

    @Test
    public void shouldNotCreate_technicalException() {
        NewScope newScope = Mockito.mock(NewScope.class);
        when(newScope.getKey()).thenReturn("my-scope");
        when(scopeRepository.findByDomainAndKey_migrated(DOMAIN, "my-scope")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(scopeService.create_migrated(DOMAIN, newScope)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(scopeRepository, never()).create_migrated(any(Scope.class));
    }

    @Test
    public void shouldNotCreate_existingScope() {
        NewScope newScope = Mockito.mock(NewScope.class);
        when(newScope.getKey()).thenReturn("my-scope");
        when(scopeRepository.findByDomainAndKey_migrated(DOMAIN, "my-scope")).thenReturn(Mono.just(new Scope()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(scopeService.create_migrated(DOMAIN, newScope)).subscribe(testObserver);

        testObserver.assertError(ScopeAlreadyExistsException.class);
        testObserver.assertNotComplete();

        verify(scopeRepository, never()).create_migrated(any(Scope.class));
    }

    @Test
    public void shouldPatch_systemScope_discoveryNotReplaced() {
        PatchScope patch = new PatchScope();
        patch.setDiscovery(Optional.of(true));
        patch.setName(Optional.of("name"));

        final String scopeId = "toPatchId";

        Scope toPatch = new Scope();
        toPatch.setId(scopeId);
        toPatch.setSystem(true);
        toPatch.setDiscovery(false);
        toPatch.setName("oldName");
        toPatch.setDescription("oldDescription");

        ArgumentCaptor<Scope> argument = ArgumentCaptor.forClass(Scope.class);

        when(scopeRepository.findById_migrated(scopeId)).thenReturn(Mono.just(toPatch));
        when(scopeRepository.update_migrated(argument.capture())).thenReturn(Mono.just(new Scope()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.patch_migrated(DOMAIN, scopeId, patch)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(scopeRepository, times(1)).update_migrated(any(Scope.class));
        assertNotNull(argument.getValue());
        assertEquals("name",argument.getValue().getName());
        assertEquals("oldDescription",argument.getValue().getDescription());
        assertFalse(argument.getValue().isDiscovery());
    }

    @Test
    public void shouldPatch_nonSystemScope_discoveryNotReplaced() {
        PatchScope patch = new PatchScope();
        patch.setDiscovery(Optional.of(true));
        patch.setName(Optional.of("name"));

        final String scopeId = "toPatchId";

        Scope toPatch = new Scope();
        toPatch.setId(scopeId);
        toPatch.setSystem(false);
        toPatch.setDiscovery(false);
        toPatch.setName("oldName");
        toPatch.setDescription("oldDescription");

        ArgumentCaptor<Scope> argument = ArgumentCaptor.forClass(Scope.class);

        when(scopeRepository.findById_migrated(scopeId)).thenReturn(Mono.just(toPatch));
        when(scopeRepository.update_migrated(argument.capture())).thenReturn(Mono.just(new Scope()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.patch_migrated(DOMAIN, scopeId, patch)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(scopeRepository, times(1)).update_migrated(any(Scope.class));
        assertNotNull(argument.getValue());
        assertEquals("name",argument.getValue().getName());
        assertEquals("oldDescription",argument.getValue().getDescription());
        assertTrue(argument.getValue().isDiscovery());
    }

    @Test
    public void shouldNotPatch() {
        Scope toPatch = new Scope();
        toPatch.setId("toPatchId");

        when(scopeRepository.findById_migrated("toPatchId")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.patch_migrated(DOMAIN, "toPatchId", new PatchScope())).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldNotPatch_scopeNotFound() {
        PatchScope patchScope = new PatchScope();
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.empty());

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(scopeService.patch_migrated(DOMAIN, "my-scope", patchScope)).subscribe(testObserver);

        testObserver.assertError(ScopeNotFoundException.class);
        testObserver.assertNotComplete();

        verify(scopeRepository, times(1)).findById_migrated("my-scope");
        verify(scopeRepository, never()).update_migrated(any(Scope.class));
    }

    @Test
    public void shouldNotPatch_malformedIconUri() {
        PatchScope patchScope = new PatchScope();
        patchScope.setIconUri(Optional.of("malformedIconUri"));
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.just(new Scope()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(scopeService.patch_migrated(DOMAIN, "my-scope", patchScope)).subscribe(testObserver);

        testObserver.assertError(MalformedIconUriException.class);
        testObserver.assertNotComplete();

        verify(scopeRepository, times(1)).findById_migrated("my-scope");
        verify(scopeRepository, never()).update_migrated(any(Scope.class));
    }

    @Test
    public void shouldUpdate_systemScope_discoveryNotReplaced() {
        UpdateScope updateScope = new UpdateScope();
        updateScope.setDiscovery(true);
        updateScope.setName("name");

        final String scopeId = "toUpdateId";

        Scope toUpdate = new Scope();
        toUpdate.setId(scopeId);
        toUpdate.setSystem(true);
        toUpdate.setDiscovery(false);
        toUpdate.setName("oldName");
        toUpdate.setDescription("oldDescription");

        ArgumentCaptor<Scope> argument = ArgumentCaptor.forClass(Scope.class);

        when(scopeRepository.findById_migrated(scopeId)).thenReturn(Mono.just(toUpdate));
        when(scopeRepository.update_migrated(argument.capture())).thenReturn(Mono.just(new Scope()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.update_migrated(DOMAIN, scopeId, updateScope)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(scopeRepository, times(1)).update_migrated(any(Scope.class));
        assertNotNull(argument.getValue());
        assertEquals("name",argument.getValue().getName());
        assertNull(argument.getValue().getDescription());
        assertFalse(argument.getValue().isDiscovery());
    }

    @Test
    public void shouldUpdate_nonSystemScope_discoveryNotReplaced() {
        UpdateScope updateScope = new UpdateScope();
        updateScope.setName("name");

        final String scopeId = "toUpdateId";

        Scope toUpdate = new Scope();
        toUpdate.setId(scopeId);
        toUpdate.setSystem(false);
        toUpdate.setDiscovery(true);
        toUpdate.setName("oldName");
        toUpdate.setDescription("oldDescription");

        ArgumentCaptor<Scope> argument = ArgumentCaptor.forClass(Scope.class);

        when(scopeRepository.findById_migrated(scopeId)).thenReturn(Mono.just(toUpdate));
        when(scopeRepository.update_migrated(argument.capture())).thenReturn(Mono.just(new Scope()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.update_migrated(DOMAIN, scopeId, updateScope)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(scopeRepository, times(1)).update_migrated(any(Scope.class));
        assertNotNull(argument.getValue());
        assertEquals("name",argument.getValue().getName());
        assertNull(argument.getValue().getDescription());
        assertTrue(argument.getValue().isDiscovery());
    }

    @Test
    public void shouldUpdate_nonSystemScope_discoveryReplaced() {
        UpdateScope updateScope = new UpdateScope();
        updateScope.setDiscovery(true);
        updateScope.setName("name");

        final String scopeId = "toUpdateId";

        Scope toUpdate = new Scope();
        toUpdate.setId(scopeId);
        toUpdate.setSystem(false);
        toUpdate.setDiscovery(false);
        toUpdate.setName("oldName");
        toUpdate.setDescription("oldDescription");

        ArgumentCaptor<Scope> argument = ArgumentCaptor.forClass(Scope.class);

        when(scopeRepository.findById_migrated(scopeId)).thenReturn(Mono.just(toUpdate));
        when(scopeRepository.update_migrated(argument.capture())).thenReturn(Mono.just(new Scope()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.update_migrated(DOMAIN, scopeId, updateScope)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(scopeRepository, times(1)).update_migrated(any(Scope.class));
        assertNotNull(argument.getValue());
        assertEquals("name",argument.getValue().getName());
        assertNull(argument.getValue().getDescription());
        assertTrue(argument.getValue().isDiscovery());
    }

    @Test
    public void shouldNotUpdate() {
        when(scopeRepository.findById_migrated("toUpdateId")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.update_migrated(DOMAIN, "toUpdateId", new UpdateScope())).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldNotUpdate_malformedIconUri() {
        UpdateScope updateScope = new UpdateScope();
        updateScope.setIconUri("malformedIconUri");

        when(scopeRepository.findById_migrated("toUpdateId")).thenReturn(Mono.just(new Scope()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(scopeService.update_migrated(DOMAIN, "toUpdateId", updateScope)).subscribe(testObserver);

        testObserver.assertError(MalformedIconUriException.class);
        testObserver.assertNotComplete();

        verify(scopeRepository, times(1)).findById_migrated("toUpdateId");
        verify(scopeRepository, never()).update_migrated(any(Scope.class));
    }

    @Test
    public void shouldUpdateSystemScope() {
        UpdateSystemScope updateScope = new UpdateSystemScope();
        updateScope.setDiscovery(true);
        updateScope.setName("name");

        final String scopeId = "toUpdateId";

        Scope toUpdate = new Scope();
        toUpdate.setId(scopeId);
        toUpdate.setSystem(true);
        toUpdate.setDiscovery(false);
        toUpdate.setName("oldName");
        toUpdate.setDescription("oldDescription");

        ArgumentCaptor<Scope> argument = ArgumentCaptor.forClass(Scope.class);

        when(scopeRepository.findById_migrated(scopeId)).thenReturn(Mono.just(toUpdate));
        when(scopeRepository.update_migrated(argument.capture())).thenReturn(Mono.just(new Scope()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.update_migrated(DOMAIN, scopeId, updateScope)).test();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(scopeRepository, times(1)).update_migrated(any(Scope.class));
        assertNotNull(argument.getValue());
        assertEquals("name",argument.getValue().getName());
        assertNull(argument.getValue().getDescription());
        assertTrue(argument.getValue().isDiscovery());
    }

    @Test
    public void shouldNotUpdateSystemScope() {
        Scope toUpdate = new Scope();
        toUpdate.setId("toUpdateId");

        when(scopeRepository.findById_migrated("toUpdateId")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(scopeService.update_migrated(DOMAIN, "toUpdateId", new UpdateSystemScope())).test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_notExistingScope() {
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.empty());

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToCompletable(scopeService.delete_migrated("my-scope", false)).subscribe(testObserver);

        testObserver.assertError(ScopeNotFoundException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_technicalException() {
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToCompletable(scopeService.delete_migrated("my-scope", false)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete2_technicalException() {
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.just(new Scope()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToCompletable(scopeService.delete_migrated("my-scope", false)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete3_technicalException() {
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.just(new Scope()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToCompletable(scopeService.delete_migrated("my-scope", false)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_light() {
        Scope scope = mock(Scope.class);
        when(scope.getDomain()).thenReturn(DOMAIN);
        when(roleService.findByDomain_migrated(DOMAIN)).thenReturn(Mono.just(Collections.emptySet()));
        when(applicationService.findByDomain_migrated(DOMAIN)).thenReturn(Mono.just(Collections.emptySet()));
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.just(scope));
        when(scopeRepository.delete_migrated("my-scope")).thenReturn(Mono.empty());
        when(scopeApprovalRepository.deleteByDomainAndScopeKey_migrated(scope.getDomain(), scope.getKey())).thenReturn(Mono.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(scopeService.delete_migrated("my-scope", false)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(roleService, times(1)).findByDomain_migrated(DOMAIN);
        verify(applicationService, times(1)).findByDomain_migrated(DOMAIN);
        verify(scopeRepository, times(1)).delete_migrated("my-scope");
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldDelete_full() {
        Scope scope = mock(Scope.class);
        when(scope.getDomain()).thenReturn(DOMAIN);
        when(scope.getKey()).thenReturn("my-scope");

        Role role = mock(Role.class);
        when(role.getId()).thenReturn("role-1");
        when(role.getOauthScopes()).thenReturn(new LinkedList<>(Arrays.asList("my-scope")));

        Application application = mock(Application.class);

        ApplicationSettings applicationSettings = mock(ApplicationSettings.class);
        ApplicationOAuthSettings applicationOAuthSettings = mock(ApplicationOAuthSettings.class);
        when(applicationOAuthSettings.getScopeSettings()).thenReturn(Arrays.asList(new ApplicationScopeSettings("my-scope")));
        when(applicationSettings.getOauth()).thenReturn(applicationOAuthSettings);
        when(application.getSettings()).thenReturn(applicationSettings);

        when(roleService.findByDomain_migrated(DOMAIN)).thenReturn(Mono.just(Collections.singleton(role)));
        when(applicationService.findByDomain_migrated(DOMAIN)).thenReturn(Mono.just(Collections.singleton(application)));
        when(roleService.update_migrated(anyString(), anyString(), any(UpdateRole.class))).thenReturn(Mono.just(new Role()));
        when(applicationService.update_migrated(any())).thenReturn(Mono.just(new Application()));
        when(scopeRepository.findById_migrated("my-scope")).thenReturn(Mono.just(scope));
        when(scopeRepository.delete_migrated("my-scope")).thenReturn(Mono.empty());
        when(scopeApprovalRepository.deleteByDomainAndScopeKey_migrated(scope.getDomain(), scope.getKey())).thenReturn(Mono.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(scopeService.delete_migrated("my-scope", false)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(roleService, times(1)).findByDomain_migrated(DOMAIN);
        verify(applicationService, times(1)).findByDomain_migrated(DOMAIN);
        verify(roleService, times(1)).update_migrated(anyString(), anyString(), any(UpdateRole.class));
        verify(applicationService, times(1)).update_migrated(any());
        verify(scopeRepository, times(1)).delete_migrated("my-scope");
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldNotDeleteSystemScope() throws TechnicalException {
        Scope scope = new Scope();
        scope.setKey("scope-key");
        scope.setSystem(true);
        when(scopeRepository.findById_migrated("scope-id")).thenReturn(Mono.just(scope));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(scopeService.delete_migrated("scope-id", false)).test();
        testObserver.assertError(SystemScopeDeleteException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void validateScope_nullList() {
        TestObserver<Boolean> testObserver = RxJava2Adapter.monoToSingle(scopeService.validateScope_migrated(DOMAIN, null)).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(isValid -> isValid);
    }

    @Test
    public void validateScope_unknownScope() {
        when(scopeRepository.findByDomain_migrated(DOMAIN, 0, Integer.MAX_VALUE)).thenReturn(Mono.just(new Page<>(Collections.singleton(new Scope("valid")),0,1)));
        TestObserver<Boolean> testObserver = RxJava2Adapter.monoToSingle(scopeService.validateScope_migrated(DOMAIN, Arrays.asList("unknown"))).test();
        testObserver.assertError(InvalidClientMetadataException.class);
    }

    @Test
    public void validateScope_validScope() {
        when(scopeRepository.findByDomain_migrated(DOMAIN, 0, Integer.MAX_VALUE)).thenReturn(Mono.just(new Page<>(Collections.singleton(new Scope("valid")),0,1)));
        TestObserver<Boolean> testObserver = RxJava2Adapter.monoToSingle(scopeService.validateScope_migrated(DOMAIN, Arrays.asList("valid"))).test();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(isValid -> isValid);
    }
}

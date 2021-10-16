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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.Application;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.model.uma.Resource;
import io.gravitee.am.model.uma.policy.AccessPolicy;
import io.gravitee.am.repository.management.api.AccessPolicyRepository;
import io.gravitee.am.repository.management.api.ResourceRepository;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.impl.ResourceServiceImpl;
import io.gravitee.am.service.model.NewResource;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import io.vertx.core.json.JsonObject;
import java.util.*;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceServiceTest {

    @Mock
    private ResourceRepository repository;

    @Mock
    private AccessPolicyRepository accessPolicyRepository;

    @Mock
    private ScopeService scopeService;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ResourceService service = new ResourceServiceImpl();

    private static final String DOMAIN_ID = "domainId";
    private static final String CLIENT_ID = "clientId";
    private static final String USER_ID = "userId";
    private static final String RESOURCE_ID = "resourceId";
    private static final String POLICY_ID = "policyId";

    @Before
    public void setUp() {
        when(repository.findByDomainAndClientAndUser_migrated(DOMAIN_ID, CLIENT_ID, USER_ID)).thenReturn(Flux.just(new Resource().setId(RESOURCE_ID)));
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.just(new Resource().setId(RESOURCE_ID)));
        when(scopeService.findByDomainAndKeys_migrated(DOMAIN_ID, Arrays.asList("scope"))).thenReturn(Mono.just(Arrays.asList(new Scope("scope"))));
    }

    @Test
    public void delete_nonExistingResource() {
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToCompletable(service.delete_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testObserver.assertError(ResourceNotFoundException.class);
    }

    @Test
    public void delete_existingResource() {
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.just(new Resource().setId(RESOURCE_ID)));
        when(repository.delete_migrated(RESOURCE_ID)).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToCompletable(service.delete_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testObserver.assertComplete().assertNoErrors().assertNoValues();
    }

    @Test
    public void update_nonExistingResource() {
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToSingle(service.update_migrated(new NewResource(), DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testObserver.assertError(ResourceNotFoundException.class);
    }

    @Test
    public void update_scopeMissing() {
        NewResource newResource = new JsonObject("{\"resource_scopes\":[]}").mapTo(NewResource.class);
        Resource exitingRS = new Resource().setId(RESOURCE_ID).setDomain(DOMAIN_ID);
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.just(exitingRS));
        TestObserver<Resource> testObserver = RxJava2Adapter.monoToSingle(service.update_migrated(newResource, DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testObserver.assertError(MissingScopeException.class);
        verify(repository, times(0)).update_migrated(any());
    }

    @Test
    public void update_scopeNotFound() {
        NewResource newResource = new JsonObject("{\"resource_scopes\":[\"scope\"]}").mapTo(NewResource.class);
        Resource exitingRS = new Resource().setId(RESOURCE_ID).setDomain(DOMAIN_ID);
        when(scopeService.findByDomainAndKeys_migrated(DOMAIN_ID, Arrays.asList("scope"))).thenReturn(Mono.just(Collections.emptyList()));
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.just(exitingRS));
        TestObserver<Resource> testObserver = RxJava2Adapter.monoToSingle(service.update_migrated(newResource, DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testObserver.assertError(ScopeNotFoundException.class);
        verify(repository, times(0)).update_migrated(any());
    }

    @Test
    public void update_malformedIconUri() {
        NewResource newResource = new JsonObject("{\"resource_scopes\":[\"scope\"],\"icon_uri\":\"badIconUriFormat\"}").mapTo(NewResource.class);
        Resource exitingRS = new Resource().setId(RESOURCE_ID).setDomain(DOMAIN_ID);
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.just(exitingRS));
        TestObserver<Resource> testObserver = RxJava2Adapter.monoToSingle(service.update_migrated(newResource, DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testObserver.assertError(MalformedIconUriException.class);
        verify(repository, times(0)).update_migrated(any());
    }

    @Test
    public void update_existingResource() {
        NewResource newResource = new JsonObject("{\"resource_scopes\":[\"scope\"],\"icon_uri\":\"https://gravitee.io/icon\"}").mapTo(NewResource.class);
        Resource exitingRS = new Resource().setId(RESOURCE_ID).setDomain(DOMAIN_ID);
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.just(exitingRS));
        when(repository.update_migrated(exitingRS)).thenReturn(Mono.just(exitingRS));
        TestObserver<Resource> testObserver = RxJava2Adapter.monoToSingle(service.update_migrated(newResource, DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testObserver.assertComplete().assertNoErrors().assertValue(this::assertResourceValues);
    }

    @Test
    public void create_success() {
        NewResource newResource = new JsonObject("{\"resource_scopes\":[\"scope\"]}").mapTo(NewResource.class);
        when(repository.create_migrated(any())).thenReturn(Mono.just(new Resource()));
        when(accessPolicyRepository.create_migrated(any())).thenReturn(Mono.just(new AccessPolicy()));
        TestObserver<Resource> testObserver = RxJava2Adapter.monoToSingle(service.create_migrated(newResource, DOMAIN_ID, CLIENT_ID, USER_ID)).test();
        testObserver.assertComplete().assertNoErrors();
        ArgumentCaptor<Resource> rsCaptor = ArgumentCaptor.forClass(Resource.class);
        verify(repository, times(1)).create_migrated(rsCaptor.capture());
        verify(accessPolicyRepository, times(1)).create_migrated(any());
        Assert.assertTrue(this.assertResourceValues(rsCaptor.getValue()));
    }

    @Test
    public void list() {
        TestSubscriber<Resource> testSubscriber = RxJava2Adapter.fluxToFlowable(service.listByDomainAndClientAndUser_migrated(DOMAIN_ID, CLIENT_ID, USER_ID)).test();
        testSubscriber
                .assertNoErrors()
                .assertComplete()
                .assertValue(resourceSet -> resourceSet.getId().equals(RESOURCE_ID));
    }

    @Test
    public void findByDomain_fail() {
        when(repository.findByDomain_migrated(DOMAIN_ID, 0, Integer.MAX_VALUE)).thenReturn(Mono.error(new ArrayIndexOutOfBoundsException()));
        TestObserver<Set<Resource>> testObserver = RxJava2Adapter.monoToSingle(service.findByDomain_migrated(DOMAIN_ID)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(TechnicalManagementException.class);
    }

    @Test
    public void findByDomain_success() {
        when(repository.findByDomain_migrated(DOMAIN_ID, 0, Integer.MAX_VALUE)).thenReturn(Mono.just(new Page<>(Collections.singleton(new Resource()), 0, 1)));
        TestObserver<Set<Resource>> testObserver = RxJava2Adapter.monoToSingle(service.findByDomain_migrated(DOMAIN_ID)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(applications -> applications.size() == 1);
    }

    private boolean assertResourceValues(Resource toValidate) {
        return toValidate!=null &&
                toValidate.getResourceScopes()!=null &&
                toValidate.getResourceScopes().size() == 1 &&
                toValidate.getResourceScopes().get(0).equals("scope") &&
                toValidate.getUpdatedAt() !=null;
    }

    //Testing straightforward CRUD methods
    @Test
    public void findByResources() {
        when(repository.findByResources_migrated(anyList())).thenReturn(Flux.empty());
        TestSubscriber testObserver = RxJava2Adapter.fluxToFlowable(service.findByResources_migrated(Collections.emptyList())).test();
        testObserver.assertComplete().assertNoErrors();
        verify(repository, times(1)).findByResources_migrated(Collections.emptyList());
    }

    @Test
    public void findByDomainAndClient() {
        when(repository.findByDomainAndClient_migrated(DOMAIN_ID, CLIENT_ID, 0, Integer.MAX_VALUE)).thenReturn(Mono.just(new Page<>(Collections.emptyList(), 0, 0)));
        TestObserver testObserver = RxJava2Adapter.monoToSingle(service.findByDomainAndClient_migrated(DOMAIN_ID, CLIENT_ID, 0, Integer.MAX_VALUE)).test();
        testObserver.assertComplete().assertNoErrors();
        verify(repository, times(1)).findByDomainAndClient_migrated(DOMAIN_ID, CLIENT_ID, 0, Integer.MAX_VALUE);
    }

    @Test
    public void findByDomainAndClient_technicalFailure() {
        when(repository.findByDomainAndClient_migrated(DOMAIN_ID, CLIENT_ID, 0, Integer.MAX_VALUE)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(RuntimeException::new)));
        TestObserver testObserver = RxJava2Adapter.monoToSingle(service.findByDomainAndClient_migrated(DOMAIN_ID, CLIENT_ID, 0, Integer.MAX_VALUE)).test();
        testObserver.assertNotComplete().assertError(TechnicalManagementException.class);
        verify(repository, times(1)).findByDomainAndClient_migrated(DOMAIN_ID, CLIENT_ID, 0, Integer.MAX_VALUE);
    }

    @Test
    public void findByDomainAndClientAndResources() {
        when(repository.findByDomainAndClientAndResources_migrated(DOMAIN_ID, CLIENT_ID, Collections.emptyList())).thenReturn(Flux.empty());
        TestSubscriber testSubscriber = RxJava2Adapter.fluxToFlowable(service.findByDomainAndClientAndResources_migrated(DOMAIN_ID, CLIENT_ID, Collections.emptyList())).test();
        testSubscriber.assertComplete().assertNoErrors();
        verify(repository, times(1)).findByDomainAndClientAndResources_migrated(anyString(), anyString(), anyList());
    }

    @Test
    public void findByDomainAndClientResource() {
        when(repository.findByDomainAndClientAndResources_migrated(eq(DOMAIN_ID), eq(CLIENT_ID), anyList())).thenReturn(Flux.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(service.findByDomainAndClientResource_migrated(DOMAIN_ID, CLIENT_ID, RESOURCE_ID)).test();
        testObserver.assertComplete().assertNoErrors();
        verify(repository, times(1)).findByDomainAndClientAndResources_migrated(eq(DOMAIN_ID), eq(CLIENT_ID), anyList());
    }

    @Test
    public void update() {
        Date now = new Date(System.currentTimeMillis()-1000);
        Resource toUpdate = new Resource().setId(RESOURCE_ID).setDomain(DOMAIN_ID).setUpdatedAt(now);
        when(repository.update_migrated(toUpdate)).thenReturn(Mono.just(toUpdate));
        TestObserver<Resource> testObserver = RxJava2Adapter.monoToSingle(service.update_migrated(toUpdate)).test();
        testObserver.assertComplete().assertNoErrors();
        ArgumentCaptor<Resource> rsCaptor = ArgumentCaptor.forClass(Resource.class);
        verify(repository, times(1)).update_migrated(rsCaptor.capture());
        Assert.assertTrue(rsCaptor.getValue().getUpdatedAt().after(now));
    }

    @Test
    public void delete() {
        Resource toDelete = new Resource().setId(RESOURCE_ID).setDomain(DOMAIN_ID);
        when(accessPolicyRepository.findByDomainAndResource_migrated(toDelete.getDomain(), toDelete.getId())).thenReturn(Flux.empty());
        when(repository.delete_migrated(RESOURCE_ID)).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToCompletable(service.delete_migrated(toDelete)).test();
        testObserver.assertComplete().assertNoErrors();
        verify(repository, times(1)).delete_migrated(RESOURCE_ID);
    }

    @Test
    public void findAccessPolicies() {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId("policy-id");
        accessPolicy.setResource(RESOURCE_ID);
        accessPolicy.setDomain(DOMAIN_ID);
        when(accessPolicyRepository.findByDomainAndResource_migrated(DOMAIN_ID, RESOURCE_ID)).thenReturn(Flux.just(accessPolicy));
        TestObserver<List<AccessPolicy>> testObserver = RxJava2Adapter.monoToSingle(service.findAccessPolicies_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID).collectList()).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(accessPolicies -> accessPolicies.size() == 1);
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, times(1)).findByDomainAndResource_migrated(DOMAIN_ID, RESOURCE_ID);
    }

    @Test
    public void findAccessPolicies_resourceNotFound() {
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.empty());
        TestSubscriber<AccessPolicy> testSubscriber = RxJava2Adapter.fluxToFlowable(service.findAccessPolicies_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testSubscriber.assertNotComplete().assertError(ResourceNotFoundException.class);
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, never()).findByDomainAndResource_migrated(DOMAIN_ID, RESOURCE_ID);
    }

    @Test
    public void findAccessPolicies_technicalFailure() {
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(RuntimeException::new)));
        TestSubscriber<AccessPolicy> testSubscriber = RxJava2Adapter.fluxToFlowable(service.findAccessPolicies_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testSubscriber.assertNotComplete().assertError(TechnicalManagementException.class);
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, never()).findByDomainAndResource_migrated(DOMAIN_ID, RESOURCE_ID);
    }

    @Test
    public void findAccessPoliciesByResources() {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId(POLICY_ID);
        accessPolicy.setResource(RESOURCE_ID);
        accessPolicy.setDomain(DOMAIN_ID);
        List<String> resourceIds = Collections.singletonList(RESOURCE_ID);
        when(accessPolicyRepository.findByResources_migrated(resourceIds)).thenReturn(Flux.just(accessPolicy));
        TestObserver<List<AccessPolicy>> testObserver = RxJava2Adapter.monoToSingle(service.findAccessPoliciesByResources_migrated(resourceIds).collectList()).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(accessPolicies -> accessPolicies.size() == 1);
        verify(accessPolicyRepository, times(1)).findByResources_migrated(resourceIds);
    }

    @Test
    public void findAccessPoliciesByResources_technicalFailure() {
        List<String> resourceIds = Collections.singletonList(RESOURCE_ID);
        when(accessPolicyRepository.findByResources_migrated(resourceIds)).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(RuntimeException::new)));
        TestSubscriber<AccessPolicy> testObserver = RxJava2Adapter.fluxToFlowable(service.findAccessPoliciesByResources_migrated(resourceIds)).test();
        testObserver.assertNotComplete().assertError(TechnicalManagementException.class);
        verify(accessPolicyRepository, times(1)).findByResources_migrated(resourceIds);
    }

    @Test
    public void countAccessPolicyByResource() {
        when(accessPolicyRepository.countByResource_migrated(RESOURCE_ID)).thenReturn(Mono.just(1l));
        TestObserver<Long> testObserver = RxJava2Adapter.monoToSingle(service.countAccessPolicyByResource_migrated(RESOURCE_ID)).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(accessPolicies -> accessPolicies == 1l);
        verify(accessPolicyRepository, times(1)).countByResource_migrated(RESOURCE_ID);
    }

    @Test
    public void countAccessPolicyByResource_technicalFailure() {
        when(accessPolicyRepository.countByResource_migrated(RESOURCE_ID)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(RuntimeException::new)));
        TestObserver<Long> testObserver = RxJava2Adapter.monoToSingle(service.countAccessPolicyByResource_migrated(RESOURCE_ID)).test();
        testObserver.assertNotComplete().assertError(TechnicalManagementException.class);
        verify(accessPolicyRepository, times(1)).countByResource_migrated(RESOURCE_ID);
    }

    @Test
    public void findAccessPolicy() {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId(POLICY_ID);
        accessPolicy.setResource(RESOURCE_ID);
        accessPolicy.setDomain(DOMAIN_ID);
        when(accessPolicyRepository.findById_migrated(POLICY_ID)).thenReturn(Mono.just(accessPolicy));
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToMaybe(service.findAccessPolicy_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID, POLICY_ID)).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(accessPolicy1 -> accessPolicy1.getId().equals(POLICY_ID));
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, times(1)).findById_migrated(POLICY_ID);
    }

    @Test
    public void findAccessPolicy_resourceNotFound() {
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.empty());
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToMaybe(service.findAccessPolicy_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID, POLICY_ID)).test();
        testObserver.assertNotComplete().assertError(ResourceNotFoundException.class);
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, never()).findById_migrated(POLICY_ID);
    }

    @Test
    public void findAccessPolicy_technicalFailure() {
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(RuntimeException::new)));
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToMaybe(service.findAccessPolicy_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID, POLICY_ID)).test();
        testObserver.assertNotComplete().assertError(TechnicalManagementException.class);
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, never()).findById_migrated(POLICY_ID);
    }

    @Test
    public void findAccessPolicy_byId() {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId(POLICY_ID);
        accessPolicy.setResource(RESOURCE_ID);
        accessPolicy.setDomain(DOMAIN_ID);
        when(accessPolicyRepository.findById_migrated(POLICY_ID)).thenReturn(Mono.just(accessPolicy));
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToMaybe(service.findAccessPolicy_migrated(POLICY_ID)).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(accessPolicy1 -> accessPolicy1.getId().equals(POLICY_ID));
        verify(accessPolicyRepository, times(1)).findById_migrated(POLICY_ID);
    }

    @Test
    public void findAccessPolicy_byId_technicalFailure() {
        when(accessPolicyRepository.findById_migrated(POLICY_ID)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(RuntimeException::new)));
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToMaybe(service.findAccessPolicy_migrated(POLICY_ID)).test();
        testObserver.assertNotComplete().assertError(TechnicalManagementException.class);
        verify(accessPolicyRepository, times(1)).findById_migrated(POLICY_ID);
    }

    @Test
    public void createAccessPolicy() {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId(POLICY_ID);
        accessPolicy.setResource(RESOURCE_ID);
        accessPolicy.setDomain(DOMAIN_ID);
        when(accessPolicyRepository.create_migrated(accessPolicy)).thenReturn(Mono.just(accessPolicy));
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToSingle(service.createAccessPolicy_migrated(accessPolicy, DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(accessPolicy1 -> accessPolicy1.getId().equals(POLICY_ID));
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, times(1)).create_migrated(accessPolicy);
    }

    @Test
    public void createAccessPolicy_resourceNotFound() {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId(POLICY_ID);
        accessPolicy.setResource(RESOURCE_ID);
        accessPolicy.setDomain(DOMAIN_ID);
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.empty());
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToSingle(service.createAccessPolicy_migrated(accessPolicy, DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).test();
        testObserver.assertNotComplete().assertError(ResourceNotFoundException.class);
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, never()).create_migrated(accessPolicy);
    }

    @Test
    public void updateAccessPolicy() {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId(POLICY_ID);
        accessPolicy.setResource(RESOURCE_ID);
        accessPolicy.setDomain(DOMAIN_ID);
        when(accessPolicyRepository.findById_migrated(POLICY_ID)).thenReturn(Mono.just(accessPolicy));
        when(accessPolicyRepository.update_migrated(any())).thenReturn(Mono.just(accessPolicy));
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToSingle(service.updateAccessPolicy_migrated(accessPolicy, DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID, POLICY_ID)).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(accessPolicy1 -> accessPolicy1.getId().equals(POLICY_ID));
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, times(1)).findById_migrated(POLICY_ID);
        verify(accessPolicyRepository, times(1)).update_migrated(any());
    }

    @Test
    public void updateAccessPolicy_resourceNotFound() {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId(POLICY_ID);
        accessPolicy.setResource(RESOURCE_ID);
        accessPolicy.setDomain(DOMAIN_ID);
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.empty());
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToSingle(service.updateAccessPolicy_migrated(accessPolicy, DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID, POLICY_ID)).test();
        testObserver.assertNotComplete().assertError(ResourceNotFoundException.class);
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, never()).findById_migrated(POLICY_ID);
        verify(accessPolicyRepository, never()).update_migrated(any());
    }

    @Test
    public void updateAccessPolicy_policyNotFound() {
        AccessPolicy accessPolicy = new AccessPolicy();
        accessPolicy.setId(POLICY_ID);
        accessPolicy.setResource(RESOURCE_ID);
        accessPolicy.setDomain(DOMAIN_ID);
        when(accessPolicyRepository.findById_migrated(POLICY_ID)).thenReturn(Mono.empty());
        TestObserver<AccessPolicy> testObserver = RxJava2Adapter.monoToSingle(service.updateAccessPolicy_migrated(accessPolicy, DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID, POLICY_ID)).test();
        testObserver.assertNotComplete().assertError(AccessPolicyNotFoundException.class);
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, times(1)).findById_migrated(POLICY_ID);
        verify(accessPolicyRepository, never()).update_migrated(any());
    }

    @Test
    public void deleteAccessPolicy() {
        when(accessPolicyRepository.delete_migrated(POLICY_ID)).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToCompletable(service.deleteAccessPolicy_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID, POLICY_ID)).test();
        testObserver.assertComplete().assertNoErrors();
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, times(1)).delete_migrated(POLICY_ID);
    }

    @Test
    public void deleteAccessPolicy_resourceNotFound() {
        when(repository.findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID)).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToCompletable(service.deleteAccessPolicy_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID, POLICY_ID)).test();
        testObserver.assertNotComplete().assertError(ResourceNotFoundException.class);
        verify(repository, times(1)).findByDomainAndClientAndUserAndResource_migrated(DOMAIN_ID, CLIENT_ID, USER_ID, RESOURCE_ID);
        verify(accessPolicyRepository, never()).delete_migrated(POLICY_ID);
    }

    @Test
    public void getMetadata_noResources_null() {
        TestObserver<Map<String, Map<String, Object>>> testObserver = RxJava2Adapter.monoToSingle(service.getMetadata_migrated(null)).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(Map::isEmpty);
    }

    @Test
    public void getMetadata_noResources_empty() {
        TestObserver<Map<String, Map<String, Object>>> testObserver = RxJava2Adapter.monoToSingle(service.getMetadata_migrated(Collections.emptyList())).test();
        testObserver.assertComplete().assertNoErrors();
        testObserver.assertValue(Map::isEmpty);
    }

    @Test
    public void getMetadata_resources() {
        Resource resource = new Resource();
        resource.setDomain(DOMAIN_ID);
        resource.setClientId(CLIENT_ID);
        resource.setUserId(USER_ID);
        List<Resource> resources = Collections.singletonList(resource);

        when(userService.findByIdIn_migrated(anyList())).thenReturn(Flux.just(new User()));
        when(applicationService.findByIdIn_migrated(anyList())).thenReturn(Flux.just(new Application()));
        TestObserver<Map<String, Map<String, Object>>> testObserver = RxJava2Adapter.monoToSingle(service.getMetadata_migrated(resources)).test();
        testObserver.assertComplete().assertNoErrors();
    }
}

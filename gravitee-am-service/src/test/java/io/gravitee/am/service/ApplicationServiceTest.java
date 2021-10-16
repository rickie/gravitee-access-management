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

import io.gravitee.am.common.jwt.Claims;
import io.gravitee.am.common.oauth2.ClientType;
import io.gravitee.am.common.oauth2.GrantType;
import io.gravitee.am.common.oidc.ClientAuthenticationMethod;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.model.*;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.account.FormField;
import io.gravitee.am.model.application.ApplicationOAuthSettings;
import io.gravitee.am.model.application.ApplicationSettings;
import io.gravitee.am.model.application.ApplicationType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.model.permissions.SystemRole;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.ApplicationRepository;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.impl.ApplicationServiceImpl;
import io.gravitee.am.service.model.NewApplication;
import io.gravitee.am.service.model.PatchApplication;
import io.gravitee.am.service.model.PatchApplicationOAuthSettings;
import io.gravitee.am.service.model.PatchApplicationSettings;




import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationServiceTest {

    public static final String ORGANIZATION_ID = "DEFAULT";

    @InjectMocks
    private final ApplicationService applicationService = new ApplicationServiceImpl();

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationTemplateManager applicationTemplateManager;

    @Mock
    private DomainService domainService;

    @Mock
    private EventService eventService;

    @Mock
    private ScopeService scopeService;

    @Mock
    private IdentityProviderService identityProviderService;

    @Mock
    private FormService formService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private AuditService auditService;

    @Mock
    private MembershipService membershipService;

    @Mock
    private RoleService roleService;

    @Mock
    private CertificateService certificateService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(applicationService.findById_migrated("my-client")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingClient() {
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(applicationService.findById_migrated("my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(applicationService.findById_migrated("my-client")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomainAndClientId() {
        when(applicationRepository.findByDomainAndClientId_migrated(DOMAIN, "my-client")).thenReturn(Mono.just(new Application()));
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(applicationService.findByDomainAndClientId_migrated(DOMAIN, "my-client")).test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindByDomainAndClientId_noApp() {
        when(applicationRepository.findByDomainAndClientId_migrated(DOMAIN, "my-client")).thenReturn(Mono.empty());
        TestObserver testObserver = RxJava2Adapter.monoToMaybe(applicationService.findByDomainAndClientId_migrated(DOMAIN, "my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindByDomainAndClientId_technicalException() {
        when(applicationRepository.findByDomainAndClientId_migrated(DOMAIN, "my-client")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));
        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToMaybe(applicationService.findByDomainAndClientId_migrated(DOMAIN, "my-client")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomain() {
        when(applicationRepository.findByDomain_migrated(DOMAIN, 0, Integer.MAX_VALUE)).thenReturn(Mono.just(new Page<>(Collections.singleton(new Application()), 0, 1)));
        TestObserver<Set<Application>> testObserver = RxJava2Adapter.monoToSingle(applicationService.findByDomain_migrated(DOMAIN)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(applications -> applications.size() == 1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(applicationRepository.findByDomain_migrated(DOMAIN, 0, Integer.MAX_VALUE)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.findByDomain_migrated(DOMAIN)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomainPagination() {
        Page pageClients = new Page(Collections.singleton(new Application()), 1, 1);
        when(applicationRepository.findByDomain_migrated(DOMAIN, 1, 1)).thenReturn(Mono.just(pageClients));
        TestObserver<Page<Application>> testObserver = RxJava2Adapter.monoToSingle(applicationService.findByDomain_migrated(DOMAIN, 1, 1)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.getData().size() == 1);
    }

    @Test
    public void shouldFindByDomainPagination_technicalException() {
        when(applicationRepository.findByDomain_migrated(DOMAIN, 1, 1)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.findByDomain_migrated(DOMAIN, 1, 1)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByIdentityProvider() {
        when(applicationRepository.findByIdentityProvider_migrated("client-idp")).thenReturn(Flux.just(new Application()));
        TestSubscriber<Application> testSubscriber = RxJava2Adapter.fluxToFlowable(applicationService.findByIdentityProvider_migrated("client-idp")).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldFindByIdentityProvider_technicalException() {
        when(applicationRepository.findByIdentityProvider_migrated("client-idp")).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestSubscriber testSubscriber = RxJava2Adapter.fluxToFlowable(applicationService.findByIdentityProvider_migrated("client-idp")).test();

        testSubscriber.assertError(TechnicalManagementException.class);
        testSubscriber.assertNotComplete();
    }

    @Test
    public void shouldFindByCertificate() {
        when(applicationRepository.findByCertificate_migrated("client-certificate")).thenReturn(Flux.just(new Application()));
        TestSubscriber<Application> testObserver = RxJava2Adapter.fluxToFlowable(applicationService.findByCertificate_migrated("client-certificate")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindByCertificate_technicalException() {
        when(applicationRepository.findByCertificate_migrated("client-certificate")).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestSubscriber testSub = RxJava2Adapter.fluxToFlowable(applicationService.findByCertificate_migrated("client-certificate")).test();

        testSub.assertError(TechnicalManagementException.class);
        testSub.assertNotComplete();
    }

    @Test
    public void shouldFindByExtensionGrant() {
        when(applicationRepository.findByDomainAndExtensionGrant_migrated(DOMAIN, "client-extension-grant")).thenReturn(Flux.just(new Application()));
        TestObserver<Set<Application>> testObserver = RxJava2Adapter.monoToSingle(applicationService.findByDomainAndExtensionGrant_migrated(DOMAIN, "client-extension-grant")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.size() == 1);
    }

    @Test
    public void shouldFindByExtensionGrant_technicalException() {
        when(applicationRepository.findByDomainAndExtensionGrant_migrated(DOMAIN, "client-extension-grant")).thenReturn(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.findByDomainAndExtensionGrant_migrated(DOMAIN, "client-extension-grant")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindAll() {
        when(applicationRepository.findAll_migrated(0, Integer.MAX_VALUE)).thenReturn(Mono.just(new Page(Collections.singleton(new Application()), 0, 1)));
        TestObserver<Set<Application>> testObserver = RxJava2Adapter.monoToSingle(applicationService.findAll_migrated()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.size() == 1);
    }

    @Test
    public void shouldFindAll_technicalException() {
        when(applicationRepository.findAll_migrated(0, Integer.MAX_VALUE)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.findAll_migrated()).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindAllPagination() {
        Page pageClients = new Page(Collections.singleton(new Application()), 1, 1);
        when(applicationRepository.findAll_migrated(1, 1)).thenReturn(Mono.just(pageClients));
        TestObserver<Page<Application>> testObserver = RxJava2Adapter.monoToSingle(applicationService.findAll_migrated(1, 1)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.getData().size() == 1);
    }

    @Test
    public void shouldFindAllPagination_technicalException() {
        when(applicationRepository.findAll_migrated(1, 1)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.findAll_migrated(1, 1)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindTotalClientsByDomain() {
        when(applicationRepository.countByDomain_migrated(DOMAIN)).thenReturn(Mono.just(1l));
        TestObserver<Long> testObserver = RxJava2Adapter.monoToSingle(applicationService.countByDomain_migrated(DOMAIN)).test();

        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(totalClient -> totalClient.longValue() == 1l);
    }

    @Test
    public void shouldFindTotalClientsByDomain_technicalException() {
        when(applicationRepository.countByDomain_migrated(DOMAIN)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.countByDomain_migrated(DOMAIN)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindTotalClients() {
        when(applicationRepository.count_migrated()).thenReturn(Mono.just(1l));
        TestObserver<Long> testObserver = RxJava2Adapter.monoToSingle(applicationService.count_migrated()).test();

        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(totalClient -> totalClient.longValue() == 1l);
    }

    @Test
    public void shouldFindTotalClients_technicalException() {
        when(applicationRepository.count_migrated()).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.count_migrated()).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        NewApplication newClient = Mockito.mock(NewApplication.class);
        Application createClient = Mockito.mock(Application.class);
        when(newClient.getName()).thenReturn("my-client");
        when(newClient.getType()).thenReturn(ApplicationType.SERVICE);
        when(applicationRepository.findByDomainAndClientId_migrated(DOMAIN, null)).thenReturn(Mono.empty());
        when(applicationRepository.create_migrated(any(Application.class))).thenReturn(Mono.just(createClient));
        when(domainService.findById_migrated(anyString())).thenReturn(Mono.just(new Domain()));
        when(scopeService.validateScope_migrated(anyString(), any())).thenReturn(Mono.just(true));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        doAnswer(invocation -> {
            Application mock = invocation.getArgument(0);
            mock.getSettings().getOauth().setGrantTypes(Collections.singletonList(GrantType.CLIENT_CREDENTIALS));
            return mock;
        }).when(applicationTemplateManager).apply(any());
        when(membershipService.addOrUpdate_migrated(eq(ORGANIZATION_ID), any())).thenReturn(Mono.just(new Membership()));
        when(roleService.findSystemRole_migrated(SystemRole.APPLICATION_PRIMARY_OWNER, ReferenceType.APPLICATION)).thenReturn(Mono.just(new Role()));
        when(certificateService.findByDomain_migrated(DOMAIN)).thenReturn(Flux.empty());

        DefaultUser user = new DefaultUser("username");
        user.setAdditionalInformation(Collections.singletonMap(Claims.organization, ORGANIZATION_ID));

        TestObserver<Application> testObserver = RxJava2Adapter.monoToSingle(applicationService.create_migrated(DOMAIN, newClient, user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findByDomainAndClientId_migrated(DOMAIN, null);
        verify(applicationRepository, times(1)).create_migrated(any(Application.class));
        verify(membershipService).addOrUpdate_migrated(eq(ORGANIZATION_ID), any());
    }

    @Test
    public void shouldCreate_technicalException() {
        NewApplication newClient = Mockito.mock(NewApplication.class);
        when(newClient.getName()).thenReturn("my-client");
        when(applicationRepository.findByDomainAndClientId_migrated(DOMAIN, null)).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver<Application> testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.create_migrated(DOMAIN, newClient)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, never()).create_migrated(any(Application.class));
    }

    @Test
    public void shouldCreate2_technicalException() {
        NewApplication newClient = Mockito.mock(NewApplication.class);
        when(newClient.getName()).thenReturn("my-client");
        when(newClient.getRedirectUris()).thenReturn(null);
        when(newClient.getType()).thenReturn(ApplicationType.SERVICE);
        doAnswer(invocation -> {
            Application mock = invocation.getArgument(0);
            mock.getSettings().getOauth().setGrantTypes(Collections.singletonList(GrantType.CLIENT_CREDENTIALS));
            return mock;
        }).when(applicationTemplateManager).apply(any());
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(scopeService.validateScope_migrated(anyString(), any())).thenReturn(Mono.just(true));
        when(certificateService.findByDomain_migrated(DOMAIN)).thenReturn(Flux.empty());
        when(applicationRepository.findByDomainAndClientId_migrated(DOMAIN, null)).thenReturn(Mono.empty());
        when(applicationRepository.create_migrated(any(Application.class))).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver<Application> testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.create_migrated(DOMAIN, newClient)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
        verify(applicationRepository, times(1)).findByDomainAndClientId_migrated(DOMAIN, null);
    }

    @Test
    public void shouldCreate_clientAlreadyExists() {
        NewApplication newClient = Mockito.mock(NewApplication.class);
        when(newClient.getName()).thenReturn("my-client");
        when(applicationRepository.findByDomainAndClientId_migrated(DOMAIN, null)).thenReturn(Mono.just(new Application()));

        TestObserver<Application> testObserver = new TestObserver<>();
        RxJava2Adapter.monoToSingle(applicationService.create_migrated(DOMAIN, newClient)).subscribe(testObserver);

        testObserver.assertError(ApplicationAlreadyExistsException.class);
        testObserver.assertNotComplete();
        verify(applicationRepository, times(1)).findByDomainAndClientId_migrated(DOMAIN, null);
        verify(applicationRepository, never()).create_migrated(any(Application.class));
    }

    @Test
    public void create_failWithNoDomain() {
        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.create_migrated(new Application())).test();
        testObserver.assertNotComplete();
        testObserver.assertError(InvalidClientMetadataException.class);
    }

    @Test
    public void create_implicit_invalidRedirectUri() {
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(applicationRepository.findByDomainAndClientId_migrated(DOMAIN, null)).thenReturn(Mono.empty());

        Application toCreate = new Application();
        toCreate.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setGrantTypes(Arrays.asList("implicit"));
        oAuthSettings.setResponseTypes(Arrays.asList("token"));
        oAuthSettings.setClientType(ClientType.PUBLIC);
        settings.setOauth(oAuthSettings);
        toCreate.setSettings(settings);

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.create_migrated(toCreate)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidRedirectUriException.class);
    }

    @Test
    public void create_generateUuidAsClientId() {
        NewApplication newClient = Mockito.mock(NewApplication.class);
        Application createClient = Mockito.mock(Application.class);
        when(newClient.getName()).thenReturn("my-client");
        when(newClient.getType()).thenReturn(ApplicationType.SERVICE);
        when(applicationRepository.findByDomainAndClientId_migrated(DOMAIN, "client_id")).thenReturn(Mono.empty());
        when(applicationRepository.create_migrated(any(Application.class))).thenReturn(Mono.just(createClient));
        when(domainService.findById_migrated(anyString())).thenReturn(Mono.just(new Domain()));
        when(scopeService.validateScope_migrated(anyString(), any())).thenReturn(Mono.just(true));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        doAnswer(invocation -> {
            Application mock = invocation.getArgument(0);
            mock.getSettings().getOauth().setGrantTypes(Collections.singletonList(GrantType.CLIENT_CREDENTIALS));
            mock.getSettings().getOauth().setClientId("client_id");
            mock.getSettings().getOauth().setClientSecret("client_secret");
            return mock;
        }).when(applicationTemplateManager).apply(any());
        when(certificateService.findByDomain_migrated(DOMAIN)).thenReturn(Flux.empty());

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.create_migrated(DOMAIN, newClient)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
        verify(applicationRepository, times(1)).create_migrated(captor.capture());
        Assert.assertTrue("client_id must be generated", captor.getValue().getSettings().getOauth().getClientId() != null);
        Assert.assertTrue("client_secret must be generated", captor.getValue().getSettings().getOauth().getClientSecret() != null);
    }

    @Test
    public void shouldPatch_keepingClientRedirectUris() {
        PatchApplication patchClient = new PatchApplication();
        patchClient.setIdentities(Optional.of(new HashSet<>(Arrays.asList("id1", "id2"))));
        PatchApplicationSettings patchApplicationSettings = new PatchApplicationSettings();
        PatchApplicationOAuthSettings patchApplicationOAuthSettings = new PatchApplicationOAuthSettings();
        patchApplicationOAuthSettings.setResponseTypes(Optional.of(Arrays.asList("token")));
        patchApplicationOAuthSettings.setGrantTypes(Optional.of(Arrays.asList("implicit")));
        patchApplicationSettings.setOauth(Optional.of(patchApplicationOAuthSettings));
        patchApplicationSettings.setPasswordSettings(Optional.empty());
        patchClient.setSettings(Optional.of(patchApplicationSettings));

        Application toPatch = new Application();
        toPatch.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setRedirectUris(Arrays.asList("https://callback"));
        settings.setOauth(oAuthSettings);
        toPatch.setSettings(settings);

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setId("idp1");
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setId("idp2");

        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(toPatch));
        when(identityProviderService.findById_migrated("id1")).thenReturn(Mono.just(idp1));
        when(identityProviderService.findById_migrated("id2")).thenReturn(Mono.just(idp2));
        when(applicationRepository.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(scopeService.validateScope_migrated(DOMAIN, new ArrayList<>())).thenReturn(Mono.just(true));

        TestObserver<Application> testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(identityProviderService, times(2)).findById_migrated(anyString());
        verify(applicationRepository, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void shouldUpdate_implicit_invalidRedirectUri() {
        Application client = new Application();
        ApplicationSettings clientSettings = new ApplicationSettings();
        ApplicationOAuthSettings clientOAuthSettings = new ApplicationOAuthSettings();
        clientOAuthSettings.setClientType(ClientType.PUBLIC);
        clientSettings.setOauth(clientOAuthSettings);
        client.setDomain(DOMAIN);
        client.setSettings(clientSettings);

        PatchApplication patchClient = new PatchApplication();
        PatchApplicationSettings patchApplicationSettings = new PatchApplicationSettings();
        PatchApplicationOAuthSettings patchApplicationOAuthSettings = new PatchApplicationOAuthSettings();
        patchApplicationOAuthSettings.setGrantTypes(Optional.of(Arrays.asList("implicit")));
        patchApplicationOAuthSettings.setResponseTypes(Optional.of(Arrays.asList("token")));
        patchApplicationSettings.setOauth(Optional.of(patchApplicationOAuthSettings));
        patchApplicationSettings.setPasswordSettings(Optional.empty());
        patchClient.setSettings(Optional.of(patchApplicationSettings));

        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(client));
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidRedirectUriException.class);

        verify(applicationRepository, times(1)).findById_migrated(anyString());
    }

    @Test
    public void shouldUpdate_technicalException() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void shouldUpdate2_technicalException() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void shouldUpdate_clientNotFound() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();

        testObserver.assertError(ApplicationNotFoundException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void update_failWithNoDomain() {
        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.update_migrated(new Application())).test();
        testObserver.assertNotComplete();
        testObserver.assertError(InvalidClientMetadataException.class);
    }

    @Test
    public void update_implicitGrant_invalidRedirectUri() {

        when(applicationRepository.findById_migrated(any())).thenReturn(Mono.just(new Application()));
        when(domainService.findById_migrated(any())).thenReturn(Mono.just(new Domain()));

        Application toPatch = new Application();
        toPatch.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setGrantTypes(Arrays.asList("implicit"));
        oAuthSettings.setResponseTypes(Arrays.asList("token"));
        oAuthSettings.setClientType(ClientType.PUBLIC);
        settings.setOauth(oAuthSettings);
        toPatch.setSettings(settings);

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.update_migrated(toPatch)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidRedirectUriException.class);

        verify(applicationRepository, times(1)).findById_migrated(any());
    }

    @Test
    public void update_defaultGrant_ok() {
        when(applicationRepository.findById_migrated(any())).thenReturn(Mono.just(new Application()));
        when(applicationRepository.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));
        when(domainService.findById_migrated(any())).thenReturn(Mono.just(new Domain()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(scopeService.validateScope_migrated(any(), any())).thenReturn(Mono.just(true));

        Application toPatch = new Application();
        toPatch.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setRedirectUris(Arrays.asList("https://callback"));
        oAuthSettings.setGrantTypes(Collections.singletonList(GrantType.AUTHORIZATION_CODE));
        settings.setOauth(oAuthSettings);
        toPatch.setSettings(settings);

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.update_migrated(toPatch)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findById_migrated(any());
        verify(applicationRepository, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void update_clientCredentials_ok() {
        when(applicationRepository.findById_migrated(any())).thenReturn(Mono.just(new Application()));
        when(applicationRepository.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));
        when(domainService.findById_migrated(any())).thenReturn(Mono.just(new Domain()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(scopeService.validateScope_migrated(any(), any())).thenReturn(Mono.just(true));

        Application toPatch = new Application();
        toPatch.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setGrantTypes(Arrays.asList("client_credentials"));
        oAuthSettings.setResponseTypes(Arrays.asList());
        settings.setOauth(oAuthSettings);
        toPatch.setSettings(settings);

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.update_migrated(toPatch)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findById_migrated(any());
        verify(applicationRepository, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void shouldPatch() {
        Application client = new Application();
        client.setId("my-client");
        client.setDomain(DOMAIN);

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setId("idp1");
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setId("idp2");

        PatchApplication patchClient = new PatchApplication();
        patchClient.setIdentities(Optional.of(new HashSet<>(Arrays.asList("id1", "id2"))));
        PatchApplicationSettings patchApplicationSettings = new PatchApplicationSettings();
        PatchApplicationOAuthSettings patchApplicationOAuthSettings = new PatchApplicationOAuthSettings();
        patchApplicationOAuthSettings.setGrantTypes(Optional.of(Arrays.asList("authorization_code")));
        patchApplicationOAuthSettings.setRedirectUris(Optional.of(Arrays.asList("https://callback")));
        patchApplicationSettings.setOauth(Optional.of(patchApplicationOAuthSettings));
        patchApplicationSettings.setPasswordSettings(Optional.empty());
        patchClient.setSettings(Optional.of(patchApplicationSettings));

        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(client));
        when(identityProviderService.findById_migrated("id1")).thenReturn(Mono.just(idp1));
        when(identityProviderService.findById_migrated("id2")).thenReturn(Mono.just(idp2));
        when(applicationRepository.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(scopeService.validateScope_migrated(DOMAIN, new ArrayList<>())).thenReturn(Mono.just(true));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(identityProviderService, times(2)).findById_migrated(anyString());
        verify(applicationRepository, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void shouldPatch_Application_ResetPassword_ValidField() {
        Application client = new Application();
        client.setId("my-client");
        client.setDomain(DOMAIN);

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setId("idp1");
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setId("idp2");

        PatchApplication patchClient = new PatchApplication();
        patchClient.setIdentities(Optional.of(new HashSet<>(Arrays.asList("id1", "id2"))));
        PatchApplicationSettings patchApplicationSettings = new PatchApplicationSettings();
        PatchApplicationOAuthSettings patchApplicationOAuthSettings = new PatchApplicationOAuthSettings();
        patchApplicationOAuthSettings.setGrantTypes(Optional.of(Arrays.asList("authorization_code")));
        patchApplicationOAuthSettings.setRedirectUris(Optional.of(Arrays.asList("https://callback")));
        patchApplicationSettings.setOauth(Optional.of(patchApplicationOAuthSettings));
        patchApplicationSettings.setPasswordSettings(Optional.empty());
        final AccountSettings accountSettings = new AccountSettings();
        final FormField formField = new FormField();
        formField.setKey("username");
        accountSettings.setResetPasswordCustomFormFields(Arrays.asList(formField));
        accountSettings.setResetPasswordCustomForm(true);
        patchApplicationSettings.setAccount(Optional.of(accountSettings));
        patchClient.setSettings(Optional.of(patchApplicationSettings));

        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(client));
        when(identityProviderService.findById_migrated("id1")).thenReturn(Mono.just(idp1));
        when(identityProviderService.findById_migrated("id2")).thenReturn(Mono.just(idp2));
        when(applicationRepository.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(scopeService.validateScope_migrated(DOMAIN, new ArrayList<>())).thenReturn(Mono.just(true));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(identityProviderService, times(2)).findById_migrated(anyString());
        verify(applicationRepository, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void shouldNoPatch_Application_ResetPassword_InvalidField() {
        Application client = new Application();
        client.setId("my-client");
        client.setDomain(DOMAIN);

        IdentityProvider idp1 = new IdentityProvider();
        idp1.setId("idp1");
        IdentityProvider idp2 = new IdentityProvider();
        idp2.setId("idp2");

        PatchApplication patchClient = new PatchApplication();
        patchClient.setIdentities(Optional.of(new HashSet<>(Arrays.asList("id1", "id2"))));
        PatchApplicationSettings patchApplicationSettings = new PatchApplicationSettings();
        PatchApplicationOAuthSettings patchApplicationOAuthSettings = new PatchApplicationOAuthSettings();
        patchApplicationOAuthSettings.setGrantTypes(Optional.of(Arrays.asList("authorization_code")));
        patchApplicationOAuthSettings.setRedirectUris(Optional.of(Arrays.asList("https://callback")));
        patchApplicationSettings.setOauth(Optional.of(patchApplicationOAuthSettings));
        patchApplicationSettings.setPasswordSettings(Optional.empty());
        final AccountSettings accountSettings = new AccountSettings();
        final FormField formField = new FormField();
        formField.setKey("unknown");
        accountSettings.setResetPasswordCustomFormFields(Arrays.asList(formField));
        accountSettings.setResetPasswordCustomForm(true);
        patchApplicationSettings.setAccount(Optional.of(accountSettings));
        patchClient.setSettings(Optional.of(patchApplicationSettings));

        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(client));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(InvalidParameterException.class);

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(identityProviderService, never()).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void shouldPatch_mobileApplication() {
        Application client = new Application();
        client.setDomain(DOMAIN);

        PatchApplication patchClient = new PatchApplication();
        PatchApplicationSettings patchApplicationSettings = new PatchApplicationSettings();
        PatchApplicationOAuthSettings patchApplicationOAuthSettings = new PatchApplicationOAuthSettings();
        patchApplicationOAuthSettings.setGrantTypes(Optional.of(Arrays.asList("authorization_code")));
        patchApplicationOAuthSettings.setRedirectUris(Optional.of(Arrays.asList("com.gravitee.app://callback")));
        patchApplicationSettings.setOauth(Optional.of(patchApplicationOAuthSettings));
        patchApplicationSettings.setPasswordSettings(Optional.empty());
        patchClient.setSettings(Optional.of(patchApplicationSettings));

        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(client));
        when(applicationRepository.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(scopeService.validateScope_migrated(DOMAIN, new ArrayList<>())).thenReturn(Mono.just(true));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void shouldPatch_mobileApplication_googleCase() {
        Application client = new Application();
        client.setDomain(DOMAIN);

        PatchApplication patchClient = new PatchApplication();
        PatchApplicationSettings patchApplicationSettings = new PatchApplicationSettings();
        PatchApplicationOAuthSettings patchApplicationOAuthSettings = new PatchApplicationOAuthSettings();
        patchApplicationOAuthSettings.setGrantTypes(Optional.of(Arrays.asList("authorization_code")));
        patchApplicationOAuthSettings.setRedirectUris(Optional.of(Arrays.asList("com.google.app:/callback")));
        patchApplicationSettings.setOauth(Optional.of(patchApplicationOAuthSettings));
        patchApplicationSettings.setPasswordSettings(Optional.empty());
        patchClient.setSettings(Optional.of(patchApplicationSettings));

        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(client));
        when(applicationRepository.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(scopeService.validateScope_migrated(DOMAIN, new ArrayList<>())).thenReturn(Mono.just(true));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void shouldDelete() {
        Application existingClient = Mockito.mock(Application.class);
        when(existingClient.getId()).thenReturn("my-client");
        when(existingClient.getDomain()).thenReturn("my-domain");
        when(applicationRepository.findById_migrated(existingClient.getId())).thenReturn(Mono.just(existingClient));
        when(applicationRepository.delete_migrated(existingClient.getId())).thenReturn(Mono.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        Form form = new Form();
        form.setId("form-id");
        when(formService.findByDomainAndClient_migrated(existingClient.getDomain(), existingClient.getId())).thenReturn(Flux.just(form));
        when(formService.delete_migrated(eq("my-domain"), eq(form.getId()))).thenReturn(Mono.empty());
        Email email = new Email();
        email.setId("email-id");
        when(emailTemplateService.findByClient_migrated(ReferenceType.DOMAIN, existingClient.getDomain(), existingClient.getId())).thenReturn(Flux.just(email));
        when(emailTemplateService.delete_migrated(email.getId())).thenReturn(Mono.empty());
        Membership membership = new Membership();
        membership.setId("membership-id");
        when(membershipService.findByReference_migrated(existingClient.getId(), ReferenceType.APPLICATION)).thenReturn(Flux.just(membership));
        when(membershipService.delete_migrated(anyString())).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(applicationService.delete_migrated(existingClient.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).delete_migrated(existingClient.getId());
        verify(formService, times(1)).delete_migrated(eq("my-domain"), anyString());
        verify(emailTemplateService, times(1)).delete_migrated(anyString());
        verify(membershipService, times(1)).delete_migrated(anyString());
    }

    @Test
    public void shouldDelete_withoutRelatedData() {
        Application existingClient = Mockito.mock(Application.class);
        when(existingClient.getDomain()).thenReturn("my-domain");
        when(existingClient.getId()).thenReturn("my-client");
        when(applicationRepository.findById_migrated(existingClient.getId())).thenReturn(Mono.just(existingClient));
        when(applicationRepository.delete_migrated(existingClient.getId())).thenReturn(Mono.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(formService.findByDomainAndClient_migrated(existingClient.getDomain(), existingClient.getId())).thenReturn(Flux.empty());
        when(emailTemplateService.findByClient_migrated(ReferenceType.DOMAIN, existingClient.getDomain(), existingClient.getId())).thenReturn(Flux.empty());
        when(membershipService.findByReference_migrated(existingClient.getId(), ReferenceType.APPLICATION)).thenReturn(Flux.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(applicationService.delete_migrated(existingClient.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).delete_migrated(existingClient.getId());
        verify(formService, never()).delete_migrated(anyString(), anyString());
        verify(emailTemplateService, never()).delete_migrated(anyString());
        verify(membershipService, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldDelete_technicalException() {
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));
        when(applicationRepository.delete_migrated(anyString())).thenReturn(Mono.error(TechnicalException::new));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(applicationService.delete_migrated("my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete2_technicalException() {
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(applicationService.delete_migrated("my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_clientNotFound() {
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(applicationService.delete_migrated("my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(ApplicationNotFoundException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, never()).delete_migrated("my-client");
    }

    @Test
    public void validateClientMetadata_invalidRedirectUriException_noSchemeUri() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);

        Application client = new Application();
        client.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setRedirectUris(Arrays.asList("noscheme"));
        oAuthSettings.setGrantTypes(Collections.singletonList(GrantType.CLIENT_CREDENTIALS));
        settings.setOauth(oAuthSettings);
        client.setSettings(settings);

        when(patchClient.patch(any())).thenReturn(client);
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.assertError(InvalidRedirectUriException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void validateClientMetadata_invalidRedirectUriException_malformedUri() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);

        Application client = new Application();
        client.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setRedirectUris(Arrays.asList("malformed:uri:exception"));
        oAuthSettings.setGrantTypes(Collections.singletonList(GrantType.AUTHORIZATION_CODE));
        settings.setOauth(oAuthSettings);
        client.setSettings(settings);

        when(patchClient.patch(any())).thenReturn(client);
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.assertError(InvalidRedirectUriException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void validateClientMetadata_invalidRedirectUriException_forbidLocalhost() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);

        Application client = new Application();
        client.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setRedirectUris(Arrays.asList("http://localhost/callback"));
        oAuthSettings.setGrantTypes(Collections.singletonList(GrantType.AUTHORIZATION_CODE));
        settings.setOauth(oAuthSettings);
        client.setSettings(settings);

        when(patchClient.patch(any())).thenReturn(client);
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.assertError(InvalidRedirectUriException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void validateClientMetadata_invalidRedirectUriException_forbidHttp() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);

        Application client = new Application();
        client.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setRedirectUris(Arrays.asList("http://gravitee.io/callback"));
        oAuthSettings.setGrantTypes(Collections.singletonList(GrantType.AUTHORIZATION_CODE));
        settings.setOauth(oAuthSettings);
        client.setSettings(settings);

        when(patchClient.patch(any())).thenReturn(client);
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.assertError(InvalidRedirectUriException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void validateClientMetadata_invalidRedirectUriException_forbidWildcard() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);

        Application client = new Application();
        client.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setRedirectUris(Arrays.asList("https://gravitee.io/*"));
        oAuthSettings.setGrantTypes(Collections.singletonList(GrantType.AUTHORIZATION_CODE));
        settings.setOauth(oAuthSettings);
        client.setSettings(settings);

        when(patchClient.patch(any())).thenReturn(client);
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.assertError(InvalidRedirectUriException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void validateClientMetadata_invalidClientMetadataException_unknownScope() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);

        Application client = new Application();
        client.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setRedirectUris(Arrays.asList("https://callback"));
        oAuthSettings.setScopes(Collections.emptyList());
        settings.setOauth(oAuthSettings);
        client.setSettings(settings);

        when(patchClient.patch(any())).thenReturn(client);
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void validateClientMetadata_invalidClientMetadataException_invalidTokenEndpointAuthMethod() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);

        Application client = new Application();
        client.setType(ApplicationType.SERVICE);
        client.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setTokenEndpointAuthMethod(ClientAuthenticationMethod.NONE);
        oAuthSettings.setScopes(Collections.emptyList());
        settings.setOauth(oAuthSettings);
        client.setSettings(settings);

        when(patchClient.patch(any())).thenReturn(client);
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.assertError(InvalidClientMetadataException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, never()).update_migrated(any(Application.class));
    }

    @Test
    public void validateClientMetadata_validMetadata() {
        PatchApplication patchClient = Mockito.mock(PatchApplication.class);

        Application client = new Application();
        client.setDomain(DOMAIN);
        ApplicationSettings settings = new ApplicationSettings();
        ApplicationOAuthSettings oAuthSettings = new ApplicationOAuthSettings();
        oAuthSettings.setRedirectUris(Arrays.asList("https://gravitee.io/callback"));
        oAuthSettings.setGrantTypes(Collections.singletonList(GrantType.AUTHORIZATION_CODE));
        oAuthSettings.setScopes(Collections.emptyList());
        settings.setOauth(oAuthSettings);
        client.setSettings(settings);

        when(patchClient.patch(any())).thenReturn(client);
        when(domainService.findById_migrated(DOMAIN)).thenReturn(Mono.just(new Domain()));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(new Application()));
        when(applicationRepository.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));
        when(scopeService.validateScope_migrated(DOMAIN, Collections.emptyList())).thenReturn(Mono.just(true));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.patch_migrated(DOMAIN, "my-client", patchClient)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void shouldRenewSecret() {
        Application client = new Application();
        client.setDomain(DOMAIN);
        ApplicationSettings applicationSettings = new ApplicationSettings();
        ApplicationOAuthSettings applicationOAuthSettings = new ApplicationOAuthSettings();
        applicationSettings.setOauth(applicationOAuthSettings);
        client.setSettings(applicationSettings);

        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.just(client));
        when(applicationRepository.update_migrated(any(Application.class))).thenReturn(Mono.just(new Application()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.renewClientSecret_migrated(DOMAIN, "my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(applicationRepository, times(1)).findById_migrated(anyString());
        verify(applicationRepository, times(1)).update_migrated(any(Application.class));
    }

    @Test
    public void shouldRenewSecret_clientNotFound() {
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.renewClientSecret_migrated(DOMAIN, "my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(ApplicationNotFoundException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, never()).update_migrated(any());
    }

    @Test
    public void shouldRenewSecret_technicalException() {
        when(applicationRepository.findById_migrated("my-client")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(applicationService.renewClientSecret_migrated(DOMAIN, "my-client")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(applicationRepository, never()).update_migrated(any());
    }
}

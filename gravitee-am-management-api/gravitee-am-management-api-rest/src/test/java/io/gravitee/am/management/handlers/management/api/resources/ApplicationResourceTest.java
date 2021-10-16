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
package io.gravitee.am.management.handlers.management.api.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.management.service.permissions.PermissionAcls;
import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.PasswordSettings;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.application.ApplicationAdvancedSettings;
import io.gravitee.am.model.application.ApplicationOAuthSettings;
import io.gravitee.am.model.application.ApplicationSettings;
import io.gravitee.am.model.application.ApplicationType;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.service.exception.ApplicationNotFoundException;
import io.gravitee.am.service.model.PatchApplication;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.Response;
import org.junit.Test;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ApplicationResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetApp() {
        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final Application mockApplication = buildApplicationMock();

        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(true)))).when(permissionService).hasPermission_migrated(any(User.class), any(PermissionAcls.class));
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Permission.allPermissionAcls(ReferenceType.APPLICATION))))).when(permissionService).findAllPermissions_migrated(any(User.class), any(ReferenceType.class), anyString());
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockApplication)))).when(applicationService).findById_migrated(mockApplication.getId());

        // Check all data are returned when having all permissions.
        final Response response = target("domains").path(domainId).path("applications").path(mockApplication.getId()).request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final Application application = readEntity(response, Application.class);
        assertEquals(mockApplication.getId(), application.getId());
        assertEquals(mockApplication.getName(), application.getName());
        assertEquals(mockApplication.getDomain(), application.getDomain());
        assertEquals(mockApplication.getType(), application.getType());
        assertEquals(mockApplication.getDescription(), application.getDescription());
        assertEquals(mockApplication.isEnabled(), application.isEnabled());
        assertEquals(mockApplication.isTemplate(), application.isTemplate());
        assertEquals(mockApplication.getFactors(), application.getFactors());
        assertEquals(mockApplication.getCreatedAt(), application.getCreatedAt());
        assertEquals(mockApplication.getUpdatedAt(), application.getUpdatedAt());
        assertEquals(mockApplication.getIdentities(), application.getIdentities());
        assertEquals(mockApplication.getCertificate(), application.getCertificate());
        assertNotNull(application.getSettings());
        assertNotNull(application.getSettings().getAdvanced());
        assertNotNull(application.getSettings().getAccount());
        assertNotNull(application.getSettings().getOauth());
        assertNotNull(application.getSettings().getPasswordSettings());
    }

    @Test
    public void shouldGetFilteredApp() {
        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final Application mockApplication = buildApplicationMock();

        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(true)))).when(permissionService).hasPermission_migrated(any(User.class), any(PermissionAcls.class));
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Permission.of(Permission.APPLICATION, Acl.READ))))).when(permissionService).findAllPermissions_migrated(any(User.class), any(ReferenceType.class), anyString()); // only application read permission
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockApplication)))).when(applicationService).findById_migrated(mockApplication.getId());

        // Check data are filtered according to permissions.
        final Response response = target("domains").path(domainId).path("applications").path(mockApplication.getId()).request().get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final Application application = readEntity(response, Application.class);
        assertEquals(mockApplication.getId(), application.getId());
        assertEquals(mockApplication.getName(), application.getName());
        assertEquals(mockApplication.getDomain(), application.getDomain());
        assertEquals(mockApplication.getType(), application.getType());
        assertEquals(mockApplication.getDescription(), application.getDescription());
        assertEquals(mockApplication.isEnabled(), application.isEnabled());
        assertEquals(mockApplication.isTemplate(), application.isTemplate());
        assertNull(application.getFactors());
        assertEquals(mockApplication.getCreatedAt(), application.getCreatedAt());
        assertEquals(mockApplication.getUpdatedAt(), application.getUpdatedAt());
        assertNull(application.getIdentities());
        assertNull(application.getCertificate());
        assertNotNull(application.getSettings());
        assertNull(application.getSettings().getAccount());
        assertNull(application.getSettings().getOauth());
        assertNull(application.getSettings().getPasswordSettings());
    }

    @Test
    public void shouldGetApplication_notFound() {
        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final String clientId = "client-id";

        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty()))).when(applicationService).findById_migrated(clientId);

        final Response response = target("domains").path(domainId).path("applications").path(clientId).request().get();
        assertEquals(HttpStatusCode.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void shouldGetClient_domainNotFound() {
        final String domainId = "domain-id";
        final String clientId = "client-id";

        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty()))).when(domainService).findById_migrated(domainId);

        final Response response = target("domains").path(domainId).path("applications").path(clientId).request().get();
        assertEquals(HttpStatusCode.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void shouldGetApplication_wrongDomain() {
        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final String applicationId = "application-id";
        final Application mockClient = new Application();
        mockClient.setId(applicationId);
        mockClient.setName("client-name");
        mockClient.setDomain("wrong-domain");

        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(true)))).when(permissionService).hasPermission_migrated(any(User.class), any(PermissionAcls.class));
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Permission.allPermissionAcls(ReferenceType.APPLICATION))))).when(permissionService).findAllPermissions_migrated(any(User.class), any(ReferenceType.class), anyString());
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockClient)))).when(applicationService).findById_migrated(applicationId);

        final Response response = target("domains").path(domainId).path("applications").path(applicationId).request().get();
        assertEquals(HttpStatusCode.BAD_REQUEST_400, response.getStatus());
    }

    @Test
    public void shouldUpdateApplication() {

        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final Application mockApplication = buildApplicationMock();
        PatchApplication patchApplication = new PatchApplication();
        patchApplication.setDescription(Optional.of("New description"));

        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(true)))).when(permissionService).hasPermission_migrated(any(User.class), any(PermissionAcls.class));
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Permission.allPermissionAcls(ReferenceType.APPLICATION))))).when(permissionService).findAllPermissions_migrated(any(User.class), any(ReferenceType.class), anyString());
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(mockApplication)))).when(applicationService).patch_migrated(eq(domainId), eq(mockApplication.getId()), any(PatchApplication.class), any(User.class));

        // heck all data are returned when having all permissions.
        final Response response = put(target("domains").path(domainId).path("applications").path(mockApplication.getId()), patchApplication);
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final Application application = readEntity(response, Application.class);
        assertEquals(mockApplication.getId(), application.getId());
        assertEquals(mockApplication.getName(), application.getName());
        assertEquals(mockApplication.getDomain(), application.getDomain());
        assertEquals(mockApplication.getType(), application.getType());
        assertEquals(mockApplication.getDescription(), application.getDescription());
        assertEquals(mockApplication.isEnabled(), application.isEnabled());
        assertEquals(mockApplication.isTemplate(), application.isTemplate());
        assertEquals(mockApplication.getFactors(), application.getFactors());
        assertEquals(mockApplication.getCreatedAt(), application.getCreatedAt());
        assertEquals(mockApplication.getUpdatedAt(), application.getUpdatedAt());
        assertEquals(mockApplication.getIdentities(), application.getIdentities());
        assertEquals(mockApplication.getCertificate(), application.getCertificate());
        ApplicationSettings settings = application.getSettings();
        assertNotNull(settings);
        assertNotNull(settings.getAdvanced());
        assertNotNull(settings.getAccount());
        assertNotNull(settings.getOauth());
        assertNotNull(settings.getPasswordSettings());
    }

    @Test
    public void shouldUpdateFilteredApplication() {

        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final Application mockApplication = buildApplicationMock();
        PatchApplication patchApplication = new PatchApplication();
        patchApplication.setDescription(Optional.of("New description"));

        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(true)))).when(permissionService).hasPermission_migrated(any(User.class), any(PermissionAcls.class));
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Permission.of(Permission.APPLICATION, Acl.READ))))).when(permissionService).findAllPermissions_migrated(any(User.class), any(ReferenceType.class), anyString()); // only application read permission
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(mockApplication)))).when(applicationService).patch_migrated(eq(domainId), eq(mockApplication.getId()), any(PatchApplication.class), any(User.class));

        // Check all data are returned when having all permissions.
        final Response response = put(target("domains").path(domainId).path("applications").path(mockApplication.getId()), patchApplication);
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        final Application application = readEntity(response, Application.class);
        assertEquals(mockApplication.getId(), application.getId());
        assertEquals(mockApplication.getName(), application.getName());
        assertEquals(mockApplication.getDomain(), application.getDomain());
        assertEquals(mockApplication.getType(), application.getType());
        assertEquals(mockApplication.getDescription(), application.getDescription());
        assertEquals(mockApplication.isEnabled(), application.isEnabled());
        assertEquals(mockApplication.isTemplate(), application.isTemplate());
        assertNull(application.getFactors());
        assertEquals(mockApplication.getCreatedAt(), application.getCreatedAt());
        assertEquals(mockApplication.getUpdatedAt(), application.getUpdatedAt());
        assertNull(application.getIdentities());
        assertNull(application.getCertificate());
        ApplicationSettings settings = application.getSettings();
        assertNotNull(settings);
        assertNull(settings.getAccount());
        assertNull(settings.getOauth());
        assertNull(settings.getPasswordSettings());
    }

    @Test
    public void shouldUpdateApplication_domainNotFound() {

        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final Application mockApplication = buildApplicationMock();
        PatchApplication patchApplication = new PatchApplication();
        patchApplication.setDescription(Optional.of("New description"));

        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(true)))).when(permissionService).hasPermission_migrated(any(User.class), any(PermissionAcls.class));
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Permission.of(Permission.APPLICATION, Acl.READ))))).when(permissionService).findAllPermissions_migrated(any(User.class), any(ReferenceType.class), anyString()); // only application read permission
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty()))).when(domainService).findById_migrated(domainId);

        final Response response = put(target("domains").path(domainId).path("applications").path(mockApplication.getId()), patchApplication);
        assertEquals(HttpStatusCode.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void shouldUpdateApplication_forbidden() {

        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final Application mockApplication = buildApplicationMock();
        PatchApplication patchApplication = new PatchApplication();
        patchApplication.setDescription(Optional.of("New description"));

        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(false)))).when(permissionService).hasPermission_migrated(any(User.class), any(PermissionAcls.class));
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);

        final Response response = put(target("domains").path(domainId).path("applications").path(mockApplication.getId()), patchApplication);
        assertEquals(HttpStatusCode.FORBIDDEN_403, response.getStatus());
    }

    @Test
    public void shouldUpdateApplication_badRequest() {

        // Empty patch should result in 400 bad request.
        PatchApplication patchApplication = new PatchApplication();

        final Response response = put(target("domains").path("domain-id").path("applications").path("application-id"), patchApplication);
        assertEquals(HttpStatusCode.BAD_REQUEST_400, response.getStatus());
    }

    @Test
    public void shouldRenewClientSecret() {
        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final String clientId = "client-id";
        final Application mockClient = new Application();
        mockClient.setId(clientId);
        mockClient.setName("client-name");
        mockClient.setDomain(domainId);

        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(Permission.allPermissionAcls(ReferenceType.APPLICATION))))).when(permissionService).findAllPermissions_migrated(any(User.class), eq(ReferenceType.APPLICATION), anyString());
        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(mockClient)))).when(applicationService).renewClientSecret_migrated(eq(domainId), eq(clientId), any());

        final Response response = target("domains")
                .path(domainId)
                .path("applications")
                .path(clientId)
                .path("secret/_renew")
                .request()
                .post(null);
        assertEquals(HttpStatusCode.OK_200, response.getStatus());
    }

    @Test
    public void shouldRenewClientSecret_appNotFound() {
        final String domainId = "domain-id";
        final Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        final String clientId = "client-id";
        final Application mockClient = new Application();
        mockClient.setId(clientId);
        mockClient.setName("client-name");
        mockClient.setDomain(domainId);

        doReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(mockDomain)))).when(domainService).findById_migrated(domainId);
        doReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new ApplicationNotFoundException(clientId))))).when(applicationService).renewClientSecret_migrated(eq(domainId), eq(clientId), any());

        final Response response = target("domains")
                .path(domainId)
                .path("applications")
                .path(clientId)
                .path("secret/_renew")
                .request()
                .post(null);
        assertEquals(HttpStatusCode.NOT_FOUND_404, response.getStatus());
    }

    private Application buildApplicationMock() {

        Application mockApplication = new Application();
        mockApplication.setId("client-id");
        mockApplication.setName("client-name");
        mockApplication.setDomain("domain-id");
        mockApplication.setType(ApplicationType.SERVICE);
        mockApplication.setDescription("description");
        mockApplication.setEnabled(true);
        mockApplication.setTemplate(true);
        mockApplication.setFactors(Collections.singleton("factor"));
        mockApplication.setCreatedAt(new Date());
        mockApplication.setUpdatedAt(new Date());
        mockApplication.setIdentities(Collections.singleton("identity"));
        mockApplication.setCertificate("certificate");

        ApplicationSettings filteredApplicationSettings = new ApplicationSettings();
        filteredApplicationSettings.setAdvanced(new ApplicationAdvancedSettings());
        filteredApplicationSettings.setAccount(new AccountSettings());
        filteredApplicationSettings.setOauth(new ApplicationOAuthSettings());
        filteredApplicationSettings.setPasswordSettings(new PasswordSettings());

        mockApplication.setSettings(filteredApplicationSettings);
        mockApplication.setMetadata(Collections.singletonMap("key", "value"));

        return mockApplication;
    }
}

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
package io.gravitee.am.management.handlers.management.api.authentication.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.management.handlers.management.api.authentication.service.impl.AuthenticationServiceImpl;
import io.gravitee.am.model.*;
import io.gravitee.am.model.User;
import io.gravitee.am.model.membership.MemberType;
import io.gravitee.am.model.permissions.DefaultRole;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.MembershipService;
import io.gravitee.am.service.OrganizationUserService;
import io.gravitee.am.service.RoleService;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {

    public static final String ORGANIZATION_ID = Organization.DEFAULT;

    @InjectMocks
    private AuthenticationService authenticationService = new AuthenticationServiceImpl();

    @Mock
    private AuthenticationSuccessEvent eventMock;

    @Mock
    private Authentication authenticationMock;

    @Mock
    private DefaultUser userDetailsMock;

    @Mock
    private OrganizationUserService userServiceMock;

    @Mock
    private RoleService roleServiceMock;

    @Mock
    private MembershipService membershipServiceMock;

    @Mock
    private User repositoryUserMock;

    @Mock
    private Membership membershipMock;

    @Mock
    private AuditService auditService;

    @Test
    public void shouldCreateUser() {
        User user = new User();
        user.setReferenceType(ReferenceType.ORGANIZATION);
        user.setReferenceId(ORGANIZATION_ID);

        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);
        when(userServiceMock.findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, userDetailsMock.getUsername(), null)).thenReturn(Mono.empty());
        when(userServiceMock.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, userDetailsMock.getUsername(), null)).thenReturn(Mono.empty());
        when(userServiceMock.create_migrated(any(User.class))).thenReturn(Mono.just(user));
        when(userServiceMock.setRoles_migrated(any(), any(User.class))).thenReturn(Mono.empty());
        when(userServiceMock.enhance_migrated(any())).thenReturn(Mono.just(user));
        authenticationService.onAuthenticationSuccess(authenticationMock);

        verify(userServiceMock, times(1)).findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, userDetailsMock.getUsername(), null);
        verify(userServiceMock, times(1)).findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, userDetailsMock.getUsername(), null);
        verify(userServiceMock, times(1)).create_migrated(any(User.class));
        verify(userServiceMock, times(1)).setRoles_migrated(any(), any(User.class));
        verify(userServiceMock, never()).update_migrated(any(User.class));
    }

    @Test
    public void shouldUpdatedUser() {
        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);
        when(userServiceMock.findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, userDetailsMock.getUsername(), null)).thenReturn(Mono.just(repositoryUserMock));
        when(userServiceMock.update_migrated(any(User.class))).thenReturn(Mono.just(new User()));
        when(userServiceMock.enhance_migrated(any())).thenReturn(Mono.just(new User()));

        authenticationService.onAuthenticationSuccess(authenticationMock);

        verify(userServiceMock, times(1)).findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, userDetailsMock.getUsername(), null);
        verify(userServiceMock, times(1)).update_migrated(any(User.class));
        verify(membershipServiceMock, never()).addOrUpdate_migrated(any(String.class), any(Membership.class));
    }

    @Test
    public void shouldUpdatedUser_update_membership() {
        when(membershipMock.getReferenceType()).thenReturn(ReferenceType.ORGANIZATION);
        when(membershipMock.getRoleId()).thenReturn("organization-user-role-id");

        when(repositoryUserMock.getId()).thenReturn("user-id");
        when(repositoryUserMock.getReferenceType()).thenReturn(ReferenceType.ORGANIZATION);
        when(repositoryUserMock.getReferenceId()).thenReturn("organization-id");

        when(userDetailsMock.getRoles()).thenReturn(Collections.singletonList("organization-owner-role-id"));

        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);

        when(userServiceMock.findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, userDetailsMock.getUsername(), null)).thenReturn(Mono.just(repositoryUserMock));
        when(userServiceMock.update_migrated(any(User.class))).thenReturn(Mono.just(new User()));
        when(userServiceMock.enhance_migrated(any())).thenReturn(Mono.just(new User()));

        when(roleServiceMock.findById_migrated(ReferenceType.ORGANIZATION, "organization-id", "organization-owner-role-id")).thenReturn(Mono.just(new Role()));

        when(membershipServiceMock.findByMember_migrated("user-id", MemberType.USER)).thenReturn(Flux.just(membershipMock));
        when(membershipServiceMock.addOrUpdate_migrated(anyString(), any(Membership.class))).thenReturn(Mono.just(new Membership()));

        authenticationService.onAuthenticationSuccess(authenticationMock);

        verify(userServiceMock, times(1)).findByExternalIdAndSource_migrated(ReferenceType.ORGANIZATION, ORGANIZATION_ID, userDetailsMock.getUsername(), null);
        verify(userServiceMock, times(1)).update_migrated(any(User.class));
        verify(membershipServiceMock, times(1)).addOrUpdate_migrated(any(String.class), any(Membership.class));
    }
}

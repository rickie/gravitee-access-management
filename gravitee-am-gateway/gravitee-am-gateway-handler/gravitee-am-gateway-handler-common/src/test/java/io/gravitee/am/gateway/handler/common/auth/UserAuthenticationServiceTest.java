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
package io.gravitee.am.gateway.handler.common.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.common.exception.authentication.AccountDisabledException;
import io.gravitee.am.gateway.handler.common.auth.user.UserAuthenticationService;
import io.gravitee.am.gateway.handler.common.auth.user.impl.UserAuthenticationServiceImpl;
import io.gravitee.am.gateway.handler.common.user.UserService;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Group;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.User;


import io.reactivex.observers.TestObserver;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAuthenticationServiceTest {

    @InjectMocks
    private UserAuthenticationService userAuthenticationService = new UserAuthenticationServiceImpl();

    @Mock
    private UserService userService;

    @Mock
    private Domain domain;

    @Test
    public void shouldConnect_unknownUser() {
        String domainId = "Domain";
        String username = "foo";
        String source = "SRC";
        String id = "id";
        io.gravitee.am.identityprovider.api.User user = mock(io.gravitee.am.identityprovider.api.User.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getId()).thenReturn(id);
        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("source", source);
        additionalInformation.put("op_id_token", "somevalue");
        when(user.getAdditionalInformation()).thenReturn(additionalInformation);

        User createdUser = mock(User.class);
        when(createdUser.isEnabled()).thenReturn(true);

        when(domain.getId()).thenReturn(domainId);
        when(userService.findByDomainAndExternalIdAndSource_migrated(domainId, id, source)).thenReturn(Mono.empty());
        when(userService.findByDomainAndUsernameAndSource_migrated(domainId, username, source)).thenReturn(Mono.empty());
        when(userService.create_migrated(any())).thenReturn(Mono.just(createdUser));
        when(userService.enhance_migrated(createdUser)).thenReturn(Mono.just(createdUser));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(userAuthenticationService.connect_migrated(user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(userService, times(1)).create_migrated(argThat(u -> u.getAdditionalInformation().containsKey("op_id_token")));
        verify(userService, never()).update_migrated(any());
    }

    @Test
    public void shouldConnect_knownUser() {
        String domainId = "Domain";
        String source = "SRC";
        String id = "id";
        io.gravitee.am.identityprovider.api.User user = mock(io.gravitee.am.identityprovider.api.User.class);
        when(user.getId()).thenReturn(id);
        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("source", source);
        when(user.getAdditionalInformation()).thenReturn(additionalInformation);

        User updatedUser = mock(User.class);
        when(updatedUser.isEnabled()).thenReturn(true);

        when(domain.getId()).thenReturn(domainId);
        when(userService.findByDomainAndExternalIdAndSource_migrated(domainId, id, source)).thenReturn(Mono.just(mock(User.class)));
        when(userService.update_migrated(any())).thenReturn(Mono.just(updatedUser));
        when(userService.enhance_migrated(updatedUser)).thenReturn(Mono.just(updatedUser));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(userAuthenticationService.connect_migrated(user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(userService, never()).create_migrated(any());
        verify(userService, times(1)).update_migrated(any());
    }

    @Test
    public void shouldNotConnect_accountDisabled() {
        String domainId = "Domain";
        String source = "SRC";
        String id = "id";
        io.gravitee.am.identityprovider.api.User user = mock(io.gravitee.am.identityprovider.api.User.class);
        when(user.getId()).thenReturn(id);
        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("source", source);
        when(user.getAdditionalInformation()).thenReturn(additionalInformation);

        User updatedUser = mock(User.class);
        when(updatedUser.isEnabled()).thenReturn(false);

        when(domain.getId()).thenReturn(domainId);
        when(userService.findByDomainAndExternalIdAndSource_migrated(domainId, id, source)).thenReturn(Mono.just(mock(User.class)));
        when(userService.update_migrated(any())).thenReturn(Mono.just(updatedUser));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(userAuthenticationService.connect_migrated(user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNotComplete();
        testObserver.assertError(AccountDisabledException.class);
    }

    @Test
    public void shouldConnect_unknownUser_withRoles() {
        String domainId = "Domain";
        String username = "foo";
        String source = "SRC";
        String id = "id";

        Role role1= new Role();
        role1.setId("idp-role");
        Role role2 = new Role();
        role2.setId("idp2-role");
        Set<Role> roles = new HashSet<>(Arrays.asList(role1, role2));

        io.gravitee.am.identityprovider.api.User user = mock(io.gravitee.am.identityprovider.api.User.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getId()).thenReturn(id);
        when(user.getRoles()).thenReturn(Arrays.asList("idp-role", "idp2-role"));
        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("source", source);
        when(user.getAdditionalInformation()).thenReturn(additionalInformation);

        User createdUser = mock(User.class);
        when(createdUser.isEnabled()).thenReturn(true);
        when(createdUser.getRoles()).thenReturn(Arrays.asList("idp-role", "idp2-role"));

        when(domain.getId()).thenReturn(domainId);
        when(userService.findByDomainAndExternalIdAndSource_migrated(domainId, id, source)).thenReturn(Mono.empty());
        when(userService.findByDomainAndUsernameAndSource_migrated(domainId, username, source)).thenReturn(Mono.empty());
        when(userService.create_migrated(any())).thenReturn(Mono.just(createdUser));
        when(userService.enhance_migrated(createdUser)).thenReturn(Mono.just(createdUser));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userAuthenticationService.connect_migrated(user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(user1 -> user1.getRoles().size() == 2);
    }

    @Test
    public void shouldConnect_knownUser_withRoles() {
        String domainId = "Domain";
        String username = "foo";
        String source = "SRC";
        String id = "id";

        Role role1= new Role();
        role1.setId("idp-role");
        Role role2 = new Role();
        role2.setId("idp2-role");
        Set<Role> roles = new HashSet<>(Arrays.asList(role1, role2));

        io.gravitee.am.identityprovider.api.User user = mock(io.gravitee.am.identityprovider.api.User.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getId()).thenReturn(id);
        when(user.getRoles()).thenReturn(Arrays.asList("idp-role", "idp2-role"));
        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("source", source);
        when(user.getAdditionalInformation()).thenReturn(additionalInformation);

        User updatedUser = mock(User.class);
        when(updatedUser.isEnabled()).thenReturn(true);
        when(updatedUser.getRoles()).thenReturn(Arrays.asList("idp-role", "idp2-role"));

        when(domain.getId()).thenReturn(domainId);
        when(userService.findByDomainAndExternalIdAndSource_migrated(domainId, id, source)).thenReturn(Mono.just(mock(User.class)));
        when(userService.update_migrated(any())).thenReturn(Mono.just(updatedUser));
        when(userService.enhance_migrated(updatedUser)).thenReturn(Mono.just(updatedUser));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userAuthenticationService.connect_migrated(user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(user1 -> user1.getRoles().size() == 2);
    }

    @Test
    public void shouldConnect_knownUser_withRoles_fromGroup() {
        String domainId = "Domain";
        String username = "foo";
        String source = "SRC";
        String id = "id";

        Group group = mock(Group.class);

        Role role1= new Role();
        role1.setId("idp-role");
        Role role2 = new Role();
        role2.setId("idp2-role");
        Set<Role> roles = new HashSet<>(Arrays.asList(role1, role2));

        io.gravitee.am.identityprovider.api.User user = mock(io.gravitee.am.identityprovider.api.User.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getId()).thenReturn(id);
        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("source", source);
        when(user.getAdditionalInformation()).thenReturn(additionalInformation);

        User updatedUser = mock(User.class);
        when(updatedUser.isEnabled()).thenReturn(true);
        when(updatedUser.getRoles()).thenReturn(Arrays.asList("group-role", "group2-role"));

        when(domain.getId()).thenReturn(domainId);
        when(userService.findByDomainAndExternalIdAndSource_migrated(domainId, id, source)).thenReturn(Mono.just(mock(User.class)));
        when(userService.update_migrated(any())).thenReturn(Mono.just(updatedUser));
        when(userService.enhance_migrated(updatedUser)).thenReturn(Mono.just(updatedUser));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userAuthenticationService.connect_migrated(user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(user1 -> user1.getRoles().size() == 2);
    }

    @Test
    public void shouldConnect_knownUser_with_OpIdToken() {
        String domainId = "Domain";
        String username = "foo";
        String source = "SRC";
        String id = "id";

        io.gravitee.am.identityprovider.api.User user = mock(io.gravitee.am.identityprovider.api.User.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getId()).thenReturn(id);
        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("source", source);
        additionalInformation.put("op_id_token", "token2");
        when(user.getAdditionalInformation()).thenReturn(additionalInformation);

        User updatedUser = mock(User.class);
        when(updatedUser.isEnabled()).thenReturn(true);

        when(domain.getId()).thenReturn(domainId);
        final User existingUser = new User();
        HashMap<String, Object> existingAdditionalInformation = new HashMap<>();
        existingAdditionalInformation.put("source", source);
        existingAdditionalInformation.put("op_id_token", "token1");
        existingUser.setAdditionalInformation(existingAdditionalInformation);

        when(userService.findByDomainAndExternalIdAndSource_migrated(domainId, id, source)).thenReturn(Mono.just(existingUser));
        when(userService.update_migrated(any())).thenReturn(Mono.just(updatedUser));
        when(userService.enhance_migrated(updatedUser)).thenReturn(Mono.just(updatedUser));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userAuthenticationService.connect_migrated(user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        verify(userService).update_migrated(argThat(user1 -> "token2".equals(user1.getAdditionalInformation().get("op_id_token"))));
    }


    @Test
    public void shouldConnect_knownUser_with_OpIdToken_removed() {
        String domainId = "Domain";
        String username = "foo";
        String source = "SRC";
        String id = "id";

        io.gravitee.am.identityprovider.api.User user = mock(io.gravitee.am.identityprovider.api.User.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getId()).thenReturn(id);
        HashMap<String, Object> additionalInformation = new HashMap<>();
        additionalInformation.put("source", source);
        when(user.getAdditionalInformation()).thenReturn(additionalInformation);

        User updatedUser = mock(User.class);
        when(updatedUser.isEnabled()).thenReturn(true);

        when(domain.getId()).thenReturn(domainId);
        final User existingUser = new User();
        HashMap<String, Object> existingAdditionalInformation = new HashMap<>();
        existingAdditionalInformation.put("source", source);
        existingAdditionalInformation.put("op_id_token", "token1");
        existingUser.setAdditionalInformation(existingAdditionalInformation);

        when(userService.findByDomainAndExternalIdAndSource_migrated(domainId, id, source)).thenReturn(Mono.just(existingUser));
        when(userService.update_migrated(any())).thenReturn(Mono.just(updatedUser));
        when(userService.enhance_migrated(updatedUser)).thenReturn(Mono.just(updatedUser));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userAuthenticationService.connect_migrated(user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(userService).update_migrated(argThat(user1 -> !user1.getAdditionalInformation().containsKey("op_id_token")));
    }
}

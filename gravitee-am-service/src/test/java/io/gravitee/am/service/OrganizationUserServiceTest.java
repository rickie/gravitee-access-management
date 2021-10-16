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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.Credential;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.User;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.OrganizationUserRepository;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.impl.OrganizationUserServiceImpl;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.gravitee.am.service.validators.UserValidator;




import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class OrganizationUserServiceTest {

    @InjectMocks
    private OrganizationUserService userService = new OrganizationUserServiceImpl();

    @Spy
    private UserValidator userValidator = new UserValidator();

    @Mock
    private OrganizationUserRepository userRepository;

    @Mock
    private EventService eventService;

    @Mock
    private CredentialService credentialService;

    private final static String ORG = "organization1";

    @Test
    public void shouldCreate() {
        NewUser newUser = Mockito.mock(NewUser.class);
        User user = new User();
        user.setReferenceType(ReferenceType.ORGANIZATION);
        user.setReferenceId(ORG);

        when(newUser.getUsername()).thenReturn("username");
        when(newUser.getSource()).thenReturn("source");
        when(userRepository.create_migrated(any(User.class))).thenReturn(Mono.just(user));
        when(userRepository.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, ORG, newUser.getUsername(), newUser.getSource())).thenReturn(Mono.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(userService.create_migrated(ReferenceType.ORGANIZATION, ORG, newUser)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(userRepository, times(1)).create_migrated(any(User.class));
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldNotCreate_emailFormatInvalidException() {
        User user = new User();
        user.setReferenceType(ReferenceType.ORGANIZATION);
        user.setReferenceId(ORG);

        NewUser newUser = new NewUser();
        newUser.setEmail("invalid");
        when(userRepository.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, ORG, newUser.getUsername(), newUser.getSource())).thenReturn(Mono.empty());
        when(userRepository.create_migrated(any(User.class))).thenReturn(Mono.just(user));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userService.create_migrated(ReferenceType.ORGANIZATION, ORG, newUser)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(EmailFormatInvalidException.class);

        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldNotCreate_invalidUserException() {
        User user = new User();
        user.setReferenceType(ReferenceType.ORGANIZATION);
        user.setReferenceId(ORG);

        NewUser newUser = new NewUser();
        newUser.setUsername("##&##");
        when(userRepository.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, ORG, newUser.getUsername(), newUser.getSource())).thenReturn(Mono.empty());
        when(userRepository.create_migrated(any(User.class))).thenReturn(Mono.just(user));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userService.create_migrated(ReferenceType.ORGANIZATION, ORG, newUser)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(InvalidUserException.class);

        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldCreate_technicalException() {
        NewUser newUser = Mockito.mock(NewUser.class);
        when(newUser.getUsername()).thenReturn("username");
        when(newUser.getSource()).thenReturn("source");
        when(userRepository.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, ORG, newUser.getUsername(), newUser.getSource())).thenReturn(Mono.empty());
        when(userRepository.create_migrated(any(User.class))).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(userService.create_migrated(ReferenceType.ORGANIZATION, ORG, newUser)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate_alreadyExists() {
        NewUser newUser = Mockito.mock(NewUser.class);
        when(newUser.getUsername()).thenReturn("username");
        when(newUser.getSource()).thenReturn("source");
        when(userRepository.findByUsernameAndSource_migrated(ReferenceType.ORGANIZATION, ORG, newUser.getUsername(), newUser.getSource())).thenReturn(Mono.just(new User()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(userService.create_migrated(ReferenceType.ORGANIZATION, ORG, newUser)).subscribe(testObserver);

        testObserver.assertError(UserAlreadyExistsException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldUpdate() {
        UpdateUser updateUser = Mockito.mock(UpdateUser.class);
        User user = new User();
        user.setReferenceType(ReferenceType.ORGANIZATION);
        user.setReferenceId(ORG);

        when(userRepository.findById_migrated(eq(ReferenceType.ORGANIZATION), eq(ORG), eq("my-user"))).thenReturn(Mono.just(user));
        when(userRepository.findByUsernameAndSource_migrated(eq(ReferenceType.ORGANIZATION), eq(ORG), any(), any())).thenReturn(Mono.just(user));
        when(userRepository.update_migrated(any(User.class))).thenReturn(Mono.just(user));
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));

        TestObserver testObserver = RxJava2Adapter.monoToSingle(userService.update_migrated(ReferenceType.ORGANIZATION, ORG, "my-user", updateUser)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(userRepository, times(1)).findById_migrated(eq(ReferenceType.ORGANIZATION), eq(ORG), eq("my-user"));
        verify(userRepository, times(1)).update_migrated(any(User.class));
        verify(eventService, times(1)).create_migrated(any());
    }

    @Test
    public void shouldNotUpdate_emailFormatInvalidException() {
        User user = new User();
        user.setReferenceType(ReferenceType.ORGANIZATION);
        user.setReferenceId(ORG);

        UpdateUser updateUser = new UpdateUser();
        updateUser.setEmail("invalid");
        when(userRepository.findByUsernameAndSource_migrated(eq(ReferenceType.ORGANIZATION), eq(ORG), any(), any())).thenReturn(Mono.just(user));
        when(userRepository.findById_migrated(eq(ReferenceType.ORGANIZATION), eq(ORG), eq("my-user"))).thenReturn(Mono.just(user));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userService.update_migrated(ReferenceType.ORGANIZATION, ORG, "my-user", updateUser)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(EmailFormatInvalidException.class);

        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldNotUpdate_invalidUserException() {
        User user = new User();
        user.setReferenceType(ReferenceType.ORGANIZATION);
        user.setReferenceId(ORG);

        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName("$$^^^^¨¨¨)");
        when(userRepository.findByUsernameAndSource_migrated(eq(ReferenceType.ORGANIZATION), eq(ORG), any(), any())).thenReturn(Mono.just(user));
        when(userRepository.findById_migrated(eq(ReferenceType.ORGANIZATION), eq(ORG), eq("my-user"))).thenReturn(Mono.just(user));

        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userService.update_migrated(ReferenceType.ORGANIZATION, ORG, "my-user", updateUser)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(InvalidUserException.class);

        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldUpdate_technicalException() {
        UpdateUser updateUser = Mockito.mock(UpdateUser.class);
        when(userRepository.findById_migrated(eq(ReferenceType.ORGANIZATION), eq(ORG), eq("my-user"))).thenReturn(Mono.just(new User()));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(userService.update_migrated(ReferenceType.ORGANIZATION, ORG, "my-user", updateUser)).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldUpdate_userNotFound() {
        UpdateUser updateUser = Mockito.mock(UpdateUser.class);
        when(userRepository.findById_migrated(eq(ReferenceType.ORGANIZATION), eq(ORG), eq("my-user"))).thenReturn(Mono.empty());

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToSingle(userService.update_migrated(ReferenceType.ORGANIZATION, ORG, "my-user", updateUser)).subscribe(testObserver);

        testObserver.assertError(UserNotFoundException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete() {
        User user = new User();
        user.setId("my-user");
        user.setReferenceType(ReferenceType.ORGANIZATION);
        user.setReferenceId(ORG);

        when(userRepository.findById_migrated("my-user")).thenReturn(Mono.just(user));
        when(userRepository.delete_migrated("my-user")).thenReturn(Mono.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(credentialService.findByUserId_migrated(user.getReferenceType(), user.getReferenceId(), user.getId())).thenReturn(Flux.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(userService.delete_migrated("my-user")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(userRepository, times(1)).delete_migrated("my-user");
        verify(eventService, times(1)).create_migrated(any());
        verify(credentialService, never()).delete_migrated(anyString());
    }

    @Test
    public void shouldDelete_with_webauthn_credentials() {
        User user = new User();
        user.setId("my-user");
        user.setReferenceType(ReferenceType.ORGANIZATION);
        user.setReferenceId(ORG);

        Credential credential = new Credential();
        credential.setId("credential-id");

        when(userRepository.findById_migrated("my-user")).thenReturn(Mono.just(user));
        when(userRepository.delete_migrated("my-user")).thenReturn(Mono.empty());
        when(eventService.create_migrated(any())).thenReturn(Mono.just(new Event()));
        when(credentialService.findByUserId_migrated(user.getReferenceType(), user.getReferenceId(), user.getId())).thenReturn(Flux.just(credential));
        when(credentialService.delete_migrated(credential.getId())).thenReturn(Mono.empty());

        TestObserver testObserver = RxJava2Adapter.monoToCompletable(userService.delete_migrated("my-user")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(userRepository, times(1)).delete_migrated("my-user");
        verify(eventService, times(1)).create_migrated(any());
        verify(credentialService, times(1)).delete_migrated("credential-id");
    }

    @Test
    public void shouldDelete_technicalException() {
        when(userRepository.findById_migrated("my-user")).thenReturn(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new)));

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToCompletable(userService.delete_migrated("my-user")).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_userNotFound() {
        when(userRepository.findById_migrated("my-user")).thenReturn(Mono.empty());

        TestObserver testObserver = new TestObserver();
        RxJava2Adapter.monoToCompletable(userService.delete_migrated("my-user")).subscribe(testObserver);

        testObserver.assertError(UserNotFoundException.class);
        testObserver.assertNotComplete();

        verify(userRepository, never()).delete_migrated("my-user");
    }
}

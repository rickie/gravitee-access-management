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
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.UserRepository;
import io.gravitee.am.service.exception.*;
import io.gravitee.am.service.impl.UserServiceImpl;
import io.gravitee.am.service.model.NewUser;
import io.gravitee.am.service.model.UpdateUser;
import io.gravitee.am.service.validators.UserValidator;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
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
public class UserServiceTest {

    @InjectMocks
    private UserService userService = new UserServiceImpl();

    @Spy
    private UserValidator userValidator = new UserValidator();

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventService eventService;

    @Mock
    private CredentialService credentialService;

    private final static String DOMAIN = "domain1";

    /*
    @Before
    public void setUp() {
        doReturn(Completable.complete()).when(userValidator).validate(any());
    }
     */

    @Test
    public void shouldFindById() {
        when(userRepository.findById("my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.just(new User())));
        TestObserver testObserver = userService.findById("my-user").test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingUser() {
        when(userRepository.findById("my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.empty()));
        TestObserver testObserver = userService.findById("my-user").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(userRepository.findById("my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new))));
        TestObserver testObserver = new TestObserver();
        userService.findById("my-user").subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }


    @Test
    public void shouldFindByDomain() {
        when(userRepository.findAll(ReferenceType.DOMAIN, DOMAIN)).thenReturn(RxJava2Adapter.fluxToFlowable(Flux.just(new User())));
        TestSubscriber<User> testSubscriber = userService.findByDomain(DOMAIN).test();
        testSubscriber.awaitTerminalEvent();

        testSubscriber.assertComplete();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(userRepository.findAll(ReferenceType.DOMAIN, DOMAIN)).thenReturn(RxJava2Adapter.fluxToFlowable(Flux.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new))));

        TestSubscriber testSubscriber = userService.findByDomain(DOMAIN).test();

        testSubscriber.assertError(TechnicalManagementException.class);
        testSubscriber.assertNotComplete();
    }

    @Test
    public void shouldFindByDomainPagination() {
        Page pageUsers = new Page(Collections.singleton(new User()), 1 , 1);
        when(userRepository.findAll(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq(1) , eq(1))).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(pageUsers)));
        TestObserver<Page<User>> testObserver = userService.findByDomain(DOMAIN, 1, 1).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(extensionGrants -> extensionGrants.getData().size() == 1);
    }

    @Test
    public void shouldFindByDomainPagination_technicalException() {
        when(userRepository.findAll(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq(1) , eq(1))).thenReturn(RxJava2Adapter.monoToSingle(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new))));

        TestObserver testObserver = new TestObserver<>();
        userService.findByDomain(DOMAIN, 1 , 1).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldLoadUserByUsernameAndDomain() {
        when(userRepository.findByUsernameAndDomain(DOMAIN, "my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.just(new User())));
        TestObserver testObserver = userService.findByDomainAndUsername(DOMAIN, "my-user").test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldLoadUserByUsernameAndDomain_notExistingUser() {
        when(userRepository.findByUsernameAndDomain(DOMAIN, "my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.empty()));
        TestObserver testObserver = userService.findByDomainAndUsername(DOMAIN, "my-user").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldLoadUserByUsernameAndDomain_technicalException() {
        when(userRepository.findByUsernameAndDomain(DOMAIN, "my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new))));
        TestObserver testObserver = new TestObserver();
        userService.findByDomainAndUsername(DOMAIN, "my-user").subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        NewUser newUser = Mockito.mock(NewUser.class);
        User user = new User();
        user.setReferenceType(ReferenceType.DOMAIN);
        user.setReferenceId(DOMAIN);

        when(newUser.getUsername()).thenReturn("username");
        when(newUser.getSource()).thenReturn("source");
        when(userRepository.create(any(User.class))).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(user)));
        when(userRepository.findByUsernameAndSource(ReferenceType.DOMAIN, DOMAIN, newUser.getUsername(), newUser.getSource())).thenReturn(RxJava2Adapter.monoToMaybe(Mono.empty()));
        when(eventService.create(any())).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(new Event())));

        TestObserver testObserver = userService.create(DOMAIN, newUser).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(userRepository, times(1)).create(any(User.class));
        verify(eventService, times(1)).create(any());
    }

    @Test
    public void shouldNotCreate_emailFormatInvalidException() {
        User user = new User();
        user.setReferenceType(ReferenceType.DOMAIN);
        user.setReferenceId(DOMAIN);

        NewUser newUser = new NewUser();
        newUser.setEmail("invalid");
        when(userRepository.findByUsernameAndSource(ReferenceType.DOMAIN, DOMAIN, newUser.getUsername(), newUser.getSource())).thenReturn(RxJava2Adapter.monoToMaybe(Mono.empty()));
        when(userRepository.create(any(User.class))).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(user)));

        TestObserver<User> testObserver = userService.create(DOMAIN, newUser).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(EmailFormatInvalidException.class);

        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldNotCreate_invalidUserException() {
        User user = new User();
        user.setReferenceType(ReferenceType.DOMAIN);
        user.setReferenceId(DOMAIN);

        NewUser newUser = new NewUser();
        newUser.setUsername("##&##");
        when(userRepository.findByUsernameAndSource(ReferenceType.DOMAIN, DOMAIN, newUser.getUsername(), newUser.getSource())).thenReturn(RxJava2Adapter.monoToMaybe(Mono.empty()));
        when(userRepository.create(any(User.class))).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(user)));

        TestObserver<User> testObserver = userService.create(DOMAIN, newUser).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(InvalidUserException.class);

        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldCreate_technicalException() {
        NewUser newUser = Mockito.mock(NewUser.class);
        when(newUser.getUsername()).thenReturn("username");
        when(newUser.getSource()).thenReturn("source");
        when(userRepository.findByUsernameAndSource(ReferenceType.DOMAIN, DOMAIN, newUser.getUsername(), newUser.getSource())).thenReturn(RxJava2Adapter.monoToMaybe(Mono.empty()));
        when(userRepository.create(any(User.class))).thenReturn(RxJava2Adapter.monoToSingle(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new))));

        TestObserver testObserver = new TestObserver();
        userService.create(DOMAIN, newUser).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate_alreadyExists() {
        NewUser newUser = Mockito.mock(NewUser.class);
        when(newUser.getUsername()).thenReturn("username");
        when(newUser.getSource()).thenReturn("source");
        when(userRepository.findByUsernameAndSource(ReferenceType.DOMAIN, DOMAIN, newUser.getUsername(), newUser.getSource())).thenReturn(RxJava2Adapter.monoToMaybe(Mono.just(new User())));

        TestObserver testObserver = new TestObserver();
        userService.create(DOMAIN, newUser).subscribe(testObserver);

        testObserver.assertError(UserAlreadyExistsException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldUpdate() {
        UpdateUser updateUser = Mockito.mock(UpdateUser.class);
        User user = new User();
        user.setReferenceType(ReferenceType.DOMAIN);
        user.setReferenceId(DOMAIN);

        when(userRepository.findById(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-user"))).thenReturn(RxJava2Adapter.monoToMaybe(Mono.just(user)));
        when(userRepository.update(any(User.class))).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(user)));
        when(eventService.create(any())).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(new Event())));

        TestObserver testObserver = userService.update(DOMAIN, "my-user", updateUser).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(userRepository, times(1)).findById(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-user"));
        verify(userRepository, times(1)).update(any(User.class));
        verify(eventService, times(1)).create(any());
    }

    @Test
    public void shouldNotUpdate_emailFormatInvalidException() {
        User user = new User();
        user.setReferenceType(ReferenceType.DOMAIN);
        user.setReferenceId(DOMAIN);

        UpdateUser updateUser = new UpdateUser();
        updateUser.setEmail("invalid");
        when(userRepository.findById(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-user"))).thenReturn(RxJava2Adapter.monoToMaybe(Mono.just(user)));
        when(userRepository.update(any(User.class))).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(user)));

        TestObserver<User> testObserver = userService.update(DOMAIN, "my-user", updateUser).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(EmailFormatInvalidException.class);

        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldNotUpdate_invalidUserException() {
        User user = new User();
        user.setReferenceType(ReferenceType.DOMAIN);
        user.setReferenceId(DOMAIN);

        UpdateUser updateUser = new UpdateUser();
        updateUser.setFirstName("$$^^^^¨¨¨)");
        when(userRepository.findById(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-user"))).thenReturn(RxJava2Adapter.monoToMaybe(Mono.just(user)));
        when(userRepository.update(any(User.class))).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(user)));

        TestObserver<User> testObserver = userService.update(DOMAIN, "my-user", updateUser).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(InvalidUserException.class);

        verifyZeroInteractions(eventService);
    }

    @Test
    public void shouldUpdate_technicalException() {
        UpdateUser updateUser = Mockito.mock(UpdateUser.class);
        when(userRepository.findById(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-user"))).thenReturn(RxJava2Adapter.monoToMaybe(Mono.just(new User())));
        when(userRepository.update(any(User.class))).thenReturn(RxJava2Adapter.monoToSingle(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new))));

        TestObserver testObserver = new TestObserver();
        userService.update(DOMAIN, "my-user", updateUser).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldUpdate_userNotFound() {
        UpdateUser updateUser = Mockito.mock(UpdateUser.class);
        when(userRepository.findById(eq(ReferenceType.DOMAIN), eq(DOMAIN), eq("my-user"))).thenReturn(RxJava2Adapter.monoToMaybe(Mono.empty()));

        TestObserver testObserver = new TestObserver();
        userService.update(DOMAIN, "my-user", updateUser).subscribe(testObserver);

        testObserver.assertError(UserNotFoundException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete() {
        User user = new User();
        user.setId("my-user");
        user.setReferenceType(ReferenceType.DOMAIN);
        user.setReferenceId(DOMAIN);

        when(userRepository.findById("my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.just(user)));
        when(userRepository.delete("my-user")).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
        when(eventService.create(any())).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(new Event())));
        when(credentialService.findByUserId(user.getReferenceType(), user.getReferenceId(), user.getId())).thenReturn(RxJava2Adapter.fluxToFlowable(Flux.empty()));

        TestObserver testObserver = userService.delete("my-user").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(userRepository, times(1)).delete("my-user");
        verify(eventService, times(1)).create(any());
        verify(credentialService, never()).delete(anyString());
    }

    @Test
    public void shouldDelete_with_webauthn_credentials() {
        User user = new User();
        user.setId("my-user");
        user.setReferenceType(ReferenceType.DOMAIN);
        user.setReferenceId(DOMAIN);

        Credential credential = new Credential();
        credential.setId("credential-id");

        when(userRepository.findById("my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.just(user)));
        when(userRepository.delete("my-user")).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));
        when(eventService.create(any())).thenReturn(RxJava2Adapter.monoToSingle(Mono.just(new Event())));
        when(credentialService.findByUserId(user.getReferenceType(), user.getReferenceId(), user.getId())).thenReturn(RxJava2Adapter.fluxToFlowable(Flux.just(credential)));
        when(credentialService.delete(credential.getId())).thenReturn(RxJava2Adapter.monoToCompletable(Mono.empty()));

        TestObserver testObserver = userService.delete("my-user").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(userRepository, times(1)).delete("my-user");
        verify(eventService, times(1)).create(any());
        verify(credentialService, times(1)).delete("credential-id");
    }

    @Test
    public void shouldDelete_technicalException() {
        when(userRepository.findById("my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(TechnicalException::new))));

        TestObserver testObserver = new TestObserver();
        userService.delete("my-user").subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete_userNotFound() {
        when(userRepository.findById("my-user")).thenReturn(RxJava2Adapter.monoToMaybe(Mono.empty()));

        TestObserver testObserver = new TestObserver();
        userService.delete("my-user").subscribe(testObserver);

        testObserver.assertError(UserNotFoundException.class);
        testObserver.assertNotComplete();

        verify(userRepository, never()).delete("my-user");
    }
}

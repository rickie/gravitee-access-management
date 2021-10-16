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
package io.gravitee.am.identityprovider.jdbc.user;

import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.identityprovider.api.UserProvider;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class JdbcUserProvider_Test {

    @Autowired
    private UserProvider userProvider;

    @Test
    public void shouldSelectUserByUsername() {
        TestObserver<User> testObserver = RxJava2Adapter.monoToMaybe(userProvider.findByUsername_migrated("bob")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(u -> "bob".equals(u.getUsername()));
    }

    @Test
    public void shouldSelectUserByEmail() {
        TestObserver<User> testObserver = RxJava2Adapter.monoToMaybe(userProvider.findByEmail_migrated("user01@acme.com")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(u -> "user01".equals(u.getUsername()));
    }

    @Test
    public void shouldNotSelectUserByUsername_userNotFound() {
        TestObserver<User> testObserver = RxJava2Adapter.monoToMaybe(userProvider.findByUsername_migrated("unknown")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldNotSelectUserByEmail_userNotFound() {
        TestObserver<User> testObserver = RxJava2Adapter.monoToMaybe(userProvider.findByEmail_migrated("unknown@acme.com")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldCreate() {
        DefaultUser user = new DefaultUser("username1");
        TestObserver<User> testObserver = RxJava2Adapter.monoToSingle(userProvider.create_migrated(user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertValue(u -> u.getId() != null);
    }

    @Test
    public void shouldUpdate() {
        DefaultUser user = new DefaultUser("userToUpdate");
        user.setCredentials("password");
        User createdUser = userProvider.create_migrated(user).block();

        DefaultUser updateUser = new DefaultUser("userToUpdate");
        updateUser.setCredentials("password2");
        userProvider.update_migrated(createdUser.getId(), updateUser).block();

        TestObserver<User> testObserver = RxJava2Adapter.monoToMaybe(userProvider.findByUsername_migrated("userToUpdate")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertValue(u -> "password2".equals(updateUser.getCredentials()));
    }

    @Test
    public void shouldNotUpdate_userNotFound() {
        DefaultUser user = new DefaultUser("userToUpdate");
        TestObserver testObserver = RxJava2Adapter.monoToSingle(userProvider.update_migrated("unknown", user)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(UserNotFoundException.class);
        testObserver.assertNoValues();
    }

    @Test
    public void shouldDelete() {
        DefaultUser user = new DefaultUser("userToDelete");
        User createdUser = userProvider.create_migrated(user).block();
        RxJava2Adapter.monoToCompletable(userProvider.delete_migrated(createdUser.getId())).blockingGet();

        TestObserver<User> testObserver = RxJava2Adapter.monoToMaybe(userProvider.findByUsername_migrated("userToDelete")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldNotDelete_userNotFound() {
        TestObserver testObserver = RxJava2Adapter.monoToCompletable(userProvider.delete_migrated("unknown")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(UserNotFoundException.class);
        testObserver.assertNoValues();
    }
}

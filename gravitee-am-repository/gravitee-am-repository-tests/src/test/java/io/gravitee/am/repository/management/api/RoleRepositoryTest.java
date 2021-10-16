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
package io.gravitee.am.repository.management.api;

import static org.junit.Assert.assertTrue;

import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Platform;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.permissions.Permission;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RoleRepositoryTest extends AbstractManagementTest {
    public static final String DOMAIN_ID = "domain#1";

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testFindByDomain() throws TechnicalException {
        // create role
        Role role = new Role();
        role.setName("testName");
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setReferenceId("testDomain");
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role))).block();

        // fetch roles
        TestObserver<List<Role>> testObserver = RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(roleRepository.findAll_migrated(ReferenceType.DOMAIN, "testDomain"))).collectList()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(roles -> roles.size() == 1);
    }

    @Test
    public void testFindByNamesAndAssignable() throws TechnicalException {
        // create role
        Role role = new Role();
        final String NAME_1 = "testName";
        role.setName(NAME_1);
        role.setReferenceType(ReferenceType.PLATFORM);
        role.setReferenceId(Platform.DEFAULT);
        role.setAssignableType(ReferenceType.ORGANIZATION);
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role))).block();

        Role role2 = new Role();
        final String NAME_2 = "testName2";
        role2.setName(NAME_2);
        role2.setReferenceType(ReferenceType.PLATFORM);
        role2.setReferenceId(Platform.DEFAULT);
        role2.setAssignableType(ReferenceType.ORGANIZATION);
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role2))).block();

        Role role3 = new Role();
        final String NAME_3 = "testName3";
        role3.setName(NAME_3);
        role3.setReferenceType(ReferenceType.PLATFORM);
        role3.setReferenceId(Platform.DEFAULT);
        role3.setAssignableType(ReferenceType.ORGANIZATION);
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role3))).block();

        Role role4 = new Role();
        final String NAME_4 = "testName4";
        role4.setName(NAME_4);
        role4.setReferenceType(ReferenceType.PLATFORM);
        role4.setReferenceId(Platform.DEFAULT);
        role4.setAssignableType(ReferenceType.ENVIRONMENT);
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role4))).block();

        // fetch roles 1 & 2
        TestObserver<List<Role>> testObserver = RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(RxJava2Adapter.fluxToFlowable(roleRepository.findByNamesAndAssignableType_migrated(ReferenceType.PLATFORM, Platform.DEFAULT, Arrays.asList(NAME_1, NAME_2), ReferenceType.ORGANIZATION))).collectList()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(roles -> roles.size() == 2 && roles.stream().map(Role::getName).collect(Collectors.toList()).containsAll(Arrays.asList(NAME_1, NAME_2)));
    }

    @Test
    public void testFindById() throws TechnicalException {
        // create role
        Role role = buildRole();
        Role roleCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role))).block();

        // fetch role
        TestObserver<Role> testObserver = RxJava2Adapter.monoToMaybe(roleRepository.findById_migrated(roleCreated.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(r -> r.getId().equals(roleCreated.getId()));
        assertEqualsTo(role, testObserver);
    }

    private Role buildRole() {
        Role role = new Role();
        String random = UUID.randomUUID().toString();
        role.setSystem(true);
        role.setDefaultRole(true);
        role.setName("name"+random);
        role.setDescription("desc"+random);
        role.setReferenceId("ref"+random);
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setAssignableType(ReferenceType.APPLICATION);
        role.setOauthScopes(Arrays.asList("scope1"+random, "scope2"+random));
        role.setCreatedAt(new Date());
        role.setUpdatedAt(new Date());
        Map<Permission, Set<Acl>> permissions = new HashMap<>();
        permissions.put(Permission.APPLICATION, Sets.newSet(Acl.CREATE));
        role.setPermissionAcls(permissions);
        return role;
    }

    private void assertEqualsTo(Role role, TestObserver<Role> testObserver) {
        testObserver.assertValue(r -> r.getName().equals(role.getName()));
        testObserver.assertValue(r -> r.getAssignableType() == role.getAssignableType());
        testObserver.assertValue(r -> r.getDescription().equals(role.getDescription()));
        testObserver.assertValue(r -> r.getReferenceId().equals(role.getReferenceId()));
        testObserver.assertValue(r -> r.getReferenceType() == role.getReferenceType());
        testObserver.assertValue(r -> r.getOauthScopes().containsAll(role.getOauthScopes()));
        testObserver.assertValue(r -> r.getPermissionAcls().keySet().containsAll(role.getPermissionAcls().keySet()));
        testObserver.assertValue(r -> r.getPermissionAcls().get(Permission.APPLICATION).containsAll(role.getPermissionAcls().get(Permission.APPLICATION)));
    }

    @Test
    public void testFindById_referenceType() throws TechnicalException {
        // create role
        Role role = buildRole();
        role.setReferenceType(ReferenceType.DOMAIN);
        role.setReferenceId(DOMAIN_ID);
        Role roleCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role))).block();

        // fetch role
        TestObserver<Role> testObserver = RxJava2Adapter.monoToMaybe(roleRepository.findById_migrated(ReferenceType.DOMAIN, DOMAIN_ID, roleCreated.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(role, testObserver);
    }

    @Test
    public void testFindAll() throws TechnicalException {
        // create role
        Role role1 = new Role();
        role1.setName("testName1");
        role1.setReferenceType(ReferenceType.DOMAIN);
        role1.setReferenceId(DOMAIN_ID);
        Role roleCreated1 = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role1))).block();

        Role role2 = new Role();
        role2.setName("testName2");
        role2.setReferenceType(ReferenceType.DOMAIN);
        role2.setReferenceId(DOMAIN_ID);
        Role roleCreated2 = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role2))).block();

        // Role 3 is on domain#2.
        Role role3 = new Role();
        role3.setName("testName3");
        role3.setReferenceType(ReferenceType.DOMAIN);
        role3.setReferenceId("domain#2");
        RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role3))).block();

        // fetch role
        TestSubscriber<Role> testObserver = RxJava2Adapter.fluxToFlowable(roleRepository.findAll_migrated(ReferenceType.DOMAIN, DOMAIN_ID)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(2);
        List<Role> roles = testObserver.values();
        assertTrue(roles.stream().anyMatch(role -> role.getId().equals(roleCreated1.getId())));
        assertTrue(roles.stream().anyMatch(role -> role.getId().equals(roleCreated2.getId())));
    }

    @Test
    public void testNotFoundById() throws TechnicalException {
        RxJava2Adapter.monoToMaybe(roleRepository.findById_migrated("test")).test().assertEmpty();
    }

    @Test
    public void testCreate() throws TechnicalException {
        Role role = new Role();
        role.setName("testName");
        TestObserver<Role> testObserver = RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(r -> r.getName().equals(role.getName()));
    }

    @Test
    public void testUpdate() throws TechnicalException {
        // create role
        Role role = new Role();
        role.setName("testName");
        Role roleCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role))).block();

        // update role
        Role updatedRole = new Role();
        updatedRole.setId(roleCreated.getId());
        updatedRole.setName("testUpdatedName");

        TestObserver<Role> testObserver = RxJava2Adapter.monoToSingle(roleRepository.update_migrated(updatedRole)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(r -> r.getName().equals(updatedRole.getName()));
    }

    @Test
    public void testDelete() throws TechnicalException {
        // create role
        Role role = new Role();
        role.setName("testName");
        Role roleCreated = RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(roleRepository.create_migrated(role))).block();

        // fetch role
        TestObserver<Role> testObserver = RxJava2Adapter.monoToMaybe(roleRepository.findById_migrated(roleCreated.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(r -> r.getName().equals(roleCreated.getName()));

        // delete role
        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(roleRepository.delete_migrated(roleCreated.getId())).test();
        testObserver1.awaitTerminalEvent();

        // fetch role
        RxJava2Adapter.monoToMaybe(roleRepository.findById_migrated(roleCreated.getId())).test().assertEmpty();
    }

}

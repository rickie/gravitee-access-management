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

import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.AbstractManagementTest;
import io.reactivex.observers.TestObserver;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ScopeRepositoryTest extends AbstractManagementTest {
    @Autowired
    private ScopeRepository scopeRepository;

    @Test
    public void testFindByDomain() {
        // create scope
        Scope scope = new Scope();
        scope.setName("testName");
        scope.setDomain("testDomain");
        scope.setClaims(Arrays.asList("claim1", "claim2"));
        scopeRepository.create_migrated(scope).block();

        // fetch scopes
        TestObserver<Page<Scope>> testObserver = RxJava2Adapter.monoToSingle(scopeRepository.findByDomain_migrated("testDomain", 0, Integer.MAX_VALUE)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(scopes -> scopes.getData().size() == 1);
        testObserver.assertValue(scopes -> scopes.getData().iterator().next().getClaims().size() == 2);
    }

    @Test
    public void testFindByDomainAndKey() {
        // create scope
        Scope scope = new Scope();
        scope.setName("firstOne");
        scope.setKey("one");
        scope.setDomain("testDomain");
        scopeRepository.create_migrated(scope).block();

        scope.setId(null);
        scope.setName("anotherOne");
        scope.setDomain("another");
        scopeRepository.create_migrated(scope).block();


        // fetch scopes
        TestObserver<Scope> testObserver = RxJava2Adapter.monoToMaybe(scopeRepository.findByDomainAndKey_migrated("testDomain", "one")).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(result -> "firstOne".equals(result.getName()));
    }

    @Test
    public void testFindByDomainAndKeys() {
        // create scope
        Scope scope = new Scope();
        scope.setName("firstOne");
        scope.setKey("one");
        scope.setDomain("testDomain");
        Scope scopeCreated1 = scopeRepository.create_migrated(scope).block();

        scope.setId(null);
        scope.setName("anotherOne");
        scope.setDomain("another");
        scopeRepository.create_migrated(scope).block();

        scope.setId(null);
        scope.setName("secondOne");
        scope.setKey("two");
        scope.setDomain("testDomain");
        Scope scopeCreated2 = scopeRepository.create_migrated(scope).block();

        // fetch scopes
        TestObserver<List<Scope>> testObserver = RxJava2Adapter.monoToSingle(scopeRepository.findByDomainAndKeys_migrated("testDomain", Arrays.asList("one","two","three")).collectList()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(scopes -> scopes.size()==2 &&
                scopes.stream()
                        .map(Scope::getId)
                        .collect(Collectors.toList())
                        .containsAll(Arrays.asList(scopeCreated1.getId(), scopeCreated2.getId()))
        );
    }

    @Test
    public void testFindById() {
        // create scope
        Scope scope = buildScope();
        Scope scopeCreated = scopeRepository.create_migrated(scope).block();

        // fetch scope
        TestObserver<Scope> testObserver = RxJava2Adapter.monoToMaybe(scopeRepository.findById_migrated(scopeCreated.getId())).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(scope, testObserver);
    }

    private void assertEqualsTo(Scope scope, TestObserver<Scope> testObserver) {
        testObserver.assertValue(s -> s.getName().equals(scope.getName()));
        testObserver.assertValue(s -> s.getDescription().equals(scope.getDescription()));
        testObserver.assertValue(s -> s.getDomain().equals(scope.getDomain()));
        testObserver.assertValue(s -> s.getExpiresIn().equals(scope.getExpiresIn()));
        testObserver.assertValue(s -> s.isDiscovery() == scope.isDiscovery());
        testObserver.assertValue(s -> s.isParameterized() == scope.isParameterized());
        testObserver.assertValue(s -> s.isSystem() == scope.isSystem());
        testObserver.assertValue(s -> s.getIconUri().equals(scope.getIconUri()));
        testObserver.assertValue(s -> s.getKey().equals(scope.getKey()));
        testObserver.assertValue(s -> s.getClaims().containsAll(scope.getClaims()));
    }

    private Scope buildScope() {
        Scope scope = new Scope();
        String rand = UUID.randomUUID().toString();
        scope.setName("name"+rand);
        scope.setDescription("desc"+rand);
        scope.setDiscovery(true);
        scope.setDomain("domain"+rand);
        scope.setExpiresIn(321);
        scope.setIconUri("http://icon.acme.fr/"+rand);
        scope.setKey("key"+rand);
        scope.setSystem(true);
        scope.setParameterized(true);
        scope.setCreatedAt(new Date());
        scope.setUpdatedAt(new Date());
        scope.setClaims(Arrays.asList("claim1" + rand, "claim2" + rand));
        return scope;
    }

    @Test
    public void testNotFoundById() throws TechnicalException {
        RxJava2Adapter.monoToMaybe(scopeRepository.findById_migrated("test")).test().assertEmpty();
    }

    @Test
    public void testCreate() {
        Scope scope = new Scope();
        scope.setName("testName");
        scope.setSystem(true);
        scope.setClaims(Collections.emptyList());
        TestObserver<Scope> testObserver = RxJava2Adapter.monoToSingle(scopeRepository.create_migrated(scope)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(s -> s.getName().equals(scope.getName()));
    }

    @Test
    public void testUpdate() {
        // create scope
        Scope scope = buildScope();
        Scope scopeCreated = scopeRepository.create_migrated(scope).block();

        // update scope
        Scope updatedScope = buildScope();
        updatedScope.setId(scopeCreated.getId());

        TestObserver<Scope> testObserver = RxJava2Adapter.monoToSingle(scopeRepository.update_migrated(updatedScope)).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        assertEqualsTo(updatedScope, testObserver);
    }

    @Test
    public void testDelete() {
        // create scope
        Scope scope = new Scope();
        scope.setName("testName");
        Scope scopeCreated = scopeRepository.create_migrated(scope).block();

        // fetch scope
        TestObserver<Scope> testObserver = RxJava2Adapter.monoToMaybe(scopeRepository.findById_migrated(scopeCreated.getId())).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(s -> s.getName().equals(scope.getName()));

        // delete scope
        TestObserver testObserver1 = RxJava2Adapter.monoToCompletable(scopeRepository.delete_migrated(scopeCreated.getId())).test();
        testObserver1.awaitTerminalEvent();

        // fetch scope
        RxJava2Adapter.monoToMaybe(scopeRepository.findById_migrated(scopeCreated.getId())).test().assertEmpty();
    }

    @Test
    public void testSearch_wildcard() {
        String scopeName = "testName";
        Scope scope = new Scope();
        scope.setDomain("mydomain");
        scope.setName("testName");
        scope.setKey("testName");
        scope.setClaims(Arrays.asList("claim1", "claim2"));
        Scope scopeCreated = scopeRepository.create_migrated(scope).block();

        TestObserver<Page<Scope>> testObserver = RxJava2Adapter.monoToSingle(scopeRepository.search_migrated(scopeCreated.getDomain(), "*" + scopeName + "*", 0, Integer.MAX_VALUE)).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(s -> s.getData().size() == 1);
        testObserver.assertValue(scopes -> scopes.getData().iterator().next().getClaims().size() == 2);

    }
}

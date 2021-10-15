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
package io.gravitee.am.repository.oauth2.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.repository.oauth2.AbstractOAuthTest;
import io.gravitee.am.repository.oauth2.model.AccessToken;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.Arrays;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AccessTokenRepositoryTest extends AbstractOAuthTest {
    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Test
    public void shouldNotFindToken() {
        TestObserver<AccessToken> observer = accessTokenRepository.findByToken("unknown-token").test();

        observer.awaitTerminalEvent();

        observer.assertComplete();
        observer.assertValueCount(0);
        observer.assertNoErrors();
    }

    @Test
    public void shouldFindToken() {
        AccessToken token = new AccessToken();
        token.setId(RandomString.generate());
        token.setToken("my-token");

        TestObserver<AccessToken> observer = accessTokenRepository
                .create(token)
                .toCompletable().as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(accessTokenRepository.findByToken("my-token"))).as(RxJava2Adapter::monoToMaybe)
                .test();

        observer.awaitTerminalEvent();

        observer.assertComplete();
        observer.assertValueCount(1);
        observer.assertNoErrors();
    }

    @Test
    public void shouldDeleteByToken() {
        AccessToken token = new AccessToken();
        token.setId(RandomString.generate());
        token.setToken("my-token-todelete");

        TestObserver<AccessToken> observer = accessTokenRepository
                .create(token)
                .toCompletable().as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(accessTokenRepository.findByToken(token.getToken()))).as(RxJava2Adapter::monoToMaybe)
                .test();

        observer.awaitTerminalEvent();

        observer.assertComplete();
        observer.assertValueCount(1);
        observer.assertNoErrors();

        TestObserver<Void> testDelete = accessTokenRepository.delete(token.getToken()).test();
        testDelete.awaitTerminalEvent();
        testDelete.assertNoErrors();
    }

    @Test
    public void shouldFindAuthorizationCode() {
        AccessToken token = new AccessToken();
        token.setId(RandomString.generate());
        token.setToken("my-token");
        token.setAuthorizationCode("some-auth-code");

        TestObserver<AccessToken> observer = accessTokenRepository
                .create(token)
                .toCompletable()
                .andThen(accessTokenRepository.findByAuthorizationCode(token.getAuthorizationCode()))
                .test();

        observer.awaitTerminalEvent();

        observer.assertComplete();
        observer.assertValueCount(1);
        observer.assertNoErrors();
    }
    @Test
    public void shouldNotFindByUnknownAuthorizationCode() {
        AccessToken token = new AccessToken();
        token.setId(RandomString.generate());
        token.setToken("my-token");
        token.setAuthorizationCode("some-auth-code");

        TestObserver<AccessToken> observer = accessTokenRepository
                .create(token)
                .toCompletable()
                .andThen(accessTokenRepository.findByAuthorizationCode("unknown"))
                .test();

        observer.awaitTerminalEvent();
        observer.assertComplete();
        observer.assertNoValues();
        observer.assertNoErrors();
    }

    @Test
    public void shouldFindByClientIdAndSubject() {
        AccessToken token = new AccessToken();
        token.setId(RandomString.generate());
        token.setToken("my-token");
        token.setClient("my-client-id");
        token.setSubject("my-subject");

        TestObserver<AccessToken> observer = accessTokenRepository.create(token)
                .toCompletable()
                .andThen(accessTokenRepository.findByClientIdAndSubject("my-client-id", "my-subject"))
                .test();


        observer.awaitTerminalEvent();

        observer.assertComplete();
        observer.assertNoErrors();
        observer.assertValue(accessToken -> accessToken.getSubject().equals("my-subject") && accessToken.getClient().equals("my-client-id"));
    }

    @Test
    public void shouldFindByClientId() {
        AccessToken token = new AccessToken();
        token.setId(RandomString.generate());
        token.setToken("my-token");
        token.setClient("my-client-id-2");

        TestObserver<AccessToken> observer = accessTokenRepository.create(token)
                .toCompletable()
                .andThen(accessTokenRepository.findByClientId("my-client-id-2"))
                .test();

        observer.awaitTerminalEvent();
        observer.assertComplete();
        observer.assertNoErrors();
        observer.assertValueCount(1);
    }

    @Test
    public void shouldCountByClientId() {
        AccessToken token = new AccessToken();
        token.setId(RandomString.generate());
        token.setToken("my-token");
        token.setClient("my-client-id-count");

        TestObserver<Long> observer = RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(accessTokenRepository.create(token)
                .toCompletable()).then(RxJava2Adapter.singleToMono(accessTokenRepository.countByClientId("my-client-id-count"))))
                .test();

        observer.awaitTerminalEvent();
        observer.assertComplete();
        observer.assertNoErrors();
        observer.assertValue(new Long(1));
    }

    @Test
    public void shouldDeleteByDomainIdClientIdAndUserId() {
        AccessToken token1 = new AccessToken();
        token1.setId("my-token");
        token1.setToken("my-token");
        token1.setClient("client-id");
        token1.setDomain("domain-id");
        token1.setSubject("user-id");

        AccessToken token2 = new AccessToken();
        token2.setId("my-token2");
        token2.setToken("my-token2");
        token2.setClient("client-id2");
        token2.setDomain("domain-id2");
        token2.setSubject("user-id2");

        TestObserver<AccessToken> testObserver = RxJava2Adapter.completableToMono(accessTokenRepository
                .bulkWrite(Arrays.asList(token1, token2))).then(RxJava2Adapter.completableToMono(Completable.wrap(accessTokenRepository.deleteByDomainIdClientIdAndUserId("domain-id", "client-id", "user-id")))).then(RxJava2Adapter.maybeToMono(accessTokenRepository.findByToken("my-token"))).as(RxJava2Adapter::monoToMaybe)
                .test();
        testObserver.awaitTerminalEvent();
        assertEquals(0, testObserver.valueCount());

        assertNotNull(RxJava2Adapter.maybeToMono(accessTokenRepository.findByToken("my-token2")).block());
    }

    @Test
    public void shouldDeleteByDomainIdAndUserId() {
        AccessToken token1 = new AccessToken();
        token1.setId("my-token");
        token1.setToken("my-token");
        token1.setClient("client-id");
        token1.setDomain("domain-id");
        token1.setSubject("user-id");

        AccessToken token2 = new AccessToken();
        token2.setId("my-token2");
        token2.setToken("my-token2");
        token2.setClient("client-id2");
        token2.setDomain("domain-id2");
        token2.setSubject("user-id2");

        TestObserver<AccessToken> testObservable = RxJava2Adapter.completableToMono(accessTokenRepository
                .bulkWrite(Arrays.asList(token1, token2))).then(RxJava2Adapter.completableToMono(Completable.wrap(accessTokenRepository.deleteByDomainIdAndUserId("domain-id", "user-id")))).then(RxJava2Adapter.maybeToMono(accessTokenRepository.findByToken("my-token"))).as(RxJava2Adapter::monoToMaybe)
                .test();
        testObservable.awaitTerminalEvent();
        assertEquals(0, testObservable.valueCount());

        assertNotNull(RxJava2Adapter.maybeToMono(accessTokenRepository.findByToken("my-token2")).block());
    }

}

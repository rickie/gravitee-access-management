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
        TestObserver<AccessToken> observer = RxJava2Adapter.monoToMaybe(accessTokenRepository.findByToken_migrated("unknown-token")).test();

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

        TestObserver<AccessToken> observer = RxJava2Adapter.monoToSingle(accessTokenRepository.create_migrated(token))
                .toCompletable().as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(accessTokenRepository.findByToken_migrated("my-token")))).as(RxJava2Adapter::monoToMaybe)
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

        TestObserver<AccessToken> observer = RxJava2Adapter.monoToSingle(accessTokenRepository.create_migrated(token))
                .toCompletable().as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(accessTokenRepository.findByToken_migrated(token.getToken())))).as(RxJava2Adapter::monoToMaybe)
                .test();

        observer.awaitTerminalEvent();

        observer.assertComplete();
        observer.assertValueCount(1);
        observer.assertNoErrors();

        TestObserver<Void> testDelete = RxJava2Adapter.monoToCompletable(accessTokenRepository.delete_migrated(token.getToken())).test();
        testDelete.awaitTerminalEvent();
        testDelete.assertNoErrors();
    }

    @Test
    public void shouldFindAuthorizationCode() {
        AccessToken token = new AccessToken();
        token.setId(RandomString.generate());
        token.setToken("my-token");
        token.setAuthorizationCode("some-auth-code");

        TestObserver<AccessToken> observer = RxJava2Adapter.monoToSingle(accessTokenRepository.create_migrated(token))
                .toCompletable()
                .andThen(RxJava2Adapter.fluxToObservable(accessTokenRepository.findByAuthorizationCode_migrated(token.getAuthorizationCode())))
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

        TestObserver<AccessToken> observer = RxJava2Adapter.monoToSingle(accessTokenRepository.create_migrated(token))
                .toCompletable()
                .andThen(RxJava2Adapter.fluxToObservable(accessTokenRepository.findByAuthorizationCode_migrated("unknown")))
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

        TestObserver<AccessToken> observer = RxJava2Adapter.monoToSingle(accessTokenRepository.create_migrated(token))
                .toCompletable()
                .andThen(RxJava2Adapter.fluxToObservable(accessTokenRepository.findByClientIdAndSubject_migrated("my-client-id", "my-subject")))
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

        TestObserver<AccessToken> observer = RxJava2Adapter.monoToSingle(accessTokenRepository.create_migrated(token))
                .toCompletable()
                .andThen(RxJava2Adapter.fluxToObservable(accessTokenRepository.findByClientId_migrated("my-client-id-2")))
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

        TestObserver<Long> observer = RxJava2Adapter.monoToSingle(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToSingle(accessTokenRepository.create_migrated(token))
                .toCompletable()).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(accessTokenRepository.countByClientId_migrated("my-client-id-count")))))
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

        TestObserver<AccessToken> testObserver = RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(accessTokenRepository.bulkWrite_migrated(Arrays.asList(token1, token2)))).then(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(accessTokenRepository.deleteByDomainIdClientIdAndUserId_migrated("domain-id", "client-id", "user-id")))).then(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(accessTokenRepository.findByToken_migrated("my-token")))).as(RxJava2Adapter::monoToMaybe)
                .test();
        testObserver.awaitTerminalEvent();
        assertEquals(0, testObserver.valueCount());

        assertNotNull(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(accessTokenRepository.findByToken_migrated("my-token2"))).block());
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

        TestObserver<AccessToken> testObservable = RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(accessTokenRepository.bulkWrite_migrated(Arrays.asList(token1, token2)))).then(RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(accessTokenRepository.deleteByDomainIdAndUserId_migrated("domain-id", "user-id")))).then(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(accessTokenRepository.findByToken_migrated("my-token")))).as(RxJava2Adapter::monoToMaybe)
                .test();
        testObservable.awaitTerminalEvent();
        assertEquals(0, testObservable.valueCount());

        assertNotNull(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(accessTokenRepository.findByToken_migrated("my-token2"))).block());
    }

}

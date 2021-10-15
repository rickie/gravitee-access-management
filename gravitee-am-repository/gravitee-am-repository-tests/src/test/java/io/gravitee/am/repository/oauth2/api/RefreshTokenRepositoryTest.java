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
import io.gravitee.am.repository.oauth2.model.RefreshToken;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;
import java.util.Arrays;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RefreshTokenRepositoryTest extends AbstractOAuthTest {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    public void shouldNotFindToken() {
        TestObserver<RefreshToken> observer = refreshTokenRepository.findByToken("unknown-token").test();

        observer.awaitTerminalEvent();

        observer.assertComplete();
        observer.assertValueCount(0);
        observer.assertNoErrors();
    }

    @Test
    public void shouldFindToken() {
        RefreshToken token = new RefreshToken();
        token.setId(RandomString.generate());
        token.setToken("my-token");

        TestObserver<RefreshToken> observer = refreshTokenRepository
                .create(token)
                .toCompletable().as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(refreshTokenRepository.findByToken("my-token"))).as(RxJava2Adapter::monoToMaybe)
                .test();

        observer.awaitTerminalEvent();

        observer.assertComplete();
        observer.assertValueCount(1);
        observer.assertNoErrors();
    }

    @Test
    public void shouldDelete() {
        RefreshToken token = new RefreshToken();
        token.setId("my-token");
        token.setToken("my-token");

        TestObserver<RefreshToken> testObserver = RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(refreshTokenRepository
                .create(token)
                .toCompletable()).then(RxJava2Adapter.completableToMono(Completable.wrap(refreshTokenRepository.delete("my-token"))))).as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(refreshTokenRepository.findByToken("my-token"))).as(RxJava2Adapter::monoToMaybe)
                .test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoValues();
    }

    @Test
    public void shouldDeleteByDomainIdClientIdAndUserId() {
        RefreshToken token1 = new RefreshToken();
        token1.setId("my-token");
        token1.setToken("my-token");
        token1.setClient("client-id");
        token1.setDomain("domain-id");
        token1.setSubject("user-id");

        RefreshToken token2 = new RefreshToken();
        token2.setId("my-token2");
        token2.setToken("my-token2");
        token2.setClient("client-id2");
        token2.setDomain("domain-id2");
        token2.setSubject("user-id2");

        TestObserver<RefreshToken> testObserver = RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(refreshTokenRepository
                .bulkWrite(Arrays.asList(token1, token2))).then(RxJava2Adapter.completableToMono(Completable.wrap(refreshTokenRepository.deleteByDomainIdClientIdAndUserId("domain-id", "client-id", "user-id"))))).as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(refreshTokenRepository.findByToken("my-token"))).as(RxJava2Adapter::monoToMaybe)
                .test();
        testObserver.awaitTerminalEvent();

        assertEquals(0, testObserver.valueCount());

        assertNotNull(RxJava2Adapter.maybeToMono(refreshTokenRepository.findByToken("my-token2")).block());
    }

    @Test
    public void shouldDeleteByDomainIdAndUserId() {
        RefreshToken token1 = new RefreshToken();
        token1.setId("my-token");
        token1.setToken("my-token");
        token1.setClient("client-id");
        token1.setDomain("domain-id");
        token1.setSubject("user-id");

        RefreshToken token2 = new RefreshToken();
        token2.setId("my-token2");
        token2.setToken("my-token2");
        token2.setClient("client-id2");
        token2.setDomain("domain-id2");
        token2.setSubject("user-id2");

        TestObserver<RefreshToken> testObserver = RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(refreshTokenRepository
                .bulkWrite(Arrays.asList(token1, token2))).then(RxJava2Adapter.completableToMono(Completable.wrap(refreshTokenRepository.deleteByDomainIdAndUserId("domain-id", "user-id"))))).as(RxJava2Adapter::completableToMono).then(RxJava2Adapter.maybeToMono(refreshTokenRepository.findByToken("my-token"))).as(RxJava2Adapter::monoToMaybe)
                .test();
        testObserver.awaitTerminalEvent();
        assertEquals(0, testObserver.valueCount());

        assertNotNull(RxJava2Adapter.maybeToMono(refreshTokenRepository.findByToken("my-token2")).block());
    }

}

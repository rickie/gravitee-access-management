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
package io.gravitee.am.service.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.Application;
import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.repository.oauth2.api.RefreshTokenRepository;
import io.gravitee.am.service.ApplicationService;
import io.gravitee.am.service.TokenService;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.TotalToken;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class TokenServiceImpl implements TokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Autowired
    private ApplicationService applicationService;

    @Lazy
    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Lazy
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTotalTokensByDomain_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<TotalToken> findTotalTokensByDomain(String domain) {
 return RxJava2Adapter.monoToSingle(findTotalTokensByDomain_migrated(domain));
}
@Override
    public Mono<TotalToken> findTotalTokensByDomain_migrated(String domain) {
        LOGGER.debug("Find total tokens by domain: {}", domain);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationService.findByDomain_migrated(domain))
                .flatMapObservable(Observable::fromIterable)
                .flatMapSingle((io.gravitee.am.model.Application ident) -> RxJava2Adapter.monoToSingle(countByClientId_migrated(ident)))
                .toList()).flatMap(v->RxJava2Adapter.singleToMono((Single<TotalToken>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Long>, Single<TotalToken>>)totalAccessTokens -> {
                    TotalToken totalToken = new TotalToken();
                    totalToken.setTotalAccessTokens(totalAccessTokens.stream().mapToLong(Long::longValue).sum());
                    return RxJava2Adapter.monoToSingle(Mono.just(totalToken));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find total tokens by domain: {}", domain, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find total tokens by domain: %s", domain), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTotalTokensByApplication_migrated(application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<TotalToken> findTotalTokensByApplication(Application application) {
 return RxJava2Adapter.monoToSingle(findTotalTokensByApplication_migrated(application));
}
@Override
    public Mono<TotalToken> findTotalTokensByApplication_migrated(Application application) {
        LOGGER.debug("Find total tokens by application : {}", application);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(countByClientId_migrated(application).map(RxJavaReactorMigrationUtil.toJdkFunction(totalAccessTokens -> {
                    TotalToken totalToken = new TotalToken();
                    totalToken.setTotalAccessTokens(totalAccessTokens);
                    return totalToken;
                })))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find total tokens by application: {}", application, ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find total tokens by application: %s", application), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.findTotalTokens_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<TotalToken> findTotalTokens() {
 return RxJava2Adapter.monoToSingle(findTotalTokens_migrated());
}
@Override
    public Mono<TotalToken> findTotalTokens_migrated() {
        LOGGER.debug("Find total tokens");
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(applicationService.findAll_migrated())
                .flatMapObservable(Observable::fromIterable)
                .flatMapSingle((io.gravitee.am.model.Application ident) -> RxJava2Adapter.monoToSingle(countByClientId_migrated(ident)))
                .toList()).flatMap(v->RxJava2Adapter.singleToMono((Single<TotalToken>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Long>, Single<TotalToken>>)totalAccessTokens -> {
                    TotalToken totalToken = new TotalToken();
                    totalToken.setTotalAccessTokens(totalAccessTokens.stream().mapToLong(Long::longValue).sum());
                    return RxJava2Adapter.monoToSingle(Mono.just(totalToken));
                }).apply(v))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find total tokens", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to find total tokens", ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.deleteByUserId_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable deleteByUserId(String userId) {
 return RxJava2Adapter.monoToCompletable(deleteByUserId_migrated(userId));
}
@Override
    public Mono<Void> deleteByUserId_migrated(String userId) {
        LOGGER.debug("Delete tokens by user : {}", userId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(accessTokenRepository.deleteByUserId_migrated(userId).then(refreshTokenRepository.deleteByUserId_migrated(userId)))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to delete tokens by user {}", userId, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find total tokens by user: %s", userId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.countByClientId_migrated(application))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Long> countByClientId(Application application) {
 return RxJava2Adapter.monoToSingle(countByClientId_migrated(application));
}
private Mono<Long> countByClientId_migrated(Application application) {
        if (application.getSettings() == null) {
            return Mono.just(0l);
        }
        if (application.getSettings().getOauth() == null) {
            return Mono.just(0l);
        }
        return accessTokenRepository.countByClientId_migrated(application.getSettings().getOauth().getClientId());
    }
}

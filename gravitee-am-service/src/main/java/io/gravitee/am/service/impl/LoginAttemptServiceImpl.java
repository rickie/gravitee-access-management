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
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.LoginAttempt;
import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.repository.management.api.LoginAttemptRepository;
import io.gravitee.am.repository.management.api.search.LoginAttemptCriteria;
import io.gravitee.am.service.LoginAttemptService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.LoginAttemptNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Date;
import java.util.Optional;
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
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginAttemptServiceImpl.class);

    @Lazy
    @Autowired
    private LoginAttemptRepository loginAttemptRepository;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.loginFailed_migrated(criteria, accountSettings))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<LoginAttempt> loginFailed(LoginAttemptCriteria criteria, AccountSettings accountSettings) {
 return RxJava2Adapter.monoToSingle(loginFailed_migrated(criteria, accountSettings));
}
@Override
    public Mono<LoginAttempt> loginFailed_migrated(LoginAttemptCriteria criteria, AccountSettings accountSettings) {
        LOGGER.debug("Add login attempt for {}", criteria);
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(loginAttemptRepository.findByCriteria_migrated(criteria).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()))
                .flatMapSingle(optionalLoginAttempt -> {
                    if (optionalLoginAttempt.isPresent()) {
                        LoginAttempt loginAttempt = optionalLoginAttempt.get();
                        loginAttempt.setAttempts(loginAttempt.getAttempts() + 1);
                        if (loginAttempt.getAttempts() >= accountSettings.getMaxLoginAttempts()) {
                            loginAttempt.setExpireAt(new Date(System.currentTimeMillis() + (accountSettings.getAccountBlockedDuration() * 1000)));
                        }
                        loginAttempt.setUpdatedAt(new Date());
                        return RxJava2Adapter.monoToSingle(loginAttemptRepository.update_migrated(loginAttempt));
                    } else {
                        LoginAttempt loginAttempt = new LoginAttempt();
                        loginAttempt.setId(RandomString.generate());
                        loginAttempt.setDomain(criteria.domain());
                        loginAttempt.setClient(criteria.client());
                        loginAttempt.setIdentityProvider(criteria.identityProvider());
                        loginAttempt.setUsername(criteria.username());
                        loginAttempt.setAttempts(1);
                        if (loginAttempt.getAttempts() >= accountSettings.getMaxLoginAttempts()) {
                            loginAttempt.setExpireAt(new Date(System.currentTimeMillis() + (accountSettings.getAccountBlockedDuration() * 1000)));
                        } else {
                            loginAttempt.setExpireAt(new Date(System.currentTimeMillis() + (accountSettings.getLoginAttemptsResetTime() * 1000)));
                        }
                        loginAttempt.setCreatedAt(new Date());
                        loginAttempt.setUpdatedAt(loginAttempt.getCreatedAt());
                        return RxJava2Adapter.monoToSingle(loginAttemptRepository.create_migrated(loginAttempt));
                    }
                })).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<LoginAttempt>>toJdkFunction(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToSingle(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to add a login attempt", ex);
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException("An error occurs while trying to add a login attempt", ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.loginSucceeded_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable loginSucceeded(LoginAttemptCriteria criteria) {
 return RxJava2Adapter.monoToCompletable(loginSucceeded_migrated(criteria));
}
@Override
    public Mono<Void> loginSucceeded_migrated(LoginAttemptCriteria criteria) {
        LOGGER.debug("Delete login attempt for {}", criteria);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(loginAttemptRepository.delete_migrated(criteria))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to delete login attempt for", criteria, ex);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to delete login attempt: %s", criteria), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.reset_migrated(criteria))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable reset(LoginAttemptCriteria criteria) {
 return RxJava2Adapter.monoToCompletable(reset_migrated(criteria));
}
@Override
    public Mono<Void> reset_migrated(LoginAttemptCriteria criteria) {
        return loginSucceeded_migrated(criteria);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.checkAccount_migrated(criteria, accountSettings))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<LoginAttempt> checkAccount(LoginAttemptCriteria criteria, AccountSettings accountSettings) {
 return RxJava2Adapter.monoToMaybe(checkAccount_migrated(criteria, accountSettings));
}
@Override
    public Mono<LoginAttempt> checkAccount_migrated(LoginAttemptCriteria criteria, AccountSettings accountSettings) {
        LOGGER.debug("Check account status for {}", criteria);
        return loginAttemptRepository.findByCriteria_migrated(criteria);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<LoginAttempt> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<LoginAttempt> findById_migrated(String id) {
        LOGGER.debug("Find login attempt by id {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(loginAttemptRepository.findById_migrated(id).switchIfEmpty(Mono.error(new LoginAttemptNotFoundException(id))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToMaybe(Mono.error(ex));
                    }
                    LOGGER.error("An error occurs while trying to find login attempt by id {}", id, ex);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to fin login attempt by id: %s", id), ex)));
                }));
    }
}

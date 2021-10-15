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

import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.oauth2.ScopeApproval;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.repository.oauth2.api.RefreshTokenRepository;
import io.gravitee.am.repository.oauth2.api.ScopeApprovalRepository;
import io.gravitee.am.service.AuditService;
import io.gravitee.am.service.ScopeApprovalService;
import io.gravitee.am.service.UserService;
import io.gravitee.am.service.exception.AbstractManagementException;
import io.gravitee.am.service.exception.ScopeApprovalNotFoundException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.gravitee.am.service.reporter.builder.AuditBuilder;
import io.gravitee.am.service.reporter.builder.UserConsentAuditBuilder;
import io.reactivex.*;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Maybe;
import io.reactivex.functions.Function;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class ScopeApprovalServiceImpl implements ScopeApprovalService {

    public static final Logger LOGGER = LoggerFactory.getLogger(ScopeApprovalServiceImpl.class);

    @Lazy
    @Autowired
    private ScopeApprovalRepository scopeApprovalRepository;

    @Lazy
    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Lazy
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditService auditService;

    @Override
    public Maybe<ScopeApproval> findById(String id) {
        LOGGER.debug("Find scope approval by id: {}", id);
        return scopeApprovalRepository.findById(id)
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a scope approval by id: {}", id);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a scope approval by id %s", id), ex)));
                });
    }

    @Override
    public Flowable<ScopeApproval> findByDomainAndUser(String domain, String user) {
        LOGGER.debug("Find scope approvals by domain: {} and user: {}", domain, user);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(scopeApprovalRepository.findByDomainAndUser(domain, user)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a scope approval for domain: {} and user: {}", domain, user);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a scope approval for domain: %s and user: %s", domain, user), ex)));
                })));
    }

    @Override
    public Flowable<ScopeApproval> findByDomainAndUserAndClient(String domain, String user, String client) {
        LOGGER.debug("Find scope approvals by domain: {} and user: {} and client: {}", domain, user);
        return RxJava2Adapter.fluxToFlowable(RxJava2Adapter.flowableToFlux(scopeApprovalRepository.findByDomainAndUserAndClient(domain, user, client)).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a scope approval for domain: {}, user: {} and client: {}", domain, user, client);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a scope approval for domain: %s, user: %s and client: %s", domain, user, client), ex)));
                })));
    }

    @Override
    public Single<List<ScopeApproval>> saveConsent(String domain, Client client, List<ScopeApproval> approvals, User principal) {
        LOGGER.debug("Save approvals for user: {}", approvals.get(0).getUserId());
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(Observable.fromIterable(approvals)
                .flatMapSingle(scopeApprovalRepository::upsert)
                .toList()).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(__ -> auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).domain(domain).client(client).principal(principal).type(EventType.USER_CONSENT_CONSENTED).approvals(approvals)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).domain(domain).client(client).principal(principal).type(EventType.USER_CONSENT_CONSENTED).throwable(throwable)))))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to save consent for domain: {}, client: {} and user: {} ", domain, client.getId(), approvals.get(0).getUserId());
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to save consent for domain: %s, client: %s and user: %s", domain, client.getId(), approvals.get(0).getUserId()), ex)));
                });
    }

    @Override
    public Completable revokeByConsent(String domain, String userId, String consentId, User principal) {
        LOGGER.debug("Revoke approval for consent: {} and user: {}", consentId, userId);

        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(userService.findById(userId)).switchIfEmpty(Mono.error(new UserNotFoundException(userId))).flatMap(user->RxJava2Adapter.maybeToMono(scopeApprovalRepository.findById(consentId)).switchIfEmpty(RxJava2Adapter.maybeToMono(Maybe.error(new ScopeApprovalNotFoundException(consentId)))).flatMap(scopeApproval->RxJava2Adapter.completableToMono(scopeApprovalRepository.delete(consentId).doOnComplete(()->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user).approvals(Collections.singleton(scopeApproval)))).doOnError((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user).throwable(throwable)))).then(RxJava2Adapter.completableToMono(Completable.wrap(Completable.mergeArrayDelayError(accessTokenRepository.deleteByDomainIdClientIdAndUserId(scopeApproval.getDomain(), scopeApproval.getClientId(), scopeApproval.getUserId()), refreshTokenRepository.deleteByDomainIdClientIdAndUserId(scopeApproval.getDomain(), scopeApproval.getClientId(), scopeApproval.getUserId())))))).then()).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to revoke approval for scope: {}", consentId);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to revoke approval for scope: %s", consentId), ex)));
                });
    }

    @Override
    public Completable revokeByUser(String domain, String user, User principal) {
        LOGGER.debug("Revoke approvals for domain: {} and user: {}", domain, user);
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(userService.findById(user)).switchIfEmpty(Mono.error(new UserNotFoundException(user))).flatMap(user1->RxJava2Adapter.singleToMono(scopeApprovalRepository.findByDomainAndUser(domain, user).collect(HashSet<ScopeApproval>::new, Set::add)).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<HashSet<ScopeApproval>, CompletableSource>toJdkFunction((java.util.HashSet<io.gravitee.am.model.oauth2.ScopeApproval> scopeApprovals)->scopeApprovalRepository.deleteByDomainAndUser(domain, user).doOnComplete(()->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user1).approvals(scopeApprovals))).doOnError((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user1).throwable(throwable)))).apply(y)))).then(RxJava2Adapter.completableToMono(Completable.mergeArrayDelayError(accessTokenRepository.deleteByDomainIdAndUserId(domain, user), refreshTokenRepository.deleteByDomainIdAndUserId(domain, user))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to revoke scope approvals for domain: {} and user : {}", domain, user);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to revoke scope approvals for domain: %s and user: %s", domain, user), ex)));
                });
    }

    @Override
    public Completable revokeByUserAndClient(String domain, String user, String clientId, User principal) {
        LOGGER.debug("Revoke approvals for domain: {}, user: {} and client: {}", domain, user, clientId);
        return RxJava2Adapter.monoToCompletable(RxJava2Adapter.maybeToMono(userService.findById(user)).switchIfEmpty(Mono.error(new UserNotFoundException(user))).flatMap(user1->RxJava2Adapter.singleToMono(scopeApprovalRepository.findByDomainAndUserAndClient(domain, user, clientId).collect(HashSet<ScopeApproval>::new, Set::add)).flatMap(y->RxJava2Adapter.completableToMono(Completable.wrap(RxJavaReactorMigrationUtil.<HashSet<ScopeApproval>, CompletableSource>toJdkFunction((java.util.HashSet<io.gravitee.am.model.oauth2.ScopeApproval> scopeApprovals)->scopeApprovalRepository.deleteByDomainAndUserAndClient(domain, user, clientId).doOnComplete(()->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user1).approvals(scopeApprovals))).doOnError((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user1).throwable(throwable)))).apply(y)))).then(RxJava2Adapter.completableToMono(Completable.mergeArrayDelayError(accessTokenRepository.deleteByDomainIdClientIdAndUserId(domain, clientId, user), refreshTokenRepository.deleteByDomainIdClientIdAndUserId(domain, clientId, user))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to revoke scope approvals for domain: {}, user: {} and client: {}", domain, user, clientId);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to revoke scope approvals for domain: %s, user: %s and client: %s", domain, user, clientId), ex)));
                });

    }
}

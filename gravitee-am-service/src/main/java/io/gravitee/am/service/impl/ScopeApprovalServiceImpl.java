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

import io.reactivex.Maybe;
import io.reactivex.Single;

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

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<ScopeApproval> findById(String id) {
 return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
@Override
    public Mono<ScopeApproval> findById_migrated(String id) {
        LOGGER.debug("Find scope approval by id: {}", id);
        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(scopeApprovalRepository.findById_migrated(id))
                .onErrorResumeNext(ex -> {
                    LOGGER.error("An error occurs while trying to find a scope approval by id: {}", id);
                    return RxJava2Adapter.monoToMaybe(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a scope approval by id %s", id), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndUser_migrated(domain, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<ScopeApproval> findByDomainAndUser(String domain, String user) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndUser_migrated(domain, user));
}
@Override
    public Flux<ScopeApproval> findByDomainAndUser_migrated(String domain, String user) {
        LOGGER.debug("Find scope approvals by domain: {} and user: {}", domain, user);
        return scopeApprovalRepository.findByDomainAndUser_migrated(domain, user).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a scope approval for domain: {} and user: {}", domain, user);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a scope approval for domain: %s and user: %s", domain, user), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.fluxToFlowable(this.findByDomainAndUserAndClient_migrated(domain, user, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Flowable<ScopeApproval> findByDomainAndUserAndClient(String domain, String user, String client) {
 return RxJava2Adapter.fluxToFlowable(findByDomainAndUserAndClient_migrated(domain, user, client));
}
@Override
    public Flux<ScopeApproval> findByDomainAndUserAndClient_migrated(String domain, String user, String client) {
        LOGGER.debug("Find scope approvals by domain: {} and user: {} and client: {}", domain, user);
        return scopeApprovalRepository.findByDomainAndUserAndClient_migrated(domain, user, client).onErrorResume(RxJavaReactorMigrationUtil.toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to find a scope approval for domain: {}, user: {} and client: {}", domain, user, client);
                    return RxJava2Adapter.fluxToFlowable(Flux.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to find a scope approval for domain: %s, user: %s and client: %s", domain, user, client), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.saveConsent_migrated(domain, client, approvals, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<List<ScopeApproval>> saveConsent(String domain, Client client, List<ScopeApproval> approvals, User principal) {
 return RxJava2Adapter.monoToSingle(saveConsent_migrated(domain, client, approvals, principal));
}
@Override
    public Mono<List<ScopeApproval>> saveConsent_migrated(String domain, Client client, List<ScopeApproval> approvals, User principal) {
        LOGGER.debug("Save approvals for user: {}", approvals.get(0).getUserId());
        return RxJava2Adapter.singleToMono(Observable.fromIterable(approvals)
                .flatMapSingle((io.gravitee.am.model.oauth2.ScopeApproval ident) -> RxJava2Adapter.monoToSingle(scopeApprovalRepository.upsert_migrated(ident)))
                .toList()).doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(__ -> auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).domain(domain).client(client).principal(principal).type(EventType.USER_CONSENT_CONSENTED).approvals(approvals)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer(throwable -> auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).domain(domain).client(client).principal(principal).type(EventType.USER_CONSENT_CONSENTED).throwable(throwable)))).onErrorResume(err->RxJava2Adapter.singleToMono(RxJavaReactorMigrationUtil.<Throwable, Single<List<ScopeApproval>>>toJdkFunction(ex -> {
                    LOGGER.error("An error occurs while trying to save consent for domain: {}, client: {} and user: {} ", domain, client.getId(), approvals.get(0).getUserId());
                    return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to save consent for domain: %s, client: %s and user: %s", domain, client.getId(), approvals.get(0).getUserId()), ex)));
                }).apply(err)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByConsent_migrated(domain, userId, consentId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable revokeByConsent(String domain, String userId, String consentId, User principal) {
 return RxJava2Adapter.monoToCompletable(revokeByConsent_migrated(domain, userId, consentId, principal));
}
@Override
    public Mono<Void> revokeByConsent_migrated(String domain, String userId, String consentId, User principal) {
        LOGGER.debug("Revoke approval for consent: {} and user: {}", consentId, userId);

        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(userService.findById_migrated(userId).switchIfEmpty(Mono.error(new UserNotFoundException(userId))).flatMap(user->scopeApprovalRepository.findById_migrated(consentId).switchIfEmpty(Mono.error(new ScopeApprovalNotFoundException(consentId))).flatMap(scopeApproval->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(scopeApprovalRepository.delete_migrated(consentId)).doOnComplete(()->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user).approvals(Collections.singleton(scopeApproval))))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user).throwable(throwable)))).then(RxJava2Adapter.completableToMono(Completable.mergeArrayDelayError(RxJava2Adapter.monoToCompletable(accessTokenRepository.deleteByDomainIdClientIdAndUserId_migrated(scopeApproval.getDomain(), scopeApproval.getClientId(), scopeApproval.getUserId())), RxJava2Adapter.monoToCompletable(refreshTokenRepository.deleteByDomainIdClientIdAndUserId_migrated(scopeApproval.getDomain(), scopeApproval.getClientId(), scopeApproval.getUserId())))))).then()).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to revoke approval for scope: {}", consentId);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to revoke approval for scope: %s", consentId), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByUser_migrated(domain, user, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable revokeByUser(String domain, String user, User principal) {
 return RxJava2Adapter.monoToCompletable(revokeByUser_migrated(domain, user, principal));
}
@Override
    public Mono<Void> revokeByUser_migrated(String domain, String user, User principal) {
        LOGGER.debug("Revoke approvals for domain: {} and user: {}", domain, user);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(userService.findById_migrated(user).switchIfEmpty(Mono.error(new UserNotFoundException(user))).flatMap(user1->RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(scopeApprovalRepository.findByDomainAndUser_migrated(domain, user)).collect(HashSet<ScopeApproval>::new, Set::add)).flatMap(v->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(scopeApprovalRepository.deleteByDomainAndUser_migrated(domain, user)).doOnComplete(()->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user1).approvals(v)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user1).throwable(throwable))))).then(RxJava2Adapter.completableToMono(Completable.mergeArrayDelayError(RxJava2Adapter.monoToCompletable(accessTokenRepository.deleteByDomainIdAndUserId_migrated(domain, user)), RxJava2Adapter.monoToCompletable(refreshTokenRepository.deleteByDomainIdAndUserId_migrated(domain, user)))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to revoke scope approvals for domain: {} and user : {}", domain, user);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to revoke scope approvals for domain: %s and user: %s", domain, user), ex)));
                }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.revokeByUserAndClient_migrated(domain, user, clientId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable revokeByUserAndClient(String domain, String user, String clientId, User principal) {
 return RxJava2Adapter.monoToCompletable(revokeByUserAndClient_migrated(domain, user, clientId, principal));
}
@Override
    public Mono<Void> revokeByUserAndClient_migrated(String domain, String user, String clientId, User principal) {
        LOGGER.debug("Revoke approvals for domain: {}, user: {} and client: {}", domain, user, clientId);
        return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(userService.findById_migrated(user).switchIfEmpty(Mono.error(new UserNotFoundException(user))).flatMap(user1->RxJava2Adapter.singleToMono(RxJava2Adapter.fluxToFlowable(scopeApprovalRepository.findByDomainAndUserAndClient_migrated(domain, user, clientId)).collect(HashSet<ScopeApproval>::new, Set::add)).flatMap(v->RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(scopeApprovalRepository.deleteByDomainAndUserAndClient_migrated(domain, user, clientId)).doOnComplete(()->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user1).approvals(v)))).doOnError(RxJavaReactorMigrationUtil.toJdkConsumer((java.lang.Throwable throwable)->auditService.report(AuditBuilder.builder(UserConsentAuditBuilder.class).type(EventType.USER_CONSENT_REVOKED).domain(domain).principal(principal).user(user1).throwable(throwable))))).then(RxJava2Adapter.completableToMono(Completable.mergeArrayDelayError(RxJava2Adapter.monoToCompletable(accessTokenRepository.deleteByDomainIdClientIdAndUserId_migrated(domain, clientId, user)), RxJava2Adapter.monoToCompletable(refreshTokenRepository.deleteByDomainIdClientIdAndUserId_migrated(domain, clientId, user)))))).then())
                .onErrorResumeNext(ex -> {
                    if (ex instanceof AbstractManagementException) {
                        return RxJava2Adapter.monoToCompletable(Mono.error(ex));
                    }

                    LOGGER.error("An error occurs while trying to revoke scope approvals for domain: {}, user: {} and client: {}", domain, user, clientId);
                    return RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalManagementException(
                            String.format("An error occurs while trying to revoke scope approvals for domain: %s, user: %s and client: %s", domain, user, clientId), ex)));
                }));

    }
}

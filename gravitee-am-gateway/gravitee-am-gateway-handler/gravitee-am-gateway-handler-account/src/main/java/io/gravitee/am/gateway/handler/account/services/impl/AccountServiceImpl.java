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
package io.gravitee.am.gateway.handler.account.services.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.exception.oauth2.InvalidRequestException;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.gateway.handler.account.services.AccountService;
import io.gravitee.am.gateway.handler.common.audit.AuditReporterManager;
import io.gravitee.am.gateway.handler.common.auth.idp.IdentityProviderManager;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.model.*;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.reporter.api.audit.AuditReportableCriteria;
import io.gravitee.am.reporter.api.audit.model.Audit;
import io.gravitee.am.repository.management.api.UserRepository;
import io.gravitee.am.service.CredentialService;
import io.gravitee.am.service.FactorService;
import io.gravitee.am.service.UserService;
import io.gravitee.am.service.exception.CredentialNotFoundException;
import io.gravitee.am.service.exception.UserInvalidException;
import io.gravitee.am.service.exception.UserNotFoundException;
import io.gravitee.am.service.exception.UserProviderNotFoundException;
import io.gravitee.am.service.validators.UserValidator;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Donald Courtney (donald.courtney at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Autowired
    private Domain domain;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IdentityProviderManager identityProviderManager;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private UserService userService;

    @Autowired
    private FactorService factorService;

    @Autowired
    private AuditReporterManager auditReporterManager;

    @Autowired
    private CredentialService credentialService;

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.get_migrated(userId))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<User> get(String userId) {
 return RxJava2Adapter.monoToMaybe(get_migrated(userId));
}
@Override
    public Mono<User> get_migrated(String userId) {
        return userService.findById_migrated(userId);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getActivity_migrated(user, criteria, page, size))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Page<Audit>> getActivity(User user, AuditReportableCriteria criteria, int page, int size) {
 return RxJava2Adapter.monoToSingle(getActivity_migrated(user, criteria, page, size));
}
@Override
    public Mono<Page<Audit>> getActivity_migrated(User user, AuditReportableCriteria criteria, int page, int size) {
        try {
            Single<Page<Audit>> reporter = RxJava2Adapter.monoToSingle(auditReporterManager.getReporter().search_migrated(ReferenceType.DOMAIN, user.getReferenceId(), criteria, page, size));
            return RxJava2Adapter.singleToMono(reporter).map(RxJavaReactorMigrationUtil.toJdkFunction(result -> {
                if(Objects.isNull(result) || Objects.isNull(result.getData())){
                    return new Page<>(new ArrayList<>(), 0, 0);
                }
                return result;
            }));
        } catch (Exception ex) {
            LOGGER.error("An error occurs during audits search for {}}: {}", ReferenceType.DOMAIN, user.getReferenceId(), ex);
            return Mono.error(ex);
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> update(User user) {
 return RxJava2Adapter.monoToSingle(update_migrated(user));
}
@Override
    public Mono<User> update_migrated(User user) {
        LOGGER.debug("Update a user {} for domain {}", user.getUsername(), domain.getName());

        return userValidator.validate_migrated(user).then(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(identityProviderManager.getUserProvider_migrated(user.getSource()).switchIfEmpty(Mono.error(new UserProviderNotFoundException(user.getSource()))))
                .flatMapSingle(userProvider -> {
                    if (user.getExternalId() == null) {
                        return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRequestException("User does not exist in upstream IDP")));
                    } else {
                        return RxJava2Adapter.monoToSingle(userProvider.update_migrated(user.getExternalId(), convert(user)));
                    }
                })).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<io.gravitee.am.identityprovider.api.User, SingleSource<io.gravitee.am.model.User>>toJdkFunction(idpUser -> {
                    return RxJava2Adapter.monoToSingle(userRepository.update_migrated(user));
                }).apply(v)))))
                .onErrorResumeNext(ex -> {
                    if (ex instanceof UserNotFoundException || ex instanceof UserInvalidException) {
                        // idp user does not exist, only update AM user
                        // clear password
                        user.setPassword(null);
                        return RxJava2Adapter.monoToSingle(userRepository.update_migrated(user));
                    }
                    return RxJava2Adapter.monoToSingle(Mono.error(ex));
                })));

    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getFactors_migrated(domain))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<List<Factor>> getFactors(String domain) {
 return RxJava2Adapter.monoToSingle(getFactors_migrated(domain));
}
@Override
    public Mono<List<Factor>> getFactors_migrated(String domain) {
        return factorService.findByDomain_migrated(domain).collectList();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.getFactor_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Maybe<Factor> getFactor(String id) {
 return RxJava2Adapter.monoToMaybe(getFactor_migrated(id));
}
@Override
    public Mono<Factor> getFactor_migrated(String id) {
        return factorService.findById_migrated(id);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.upsertFactor_migrated(userId, enrolledFactor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<User> upsertFactor(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToSingle(upsertFactor_migrated(userId, enrolledFactor, principal));
}
@Override
    public Mono<User> upsertFactor_migrated(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
        return userService.upsertFactor_migrated(userId, enrolledFactor, principal);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.removeFactor_migrated(userId, factorId, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable removeFactor(String userId, String factorId, io.gravitee.am.identityprovider.api.User principal) {
 return RxJava2Adapter.monoToCompletable(removeFactor_migrated(userId, factorId, principal));
}
@Override
    public Mono<Void> removeFactor_migrated(String userId, String factorId, io.gravitee.am.identityprovider.api.User principal) {
        return userService.removeFactor_migrated(userId, factorId, principal);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getWebAuthnCredentials_migrated(user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<List<Credential>> getWebAuthnCredentials(User user) {
 return RxJava2Adapter.monoToSingle(getWebAuthnCredentials_migrated(user));
}
@Override
    public Mono<List<Credential>> getWebAuthnCredentials_migrated(User user) {
        return credentialService.findByUserId_migrated(ReferenceType.DOMAIN, user.getReferenceId(), user.getId()).map(RxJavaReactorMigrationUtil.toJdkFunction(credential -> {
                    removeSensitiveData(credential);
                    return credential;
                })).collectList();
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getWebAuthnCredential_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Credential> getWebAuthnCredential(String id) {
 return RxJava2Adapter.monoToSingle(getWebAuthnCredential_migrated(id));
}
@Override
    public Mono<Credential> getWebAuthnCredential_migrated(String id) {
        return credentialService.findById_migrated(id).switchIfEmpty(Mono.error(new CredentialNotFoundException(id))).map(RxJavaReactorMigrationUtil.toJdkFunction(credential -> {
                    removeSensitiveData(credential);
                    return credential;
                }));
    }

    private io.gravitee.am.identityprovider.api.User convert(io.gravitee.am.model.User user) {
        DefaultUser idpUser = new DefaultUser(user.getUsername());
        idpUser.setId(user.getExternalId());
        idpUser.setCredentials(user.getPassword());

        Map<String, Object> additionalInformation = new HashMap<>();
        if (user.getFirstName() != null) {
            idpUser.setFirstName(user.getFirstName());
            additionalInformation.put(StandardClaims.GIVEN_NAME, user.getFirstName());
        }
        if (user.getLastName() != null) {
            idpUser.setLastName(user.getLastName());
            additionalInformation.put(StandardClaims.FAMILY_NAME, user.getLastName());
        }
        if (user.getEmail() != null) {
            idpUser.setEmail(user.getEmail());
            additionalInformation.put(StandardClaims.EMAIL, user.getEmail());
        }
        if (user.getAdditionalInformation() != null) {
            user.getAdditionalInformation().forEach(additionalInformation::putIfAbsent);
        }
        idpUser.setAdditionalInformation(additionalInformation);
        return idpUser;
    }

    private void removeSensitiveData(Credential credential) {
        credential.setReferenceType(null);
        credential.setReferenceId(null);
        credential.setUserId(null);
        credential.setUsername(null);
        credential.setCounter(null);
    }

}

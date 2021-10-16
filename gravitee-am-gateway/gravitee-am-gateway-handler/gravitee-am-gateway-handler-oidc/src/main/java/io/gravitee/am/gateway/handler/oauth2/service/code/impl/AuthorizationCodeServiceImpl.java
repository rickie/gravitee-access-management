/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.gateway.handler.oauth2.service.code.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.common.utils.SecureRandomString;
import io.gravitee.am.gateway.handler.oauth2.exception.InvalidGrantException;
import io.gravitee.am.gateway.handler.oauth2.service.code.AuthorizationCodeService;
import io.gravitee.am.gateway.handler.oauth2.service.request.AuthorizationRequest;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.am.repository.oauth2.api.AuthorizationCodeRepository;
import io.gravitee.am.repository.oauth2.api.RefreshTokenRepository;
import io.gravitee.am.repository.oauth2.model.AuthorizationCode;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AuthorizationCodeServiceImpl implements AuthorizationCodeService {

  @Value("${authorization.code.validity:60000}")
  private int authorizationCodeValidity;

  @Lazy @Autowired private AuthorizationCodeRepository authorizationCodeRepository;

  @Lazy @Autowired private AccessTokenRepository accessTokenRepository;

  @Lazy @Autowired private RefreshTokenRepository refreshTokenRepository;

  @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(authorizationRequest, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
  public Single<AuthorizationCode> create(AuthorizationRequest authorizationRequest, User user) {
 return RxJava2Adapter.monoToSingle(create_migrated(authorizationRequest, user));
}
@Override
  public Mono<AuthorizationCode> create_migrated(AuthorizationRequest authorizationRequest, User user) {
    AuthorizationCode authorizationCode = new AuthorizationCode();
    authorizationCode.setId(RandomString.generate());
    authorizationCode.setTransactionId(authorizationRequest.transactionId());
    authorizationCode.setContextVersion(authorizationRequest.getContextVersion());
    authorizationCode.setCode(SecureRandomString.generate());
    authorizationCode.setClientId(authorizationRequest.getClientId());
    authorizationCode.setSubject(user.getId());
    authorizationCode.setScopes(authorizationRequest.getScopes());
    authorizationCode.setRequestParameters(authorizationRequest.parameters());
    authorizationCode.setExpireAt(new Date(System.currentTimeMillis() + authorizationCodeValidity));
    authorizationCode.setCreatedAt(new Date());

    return authorizationCodeRepository.create_migrated(authorizationCode);
  }

  @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.remove_migrated(code, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
  public Maybe<AuthorizationCode> remove(String code, Client client) {
 return RxJava2Adapter.monoToMaybe(remove_migrated(code, client));
}
@Override
  public Mono<AuthorizationCode> remove_migrated(String code, Client client) {
    return authorizationCodeRepository.findByCode_migrated(code).switchIfEmpty(handleInvalidCode_migrated(code)).flatMap(v->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<AuthorizationCode, MaybeSource<AuthorizationCode>>toJdkFunction(authorizationCode -> {
                          if (!authorizationCode.getClientId().equals(client.getClientId())) {
                            return RxJava2Adapter.monoToMaybe(Mono.error(new InvalidGrantException(
                                    "The authorization code "
                                        + code
                                        + " does not belong to the client "
                                        + client.getClientId()
                                        + ".")));
                          }
                          return RxJava2Adapter.monoToMaybe(Mono.just(authorizationCode));
                        }).apply(v))))
            .flatMap(
                z ->
                    authorizationCodeRepository.delete_migrated(z.getId()));
  }

  
private Mono<AuthorizationCode> handleInvalidCode_migrated(String code) {
    // The client MUST NOT use the authorization code more than once.
    // If an authorization code is used more than once, the authorization server MUST deny the
    // request and SHOULD
    // revoke (when possible) all tokens previously issued based on that authorization code.
    // https://tools.ietf.org/html/rfc6749#section-4.1.2
    return RxJava2Adapter.completableToMono(
                RxJava2Adapter.fluxToObservable(accessTokenRepository.findByAuthorizationCode_migrated(code))
                    .flatMapCompletable(
                        accessToken -> {
                          Completable deleteAccessTokenAction =
                              RxJava2Adapter.monoToCompletable(accessTokenRepository.delete_migrated(accessToken.getToken()));
                          if (accessToken.getRefreshToken() != null) {
                            RxJava2Adapter.monoToCompletable(RxJava2Adapter.completableToMono(deleteAccessTokenAction).then(refreshTokenRepository.delete_migrated(accessToken.getRefreshToken())));
                          }
                          return deleteAccessTokenAction;
                        }))
            .then(
                Mono.error(new InvalidGrantException(
                                "The authorization code " + code + " is invalid.")));
  }
}

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
package io.gravitee.am.gateway.handler.oidc.service.jwe.impl;

import static io.gravitee.am.gateway.handler.oidc.service.utils.JWAlgorithmUtils.*;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.crypto.impl.*;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import io.gravitee.am.common.exception.oauth2.InvalidRequestObjectException;
import io.gravitee.am.common.exception.oauth2.OAuth2Exception;
import io.gravitee.am.common.exception.oauth2.ServerErrorException;
import io.gravitee.am.gateway.handler.oidc.service.jwe.JWEService;
import io.gravitee.am.gateway.handler.oidc.service.jwk.JWKFilter;
import io.gravitee.am.gateway.handler.oidc.service.jwk.JWKService;
import io.gravitee.am.gateway.handler.oidc.service.jwk.converter.JWKConverter;
import io.gravitee.am.gateway.handler.oidc.service.utils.JWAlgorithmUtils;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.jose.*;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.oidc.JWKSet;
import io.gravitee.am.service.exception.InvalidClientMetadataException;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Optional;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class JWEServiceImpl implements JWEService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JWEServiceImpl.class);

    @Autowired
    private JWKService jwkService;

    @Autowired
    private Domain domain;

    @Override
    public Single<String> encryptIdToken(String signedJwt, Client client) {
        //Return input without encryption if client does not require JWE or algorithm is set to none
        if (client.getIdTokenEncryptedResponseAlg() == null || JWEAlgorithm.NONE.getName().equalsIgnoreCase(client.getIdTokenEncryptedResponseAlg())) {
            return RxJava2Adapter.monoToSingle(Mono.just(signedJwt));
        }

        JWEObject jwe = new JWEObject(
                new JWEHeader.Builder(
                        JWEAlgorithm.parse(client.getIdTokenEncryptedResponseAlg()),
                        EncryptionMethod.parse(client.getIdTokenEncryptedResponseEnc()!=null?client.getIdTokenEncryptedResponseEnc(): JWAlgorithmUtils.getDefaultIdTokenResponseEnc())
                ).contentType("JWT").build(),
                new Payload(signedJwt)
        );

        return encrypt(jwe,client)
                .onErrorResumeNext(throwable -> {
                    if(throwable instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(throwable));
                    }
                    LOGGER.error(throwable.getMessage(), throwable);
                    return RxJava2Adapter.monoToSingle(Mono.error(new ServerErrorException("Unable to encrypt id_token")));
                });
    }

    @Override
    public Single<String> encryptUserinfo(String signedJwt, Client client) {
        //Return input without encryption if client does not require JWE or algorithm is set to none
        if(client.getUserinfoEncryptedResponseAlg()==null || JWEAlgorithm.NONE.getName().equalsIgnoreCase(client.getUserinfoEncryptedResponseAlg())) {
            return RxJava2Adapter.monoToSingle(Mono.just(signedJwt));
        }

        JWEObject jwe = new JWEObject(
                new JWEHeader.Builder(
                        JWEAlgorithm.parse(client.getUserinfoEncryptedResponseAlg()),
                        EncryptionMethod.parse(client.getUserinfoEncryptedResponseEnc()!=null?client.getUserinfoEncryptedResponseEnc(): JWAlgorithmUtils.getDefaultUserinfoResponseEnc())
                ).contentType("JWT").build(),
                new Payload(signedJwt)
        );

        return encrypt(jwe,client)
                .onErrorResumeNext(throwable -> {
                    if(throwable instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(throwable));
                    }
                    LOGGER.error(throwable.getMessage(), throwable);
                    return RxJava2Adapter.monoToSingle(Mono.error(new ServerErrorException("Unable to encrypt userinfo")));
                });
    }

    @Override
    public Single<Boolean> isEncrypted(String jwt) {
        try {
            return RxJava2Adapter.monoToSingle(Mono.just(JWTParser.parse(jwt) instanceof EncryptedJWT));
        } catch (Exception ex) {
            return RxJava2Adapter.monoToSingle(Mono.error(ex));
        }
    }

    @Override
    public Single<JWT> decrypt(String jwt, boolean encRequired) {
        return decrypt(jwt, null, encRequired);
    }

    @Override
    public Single<JWT> decrypt(String jwt, Client client, boolean encRequired) {
        try {
            // Parse a first time to check if the JWT is encrypted
            JWT parsedJwt = JWTParser.parse(jwt);

            if (parsedJwt instanceof EncryptedJWT) {

                JWEObject jweObject = JWEObject.parse(jwt);

                JWEAlgorithm algorithm = jweObject.getHeader().getAlgorithm();

                if (this.domain.useFapiBrazilProfile() &&
                        !(isKeyEncCompliantWithFapiBrazil(algorithm.getName()) &&
                                isContentEncCompliantWithFapiBrazil(jweObject.getHeader().getEncryptionMethod().getName()))) {
                    return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRequestObjectException("Request object must be encrypted using RSA-OAEP with A256GCM")));
                }

                //RSA decryption
                if (RSACryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
                    return decrypt(jweObject, client, JWKFilter.RSA_KEY_ENCRYPTION(), jwk ->
                            new RSADecrypter(JWKConverter.convert((RSAKey) jwk))
                    );
                }
                //Curve decryption (Elliptic "EC" & Edward "OKP")
                else if (ECDHCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
                    return decrypt(jweObject, client, JWKFilter.CURVE_KEY_ENCRYPTION(), jwk -> {
                        if (KeyType.EC.getValue().equals(jwk.getKty())) {
                            return new ECDHDecrypter(JWKConverter.convert((ECKey) jwk));
                        }
                        return new X25519Decrypter(JWKConverter.convert((OKPKey) jwk));
                    });
                }
                //AES decryption ("OCT" keys)
                else if (AESCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
                    return decrypt(jweObject, client, JWKFilter.OCT_KEY_ENCRYPTION(algorithm), jwk ->
                            new AESDecrypter(JWKConverter.convert((OCTKey) jwk))
                    );
                }
                //Direct decryption ("OCT" keys)
                else if (DirectCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
                    return decrypt(jweObject, client, JWKFilter.OCT_KEY_ENCRYPTION(jweObject.getHeader().getEncryptionMethod()), jwk ->
                            new DirectDecrypter(JWKConverter.convert((OCTKey) jwk))
                    );
                }
                //Password Base decryption ("OCT" keys)
                else if (PasswordBasedCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
                    return decrypt(jweObject, client, JWKFilter.OCT_KEY_ENCRYPTION(), jwk -> {
                        OctetSequenceKey octKey = JWKConverter.convert((OCTKey) jwk);
                        return new PasswordBasedDecrypter(octKey.getKeyValue().decode());
                    });
                }

                return RxJava2Adapter.monoToSingle(Mono.error(new ServerErrorException("Unable to perform Json Web Decryption, unsupported algorithm: " + algorithm.getName())));
            } else if (encRequired) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidRequestObjectException("Request Object must be encrypted")));
            } else {
                return RxJava2Adapter.monoToSingle(Mono.just(parsedJwt));
            }
        } catch (Exception ex) {
            return RxJava2Adapter.monoToSingle(Mono.error(ex));
        }
    }

    private Single<JWT> decrypt(JWEObject jwe, Client client, Predicate<JWK> filter, JWEDecrypterFunction<JWK, JWEDecrypter> function) {
        final Maybe<JWKSet> jwks = client != null ? jwkService.getKeys(client) : jwkService.getDomainPrivateKeys();
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(jwks
                .flatMapPublisher(jwkset -> RxJava2Adapter.fluxToFlowable(Flux.fromIterable(jwkset.getKeys())))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(filter::test)).filter(RxJavaReactorMigrationUtil.toJdkPredicate(jwk -> jwk.getUse() == null || jwk.getUse().equals(KeyUse.ENCRYPTION.getValue()))).filter(RxJavaReactorMigrationUtil.toJdkPredicate(jwk -> jwe.getHeader().getKeyID() == null || jwe.getHeader().getKeyID().equals(jwk.getKid()))).map(RxJavaReactorMigrationUtil.toJdkFunction(function::apply)).map(RxJavaReactorMigrationUtil.toJdkFunction(decrypter -> {
                    try {
                        jwe.decrypt(decrypter);
                        return Optional.<JWT>ofNullable(jwe.getPayload().toSignedJWT());
                    } catch (Exception e) {
                        return Optional.<JWT>empty();
                    }
                })).filter(RxJavaReactorMigrationUtil.toJdkPredicate(Optional::isPresent)).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::get)).next().single());
    }

    public Single<String> encryptAuthorization(String signedJwt, Client client) {
        //Return input without encryption if client does not require JWE or algorithm is set to none
        if (client.getAuthorizationEncryptedResponseAlg() == null || JWEAlgorithm.NONE.getName().equals(client.getAuthorizationEncryptedResponseAlg())) {
            return RxJava2Adapter.monoToSingle(Mono.just(signedJwt));
        }

        JWEObject jwe = new JWEObject(
                new JWEHeader.Builder(
                        JWEAlgorithm.parse(client.getAuthorizationEncryptedResponseAlg()),
                        EncryptionMethod.parse(client.getAuthorizationEncryptedResponseEnc()!=null?client.getAuthorizationEncryptedResponseEnc(): JWAlgorithmUtils.getDefaultAuthorizationResponseEnc())
                ).contentType("JWT").build(),
                new Payload(signedJwt)
        );

        return encrypt(jwe,client)
                .onErrorResumeNext(throwable -> {
                    if(throwable instanceof OAuth2Exception) {
                        return RxJava2Adapter.monoToSingle(Mono.error(throwable));
                    }
                    LOGGER.error(throwable.getMessage(), throwable);
                    return RxJava2Adapter.monoToSingle(Mono.error(new ServerErrorException("Unable to encrypt authorization")));
                });
    }

    private Single<String> encrypt(JWEObject jwe, Client client) {

        JWEAlgorithm algorithm = jwe.getHeader().getAlgorithm();

        //RSA encryption
        if(RSACryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            return encrypt(jwe, client, JWKFilter.RSA_KEY_ENCRYPTION(), jwk ->
                    new RSAEncrypter(JWKConverter.convert((RSAKey) jwk))
            );
        }
        //Curve encryption (Elliptic "EC" & Edward "OKP")
        else if(ECDHCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            return encrypt(jwe, client, JWKFilter.CURVE_KEY_ENCRYPTION(), jwk -> {
                if(KeyType.EC.getValue().equals(jwk.getKty())) {
                    return new ECDHEncrypter(JWKConverter.convert((ECKey) jwk));
                }
                return new X25519Encrypter(JWKConverter.convert((OKPKey) jwk));
            });
        }
        //AES encryption ("OCT" keys)
        else if(AESCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            return encrypt(jwe, client, JWKFilter.OCT_KEY_ENCRYPTION(algorithm), jwk ->
                    new AESEncrypter(JWKConverter.convert((OCTKey) jwk))
            );
        }
        //Direct encryption ("OCT" keys)
        else if(DirectCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            return encrypt(jwe, client, JWKFilter.OCT_KEY_ENCRYPTION(jwe.getHeader().getEncryptionMethod()), jwk ->
                    new DirectEncrypter(JWKConverter.convert((OCTKey) jwk))
            );
        }
        //Password Base Encryption ("OCT" keys)
        else if(PasswordBasedCryptoProvider.SUPPORTED_ALGORITHMS.contains(algorithm)) {
            return encrypt(jwe, client, JWKFilter.OCT_KEY_ENCRYPTION(), jwk -> {
                OctetSequenceKey octKey = JWKConverter.convert((OCTKey) jwk);
                return new PasswordBasedEncrypter(
                        octKey.getKeyValue().decode(),
                        PasswordBasedEncrypter.MIN_SALT_LENGTH,
                        PasswordBasedEncrypter.MIN_RECOMMENDED_ITERATION_COUNT
                );
            });
        }
        return RxJava2Adapter.monoToSingle(Mono.error(new ServerErrorException("Unable to perform Json Web Encryption, unsupported algorithm: "+algorithm.getName())));
    }

    private Single<String> encrypt(JWEObject jwe, Client client, Predicate<JWK> filter, JWEEncrypterFunction<JWK, JWEEncrypter> function) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(jwkService.getKeys(client)).flatMap(z->jwkService.filter(z, filter).as(RxJava2Adapter::maybeToMono)).switchIfEmpty(Mono.error(new InvalidClientMetadataException("no matching key found to encrypt"))))
                .flatMapSingle(jwk -> RxJava2Adapter.monoToSingle(Mono.just(function.apply(jwk))))).map(RxJavaReactorMigrationUtil.toJdkFunction(encrypter -> {
                    jwe.encrypt(encrypter);
                    return jwe.serialize();
                })));
    }

    @FunctionalInterface
    private interface JWEDecrypterFunction<JWK, JWEDecrypter> {
        JWEDecrypter apply(JWK jwk) throws JOSEException;
    }

    @FunctionalInterface
    private interface JWEEncrypterFunction<JWK, JWEEncrypter> {
        JWEEncrypter apply(JWK jwk) throws JOSEException;
    }
}

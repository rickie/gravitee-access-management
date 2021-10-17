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
package io.gravitee.am.certificate.pkcs12.provider;


import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyOperation;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.util.Base64;
import io.gravitee.am.certificate.api.*;
import io.gravitee.am.certificate.pkcs12.PKCS12Configuration;
import io.gravitee.am.common.jwt.SignatureAlgorithm;
import io.gravitee.am.model.jose.JWK;
import io.gravitee.am.model.jose.RSAKey;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PKCS12Provider implements CertificateProvider, InitializingBean {

    private final static String KEYSTORE_TYPE = "pkcs12";

    private KeyPair keyPair;
    private Certificate cert;
    private JWKSet jwkSet;
    private Set<JWK> keys;
    private SignatureAlgorithm signature = SignatureAlgorithm.RS256;
    private io.gravitee.am.certificate.api.Key certificateKey;
    private List<CertificateKey> certificateKeys;

    @Autowired
    private PKCS12Configuration configuration;

    @Autowired
    private CertificateMetadata certificateMetadata;

    @Override
    public void afterPropertiesSet() throws Exception {
        Object file = certificateMetadata.getMetadata().get(CertificateMetadata.FILE);
        Objects.requireNonNull(file, "A .p12 / .pfx  file is required to use PKCS#12 certificate");

        try (InputStream is = new ByteArrayInputStream((byte[]) file)) {
            KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
            keystore.load(is, configuration.getStorepass().toCharArray());

            // generate JWK set
            // TODO : should be moved to the gravitee-am-jwt module
            jwkSet = JWKSet.load(keystore, name -> configuration.getKeypass().toCharArray());
            keys = getKeys();
            // generate Key pair
            Key key = keystore.getKey(configuration.getAlias(), configuration.getKeypass().toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                cert = keystore.getCertificate(configuration.getAlias());
                // create key pair
                keyPair = new KeyPair(cert.getPublicKey(), (PrivateKey) key);
                // create key
                certificateKey = new DefaultKey(configuration.getAlias(), keyPair);
                // update metadata
                certificateMetadata.getMetadata().put(CertificateMetadata.DIGEST_ALGORITHM_NAME, signature.getDigestName());
                // generate public certificate keys
                certificateKeys = new ArrayList<>();
                // Get Signing Algorithm name
                if (cert instanceof X509Certificate) {
                    signature = getSignature(((X509Certificate) cert).getSigAlgName());
                    String pem = X509CertUtils.toPEMString((X509Certificate) cert);
                    certificateKeys.add(new CertificateKey(CertificateFormat.PEM, pem));
                }
                certificateKeys.add(new CertificateKey(CertificateFormat.SSH_RSA, RSAKeyUtils.toSSHRSAString((RSAPublicKey) keyPair.getPublic())));
            } else {
                throw new IllegalArgumentException("A RSA Signer must be supplied");
            }
        }
    }

    
@Override
    public Flux<JWK> privateKey_migrated() {
        // CertificateProvider only manage RSA key.
        com.nimbusds.jose.jwk.JWK nimbusJwk = new com.nimbusds.jose.jwk.RSAKey.Builder((RSAPublicKey) ((KeyPair) certificateKey.getValue()).getPublic())
                .privateKey((RSAPrivateKey) ((KeyPair) certificateKey.getValue()).getPrivate())
                .keyID(configuration.getAlias())
                .build();
        return Flux.fromIterable(convert(nimbusJwk, true).collect(Collectors.toList()));
    }

    
@Override
    public Mono<io.gravitee.am.certificate.api.Key> key_migrated() {
        return Mono.just(certificateKey);
    }

    
@Override
    public Mono<String> publicKey_migrated() {
        // fallback to ssh-rsa
        return Mono.just(certificateKeys
                        .stream()
                        .filter(c -> c.getFmt().equals(CertificateFormat.SSH_RSA))
                        .map(CertificateKey::getPayload)
                        .findFirst()
                        .get());
    }

    
@Override
    public Mono<List<CertificateKey>> publicKeys_migrated() {
        return Mono.just(certificateKeys);
    }

    
@Override
    public Flux<JWK> keys_migrated() {
        return Flux.fromIterable(keys);
    }

    @Override
    public CertificateMetadata certificateMetadata() {
        return certificateMetadata;
    }

    @Override
    public Certificate certificate() {
        return cert;
    }

    private Set<JWK> getKeys() {
        return jwkSet.toPublicJWKSet().getKeys().stream().flatMap(this::convert).collect(Collectors.toSet());
    }

    // TODO : should be moved to the gravitee-am-jwt module
    private Stream<JWK> convert(com.nimbusds.jose.jwk.JWK nimbusJwk) {
        final Set<String> useFor = configuration.getUse() == null || configuration.getUse().isEmpty() ? Set.of(KeyUse.SIGNATURE.getValue()) : configuration.getUse();
        return useFor.stream().map(use -> createRSAKey(nimbusJwk, false, use));
    }

    private Stream<JWK> convert(com.nimbusds.jose.jwk.JWK nimbusJwk, boolean includePrivate) {
        final Set<String> useFor = configuration.getUse() == null || configuration.getUse().isEmpty() ? Set.of(KeyUse.SIGNATURE.getValue()) : configuration.getUse();
        return useFor.stream().map(use -> createRSAKey(nimbusJwk, includePrivate, use));
    }

    private JWK createRSAKey(com.nimbusds.jose.jwk.JWK nimbusJwk, boolean includePrivate, String use) {
        RSAKey jwk = new RSAKey();
        if (nimbusJwk.getKeyType() != null) {
            jwk.setKty(nimbusJwk.getKeyType().getValue());
        }

        if (nimbusJwk.getKeyUse() != null) {
            jwk.setUse(nimbusJwk.getKeyUse().identifier());
        } else {
            jwk.setUse(use);
        }

        if (nimbusJwk.getKeyOperations() != null) {
            jwk.setKeyOps(nimbusJwk.getKeyOperations().stream().map(KeyOperation::identifier).collect(Collectors.toSet()));
        }

        if (configuration.getAlgorithm() != null && !configuration.getAlgorithm().isEmpty()) {
            jwk.setAlg(configuration.getAlgorithm());
        } else if (nimbusJwk.getAlgorithm() != null) {
            jwk.setAlg(nimbusJwk.getAlgorithm().getName());
        }
        if (nimbusJwk.getKeyID() != null) {
            jwk.setKid(nimbusJwk.getKeyID());
        }
        if (nimbusJwk.getX509CertURL() != null) {
            jwk.setX5u(nimbusJwk.getX509CertURL().toString());
        }
        if (nimbusJwk.getX509CertChain() != null) {
            jwk.setX5c(nimbusJwk.getX509CertChain().stream().map(Base64::toString).collect(Collectors.toSet()));
        }
        if (nimbusJwk.getX509CertThumbprint() != null) {
            jwk.setX5t(nimbusJwk.getX509CertThumbprint().toString());
        }
        if (nimbusJwk.getX509CertSHA256Thumbprint() != null) {
            jwk.setX5tS256(nimbusJwk.getX509CertSHA256Thumbprint().toString());
        }

        // specific RSA Key
        com.nimbusds.jose.jwk.RSAKey nimbusRSAJwk = (com.nimbusds.jose.jwk.RSAKey) nimbusJwk;
        if (nimbusRSAJwk.getPublicExponent() != null) {
            jwk.setE(nimbusRSAJwk.getPublicExponent().toString());
        }
        if (nimbusRSAJwk.getModulus() != null) {
            jwk.setN(nimbusRSAJwk.getModulus().toString());
        }

        if (includePrivate) {
            if (nimbusRSAJwk.getPrivateExponent() != null) {
                jwk.setD(nimbusRSAJwk.getPrivateExponent().toString());
            }

            if (nimbusRSAJwk.getFirstPrimeFactor() != null) {
                jwk.setP(nimbusRSAJwk.getFirstPrimeFactor().toString());
            }

            if (nimbusRSAJwk.getFirstFactorCRTExponent() != null) {
                jwk.setDp(nimbusRSAJwk.getFirstFactorCRTExponent().toString());
            }

            if (nimbusRSAJwk.getFirstCRTCoefficient() != null) {
                jwk.setQi(nimbusRSAJwk.getFirstCRTCoefficient().toString());
            }

            if (nimbusRSAJwk.getSecondPrimeFactor() != null) {
                jwk.setQ(nimbusRSAJwk.getSecondPrimeFactor().toString());
            }

            if (nimbusRSAJwk.getSecondFactorCRTExponent() != null) {
                jwk.setDq(nimbusRSAJwk.getSecondFactorCRTExponent().toString());
            }
        }
        return jwk;
    }

    private SignatureAlgorithm getSignature(String signingAlgorithm) {
        return Stream.of(SignatureAlgorithm.values())
                .filter(signatureAlgorithm -> signatureAlgorithm.getJcaName() != null)
                .filter(signatureAlgorithm -> signatureAlgorithm.getJcaName().equals(signingAlgorithm))
                .findFirst()
                .orElse(SignatureAlgorithm.RS256);
    }

    @Override
    public String signatureAlgorithm() {
        if (configuration.getAlgorithm() != null) {
            return configuration.getAlgorithm();
        }
        return signature.getValue();
    }
}

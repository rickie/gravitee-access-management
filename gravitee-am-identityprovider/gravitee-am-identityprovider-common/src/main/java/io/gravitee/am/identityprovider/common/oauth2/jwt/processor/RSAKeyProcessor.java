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
package io.gravitee.am.identityprovider.common.oauth2.jwt.processor;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;

import io.gravitee.am.common.jwt.SignatureAlgorithm;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RSAKeyProcessor<C extends SecurityContext> extends AbstractKeyProcessor<C> {

    @Override
    JWSKeySelector<C> jwsKeySelector(JWKSource<C> jwkSource, SignatureAlgorithm signature) {
        return new JWSVerificationKeySelector<C>(
                JWSAlgorithm.parse(signature.getValue()), jwkSource) {
            @Override
            protected JWKMatcher createJWKMatcher(final JWSHeader jwsHeader) {

                if (!getExpectedJWSAlgorithm().equals(jwsHeader.getAlgorithm())) {
                    // Unexpected JWS alg
                    return null;
                } else if (JWSAlgorithm.Family.RSA.contains(getExpectedJWSAlgorithm())
                        || JWSAlgorithm.Family.EC.contains(getExpectedJWSAlgorithm())) {
                    // RSA or EC key matcher
                    return new JWKMatcher.Builder()
                            .keyType(KeyType.forAlgorithm(getExpectedJWSAlgorithm()))
                            .keyUses(KeyUse.SIGNATURE, null)
                            .algorithms(getExpectedJWSAlgorithm(), null)
                            .x509CertSHA256Thumbprint(jwsHeader.getX509CertSHA256Thumbprint())
                            .build();
                } else {
                    return null; // Unsupported algorithm
                }
            }
        };
    }
}

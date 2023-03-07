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
package io.gravitee.am.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jose.jca.JCASupport;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.gravitee.am.common.exception.jwt.MalformedJWTException;
import io.gravitee.am.common.exception.jwt.SignatureException;
import io.gravitee.am.common.jwt.Claims;
import io.gravitee.am.common.jwt.JWT;

import net.minidev.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.interfaces.ECPrivateKey;
import java.text.ParseException;

import javax.crypto.SecretKey;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DefaultJWTBuilder implements JWTBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultJWTBuilder.class);
    private final JWSSigner signer;
    private final JWSHeader header;
    private String issuer;

    public DefaultJWTBuilder(Key key, String signatureAlgorithm, String keyId)
            throws InvalidKeyException {
        if (key instanceof PrivateKey) {
            if (key.getAlgorithm().equals("RSA")) {
                signer = new RSASSASigner((PrivateKey) key, true);
            } else {
                try {
                    signer = new ECDSASigner((ECPrivateKey) key);
                } catch (JOSEException e) {
                    throw new InvalidKeyException(e);
                }
            }
            // if JCA doesn't support at least the PS256 algorithm (jdk <= 8)
            // add BouncyCastle JCA provider
            if (!JCASupport.isSupported(JWSAlgorithm.PS256)) {
                signer.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
            }
        } else if (key instanceof SecretKey) {
            try {
                signer = new MACSigner((SecretKey) key);
            } catch (KeyLengthException e) {
                throw new InvalidKeyException(e);
            }
        } else {
            throw new InvalidKeyException("No matching JWT signer for key : " + key);
        }
        header =
                new JWSHeader.Builder(new JWSAlgorithm(signatureAlgorithm))
                        .keyID(keyId)
                        .type(JOSEObjectType.JWT)
                        .build();
    }

    public DefaultJWTBuilder(Key key, String signatureAlgorithm, String keyId, String issuer)
            throws InvalidKeyException {
        this(key, signatureAlgorithm, keyId);
        this.issuer = issuer;
    }

    @Override
    public String sign(JWT payload) {
        try {
            JSONObject jsonObject = new JSONObject(payload);
            if (issuer != null && !jsonObject.containsKey(Claims.iss)) {
                jsonObject.put(Claims.iss, issuer);
            }
            SignedJWT signedJWT = new SignedJWT(header, JWTClaimsSet.parse(jsonObject));
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (ParseException ex) {
            logger.debug("Signing JWT token: {} has failed", payload);
            throw new MalformedJWTException("Signing JWT token has failed", ex);
        } catch (JOSEException ex) {
            logger.debug("Signing JWT token: {} has failed", payload);
            throw new SignatureException("Signing JWT token has failed", ex);
        } catch (Exception ex) {
            logger.error("An error occurs while signing JWT token : {}", payload, ex);
            throw ex;
        }
    }
}

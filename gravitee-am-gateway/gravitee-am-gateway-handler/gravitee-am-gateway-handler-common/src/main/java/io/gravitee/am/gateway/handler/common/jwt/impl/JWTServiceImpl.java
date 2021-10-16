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
package io.gravitee.am.gateway.handler.common.jwt.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.errorprone.annotations.InlineMe;
import com.nimbusds.jose.JWSAlgorithm;
import io.gravitee.am.common.exception.oauth2.InvalidTokenException;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.gateway.certificate.CertificateProvider;
import io.gravitee.am.gateway.handler.common.certificate.CertificateManager;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class JWTServiceImpl implements JWTService {

    private static final Logger logger = LoggerFactory.getLogger(JWTServiceImpl.class);

    @Autowired
    private CertificateManager certificateManager;

    @Autowired
    private ObjectMapper objectMapper;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encode_migrated(jwt, certificateProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<String> encode(JWT jwt, CertificateProvider certificateProvider) {
 return RxJava2Adapter.monoToSingle(encode_migrated(jwt, certificateProvider));
}
@Override
    public Mono<String> encode_migrated(JWT jwt, CertificateProvider certificateProvider) {
        Objects.requireNonNull(certificateProvider, "Certificate provider is required to sign JWT");
        return sign_migrated(certificateProvider, jwt);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encode_migrated(jwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<String> encode(JWT jwt, Client client) {
 return RxJava2Adapter.monoToSingle(encode_migrated(jwt, client));
}
@Override
    public Mono<String> encode_migrated(JWT jwt, Client client) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(certificateManager.get_migrated(client.getCertificate()).defaultIfEmpty(certificateManager.defaultCertificateProvider()))
                .flatMapSingle(certificateProvider -> RxJava2Adapter.monoToSingle(encode_migrated(jwt, certificateProvider))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encodeUserinfo_migrated(jwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<String> encodeUserinfo(JWT jwt, Client client) {
 return RxJava2Adapter.monoToSingle(encodeUserinfo_migrated(jwt, client));
}
@Override
    public Mono<String> encodeUserinfo_migrated(JWT jwt, Client client) {
        //Userinfo may not be signed but only encrypted
        if(client.getUserinfoSignedResponseAlg()==null) {
            return encode_migrated(jwt, certificateManager.noneAlgorithmCertificateProvider());
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(certificateManager.findByAlgorithm_migrated(client.getUserinfoSignedResponseAlg()).switchIfEmpty(certificateManager.get_migrated(client.getCertificate())).defaultIfEmpty(certificateManager.defaultCertificateProvider()))
                .flatMapSingle(certificateProvider -> RxJava2Adapter.monoToSingle(encode_migrated(jwt, certificateProvider))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.encodeAuthorization_migrated(jwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<String> encodeAuthorization(JWT jwt, Client client) {
 return RxJava2Adapter.monoToSingle(encodeAuthorization_migrated(jwt, client));
}
@Override
    public Mono<String> encodeAuthorization_migrated(JWT jwt, Client client) {
        // Signing an authorization response is required
        // As per https://bitbucket.org/openid/fapi/src/master/Financial_API_JWT_Secured_Authorization_Response_Mode.md#markdown-header-5-client-metadata
        // If unspecified, the default algorithm to use for signing authorization responses is RS256. The algorithm none is not allowed.
        String signedResponseAlg = client.getAuthorizationSignedResponseAlg();

        // To ensure backward compatibility
        if (signedResponseAlg == null) {
            signedResponseAlg = JWSAlgorithm.RS256.getName();
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(certificateManager.findByAlgorithm_migrated(signedResponseAlg).switchIfEmpty(certificateManager.get_migrated(client.getCertificate())).defaultIfEmpty(certificateManager.defaultCertificateProvider()))
                .flatMapSingle(certificateProvider -> RxJava2Adapter.monoToSingle(encode_migrated(jwt, certificateProvider))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.decodeAndVerify_migrated(jwt, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<JWT> decodeAndVerify(String jwt, Client client) {
 return RxJava2Adapter.monoToSingle(decodeAndVerify_migrated(jwt, client));
}
@Override
    public Mono<JWT> decodeAndVerify_migrated(String jwt, Client client) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(certificateManager.get_migrated(client.getCertificate()).defaultIfEmpty(certificateManager.defaultCertificateProvider()))
                .flatMapSingle(certificateProvider -> RxJava2Adapter.monoToSingle(decodeAndVerify_migrated(jwt, certificateProvider))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.decodeAndVerify_migrated(jwt, certificateProvider))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<JWT> decodeAndVerify(String jwt, CertificateProvider certificateProvider) {
 return RxJava2Adapter.monoToSingle(decodeAndVerify_migrated(jwt, certificateProvider));
}
@Override
    public Mono<JWT> decodeAndVerify_migrated(String jwt, CertificateProvider certificateProvider) {
        return decode_migrated(certificateProvider, jwt).map(RxJavaReactorMigrationUtil.toJdkFunction(JWT::new));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.decode_migrated(jwt))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<JWT> decode(String jwt) {
 return RxJava2Adapter.monoToSingle(decode_migrated(jwt));
}
@Override
    public Mono<JWT> decode_migrated(String jwt) {
        return RxJava2Adapter.singleToMono(Single.create(emitter -> {
            try {
                String json = new String(Base64.getDecoder().decode(jwt.split("\\.")[1]), "UTF-8");
                emitter.onSuccess(objectMapper.readValue(json, JWT.class));
            } catch (Exception ex) {
                logger.debug("Failed to decode JWT", ex);
                emitter.onError(new InvalidTokenException("The access token is invalid", ex));
            }
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.sign_migrated(certificateProvider, jwt))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<String> sign(CertificateProvider certificateProvider, JWT jwt) {
 return RxJava2Adapter.monoToSingle(sign_migrated(certificateProvider, jwt));
}
private Mono<String> sign_migrated(CertificateProvider certificateProvider, JWT jwt) {
        return RxJava2Adapter.singleToMono(Single.create(emitter -> {
            try {
                String encodedToken = certificateProvider.getJwtBuilder().sign(jwt);
                emitter.onSuccess(encodedToken);
            } catch (Exception ex) {
                logger.error("Failed to sign JWT", ex);
                emitter.onError(new InvalidTokenException("The JWT token couldn't be signed", ex));
            }
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.decode_migrated(certificateProvider, payload))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<Map<String, Object>> decode(CertificateProvider certificateProvider, String payload) {
 return RxJava2Adapter.monoToSingle(decode_migrated(certificateProvider, payload));
}
private Mono<Map<String,Object>> decode_migrated(CertificateProvider certificateProvider, String payload) {
        return RxJava2Adapter.singleToMono(Single.create(emitter -> {
            try {
                Map<String, Object> decodedPayload = certificateProvider.getJwtParser().parse(payload);
                emitter.onSuccess(decodedPayload);
            } catch (Exception ex) {
                logger.error("Failed to decode JWT", ex);
                emitter.onError(new InvalidTokenException("The access token is invalid", ex));
            }
        }));
    }

}

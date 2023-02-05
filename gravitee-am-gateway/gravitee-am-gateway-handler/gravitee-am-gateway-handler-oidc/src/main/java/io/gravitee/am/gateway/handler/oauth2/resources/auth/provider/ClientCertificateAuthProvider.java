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
package io.gravitee.am.gateway.handler.oauth2.resources.auth.provider;

import io.gravitee.am.common.oidc.ClientAuthenticationMethod;
import io.gravitee.am.gateway.handler.oauth2.exception.InvalidClientException;
import io.gravitee.am.model.oidc.Client;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.GeneralName;

import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * Client Authentication method : tls_client_auth
 *
 * <p>The PKI (public key infrastructure) method of mutual-TLS OAuth client authentication adheres
 * to the way in which X.509 certificates are traditionally used for authentication. It relies on a
 * validated certificate chain [RFC5280] and a single subject distinguished name (DN) or a single
 * subject alternative name (SAN) to authenticate the client.
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ClientCertificateAuthProvider implements ClientAuthProvider {

    private final String certificateHeader;

    public ClientCertificateAuthProvider(String certificateHeader) {
        this.certificateHeader = certificateHeader;
    }

    @Override
    public boolean canHandle(Client client, RoutingContext context) {
        // client_id is a required parameter for tls_client_auth so we are sure to have a client
        // here
        return client != null
                && CertificateUtils.hasPeerCertificate(context, certificateHeader)
                && ClientAuthenticationMethod.TLS_CLIENT_AUTH.equals(
                        client.getTokenEndpointAuthMethod());
    }

    @Override
    public void handle(
            Client client, RoutingContext context, Handler<AsyncResult<Client>> handler) {
        // We ensure that the authentication is done over TLS thanks to the canHandle method which
        // checks for an SSL
        // session or certificate provided through the Headers
        try {
            final Optional<X509Certificate> peerCertificate =
                    CertificateUtils.extractPeerCertificate(context, certificateHeader);
            if (peerCertificate.isPresent()
                            && (client.getTlsClientAuthSubjectDn() != null
                                    && validateSubjectDn(client, peerCertificate.get()))
                    || (client.getTlsClientAuthSanDns() != null
                            && validateSAN(
                                    peerCertificate.get(),
                                    GeneralName.dNSName,
                                    client.getTlsClientAuthSanDns()))
                    || (client.getTlsClientAuthSanEmail() != null
                            && validateSAN(
                                    peerCertificate.get(),
                                    GeneralName.rfc822Name,
                                    client.getTlsClientAuthSanEmail()))
                    || (client.getTlsClientAuthSanIp() != null
                            && validateSAN(
                                    peerCertificate.get(),
                                    GeneralName.iPAddress,
                                    client.getTlsClientAuthSanIp()))
                    || (client.getTlsClientAuthSanUri() != null
                            && validateSAN(
                                    peerCertificate.get(),
                                    GeneralName.uniformResourceIdentifier,
                                    client.getTlsClientAuthSanUri()))) {
                handler.handle(Future.succeededFuture(client));
            } else {
                handler.handle(
                        Future.failedFuture(
                                new InvalidClientException(
                                        "Invalid client: missing TLS configuration")));
            }
        } catch (SSLPeerUnverifiedException | CertificateException ce) {
            handler.handle(
                    Future.failedFuture(
                            new InvalidClientException(
                                    "Invalid client: missing or unsupported certificate")));
        }
    }

    private boolean validateSubjectDn(Client client, X509Certificate certificate) {
        return certificate.getSubjectDN() != null
                && X500Name.getDefaultStyle()
                        .areEqual(
                                new X500Name(client.getTlsClientAuthSubjectDn()),
                                new X500Name(certificate.getSubjectDN().getName()));
    }

    private boolean validateSAN(
            X509Certificate certificate, int subjectAlternativeName, String expected)
            throws CertificateParsingException {
        if (certificate == null
                || certificate.getSubjectAlternativeNames() == null
                || certificate.getSubjectAlternativeNames().isEmpty()) {
            return false;
        }

        Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
        for (List<?> altName : subjectAlternativeNames) {
            if ((Integer) altName.get(0) == subjectAlternativeName) {
                Object data = altName.get(1);
                if (data instanceof String && expected.equals(data)) {
                    return true;
                }
            }
        }

        return false;
    }
}

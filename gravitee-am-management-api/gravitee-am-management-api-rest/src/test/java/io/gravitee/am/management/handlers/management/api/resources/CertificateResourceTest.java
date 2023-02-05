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
package io.gravitee.am.management.handlers.management.api.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.model.Certificate;
import io.gravitee.am.model.Domain;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Maybe;

import org.junit.Test;

import javax.ws.rs.core.Response;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CertificateResourceTest extends JerseySpringTest {

    @Test
    public void shouldGetCertificate() {
        String domainId = "domain-id";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        String certificateId = "certificate-id";
        Certificate mockCertificate = new Certificate();
        mockCertificate.setId(certificateId);
        mockCertificate.setName("certificate-name");
        mockCertificate.setDomain(domainId);

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Maybe.just(mockCertificate)).when(certificateService).findById(certificateId);

        Response response =
                target("domains")
                        .path(domainId)
                        .path("certificates")
                        .path(certificateId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        Certificate certificate = readEntity(response, Certificate.class);
        assertEquals(domainId, certificate.getDomain());
        assertEquals(certificateId, certificate.getId());
    }

    @Test
    public void shouldGetCertificate_notFound() {
        String domainId = "domain-id";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        String certificateId = "certificate-id";

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Maybe.empty()).when(certificateService).findById(certificateId);

        Response response =
                target("domains")
                        .path(domainId)
                        .path("certificates")
                        .path(certificateId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void shouldGetCertificate_domainNotFound() {
        String domainId = "domain-id";
        String certificateId = "certificate-id";

        doReturn(Maybe.empty()).when(domainService).findById(domainId);

        Response response =
                target("domains")
                        .path(domainId)
                        .path("certificates")
                        .path(certificateId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.NOT_FOUND_404, response.getStatus());
    }

    @Test
    public void shouldGetCertificate_wrongDomain() {
        String domainId = "domain-id";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        String certificateId = "certificate-id";
        Certificate mockCertificate = new Certificate();
        mockCertificate.setId(certificateId);
        mockCertificate.setName("certificate-name");
        mockCertificate.setDomain("wrong-domain");

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(domainId);
        doReturn(Maybe.just(mockCertificate)).when(certificateService).findById(certificateId);

        Response response =
                target("domains")
                        .path(domainId)
                        .path("certificates")
                        .path(certificateId)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.BAD_REQUEST_400, response.getStatus());
    }
}

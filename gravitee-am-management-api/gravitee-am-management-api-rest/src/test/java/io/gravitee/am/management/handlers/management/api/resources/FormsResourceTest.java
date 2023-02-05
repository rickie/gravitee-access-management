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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Form;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.Template;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.model.NewForm;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Maybe;
import io.reactivex.Single;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class FormsResourceTest extends JerseySpringTest {
    private final String DOMAIN_ID = "domain-1";
    private Domain mockDomain;

    @Before
    public void setUp() {
        mockDomain = new Domain();
        mockDomain.setId(DOMAIN_ID);
    }

    @Test
    public void shouldGetForm() {
        Form mockForm = new Form();
        mockForm.setId("form-1-id");
        mockForm.setTemplate(Template.LOGIN.template());
        mockForm.setReferenceType(ReferenceType.DOMAIN);
        mockForm.setReferenceId(DOMAIN_ID);

        Form defaultMockForm = new Form();
        defaultMockForm.setId("default-form-1-id");
        defaultMockForm.setTemplate(Template.LOGIN.template());
        defaultMockForm.setReferenceType(ReferenceType.DOMAIN);
        defaultMockForm.setReferenceId(DOMAIN_ID);

        doReturn(Maybe.just(mockForm))
                .when(formService)
                .findByDomainAndTemplate(DOMAIN_ID, Template.LOGIN.template());
        doReturn(Single.just(defaultMockForm))
                .when(formService)
                .getDefaultByDomainAndTemplate(DOMAIN_ID, Template.LOGIN.template());

        Response response =
                target("domains")
                        .path(DOMAIN_ID)
                        .path("forms")
                        .queryParam("template", Template.LOGIN)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        Form responseEntity = readEntity(response, Form.class);
        assertTrue(responseEntity.getId().equals("form-1-id"));
    }

    @Test
    public void shouldGetDefaultForm() {
        Form defaultMockForm = new Form();
        defaultMockForm.setId("default-form-1-id");
        defaultMockForm.setTemplate(Template.LOGIN.template());
        defaultMockForm.setReferenceType(ReferenceType.DOMAIN);
        defaultMockForm.setReferenceId(DOMAIN_ID);

        doReturn(Maybe.empty())
                .when(formService)
                .findByDomainAndTemplate(DOMAIN_ID, Template.LOGIN.template());
        doReturn(Single.just(defaultMockForm))
                .when(formService)
                .getDefaultByDomainAndTemplate(DOMAIN_ID, Template.LOGIN.template());

        Response response =
                target("domains")
                        .path(DOMAIN_ID)
                        .path("forms")
                        .queryParam("template", Template.LOGIN)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.OK_200, response.getStatus());

        Form responseEntity = readEntity(response, Form.class);
        assertTrue(responseEntity.getId().equals("default-form-1-id"));
    }

    @Test
    public void shouldGetForms_technicalManagementException() {
        doReturn(Maybe.error(new TechnicalManagementException("error occurs")))
                .when(formService)
                .findByDomainAndTemplate(DOMAIN_ID, Template.LOGIN.template());

        Response response =
                target("domains")
                        .path(DOMAIN_ID)
                        .path("forms")
                        .queryParam("template", Template.LOGIN)
                        .request()
                        .get();
        assertEquals(HttpStatusCode.INTERNAL_SERVER_ERROR_500, response.getStatus());
    }

    @Test
    @Ignore
    public void shouldCreate() {
        NewForm newForm = new NewForm();
        newForm.setTemplate(Template.LOGIN);
        newForm.setContent("content");

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(DOMAIN_ID);
        doReturn(Single.just(new Form()))
                .when(formService)
                .create(eq(DOMAIN_ID), any(), any(User.class));

        Response response =
                target("domains")
                        .path(DOMAIN_ID)
                        .path("forms")
                        .request()
                        .post(Entity.json(newForm));
        assertEquals(HttpStatusCode.CREATED_201, response.getStatus());
    }
}

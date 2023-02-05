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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

import io.gravitee.am.management.handlers.management.api.JerseySpringTest;
import io.gravitee.am.management.handlers.management.api.model.PreviewRequest;
import io.gravitee.am.management.handlers.management.api.model.PreviewResponse;
import io.gravitee.am.management.handlers.management.api.model.TemplateType;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.Template;
import io.gravitee.am.model.Theme;
import io.gravitee.common.http.HttpStatusCode;
import io.reactivex.Maybe;

import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PreviewResourceTest extends JerseySpringTest {

    @Test
    public void shouldRenderDomainTemplate() {
        String domainId = "domain-1";
        Domain mockDomain = new Domain();
        mockDomain.setId(domainId);

        PreviewRequest request = new PreviewRequest();
        request.setTemplate(Template.LOGIN.template());
        request.setContent("content");
        request.setType(TemplateType.FORM);
        request.setTheme(new Theme());

        doReturn(Maybe.just(mockDomain)).when(domainService).findById(any());
        PreviewResponse previewResponse = new PreviewResponse();
        previewResponse.setContent("OK");
        previewResponse.setTemplate(Template.LOGIN.template());
        doReturn(Maybe.just(previewResponse))
                .when(previewService)
                .previewDomainForm(any(), any(), any());

        Response response =
                target("domains")
                        .path(domainId)
                        .path("forms")
                        .path("preview")
                        .request()
                        .post(Entity.json(request));

        assertEquals(HttpStatusCode.OK_200, response.getStatus());
        PreviewResponse entity = response.readEntity(PreviewResponse.class);
        assertNotNull(entity);
        assertNotNull(entity.getContent());
        assertEquals("OK", entity.getContent());
    }
}

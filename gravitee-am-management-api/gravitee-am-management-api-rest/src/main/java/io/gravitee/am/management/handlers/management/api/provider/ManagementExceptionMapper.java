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
package io.gravitee.am.management.handlers.management.api.provider;

import static javax.ws.rs.core.Response.Status.Family.familyOf;

import io.gravitee.am.service.exception.AbstractManagementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
@Provider
public class ManagementExceptionMapper
        extends AbstractExceptionMapper<AbstractManagementException> {

    private static Logger LOGGER = LoggerFactory.getLogger(ManagementExceptionMapper.class);

    @Override
    public Response toResponse(AbstractManagementException mex) {

        if (familyOf(mex.getHttpStatusCode()) == Response.Status.Family.SERVER_ERROR) {
            LOGGER.error("Unexpected error occurred", mex);
        }

        return Response.status(mex.getHttpStatusCode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(convert(mex))
                .build();
    }
}

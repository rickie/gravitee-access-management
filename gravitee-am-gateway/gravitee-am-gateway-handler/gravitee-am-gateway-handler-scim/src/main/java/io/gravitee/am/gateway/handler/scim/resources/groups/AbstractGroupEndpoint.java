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
package io.gravitee.am.gateway.handler.scim.resources.groups;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.gravitee.am.gateway.handler.common.vertx.utils.UriBuilderRequest;
import io.gravitee.am.gateway.handler.scim.exception.InvalidSyntaxException;
import io.gravitee.am.gateway.handler.scim.exception.InvalidValueException;
import io.gravitee.am.gateway.handler.scim.service.GroupService;
import io.gravitee.am.gateway.handler.scim.service.UserService;
import io.vertx.reactivex.core.http.HttpServerRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AbstractGroupEndpoint {

    protected UserService userService;
    protected GroupService groupService;
    protected ObjectMapper objectMapper;

    public AbstractGroupEndpoint(
            GroupService groupService, ObjectMapper objectMapper, UserService userService) {
        this.groupService = groupService;
        this.objectMapper = objectMapper;
        this.userService = userService;
    }

    protected String location(HttpServerRequest request) {
        return UriBuilderRequest.resolveProxyRequest(request, request.path());
    }

    protected void checkSchemas(List<String> schemas, List<String> restrictedSchemas) {
        if (schemas == null || schemas.isEmpty()) {
            throw new InvalidValueException("Field [schemas] is required");
        }
        Set<String> schemaSet = new HashSet();
        // check duplicate and check if values are supported
        schemas.forEach(
                schema -> {
                    if (!schemaSet.add(schema)) {
                        throw new InvalidSyntaxException(
                                "Duplicate 'schemas' values are forbidden");
                    }
                    if (!restrictedSchemas.contains(schema)) {
                        throw new InvalidSyntaxException(
                                "The 'schemas' attribute MUST only contain values defined as 'schema' and schemaExtensions' for the resource's defined User type");
                    }
                });
    }
}

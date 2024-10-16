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
package io.gravitee.am.gateway.handler.context;

import io.gravitee.common.util.MultiValueMap;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.http.HttpHeaders;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EvaluableRequest {

    private final Request request;

    public EvaluableRequest(final Request request) {
        this.request = request;
    }

    public String getId() {
        return request.id();
    }

    public String getTransactionId() {
        return request.transactionId();
    }

    public HttpHeaders getHeaders() {
        return request.headers();
    }

    public MultiValueMap<String, String> getParams() {
        return request.parameters();
    }

    public String getScheme() {
        return request.scheme();
    }

    public String getMethod() {
        return request.method().name();
    }

    public String getLocalAddress() {
        return request.localAddress();
    }

    public String[] getPaths() {
        return request.path().split("/");
    }

    public String getPath() {
        return request.path();
    }

    public String getContextPath() {
        return request.contextPath();
    }

    public String[] getPathInfos() {
        return request.pathInfo().split("/");
    }

    public String getPathInfo() {
        return request.pathInfo();
    }
}

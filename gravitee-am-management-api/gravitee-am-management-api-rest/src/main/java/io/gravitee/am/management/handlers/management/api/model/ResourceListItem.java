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
package io.gravitee.am.management.handlers.management.api.model;

import java.util.List;
import java.util.Map;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ResourceListItem {

    private List<ResourceEntity> resources;
    private Map<String, Map<String, Object>> metadata;

    public ResourceListItem() {}

    public ResourceListItem(
            List<ResourceEntity> resources, Map<String, Map<String, Object>> metadata) {
        this.resources = resources;
        this.metadata = metadata;
    }

    public List<ResourceEntity> getResources() {
        return resources;
    }

    public void setResources(List<ResourceEntity> resources) {
        this.resources = resources;
    }

    public Map<String, Map<String, Object>> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Map<String, Object>> metadata) {
        this.metadata = metadata;
    }
}

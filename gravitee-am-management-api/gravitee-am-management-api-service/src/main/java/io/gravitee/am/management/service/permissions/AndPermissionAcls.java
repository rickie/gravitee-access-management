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
package io.gravitee.am.management.service.permissions;

import static java.util.Arrays.asList;

import io.gravitee.am.model.Acl;
import io.gravitee.am.model.Membership;
import io.gravitee.am.model.ReferenceType;
import io.gravitee.am.model.permissions.Permission;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
class AndPermissionAcls implements PermissionAcls {

    private List<PermissionAcls> permissionAcls;

    AndPermissionAcls(PermissionAcls... permissionAcls) {

        this.permissionAcls = asList(permissionAcls);
    }

    @Override
    public boolean match(Map<Membership, Map<Permission, Set<Acl>>> permissions) {

        return permissionAcls.stream().allMatch(p -> p.match(permissions));
    }

    @Override
    public Stream<Map.Entry<ReferenceType, String>> referenceStream() {

        return permissionAcls.stream().flatMap(PermissionAcls::referenceStream).distinct();
    }
}

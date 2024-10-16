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
package io.gravitee.am.service;

import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Organization;
import io.gravitee.am.service.model.NewOrganization;
import io.gravitee.am.service.model.PatchOrganization;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface OrganizationService {

    Single<Organization> findById(String organizationId);

    Maybe<Organization> createDefault();

    Single<Organization> createOrUpdate(
            String organizationId, NewOrganization newOrganization, User byUser);

    Single<Organization> update(
            String organizationId, PatchOrganization patchOrganization, User authenticatedUser);
}

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
package io.gravitee.am.repository.jdbc.management.api.spring.environment;

import io.gravitee.am.repository.jdbc.management.api.model.JdbcEnvironment;
import io.reactivex.Flowable;
import io.reactivex.Maybe;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringEnvironmentRepository
        extends RxJava2CrudRepository<JdbcEnvironment, String> {
    @Query("select * from environments e where e.id = :id and e.organization_id = :organizationId")
    Maybe<JdbcEnvironment> findByIdAndOrganization(String id, String organizationId);

    @Query("select * from environments e where e.organization_id = :organizationId")
    Flowable<JdbcEnvironment> findByOrganization(String organizationId);
}

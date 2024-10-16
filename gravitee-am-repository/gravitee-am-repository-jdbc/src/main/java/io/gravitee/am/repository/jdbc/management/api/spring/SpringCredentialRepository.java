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
package io.gravitee.am.repository.jdbc.management.api.spring;

import io.gravitee.am.repository.jdbc.management.api.model.JdbcCredential;
import io.reactivex.Flowable;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.RxJava2CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface SpringCredentialRepository extends RxJava2CrudRepository<JdbcCredential, String> {
    @Query(
            "Select * from webauthn_credentials c where c.reference_id = :refId and c.reference_type = :refType and user_id = :userId")
    Flowable<JdbcCredential> findByUserId(
            @Param("refType") String referenceType,
            @Param("refId") String referenceId,
            @Param("userId") String userId);

    @Query(
            "Select * from webauthn_credentials c where c.reference_id = :refId and c.reference_type = :refType and username = :username")
    Flowable<JdbcCredential> findByUsername(
            @Param("refType") String referenceType,
            @Param("refId") String referenceId,
            @Param("username") String username);

    @Query(
            "Select * from webauthn_credentials c where c.reference_id = :refId and c.reference_type = :refType and credential_id = :credId")
    Flowable<JdbcCredential> findByCredentialId(
            @Param("refType") String referenceType,
            @Param("refId") String referenceId,
            @Param("credId") String credentialId);
}

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
package io.gravitee.am.service.reporter.builder.management;

import io.gravitee.am.common.audit.EntityType;
import io.gravitee.am.common.audit.EventType;
import io.gravitee.am.model.Credential;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class CredentialAuditBuilder extends ManagementAuditBuilder<CredentialAuditBuilder> {

    public CredentialAuditBuilder() {
        super();
    }

    public CredentialAuditBuilder credential(Credential credential) {
        if (credential != null) {
            if (EventType.CREDENTIAL_CREATED.equals(getType())
                    || EventType.CREDENTIAL_UPDATED.equals(getType())) {
                setNewValue(credential);
            }
            referenceId(credential.getReferenceId());
            referenceType(credential.getReferenceType());
            setTarget(
                    credential.getId(),
                    EntityType.CREDENTIAL,
                    credential.getAaguid(),
                    credential.getCredentialId(),
                    credential.getReferenceType(),
                    credential.getReferenceId());
        }
        return this;
    }
}

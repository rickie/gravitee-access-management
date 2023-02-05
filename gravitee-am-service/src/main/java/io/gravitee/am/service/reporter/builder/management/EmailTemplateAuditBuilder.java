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
import io.gravitee.am.model.Email;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EmailTemplateAuditBuilder extends ManagementAuditBuilder<EmailTemplateAuditBuilder> {

    public EmailTemplateAuditBuilder() {
        super();
    }

    public EmailTemplateAuditBuilder email(Email email) {
        if (email != null) {
            if (EventType.EMAIL_TEMPLATE_CREATED.equals(getType())
                    || EventType.EMAIL_TEMPLATE_UPDATED.equals(getType())) {
                setNewValue(email);
            }

            referenceType(email.getReferenceType());
            referenceId(email.getReferenceId());

            setTarget(
                    email.getId(),
                    EntityType.EMAIL,
                    null,
                    email.getTemplate(),
                    email.getReferenceType(),
                    email.getReferenceId());
        }
        return this;
    }
}

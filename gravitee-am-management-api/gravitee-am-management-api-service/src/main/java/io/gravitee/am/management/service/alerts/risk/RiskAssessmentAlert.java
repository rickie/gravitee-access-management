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
package io.gravitee.am.management.service.alerts.risk;

import static io.gravitee.alert.api.condition.StringCondition.matches;
import static io.gravitee.am.common.event.AlertEventKeys.PROPERTY_RISK_ASSESSMENT;

import static java.util.stream.Collectors.joining;

import io.gravitee.alert.api.condition.Condition;
import io.gravitee.alert.api.trigger.Trigger;

import org.springframework.core.env.Environment;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
abstract class RiskAssessmentAlert extends Trigger {

    protected static final String PROPERTY_ALERTS = "alerts.";
    protected static final String DESCRIPTION_SUFFIX = ".description";
    protected static final String SEVERITY_SUFFIX = ".severity";
    protected static final String NAME_SUFFIX = ".name";

    private static final String ASSESSMENTS_SUFFIX = ".assessments";

    protected RiskAssessmentAlert(
            String id, String name, Severity severity, String source, boolean enabled) {
        super(id, name, severity, source, enabled);
    }

    protected Condition getCondition(
            Environment environment, String alertProperty, String defaultAssessment) {
        final String assessmentProperty = PROPERTY_RISK_ASSESSMENT + "." + alertProperty;
        final String envProperty = PROPERTY_ALERTS + assessmentProperty + ASSESSMENTS_SUFFIX;
        final String assessments = environment.getProperty(envProperty, defaultAssessment);
        final String expression =
                Stream.of(assessments.split(", *")).map(Pattern::quote).collect(joining("|"));
        return matches(assessmentProperty, "^(" + expression + ")$").build();
    }
}

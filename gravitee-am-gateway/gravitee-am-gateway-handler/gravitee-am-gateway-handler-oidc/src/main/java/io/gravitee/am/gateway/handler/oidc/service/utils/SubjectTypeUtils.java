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
package io.gravitee.am.gateway.handler.oidc.service.utils;

import static io.gravitee.am.common.oidc.SubjectType.PUBLIC;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class SubjectTypeUtils {

    private static final Set<String> SUPPORTED_SUBJECT_TYPES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(PUBLIC)));

    /**
     * Return the supported list of subject types.
     *
     * @return
     */
    public static List<String> getSupportedSubjectTypes() {
        return Collections.unmodifiableList(
                SUPPORTED_SUBJECT_TYPES.stream().collect(Collectors.toList()));
    }

    /**
     * Throw InvalidClientMetadataException if null or contains unsupported subject type.
     *
     * @param subjectType String subject type to validate.
     */
    public static boolean isValidSubjectType(String subjectType) {
        return SUPPORTED_SUBJECT_TYPES.contains(subjectType);
    }
}

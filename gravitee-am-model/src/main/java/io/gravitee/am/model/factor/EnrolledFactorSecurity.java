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
package io.gravitee.am.model.factor;

import java.util.HashMap;
import java.util.Map;

/**
 * Enrolled factor for a specific user
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EnrolledFactorSecurity {

    private String type;

    private String value;

    private Map<String, Object> additionalData = new HashMap<>();

    public EnrolledFactorSecurity() {}

    public EnrolledFactorSecurity(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public EnrolledFactorSecurity(String type, String value, Map<String, Object> additionalData) {
        this.type = type;
        this.value = value;
        this.additionalData = additionalData;
    }

    public EnrolledFactorSecurity(EnrolledFactorSecurity other) {
        this.type = other.type;
        this.value = other.value;
        this.additionalData = other.additionalData;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    public <T> T getData(String key, Class<T> dataType) {
        return dataType.cast(this.additionalData.get(key));
    }

    public void putData(String key, Object value) {
        this.additionalData.put(key, value);
    }

    public void removeData(String key) {
        this.additionalData.remove(key);
    }
}

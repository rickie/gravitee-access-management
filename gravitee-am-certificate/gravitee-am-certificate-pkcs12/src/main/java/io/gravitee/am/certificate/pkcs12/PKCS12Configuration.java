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
package io.gravitee.am.certificate.pkcs12;

import io.gravitee.am.certificate.api.CertificateConfiguration;

import java.util.Set;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class PKCS12Configuration implements CertificateConfiguration {

    private String content;
    private String storepass;
    private String alias;
    private Set<String> use;
    private String keypass;
    private String algorithm;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStorepass() {
        return storepass;
    }

    public void setStorepass(String storepass) {
        this.storepass = storepass;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getKeypass() {
        return keypass;
    }

    public void setKeypass(String keypass) {
        this.keypass = keypass;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Set<String> getUse() {
        return use;
    }

    public void setUse(Set<String> use) {
        this.use = use;
    }
}

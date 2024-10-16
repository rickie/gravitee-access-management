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
package io.gravitee.am.certificate.javakeystore;

import io.gravitee.am.certificate.api.Certificate;
import io.gravitee.am.certificate.api.CertificateConfiguration;
import io.gravitee.am.certificate.api.CertificateProvider;
import io.gravitee.am.certificate.javakeystore.provider.JavaKeyStoreProvider;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class JavaKeyStore implements Certificate {
    @Override
    public Class<? extends CertificateConfiguration> configuration() {
        return JavaKeyStoreConfiguration.class;
    }

    @Override
    public Class<? extends CertificateProvider> certificateProvider() {
        return JavaKeyStoreProvider.class;
    }
}

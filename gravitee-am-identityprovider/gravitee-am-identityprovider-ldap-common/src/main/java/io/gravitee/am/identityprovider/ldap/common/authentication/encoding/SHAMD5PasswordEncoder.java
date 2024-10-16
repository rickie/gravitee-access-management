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
package io.gravitee.am.identityprovider.ldap.common.authentication.encoding;

import org.ldaptive.Credential;
import org.ldaptive.LdapException;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SHAMD5PasswordEncoder implements PasswordEncoder {

    private MD5PasswordEncoder md5PasswordEncoder = new MD5PasswordEncoder();
    private SHAPasswordEncoder shaPasswordEncoder = new SHAPasswordEncoder();
    private BinaryToTextEncoder binaryToTextEncoder;

    @Override
    public byte[] digestCredential(Credential credential) throws LdapException {
        String md5EncodedValue =
                binaryToTextEncoder.encode(md5PasswordEncoder.digestCredential(credential));
        return shaPasswordEncoder.digestCredential(new Credential(md5EncodedValue));
    }

    @Override
    public String getPasswordSchemeLabel() {
        // custom password encoder, no password label
        return null;
    }

    public void setBinaryToTextEncoder(BinaryToTextEncoder binaryToTextEncoder) {
        this.binaryToTextEncoder = binaryToTextEncoder;
    }

    public void setStrength(int strength) {
        shaPasswordEncoder.setStrength(strength);
    }
}

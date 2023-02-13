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
package io.gravitee.am.model.login;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public class LoginSettingsTest {

    @Test
    public void mustInstantiateLoginSettingsWithoutHideFormWithoutSecondSteps() {
        var loginSettings = new LoginSettings();
        loginSettings.setHideForm(false);
        loginSettings.setIdentifierFirstEnabled(false);

        assertResult(loginSettings, false, false);
    }

    @Test
    public void mustInstantiateLoginSettingsWithHideFormWithoutSecondSteps() {
        var loginSettings = new LoginSettings();
        loginSettings.setHideForm(true);
        loginSettings.setIdentifierFirstEnabled(false);

        assertResult(loginSettings, true, false);
    }

    @Test
    public void mustInstantiateLoginSettingsWithoutHideFormWithSecondSteps() {
        var loginSettings = new LoginSettings();
        loginSettings.setHideForm(false);
        loginSettings.setIdentifierFirstEnabled(true);

        assertResult(loginSettings, false, true);
    }

    @Test
    public void mustInstantiateLoginSettingsWithHideFormWithSecondSteps() {
        var loginSettings = new LoginSettings();
        loginSettings.setHideForm(true);
        loginSettings.setIdentifierFirstEnabled(true);
        // We cannot have both set to true
        assertResult(loginSettings, false, true);
    }

    private void assertResult(
            LoginSettings loginSettings, boolean isHideForm, boolean isIdentifierFirstEnabled) {
        var expectedSetting = new LoginSettings(loginSettings);
        assertEquals(expectedSetting.isHideForm(), isHideForm);
        assertEquals(expectedSetting.isIdentifierFirstEnabled(), isIdentifierFirstEnabled);
    }
}

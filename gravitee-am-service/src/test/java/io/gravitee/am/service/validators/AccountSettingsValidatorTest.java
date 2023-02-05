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
package io.gravitee.am.service.validators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import io.gravitee.am.model.account.AccountSettings;
import io.gravitee.am.model.account.FormField;
import io.gravitee.am.service.validators.accountsettings.AccountSettingsValidator;
import io.gravitee.am.service.validators.accountsettings.AccountSettingsValidatorImpl;

import org.junit.Test;

import java.util.List;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AccountSettingsValidatorTest {

    private final AccountSettingsValidator accountSettingsValidator =
            new AccountSettingsValidatorImpl();

    @Test
    public void validSettings_null() {
        assertTrue(accountSettingsValidator.validate(null));
    }

    @Test
    public void validSettings_notResetPassword() {
        AccountSettings element = new AccountSettings();
        element.setResetPasswordCustomForm(false);
        assertTrue(accountSettingsValidator.validate(element));
    }

    @Test
    public void validSettings_hasNoneOfTheFields() {
        AccountSettings element = new AccountSettings();
        element.setResetPasswordCustomForm(true);
        assertTrue(accountSettingsValidator.validate(element));
    }

    @Test
    public void validSettings_hasNoneOfTheFields_2() {
        AccountSettings element = new AccountSettings();
        element.setResetPasswordCustomForm(true);
        element.setResetPasswordCustomFormFields(List.of());
        assertTrue(accountSettingsValidator.validate(element));
    }

    @Test
    public void validSettings_containsEmail() {
        AccountSettings element = new AccountSettings();
        element.setResetPasswordCustomForm(true);
        FormField email = new FormField();
        email.setKey("email");
        element.setResetPasswordCustomFormFields(List.of(email));
        assertTrue(accountSettingsValidator.validate(element));
    }

    @Test
    public void validSettings_containsUsername() {
        AccountSettings element = new AccountSettings();
        element.setResetPasswordCustomForm(true);
        FormField username = new FormField();
        username.setKey("username");
        element.setResetPasswordCustomFormFields(List.of(username));
        assertTrue(accountSettingsValidator.validate(element));
    }

    @Test
    public void validSettings_containsEmailAndUsername() {
        AccountSettings element = new AccountSettings();
        element.setResetPasswordCustomForm(true);
        FormField email = new FormField();
        email.setKey("email");
        FormField username = new FormField();
        username.setKey("username");
        element.setResetPasswordCustomFormFields(List.of(email, username));
        assertTrue(accountSettingsValidator.validate(element));
    }

    @Test
    public void invalidSettings_containsEmailAndPasswordAndSomethingElse() {
        AccountSettings element = new AccountSettings();
        element.setResetPasswordCustomForm(true);
        FormField email = new FormField();
        email.setKey("email");
        FormField username = new FormField();
        username.setKey("username");
        FormField wrongField = new FormField();
        wrongField.setKey("wrongField");
        element.setResetPasswordCustomFormFields(List.of(email, wrongField, username));
        assertFalse(accountSettingsValidator.validate(element));
    }

    @Test
    public void invalidSettings_wrongFields() {
        AccountSettings element = new AccountSettings();
        element.setResetPasswordCustomForm(true);
        FormField wrongField = new FormField();
        wrongField.setKey("wrongField");
        element.setResetPasswordCustomFormFields(List.of(wrongField, wrongField));
        assertFalse(accountSettingsValidator.validate(element));
    }
}

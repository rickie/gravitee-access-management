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
package io.gravitee.am.factor.sms.provider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import io.gravitee.am.factor.sms.SMSFactorConfiguration;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.model.factor.EnrolledFactorChannel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class SMSFactorProviderTest {

    @InjectMocks private SMSFactorProvider provider;

    @Mock private SMSFactorConfiguration configuration;

    @Test
    public void shouldValidatePhoneNumber() {
        when(configuration.countries()).thenReturn(Arrays.asList("fr"));
        EnrolledFactor factor = new EnrolledFactor();
        factor.setChannel(
                new EnrolledFactorChannel(EnrolledFactorChannel.Type.SMS, "+33615492508"));
        assertTrue(provider.checkSecurityFactor(factor));
    }

    @Test
    public void shouldNotBeValidePhoneNumber_WrongCountry() {
        when(configuration.countries()).thenReturn(Arrays.asList("US", "GB"));
        EnrolledFactor factor = new EnrolledFactor();
        factor.setChannel(
                new EnrolledFactorChannel(EnrolledFactorChannel.Type.SMS, "+33615492508"));
        assertFalse(provider.checkSecurityFactor(factor));
    }

    @Test
    public void shouldValidatePhoneNumber_MultipleCountries() {
        when(configuration.countries()).thenReturn(Arrays.asList("US", "FR", "GB"));
        EnrolledFactor factor = new EnrolledFactor();
        factor.setChannel(
                new EnrolledFactorChannel(EnrolledFactorChannel.Type.SMS, "+33615492508"));
        assertTrue(provider.checkSecurityFactor(factor));
    }
}

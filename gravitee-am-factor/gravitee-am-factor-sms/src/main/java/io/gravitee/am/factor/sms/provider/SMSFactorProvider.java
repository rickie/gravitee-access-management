/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.factor.sms.provider;

import com.google.errorprone.annotations.InlineMe;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import io.gravitee.am.factor.api.Enrollment;
import io.gravitee.am.factor.api.FactorContext;
import io.gravitee.am.factor.api.FactorProvider;
import io.gravitee.am.factor.sms.SMSFactorConfiguration;
import io.gravitee.am.gateway.handler.manager.resource.ResourceManager;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.model.factor.EnrolledFactorChannel;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.resource.api.ResourceProvider;
import io.gravitee.am.resource.api.mfa.MFAChallenge;
import io.gravitee.am.resource.api.mfa.MFALink;
import io.gravitee.am.resource.api.mfa.MFAResourceProvider;
import io.gravitee.am.resource.api.mfa.MFAType;
import io.reactivex.Completable;
import io.reactivex.Single;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class SMSFactorProvider implements FactorProvider {

    private static final Logger logger = LoggerFactory.getLogger(SMSFactorProvider.class);

    @Autowired
    private SMSFactorConfiguration configuration;

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.verify_migrated(context))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable verify(FactorContext context) {
 return RxJava2Adapter.monoToCompletable(verify_migrated(context));
}
@Override
    public Mono<Void> verify_migrated(FactorContext context) {
        final String code = context.getData(FactorContext.KEY_CODE, String.class);
        final EnrolledFactor enrolledFactor = context.getData(FactorContext.KEY_ENROLLED_FACTOR, EnrolledFactor.class);
        ResourceManager component = context.getComponent(ResourceManager.class);
        ResourceProvider provider = component.getResourceProvider(configuration.getGraviteeResource());
        if (provider instanceof MFAResourceProvider) {
            MFAResourceProvider mfaProvider = (MFAResourceProvider)provider;
            MFAChallenge challenge = new MFAChallenge(enrolledFactor.getChannel().getTarget(), code);
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(mfaProvider.verify_migrated(challenge)));
        } else {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalException("Resource referenced can't be used for MultiFactor Authentication  with type SMS"))));
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enroll_migrated(account))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Enrollment> enroll(String account) {
 return RxJava2Adapter.monoToSingle(enroll_migrated(account));
}
@Override
    public Mono<Enrollment> enroll_migrated(String account) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(new Enrollment(this.configuration.countries()))));
    }

    @Override
    public boolean needChallengeSending() {
        return true;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.sendChallenge_migrated(context))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable sendChallenge(FactorContext context) {
 return RxJava2Adapter.monoToCompletable(sendChallenge_migrated(context));
}
@Override
    public Mono<Void> sendChallenge_migrated(FactorContext context) {
        final EnrolledFactor enrolledFactor = context.getData(FactorContext.KEY_ENROLLED_FACTOR, EnrolledFactor.class);
        ResourceManager component = context.getComponent(ResourceManager.class);
        ResourceProvider provider = component.getResourceProvider(configuration.getGraviteeResource());
        if (provider instanceof MFAResourceProvider) {
            MFAResourceProvider mfaProvider = (MFAResourceProvider)provider;
            MFALink link = new MFALink(MFAType.SMS, enrolledFactor.getChannel().getTarget());
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(mfaProvider.send_migrated(link)));
        } else {
            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(Mono.error(new TechnicalException("Resource referenced can't be used for MultiFactor Authentication  with type SMS"))));
        }
    }

    @Override
    public boolean checkSecurityFactor(EnrolledFactor factor) {
        boolean valid = false;
        if (factor != null) {
            EnrolledFactorChannel enrolledFactorChannel = factor.getChannel();
            if (enrolledFactorChannel == null || enrolledFactorChannel.getTarget() == null) {
                logger.warn("No phone number in form");
            } else {
                // check phone format according to Factor configuration
                final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
                try {
                    Phonenumber.PhoneNumber phone = phoneNumberUtil.parse(enrolledFactorChannel.getTarget(), Phonenumber.PhoneNumber.CountryCodeSource.UNSPECIFIED.name());
                    for (String country : configuration.countries()) {
                        if (phoneNumberUtil.isValidNumberForRegion(phone, country.toUpperCase(Locale.ROOT))) {
                            valid = true;
                            break;
                        }
                    }

                    if (!valid) {
                        logger.warn("Invalid phone number");
                    }
                } catch (NumberParseException e) {
                    logger.warn("Invalid phone number", e);
                }
            }
        }
        return valid;
    }

}

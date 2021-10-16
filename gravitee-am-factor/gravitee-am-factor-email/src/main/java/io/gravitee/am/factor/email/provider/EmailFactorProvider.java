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
package io.gravitee.am.factor.email.provider;

import static java.util.Arrays.asList;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.exception.mfa.InvalidCodeException;
import io.gravitee.am.common.factor.FactorDataKeys;
import io.gravitee.am.factor.api.Enrollment;
import io.gravitee.am.factor.api.FactorContext;
import io.gravitee.am.factor.api.FactorProvider;
import io.gravitee.am.factor.email.EmailFactorConfiguration;
import io.gravitee.am.factor.email.utils.HOTP;
import io.gravitee.am.factor.utils.SharedSecret;
import io.gravitee.am.gateway.handler.common.email.EmailService;
import io.gravitee.am.gateway.handler.manager.resource.ResourceManager;
import io.gravitee.am.gateway.handler.root.service.user.UserService;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.model.Template;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.model.factor.EnrolledFactorChannel;
import io.gravitee.am.model.factor.EnrolledFactorSecurity;
import io.gravitee.am.model.factor.FactorStatus;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.resource.api.ResourceProvider;
import io.gravitee.am.resource.api.email.EmailSenderProvider;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EmailFactorProvider implements FactorProvider {

    private static final Logger logger = LoggerFactory.getLogger(EmailFactorProvider.class);
    public static final String TEMPLATE_SUFFIX = ".html";

    @Autowired
    private EmailFactorConfiguration configuration;

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

        return RxJava2Adapter.completableToMono(Completable.create(emitter -> {
            try {
                final String otpCode = generateOTP(enrolledFactor);
                if (!code.equals(otpCode)) {
                    emitter.onError(new InvalidCodeException("Invalid 2FA Code"));
                }
                // get last connection date of the user to test code
                if (Instant.now().isAfter(Instant.ofEpochMilli(enrolledFactor.getSecurity().getData(FactorDataKeys.KEY_EXPIRE_AT, Long.class)))) {
                    emitter.onError(new InvalidCodeException("Invalid 2FA Code"));
                }
                emitter.onComplete();
            } catch (Exception ex) {
                logger.error("An error occurs while validating 2FA code", ex);
                emitter.onError(new InvalidCodeException("Invalid 2FA Code"));
            }
        }));

    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enroll_migrated(account))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Enrollment> enroll(String account) {
 return RxJava2Adapter.monoToSingle(enroll_migrated(account));
}
@Override
    public Mono<Enrollment> enroll_migrated(String account) {
        return Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(() -> new Enrollment(SharedSecret.generate())));
    }

    @Override
    public boolean checkSecurityFactor(EnrolledFactor  factor) {
        boolean valid = false;
        if (factor != null) {
            EnrolledFactorSecurity securityFactor = factor.getSecurity();
            if (securityFactor == null || securityFactor.getValue() == null) {
                logger.warn("No shared secret in form");
            } else {
                EnrolledFactorChannel enrolledFactorChannel = factor.getChannel();
                if (enrolledFactorChannel == null || enrolledFactorChannel.getTarget() == null) {
                    logger.warn("No email address in form");
                } else {
                    try {
                        InternetAddress internetAddress = new InternetAddress(enrolledFactorChannel.getTarget());
                        internetAddress.validate();
                        valid = true;
                    } catch (AddressException e) {
                        logger.warn("Email address is invalid", e);
                    }
                }
            }
        }
        return valid;
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

        if (provider instanceof EmailSenderProvider) {

            return generateCodeAndSendEmail_migrated(context, (EmailSenderProvider) provider, enrolledFactor);

        } else {

            return Mono.error(new TechnicalException("Resource referenced can't be used for MultiFactor Authentication with type EMAIL"));
        }
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.generateCodeAndSendEmail_migrated(context, provider, enrolledFactor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Completable generateCodeAndSendEmail(FactorContext context, EmailSenderProvider provider, EnrolledFactor enrolledFactor) {
 return RxJava2Adapter.monoToCompletable(generateCodeAndSendEmail_migrated(context, provider, enrolledFactor));
}
private Mono<Void> generateCodeAndSendEmail_migrated(FactorContext context, EmailSenderProvider provider, EnrolledFactor enrolledFactor) {
        logger.debug("Generating factor code of {} digits", configuration.getReturnDigits());

        try {
            UserService userService = context.getComponent(UserService.class);
            EmailService emailService = context.getComponent(EmailService.class);

            // register mfa code to make it available into the TemplateEngine values
            Map<String, Object> params =  context.getTemplateValues();
            params.put(FactorContext.KEY_CODE, generateOTP(enrolledFactor));

            final String recipient = enrolledFactor.getChannel().getTarget();
            EmailService.EmailWrapper emailWrapper = emailService.createEmail(Template.MFA_CHALLENGE, context.getClient(), asList(recipient), params);

            return RxJava2Adapter.completableToMono(RxJava2Adapter.monoToCompletable(provider.sendMessage_migrated(emailWrapper.getEmail()))).then(Mono.just(enrolledFactor).flatMap(v->RxJava2Adapter.singleToMono(Single.wrap(RxJavaReactorMigrationUtil.<EnrolledFactor, SingleSource<io.gravitee.am.model.User>>toJdkFunction(ef ->  {
                                ef.setPrimary(true);
                                ef.setStatus(FactorStatus.ACTIVATED);
                                ef.getSecurity().putData(FactorDataKeys.KEY_EXPIRE_AT, emailWrapper.getExpireAt());
                                return RxJava2Adapter.monoToSingle(userService.addFactor_migrated(context.getUser().getId(), ef, new DefaultUser(context.getUser())));
                            }).apply(v)))).then());

        } catch (NoSuchAlgorithmException| InvalidKeyException e) {
            logger.error("Code generation fails", e);
            return Mono.error(new TechnicalException("Code can't be sent"));
        } catch (Exception e) {
            logger.error("Email templating fails", e);
            return Mono.error(new TechnicalException("Email can't be sent"));
        }
    }

    String generateOTP(EnrolledFactor enrolledFactor) throws NoSuchAlgorithmException, InvalidKeyException {
        return HOTP.generateOTP(SharedSecret.base32Str2Bytes(enrolledFactor.getSecurity().getValue()), enrolledFactor.getSecurity().getData(FactorDataKeys.KEY_MOVING_FACTOR, Integer.class), configuration.getReturnDigits(), false, 0);
    }

    @Override
    public boolean useVariableFactorSecurity() {
        return true;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.changeVariableFactorSecurity_migrated(factor))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<EnrolledFactor> changeVariableFactorSecurity(EnrolledFactor factor) {
 return RxJava2Adapter.monoToSingle(changeVariableFactorSecurity_migrated(factor));
}
@Override
    public Mono<EnrolledFactor> changeVariableFactorSecurity_migrated(EnrolledFactor factor) {
        return Mono.fromSupplier(RxJavaReactorMigrationUtil.callableAsSupplier(() -> {
            int counter = factor.getSecurity().getData(FactorDataKeys.KEY_MOVING_FACTOR, Integer.class);
            factor.getSecurity().putData(FactorDataKeys.KEY_MOVING_FACTOR, counter + 1);
            factor.getSecurity().removeData(FactorDataKeys.KEY_EXPIRE_AT);
            return factor;
        }));
    }
}

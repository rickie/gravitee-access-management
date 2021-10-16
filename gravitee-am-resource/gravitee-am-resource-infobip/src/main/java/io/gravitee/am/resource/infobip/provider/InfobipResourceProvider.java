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
package io.gravitee.am.resource.infobip.provider;

import com.google.errorprone.annotations.InlineMe;
import com.infobip.ApiClient;
import com.infobip.ApiException;
import com.infobip.api.TfaApi;
import com.infobip.model.TfaStartAuthenticationRequest;
import com.infobip.model.TfaStartAuthenticationResponse;
import com.infobip.model.TfaVerifyPinRequest;
import com.infobip.model.TfaVerifyPinResponse;
import io.gravitee.am.common.exception.mfa.InvalidCodeException;
import io.gravitee.am.common.exception.mfa.SendChallengeException;
import io.gravitee.am.resource.api.ResourceProvider;
import io.gravitee.am.resource.api.mfa.MFAChallenge;
import io.gravitee.am.resource.api.mfa.MFALink;
import io.gravitee.am.resource.api.mfa.MFAResourceProvider;
import io.gravitee.am.resource.infobip.InfobipResourceConfiguration;
import io.reactivex.Completable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Ruan Ferreira (ruan@incentive.me)
 * @author Incentive.me
 */
public class InfobipResourceProvider implements MFAResourceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfobipResourceProvider.class);

    TfaApi tfaApi;
    public static final String APPROVED = "approved";

    private String pinId;
    private String to;

    @Autowired
    private InfobipResourceConfiguration configuration;

    @Override
    public ResourceProvider start() throws Exception {
        String baseUrl = this.configuration.getBaseUrl();
        String apiKey = this.configuration.getApiKey();
        String apiKeyPrefix = this.configuration.getApiKeyPrefix();

        ApiClient apiClient = new ApiClient();
        apiClient.setApiKeyPrefix(apiKeyPrefix);
        apiClient.setApiKey(apiKey);
        apiClient.setBasePath(baseUrl);

        this.tfaApi = new TfaApi(apiClient);
        return this;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.send_migrated(target))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable send(MFALink target) {
 return RxJava2Adapter.monoToCompletable(send_migrated(target));
}
@Override
    public Mono<Void> send_migrated(MFALink target) {
        this.to = target.getTarget();

        String messageId = this.configuration.getMessageId();
        String applicationId = this.configuration.getApplicationId();

        return RxJava2Adapter.completableToMono(Completable.create((emitter) -> {
            try{
                TfaStartAuthenticationResponse sendCodeResponse = this.tfaApi.sendTfaPinCodeOverSms(true,
                        new TfaStartAuthenticationRequest()
                                .applicationId(applicationId)
                                .messageId(messageId)
                                .from("InfoSMS")
                                .to(this.to)
                );

                boolean isSuccessful = sendCodeResponse.getSmsStatus().equals("MESSAGE_SENT");

                if(!isSuccessful) {
                    emitter.onError(new SendChallengeException("Message not sent"));
                } else {
                    this.pinId = sendCodeResponse.getPinId();

                    LOGGER.debug("Infobip Verification code asked with ID '{}'", sendCodeResponse.getPinId());
                    emitter.onComplete();
                }
            } catch (ApiException e) {
                this.LOGGER.error("Challenge emission fails", e);
                emitter.onError(new SendChallengeException("Unable to send challenge"));
            }
        }));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.verify_migrated(challenge))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable verify(MFAChallenge challenge) {
 return RxJava2Adapter.monoToCompletable(verify_migrated(challenge));
}
@Override
    public Mono<Void> verify_migrated(MFAChallenge challenge) {
        return RxJava2Adapter.completableToMono(Completable.create((emitter) -> {
            String pin = challenge.getCode();
            try {
                TfaVerifyPinResponse verifyResponse = this.tfaApi.verifyTfaPhoneNumber(pinId,
                        new TfaVerifyPinRequest().pin(pin)
                );
                boolean verified = verifyResponse.getVerified();

                LOGGER.debug(
                        "Infobip Verification code with ID '{}' verified with status '{}'",
                        this.pinId,
                        verified);
                if (!verified) {
                    emitter.onError(new InvalidCodeException("Challenger not verified"));
                } else {
                    emitter.onComplete();
                }
            } catch (ApiException e) {
                LOGGER.error("Challenge verification fails", e);
                emitter.onError(new InvalidCodeException("Invalid 2FA Code"));
            }
        }));
    }
}

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
package io.gravitee.am.gateway.handler.oauth2.resources.handler.risk;

import static io.gravitee.am.common.utils.ConstantKeys.DEVICE_ID;
import static io.gravitee.am.gateway.handler.common.vertx.utils.RequestUtils.remoteAddress;
import static io.gravitee.risk.assessment.api.assessment.Assessment.NONE;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.gravitee.am.common.utils.ConstantKeys;
import io.gravitee.am.gateway.handler.common.vertx.web.auth.user.User;
import io.gravitee.am.model.Device;
import io.gravitee.am.model.MFASettings;
import io.gravitee.am.model.RememberDeviceSettings;
import io.gravitee.am.model.UserActivity;
import io.gravitee.am.model.UserActivity.Type;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.service.DeviceService;
import io.gravitee.am.service.UserActivityService;
import io.gravitee.risk.assessment.api.assessment.AssessmentMessage;
import io.gravitee.risk.assessment.api.assessment.AssessmentMessageResult;
import io.gravitee.risk.assessment.api.assessment.AssessmentResult;
import io.gravitee.risk.assessment.api.assessment.data.AssessmentData;
import io.gravitee.risk.assessment.api.assessment.settings.AssessmentSettings;
import io.gravitee.risk.assessment.api.assessment.settings.RiskAssessmentSettings;
import io.gravitee.risk.assessment.api.devices.Devices;
import io.gravitee.risk.assessment.api.geovelocity.GeoTimeCoordinate;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.RoutingContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * @author Rémi SULTAN (remi.sultan at graviteesource.com)
 * @author GraviteeSource Team
 */
public class RiskAssessmentHandler implements Handler<RoutingContext> {

    private static final Logger logger = LoggerFactory.getLogger(RiskAssessmentHandler.class);
    private static final String RISK_ASSESSMENT_SERVICE = "service:risk-assessment";
    private final DeviceService deviceService;
    private final ObjectMapper objectMapper;
    private final EventBus eventBus;
    private final UserActivityService userActivityService;

    public RiskAssessmentHandler(
            DeviceService deviceService,
            UserActivityService userActivityService,
            EventBus eventBus,
            ObjectMapper objectMapper) {
        this.deviceService = deviceService;
        this.eventBus = eventBus;
        this.userActivityService = userActivityService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        final Optional<Client> client =
                ofNullable(routingContext.get(ConstantKeys.CLIENT_CONTEXT_KEY));
        final Optional<io.gravitee.am.model.User> user =
                ofNullable(routingContext.user())
                        .map(u -> (User) u.getDelegate())
                        .map(User::getUser);
        var riskAssessment =
                client.map(Client::getRiskAssessment).orElse(new RiskAssessmentSettings());
        if (client.isPresent() && user.isPresent() && riskAssessment.isEnabled()) {
            computeRiskAssessment(routingContext, client.get(), user.get(), riskAssessment);
        } else {
            routingContext.next();
        }
    }

    private void computeRiskAssessment(
            RoutingContext context,
            Client client,
            io.gravitee.am.model.User user,
            RiskAssessmentSettings riskAssessment) {
        var assessmentMessage =
                Single.just(
                        new AssessmentMessage()
                                .setSettings(riskAssessment)
                                .setData(new AssessmentData()));
        final String deviceId = context.session().get(DEVICE_ID);
        assessmentMessage
                .flatMap(buildDeviceMessage(client, user.getId(), deviceId))
                .flatMap(buildIpReputationMessage(context.request()))
                .flatMap(buildGeoVelocityMessage(client.getDomain(), user.getId()))
                .doOnSuccess(message -> decorateWithRiskAssessment(context, message))
                .doOnError(
                        throwable -> {
                            logger.error(
                                    "An unexpected error has occurred while trying to apply risk assessment: ",
                                    throwable);
                            context.next();
                        })
                .subscribe();
    }

    private Function<AssessmentMessage, Single<AssessmentMessage>> buildDeviceMessage(
            Client client, String userId, String deviceId) {
        return assessmentMessage -> {
            var riskAssessment = assessmentMessage.getSettings();
            var rememberDevice =
                    ofNullable(client.getMfaSettings())
                            .map(MFASettings::getRememberDevice)
                            .orElse(new RememberDeviceSettings());
            var deviceAssessment =
                    ofNullable(riskAssessment.getDeviceAssessment())
                            .orElse(new AssessmentSettings());

            if (deviceAssessment.isEnabled() && rememberDevice.isActive()) {
                logger.debug("Decorating assessment with devices");
                return deviceService
                        .findByDomainAndUser(client.getDomain(), userId)
                        .map(Device::getDeviceId)
                        .toList()
                        .flatMap(
                                deviceIds -> {
                                    assessmentMessage
                                            .getData()
                                            .setDevices(
                                                    new Devices()
                                                            .setKnownDevices(deviceIds)
                                                            .setEvaluatedDevice(deviceId));
                                    return Single.just(assessmentMessage);
                                });
            }
            return Single.just(assessmentMessage);
        };
    }

    private Function<AssessmentMessage, Single<AssessmentMessage>> buildIpReputationMessage(
            HttpServerRequest request) {
        return assessmentMessage -> {
            var settings = assessmentMessage.getSettings();
            var ipReputationAssessment =
                    ofNullable(settings.getIpReputationAssessment())
                            .orElse(new AssessmentSettings());
            if (ipReputationAssessment.isEnabled()) {
                logger.debug("Decorating assessment with IP reputation");
                var ip = remoteAddress(request);
                assessmentMessage.getData().setCurrentIp(ip);
            }
            return Single.just(assessmentMessage);
        };
    }

    private Function<AssessmentMessage, Single<AssessmentMessage>> buildGeoVelocityMessage(
            String domainId, String userId) {
        return assessmentMessage -> {
            var settings = assessmentMessage.getSettings();
            var geoVelocityAssessment =
                    ofNullable(settings.getGeoVelocityAssessment())
                            .orElse(new AssessmentSettings());
            if (geoVelocityAssessment.isEnabled()) {
                return userActivityService
                        .findByDomainAndTypeAndUserAndLimit(domainId, Type.LOGIN, userId, 2)
                        .toList()
                        .flatMap(
                                activityList -> {
                                    assessmentMessage
                                            .getData()
                                            .setGeoTimeCoordinates(
                                                    computeGeoTimeCoordinates(activityList));
                                    return Single.just(assessmentMessage);
                                });
            }
            return Single.just(assessmentMessage);
        };
    }

    private List<GeoTimeCoordinate> computeGeoTimeCoordinates(List<UserActivity> activityList) {
        return activityList.stream()
                .filter(ua -> nonNull(ua.getLatitude()))
                .filter(ua -> nonNull(ua.getLongitude()))
                .map(
                        ua ->
                                new GeoTimeCoordinate(
                                        ua.getLatitude(),
                                        ua.getLongitude(),
                                        ua.getCreatedAt().toInstant().getEpochSecond()))
                .collect(toList());
    }

    private void decorateWithRiskAssessment(
            RoutingContext routingContext, AssessmentMessage message)
            throws JsonProcessingException {
        eventBus.<String>request(
                RISK_ASSESSMENT_SERVICE,
                objectMapper.writeValueAsString(message),
                response -> {
                    if (response.succeeded()) {
                        var messageResult = extractMessageResult(response);
                        routingContext
                                .session()
                                .put(ConstantKeys.RISK_ASSESSMENT_KEY, messageResult);
                    } else if (response.failed()) {
                        logger.warn(
                                "{} could not be called, reason: {}",
                                RISK_ASSESSMENT_SERVICE,
                                response.cause().getMessage());
                        logger.debug("", response.cause());
                    }
                    routingContext.next();
                });
    }

    private AssessmentMessageResult extractMessageResult(AsyncResult<Message<String>> response) {
        try {
            return objectMapper.readValue(response.result().body(), AssessmentMessageResult.class);
        } catch (JsonProcessingException e) {
            logger.error("An unexpected error has occurred: ", e);
            return new AssessmentMessageResult()
                    .setDevices(new AssessmentResult<Double>().setAssessment(NONE))
                    .setGeoVelocity(new AssessmentResult<Double>().setAssessment(NONE))
                    .setIpReputation(new AssessmentResult<Double>().setAssessment(NONE));
        }
    }
}

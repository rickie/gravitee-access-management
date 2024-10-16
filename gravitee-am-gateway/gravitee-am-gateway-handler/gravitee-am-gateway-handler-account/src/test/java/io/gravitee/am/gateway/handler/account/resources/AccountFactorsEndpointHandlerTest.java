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
package io.gravitee.am.gateway.handler.account.resources;

import static io.gravitee.am.common.factor.FactorSecurityType.RECOVERY_CODE;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.gravitee.am.common.exception.mfa.InvalidCodeException;
import io.gravitee.am.common.exception.mfa.SendChallengeException;
import io.gravitee.am.common.utils.ConstantKeys;
import io.gravitee.am.factor.api.FactorProvider;
import io.gravitee.am.gateway.handler.account.resources.util.AccountRoutes;
import io.gravitee.am.gateway.handler.account.services.AccountService;
import io.gravitee.am.gateway.handler.common.factor.FactorManager;
import io.gravitee.am.gateway.handler.common.vertx.RxWebTestBase;
import io.gravitee.am.gateway.handler.common.vertx.web.handler.ErrorHandler;
import io.gravitee.am.model.Factor;
import io.gravitee.am.model.User;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.model.factor.EnrolledFactorChannel;
import io.gravitee.am.model.factor.EnrolledFactorSecurity;
import io.gravitee.am.service.RateLimiterService;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Ashraful Hasan (ashraful.hasan at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountFactorsEndpointHandlerTest extends RxWebTestBase {

    private static final String REQUEST_PATH = "/account/api/";

    @Mock AccountService accountService;

    @Mock FactorManager factorManager;

    @Mock ApplicationContext applicationContext;

    @Mock RateLimiterService rateLimiterService;

    private AccountFactorsEndpointHandler accountFactorsEndpointHandler;
    private User user;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        accountFactorsEndpointHandler =
                new AccountFactorsEndpointHandler(
                        accountService, factorManager, applicationContext, rateLimiterService);
        user = new User();

        router.route()
                .handler(
                        ctx -> {
                            ctx.put(ConstantKeys.USER_CONTEXT_KEY, user);
                            ctx.next();
                        })
                .handler(BodyHandler.create())
                .failureHandler(new ErrorHandler());
    }

    @Test
    public void listEnrolledFactorsShouldNotReturnRecoveryCode() throws Exception {
        addFactors(user);

        router.route(REQUEST_PATH + "factors")
                .handler(accountFactorsEndpointHandler::listEnrolledFactors)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.GET,
                REQUEST_PATH + "factors",
                req -> req.headers().set("content-type", "application/json"),
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals("Should contains 1 factor", 1, h.toJsonArray().size());
                                String body = h.toString();
                                assertTrue("SMS should present", body.contains("SMS"));
                                assertFalse(
                                        "There should not be recovery code",
                                        body.contains("RECOVERY_CODE"));
                            });
                },
                200,
                "OK",
                null);
    }

    @Test
    public void listRecoveryCodesShouldReturnUserRecoveryCodes() throws Exception {
        addFactors(user);
        final String[] expectedRecoveryCodes = {"one", "two", "three"};

        router.route(REQUEST_PATH + "recovery_code")
                .handler(accountFactorsEndpointHandler::listRecoveryCodes)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.GET,
                REQUEST_PATH + "recovery_code",
                req -> req.headers().set("content-type", "application/json"),
                res -> {
                    res.bodyHandler(
                            h -> {
                                ObjectMapper objectMapper = new ObjectMapper();
                                try {
                                    final Object[] actualRecoveryCodes =
                                            objectMapper
                                                    .readValue(h.toString(), List.class)
                                                    .toArray();
                                    assertArrayEquals(expectedRecoveryCodes, actualRecoveryCodes);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            });
                },
                200,
                "OK",
                null);
    }

    @Test
    public void shouldReturnEmptyListInAbsenceOfFactor() throws Exception {
        final String expectedValue = "[ ]";

        router.route(REQUEST_PATH + "recovery_code")
                .handler(accountFactorsEndpointHandler::listRecoveryCodes)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.GET,
                REQUEST_PATH + "recovery_code",
                req -> req.headers().set("content-type", "application/json"),
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals("", expectedValue, h.toString());
                            });
                },
                200,
                "OK",
                null);
    }

    @Test
    public void shouldReturnEmptyListInAbsenceOfRecoveryCodeFactor() throws Exception {
        addSMSFactor(user);
        final String expectedValue = "[ ]";

        router.route(REQUEST_PATH + "recovery_code")
                .handler(accountFactorsEndpointHandler::listRecoveryCodes)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.GET,
                REQUEST_PATH + "recovery_code",
                req -> req.headers().set("content-type", "application/json"),
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals("", expectedValue, h.toString());
                            });
                },
                200,
                "OK",
                null);
    }

    @Test
    public void shouldNotVerifyFactor_invalidRequest() throws Exception {
        router.post(AccountRoutes.FACTORS_VERIFY.getRoute())
                .handler(accountFactorsEndpointHandler::verifyFactor)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/verify",
                null,
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"Unable to parse body message\",\n"
                                                + "  \"http_status\" : 400\n"
                                                + "}",
                                        h.toString());
                            });
                },
                400,
                "Bad Request",
                null);
    }

    @Test
    public void shouldNotVerifyFactor_missingCode() throws Exception {
        router.post(AccountRoutes.FACTORS_VERIFY.getRoute())
                .handler(accountFactorsEndpointHandler::verifyFactor)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/verify",
                req -> {
                    Buffer buffer = Buffer.buffer();
                    buffer.appendString("{\"invalidCodeKey\":\"123456\"}");
                    req.headers().set("content-length", String.valueOf(buffer.length()));
                    req.headers().set("content-type", "application/json");
                    req.write(buffer);
                },
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"Field [code] is required\",\n"
                                                + "  \"http_status\" : 400\n"
                                                + "}",
                                        h.toString());
                            });
                },
                400,
                "Bad Request",
                null);
    }

    @Test
    public void shouldNotVerifyFactor_unknownFactor() throws Exception {
        router.post(AccountRoutes.FACTORS_VERIFY.getRoute())
                .handler(accountFactorsEndpointHandler::verifyFactor)
                .handler(rc -> rc.response().end());

        when(accountService.getFactor("factor-id")).thenReturn(Maybe.empty());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/verify",
                req -> {
                    Buffer buffer = Buffer.buffer();
                    buffer.appendString("{\"code\":\"123456\"}");
                    req.headers().set("content-length", String.valueOf(buffer.length()));
                    req.headers().set("content-type", "application/json");
                    req.write(buffer);
                },
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"Factor [factor-id] can not be found.\",\n"
                                                + "  \"http_status\" : 404\n"
                                                + "}",
                                        h.toString());
                            });
                },
                404,
                "Not Found",
                null);
    }

    @Test
    public void shouldNotVerifyFactor_invalidFactor() throws Exception {
        router.post(AccountRoutes.FACTORS_VERIFY.getRoute())
                .handler(accountFactorsEndpointHandler::verifyFactor)
                .handler(rc -> rc.response().end());

        when(accountService.getFactor("factor-id")).thenReturn(Maybe.just(new Factor()));
        when(factorManager.get("factor-id")).thenReturn(null);

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/verify",
                req -> {
                    Buffer buffer = Buffer.buffer();
                    buffer.appendString("{\"code\":\"123456\"}");
                    req.headers().set("content-length", String.valueOf(buffer.length()));
                    req.headers().set("content-type", "application/json");
                    req.write(buffer);
                },
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"Factor [factor-id] can not be found.\",\n"
                                                + "  \"http_status\" : 404\n"
                                                + "}",
                                        h.toString());
                            });
                },
                404,
                "Not Found",
                null);
    }

    @Test
    public void shouldNotVerifyFactor_factorNotEnrolled() throws Exception {
        Factor factor = mock(Factor.class);
        FactorProvider factorProvider = mock(FactorProvider.class);
        when(accountService.getFactor("factor-id")).thenReturn(Maybe.just(factor));
        when(factorManager.get("factor-id")).thenReturn(factorProvider);

        router.post(AccountRoutes.FACTORS_VERIFY.getRoute())
                .handler(
                        rc -> {
                            User user = rc.get(ConstantKeys.USER_CONTEXT_KEY);
                            user.setFactors(Collections.emptyList());
                            rc.next();
                        })
                .handler(accountFactorsEndpointHandler::verifyFactor)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/verify",
                req -> {
                    Buffer buffer = Buffer.buffer();
                    buffer.appendString("{\"code\":\"123456\"}");
                    req.headers().set("content-length", String.valueOf(buffer.length()));
                    req.headers().set("content-type", "application/json");
                    req.write(buffer);
                },
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"Factor [factor-id] can not be found.\",\n"
                                                + "  \"http_status\" : 404\n"
                                                + "}",
                                        h.toString());
                            });
                },
                404,
                "Not Found",
                null);
    }

    @Test
    public void shouldNotVerifyFactor_invalidCode() throws Exception {
        Factor factor = mock(Factor.class);
        FactorProvider factorProvider = mock(FactorProvider.class);
        when(factorProvider.verify(any()))
                .thenReturn(Completable.error(new InvalidCodeException("invalid code")));
        when(accountService.getFactor("factor-id")).thenReturn(Maybe.just(factor));
        when(factorManager.get("factor-id")).thenReturn(factorProvider);

        router.post(AccountRoutes.FACTORS_VERIFY.getRoute())
                .handler(
                        rc -> {
                            User user = rc.get(ConstantKeys.USER_CONTEXT_KEY);
                            EnrolledFactor enrolledFactor = new EnrolledFactor();
                            enrolledFactor.setFactorId("factor-id");
                            user.setFactors(Collections.singletonList(enrolledFactor));
                            rc.next();
                        })
                .handler(accountFactorsEndpointHandler::verifyFactor)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/verify",
                req -> {
                    Buffer buffer = Buffer.buffer();
                    buffer.appendString("{\"code\":\"123456\"}");
                    req.headers().set("content-length", String.valueOf(buffer.length()));
                    req.headers().set("content-type", "application/json");
                    req.write(buffer);
                },
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"invalid_mfa : invalid code\",\n"
                                                + "  \"http_status\" : 403\n"
                                                + "}",
                                        h.toString());
                            });
                },
                403,
                "Forbidden",
                null);
    }

    @Test
    public void shouldVerifyFactor_nominalCase() throws Exception {
        Factor factor = mock(Factor.class);
        FactorProvider factorProvider = mock(FactorProvider.class);
        EnrolledFactor enrolledFactor = new EnrolledFactor();
        enrolledFactor.setFactorId("factor-id");
        when(factorProvider.verify(any())).thenReturn(Completable.complete());
        when(factorProvider.changeVariableFactorSecurity(any()))
                .thenReturn(Single.just(enrolledFactor));
        when(accountService.getFactor("factor-id")).thenReturn(Maybe.just(factor));
        when(factorManager.get("factor-id")).thenReturn(factorProvider);
        when(accountService.upsertFactor(any(), any(), any())).thenReturn(Single.just(new User()));

        router.post(AccountRoutes.FACTORS_VERIFY.getRoute())
                .handler(
                        rc -> {
                            User user = rc.get(ConstantKeys.USER_CONTEXT_KEY);
                            user.setFactors(Collections.singletonList(enrolledFactor));
                            rc.next();
                        })
                .handler(accountFactorsEndpointHandler::verifyFactor)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/verify",
                req -> {
                    Buffer buffer = Buffer.buffer();
                    buffer.appendString("{\"code\":\"123456\"}");
                    req.headers().set("content-length", String.valueOf(buffer.length()));
                    req.headers().set("content-type", "application/json");
                    req.write(buffer);
                },
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"factorId\" : \"factor-id\",\n"
                                                + "  \"appId\" : null,\n"
                                                + "  \"status\" : \"ACTIVATED\",\n"
                                                + "  \"security\" : null,\n"
                                                + "  \"channel\" : null,\n"
                                                + "  \"primary\" : null,\n"
                                                + "  \"createdAt\" : null,\n"
                                                + "  \"updatedAt\" : null\n"
                                                + "}",
                                        h.toString());
                            });
                },
                200,
                "OK",
                null);

        verify(factorProvider, times(1)).changeVariableFactorSecurity(any());
    }

    @Test
    public void shouldNotSendChallenge_unknownFactor() throws Exception {
        router.post(AccountRoutes.FACTORS_SEND_CHALLENGE.getRoute())
                .handler(accountFactorsEndpointHandler::sendChallenge)
                .handler(rc -> rc.response().end());

        when(accountService.getFactor("factor-id")).thenReturn(Maybe.empty());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/sendChallenge",
                null,
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"Factor [factor-id] can not be found.\",\n"
                                                + "  \"http_status\" : 404\n"
                                                + "}",
                                        h.toString());
                            });
                },
                404,
                "Not Found",
                null);
    }

    @Test
    public void shouldNotSendChallenge_invalidFactor() throws Exception {
        router.post(AccountRoutes.FACTORS_SEND_CHALLENGE.getRoute())
                .handler(accountFactorsEndpointHandler::sendChallenge)
                .handler(rc -> rc.response().end());

        when(accountService.getFactor("factor-id")).thenReturn(Maybe.just(new Factor()));
        when(factorManager.get("factor-id")).thenReturn(null);

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/sendChallenge",
                null,
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"Factor [factor-id] can not be found.\",\n"
                                                + "  \"http_status\" : 404\n"
                                                + "}",
                                        h.toString());
                            });
                },
                404,
                "Not Found",
                null);
    }

    @Test
    public void shouldNotSendChallenge_factorNotEnrolled() throws Exception {
        Factor factor = mock(Factor.class);
        FactorProvider factorProvider = mock(FactorProvider.class);
        when(factorProvider.needChallengeSending()).thenReturn(true);
        when(accountService.getFactor("factor-id")).thenReturn(Maybe.just(factor));
        when(factorManager.get("factor-id")).thenReturn(factorProvider);

        router.post(AccountRoutes.FACTORS_SEND_CHALLENGE.getRoute())
                .handler(
                        rc -> {
                            User user = rc.get(ConstantKeys.USER_CONTEXT_KEY);
                            user.setFactors(Collections.emptyList());
                            rc.next();
                        })
                .handler(accountFactorsEndpointHandler::sendChallenge)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/sendChallenge",
                null,
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"Factor [factor-id] can not be found.\",\n"
                                                + "  \"http_status\" : 404\n"
                                                + "}",
                                        h.toString());
                            });
                },
                404,
                "Not Found",
                null);
    }

    @Test
    public void shouldNotSendChallenge_sendChallengeException() throws Exception {
        Factor factor = mock(Factor.class);
        FactorProvider factorProvider = mock(FactorProvider.class);
        when(factorProvider.needChallengeSending()).thenReturn(true);
        when(factorProvider.sendChallenge(any()))
                .thenReturn(
                        Completable.error(
                                new SendChallengeException("unable to send the challenge")));
        when(accountService.getFactor("factor-id")).thenReturn(Maybe.just(factor));
        when(factorManager.get("factor-id")).thenReturn(factorProvider);

        router.post(AccountRoutes.FACTORS_SEND_CHALLENGE.getRoute())
                .handler(
                        rc -> {
                            User user = rc.get(ConstantKeys.USER_CONTEXT_KEY);
                            EnrolledFactor enrolledFactor = new EnrolledFactor();
                            enrolledFactor.setFactorId("factor-id");
                            user.setFactors(Collections.singletonList(enrolledFactor));
                            rc.next();
                        })
                .handler(accountFactorsEndpointHandler::sendChallenge)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/sendChallenge",
                null,
                null,
                500,
                "Internal Server Error",
                null);
    }

    @Test
    public void shouldNotSendChallenge_noNeedChallenge() throws Exception {
        Factor factor = mock(Factor.class);
        FactorProvider factorProvider = mock(FactorProvider.class);
        when(factorProvider.needChallengeSending()).thenReturn(false);
        when(accountService.getFactor("factor-id")).thenReturn(Maybe.just(factor));
        when(factorManager.get("factor-id")).thenReturn(factorProvider);

        router.post(AccountRoutes.FACTORS_SEND_CHALLENGE.getRoute())
                .handler(
                        rc -> {
                            User user = rc.get(ConstantKeys.USER_CONTEXT_KEY);
                            EnrolledFactor enrolledFactor = new EnrolledFactor();
                            enrolledFactor.setFactorId("factor-id");
                            user.setFactors(Collections.singletonList(enrolledFactor));
                            rc.next();
                        })
                .handler(accountFactorsEndpointHandler::sendChallenge)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/sendChallenge",
                null,
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"message\" : \"Invalid factor\",\n"
                                                + "  \"http_status\" : 400\n"
                                                + "}",
                                        h.toString());
                            });
                },
                400,
                "Bad Request",
                null);

        verify(factorProvider, never()).sendChallenge(any());
    }

    @Test
    public void shouldSendChallenge_nominalCase() throws Exception {
        Factor factor = mock(Factor.class);
        FactorProvider factorProvider = mock(FactorProvider.class);
        when(factorProvider.needChallengeSending()).thenReturn(true);
        when(factorProvider.sendChallenge(any())).thenReturn(Completable.complete());
        when(accountService.getFactor("factor-id")).thenReturn(Maybe.just(factor));
        when(factorManager.get("factor-id")).thenReturn(factorProvider);

        router.post(AccountRoutes.FACTORS_SEND_CHALLENGE.getRoute())
                .handler(
                        rc -> {
                            User user = rc.get(ConstantKeys.USER_CONTEXT_KEY);
                            EnrolledFactor enrolledFactor = new EnrolledFactor();
                            enrolledFactor.setFactorId("factor-id");
                            user.setFactors(Collections.singletonList(enrolledFactor));
                            rc.next();
                        })
                .handler(accountFactorsEndpointHandler::sendChallenge)
                .handler(rc -> rc.response().end());

        testRequest(
                HttpMethod.POST,
                "/api/factors/factor-id/sendChallenge",
                null,
                res -> {
                    res.bodyHandler(
                            h -> {
                                assertEquals(
                                        "{\n"
                                                + "  \"factorId\" : \"factor-id\",\n"
                                                + "  \"appId\" : null,\n"
                                                + "  \"status\" : \"NULL\",\n"
                                                + "  \"security\" : null,\n"
                                                + "  \"channel\" : null,\n"
                                                + "  \"primary\" : null,\n"
                                                + "  \"createdAt\" : null,\n"
                                                + "  \"updatedAt\" : null\n"
                                                + "}",
                                        h.toString());
                            });
                },
                200,
                "OK",
                null);
    }

    private void addFactors(User user) {
        final Map<String, Object> recoveryCode =
                Map.of(RECOVERY_CODE, Arrays.asList("one", "two", "three"));
        final EnrolledFactor securityEnrolledFactor = new EnrolledFactor();
        securityEnrolledFactor.setSecurity(
                new EnrolledFactorSecurity(RECOVERY_CODE, "3", recoveryCode));

        user.setFactors(Arrays.asList(securityEnrolledFactor, smsFactor()));
    }

    private void addSMSFactor(User user) {
        user.setFactors(List.of(smsFactor()));
    }

    private EnrolledFactor smsFactor() {
        final EnrolledFactor smsFactor = new EnrolledFactor();
        smsFactor.setChannel(new EnrolledFactorChannel(EnrolledFactorChannel.Type.SMS, "1234"));
        return smsFactor;
    }
}

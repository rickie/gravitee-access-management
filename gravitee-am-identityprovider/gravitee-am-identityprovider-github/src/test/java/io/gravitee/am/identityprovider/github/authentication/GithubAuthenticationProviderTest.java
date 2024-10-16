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
package io.gravitee.am.identityprovider.github.authentication;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.gravitee.am.common.exception.authentication.BadCredentialsException;
import io.gravitee.am.identityprovider.api.*;
import io.gravitee.am.identityprovider.common.oauth2.utils.URLEncodedUtils;
import io.gravitee.am.identityprovider.github.authentication.spring.GithubAuthenticationProviderConfiguration;
import io.gravitee.common.http.HttpHeaders;
import io.reactivex.observers.TestObserver;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = {
            GithubAuthenticationProviderTestConfiguration.class,
            GithubAuthenticationProviderConfiguration.class
        },
        loader = AnnotationConfigContextLoader.class)
public class GithubAuthenticationProviderTest {

    @Autowired private AuthenticationProvider authenticationProvider;

    @Autowired private DefaultIdentityProviderRoleMapper roleMapper;

    @Rule public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(19998));

    @Test
    public void shouldLoadUserByUsername_authentication() {
        stubFor(
                any(urlPathEqualTo("/oauth/token"))
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE, containing(URLEncodedUtils.CONTENT_TYPE))
                        .withRequestBody(matching(".*"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody("access_token=test_token&token_type=bearer")));

        stubFor(
                any(urlPathEqualTo("/profile"))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("token test_token"))
                        .willReturn(okJson("{ \"login\": \"bob\" }")));

        TestObserver<User> testObserver =
                authenticationProvider
                        .loadUserByUsername(
                                new Authentication() {
                                    @Override
                                    public Object getCredentials() {
                                        return "__social__";
                                    }

                                    @Override
                                    public Object getPrincipal() {
                                        return "__social__";
                                    }

                                    @Override
                                    public AuthenticationContext getContext() {
                                        DummyRequest dummyRequest = new DummyRequest();
                                        dummyRequest.setParameters(
                                                Collections.singletonMap(
                                                        "code", Arrays.asList("test-code")));
                                        return new DummyAuthenticationContext(
                                                Collections.singletonMap(
                                                        "redirect_uri", "http://redirect_uri"),
                                                dummyRequest);
                                    }
                                })
                        .test();

        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(u -> "bob".equals(u.getUsername()));
    }

    @Test
    public void shouldLoadUserByUsername_authentication_badCredentials() {
        stubFor(
                any(urlPathEqualTo("/oauth/token"))
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE, containing(URLEncodedUtils.CONTENT_TYPE))
                        .withRequestBody(matching(".*"))
                        .willReturn(unauthorized()));

        TestObserver<User> testObserver =
                authenticationProvider
                        .loadUserByUsername(
                                new Authentication() {
                                    @Override
                                    public Object getCredentials() {
                                        return "__social__";
                                    }

                                    @Override
                                    public Object getPrincipal() {
                                        return "__social__";
                                    }

                                    @Override
                                    public AuthenticationContext getContext() {
                                        DummyRequest dummyRequest = new DummyRequest();
                                        dummyRequest.setParameters(
                                                Collections.singletonMap(
                                                        "code", Arrays.asList("wrong-code")));
                                        return new DummyAuthenticationContext(
                                                Collections.singletonMap(
                                                        "redirect_uri", "http://redirect_uri"),
                                                dummyRequest);
                                    }
                                })
                        .test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(BadCredentialsException.class);
    }

    @Test
    public void shouldLoadUserByUsername_authentication_usernameNotFound() {
        stubFor(
                any(urlPathEqualTo("/oauth/token"))
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE, containing(URLEncodedUtils.CONTENT_TYPE))
                        .withRequestBody(matching(".*"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody("access_token=test_token&token_type=bearer")));

        stubFor(
                any(urlPathEqualTo("/profile"))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("token test_token"))
                        .willReturn(notFound()));

        TestObserver<User> testObserver =
                authenticationProvider
                        .loadUserByUsername(
                                new Authentication() {
                                    @Override
                                    public Object getCredentials() {
                                        return "__social__";
                                    }

                                    @Override
                                    public Object getPrincipal() {
                                        return "__social__";
                                    }

                                    @Override
                                    public AuthenticationContext getContext() {
                                        DummyRequest dummyRequest = new DummyRequest();
                                        dummyRequest.setParameters(
                                                Collections.singletonMap(
                                                        "code", Arrays.asList("test-code")));
                                        return new DummyAuthenticationContext(
                                                Collections.singletonMap(
                                                        "redirect_uri", "http://redirect_uri"),
                                                dummyRequest);
                                    }
                                })
                        .test();
        testObserver.awaitTerminalEvent();

        testObserver.assertError(BadCredentialsException.class);
    }

    @Test
    public void shouldLoadUserByUsername_roleMapping() {
        // configure role mapping
        Map<String, String[]> roles = new HashMap<>();
        roles.put("admin", new String[] {"preferred_username=bob"});
        roleMapper.setRoles(roles);

        stubFor(
                any(urlPathEqualTo("/oauth/token"))
                        .withHeader(
                                HttpHeaders.CONTENT_TYPE, containing(URLEncodedUtils.CONTENT_TYPE))
                        .withRequestBody(matching(".*"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withBody("access_token=test_token&token_type=bearer")));

        stubFor(
                any(urlPathEqualTo("/profile"))
                        .withHeader(HttpHeaders.AUTHORIZATION, equalTo("token test_token"))
                        .willReturn(
                                okJson("{ \"login\": \"bob\", \"preferred_username\": \"bob\"}")));

        TestObserver<User> testObserver =
                authenticationProvider
                        .loadUserByUsername(
                                new Authentication() {
                                    @Override
                                    public Object getCredentials() {
                                        return "__social__";
                                    }

                                    @Override
                                    public Object getPrincipal() {
                                        return "__social__";
                                    }

                                    @Override
                                    public AuthenticationContext getContext() {
                                        DummyRequest dummyRequest = new DummyRequest();
                                        dummyRequest.setParameters(
                                                Collections.singletonMap(
                                                        "code", Arrays.asList("test-code")));
                                        return new DummyAuthenticationContext(
                                                Collections.singletonMap(
                                                        "redirect_uri", "http://redirect_uri"),
                                                dummyRequest);
                                    }
                                })
                        .test();

        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(u -> "bob".equals(u.getUsername()));
        testObserver.assertValue(u -> u.getRoles().contains("admin"));
    }
}

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
package io.gravitee.am.management.handlers.management.api.authentication.filter;

import io.gravitee.am.common.jwt.Claims;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.jwt.DefaultJWTParser;
import io.gravitee.am.jwt.JWTParser;
import io.gravitee.am.management.handlers.management.api.authentication.provider.generator.JWTGenerator;
import io.gravitee.am.management.handlers.management.api.authentication.service.AuthenticationService;
import io.gravitee.am.model.Environment;
import io.gravitee.am.service.EnvironmentService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.HashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CockpitAuthenticationFilter extends GenericFilterBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(CockpitAuthenticationFilter.class);
    private static final String COCKPIT_SOURCE = "cockpit";

    @Value("${cockpit.enabled:false}")
    private boolean enabled;

    /** Cockpit keystore type for client certificate (mtls) and jwt signature verification. */
    @Value("${cockpit.keystore.type:#{null}}")
    private String keyStoreType;

    /** Cockpit keystore path for client mtls and jwt. */
    @Value("${cockpit.keystore.path:#{null}}")
    private String keyStorePath;

    /** Cockpit keystore password. */
    @Value("${cockpit.keystore.password:#{null}}")
    private String keyStorePassword;

    /** Cockpit key alias. */
    @Value("${cockpit.keystore.key.alias:cockpit-client}")
    private String keyAlias;

    @Autowired @Lazy private JWTGenerator jwtGenerator;

    @Autowired private AuthenticationService authenticationService;

    @Autowired private EnvironmentService environmentService;

    private JWTParser jwtParser;

    @Override
    public void afterPropertiesSet() {

        if (enabled) {
            try {
                this.jwtParser = new DefaultJWTParser(this.getPublicKey());
            } catch (Exception e) {
                throw new RuntimeException("Unable to load cockpit JWT public key");
            }
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (enabled && httpRequest.getPathInfo().equals("/cockpit")) {

            String token = request.getParameter("token");

            if (StringUtils.isEmpty(token)) {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                try {
                    JWT jwt = jwtParser.parse(token);
                    UsernamePasswordAuthenticationToken authentication =
                            convertToAuthentication(jwt);
                    User principal = authenticationService.onAuthenticationSuccess(authentication);

                    final Environment environment =
                            environmentService
                                    .findById(
                                            (String) jwt.get(Claims.environment),
                                            (String) jwt.get(Claims.organization))
                                    .blockingGet();
                    String redirectPath = "";

                    if (environment != null) {
                        redirectPath = "/environments/" + environment.getHrids().get(0);
                    }

                    Cookie jwtAuthenticationCookie = jwtGenerator.generateCookie(principal);
                    httpResponse.addCookie(jwtAuthenticationCookie);
                    httpResponse.sendRedirect((String) jwt.get("redirect_uri") + redirectPath);
                } catch (Exception e) {
                    LOGGER.error("Error occurred when trying to login using cockpit.", e);
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private UsernamePasswordAuthenticationToken convertToAuthentication(JWT jwt) {

        String username = (String) jwt.get(StandardClaims.PREFERRED_USERNAME);
        String organizationId = (String) jwt.get(Claims.organization);

        DefaultUser user = new DefaultUser(username);
        user.setId((String) jwt.get(StandardClaims.SUB));
        user.setAdditionalInformation(new HashMap<>());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
        HashMap<Object, Object> details = new HashMap<>();
        details.put("source", COCKPIT_SOURCE);
        details.put(StandardClaims.PREFERRED_USERNAME, username);
        details.put(Claims.organization, organizationId);
        authentication.setDetails(details);
        return authentication;
    }

    private Key getPublicKey() throws Exception {

        final KeyStore trustStore = loadKeyStore();
        final Certificate cert = trustStore.getCertificate(keyAlias);

        return cert.getPublicKey();
    }

    private KeyStore loadKeyStore() throws Exception {

        final KeyStore keystore = KeyStore.getInstance(keyStoreType);

        try (InputStream is = new File(keyStorePath).toURI().toURL().openStream()) {
            keystore.load(is, null == keyStorePassword ? null : keyStorePassword.toCharArray());
        }

        return keystore;
    }
}

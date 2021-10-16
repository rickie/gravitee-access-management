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
package io.gravitee.am.gateway.handler.oidc.resources.endpoint;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.exception.oauth2.InvalidRequestException;
import io.gravitee.am.common.exception.oauth2.InvalidTokenException;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.common.oidc.CustomClaims;
import io.gravitee.am.common.oidc.Scope;
import io.gravitee.am.common.oidc.StandardClaims;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.gateway.handler.common.utils.ConstantKeys;
import io.gravitee.am.gateway.handler.common.vertx.utils.UriBuilderRequest;
import io.gravitee.am.gateway.handler.oidc.service.discovery.OpenIDDiscoveryService;
import io.gravitee.am.gateway.handler.oidc.service.jwe.JWEService;
import io.gravitee.am.gateway.handler.oidc.service.request.ClaimsRequest;
import io.gravitee.am.model.Role;
import io.gravitee.am.model.User;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.service.GroupService;
import io.gravitee.am.service.RoleService;
import io.gravitee.am.service.UserService;
import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.MediaType;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.reactivex.ext.web.RoutingContext;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * The Client sends the UserInfo Request using either HTTP GET or HTTP POST.
 * The Access Token obtained from an OpenID Connect Authentication Request MUST be sent as a Bearer Token, per Section 2 of OAuth 2.0 Bearer Token Usage [RFC6750].
 * It is RECOMMENDED that the request use the HTTP GET method and the Access Token be sent using the Authorization header field.
 *
 * See <a href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfo">5.3.1. UserInfo Request</a>
 *
 * The UserInfo Endpoint is an OAuth 2.0 Protected Resource that returns Claims about the authenticated End-User.
 * To obtain the requested Claims about the End-User, the Client makes a request to the UserInfo Endpoint using an Access Token obtained through OpenID Connect Authentication.
 * These Claims are normally represented by a JSON object that contains a collection of name and value pairs for the Claims.
 *
 * See <a href="http://openid.net/specs/openid-connect-core-1_0.html#UserInfo">5.3. UserInfo Endpoint</a>
 *
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UserInfoEndpoint implements Handler<RoutingContext> {

    private UserService userService;
    private JWTService jwtService;
    private JWEService jweService;
    private OpenIDDiscoveryService openIDDiscoveryService;

    public UserInfoEndpoint(UserService userService,
                            JWTService jwtService,
                            JWEService jweService,
                            OpenIDDiscoveryService openIDDiscoveryService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.jweService = jweService;
        this.openIDDiscoveryService = openIDDiscoveryService;
    }

    @Override
    public void handle(RoutingContext context) {
        JWT accessToken = context.get(ConstantKeys.TOKEN_CONTEXT_KEY);
        Client client = context.get(ConstantKeys.CLIENT_CONTEXT_KEY);
        String subject = accessToken.getSub();
        RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(userService.findById_migrated(subject))).switchIfEmpty(Mono.error(new InvalidTokenException("No user found for this token"))))
                // enhance user information
                .flatMapSingle(user -> RxJava2Adapter.monoToSingle(enhance_migrated(user, accessToken)))).map(RxJavaReactorMigrationUtil.toJdkFunction(user -> processClaims(user, accessToken))).flatMap(v->RxJava2Adapter.singleToMono((Single<String>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Map<String, Object>, Single<String>>)claims -> {
                        if (!expectSignedOrEncryptedUserInfo(client)) {
                            context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                            return RxJava2Adapter.monoToSingle(Mono.just(Json.encodePrettily(claims)));
                        } else {
                            context.response().putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JWT);

                            JWT jwt = new JWT(claims);
                            jwt.setIss(openIDDiscoveryService.getIssuer(UriBuilderRequest.resolveProxyRequest(context)));
                            jwt.setSub(accessToken.getSub());
                            jwt.setAud(accessToken.getAud());
                            jwt.setIat(new Date().getTime() / 1000l);
                            jwt.setExp(accessToken.getExp() / 1000l);

                            return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(jwtService.encodeUserinfo_migrated(jwt, client))).flatMap(userinfo->RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(jweService.encryptUserinfo_migrated(userinfo, client)))));//Encrypt if needed, else return JWT
                        }
                    }).apply(v))))
                .subscribe(
                        buffer -> context.response()
                                .putHeader(HttpHeaders.CACHE_CONTROL, "no-store")
                                .putHeader(HttpHeaders.PRAGMA, "no-cache")
                                .end(buffer)
                        ,
                        context::fail
                );
    }

    /**
     * Process user claims against user data and access token information
     * @param user the end user
     * @param accessToken the access token
     * @return user claims
     */
    private Map<String, Object> processClaims(User user, JWT accessToken) {
        Map<String, Object> userClaims = user.getAdditionalInformation() == null ? new HashMap<>() : new HashMap<>(user.getAdditionalInformation());
        if (userClaims.isEmpty() || !userClaims.containsKey(StandardClaims.SUB)) {
            // The sub (subject) Claim MUST always be returned in the UserInfo Response.
            // https://openid.net/specs/openid-connect-core-1_0.html#UserInfoResponse
            throw new InvalidRequestException("UserInfo response is missing required claims");
        }

        // Exchange the sub claim from the identity provider to its technical id
        userClaims.put(StandardClaims.SUB, user.getId());

        // prepare requested claims
        Map<String, Object> requestedClaims = new HashMap<>();
        // SUB claim is required
        requestedClaims.put(StandardClaims.SUB, user.getId());

        boolean requestForSpecificClaims = false;
        // processing claims list
        // 1. process the request using scope values
        if (accessToken.getScope() != null) {
            final Set<String> scopes = new HashSet<>(Arrays.asList(accessToken.getScope().split("\\s+")));
            requestForSpecificClaims = processScopesRequest(scopes, userClaims, requestedClaims);
        }
        // 2. process the request using the claims values (If present, the listed Claims are being requested to be added to any Claims that are being requested using scope values.
        // If not present, the Claims being requested from the UserInfo Endpoint are only those requested using scope values.)
        if (accessToken.getClaimsRequestParameter() != null) {
            requestForSpecificClaims = processClaimsRequest((String) accessToken.getClaimsRequestParameter(), userClaims, requestedClaims);
        }

        return (requestForSpecificClaims) ? requestedClaims : userClaims;
    }

    /**
     * For OpenID Connect, scopes can be used to request that specific sets of information be made available as Claim Values.
     *
     * @param scopes scopes request parameter
     * @param userClaims user full claims list
     * @param requestedClaims requested claims
     * @return true if OpenID Connect scopes have been found
     */
    private boolean processScopesRequest(Set<String> scopes, final Map<String, Object> userClaims, Map<String, Object> requestedClaims) {
        // if full_profile requested, continue
        if (scopes.contains(Scope.FULL_PROFILE.getKey())) {
            return false;
        }

        // get requested scopes claims
        final List<String> scopesClaims = scopes.stream()
                .map(String::toUpperCase)
                .filter(scope -> Scope.exists(scope) && !Scope.valueOf(scope).getClaims().isEmpty())
                .map(Scope::valueOf)
                .map(Scope::getClaims)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // no OpenID Connect scopes requested continue
        if (scopesClaims.isEmpty()) {
            return false;
        }

        // return specific available sets of information made by scope value request
        scopesClaims.forEach(scopeClaim -> {
            if (userClaims.containsKey(scopeClaim)) {
                requestedClaims.putIfAbsent(scopeClaim, userClaims.get(scopeClaim));
            }
        });

        return true;
    }

    /**
     * Handle claims request previously made during the authorization request
     * @param claimsValue claims request parameter
     * @param userClaims user full claims list
     * @param requestedClaims requested claims
     * @return true if userinfo claims have been found
     */
    private boolean processClaimsRequest(String claimsValue, final Map<String, Object> userClaims, Map<String, Object> requestedClaims) {
        try {
            ClaimsRequest claimsRequest = Json.decodeValue(claimsValue, ClaimsRequest.class);
            if (claimsRequest != null && claimsRequest.getUserInfoClaims() != null) {
                claimsRequest.getUserInfoClaims().forEach((key, value) -> {
                    if (userClaims.containsKey(key)) {
                        requestedClaims.putIfAbsent(key, userClaims.get(key));
                    }
                });
                return true;
            }
        } catch (Exception e) {
            // Any members used that are not understood MUST be ignored.
        }
        return false;
    }

    /**
     * Enhance user information with roles and groups if the access token contains those scopes
     * @param user The end user
     * @param accessToken The access token with required scopes
     * @return enhanced user
     */
    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.enhance_migrated(user, accessToken))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
private Single<User> enhance(User user, JWT accessToken) {
 return RxJava2Adapter.monoToSingle(enhance_migrated(user, accessToken));
}
private Mono<User> enhance_migrated(User user, JWT accessToken) {
        if (!loadRoles(user, accessToken) && !loadGroups(accessToken)) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(user)));
        }

        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(userService.enhance_migrated(user))).map(RxJavaReactorMigrationUtil.toJdkFunction(user1 -> {
                    Map<String, Object> userClaims = user.getAdditionalInformation() == null ?
                            new HashMap<>() :
                            new HashMap<>(user.getAdditionalInformation());

                    if (user.getRolesPermissions() != null && !user.getRolesPermissions().isEmpty()) {
                        userClaims.putIfAbsent(CustomClaims.ROLES, user.getRolesPermissions().stream().map(Role::getName).collect(Collectors.toList()));
                    }
                    if (user.getGroups() != null && !user.getGroups().isEmpty()) {
                        userClaims.putIfAbsent(CustomClaims.GROUPS, user.getGroups());
                    }
                    user1.setAdditionalInformation(userClaims);
                    return user1;
                }))));
    }

    /**
     * @param client Client
     * @return Return true if client request signed or encrypted (or both) userinfo.
     */
    private boolean expectSignedOrEncryptedUserInfo(Client client) {
        return client.getUserinfoSignedResponseAlg()!=null || client.getUserinfoEncryptedResponseAlg()!=null;
    }

    private boolean loadRoles(User user, JWT accessToken) {
        return accessToken.hasScope(Scope.ROLES.getKey());
    }

    private boolean loadGroups(JWT accessToken) {
        return accessToken.hasScope(Scope.GROUPS.getKey());
    }
}

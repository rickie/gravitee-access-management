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
package io.gravitee.am.gateway.handler.oauth2.service.granter.uma;

import static io.gravitee.am.common.oauth2.Parameters.*;

import io.gravitee.am.common.exception.oauth2.InvalidTokenException;
import io.gravitee.am.common.exception.uma.RequiredClaims;
import io.gravitee.am.common.exception.uma.UmaException;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.common.oauth2.GrantType;
import io.gravitee.am.common.oauth2.TokenType;
import io.gravitee.am.gateway.handler.common.auth.user.UserAuthenticationManager;
import io.gravitee.am.gateway.handler.common.jwt.JWTService;
import io.gravitee.am.gateway.handler.context.ExecutionContextFactory;
import io.gravitee.am.gateway.handler.context.provider.ClientProperties;
import io.gravitee.am.gateway.handler.context.provider.UserProperties;
import io.gravitee.am.gateway.handler.oauth2.exception.InvalidGrantException;
import io.gravitee.am.gateway.handler.oauth2.exception.InvalidScopeException;
import io.gravitee.am.gateway.handler.oauth2.service.granter.AbstractTokenGranter;
import io.gravitee.am.gateway.handler.oauth2.service.request.OAuth2Request;
import io.gravitee.am.gateway.handler.oauth2.service.request.TokenRequest;
import io.gravitee.am.gateway.handler.oauth2.service.token.Token;
import io.gravitee.am.gateway.handler.oauth2.service.token.TokenService;
import io.gravitee.am.gateway.handler.uma.policy.DefaultRule;
import io.gravitee.am.gateway.handler.uma.policy.Rule;
import io.gravitee.am.gateway.handler.uma.policy.RulesEngine;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.User;
import io.gravitee.am.model.application.ApplicationScopeSettings;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.am.model.uma.PermissionRequest;
import io.gravitee.am.model.uma.PermissionTicket;
import io.gravitee.am.model.uma.Resource;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.service.PermissionTicketService;
import io.gravitee.am.service.ResourceService;
import io.gravitee.am.service.exception.UserInvalidException;
import io.gravitee.common.util.MultiValueMap;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.context.SimpleExecutionContext;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.vertx.core.json.JsonObject;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.util.StringUtils;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * Implementation of the User Managed Access 2.0 Grant Flow.
 * This flow enable to get an access token named RPT (Requesting Party Token), associated with a Permission Ticket.
 * See <a href="https://docs.kantarainitiative.org/uma/wg/rec-oauth-uma-grant-2.0.html#uma-grant-type">3.3.1 Client Request to Authorization Server for RPT</a>
 *
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class UMATokenGranter extends AbstractTokenGranter {

    private static final List<String> CLAIM_TOKEN_FORMAT_SUPPORTED = Arrays.asList(TokenType.ID_TOKEN);
    private UserAuthenticationManager userAuthenticationManager;
    private PermissionTicketService permissionTicketService;
    private ResourceService resourceService;
    private JWTService jwtService;
    private Domain domain;
    private RulesEngine rulesEngine;
    private ExecutionContextFactory executionContextFactory;

    public UMATokenGranter() {
        super(GrantType.UMA);
    }

    public UMATokenGranter(TokenService tokenService,
                           UserAuthenticationManager userAuthenticationManager,
                           PermissionTicketService permissionTicketService,
                           ResourceService resourceService,
                           JWTService jwtService,
                           Domain domain,
                           RulesEngine rulesEngine,
                           ExecutionContextFactory executionContextFactory) {
        this();
        setTokenService(tokenService);
        this.userAuthenticationManager = userAuthenticationManager;
        this.permissionTicketService = permissionTicketService;
        this.resourceService = resourceService;
        this.jwtService = jwtService;
        this.domain = domain;
        this.rulesEngine = rulesEngine;
        this.executionContextFactory = executionContextFactory;
    }

    @Override
    public boolean handle(String grantType, Client client) {
        return super.handle(grantType, client) &&
        domain!=null && domain.getUma()!=null && domain.getUma().isEnabled();
    }

    @Override
    public Single<Token> grant(TokenRequest tokenRequest, Client client) {
        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(parseRequest(tokenRequest, client)).flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<TokenRequest, MaybeSource<User>>toJdkFunction(tokenRequest1 -> resolveResourceOwner(tokenRequest, client)).apply(e)))).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()))
                .flatMapSingle(user -> handleRequest(tokenRequest, client, user.orElse(null)));
    }

    @Override
    protected Single<TokenRequest> parseRequest(TokenRequest tokenRequest, Client client) {
        MultiValueMap<String, String> parameters = tokenRequest.parameters();
        String ticket = parameters.getFirst(TICKET);
        String claimToken = parameters.getFirst(CLAIM_TOKEN);
        String claimTokenFormat = parameters.getFirst(CLAIM_TOKEN_FORMAT);
        String persistedClaimsToken = parameters.getFirst(PCT);
        String requestingPartyToken = parameters.getFirst(RPT);

        if(ticket == null) {
            return RxJava2Adapter.monoToSingle(Mono.error(new InvalidGrantException("Missing parameter: ticket")));
        }

        //if there's only one of both informed
        if(claimToken != null ^ claimTokenFormat != null) {
            return RxJava2Adapter.monoToSingle(Mono.error(UmaException.needInfoBuilder(ticket)
                    .requiredClaims(Arrays.asList(
                            new RequiredClaims(CLAIM_TOKEN).setFriendlyName("Requesting party token"),
                            new RequiredClaims(CLAIM_TOKEN_FORMAT).setFriendlyName("supported claims token format")
                                    .setClaimTokenFormat(CLAIM_TOKEN_FORMAT_SUPPORTED)
                    ))
                    .build()));
        }

        if(!StringUtils.isEmpty(claimTokenFormat) && !CLAIM_TOKEN_FORMAT_SUPPORTED.contains(claimTokenFormat)) {
            return RxJava2Adapter.monoToSingle(Mono.error(UmaException.needInfoBuilder(ticket)
                    .requiredClaims(Arrays.asList(new RequiredClaims(CLAIM_TOKEN_FORMAT)
                                    .setFriendlyName("supported claims token format")
                                    .setClaimTokenFormat(CLAIM_TOKEN_FORMAT_SUPPORTED)
                    ))
                    .build()));
        }

        // set required parameters
        tokenRequest.setTicket(ticket);
        // set optional parameters
        tokenRequest.setClaimToken(claimToken);
        tokenRequest.setClaimTokenFormat(claimTokenFormat);
        tokenRequest.setPersistedClaimsToken(persistedClaimsToken);
        tokenRequest.setRequestingPartyToken(requestingPartyToken);

        return super.parseRequest(tokenRequest, client);
    }

    @Override
    protected Maybe<User> resolveResourceOwner(TokenRequest tokenRequest, Client client) {
        if(StringUtils.isEmpty(tokenRequest.getClaimToken())) {
            return RxJava2Adapter.monoToMaybe(Mono.empty());
        }

        return RxJava2Adapter.monoToMaybe(RxJava2Adapter.singleToMono(jwtService.decodeAndVerify(tokenRequest.getClaimToken(), client)).flatMap(e->RxJava2Adapter.maybeToMono(Maybe.wrap(RxJavaReactorMigrationUtil.<JWT, MaybeSource<User>>toJdkFunction(jwt -> userAuthenticationManager.loadPreAuthenticatedUser(jwt.getSub(), tokenRequest)).apply(e)))).switchIfEmpty(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(UserInvalidException::new))))
                .onErrorResumeNext(ex -> {
                    //If user
                    return RxJava2Adapter.monoToMaybe(Mono.error(UmaException.needInfoBuilder(tokenRequest.getTicket())
                            .requiredClaims(Arrays.asList(
                                    new RequiredClaims(CLAIM_TOKEN).setFriendlyName("Malformed or expired claim_token")
                            ))
                            .build()));
                });
    }

    private Single<Token> handleRequest(TokenRequest tokenRequest, Client client, User endUser) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(resolveRequestedScopes(tokenRequest, client)).flatMap(tokenRequest1->RxJava2Adapter.singleToMono(this.resolvePermissions(tokenRequest1, client, endUser))).flatMap(tokenRequest1->RxJava2Adapter.singleToMono(this.createOAuth2Request(tokenRequest1, client, endUser))).flatMap(oAuth2Request->RxJava2Adapter.singleToMono(this.executePolicies(oAuth2Request, client, endUser))).flatMap(oAuth2Request->RxJava2Adapter.singleToMono(getTokenService().create(oAuth2Request, client, endUser))).map(RxJavaReactorMigrationUtil.toJdkFunction(token -> this.handleUpgradedToken(tokenRequest, token))));
    }

    /**
     * Validates the request to ensure that Client Requested UMA scopes are well pre-registered.
     */
    private Single<TokenRequest> resolveRequestedScopes(TokenRequest tokenRequest, Client client) {
        if(tokenRequest.getScopes()!=null && !tokenRequest.getScopes().isEmpty()) {
            if(client.getScopeSettings()==null || client.getScopeSettings().isEmpty() || !client.getScopeSettings().stream().map(ApplicationScopeSettings::getScope).collect(Collectors.toList()).containsAll(tokenRequest.getScopes())) {
                //TokenRequest scopes are not null and not empty, already did the check in earlier step.
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidScopeException("At least one of the scopes included in the request does not match client pre-registered scopes")));
            }
        }
        return RxJava2Adapter.monoToSingle(Mono.just(tokenRequest));
    }

    private Single<TokenRequest> resolvePermissions(TokenRequest tokenRequest, Client client, User endUser) {
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(this.permissionTicketService.remove(tokenRequest.getTicket())).map(RxJavaReactorMigrationUtil.toJdkFunction(PermissionTicket::getPermissionRequest)).flatMap(v->RxJava2Adapter.singleToMono((Single<TokenRequest>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<PermissionRequest>, Single<TokenRequest>>)permissionRequests -> {
                    List<String> resourceIds = permissionRequests.stream().map(PermissionRequest::getResourceId).collect(Collectors.toList());
                    return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(resourceService.findByResources(resourceIds)
                            .toList()).flatMap(resourceSet->RxJava2Adapter.singleToMono(this.checkRequestedScopesMatchResource(tokenRequest, resourceSet))).flatMap(resourceMap->RxJava2Adapter.singleToMono(this.resolveScopeRequestAssessment(tokenRequest, permissionRequests, resourceMap))).flatMap(resolvedPermissionRequests->RxJava2Adapter.singleToMono(this.extendPermissionWithRPT(tokenRequest, client, endUser, resolvedPermissionRequests))).map(RxJavaReactorMigrationUtil.toJdkFunction(extendedPermissionRequests -> {tokenRequest.setPermissions(extendedPermissionRequests); return tokenRequest;})));
                }).apply(v))));
    }

    /**
     * Check token request scopes are all known/referenced within the target resources
     * @param tokenRequest TokenRequest
     * @param resourceSet List<Resource>
     * @return Single<Map<String, Resource>> if no errors
     */
    private Single<Map<String, Resource>> checkRequestedScopesMatchResource(TokenRequest tokenRequest, List<Resource> resourceSet) {
        //Return InvalidScopeException if the token request contains unbounded resource set scopes.
        if(tokenRequest.getScopes()!=null && !tokenRequest.getScopes().isEmpty()) {
            Set<String> allResourcesScopes = resourceSet.stream().map(Resource::getResourceScopes).flatMap(List::stream).collect(Collectors.toSet());
            if (!allResourcesScopes.containsAll(tokenRequest.getScopes())) {
                return RxJava2Adapter.monoToSingle(Mono.error(new InvalidScopeException("At least one of the scopes included in the request does not match resource registered scopes")));
            }
        }

        //Return retrieve resource set as a map.
        return RxJava2Adapter.monoToSingle(Mono.just(resourceSet.stream().collect(Collectors.toMap(Resource::getId, resource -> resource))));
    }

    private Single<List<PermissionRequest>> resolveScopeRequestAssessment(TokenRequest tokenRequest, List<PermissionRequest> requestedPermissions, Map<String, Resource> fetchedResources) {
        //If request does not contains additional scopes, just return the permission Ticket resources.
        if(tokenRequest.getScopes()==null || tokenRequest.getScopes().isEmpty()) {
            return RxJava2Adapter.monoToSingle(Mono.just(requestedPermissions));
        }

        //Else return each Permission Requests scopes such as RequestedScopes = PermissionTicket ∪ (ClientRegistered ∩ ClientRequested ∩ RSRegistered)
        return RxJava2Adapter.monoToSingle(Mono.just(requestedPermissions.stream()
                .map(permissionRequest -> {
                    Set<String> registeredScopes = new HashSet(fetchedResources.get(permissionRequest.getResourceId()).getResourceScopes());
                    //RequestedScopes = PermissionTicket ∪ (ClientRequested ∩ RSRegistered) /!\ ClientRegistered scopes already have been checked earlier. (#resolveRequest)
                    permissionRequest.getResourceScopes().addAll(tokenRequest.getScopes().stream().filter(registeredScopes::contains).collect(Collectors.toSet()));
                    return permissionRequest;
                })
                .collect(Collectors.toList())));
    }

    /**
     * If a valid Requesting Party Token was provided, then we inject previous permissions from this RPT.
     * This permissions must be reviewed by the Permission Policy Management as some of them may have been revoked in the meantime.
     * @param tokenRequest TokenRequest
     * @param client Client
     * @param requestedPermissions List<PermissionRequest>
     * @return List<PermissionRequest>
     */
    private Single<List<PermissionRequest>> extendPermissionWithRPT(TokenRequest tokenRequest, Client client, User endUser, List<PermissionRequest> requestedPermissions) {
        if(!StringUtils.isEmpty(tokenRequest.getRequestingPartyToken())) {
            return RxJava2Adapter.monoToSingle(RxJava2Adapter.singleToMono(jwtService.decodeAndVerify(tokenRequest.getRequestingPartyToken(), client)).flatMap(rpt->RxJava2Adapter.singleToMono(this.checkRequestingPartyToken(rpt, client, endUser))).map(RxJavaReactorMigrationUtil.toJdkFunction(rpt -> this.mergePermissions(rpt, requestedPermissions))))
                    .onErrorResumeNext(throwable -> RxJava2Adapter.monoToSingle(Mono.error(new InvalidGrantException("Requesting Party Token (rpt) not valid"))));
        }

        return RxJava2Adapter.monoToSingle(Mono.just(requestedPermissions));
    }

    /**
     * Provided "previous" Requesting Party Token to extend must belong to the same user & client.
     */
    private Single<JWT> checkRequestingPartyToken(JWT rpt, Client client, User user) {
        String expectedSub = user!=null?user.getId():client.getClientId();
        if(!expectedSub.equals(rpt.getSub()) || !client.getClientId().equals(rpt.getAud())) {
            return RxJava2Adapter.monoToSingle(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(InvalidTokenException::new)));
        }
        return RxJava2Adapter.monoToSingle(Mono.just(rpt));
    }

    private List<PermissionRequest> mergePermissions(JWT rpt, List<PermissionRequest> requested) {
        if(rpt.get("permissions")!=null) {
            //Build Map with current request
            Map<String, PermissionRequest> newRequestedPermission = requested.stream().collect(Collectors.toMap(PermissionRequest::getResourceId,pr->pr));
            //Build map with previous permissions from the old RPT (Requesting Party Token)
            Map<String, PermissionRequest> rptPermission = convert(((List)rpt.get("permissions")));
            //Merge both
            return Stream.concat(newRequestedPermission.entrySet().stream(), rptPermission.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (requestedPermission, fromRpt) -> {
                        requestedPermission.setResourceScopes(
                                Stream.concat(
                                        requestedPermission.getResourceScopes().stream(),
                                        fromRpt.getResourceScopes().stream()
                                ).distinct().collect(Collectors.toList())//Keep distinct values
                        );
                        return requestedPermission;
                    }))
                    .values().stream().collect(Collectors.toList());
        }

        return requested;
    }

    private Map<String, PermissionRequest> convert(List<HashMap> permissions) {
        Map<String,PermissionRequest> result = new LinkedHashMap<>(permissions.size());
        for(HashMap permission : permissions) {
            JsonObject json = new JsonObject(permission);
            PermissionRequest permissionRequest = json.mapTo(PermissionRequest.class);
            result.put(permissionRequest.getResourceId(), permissionRequest);
        }
        return result;
    }

    private Single<OAuth2Request> createOAuth2Request(TokenRequest tokenRequest, Client client, User endUser) {
        //Remove Token Request scopes as they are now injected into each permission requests.
        tokenRequest.setScopes(null);

        //Create Request
        OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request();
        //Set User
        oAuth2Request.setSubject(endUser!=null?endUser.getId():oAuth2Request.getSubject());
        //Client may have refresh_token grant, but if request is not made for an end user, then we should not generate refresh.
        oAuth2Request.setSupportRefreshToken(endUser!=null && isSupportRefreshToken(client));

        return RxJava2Adapter.monoToSingle(Mono.just(oAuth2Request));
    }

    private Token handleUpgradedToken(TokenRequest tokenRequest, Token token) {
        if(tokenRequest.getRequestingPartyToken()!=null) {
            token.setUpgraded(true);
        }
        return token;
    }

    /**
     * Do not call super.resolveRequest(xxx), as scopes have nothing related with oauth2 scopes mindset.
     * While oauth2 imply user approvals (RO grant access to his resources to an application),
     * here the subject is a Requesting Party that request grant access to someone else resources.
     */
    protected Single<TokenRequest> resolveRequest(TokenRequest tokenRequest, Client client, User endUser) {
        return RxJava2Adapter.monoToSingle(Mono.error(new TechnicalException("Should not be used")));
    }

    /**
     * The resource owner works with the authorization server to configure policy conditions (authorization grant rules), which the authorization server executes in the process of issuing access tokens.
     * The authorization process makes use of claims gathered from the requesting party and client in order to satisfy all operative operative policy conditions.
     * @param oAuth2Request OAuth 2.0 Token Request
     * @param client client
     * @param endUser requesting party
     * @return
     */
    private Single<OAuth2Request> executePolicies(OAuth2Request oAuth2Request, Client client, User endUser) {
        List<PermissionRequest> permissionRequests = oAuth2Request.getPermissions();
        if (permissionRequests == null || permissionRequests.isEmpty()) {
            return RxJava2Adapter.monoToSingle(Mono.just(oAuth2Request));
        }
        List<String> resourceIds = permissionRequests.stream().map(PermissionRequest::getResourceId).collect(Collectors.toList());
        // find access policies for the given resources
        return RxJava2Adapter.monoToSingle(RxJava2Adapter.flowableToFlux(resourceService.findAccessPoliciesByResources(resourceIds)).map(RxJavaReactorMigrationUtil.toJdkFunction(accessPolicy -> {
                    Rule rule = new DefaultRule(accessPolicy);
                    Optional<PermissionRequest> permission = permissionRequests
                            .stream()
                            .filter(permissionRequest -> permissionRequest.getResourceId().equals(accessPolicy.getResource()))
                            .findFirst();
                    if (permission.isPresent()) {
                        ((DefaultRule) rule).setMetadata(Collections.singletonMap("permissionRequest", permission.get()));
                    }
                    return rule;
                })).collectList().flatMap(v->RxJava2Adapter.singleToMono((Single<OAuth2Request>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<Rule>, Single<OAuth2Request>>)rules -> {
                    // no policy registered, continue
                    if (rules.isEmpty()) {
                        return RxJava2Adapter.monoToSingle(Mono.just(oAuth2Request));
                    }
                    // prepare the execution context
                    ExecutionContext simpleExecutionContext = new SimpleExecutionContext(oAuth2Request, oAuth2Request.getHttpResponse());
                    ExecutionContext executionContext = executionContextFactory.create(simpleExecutionContext);
                    executionContext.setAttribute("client", new ClientProperties(client));
                    if (endUser != null) {
                        executionContext.setAttribute("user", new UserProperties(endUser));
                    }
                    // execute the policies
                    return rulesEngine.fire(rules, executionContext)
                            .toSingleDefault(oAuth2Request)
                            .onErrorResumeNext(ex -> RxJava2Adapter.monoToSingle(Mono.error(new InvalidGrantException("Policy conditions are not met for actual request parameters"))));
                }).apply(v))));
    }
}

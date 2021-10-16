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

import com.google.errorprone.annotations.InlineMe;
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

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.grant_migrated(tokenRequest, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Token> grant(TokenRequest tokenRequest, Client client) {
 return RxJava2Adapter.monoToSingle(grant_migrated(tokenRequest, client));
}
@Override
    public Mono<Token> grant_migrated(TokenRequest tokenRequest, Client client) {
        return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToMaybe(parseRequest_migrated(tokenRequest, client).flatMap(e->resolveResourceOwner_migrated(tokenRequest, client)).map(RxJavaReactorMigrationUtil.toJdkFunction(Optional::of)).defaultIfEmpty(Optional.empty()))
                .flatMapSingle(user -> RxJava2Adapter.monoToSingle(handleRequest_migrated(tokenRequest, client, user.orElse(null)))));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.parseRequest_migrated(tokenRequest, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    protected Single<TokenRequest> parseRequest(TokenRequest tokenRequest, Client client) {
 return RxJava2Adapter.monoToSingle(parseRequest_migrated(tokenRequest, client));
}
@Override
    protected Mono<TokenRequest> parseRequest_migrated(TokenRequest tokenRequest, Client client) {
        MultiValueMap<String, String> parameters = tokenRequest.parameters();
        String ticket = parameters.getFirst(TICKET);
        String claimToken = parameters.getFirst(CLAIM_TOKEN);
        String claimTokenFormat = parameters.getFirst(CLAIM_TOKEN_FORMAT);
        String persistedClaimsToken = parameters.getFirst(PCT);
        String requestingPartyToken = parameters.getFirst(RPT);

        if(ticket == null) {
            return Mono.error(new InvalidGrantException("Missing parameter: ticket"));
        }

        //if there's only one of both informed
        if(claimToken != null ^ claimTokenFormat != null) {
            return Mono.error(UmaException.needInfoBuilder(ticket)
                    .requiredClaims(Arrays.asList(
                            new RequiredClaims(CLAIM_TOKEN).setFriendlyName("Requesting party token"),
                            new RequiredClaims(CLAIM_TOKEN_FORMAT).setFriendlyName("supported claims token format")
                                    .setClaimTokenFormat(CLAIM_TOKEN_FORMAT_SUPPORTED)
                    ))
                    .build());
        }

        if(!StringUtils.isEmpty(claimTokenFormat) && !CLAIM_TOKEN_FORMAT_SUPPORTED.contains(claimTokenFormat)) {
            return Mono.error(UmaException.needInfoBuilder(ticket)
                    .requiredClaims(Arrays.asList(new RequiredClaims(CLAIM_TOKEN_FORMAT)
                                    .setFriendlyName("supported claims token format")
                                    .setClaimTokenFormat(CLAIM_TOKEN_FORMAT_SUPPORTED)
                    ))
                    .build());
        }

        // set required parameters
        tokenRequest.setTicket(ticket);
        // set optional parameters
        tokenRequest.setClaimToken(claimToken);
        tokenRequest.setClaimTokenFormat(claimTokenFormat);
        tokenRequest.setPersistedClaimsToken(persistedClaimsToken);
        tokenRequest.setRequestingPartyToken(requestingPartyToken);

        return super.parseRequest_migrated(tokenRequest, client);
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.resolveResourceOwner_migrated(tokenRequest, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    protected Maybe<User> resolveResourceOwner(TokenRequest tokenRequest, Client client) {
 return RxJava2Adapter.monoToMaybe(resolveResourceOwner_migrated(tokenRequest, client));
}
@Override
    protected Mono<User> resolveResourceOwner_migrated(TokenRequest tokenRequest, Client client) {
        if(StringUtils.isEmpty(tokenRequest.getClaimToken())) {
            return Mono.empty();
        }

        return RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(jwtService.decodeAndVerify_migrated(tokenRequest.getClaimToken(), client).flatMap(e->userAuthenticationManager.loadPreAuthenticatedUser_migrated(e.getSub(), tokenRequest)).switchIfEmpty(Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(UserInvalidException::new))))
                .onErrorResumeNext(ex -> {
                    //If user
                    return RxJava2Adapter.monoToMaybe(Mono.error(UmaException.needInfoBuilder(tokenRequest.getTicket())
                            .requiredClaims(Arrays.asList(
                                    new RequiredClaims(CLAIM_TOKEN).setFriendlyName("Malformed or expired claim_token")
                            ))
                            .build()));
                }));
    }

    
private Mono<Token> handleRequest_migrated(TokenRequest tokenRequest, Client client, User endUser) {
        return resolveRequestedScopes_migrated(tokenRequest, client).flatMap(tokenRequest1->this.resolvePermissions_migrated(tokenRequest1, client, endUser)).flatMap(tokenRequest1->this.createOAuth2Request_migrated(tokenRequest1, client, endUser)).flatMap(oAuth2Request->this.executePolicies_migrated(oAuth2Request, client, endUser)).flatMap(oAuth2Request->getTokenService().create_migrated(oAuth2Request, client, endUser)).map(RxJavaReactorMigrationUtil.toJdkFunction(token -> this.handleUpgradedToken(tokenRequest, token)));
    }

    
private Mono<TokenRequest> resolveRequestedScopes_migrated(TokenRequest tokenRequest, Client client) {
        if(tokenRequest.getScopes()!=null && !tokenRequest.getScopes().isEmpty()) {
            if(client.getScopeSettings()==null || client.getScopeSettings().isEmpty() || !client.getScopeSettings().stream().map(ApplicationScopeSettings::getScope).collect(Collectors.toList()).containsAll(tokenRequest.getScopes())) {
                //TokenRequest scopes are not null and not empty, already did the check in earlier step.
                return Mono.error(new InvalidScopeException("At least one of the scopes included in the request does not match client pre-registered scopes"));
            }
        }
        return Mono.just(tokenRequest);
    }

    
private Mono<TokenRequest> resolvePermissions_migrated(TokenRequest tokenRequest, Client client, User endUser) {
        return this.permissionTicketService.remove_migrated(tokenRequest.getTicket()).map(RxJavaReactorMigrationUtil.toJdkFunction(PermissionTicket::getPermissionRequest)).flatMap(v->RxJava2Adapter.singleToMono((Single<TokenRequest>)RxJavaReactorMigrationUtil.toJdkFunction((Function<List<PermissionRequest>, Single<TokenRequest>>)permissionRequests -> {
                    List<String> resourceIds = permissionRequests.stream().map(PermissionRequest::getResourceId).collect(Collectors.toList());
                    return RxJava2Adapter.monoToSingle(resourceService.findByResources_migrated(resourceIds).collectList().flatMap(resourceSet->this.checkRequestedScopesMatchResource_migrated(tokenRequest, resourceSet)).flatMap(resourceMap->this.resolveScopeRequestAssessment_migrated(tokenRequest, permissionRequests, resourceMap)).flatMap(resolvedPermissionRequests->this.extendPermissionWithRPT_migrated(tokenRequest, client, endUser, resolvedPermissionRequests)).map(RxJavaReactorMigrationUtil.toJdkFunction(extendedPermissionRequests -> {tokenRequest.setPermissions(extendedPermissionRequests); return tokenRequest;})));
                }).apply(v)));
    }

    
private Mono<Map<String,Resource>> checkRequestedScopesMatchResource_migrated(TokenRequest tokenRequest, List<Resource> resourceSet) {
        //Return InvalidScopeException if the token request contains unbounded resource set scopes.
        if(tokenRequest.getScopes()!=null && !tokenRequest.getScopes().isEmpty()) {
            Set<String> allResourcesScopes = resourceSet.stream().map(Resource::getResourceScopes).flatMap(List::stream).collect(Collectors.toSet());
            if (!allResourcesScopes.containsAll(tokenRequest.getScopes())) {
                return Mono.error(new InvalidScopeException("At least one of the scopes included in the request does not match resource registered scopes"));
            }
        }

        //Return retrieve resource set as a map.
        return Mono.just(resourceSet.stream().collect(Collectors.toMap(Resource::getId, resource -> resource)));
    }

    
private Mono<List<PermissionRequest>> resolveScopeRequestAssessment_migrated(TokenRequest tokenRequest, List<PermissionRequest> requestedPermissions, Map<String, Resource> fetchedResources) {
        //If request does not contains additional scopes, just return the permission Ticket resources.
        if(tokenRequest.getScopes()==null || tokenRequest.getScopes().isEmpty()) {
            return Mono.just(requestedPermissions);
        }

        //Else return each Permission Requests scopes such as RequestedScopes = PermissionTicket ∪ (ClientRegistered ∩ ClientRequested ∩ RSRegistered)
        return Mono.just(requestedPermissions.stream()
                .map(permissionRequest -> {
                    Set<String> registeredScopes = new HashSet(fetchedResources.get(permissionRequest.getResourceId()).getResourceScopes());
                    //RequestedScopes = PermissionTicket ∪ (ClientRequested ∩ RSRegistered) /!\ ClientRegistered scopes already have been checked earlier. (#resolveRequest)
                    permissionRequest.getResourceScopes().addAll(tokenRequest.getScopes().stream().filter(registeredScopes::contains).collect(Collectors.toSet()));
                    return permissionRequest;
                })
                .collect(Collectors.toList()));
    }

    
private Mono<List<PermissionRequest>> extendPermissionWithRPT_migrated(TokenRequest tokenRequest, Client client, User endUser, List<PermissionRequest> requestedPermissions) {
        if(!StringUtils.isEmpty(tokenRequest.getRequestingPartyToken())) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(jwtService.decodeAndVerify_migrated(tokenRequest.getRequestingPartyToken(), client).flatMap(rpt->this.checkRequestingPartyToken_migrated(rpt, client, endUser)).map(RxJavaReactorMigrationUtil.toJdkFunction(rpt -> this.mergePermissions(rpt, requestedPermissions))))
                    .onErrorResumeNext(throwable -> RxJava2Adapter.monoToSingle(Mono.error(new InvalidGrantException("Requesting Party Token (rpt) not valid")))));
        }

        return Mono.just(requestedPermissions);
    }

    
private Mono<JWT> checkRequestingPartyToken_migrated(JWT rpt, Client client, User user) {
        String expectedSub = user!=null?user.getId():client.getClientId();
        if(!expectedSub.equals(rpt.getSub()) || !client.getClientId().equals(rpt.getAud())) {
            return Mono.error(RxJavaReactorMigrationUtil.callableAsSupplier(InvalidTokenException::new));
        }
        return Mono.just(rpt);
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

    
private Mono<OAuth2Request> createOAuth2Request_migrated(TokenRequest tokenRequest, Client client, User endUser) {
        //Remove Token Request scopes as they are now injected into each permission requests.
        tokenRequest.setScopes(null);

        //Create Request
        OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request();
        //Set User
        oAuth2Request.setSubject(endUser!=null?endUser.getId():oAuth2Request.getSubject());
        //Client may have refresh_token grant, but if request is not made for an end user, then we should not generate refresh.
        oAuth2Request.setSupportRefreshToken(endUser!=null && isSupportRefreshToken(client));

        return Mono.just(oAuth2Request);
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
    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.resolveRequest_migrated(tokenRequest, client, endUser))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Single<TokenRequest> resolveRequest(TokenRequest tokenRequest, Client client, User endUser) {
 return RxJava2Adapter.monoToSingle(resolveRequest_migrated(tokenRequest, client, endUser));
}
protected Mono<TokenRequest> resolveRequest_migrated(TokenRequest tokenRequest, Client client, User endUser) {
        return Mono.error(new TechnicalException("Should not be used"));
    }

    
private Mono<OAuth2Request> executePolicies_migrated(OAuth2Request oAuth2Request, Client client, User endUser) {
        List<PermissionRequest> permissionRequests = oAuth2Request.getPermissions();
        if (permissionRequests == null || permissionRequests.isEmpty()) {
            return Mono.just(oAuth2Request);
        }
        List<String> resourceIds = permissionRequests.stream().map(PermissionRequest::getResourceId).collect(Collectors.toList());
        // find access policies for the given resources
        return resourceService.findAccessPoliciesByResources_migrated(resourceIds).map(RxJavaReactorMigrationUtil.toJdkFunction(accessPolicy -> {
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
                    return RxJava2Adapter.monoToCompletable(rulesEngine.fire_migrated(rules, executionContext))
                            .toSingleDefault(oAuth2Request)
                            .onErrorResumeNext(ex -> RxJava2Adapter.monoToSingle(Mono.error(new InvalidGrantException("Policy conditions are not met for actual request parameters"))));
                }).apply(v)));
    }
}

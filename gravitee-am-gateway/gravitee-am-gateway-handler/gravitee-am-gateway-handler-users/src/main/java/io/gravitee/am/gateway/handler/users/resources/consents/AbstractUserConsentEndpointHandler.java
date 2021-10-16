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
package io.gravitee.am.gateway.handler.users.resources.consents;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.jwt.Claims;
import io.gravitee.am.common.jwt.JWT;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.common.utils.ConstantKeys;
import io.gravitee.am.gateway.handler.common.vertx.utils.RequestUtils;
import io.gravitee.am.gateway.handler.users.service.UserService;
import io.gravitee.am.identityprovider.api.DefaultUser;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Domain;
import io.reactivex.Single;
import io.vertx.reactivex.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AbstractUserConsentEndpointHandler {

    protected UserService userService;
    private ClientSyncService clientSyncService;
    private Domain domain;

    public AbstractUserConsentEndpointHandler(UserService userService, ClientSyncService clientSyncService, Domain domain) {
        this.userService = userService;
        this.clientSyncService = clientSyncService;
        this.domain = domain;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getPrincipal_migrated(context))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
protected Single<User> getPrincipal(RoutingContext context) {
 return RxJava2Adapter.monoToSingle(getPrincipal_migrated(context));
}
protected Mono<User> getPrincipal_migrated(RoutingContext context) {
        JWT token = context.get(ConstantKeys.TOKEN_CONTEXT_KEY);

        if (token.getSub() == null) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(defaultPrincipal(context, token))));
        }

        // end user
        if (!token.getSub().equals(token.getAud())) {
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(userService.findById_migrated(token.getSub()))).map(RxJavaReactorMigrationUtil.toJdkFunction(user -> {
                        User principal = new DefaultUser(user.getUsername());
                        ((DefaultUser) principal).setId(user.getId());
                        Map<String, Object> additionalInformation =
                                user.getAdditionalInformation() != null ? new HashMap<>(user.getAdditionalInformation()) :  new HashMap<>();
                        // add ip address and user agent
                        additionalInformation.put(Claims.ip_address, RequestUtils.remoteAddress(context.request()));
                        additionalInformation.put(Claims.user_agent, RequestUtils.userAgent(context.request()));
                        additionalInformation.put(Claims.domain, domain.getId());
                        ((DefaultUser) principal).setAdditionalInformation(additionalInformation);
                        return principal;
                    })).defaultIfEmpty(defaultPrincipal(context, token)).single()));
        } else {
            // revocation made oauth2 clients
            return RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(clientSyncService.findByClientId_migrated(token.getAud()))).map(RxJavaReactorMigrationUtil.toJdkFunction(client -> {
                        User principal = new DefaultUser(client.getClientId());
                        ((DefaultUser) principal).setId(client.getId());
                        Map<String, Object> additionalInformation = new HashMap<>();
                        // add ip address and user agent
                        additionalInformation.put(Claims.ip_address, RequestUtils.remoteAddress(context.request()));
                        additionalInformation.put(Claims.user_agent, RequestUtils.userAgent(context.request()));
                        additionalInformation.put(Claims.domain, domain.getId());
                        ((DefaultUser) principal).setAdditionalInformation(additionalInformation);
                        return principal;
                    })).defaultIfEmpty(defaultPrincipal(context, token)).single()));
        }

    }

    private User defaultPrincipal(RoutingContext context, JWT token) {
        final String username = token.getSub() != null ? token.getSub() : "unknown-user";
        User principal = new DefaultUser(username);
        ((DefaultUser) principal).setId(username);
        Map<String, Object> additionalInformation = new HashMap<>();
        // add ip address and user agent
        additionalInformation.put(Claims.ip_address, RequestUtils.remoteAddress(context.request()));
        additionalInformation.put(Claims.user_agent, RequestUtils.userAgent(context.request()));
        additionalInformation.put(Claims.domain, domain.getId());
        ((DefaultUser) principal).setAdditionalInformation(additionalInformation);
        return principal;
    }

}

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
package io.gravitee.am.management.service.impl.upgrades;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import io.gravitee.am.management.service.impl.upgrades.OpenIDScopeUpgrader;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oauth2.Scope;
import io.gravitee.am.service.DomainService;
import io.gravitee.am.service.ScopeService;
import io.gravitee.am.service.model.NewSystemScope;
import io.gravitee.am.service.model.UpdateSystemScope;


import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import reactor.core.publisher.Mono;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenIDScopeUpgraderTest {

    @InjectMocks
    private OpenIDScopeUpgrader openIDScopeUpgrader = new OpenIDScopeUpgrader();

    @Mock
    private DomainService domainService;

    @Mock
    private ScopeService scopeService;

    @Mock
    private Domain domain;

    private static final String DOMAIN_ID = "domainId";

    @Before
    public void setUp() {
        when(domain.getId()).thenReturn(DOMAIN_ID);
        when(domainService.findAll_migrated()).thenReturn(Mono.just(Arrays.asList(domain)));
    }

    @Test
    public void shouldCreateSystemScope() {
        when(scopeService.findByDomainAndKey_migrated(eq(DOMAIN_ID), anyString())).thenReturn(Mono.empty());
        when(scopeService.create_migrated(anyString(),any(NewSystemScope.class))).thenReturn(Mono.just(new Scope()));

        assertTrue(openIDScopeUpgrader.upgrade());
        verify(scopeService, times(io.gravitee.am.common.oidc.Scope.values().length)).create_migrated(anyString(), any(NewSystemScope.class));
    }

    @Test
    public void shouldUpdateSystemScope() {
        Scope openId = new Scope();
        openId.setId("1");
        openId.setSystem(false);//expect to be updated because not set as system
        openId.setKey("openid");

        Scope phone = new Scope();
        phone.setId("2");
        phone.setSystem(true);
        phone.setKey("phone");
        phone.setDiscovery(false);//expect to be updated because not same discovery value

        Scope email = new Scope();
        email.setId("3");
        email.setSystem(true);//expect not to be updated
        email.setKey("email");
        email.setDiscovery(true);

        when(scopeService.findByDomainAndKey_migrated(eq(DOMAIN_ID), anyString())).thenReturn(Mono.empty());
        when(scopeService.findByDomainAndKey_migrated(DOMAIN_ID, "openid")).thenReturn(Mono.just(openId));
        when(scopeService.findByDomainAndKey_migrated(DOMAIN_ID, "phone")).thenReturn(Mono.just(phone));
        when(scopeService.findByDomainAndKey_migrated(DOMAIN_ID, "email")).thenReturn(Mono.just(email));
        when(scopeService.create_migrated(anyString(),any(NewSystemScope.class))).thenReturn(Mono.just(new Scope()));
        when(scopeService.update_migrated(anyString(), anyString(), any(UpdateSystemScope.class))).thenReturn(Mono.just(new Scope()));

        assertTrue(openIDScopeUpgrader.upgrade());
        verify(scopeService, times(io.gravitee.am.common.oidc.Scope.values().length-3)).create_migrated(anyString(), any(NewSystemScope.class));
        verify(scopeService, times(2)).update_migrated(anyString(), anyString(), any(UpdateSystemScope.class));
    }
}

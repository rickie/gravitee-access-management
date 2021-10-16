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
package io.gravitee.am.gateway.handler.saml2.service.sp;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.gravitee.am.gateway.handler.common.auth.idp.IdentityProviderManager;
import io.gravitee.am.gateway.handler.saml2.service.sp.impl.ServiceProviderServiceImpl;
import io.gravitee.am.identityprovider.api.AuthenticationProvider;
import io.gravitee.am.identityprovider.api.Metadata;
import io.gravitee.am.service.exception.IdentityProviderMetadataNotFoundException;
import io.gravitee.am.service.exception.IdentityProviderNotFoundException;
import io.reactivex.Maybe;
import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceProviderServiceTest {

    @InjectMocks
    private ServiceProviderService serviceProviderService = new ServiceProviderServiceImpl();

    @Mock
    private IdentityProviderManager identityProviderManager;

    @Test
    public void shouldNotGetMetadata_idp_not_found() {
        when(identityProviderManager.get_migrated("provider-id")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.empty())));
        TestObserver testObserver = RxJava2Adapter.monoToSingle(serviceProviderService.metadata_migrated("provider-id", "https://idp.example.com")).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(IdentityProviderNotFoundException.class);
    }

    @Test
    public void shouldNotGetMetadata_null_metadata() {
        AuthenticationProvider authenticationProvider = mock(AuthenticationProvider.class);
        when(authenticationProvider.metadata("https://idp.example.com")).thenReturn(null);
        when(identityProviderManager.get_migrated("provider-id")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(authenticationProvider))));
        TestObserver testObserver = RxJava2Adapter.monoToSingle(serviceProviderService.metadata_migrated("provider-id", "https://idp.example.com")).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertError(IdentityProviderMetadataNotFoundException.class);
    }

    @Test
    public void shouldGetMetadata() {
        Metadata metadata = mock(Metadata.class);
        when(metadata.getBody()).thenReturn("metadata-payload");
        AuthenticationProvider authenticationProvider = mock(AuthenticationProvider.class);
        when(authenticationProvider.metadata("https://idp.example.com")).thenReturn(metadata);
        when(identityProviderManager.get_migrated("provider-id")).thenReturn(RxJava2Adapter.maybeToMono(RxJava2Adapter.monoToMaybe(Mono.just(authenticationProvider))));
        TestObserver<Metadata> testObserver = RxJava2Adapter.monoToSingle(serviceProviderService.metadata_migrated("provider-id", "https://idp.example.com")).test();
        testObserver.awaitTerminalEvent();
        testObserver.assertNoErrors();
        testObserver.assertValue(m -> m.getBody().equals("metadata-payload"));
    }

}

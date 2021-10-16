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
package io.gravitee.am.identityprovider.api.social;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.Authentication;
import io.gravitee.am.identityprovider.api.AuthenticationProvider;
import io.gravitee.am.identityprovider.api.common.Request;
import io.reactivex.Maybe;
import java.util.Optional;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface SocialAuthenticationProvider extends AuthenticationProvider {

    /**
     * Generate the signIn Url.
     *
     * @param redirectUri
     * @return
     * @Deprecated use the asyncSignInUrl instead
     */
    @Deprecated
    Request signInUrl(String redirectUri, String state);

    /**
     * Generate the signIn Url in asynchronous way
     * to avoid blocking thread when an http
     * call out is required to generate the url.
     *
     * @param redirectUri
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.asyncSignInUrl_migrated(redirectUri, state))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.identityprovider.api.common.Request> asyncSignInUrl(java.lang.String redirectUri, java.lang.String state) {
    return RxJava2Adapter.monoToMaybe(asyncSignInUrl_migrated(redirectUri, state));
}default Mono<Request> asyncSignInUrl_migrated(String redirectUri, String state) {
        Request request = signInUrl(redirectUri, state);
        if (request != null) {
            return Mono.just(request);
        } else {
            return Mono.empty();
        }
    }

    /**
     * Get the logout endpoint related to the IdentityProvider (ex: "End Session Endpoint" in OIDC)
     * By default, do nothing since not all IdentityProvider implement this capabilities
     *
     * @return
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.signOutUrl_migrated(authentication))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.identityprovider.api.common.Request> signOutUrl(io.gravitee.am.identityprovider.api.Authentication authentication) {
    return RxJava2Adapter.monoToMaybe(signOutUrl_migrated(authentication));
}default Mono<Request> signOutUrl_migrated(Authentication authentication) {
        return Mono.empty();
    }
}

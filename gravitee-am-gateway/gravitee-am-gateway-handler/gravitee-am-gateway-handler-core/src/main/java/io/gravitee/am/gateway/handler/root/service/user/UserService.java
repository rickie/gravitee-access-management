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
package io.gravitee.am.gateway.handler.root.service.user;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.root.service.response.RegistrationResponse;
import io.gravitee.am.gateway.handler.root.service.response.ResetPasswordResponse;
import io.gravitee.am.gateway.handler.root.service.user.model.ForgotPasswordParameters;
import io.gravitee.am.gateway.handler.root.service.user.model.UserToken;
import io.gravitee.am.model.User;
import io.gravitee.am.model.factor.EnrolledFactor;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface UserService {

      
Mono<UserToken> verifyToken_migrated(String token);

      
Mono<RegistrationResponse> register_migrated(Client client, User user, io.gravitee.am.identityprovider.api.User principal);

      
Mono<RegistrationResponse> confirmRegistration_migrated(Client client, User user, io.gravitee.am.identityprovider.api.User principal);

      
Mono<ResetPasswordResponse> resetPassword_migrated(Client client, User user, io.gravitee.am.identityprovider.api.User principal);

      
Mono<Void> forgotPassword_migrated(ForgotPasswordParameters inputParameters, Client client, io.gravitee.am.identityprovider.api.User principal);

      
Mono<io.gravitee.am.model.User> addFactor_migrated(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal);

      default Mono<RegistrationResponse> register_migrated(Client client, User user) {
          return register_migrated(client, user, null);
      }

     default Mono<ResetPasswordResponse> resetPassword_migrated(Client client, User user) {
          return resetPassword_migrated(client, user, null);
     }

      default Mono<Void> forgotPassword_migrated(String email, Client client) {
        ForgotPasswordParameters params = new ForgotPasswordParameters(email, false, false);
        return forgotPassword_migrated(params, client, null);
    }

      default Mono<RegistrationResponse> confirmRegistration_migrated(Client client, User user) {
          return confirmRegistration_migrated(client, user, null);
      }

}

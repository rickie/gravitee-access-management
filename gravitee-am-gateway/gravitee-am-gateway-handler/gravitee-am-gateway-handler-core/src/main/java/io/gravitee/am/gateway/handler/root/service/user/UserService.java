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

      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.verifyToken_migrated(token))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Maybe<io.gravitee.am.gateway.handler.root.service.user.model.UserToken> verifyToken(java.lang.String token) {
    return RxJava2Adapter.monoToMaybe(verifyToken_migrated(token));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.root.service.user.model.UserToken> verifyToken_migrated(String token) {
    return RxJava2Adapter.maybeToMono(verifyToken(token));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.register_migrated(client, user, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.root.service.response.RegistrationResponse> register(io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(register_migrated(client, user, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.root.service.response.RegistrationResponse> register_migrated(Client client, User user, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(register(client, user, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.confirmRegistration_migrated(client, user, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.root.service.response.RegistrationResponse> confirmRegistration(io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(confirmRegistration_migrated(client, user, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.root.service.response.RegistrationResponse> confirmRegistration_migrated(Client client, User user, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(confirmRegistration(client, user, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.resetPassword_migrated(client, user, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.root.service.response.ResetPasswordResponse> resetPassword(io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(resetPassword_migrated(client, user, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.gateway.handler.root.service.response.ResetPasswordResponse> resetPassword_migrated(Client client, User user, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(resetPassword(client, user, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.forgotPassword_migrated(inputParameters, client, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable forgotPassword(io.gravitee.am.gateway.handler.root.service.user.model.ForgotPasswordParameters inputParameters, io.gravitee.am.model.oidc.Client client, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToCompletable(forgotPassword_migrated(inputParameters, client, principal));
}
default reactor.core.publisher.Mono<java.lang.Void> forgotPassword_migrated(ForgotPasswordParameters inputParameters, Client client, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.completableToMono(forgotPassword(inputParameters, client, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.addFactor_migrated(userId, enrolledFactor, principal))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.User> addFactor(java.lang.String userId, io.gravitee.am.model.factor.EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.monoToSingle(addFactor_migrated(userId, enrolledFactor, principal));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.User> addFactor_migrated(String userId, EnrolledFactor enrolledFactor, io.gravitee.am.identityprovider.api.User principal) {
    return RxJava2Adapter.singleToMono(addFactor(userId, enrolledFactor, principal));
}

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.register_migrated(client, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.root.service.response.RegistrationResponse> register(io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(register_migrated(client, user));
}default Mono<RegistrationResponse> register_migrated(Client client, User user) {
        return RxJava2Adapter.singleToMono(register(client, user, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.resetPassword_migrated(client, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.root.service.response.ResetPasswordResponse> resetPassword(io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(resetPassword_migrated(client, user));
}default Mono<ResetPasswordResponse> resetPassword_migrated(Client client, User user) {
        return RxJava2Adapter.singleToMono(resetPassword(client, user, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.forgotPassword_migrated(email, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable forgotPassword(java.lang.String email, io.gravitee.am.model.oidc.Client client) {
    return RxJava2Adapter.monoToCompletable(forgotPassword_migrated(email, client));
}default Mono<Void> forgotPassword_migrated(String email, Client client) {
        ForgotPasswordParameters params = new ForgotPasswordParameters(email, false, false);
        return RxJava2Adapter.completableToMono(forgotPassword(params, client, null));
    }

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.confirmRegistration_migrated(client, user))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.gateway.handler.root.service.response.RegistrationResponse> confirmRegistration(io.gravitee.am.model.oidc.Client client, io.gravitee.am.model.User user) {
    return RxJava2Adapter.monoToSingle(confirmRegistration_migrated(client, user));
}default Mono<RegistrationResponse> confirmRegistration_migrated(Client client, User user) {
        return RxJava2Adapter.singleToMono(confirmRegistration(client, user, null));
    }

}

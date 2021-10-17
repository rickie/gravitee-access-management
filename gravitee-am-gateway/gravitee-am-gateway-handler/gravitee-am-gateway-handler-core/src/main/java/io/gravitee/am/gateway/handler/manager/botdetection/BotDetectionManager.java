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
package io.gravitee.am.gateway.handler.manager.botdetection;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.botdetection.api.BotDetectionContext;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.common.service.Service;
import io.reactivex.Single;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Eric LELEU (eric.leleu at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface BotDetectionManager extends Service {

    Map<String, Object> getTemplateVariables(Domain domain, Client client);

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.validate_migrated(context))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<Boolean> validate(BotDetectionContext context) {
    return RxJava2Adapter.monoToSingle(validate_migrated(context));
}
default Mono<Boolean> validate_migrated(BotDetectionContext context) {
    return RxJava2Adapter.singleToMono(validate(context));
}
}

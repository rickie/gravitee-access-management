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
package io.gravitee.am.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.model.uma.PermissionRequest;
import io.gravitee.am.model.uma.PermissionTicket;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface PermissionTicketService {

      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(requestedPermission, domain, client))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Single<PermissionTicket> create(List<PermissionRequest> requestedPermission, String domain, String client) {
    return RxJava2Adapter.monoToSingle(create_migrated(requestedPermission, domain, client));
}
default Mono<PermissionTicket> create_migrated(List<PermissionRequest> requestedPermission, String domain, String client) {
    return RxJava2Adapter.singleToMono(create(requestedPermission, domain, client));
}
      @InlineMe(replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Maybe<PermissionTicket> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default Mono<PermissionTicket> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}
      
Mono<PermissionTicket> remove_migrated(String id);
}

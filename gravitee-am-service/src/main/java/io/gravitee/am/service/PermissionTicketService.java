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

import io.gravitee.am.model.uma.PermissionRequest;
import io.gravitee.am.model.uma.PermissionTicket;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import reactor.adapter.rxjava.RxJava2Adapter;

/**
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public interface PermissionTicketService {

      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.uma.PermissionTicket> create(java.util.List<io.gravitee.am.model.uma.PermissionRequest> requestedPermission, java.lang.String domain, java.lang.String client) {
    return RxJava2Adapter.monoToSingle(create_migrated(requestedPermission, domain, client));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.PermissionTicket> create_migrated(List<PermissionRequest> requestedPermission, String domain, String client) {
    return RxJava2Adapter.singleToMono(create(requestedPermission, domain, client));
}
      @Deprecated  
default io.reactivex.Maybe<io.gravitee.am.model.uma.PermissionTicket> findById(java.lang.String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.PermissionTicket> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
}
      @Deprecated  
default io.reactivex.Single<io.gravitee.am.model.uma.PermissionTicket> remove(java.lang.String id) {
    return RxJava2Adapter.monoToSingle(remove_migrated(id));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.uma.PermissionTicket> remove_migrated(String id) {
    return RxJava2Adapter.singleToMono(remove(id));
}
}

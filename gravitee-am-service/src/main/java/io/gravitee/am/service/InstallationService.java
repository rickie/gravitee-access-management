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
import io.gravitee.am.model.Installation;
import io.reactivex.Completable;

import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface InstallationService {

    /**
     * Get the current installation.
     *
     * @return the current installation or an {@link io.gravitee.am.service.exception.InstallationNotFoundException} exception.
     */
      
Mono<Installation> get_migrated();


    /**
     * Get or initialize the installation. A new installation will be created only if none exists.
     *
     * @return the created or already existing installation.
     */
      
Mono<Installation> getOrInitialize_migrated();

    /**
     * Set additional information of the current installation.
     *
     * @param additionalInformation the list of additional information to set on the existing installation.
     *
     * @return the updated installation
     */
      
Mono<Installation> setAdditionalInformation_migrated(Map<String, String> additionalInformation);

    /**
     * Add or update the additional information of the current installation.
     *
     * @param additionalInformation the list of additional information to add or update on the existing installation.
     *
     * @return the updated installation.
     */
      
Mono<Installation> addAdditionalInformation_migrated(Map<String, String> additionalInformation);

    /**
     * Delete the current installation.
     *
     * @return the operation status
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default Completable delete() {
    return RxJava2Adapter.monoToCompletable(delete_migrated());
}
default Mono<Void> delete_migrated() {
    return RxJava2Adapter.completableToMono(delete());
}
}

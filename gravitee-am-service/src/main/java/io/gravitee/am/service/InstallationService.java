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
import io.reactivex.Single;
import java.util.Map;
import reactor.adapter.rxjava.RxJava2Adapter;

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
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.get_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Installation> get() {
    return RxJava2Adapter.monoToSingle(get_migrated());
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Installation> get_migrated() {
    return RxJava2Adapter.singleToMono(get());
}


    /**
     * Get or initialize the installation. A new installation will be created only if none exists.
     *
     * @return the created or already existing installation.
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getOrInitialize_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Installation> getOrInitialize() {
    return RxJava2Adapter.monoToSingle(getOrInitialize_migrated());
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Installation> getOrInitialize_migrated() {
    return RxJava2Adapter.singleToMono(getOrInitialize());
}

    /**
     * Set additional information of the current installation.
     *
     * @param additionalInformation the list of additional information to set on the existing installation.
     *
     * @return the updated installation
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.setAdditionalInformation_migrated(additionalInformation))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Installation> setAdditionalInformation(java.util.Map<java.lang.String, java.lang.String> additionalInformation) {
    return RxJava2Adapter.monoToSingle(setAdditionalInformation_migrated(additionalInformation));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Installation> setAdditionalInformation_migrated(Map<String, String> additionalInformation) {
    return RxJava2Adapter.singleToMono(setAdditionalInformation(additionalInformation));
}

    /**
     * Add or update the additional information of the current installation.
     *
     * @param additionalInformation the list of additional information to add or update on the existing installation.
     *
     * @return the updated installation.
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.addAdditionalInformation_migrated(additionalInformation))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Single<io.gravitee.am.model.Installation> addAdditionalInformation(java.util.Map<java.lang.String, java.lang.String> additionalInformation) {
    return RxJava2Adapter.monoToSingle(addAdditionalInformation_migrated(additionalInformation));
}
default reactor.core.publisher.Mono<io.gravitee.am.model.Installation> addAdditionalInformation_migrated(Map<String, String> additionalInformation) {
    return RxJava2Adapter.singleToMono(addAdditionalInformation(additionalInformation));
}

    /**
     * Delete the current installation.
     *
     * @return the operation status
     */
      @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated  
default io.reactivex.Completable delete() {
    return RxJava2Adapter.monoToCompletable(delete_migrated());
}
default reactor.core.publisher.Mono<java.lang.Void> delete_migrated() {
    return RxJava2Adapter.completableToMono(delete());
}
}

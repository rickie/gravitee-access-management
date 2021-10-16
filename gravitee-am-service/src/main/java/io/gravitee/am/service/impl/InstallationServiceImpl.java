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
package io.gravitee.am.service.impl;

import static io.gravitee.am.model.Installation.COCKPIT_INSTALLATION_STATUS;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.common.utils.RandomString;
import io.gravitee.am.model.Installation;
import io.gravitee.am.repository.management.api.InstallationRepository;
import io.gravitee.am.service.*;
import io.gravitee.am.service.exception.InstallationNotFoundException;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class InstallationServiceImpl implements InstallationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallationServiceImpl.class);

    private final InstallationRepository installationRepository;

    public InstallationServiceImpl(@Lazy InstallationRepository installationRepository) {
        this.installationRepository = installationRepository;
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.get_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Installation> get() {
 return RxJava2Adapter.monoToSingle(get_migrated());
}
@Override
    public Mono<Installation> get_migrated() {
        return this.installationRepository.find_migrated().switchIfEmpty(Mono.error(new InstallationNotFoundException()));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getOrInitialize_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Installation> getOrInitialize() {
 return RxJava2Adapter.monoToSingle(getOrInitialize_migrated());
}
@Override
    public Mono<Installation> getOrInitialize_migrated() {
        return this.installationRepository.find_migrated().switchIfEmpty(createInternal_migrated());
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.setAdditionalInformation_migrated(additionalInformation))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Installation> setAdditionalInformation(Map<String, String> additionalInformation) {
 return RxJava2Adapter.monoToSingle(setAdditionalInformation_migrated(additionalInformation));
}
@Override
    public Mono<Installation> setAdditionalInformation_migrated(Map<String, String> additionalInformation) {
        return get_migrated().flatMap(v->RxJava2Adapter.singleToMono((Single<Installation>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Installation, Single<Installation>>)installation -> {
                    Installation toUpdate = new Installation(installation);
                    toUpdate.setAdditionalInformation(additionalInformation);

                    return RxJava2Adapter.monoToSingle(updateInternal_migrated(toUpdate));
                }).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.addAdditionalInformation_migrated(additionalInformation))", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Installation> addAdditionalInformation(Map<String, String> additionalInformation) {
 return RxJava2Adapter.monoToSingle(addAdditionalInformation_migrated(additionalInformation));
}
@Override
    public Mono<Installation> addAdditionalInformation_migrated(Map<String, String> additionalInformation) {
        return getOrInitialize_migrated().doOnSuccess(RxJavaReactorMigrationUtil.toJdkConsumer(installation -> installation.getAdditionalInformation().putAll(additionalInformation))).flatMap(v->RxJava2Adapter.singleToMono((Single<Installation>)RxJavaReactorMigrationUtil.toJdkFunction((Function<Installation, Single<Installation>>)(io.gravitee.am.model.Installation ident) -> RxJava2Adapter.monoToSingle(updateInternal_migrated(ident))).apply(v)));
    }

    @InlineMe(replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Completable delete() {
 return RxJava2Adapter.monoToCompletable(delete_migrated());
}
@Override
    public Mono<Void> delete_migrated() {
        return this.installationRepository.find_migrated().flatMap(installation->installationRepository.delete_migrated(installation.getId())).then();
    }

    
private Mono<Installation> createInternal_migrated() {

        final Date now = Date.from(Instant.now());
        final Installation installation = new Installation();
        installation.setId(RandomString.generate());
        installation.setCreatedAt(now);
        installation.setUpdatedAt(now);
        installation.setAdditionalInformation(new HashMap<>());

        return installationRepository.create_migrated(installation);
    }

    
private Mono<Installation> updateInternal_migrated(Installation toUpdate) {

        final Date now = Date.from(Instant.now());
        toUpdate.setUpdatedAt(now);

        return installationRepository.update_migrated(toUpdate);
    }
}

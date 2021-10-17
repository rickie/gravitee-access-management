/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.service;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.identityprovider.api.User;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.application.ApplicationType;
import io.gravitee.am.model.common.Page;
import io.gravitee.am.service.model.NewApplication;
import io.gravitee.am.service.model.PatchApplication;
import io.gravitee.am.service.model.TopApplication;
import io.reactivex.Completable;

import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ApplicationService {

  Mono<Page<Application>> findAll_migrated(int page, int size);

  @InlineMe(
      replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain, page, size))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Page<Application>> findByDomain(String domain, int page, int size) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain, page, size));
  }

  default Mono<Page<Application>> findByDomain_migrated(String domain, int page, int size) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, page, size));
  }

  Mono<Page<Application>> search_migrated(String domain, String query, int page, int size);

  Flux<Application> findByCertificate_migrated(String certificate);

  Flux<Application> findByIdentityProvider_migrated(String identityProvider);

  Flux<Application> findByFactor_migrated(String factor);

  Mono<Set<Application>> findByDomainAndExtensionGrant_migrated(
      String domain, String extensionGrant);

  Flux<Application> findByIdIn_migrated(List<String> ids);

  @InlineMe(
      replacement = "RxJava2Adapter.monoToMaybe(this.findById_migrated(id))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Maybe<Application> findById(String id) {
    return RxJava2Adapter.monoToMaybe(findById_migrated(id));
  }

  default Mono<Application> findById_migrated(String id) {
    return RxJava2Adapter.maybeToMono(findById(id));
  }

  Mono<Application> findByDomainAndClientId_migrated(String domain, String clientId);

  @InlineMe(
      replacement =
          "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newApplication, principal))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Application> create(String domain, NewApplication newApplication, User principal) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newApplication, principal));
  }

  default Mono<Application> create_migrated(
      String domain, NewApplication newApplication, User principal) {
    return RxJava2Adapter.singleToMono(create(domain, newApplication, principal));
  }

  @InlineMe(
      replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(application))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Application> create(Application application) {
    return RxJava2Adapter.monoToSingle(create_migrated(application));
  }

  default Mono<Application> create_migrated(Application application) {
    return RxJava2Adapter.singleToMono(create(application));
  }

  @InlineMe(
      replacement = "RxJava2Adapter.monoToSingle(this.update_migrated(application))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Application> update(Application application) {
    return RxJava2Adapter.monoToSingle(update_migrated(application));
  }

  default Mono<Application> update_migrated(Application application) {
    return RxJava2Adapter.singleToMono(update(application));
  }

  Mono<Application> updateType_migrated(
      String domain, String id, ApplicationType type, User principal);

  @InlineMe(
      replacement =
          "RxJava2Adapter.monoToSingle(this.patch_migrated(domain, id, patchApplication, principal))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Application> patch(
      String domain, String id, PatchApplication patchApplication, User principal) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domain, id, patchApplication, principal));
  }

  default Mono<Application> patch_migrated(
      String domain, String id, PatchApplication patchApplication, User principal) {
    return RxJava2Adapter.singleToMono(patch(domain, id, patchApplication, principal));
  }

  Mono<Application> renewClientSecret_migrated(String domain, String id, User principal);

  @InlineMe(
      replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id, principal))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Completable delete(String id, User principal) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id, principal));
  }

  default Mono<Void> delete_migrated(String id, User principal) {
    return RxJava2Adapter.completableToMono(delete(id, principal));
  }

  Mono<Long> count_migrated();

  Mono<Long> countByDomain_migrated(String domainId);

  Mono<Set<TopApplication>> findTopApplications_migrated();

  @InlineMe(
      replacement =
          "RxJava2Adapter.monoToSingle(this.findTopApplicationsByDomain_migrated(domain))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Set<TopApplication>> findTopApplicationsByDomain(String domain) {
    return RxJava2Adapter.monoToSingle(findTopApplicationsByDomain_migrated(domain));
  }

  default Mono<Set<TopApplication>> findTopApplicationsByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(findTopApplicationsByDomain(domain));
  }

  @InlineMe(
      replacement = "RxJava2Adapter.monoToSingle(this.findAll_migrated())",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Set<Application>> findAll() {
    return RxJava2Adapter.monoToSingle(findAll_migrated());
  }

  default Mono<Set<Application>> findAll_migrated() {
    return findAll_migrated(0, Integer.MAX_VALUE)
        .map(
            RxJavaReactorMigrationUtil.toJdkFunction(
                pagedApplications ->
                    (pagedApplications.getData() == null)
                        ? Collections.emptySet()
                        : new HashSet<>(pagedApplications.getData())));
  }

  @InlineMe(
      replacement = "RxJava2Adapter.monoToSingle(this.findByDomain_migrated(domain))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Set<Application>> findByDomain(String domain) {
    return RxJava2Adapter.monoToSingle(findByDomain_migrated(domain));
  }

  default Mono<Set<Application>> findByDomain_migrated(String domain) {
    return RxJava2Adapter.singleToMono(findByDomain(domain, 0, Integer.MAX_VALUE))
        .map(
            RxJavaReactorMigrationUtil.toJdkFunction(
                pagedApplications ->
                    (pagedApplications.getData() == null)
                        ? Collections.emptySet()
                        : new HashSet<>(pagedApplications.getData())));
  }

  @InlineMe(
      replacement = "RxJava2Adapter.monoToSingle(this.create_migrated(domain, newApplication))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Application> create(String domain, NewApplication newApplication) {
    return RxJava2Adapter.monoToSingle(create_migrated(domain, newApplication));
  }

  default Mono<Application> create_migrated(String domain, NewApplication newApplication) {
    return RxJava2Adapter.singleToMono(create(domain, newApplication, null));
  }

  @InlineMe(
      replacement =
          "RxJava2Adapter.monoToSingle(this.patch_migrated(domain, id, patchApplication))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Single<Application> patch(String domain, String id, PatchApplication patchApplication) {
    return RxJava2Adapter.monoToSingle(patch_migrated(domain, id, patchApplication));
  }

  default Mono<Application> patch_migrated(
      String domain, String id, PatchApplication patchApplication) {
    return RxJava2Adapter.singleToMono(patch(domain, id, patchApplication, null));
  }

  Mono<Application> renewClientSecret_migrated(String domain, String id);

  @InlineMe(
      replacement = "RxJava2Adapter.monoToCompletable(this.delete_migrated(id))",
      imports = "reactor.adapter.rxjava.RxJava2Adapter")
  @Deprecated
  default Completable delete(String id) {
    return RxJava2Adapter.monoToCompletable(delete_migrated(id));
  }

  default Mono<Void> delete_migrated(String id) {
    return RxJava2Adapter.completableToMono(delete(id, null));
  }
}

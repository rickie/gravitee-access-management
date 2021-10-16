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
package io.gravitee.am.repository.healthcheck;

import io.gravitee.am.repository.oauth2.api.AccessTokenRepository;
import io.gravitee.node.api.healthcheck.Probe;
import io.gravitee.node.api.healthcheck.Result;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import tech.picnic.errorprone.migration.util.RxJavaReactorMigrationUtil;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class OAuth2RepositoryProbe implements Probe {

    private static final String TOKEN = "token";

    @Lazy
    @Autowired
    private AccessTokenRepository accessTokenRepository;

    @Override
    public String id() {
        return "oauth2-repository";
    }

    @Override
    public CompletableFuture<Result> check() {
        // Search for an oauth 2.0 token to check repository connection
        final CompletableFuture<Result> future = new CompletableFuture<>();

        accessTokenRepository.findByToken_migrated(TOKEN).subscribe(RxJavaReactorMigrationUtil.toJdkConsumer(domain -> future.complete(Result.healthy())), RxJavaReactorMigrationUtil.toJdkConsumer(error -> future.complete(Result.unhealthy(error))), RxJavaReactorMigrationUtil.toRunnable(() -> future.complete(Result.healthy()))); // repository is up but returned no result

        return future;
    }
}

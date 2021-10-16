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
package io.gravitee.am.gateway.handler.oauth2.service.scope.impl;

import com.google.errorprone.annotations.InlineMe;
import io.gravitee.am.gateway.handler.oauth2.service.scope.ScopeManager;
import io.gravitee.am.gateway.handler.oauth2.service.scope.ScopeService;
import io.gravitee.am.model.oauth2.Scope;
import io.reactivex.Single;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ScopeServiceImpl implements ScopeService {

    @Autowired
    private ScopeManager scopeManager;

    @InlineMe(replacement = "RxJava2Adapter.monoToSingle(this.getAll_migrated())", imports = "reactor.adapter.rxjava.RxJava2Adapter")
@Deprecated
@Override
    public Single<Set<Scope>> getAll() {
 return RxJava2Adapter.monoToSingle(getAll_migrated());
}
@Override
    public Mono<Set<Scope>> getAll_migrated() {
        return Mono.just(scopeManager.findAll());
    }

    @Override
    public Scope findByKey(String key) {
        return scopeManager.findByKey(key);
    }

    @Override
    public List<String> getDiscoveryScope() {
        return scopeManager.findAll()
                .stream()
                .filter(Scope::isDiscovery)
                .map(Scope::getKey)
                .sorted()
                .collect(Collectors.toList());
    }
}

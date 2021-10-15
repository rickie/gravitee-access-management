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
package io.gravitee.am.gateway.handler.common.client.impl;

import io.gravitee.am.gateway.handler.common.client.ClientManager;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oidc.Client;
import io.reactivex.Maybe;
import io.reactivex.Single;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author Alexandre FARIA (contact at alexandrefaria.net)
 * @author GraviteeSource Team
 */
public class ClientSyncServiceImpl implements ClientSyncService {

    @Autowired
    private Domain domain;

    @Autowired
    private ClientManager clientManager;

    @Override
    public Maybe<Client> findById(String id) {
        final Client client = clientManager.get(id);
        return client != null ? RxJava2Adapter.monoToMaybe(Mono.just(client)) : RxJava2Adapter.monoToMaybe(Mono.empty());
    }

    @Override
    public Maybe<Client> findByClientId(String clientId) {
        return findByDomainAndClientId(domain.getId(), clientId);
    }

    @Override
    public Maybe<Client> findByDomainAndClientId(String domain, String clientId) {
        final Optional<Client> optClient = clientManager.entities()
                .stream()
                .filter(client -> !client.isTemplate() && client.getDomain().equals(domain) && client.getClientId().equals(clientId))
                .findFirst();
        return optClient.isPresent() ? RxJava2Adapter.monoToMaybe(Mono.just(optClient.get())) : RxJava2Adapter.monoToMaybe(Mono.empty());
    }

    @Override
    public Single<List<Client>> findTemplates() {
        final List<Client> templates = clientManager.entities()
                .stream()
                .filter(client -> client.isTemplate() && client.getDomain().equals(domain.getId()))
                .collect(Collectors.toList());
        return RxJava2Adapter.monoToSingle(Mono.just(templates));
    }

    @Override
    public Client addDynamicClientRegistred(Client client) {
        clientManager.deploy(client);
        return client;
    }

    @Override
    public Client removeDynamicClientRegistred(Client client) {
        clientManager.undeploy(client.getId());
        return client;
    }
}

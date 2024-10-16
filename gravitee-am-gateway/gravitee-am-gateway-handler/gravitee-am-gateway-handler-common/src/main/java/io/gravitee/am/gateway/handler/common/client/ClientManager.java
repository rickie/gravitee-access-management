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
package io.gravitee.am.gateway.handler.common.client;

import io.gravitee.am.model.Domain;
import io.gravitee.am.model.oidc.Client;
import io.gravitee.common.service.Service;

import java.util.Collection;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ClientManager extends Service {

    /**
     * Deploy an client
     *
     * @param client client to deploy.
     */
    void deploy(Client client);

    /**
     * Undeploy a client
     *
     * @param clientId The ID of the client to undeploy.
     */
    void undeploy(String clientId);

    /**
     * Returns a collection of deployed clients.
     *
     * @return A collection of deployed clients.
     */
    Collection<Client> entities();

    /**
     * Retrieve a deployed client using its ID.
     *
     * @param clientId The ID of the deployed client.
     * @return A deployed client
     */
    Client get(String clientId);

    void deployCrossDomain(Domain domain);

    void undeployCrossDomain(Domain domain);
}

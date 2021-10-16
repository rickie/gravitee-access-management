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
package io.gravitee.am.management.service.impl.commands;

import static io.gravitee.am.management.service.impl.commands.GoodbyeCommandHandler.DELETED_STATUS;
import static io.gravitee.am.model.Installation.COCKPIT_INSTALLATION_STATUS;
import static org.mockito.Mockito.*;

import io.gravitee.am.model.Installation;
import io.gravitee.am.service.InstallationService;
import io.gravitee.cockpit.api.command.Command;
import io.gravitee.cockpit.api.command.CommandStatus;
import io.gravitee.cockpit.api.command.goodbye.GoodbyeCommand;
import io.gravitee.cockpit.api.command.goodbye.GoodbyeReply;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class GoodbyeCommandHandlerTest extends TestCase {

    @Mock
    private InstallationService installationService;

    public GoodbyeCommandHandler cut;

    @Before
    public void before() {
        cut = new GoodbyeCommandHandler(installationService);
    }

    @Test
    public void handleType() {
        assertEquals(Command.Type.GOODBYE_COMMAND, cut.handleType());
    }

    @Test
    public void handle() {
        GoodbyeCommand command = new GoodbyeCommand();
        final Installation installation = new Installation();
        when(installationService.addAdditionalInformation_migrated(any(Map.class))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.just(installation))));

        TestObserver<GoodbyeReply> obs = cut.handle(command).test();

        obs.awaitTerminalEvent();
        obs.assertValue(reply -> reply.getCommandId().equals(command.getId()) && reply.getCommandStatus() == CommandStatus.SUCCEEDED);

        final ArgumentCaptor<Map<String, String>> expectedAdditionalInfos = ArgumentCaptor.forClass(Map.class);
        verify(installationService, times(1)).addAdditionalInformation_migrated(expectedAdditionalInfos.capture());

        assertEquals(DELETED_STATUS, expectedAdditionalInfos.getValue().get(COCKPIT_INSTALLATION_STATUS));
    }

    @Test
    public void handleWithException() {
        GoodbyeCommand command = new GoodbyeCommand();

        when(installationService.addAdditionalInformation_migrated(any(Map.class))).thenReturn(RxJava2Adapter.singleToMono(RxJava2Adapter.monoToSingle(Mono.error(new RuntimeException("Unexpected error")))));

        TestObserver<GoodbyeReply> obs = cut.handle(command).test();

        obs.awaitTerminalEvent();
        obs.assertValue(reply -> reply.getCommandId().equals(command.getId()) && reply.getCommandStatus() == CommandStatus.ERROR);
    }
}

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











import io.gravitee.am.service.MembershipService;
import io.gravitee.am.service.OrganizationUserService;
import io.gravitee.am.service.RoleService;


import io.gravitee.cockpit.api.command.Command;
import io.gravitee.cockpit.api.command.CommandHandler;

import io.gravitee.cockpit.api.command.membership.MembershipCommand;

import io.gravitee.cockpit.api.command.membership.MembershipReply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;




/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@Component
public class MembershipCommandHandler implements CommandHandler<MembershipCommand, MembershipReply> {

    private final Logger logger = LoggerFactory.getLogger(MembershipCommandHandler.class);

    private final OrganizationUserService userService;
    private final RoleService roleService;
    private final MembershipService membershipService;

    public MembershipCommandHandler(OrganizationUserService userService,
                                    RoleService roleService,
                                    MembershipService membershipService) {
        this.userService = userService;
        this.roleService = roleService;
        this.membershipService = membershipService;
    }

    @Override
    public Command.Type handleType() {
        return Command.Type.MEMBERSHIP_COMMAND;
    }

    


    

}